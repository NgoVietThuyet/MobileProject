using Azure.Core;
using BEMobile.Data.Entities;
using BEMobile.Models.RequestResponse.Account.CreateAccount;
using BEMobile.Models.RequestResponse.Account.DeleteAccount;
using BEMobile.Models.RequestResponse.Account.DetailAccount;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface IAccountService
    {
        Task<CreateAccountResponse> CreateAccountAsync(CreateAccountRequest req);
        Task<DetailAccountResponse?> GetAccountByIdAsync(DetailAccountRequest req);
        Task<DeleteAccountResponse> DeleteAccountAsync(DeleteAccountRequest req);
    }

    public class AccountService : IAccountService
    {
        private readonly AppDbContext _db;
        public AccountService(AppDbContext db) { _db = db; }

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


            return new CreateAccountResponse
            {
                Success = true,
                Message = "Tạo tài khoản thành công",
                Account = dto
            };
        }

        public async Task<DetailAccountResponse?> GetAccountByIdAsync(DetailAccountRequest req)
        {
            var acc = await _db.Accounts.FirstOrDefaultAsync(x => x.AccountId == req.AccountId);
            if (acc == null) return null;

            return new DetailAccountResponse
            {
                Success = true,
                Message = "OK",
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

            return new DeleteAccountResponse { Success = true, Message = "Xóa thành công" };
        }
    }
}
