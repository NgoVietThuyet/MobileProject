using Azure.Core;
using BEMobile.Data.Entities;
using BEMobile.Models.RequestResponse.Account.CreateAccount;
using BEMobile.Models.RequestResponse.Account.DeleteAccount;
using BEMobile.Models.RequestResponse.Account.DetailAccount;
using BEMobile.Models.RequestResponse.Notification.PushNotification;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface IAccountService
    {
        Task<CreateAccountResponse> CreateAccountAsync(CreateAccountRequest req);
        Task<DetailAccountResponse?> GetAccountByUserIdAsync(DetailAccountRequest req);
        Task<DeleteAccountResponse> DeleteAccountAsync(DeleteAccountRequest req);
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
                Balance = dto.Balance
            };

            _db.Accounts.Add(acc);
            await _db.SaveChangesAsync();

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = dto.UserId,
                Content = "Bạn đã tạo tài khoản mới thành công!"
            });


            return new CreateAccountResponse
            {
                Success = true,
                Message = "Tạo tài khoản thành công",
                Account = dto
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
            if (acc== null)
                return new DeleteAccountResponse { Success = false, Message = "Không tìm thấy tài khoản" };

            _db.Accounts.Remove(acc);
            await _db.SaveChangesAsync();

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = req.UserId,
                Content = "Tài khoản của bạn đã được xóa thành công!"
            });

            return new DeleteAccountResponse { Success = true, Message = "Xóa thành công" };
        }
    }
}
