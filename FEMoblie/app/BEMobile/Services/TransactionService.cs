using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount;
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
        private readonly IBudgetService _budgetService;


        public TransactionService(AppDbContext context, INotificationService notificationService, IBudgetService budgetService)
        {
            _context = context;
            _notificationService = notificationService;
            _budgetService = budgetService;
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

        private async Task<string?> FindMatchingBudgetIdAsync(string userId, string categoryId, string transactionCreatedDate)
        {
            if (string.IsNullOrWhiteSpace(transactionCreatedDate))
                return null;

            var transParts = transactionCreatedDate.Split(' ')[0].Split('/');
            if (transParts.Length < 3) return null;

            string transMonth = transParts[1];
            string transYear = transParts[2];

            var budgets = await _context.Budgets
                .Where(b => b.UserId == userId && b.CategoryId == categoryId)
                .ToListAsync();

            var matchedBudget = budgets.FirstOrDefault(b =>
            {
                if (string.IsNullOrWhiteSpace(b.CreatedDate))
                    return false;

                var parts = b.CreatedDate.Split(' ')[0].Split('/');
                if (parts.Length < 3)
                    return false;

                return parts[1] == transMonth && parts[2] == transYear;
            });

            return matchedBudget?.BudgetId;
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

            if (dto.Type != "Income" && dto.Type != "Expense")
            {
                return new CreateTransactionResponse
                {
                    Success = false,
                    Message = "Loại giao dịch không hợp lệ"
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

            if(dto.Type == "Expense")
            {
                var budgetId = await FindMatchingBudgetIdAsync(dto.UserId, dto.CategoryId, transaction.CreatedDate);

                if (budgetId != null)
                {
                    await _budgetService.UpdateCurrentAmountByUserIdAsync(new UpdateAmountRequest
                    {
                        BudgetId = budgetId,
                        UserId = dto.UserId,
                        UpdateAmount = dto.Amount,
                        isAddAmount = dto.Type == "Expense" ? true : false
                    });
                }
            }    

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

            if (dto.Type != "Income" && dto.Type != "Expense")
            {
                return new UpdateTransactionResponse
                {
                    Success = false,
                    Message = "Loại giao dịch không hợp lệ"
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

            var isIncreased = newAmount > oldAmount ? true : false;

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

            if (dto.Type == "Expense")
            {
                var budgetId = await FindMatchingBudgetIdAsync(dto.UserId, dto.CategoryId, existing.CreatedDate);

                if (budgetId != null)
                {
                    decimal diff = Math.Abs(newAmount - oldAmount);


                    await _budgetService.UpdateCurrentAmountByUserIdAsync(new UpdateAmountRequest
                    {
                        BudgetId = budgetId,
                        UserId = dto.UserId,
                        UpdateAmount = diff.ToString(),
                        isAddAmount = isIncreased
                    });
                }
            }    

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

            if (transaction.Type == "Expense")
            {
                var budgetId = await FindMatchingBudgetIdAsync(transaction.UserId, transaction.CategoryId, transaction.CreatedDate);

                if (budgetId != null)
                {
                    await _budgetService.UpdateCurrentAmountByUserIdAsync(new UpdateAmountRequest
                    {
                        BudgetId = budgetId,
                        UserId = transaction.UserId,
                        UpdateAmount = transaction.Amount,
                        isAddAmount = false
                    });
                }
            } 
                

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = transaction.UserId,
                Content = "Bạn đã xóa một giao dịch! Số dư và ngân sách của bạn đã được cập nhật."
            });

            return new DeleteTransactionResponse
            {
                Success = true,
                Message = "Xóa giao dịch thành công và đã cập nhật ngân sách"
            };
        }


    }
}
