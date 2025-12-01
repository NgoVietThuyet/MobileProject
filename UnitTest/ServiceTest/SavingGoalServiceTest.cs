using Xunit;
using Moq;
using BEMobile.Services;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.SavingGoalRR;
using BEMobile.Models.RequestResponse.SavingGoalRR.UpdateAmount;
using BEMobile.Models.RequestResponse.SavingGoalRR.Delete;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Http;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;

namespace BEMobile.Tests.ServiceTest
{
    public class SavingGoalServiceTest : IDisposable
    {
        private readonly AppDbContext _context;
        private readonly Mock<INotificationService> _mockNotificationService;
        private readonly Mock<IAccountService> _mockAccountService;
        private readonly Mock<IHttpContextAccessor> _mockHttpContextAccessor;
        private readonly SavingGoalService _savingGoalService;

        public SavingGoalServiceTest()
        {
            // 1. Setup In-Memory Database (Tạo DB ảo sạch sẽ cho mỗi test case)
            var options = new DbContextOptionsBuilder<AppDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            // 2. Mock các dependencies
            _mockHttpContextAccessor = new Mock<IHttpContextAccessor>();
            _mockNotificationService = new Mock<INotificationService>();
            _mockAccountService = new Mock<IAccountService>();

            // 3. Khởi tạo Context thật
            _context = new AppDbContext(options, _mockHttpContextAccessor.Object);

            // 4. Khởi tạo Service cần test
            _savingGoalService = new SavingGoalService(
                _context,
                _mockNotificationService.Object,
                _mockAccountService.Object
            );
        }

        public void Dispose()
        {
            _context.Database.EnsureDeleted();
            _context.Dispose();
        }

        // =========================================================================
        // TEST 1: CREATE SAVING GOAL (TẠO MỤC TIÊU)
        // =========================================================================

        [Fact]
        public async Task Create_ShouldSuccess_WhenValidData()
        {
            // Arrange
            var request = new CreateSavingGoalRequest
            {
                savingGoalDto = new SavingGoalDto
                {
                    UserId = "user_thuyet",
                    Title = "Mua iPhone 16 Pro Max",
                    TargetAmount = "35000000", // 35 Triệu
                    CurrentAmount = "0",
                    CategoryId = "tech",
                    Deadline = "31/12/2025"
                }
            };

            // Act
            var result = await _savingGoalService.CreateSavingGoalAsync(request);

            // Assert
            Assert.NotNull(result);
            Assert.NotNull(result.GoalId);
            Assert.Equal("Mua iPhone 16 Pro Max", result.Title);
            
            // Verify notification sent
            _mockNotificationService.Verify(n => n.PushNotificationAsync(It.IsAny<PushNotificationRequest>()), Times.Once);
            
            // Verify DB saved
            var goalInDb = await _context.SavingGoals.FirstOrDefaultAsync(g => g.UserId == "user_thuyet");
            Assert.NotNull(goalInDb);
            Assert.Equal("35000000", goalInDb.TargetAmount);
        }

        // =========================================================================
        // TEST 2: GET ALL SAVING GOALS (LẤY DANH SÁCH)
        // =========================================================================

        [Fact]
        public async Task GetAll_ShouldReturnOnlyGoalsOfSpecificUser()
        {
            // Arrange
            var user1 = "user_thuyet";
            var user2 = "user_van";

            // Add 2 goals cho Thuyet, 1 goal cho Van
            _context.SavingGoals.AddRange(
                new SavingGoal { GoalId = "g1", UserId = user1, Title = "Xe máy", TargetAmount="0", CurrentAmount="0" },
                new SavingGoal { GoalId = "g2", UserId = user1, Title = "Laptop", TargetAmount = "0", CurrentAmount = "0" },
                new SavingGoal { GoalId = "g3", UserId = user2, Title = "Du lịch", TargetAmount = "0", CurrentAmount = "0" }
            );
            await _context.SaveChangesAsync();

            // Act (Lấy của Thuyet)
            var result = await _savingGoalService.GetAllSavingGoalAsync(user1);

            // Assert
            Assert.Equal(2, result.Count());
            Assert.Contains(result, g => g.Title == "Xe máy");
            Assert.Contains(result, g => g.Title == "Laptop");
            Assert.DoesNotContain(result, g => g.Title == "Du lịch");
        }

        // =========================================================================
        // TEST 3: UPDATE AMOUNT - ADD MONEY (NẠP TIỀN VÀO MỤC TIÊU)
        // Logic: Saving Tăng -> Tài khoản chính Giảm (isIncrease: false)
        // =========================================================================

