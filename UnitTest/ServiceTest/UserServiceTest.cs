using Xunit;
using Moq; 
using BEMobile.Services;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.UserRR.UpdateUser;
using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;
using MockQueryable.Moq; // Thư viện giúp mock Async query
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using System.Threading;

namespace BEMobile.Tests.Services
{
    public class UserServicePureTests
    {
        private readonly Mock<AppDbContext> _mockContext;
        private readonly Mock<IAccountService> _mockAccountService;
        private readonly UserService _userService;
        private readonly Mock<DbSet<User>> _mockUserSet;

        public UserServicePureTests()
        {
            // 1. Mock AccountService
            _mockAccountService = new Mock<IAccountService>();

            // 2. Mock DbContext
            _mockContext = new Mock<AppDbContext>();

            // 3. Khởi tạo Service với các Mock
            _userService = new UserService(_mockContext.Object, _mockAccountService.Object);
        }

        // --- TEST LOGIC: VALIDATION (Không liên quan DB) ---

        [Fact]
        public async Task UpdateUserAsync_ShouldFail_WhenRequestIsNull()
        {
            // Act
            var result = await _userService.UpdateUserAsync(null);

            // Assert
            Assert.False(result.Success);
            Assert.Equal("Request body is null.", result.Message);
        }

        [Fact]
        public async Task UpdateUserAsync_ShouldFail_WhenUserIdIsMissing()
        {
            // Arrange
            var request = new UpdateUserRequest { UserId = "" }; // Empty ID

            // Act
            var result = await _userService.UpdateUserAsync(request);

            // Assert
            Assert.False(result.Success);
            Assert.Contains("UserId is missing", result.Message);
        }

        // --- TEST LOGIC: DATA INTERACTION (Mock DbSet) ---

        [Fact]
        public async Task UpdateUserAsync_ShouldFail_WhenUserNotFound()
        {
            // Arrange
            // Tạo một List rỗng (giả lập DB không có user nào)
            var usersList = new List<User>(); 
            
            // Biến List thành Mock DbSet có hỗ trợ Async (nhờ MockQueryable.Moq)
            var mockSet = usersList.AsQueryable().BuildMockDbSet();

            // Setup: Khi gọi _context.Users thì trả về cái mockSet rỗng kia
            _mockContext.Setup(c => c.Users).Returns(mockSet.Object);

            var request = new UpdateUserRequest { UserId = "123" };

            // Act
            var result = await _userService.UpdateUserAsync(request);

            // Assert
            Assert.False(result.Success);
            Assert.Contains("not found", result.Message);
        }

        [Fact]
        public async Task UpdateUserAsync_ShouldSuccess_WhenUserExists()
        {
            // Arrange
            // Tạo data giả trong bộ nhớ
            var existingUser = new User { UserId = "123", Name = "Old Name" };
            var usersList = new List<User> { existingUser };

            // Mock DbSet từ List trên
            var mockSet = usersList.AsQueryable().BuildMockDbSet();

            // Setup Context trả về MockSet
            _mockContext.Setup(c => c.Users).Returns(mockSet.Object);

            var request = new UpdateUserRequest 
            { 
                UserId = "123", 
                Name = "New Name",
                Email = "new@test.com"
            };

            // Act
            var result = await _userService.UpdateUserAsync(request);

            // Assert
            Assert.True(result.Success);
            Assert.Equal("New Name", result.User.Name);

            // Quan trọng: Verify xem hàm SaveChangesAsync có được gọi đúng 1 lần không?
            _mockContext.Verify(c => c.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
        }

        [Fact]
        public async Task CreateUserAsync_ShouldCallAddAndSaveChanges()
        {
            // Arrange
            var mockSet = new List<User>().AsQueryable().BuildMockDbSet();
            _mockContext.Setup(c => c.Users).Returns(mockSet.Object);

            var userDto = new UserDto { Name = "Test User", Email = "test@email.com" };

            // Act
            await _userService.CreateUserAsync(userDto);

            // Assert
            // Kiểm tra xem hàm Add() của DbSet có được gọi với đúng object User không
            _mockContext.Verify(c => c.Users.Add(It.Is<User>(u => u.Name == "Test User")), Times.Once);
            
            // Kiểm tra SaveChangesAsync được gọi
            _mockContext.Verify(c => c.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
        }
    }
}