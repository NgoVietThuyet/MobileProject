using Xunit;
using Moq;
using BEMobile.Services;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Http;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;

namespace BEMobile.Tests.ServiceTest
{
    public class NotificationServiceTest : IDisposable
    {
        private readonly AppDbContext _context;
        private readonly Mock<IHttpContextAccessor> _mockHttpContextAccessor;
        private readonly NotificationService _notificationService;

        public NotificationServiceTest()
        {
            // 1. Setup In-Memory Database
            var options = new DbContextOptionsBuilder<AppDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString()) // DB riêng cho mỗi test
                .Options;

            // 2. Mock HttpContextAccessor (Do AppDbContext yêu cầu)
            _mockHttpContextAccessor = new Mock<IHttpContextAccessor>();

            // 3. Khởi tạo Context & Service
            _context = new AppDbContext(options, _mockHttpContextAccessor.Object);
            _notificationService = new NotificationService(_context);
        }

        public void Dispose()
        {
            _context.Database.EnsureDeleted();
            _context.Dispose();
        }

        // =========================================================================
        // TEST GROUP 1: PUSH NOTIFICATION (GỬI THÔNG BÁO)
        // Áp dụng: Boundary Value Analysis (Biên rỗng/null)
        // =========================================================================

        [Theory]
        [InlineData("", "Nội dung test")] // UserId rỗng
        [InlineData(null, "Nội dung test")] // UserId null
        [InlineData("u1", "")] // Content rỗng
        [InlineData("u1", null)] // Content null
        public async Task Push_ShouldFail_WhenInputIsInvalid_BoundaryEmpty(string userId, string content)
        {
            // Arrange
            var request = new PushNotificationRequest 
            { 
                UserId = userId, 
                Content = content 
            };

            // Act
            var result = await _notificationService.PushNotificationAsync(request);

            // Assert
            Assert.False(result.Success);
            Assert.Equal("Thiếu userId hoặc nội dung", result.Message);
        }

        [Fact]
        public async Task Push_ShouldSuccess_WhenInputValid()
        {
            // Arrange
            var request = new PushNotificationRequest 
            { 
                UserId = "user_thuyet", 
                Content = "Chào Thuyết, bạn vừa nhận được lương" 
            };

            // Act
            var result = await _notificationService.PushNotificationAsync(request);

            // Assert
            Assert.True(result.Success);
            Assert.NotNull(result.Notification.NotificationId);
            Assert.Equal("Chào Thuyết, bạn vừa nhận được lương", result.Notification.Content);
            Assert.False(result.Notification.IsRead); // Mặc định chưa đọc

            // Verify DB
            var dbNoti = await _context.Notifications.FirstOrDefaultAsync(n => n.UserId == "user_thuyet");
            Assert.NotNull(dbNoti);
        }

        // =========================================================================
        // TEST GROUP 2: GET ALL (LẤY DANH SÁCH)
        // Áp dụng: Equivalence Partitioning (Lọc đúng User)
        // =========================================================================

        [Fact]
        public async Task GetAll_ShouldReturnOnlyUserNotifications_OrderedByDate()
        {
            // Arrange
            var userMain = "user_thuyet";
            var userOther = "user_van";

            // Tạo dữ liệu giả: 2 cái của Thuyết, 1 cái của Văn
            _context.Notifications.AddRange(
                new Notification 
                { 
                    NotificationId = "n1", UserId = userMain, Content = "Thông báo cũ", 
                    CreatedDate = "01/12/2025 10:00:00" 
                },
                new Notification 
                { 
                    NotificationId = "n2", UserId = userMain, Content = "Thông báo mới", 
                    CreatedDate = "05/12/2025 10:00:00" // Mới hơn
                },
                new Notification 
                { 
                    NotificationId = "n3", UserId = userOther, Content = "Thông báo của Văn", 
                    CreatedDate = "03/12/2025 10:00:00" 
                }
            );
            await _context.SaveChangesAsync();

            // Act
            var result = await _notificationService.GetAllNotificationsAsync(userMain);

            // Assert
            Assert.True(result.Success);
            Assert.Equal(2, result.Notifications.Count()); // Chỉ lấy 2 cái của Thuyết
            
            // Kiểm tra sắp xếp (Mới nhất lên đầu)
            Assert.Equal("Thông báo mới", result.Notifications.First().Content); 
            Assert.Equal("Thông báo cũ", result.Notifications.Last().Content);
        }

        // =========================================================================
        // TEST GROUP 3: MARK AS READ (ĐÁNH DẤU ĐÃ ĐỌC)
        // Áp dụng: Test tồn tại (Existence)
        // =========================================================================

        [Fact]
        public async Task MarkRead_ShouldFail_WhenIdNotFound()
        {
            // Arrange
            // DB rỗng

            // Act
            var result = await _notificationService.MarkAsReadAsync("ghost_id");

            // Assert
            Assert.False(result.Success);
            Assert.Equal("Không tìm thấy thông báo", result.Message);
        }

        [Fact]
        public async Task MarkRead_ShouldSuccess_WhenIdExists()
        {
            // Arrange
            var notiId = "n_read";
            var noti = new Notification 
            { 
                NotificationId = notiId, 
                UserId = "u1", 
                IsRead = false 
            };
            _context.Notifications.Add(noti);
            await _context.SaveChangesAsync();

            // Act
            var result = await _notificationService.MarkAsReadAsync(notiId);

            // Assert
            Assert.True(result.Success);
            
            var dbNoti = await _context.Notifications.FindAsync(notiId);
            Assert.True(dbNoti.IsRead); // Phải chuyển thành true
            Assert.NotNull(dbNoti.UpdatedDate); // Ngày update phải được set
        }

        // =========================================================================
        // TEST GROUP 4: DELETE NOTIFICATION (XÓA)
        // =========================================================================

        [Fact]
        public async Task Delete_ShouldFail_WhenIdNotFound()
        {
            // Act
            var result = await _notificationService.DeleteNotificationAsync("not_exist");

            // Assert
            Assert.False(result.Success);
            Assert.Contains("Không tìm thấy", result.Message);
        }

        [Fact]
        public async Task Delete_ShouldSuccess_WhenIdExists()
        {
            // Arrange
            var notiId = "n_delete";
            _context.Notifications.Add(new Notification 
            { 
                NotificationId = notiId, 
                UserId = "u1", 
                Content = "Tin rác" 
            });
            await _context.SaveChangesAsync();

            // Act
            var result = await _notificationService.DeleteNotificationAsync(notiId);

            // Assert
            Assert.True(result.Success);
            
            var dbNoti = await _context.Notifications.FindAsync(notiId);
            Assert.Null(dbNoti); // Phải biến mất khỏi DB
        }
    }
}