        [Fact]
        public async Task UpdateAmount_Add_ShouldIncreaseSaving_AndDecreaseAccountBalance()
        {
            // Arrange
            var goalId = "g_add";
            var userId = "u1";
            var initialSaving = 1000000; // Đang có 1 triệu
            var addAmount = 500000;      // Nạp thêm 500k

            var goal = new SavingGoal 
            { 
                GoalId = goalId, 
                UserId = userId, 
                Title = "Tiết kiệm cưới vợ",
                TargetAmount = "100000000",
                CurrentAmount = initialSaving.ToString() 
            };
            _context.SavingGoals.Add(goal);
            await _context.SaveChangesAsync();

            var request = new UpdateAmountGoalRequest
            {
                GoalId = goalId,
                UpdateAmount = addAmount.ToString(),
                isAddAmount = true // NẠP TIỀN
            };

            // Act
            await _savingGoalService.UpdateAmountAsync(request);

            // Assert
            // 1. Kiểm tra Saving Goal trong DB tăng lên
            var updatedGoal = await _context.SavingGoals.FindAsync(goalId);
            Assert.Equal("1500000", updatedGoal.CurrentAmount); // 1tr + 500k

            // 2. Kiểm tra AccountService được gọi để TRỪ tiền tài khoản chính
            // isIncrease phải là FALSE
            _mockAccountService.Verify(s => s.UpdateBalanceAsync(
                userId, 
                addAmount, 
                false // False = Trừ tiền ví chính
            ), Times.Once);

            // 3. Kiểm tra thông báo
            _mockNotificationService.Verify(n => n.PushNotificationAsync(It.IsAny<PushNotificationRequest>()), Times.Once);
        }

        // =========================================================================
        // TEST 4: UPDATE AMOUNT - WITHDRAW MONEY (RÚT TIỀN TỪ MỤC TIÊU)
        // Logic: Saving Giảm -> Tài khoản chính Tăng (isIncrease: true)
        // =========================================================================

        [Fact]
        public async Task UpdateAmount_Withdraw_ShouldDecreaseSaving_AndIncreaseAccountBalance()
        {
            // Arrange
            var goalId = "g_withdraw";
            var userId = "u1";
            var initialSaving = 2000000; // Đang có 2 triệu
            var withdrawAmount = 500000; // Rút ra 500k

            var goal = new SavingGoal 
            { 
                GoalId = goalId, 
                UserId = userId, 
                CurrentAmount = initialSaving.ToString(),
                TargetAmount = "5000000"
            };
            _context.SavingGoals.Add(goal);
            await _context.SaveChangesAsync();

            var request = new UpdateAmountGoalRequest
            {
                GoalId = goalId,
                UpdateAmount = withdrawAmount.ToString(),
                isAddAmount = false // RÚT TIỀN
            };

            // Act
            await _savingGoalService.UpdateAmountAsync(request);

            // Assert
            // 1. Kiểm tra Saving Goal trong DB giảm đi
            var updatedGoal = await _context.SavingGoals.FindAsync(goalId);
            Assert.Equal("1500000", updatedGoal.CurrentAmount); // 2tr - 500k

            // 2. Kiểm tra AccountService được gọi để CỘNG tiền vào tài khoản chính
            // isIncrease phải là TRUE
            _mockAccountService.Verify(s => s.UpdateBalanceAsync(
                userId, 
                withdrawAmount, 
                true // True = Cộng tiền vào ví chính
            ), Times.Once);
        }

        // =========================================================================
        // TEST 5: DELETE SAVING GOAL (XÓA MỤC TIÊU)
        // =========================================================================

        [Fact]
        public async Task Delete_ShouldRemoveGoalFromDatabase()
        {
            // Arrange
            var goalId = "g_delete";
            var goal = new SavingGoal 
            { 
                GoalId = goalId, 
                UserId = "u1", 
                Title = "Mục tiêu tạm thời", 
                CurrentAmount = "0", 
                TargetAmount = "100" 
            };
            _context.SavingGoals.Add(goal);
            await _context.SaveChangesAsync();

            var request = new DeleteSavingRequest { id = goalId };

            // Act
            await _savingGoalService.DeleteSavingGoalAsync(request);

            // Assert
            var deletedGoal = await _context.SavingGoals.FindAsync(goalId);
            Assert.Null(deletedGoal); // Phải không còn tìm thấy

            _mockNotificationService.Verify(n => n.PushNotificationAsync(It.IsAny<PushNotificationRequest>()), Times.Once);
        }

        // =========================================================================
        // TEST 6: FAIL CASE (KHÔNG TÌM THẤY GOAL)
        // =========================================================================

        [Fact]
        public async Task UpdateAmount_ShouldThrowException_WhenGoalNotFound()
        {
            // Arrange
            var request = new UpdateAmountGoalRequest { GoalId = "ghost_id", UpdateAmount = "100" };

            // Act & Assert
            // Kiểm tra xem nó có ném Exception với message đúng không
            var ex = await Assert.ThrowsAsync<Exception>(() => _savingGoalService.UpdateAmountAsync(request));
            Assert.Contains("Bị lỗi", ex.Message); // Code service wrap lỗi trong Exception("Bị lỗi", ex)
        }
    }
}