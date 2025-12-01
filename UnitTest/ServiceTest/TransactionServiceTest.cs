using Xunit;
using Moq;
using BEMobile.Services;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.TransactionRR.CreateTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.UpdateTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.DeleteTransaction;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Http;
using System.Collections.Generic;
using System.Threading.Tasks;
using System;

namespace BEMobile.Tests.ServiceTest
{
    public class TransactionServiceTest : IDisposable
    {
        private readonly AppDbContext _context;
        private readonly Mock<INotificationService> _mockNotificationService;
        private readonly Mock<IBudgetService> _mockBudgetService;
        private readonly Mock<IHttpContextAccessor> _mockHttpContextAccessor;
        private readonly TransactionService _transactionService;

        public TransactionServiceTest()
        {
            // 1. Setup In-Memory Database
            var options = new DbContextOptionsBuilder<AppDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            _mockHttpContextAccessor = new Mock<IHttpContextAccessor>();
            _context = new AppDbContext(options, _mockHttpContextAccessor.Object);

            // 2. Mock Dependencies
            _mockNotificationService = new Mock<INotificationService>();
            _mockBudgetService = new Mock<IBudgetService>();

            // 3. Inject into Service
            _transactionService = new TransactionService(
                _context,
                _mockNotificationService.Object,
                _mockBudgetService.Object
            );
        }

        public void Dispose()
        {
            _context.Database.EnsureDeleted();
            _context.Dispose();
        }

        // =========================================================================
        // TEST GROUP 1: CREATE TRANSACTION (TẠO GIAO DỊCH)
        // Áp dụng: Equivalence Partitioning (Vùng tương đương) cho Amount
        // =========================================================================

        [Fact]
        public async Task Create_ShouldFail_WhenAmountIsZero_BoundaryValue()
        {
            // Arrange (Biên dưới: 0 là không hợp lệ)
            var request = new CreateTransactionRequest
            {
                Transaction = new TransactionDto 
                { 
                    Amount = "0", // Biên lỗi
                    Type = "INCOME", 
                    UserId = "u1" 
                }
            };

            // Act
            var result = await _transactionService.CreateTransactionAsync(request);

            // Assert
            Assert.False(result.Success);
            Assert.Contains("lớn hơn 0", result.Message);
        }

        [Fact]
        public async Task Create_ShouldFail_WhenAmountIsNotNumber_EquivalencePartition()
        {
            // Arrange (Vùng lỗi: Chuỗi không phải số)
            var request = new CreateTransactionRequest
            {
                Transaction = new TransactionDto 
                { 
                    Amount = "năm trăm nghìn", // Sai định dạng
                    Type = "INCOME",
                    UserId = "u1" 
                }
            };

            // Act
            var result = await _transactionService.CreateTransactionAsync(request);

            // Assert
            Assert.False(result.Success);
            Assert.Contains("không hợp lệ", result.Message);
        }

        // =========================================================================
        // TEST GROUP 2: EXPENSE LOGIC (CHI TIÊU)
        // Áp dụng: Boundary Value Analysis (Biên của số dư)
        // =========================================================================

        [Fact]
        public async Task Create_Expense_ShouldFail_WhenBalanceInsufficient_BoundaryOver()
        {
            // Arrange: Tài khoản có 50k, tiêu 51k -> Lỗi
            var account = new Account { UserId = "u1", Balance = "50000" }; // 50.000 VND
            _context.Accounts.Add(account);
            _context.Categories.Add(new Category { Id = "c1", Name = "Ăn uống" });
            await _context.SaveChangesAsync();

            var request = new CreateTransactionRequest
            {
                Transaction = new TransactionDto
                {
                    UserId = "u1",
                    CategoryId = "c1",
                    Type = "EXPENSE",
                    Amount = "51000", // Vượt quá biên 1 đơn vị
                    Note = "Bún bò huế"
                }
            };

            // Act
            var result = await _transactionService.CreateTransactionAsync(request);

            // Assert
            Assert.False(result.Success);
            Assert.Contains("Số dư không đủ", result.Message);
        }

        [Fact]
        public async Task Create_Expense_ShouldSuccess_WhenBalanceEqualsAmount_BoundaryExact()
        {
            // Arrange: Tài khoản có 50k, tiêu đúng 50k -> Hợp lệ (Về 0)
            var account = new Account { UserId = "u2", Balance = "50000" };
            _context.Accounts.Add(account);
            _context.Categories.Add(new Category { Id = "c1", Name = "Cà phê" });
            await _context.SaveChangesAsync();

            var request = new CreateTransactionRequest
            {
                Transaction = new TransactionDto
                {
                    UserId = "u2",
                    CategoryId = "c1",
                    Type = "EXPENSE",
                    Amount = "50000", // Chạm biên
                    Note = "Highlands Coffee"
                }
            };

            // Act
            var result = await _transactionService.CreateTransactionAsync(request);

            // Assert
            Assert.True(result.Success);
            
            // Kiểm tra số dư về 0
            var updatedAcc = await _context.Accounts.FirstOrDefaultAsync(a => a.UserId == "u2");
            Assert.Equal("0", updatedAcc.Balance); // 50000 - 50000 = 0

            // Kiểm tra BudgetService có được gọi không
            // (Lưu ý: Logic tìm budgetId khá phức tạp về ngày tháng, ở đây ta test flow chính)
            // _mockBudgetService.Verify... (Sẽ check ở mức integration sâu hơn)
        }

