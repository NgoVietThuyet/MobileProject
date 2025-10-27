//using BEMobile.Data.Entities;
//using BEMobile.Models.DTOs;
//using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;
//using BEMobile.Models.RequestResponse.AccountRR.DeleteAccount;
//using BEMobile.Models.RequestResponse.AccountRR.DetailAccount;
//using BEMobile.Services;
//using Microsoft.EntityFrameworkCore;
//using Xunit;
//using System;
//using System.Threading.Tasks;   

//public class Account
//{
//    public string AccountId { get; set; }
//    public string UserId { get; set; }
//    public decimal Balance { get; set; }
//}

//public class AppDbContext : DbContext
//{
//    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }
//    public DbSet<Account> Accounts { get; set; } 
//}

//public class AccountServiceMinimalTests : IDisposable
//{
//    private readonly AppDbContext _dbContext;
//    private readonly AccountService _accountService;
//    private readonly string _testDbName;

//    public AccountServiceMinimalTests()
//    {
//        _testDbName = Guid.NewGuid().ToString();

//        var options = new DbContextOptionsBuilder<AppDbContext>()
//            .UseInMemoryDatabase(databaseName: _testDbName)
//            .Options;

//        _dbContext = new AppDbContext(options);
//        _accountService = new AccountService(_dbContext);
//    }

//    public void Dispose()
//    {
//        _dbContext.Database.EnsureDeleted();
//        _dbContext.Dispose();
//    }

//    [Fact]
//    public async Task CreateAccountAsync_HappyPath_ShouldSucceed()
//    {
//        var req = new CreateAccountRequest
//        {
//            Account = new AccountDto { UserId = "user_new", Balance = "50" }
//        };
//        var res = await _accountService.CreateAccountAsync(req);

//        Assert.True(res.Success);
//        var savedAcc = await _dbContext.Accounts.FirstOrDefaultAsync(a => a.UserId == "user_new");
//        Assert.NotNull(savedAcc);
//    }

//    [Fact]
//    public async Task GetAccountByUserIdAsync_HappyPath_ShouldReturnAccount()
//    {
//        _dbContext.Accounts.Add(new Account { AccountId = "acc1", UserId = "user_get", Balance = 100 });
//        await _dbContext.SaveChangesAsync();

//        var req = new DetailAccountRequest { UserId = "user_get" };

//        var res = await _accountService.GetAccountByUserIdAsync(req);

//        Assert.True(res.Success);
//        Assert.Equal("user_get", res.UserId);
//        Assert.Equal("100", res.Balance);
//    }

//    [Fact]
//    public async Task GetAccountByUserIdAsync_NotFound_ShouldReturnFailure()
//    {
//        var req = new DetailAccountRequest { UserId = "non_existent" };

//        var res = await _accountService.GetAccountByUserIdAsync(req);

//        Assert.False(res.Success);
//        Assert.Contains("Không tìm thấy account", res.Message);
//    }

//    [Fact]
//    public async Task DeleteAccountAsync_HappyPath_ShouldSucceedAndRemoveFromDB()
//    {
//        _dbContext.Accounts.Add(new Account { AccountId = "acc_del", UserId = "user_del", Balance = 500m });
//        await _dbContext.SaveChangesAsync();

//        var req = new DeleteAccountRequest { AccountId = "acc_del", UserId = "user_del" };

//        var res = await _accountService.DeleteAccountAsync(req);
//        Assert.True(res.Success);
//        var deletedAcc = await _dbContext.Accounts.FirstOrDefaultAsync(a => a.AccountId == "acc_del");
//        Assert.Null(deletedAcc);
//    }

//    [Fact]
//    public async Task DeleteAccountAsync_NotFound_ShouldReturnFailure()
//    {
//        var req = new DeleteAccountRequest { AccountId = "acc_wrong", UserId = "user_wrong" };

//        var res = await _accountService.DeleteAccountAsync(req);
//        Assert.False(res.Success);
//        Assert.Equal("Không tìm thấy tài khoản", res.Message);
//    }
//}