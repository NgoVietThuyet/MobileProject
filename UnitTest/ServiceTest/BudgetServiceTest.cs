using Xunit;
using Moq;
using BEMobile.Services;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.BudgetRR.CreateBudget;
using BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount;
using BEMobile.Models.RequestResponse.BudgetRR.DeleteBudget;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Http;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;

namespace BEMobile.Tests.ServiceTest
{
    public class BudgetServiceTest : IDisposable
    {
        private readonly AppDbContext _context;
        private readonly Mock<INotificationService> _mockNotificationService;
        private readonly Mock<IHttpContextAccessor> _mockHttpContextAccessor;
        private readonly BudgetService _budgetService;

        public BudgetServiceTest()
        {
            // 1. Setup In-Memory DB
            var options = new DbContextOptionsBuilder<AppDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            _mockHttpContextAccessor = new Mock<IHttpContextAccessor>();
            _context = new AppDbContext(options, _mockHttpContextAccessor.Object);

            // 2. Mock Dependencies
            _mockNotificationService = new Mock<INotificationService>();

            // 3. Init Service
            _budgetService = new BudgetService(_context, _mockNotificationService.Object);
        }

        public void Dispose()
        {
            _context.Database.EnsureDeleted();
            _context.Dispose();
        }

        // =========================================================================
        // TEST GROUP 1: CREATE BUDGET
        // =========================================================================

        [Fact]
        public async Task CreateBudget_ShouldSuccess_WhenValidData()
        {
            // Arrange
            _context.Categories.Add(new Category { Id = "cat1", Name = "Ăn uống" });
            await _context.SaveChangesAsync();

            var request = new CreatBudgetRequest
            {
                UserId = "user1",
                CategoryId = "cat1",
                Initial_Amount = "5000000", // 5 Triệu
                EndDate = "30/12/2025"
            };

            // Act
            var result = await _budgetService.CreateBudgetByUserAsync(request);

            // Assert
            Assert.True(result.Success);
            Assert.NotNull(result.Budget.BudgetId);
            Assert.Equal("0", result.Budget.Current_Amount); // Mới tạo thì đã tiêu phải là 0
            
            _mockNotificationService.Verify(x => x.PushNotificationAsync(It.IsAny<PushNotificationRequest>()), Times.Once);
        }

        // =========================================================================
        // TEST GROUP 2: UPDATE CURRENT AMOUNT (Logic cộng dồn chi tiêu)
        // Áp dụng: Boundary Value Analysis (Biên trên của hạn mức)
        // =========================================================================

        [Fact]
        public async Task UpdateCurrent_Add_ShouldFail_WhenExceedsInitialAmount_BoundaryOver()
        {
            // Arrange
            // Ngân sách 1 triệu, đã tiêu 900k.
            // Muốn tiêu thêm 101k -> Tổng 1.001k (Vượt quá 1tr) -> Phải lỗi
            var budgetId = "b1";
            var budget = new Budget
            {
                BudgetId = budgetId,
                UserId = "u1",
                Initial_Amount = "1000000",
                Current_Amount = "900000",
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };
            _context.Budgets.Add(budget);
            await _context.SaveChangesAsync();

            var request = new UpdateAmountRequest
            {
                BudgetId = budgetId,
                UpdateAmount = "101000", // 900k + 101k > 1tr
                isAddAmount = true
            };

            // Act & Assert
            // Hàm này trả về void nhưng ném Exception, ta dùng Assert.ThrowsAsync
            var ex = await Assert.ThrowsAsync<ArgumentException>(() => 
                _budgetService.UpdateCurrentAmountByUserIdAsync(request));

            Assert.Equal("Số tiền vượt mức số tiền mặc định", ex.Message);
        }

        [Fact]
        public async Task UpdateCurrent_Add_ShouldSuccess_WhenEqualsInitialAmount_BoundaryExact()
        {
            // Arrange
            // Ngân sách 1 triệu, đã tiêu 900k.
            // Tiêu thêm đúng 100k -> Tổng 1tr (Vừa khít biên) -> Thành công
            var budgetId = "b2";
            var budget = new Budget
            {
                BudgetId = budgetId,
                UserId = "u1",
                Initial_Amount = "1000000",
                Current_Amount = "900000",
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };
            _context.Budgets.Add(budget);
            await _context.SaveChangesAsync();

            var request = new UpdateAmountRequest
            {
                BudgetId = budgetId,
                UpdateAmount = "100000",
                isAddAmount = true
            };

            // Act
            await _budgetService.UpdateCurrentAmountByUserIdAsync(request);

            // Assert
            var updatedBudget = await _context.Budgets.FindAsync(budgetId);
            Assert.Equal("1000000", updatedBudget.Current_Amount); // Đã chạm trần
        }

        // =========================================================================
        // TEST GROUP 3: UPDATE CURRENT AMOUNT (Logic hoàn tiền/giảm chi)
        // Áp dụng: Boundary Value Analysis (Biên dưới là 0)
        // =========================================================================

