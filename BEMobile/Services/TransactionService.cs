using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Notification.PushNotification;
using BEMobile.Models.RequestResponse.Transaction.CreateTransaction;
using BEMobile.Models.RequestResponse.Transaction.DeleteTransaction;
using BEMobile.Models.RequestResponse.Transaction.GetAllTransaction;
using BEMobile.Models.RequestResponse.Transaction.UpdateTransaction;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface ITransactionService
    {
        Task<IEnumerable<TransactionDto>> GetAllTransactionsAsync(string userId);
        Task<CreateTransactionResponse> CreateTransactionAsync(CreateTransactionRequest request);
        Task<UpdateTransactionResponse> UpdateTransactionAsync(UpdateTransactionRequest request);
        Task<DeleteTransactionResponse> DeleteTransactionAsync(DeleteTransactionRequest request);
    }

    public class TransactionService : ITransactionService
    {
        private readonly AppDbContext _context;
        private readonly INotificationService _notificationService;

        public TransactionService(AppDbContext context, INotificationService notificationService)
        {
            _context = context;
            _notificationService = notificationService;
        }

        public async Task<IEnumerable<TransactionDto>> GetAllTransactionsAsync(string userId)
        {
            return await _context.Transactions
                .Where(transaction => transaction.UserId == userId)
                .OrderByDescending(transaction => transaction.CreatedDate)
                .Select(transaction => new TransactionDto
                {
                    TransactionId = transaction.TransactionId,
                    UserId = transaction.UserId,
                    CategoryId = transaction.CategoryId,
                    Type = transaction.Type,
                    Amount = transaction.Amount,
                    Note = transaction.Note,
                    CreatedDate = transaction.CreatedDate,
                    UpdatedDate = transaction.UpdatedDate
                })
                .ToListAsync();
        }



        public async Task<CreateTransactionResponse> CreateTransactionAsync(CreateTransactionRequest request)
        {
            var dto = request.Transaction;

            if (!decimal.TryParse(dto.Amount, out decimal amountValue))
            {
                return new CreateTransactionResponse
                {
                    Success = false,
                    Message = "Số tiền không hợp lệ (vui lòng nhập số)"
                };
            }

            if (amountValue <= 0)
            {
                return new CreateTransactionResponse
                {
                    Success = false,
                    Message = "Số tiền phải lớn hơn 0"
                };
            }


            var account = await _context.Accounts
                .FirstOrDefaultAsync(acc => acc.UserId == dto.UserId);

            if (account == null)
            {
                return new CreateTransactionResponse
                {
                    Success = false,
                    Message = "Không tìm thấy tài khoản của người dùng"
                };
            }

            //  Balance from string to decimal
            decimal currentBalance = 0;
            if (!string.IsNullOrEmpty(account.Balance))
                decimal.TryParse(account.Balance, out currentBalance);

            if (dto.Type == "Income")
                currentBalance += amountValue;
            else if (dto.Type == "Expense")
                currentBalance -= amountValue;

            if (currentBalance < 0)
            {
                return new CreateTransactionResponse
                {
                    Success = false,
                    Message = "Số dư không đủ để thực hiện giao dịch này"
                };
            }

            account.Balance = currentBalance.ToString("0.##");
            account.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

            var transaction = new Transaction
            {
                TransactionId = Guid.NewGuid().ToString(),
                UserId = dto.UserId,
                CategoryId = dto.CategoryId,
                Type = dto.Type,
                Amount = dto.Amount,
                Note = dto.Note,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };

            _context.Transactions.Add(transaction);
            await _context.SaveChangesAsync();

            // creating notification
            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = dto.UserId,
                Content = "Bạn đã thêm một giao dịch mới!"
            });

            dto.TransactionId = transaction.TransactionId;
            dto.CreatedDate = transaction.CreatedDate;

            return new CreateTransactionResponse
            {
                Success = true,
                Message = "Tạo giao dịch thành công",
                Transaction = dto
            };
        }

        public async Task<UpdateTransactionResponse> UpdateTransactionAsync(UpdateTransactionRequest request)
        {
            var dto = request.Transaction;
            var existing = await _context.Transactions
                .FirstOrDefaultAsync(t => t.TransactionId == dto.TransactionId);

            if (existing == null)
            {
                return new UpdateTransactionResponse
                {
                    Success = false,
                    Message = $"Không tìm thấy giao dịch ID = {dto.TransactionId}"
                };
            }

            var account = await _context.Accounts.FirstOrDefaultAsync(acc => acc.UserId == dto.UserId);
            if (account == null)
            {
                return new UpdateTransactionResponse
                {
                    Success = false,
                    Message = "Không tìm thấy tài khoản người dùng"
                };
            }

            decimal.TryParse(account.Balance, out decimal currentBalance);

            decimal oldAmount = decimal.TryParse(existing.Amount, out decimal tempOld) ? tempOld : 0;
            decimal newAmount = decimal.TryParse(dto.Amount, out decimal tempNew) ? tempNew : 0;

            if (existing.Type == "Income")
                currentBalance -= oldAmount;
            else if (existing.Type == "Expense")
                currentBalance += oldAmount;

            if (dto.Type == "Income")
                currentBalance += newAmount;
            else if (dto.Type == "Expense")
                currentBalance -= newAmount;

            if (currentBalance < 0)
            {
                return new UpdateTransactionResponse
                {
                    Success = false,
                    Message = "Số dư không đủ sau khi cập nhật giao dịch"
                };
            }

            account.Balance = currentBalance.ToString("0.##");
            account.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

            existing.CategoryId = dto.CategoryId;
            existing.Type = dto.Type;
            existing.Amount = dto.Amount;
            existing.Note = dto.Note;
            existing.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

            await _context.SaveChangesAsync();

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = dto.UserId,
                Content = "Bạn đã sửa một giao dịch và số dư đã được cập nhật!"
            });

            return new UpdateTransactionResponse
            {
                Success = true,
                Message = "Cập nhật giao dịch thành công",
                UpdatedTransaction = dto
            };
        }

        public async Task<DeleteTransactionResponse> DeleteTransactionAsync(DeleteTransactionRequest request)
        {
            var transaction = await _context.Transactions
                .FirstOrDefaultAsync(t => t.TransactionId == request.TransactionId);

            if (transaction == null)
            {
                return new DeleteTransactionResponse
                {
                    Success = false,
                    Message = $"Không tìm thấy giao dịch ID = {request.TransactionId}"
                };
            }

            var account = await _context.Accounts.FirstOrDefaultAsync(acc => acc.UserId == transaction.UserId);
            if (account == null)
            {
                return new DeleteTransactionResponse
                {
                    Success = false,
                    Message = "Không tìm thấy tài khoản người dùng"
                };
            }

            decimal.TryParse(account.Balance, out decimal currentBalance);
            decimal.TryParse(transaction.Amount, out decimal amount);

            if (transaction.Type == "Income")
                currentBalance -= amount;
            else if (transaction.Type == "Expense")
                currentBalance += amount;

            if (currentBalance < 0)
                currentBalance = 0;

            account.Balance = currentBalance.ToString("0.##");
            account.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

            _context.Transactions.Remove(transaction);
            await _context.SaveChangesAsync();

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = transaction.UserId,
                Content = "Bạn đã xóa một giao dịch! Số dư của bạn đã được cập nhật."
            });

            return new DeleteTransactionResponse
            {
                Success = true,
                Message = "Xóa giao dịch thành công và số dư đã được cập nhật"
            };
        }

    }
}