        [Fact]
        public async Task Create_Income_ShouldIncreaseBalance_HappyPath()
        {
            // Arrange: Lương về 10 triệu
            var account = new Account { UserId = "u3", Balance = "2000000" }; // Đang có 2 triệu
            _context.Accounts.Add(account);
            _context.Categories.Add(new Category { Id = "c2", Name = "Lương" });
            await _context.SaveChangesAsync();

            var request = new CreateTransactionRequest
            {
                Transaction = new TransactionDto
                {
                    UserId = "u3",
                    CategoryId = "c2",
                    Type = "INCOME",
                    Amount = "10000000", // Thêm 10 triệu
                    Note = "Lương tháng 12"
                }
            };

            // Act
            var result = await _transactionService.CreateTransactionAsync(request);

            // Assert
            Assert.True(result.Success);
            var updatedAcc = await _context.Accounts.FirstOrDefaultAsync(a => a.UserId == "u3");
            Assert.Equal("12000000", updatedAcc.Balance); // 2tr + 10tr = 12tr
            
            _mockNotificationService.Verify(n => n.PushNotificationAsync(It.IsAny<PushNotificationRequest>()), Times.Once);
        }

        // =========================================================================
        // TEST GROUP 3: UPDATE TRANSACTION (CẬP NHẬT GIAO DỊCH)
        // Logic: Hoàn tiền cũ -> Trừ tiền mới
        // =========================================================================

        [Fact]
        public async Task Update_ShouldUpdateBalanceCorrectly_WhenChangingAmount()
        {
            // Arrange
            // TK: 90k. Đã tiêu: 10k (Bánh mì). Tổng gốc: 100k.
            // Giờ sửa "Bánh mì" 10k thành "Phở" 30k.
            // Logic mong đợi: 90k + 10k (hoàn lại) - 30k (mới) = 70k.
            
            var userId = "u4";
            var transactionId = "t1";

            var account = new Account { UserId = userId, Balance = "90000" };
            var transaction = new Transaction 
            { 
                TransactionId = transactionId, 
                UserId = userId, 
                Amount = "10000", // Cũ
                Type = "EXPENSE",
                CategoryId = "c1",
                CreatedDate = "01/12/2025 10:00:00"
            };

            _context.Accounts.Add(account);
            _context.Transactions.Add(transaction);
            _context.Categories.Add(new Category { Id = "c1", Name = "Ăn sáng" });
            await _context.SaveChangesAsync();

            var request = new UpdateTransactionRequest
            {
                Transaction = new TransactionDto
                {
                    TransactionId = transactionId,
                    UserId = userId,
                    CategoryId = "c1",
                    Type = "EXPENSE",
                    Amount = "30000", // Mới
                    Note = "Đổi thành ăn Phở"
                }
            };

            // Act
            var result = await _transactionService.UpdateTransactionAsync(request);

            // Assert
            Assert.True(result.Success);

            var updatedAcc = await _context.Accounts.FirstOrDefaultAsync(a => a.UserId == userId);
            // 90.000 + 10.000 - 30.000 = 70.000
            Assert.Equal("70000", updatedAcc.Balance); 

            var updatedTrans = await _context.Transactions.FindAsync(transactionId);
            Assert.Equal("30000", updatedTrans.Amount);
        }

        // =========================================================================
        // TEST GROUP 4: DELETE TRANSACTION (XÓA GIAO DỊCH)
        // Logic: Hoàn lại tiền vào tài khoản
        // =========================================================================

        [Fact]
        public async Task Delete_ShouldRestoreBalance_WhenDeletingExpense()
        {
            // Arrange
            // TK còn: 5 triệu. Giao dịch mua điện thoại: 5 triệu. (Tổng gốc 10tr).
            // Xóa giao dịch mua điện thoại -> Tiền phải về lại 10tr.
            
            var userId = "u5";
            var transId = "t-phone";

            var account = new Account { UserId = userId, Balance = "5000000" };
            var transaction = new Transaction 
            { 
                TransactionId = transId, 
                UserId = userId, 
                Amount = "5000000", 
                Type = "EXPENSE",
                CategoryId = "tech",
                CreatedDate = "01/12/2025 10:00:00"
            };

            _context.Accounts.Add(account);
            _context.Transactions.Add(transaction);
            _context.Categories.Add(new Category { Id = "tech", Name = "Công nghệ" });
            await _context.SaveChangesAsync();

            var request = new DeleteTransactionRequest { TransactionId = transId };

            // Act
            var result = await _transactionService.DeleteTransactionAsync(request);

            // Assert
            Assert.True(result.Success);

            // Kiểm tra giao dịch đã bay màu
            var deletedTrans = await _context.Transactions.FindAsync(transId);
            Assert.Null(deletedTrans);

            // Kiểm tra tiền đã về ví
            var updatedAcc = await _context.Accounts.FirstOrDefaultAsync(a => a.UserId == userId);
            Assert.Equal("10000000", updatedAcc.Balance); // 5tr + 5tr = 10tr
        }
    }
}