        [Fact]
        public async Task UpdateCurrent_Subtract_ShouldFail_WhenNegativeResult_BoundaryUnder()
        {
            // Arrange
            // Đã tiêu 50k. Muốn hoàn lại 51k -> -1k (Âm) -> Lỗi
            var budgetId = "b3";
            var budget = new Budget
            {
                BudgetId = budgetId,
                Current_Amount = "50000",
                Initial_Amount = "1000000"
            };
            _context.Budgets.Add(budget);
            await _context.SaveChangesAsync();

            var request = new UpdateAmountRequest
            {
                BudgetId = budgetId,
                UpdateAmount = "51000",
                isAddAmount = false // Trừ đi
            };

            // Act & Assert
            var ex = await Assert.ThrowsAsync<ArgumentException>(() => 
                _budgetService.UpdateCurrentAmountByUserIdAsync(request));

            Assert.Equal("Số tiền âm", ex.Message);
        }

        // =========================================================================
        // TEST GROUP 4: UPDATE INITIAL AMOUNT (Cập nhật hạn mức)
        // Áp dụng: Equivalence Partitioning (Vùng logic: Hạn mức mới < Đã tiêu)
        // =========================================================================

        [Fact]
        public async Task UpdateInit_ShouldFail_WhenNewAmountLessThanCurrentSpent()
        {
            // Arrange
            // Hạn mức cũ: 10tr, Đã tiêu: 5tr.
            // Muốn giảm hạn mức xuống còn 4tr (Nhỏ hơn số đã tiêu) -> Lỗi logic
            var budgetId = "b4";
            var budget = new Budget
            {
                BudgetId = budgetId,
                UserId = "u1",
                Initial_Amount = "10000000",
                Current_Amount = "5000000",
                CategoryId = "cat1"
            };
            _context.Budgets.Add(budget);
            _context.Categories.Add(new Category { Id = "cat1", Name = "Test" });
            await _context.SaveChangesAsync();

            var request = new UpdateAmountRequest
            {
                BudgetId = budgetId,
                UserId = "u1",
                UpdateAmount = "4000000" // 4tr < 5tr
            };

            // Act
            var result = await _budgetService.UpdateInitAmountByUserIdAsync(request);

            // Assert
            // Code của bạn đang catch Exception và trả về Success=true nhưng Message báo lỗi
            // Đây là logic trong code của bạn: return new UpdateAmountResponse { Success = true, Message = "Cập nhật thất bại" };
            Assert.Equal("Cập nhật thất bại", result.Message);
        }

        [Fact]
        public async Task UpdateInit_ShouldSuccess_WhenNewAmountValid()
        {
            // Arrange
            // Hạn mức cũ 10tr, đã tiêu 5tr. Tăng hạn mức lên 20tr -> OK
            var budgetId = "b5";
            var budget = new Budget
            {
                BudgetId = budgetId,
                UserId = "u1",
                Initial_Amount = "10000000",
                Current_Amount = "5000000",
                CategoryId = "cat1"
            };
            _context.Budgets.Add(budget);
            _context.Categories.Add(new Category { Id = "cat1", Name = "Test" });
            await _context.SaveChangesAsync();

            var request = new UpdateAmountRequest
            {
                BudgetId = budgetId,
                UserId = "u1",
                UpdateAmount = "20000000"
            };

            // Act
            var result = await _budgetService.UpdateInitAmountByUserIdAsync(request);

            // Assert
            Assert.True(result.Success);
            Assert.Equal("Cập nhật số tiền thành công cho ngân sách", result.Message);

            var dbBudget = await _context.Budgets.FindAsync(budgetId);
            Assert.Equal("20000000", dbBudget.Initial_Amount);
        }

        // =========================================================================
        // TEST GROUP 5: GET ALL BUDGETS (Filter by Current Month)
        // =========================================================================

        [Fact]
        public async Task GetAll_ShouldOnlyReturnCurrentMonthBudgets()
        {
            // Arrange
            var userId = "user_filter";
            var now = DateTime.UtcNow;
            
            // 1. Budget tháng này (Đúng)
            var currentBudget = new Budget 
            { 
                BudgetId = "b_curr", UserId = userId, 
                CreatedDate = now.ToString("dd/MM/yyyy HH:mm:ss") 
            };

            // 2. Budget tháng trước (Sai)
            var pastBudget = new Budget 
            { 
                BudgetId = "b_past", UserId = userId, 
                CreatedDate = now.AddMonths(-1).ToString("dd/MM/yyyy HH:mm:ss") 
            };

            _context.Budgets.AddRange(currentBudget, pastBudget);
            await _context.SaveChangesAsync();

            // Act
            var result = await _budgetService.GetAllBudgetsAsync(userId);

            // Assert
            Assert.Single(result); // Chỉ được trả về 1 cái
            Assert.Equal("b_curr", result.First().BudgetId); // Phải là cái tháng này
        }
    }
}