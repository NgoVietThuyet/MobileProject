using Xunit;
using Moq;
using BEMobile.Services;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.UserRR.SignUp;
using BEMobile.Models.RequestResponse.UserRR.UpdateUser;
using BEMobile.Models.RequestResponse.UserRR.Login;
using BEMobile.Models.RequestResponse.UserRR.ChangePassword;
using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Http;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System;

namespace BEMobile.Tests.ServiceTest
{
    public class UserServiceTest : IDisposable
    {
        private readonly AppDbContext _context;
        
        private readonly Mock<IAccountService> _mockAccountService;
        private readonly Mock<INotificationService> _mockNotificationService;
        private readonly Mock<IJwtService> _mockJwtService;
        private readonly Mock<IHttpContextAccessor> _mockHttpContextAccessor;

        private readonly UserService _userService;

        public UserServiceTest()
        {
            var options = new DbContextOptionsBuilder<AppDbContext>()
                .UseInMemoryDatabase(databaseName: Guid.NewGuid().ToString())
                .Options;

            _mockHttpContextAccessor = new Mock<IHttpContextAccessor>();
            _mockAccountService = new Mock<IAccountService>();
            _mockNotificationService = new Mock<INotificationService>();
            _mockJwtService = new Mock<IJwtService>();

            _context = new AppDbContext(options, _mockHttpContextAccessor.Object);

            _userService = new UserService(
                _context, 
                _mockAccountService.Object,
                _mockNotificationService.Object,
                _mockJwtService.Object
            );
        }

        public void Dispose()
        {
            _context.Database.EnsureDeleted();
            _context.Dispose();
        }

        // =========================================================
        // 1. TEST CreateUserAsync
        // =========================================================

        [Fact]
        public async Task CreateUser_ShouldFail_WhenConfirmPasswordDoesNotMatch()
        {
            var request = new SignUpRequest
            {
                UserDto = new UserDto { Password = "PassWord@123" },
                ConfirmPassword = "WrongPassword"
            };

            var result = await _userService.CreateUserAsync(request);

            Assert.False(result.Success);
            Assert.Contains("không chính xác", result.Message);
        }

        [Fact]
        public async Task CreateUser_ShouldSuccess_WhenValid()
        {
            // ===> [TEST NÀY SẼ FAIL] <===
            var request = new SignUpRequest
            {
                UserDto = new UserDto 
                { 
                    Name = "Ngọ Viết Thuyết", 
                    Email = "thuyetnv@email.com",
                    Password = "Admin@123",
                    PhoneNumber = "0900000000"
                },
                ConfirmPassword = "Admin@123"
            };

            var result = await _userService.CreateUserAsync(request);

            // LOGIC FAIL: Tạo thành công (True), nhưng ta lại Assert là False
            Assert.False(result.Success, "CỐ TÌNH FAIL: Mong đợi False nhưng thực tế là True (Tạo thành công)"); 
            
            // Các đoạn dưới này sẽ không chạy tới được vì đã Fail ở trên
            var userInDb = await _context.User.FirstOrDefaultAsync(u => u.Email == "thuyetnv@email.com");
            Assert.NotNull(userInDb);
        }

        // =========================================================
        // 2. TEST UpdateUserAsync
        // =========================================================

        [Fact]
        public async Task UpdateUser_ShouldFail_WhenUserNotFound()
        {
            var request = new UpdateUserRequest { UserId = "not-exist", Name = "New Name" };

            var result = await _userService.UpdateUserAsync(request);

            Assert.False(result.Success);
            Assert.Contains("not found", result.Message);
        }

        [Fact]
        public async Task UpdateUser_ShouldSuccess_WhenUserExists()
        {
            // ===> [TEST NÀY SẼ FAIL] <===
            var existingUser = new User 
            { 
                UserId = "u1", 
                Name = "Thuyet", 
                Email = "thuyet@email.com" 
            };
            _context.User.Add(existingUser);
            await _context.SaveChangesAsync();

            // Code update thành tên "Văn"
            var request = new UpdateUserRequest { UserId = "u1", Name = "Văn" };
            var result = await _userService.UpdateUserAsync(request);

            Assert.True(result.Success);
            
            var updatedUser = await _context.User.FindAsync("u1");

            // LOGIC FAIL: Trong DB là "Văn", nhưng ta bắt nó phải bằng "Thịnh"
            Assert.Equal("Thịnh", updatedUser.Name); 
        }

