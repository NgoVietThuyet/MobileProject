using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Account.CreateAccount;
using BEMobile.Models.RequestResponse.Notification.PushNotification;
using BEMobile.Models.RequestResponse.User.Login;
using BEMobile.Models.RequestResponse.User.UpdateUser;
using BEMobile.Models.RequestResponse.User.UploadUserImage;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface IUserService
    {
        Task<UserDto> CreateUserAsync(UserDto userDto);
        Task<UpdateUserResponse> UpdateUserAsync(UpdateUserRequest request);
        Task<LoginResponse> IsLoginAsync(LoginRequest request);
        Task<UploadUserImageResponse> UploadUserImageAsync(UploadUserImageRequest request);

    }

    public class UserService : IUserService
    {
        private readonly AppDbContext _context;
        private readonly IAccountService _accountService;
        private readonly INotificationService _notificationService;

        public UserService(AppDbContext context, IAccountService accountService, INotificationService notificationService)
        {
            _context = context;
            _accountService = accountService;
            _notificationService = notificationService;
        }

        public async Task<UserDto> CreateUserAsync(UserDto userDto)
        {
            var user = new User
            {
                UserId = Guid.NewGuid().ToString(),
                Name = userDto.Name,
                PhoneNumber = userDto.PhoneNumber,
                Facebook = userDto.Facebook,
                Twitter = userDto.Twitter,
                Email = userDto.Email,
                Password = userDto.Password,
                Job = userDto.Job,
                Google = userDto.Google,
                DateOfBirth = userDto.DateOfBirth,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            userDto.UserId = user.UserId;
            userDto.Password = null;

            await _accountService.CreateAccountAsync(new CreateAccountRequest
            {
                Account = new AccountDto
                {
                    UserId = user.UserId,
                    Balance = "0"
                }
            });

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = user.UserId,
                Content = "Chào mừng bạn! Tài khoản người dùng của bạn đã được tạo thành công 🎉"
            });

            return userDto;
        }

        public async Task<UpdateUserResponse> UpdateUserAsync(UpdateUserRequest request)
        {
            var response = new UpdateUserResponse();

            try
            {
                if (request == null)
                {
                    response.Success = false;
                    response.Message = "Request body is null.";
                    return response;
                }

                if (string.IsNullOrEmpty(request.UserId))
                {
                    response.Success = false;
                    response.Message = "UserId is missing or empty in request body.";
                    return response;
                }

                var existingUser = await _context.Users.FirstOrDefaultAsync(u => u.UserId == request.UserId);

                if (existingUser == null)
                {
                    response.Success = false;
                    response.Message = $"User with ID {request.UserId} not found.";
                    return response;
                }

                existingUser.Name = request.Name;
                existingUser.PhoneNumber = request.PhoneNumber;
                existingUser.Facebook = request.Facebook;
                existingUser.Twitter = request.Twitter;
                existingUser.Email = request.Email;
                existingUser.Job = request.Job;
                existingUser.Google = request.Google;
                existingUser.DateOfBirth = request.DateOfBirth;
                existingUser.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                await _context.SaveChangesAsync();

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = existingUser.UserId,
                    Content = "Thông tin cá nhân của bạn đã được cập nhật thành công ✅"
                });

                response.Success = true;
                response.Message = "User updated successfully.";
                response.User = new UserDto
                {
                    UserId = existingUser.UserId,
                    Name = existingUser.Name,
                    PhoneNumber = existingUser.PhoneNumber,
                    Facebook = existingUser.Facebook,
                    Twitter = existingUser.Twitter,
                    Email = existingUser.Email,
                    Job = existingUser.Job,
                    Google = existingUser.Google,
                    DateOfBirth = existingUser.DateOfBirth,
                    CreatedDate = existingUser.CreatedDate,
                    UpdatedDate = existingUser.UpdatedDate
                };
            }
            catch (Exception ex)
            {
                response.Success = false;
                response.Message = $"Error updating user: {ex.Message}";
            }

            return response;
        }

        public async Task<LoginResponse> IsLoginAsync(LoginRequest request)
        {
            var response = new LoginResponse();

            if (string.IsNullOrWhiteSpace(request.Email) || string.IsNullOrWhiteSpace(request.Password))
            {
                response.Success = false;
                response.Message = "Vui lòng nhập đầy đủ email và mật khẩu.";
                return response;
            }

            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == request.Email);

            if (user == null)
            {
                response.Success = false;
                response.Message = "Không tìm thấy tài khoản với email này.";
                return response;
            }

            if (user.Password != request.Password)
            {
                response.Success = false;
                response.Message = "Mật khẩu không chính xác.";
                return response;
            }

            response.Success = true;
            response.Message = "Đăng nhập thành công.";
            response.User = new UserDto
            {
                UserId = user.UserId,
                Name = user.Name,
                PhoneNumber = user.PhoneNumber,
                Facebook = user.Facebook,
                Twitter = user.Twitter,
                Email = user.Email,
                Job = user.Job,
                Google = user.Google,
                DateOfBirth = user.DateOfBirth,
                CreatedDate = user.CreatedDate,
                UpdatedDate = user.UpdatedDate,
                UrlImage = user.UrlImage
            };

            return response;
        }

        public async Task<UploadUserImageResponse> UploadUserImageAsync(UploadUserImageRequest request)
        {
            var response = new UploadUserImageResponse();

            if (request.ImageFile == null || request.ImageFile.Length == 0)
            {
                response.Success = false;
                response.Message = "Ảnh tải lên không hợp lệ";
                return response;
            }

            var user = await _context.Users.FirstOrDefaultAsync(u => u.UserId == request.UserId);
            if (user == null)
            {
                response.Success = false;
                response.Message = "Không tìm thấy người dùng";
                return response;
            }

            var uploadsFolder = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "images", "users");
            if (!Directory.Exists(uploadsFolder))
                Directory.CreateDirectory(uploadsFolder);

            var uniqueFileName = $"{Guid.NewGuid()}_{Path.GetFileName(request.ImageFile.FileName)}";
            var filePath = Path.Combine(uploadsFolder, uniqueFileName);

            using (var stream = new FileStream(filePath, FileMode.Create))
            {
                await request.ImageFile.CopyToAsync(stream);
            }

            user.UrlImage = $"/images/users/{uniqueFileName}";
            user.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");
            await _context.SaveChangesAsync();

            response.Success = true;
            response.Message = "Cập nhật ảnh đại diện thành công";
            response.User = new UserDto
            {
                UserId = user.UserId,
                Name = user.Name,
                UrlImage = user.UrlImage
            };

            return response;
        }

    }
}
