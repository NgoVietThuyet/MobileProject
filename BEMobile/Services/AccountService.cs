using BEMobile.Data.Entities;
using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;
using BEMobile.Models.RequestResponse.AccountRR.DeleteAccount;
using BEMobile.Models.RequestResponse.AccountRR.DetailAccount;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface IAccountService
    {
        Task<CreateAccountResponse> CreateAccountAsync(CreateAccountRequest req);
        Task<DetailAccountResponse?> GetAccountByUserIdAsync(DetailAccountRequest req);
        Task<DeleteAccountResponse> DeleteAccountAsync(DeleteAccountRequest req);
        Task UpdateBalanceAsync(string userId, long amount, bool isIncrease);
    }

    public class AccountService : IAccountService
    {
        private readonly AppDbContext _db;
        private readonly INotificationService _notificationService;

        public AccountService(AppDbContext db, INotificationService notificationService)
        {
            _db = db;
            _notificationService = notificationService;
        }

        public async Task<CreateAccountResponse> CreateAccountAsync(CreateAccountRequest req)
        {
            var dto = req.Account;
            var acc = new Account
            {
                AccountId = Guid.NewGuid().ToString(),
                UserId = dto.UserId,
                Balance = dto.Balance,

            };

            _db.Accounts.Add(acc);
            await _db.SaveChangesAsync();

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = dto.UserId,
                Content = "Tài khoản mới của bạn đã được tạo thành công!"
            });

            dto.AccountId = acc.AccountId;

            return new CreateAccountResponse
            {
                Success = true,
                Message = "Tạo tài khoản thành công",
                Account = acc
            };
        }

        public async Task<DetailAccountResponse?> GetAccountByUserIdAsync(DetailAccountRequest req)
        {
            var acc = await _db.Accounts.FirstOrDefaultAsync(x => x.UserId == req.UserId);

            if (acc == null)
            {
                return new DetailAccountResponse
                {
                    Success = false,
                    Message = $"Không tìm thấy account với User ID = {req.UserId}"
                };
            }

            return new DetailAccountResponse
            {
                Success = true,
                Message = "Tìm được tài khoản thành công",
                AccountId = acc.AccountId,
                UserId = acc.UserId,
                Balance = acc.Balance
            };
        }

        public async Task<DeleteAccountResponse> DeleteAccountAsync(DeleteAccountRequest req)
        {
            var acc = await _db.Accounts.FirstOrDefaultAsync(x => x.AccountId == req.AccountId && x.UserId == req.UserId);
            if (acc == null)
                return new DeleteAccountResponse { Success = false, Message = "Không tìm thấy tài khoản" };

            _db.Accounts.Remove(acc);
            await _db.SaveChangesAsync();

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = req.UserId,
                Content = "Tài khoản của bạn đã được xóa khỏi hệ thống."
            });

            return new DeleteAccountResponse
            {
                Success = true,
                Message = "Xóa tài khoản thành công"
            };
        }

        public async Task UpdateBalanceAsync(string userId, long amount, bool isIncrease)
        {
            // 1️ Lấy tài khoản theo UserId
            var account = await _db.Accounts.FirstOrDefaultAsync(a => a.UserId == userId);
            if (account == null)
                throw new Exception($"Không tìm thấy tài khoản với UserId = {userId}");

            // 2️ Parse số dư hiện tại
            long currentBalance = 0;
            if (!long.TryParse(account.Balance, out currentBalance))
                throw new Exception("Số dư hiện tại của tài khoản không hợp lệ.");

            // 3️Cập nhật số dư
            if (isIncrease)
            {
                // Cộng thêm tiền
                currentBalance += amount;
            }
            else
            {
                // Trừ tiền, kiểm tra đủ số dư
                if (currentBalance < amount)
                    throw new Exception("Số dư tài khoản không đủ để thực hiện giao dịch.");
                currentBalance -= amount;
            }

            // 4️Lưu lại
            account.Balance = currentBalance.ToString();
            account.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

            _db.Accounts.Update(account);
            await _db.SaveChangesAsync();

            // 5️ Gửi thông báo (nếu cần)
            string action = isIncrease ? "được cộng thêm" : "bị trừ đi";
            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = userId,
                Content = $"Số dư tài khoản của bạn vừa {action} {amount:N0} VNĐ. " +
                          $"Số dư hiện tại: {currentBalance:N0} VNĐ."
            });
        }

    }
}