        // =========================================================
        // 3. TEST IsLoginAsync
        // =========================================================

        [Fact]
        public async Task IsLogin_ShouldFail_WhenPasswordWrong()
        {
            var user = new User 
            { 
                UserId = "u2", 
                Name = "Thịnh", 
                Email = "thinh@email.com", 
                Password = "RealPassword" 
            };
            _context.User.Add(user);
            await _context.SaveChangesAsync();

            var request = new LoginRequest { Email = "thinh@email.com", Password = "WrongPassword" };
            var result = await _userService.IsLoginAsync(request);

            Assert.False(result.Success);
            Assert.Contains("không chính xác", result.Message);
        }

        [Fact]
        public async Task IsLogin_ShouldSuccess_WhenCredentialsCorrect()
        {
            var user = new User 
            { 
                UserId = "u3", 
                Name = "Ngọ Viết Thuyết", 
                Email = "thuyetnv@email.com", 
                Password = "RealPassword" 
            };
            _context.User.Add(user);
            await _context.SaveChangesAsync();

            _mockJwtService.Setup(j => j.GenerateAccessToken(It.IsAny<User>())).Returns("fake-token");
            _mockJwtService.Setup(j => j.GenerateRefreshToken()).Returns("fake-refresh");

            var request = new LoginRequest { Email = "thuyetnv@email.com", Password = "RealPassword" };
            var result = await _userService.IsLoginAsync(request);

            Assert.True(result.Success);
            Assert.Equal("fake-token", result.AccessToken);
            
            var userInDb = await _context.User.FindAsync("u3");
            Assert.Equal("fake-refresh", userInDb.RefreshToken);
        }

        // =========================================================
        // 4. TEST ChangePasswordAsync
        // =========================================================

        [Fact]
        public async Task ChangePassword_ShouldFail_WhenOldPasswordWrong()
        {
            var user = new User 
            { 
                UserId = "u4", 
                Name = "Văn", 
                Password = "OldPassword" 
            };
            _context.User.Add(user);
            await _context.SaveChangesAsync();

            var request = new ChangePasswordRequest 
            { 
                UserId = "u4", 
                OldPassword = "WrongPassword",
                NewPassword = "New@123", 
                ConfirmPassword = "New@123" 
            };

            var result = await _userService.ChangePasswordAsync(request);

            Assert.False(result.Success);
            Assert.Contains("Mật khẩu cũ không chính xác", result.Message);
        }

        [Fact]
        public async Task ChangePassword_ShouldFail_WhenNewPasswordWeak()
        {
            var user = new User 
            { 
                UserId = "u5", 
                Name = "Thịnh", 
                Password = "OldPassword" 
            };
            _context.User.Add(user);
            await _context.SaveChangesAsync();

            var request = new ChangePasswordRequest 
            { 
                UserId = "u5", 
                OldPassword = "OldPassword",
                NewPassword = "weak",
                ConfirmPassword = "weak"
            };

            var result = await _userService.ChangePasswordAsync(request);

            Assert.False(result.Success);
            Assert.Contains("ít nhất 8 ký tự", result.Message);
        }

        [Fact]
        public async Task ChangePassword_ShouldSuccess_WhenValid()
        {
            var user = new User 
            { 
                UserId = "u6", 
                Name = "Ngọ Viết Thuyết", 
                Password = "OldPassword" 
            };
            _context.User.Add(user);
            await _context.SaveChangesAsync();

            var request = new ChangePasswordRequest 
            { 
                UserId = "u6", 
                OldPassword = "OldPassword",
                NewPassword = "NewPass@123",
                ConfirmPassword = "NewPass@123"
            };

            var result = await _userService.ChangePasswordAsync(request);

            Assert.True(result.Success);
            
            var updatedUser = await _context.User.FindAsync("u6");
            Assert.Equal("NewPass@123", updatedUser.Password);
            
            _mockNotificationService.Verify(x => x.PushNotificationAsync(It.IsAny<PushNotificationRequest>()), Times.Once);
        }
    }
}