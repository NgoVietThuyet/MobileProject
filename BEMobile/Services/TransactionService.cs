using BEMobile.Connectors;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using BEMobile.Models.RequestResponse.TransactionRR.CreateTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.DeleteTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.GetAllTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.UpdateTransaction;
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
        private readonly INeo4jConnector _neo4jConnector;

        public TransactionService(AppDbContext context, INotificationService notificationService, INeo4jConnector neo4J)
        {
            _context = context;
            _notificationService = notificationService;
            _neo4jConnector = neo4J;
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
            string a= "0";
            try
            {
                await _neo4jConnector.AddTransactionAsync(dto);
                a = "10";
            }
            catch (Exception ex)
            {
                Console.WriteLine($"lỗi Neo4j: {ex.Message}");
            }

            return new CreateTransactionResponse
            {
                Success = true,
                Message = "Tạo giao dịch thành công" + a,
                Transaction = dto
            };
        }

        public async Task<UpdateTransactionResponse> UpdateTransactionAsync(UpdateTransactionRequest request)
        {
            var dto = request.Transaction;
            var existing = await _context.Transactions.FirstOrDefaultAsync(transaction => transaction.TransactionId == dto.TransactionId);

            if (existing == null)
            {
                return new UpdateTransactionResponse
                {
                    Success = false,
                    Message = $"Không tìm thấy giao dịch ID = {dto.TransactionId}"
                };
            }

            existing.CategoryId = dto.CategoryId;
            existing.Type = dto.Type;
            existing.Amount = dto.Amount;
            existing.Note = dto.Note;
            existing.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

            await _context.SaveChangesAsync();

            // creating notification
            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = dto.UserId,
                Content = "Bạn đã sửa một giao dịch!"
            });

            await _neo4jConnector.UpdateTransactionAsync(dto);

            return new UpdateTransactionResponse
            {
                Success = true,
                Message = "Cập nhật giao dịch thành công",
                UpdatedTransaction = dto
            };
        }

        public async Task<DeleteTransactionResponse> DeleteTransactionAsync(DeleteTransactionRequest request)
        {
            var transaction = await _context.Transactions.FirstOrDefaultAsync(transaction => transaction.TransactionId == request.TransactionId);
            if (transaction == null)
            {
                return new DeleteTransactionResponse
                {
                    Success = false,
                    Message = $"Không tìm thấy giao dịch ID = {request.TransactionId}"
                };
            }

            _context.Transactions.Remove(transaction);
            await _context.SaveChangesAsync();
            
            await _neo4jConnector.DeleteTransactionAsync(new TransactionDto
            {
                TransactionId = transaction.TransactionId,
                UserId = transaction.UserId,
                CategoryId = transaction.CategoryId,
                Type = transaction.Type,
                Amount = transaction.Amount,
                Note = transaction.Note,
                CreatedDate = transaction.CreatedDate,
                UpdatedDate = transaction.UpdatedDate
            });

            return new DeleteTransactionResponse
            {
                Success = true,
                Message = "Xóa giao dịch thành công"
            };
        }
    }
}
