using System.Text.RegularExpressions;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using BEMobile.Models.RequestResponse.UserRR.ChangePassword;
using BEMobile.Models.RequestResponse.UserRR.Login;
using BEMobile.Models.RequestResponse.UserRR.SignUp;
using BEMobile.Models.RequestResponse.UserRR.UpdateUser;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface IUserService
    {

        Task<SignUpResponse> CreateUserAsync(SignUpRequest request);
        Task<UpdateUserResponse> UpdateUserAsync(UpdateUserRequest request);
        Task<LoginResponse> IsLoginAsync(LoginRequest request);
        Task<ChangePasswordResponse> ChangePasswordAsync(ChangePasswordRequest request);

        Task<User?> GetUserByRefreshTokenAsync(string refreshToken);


    }

    public class UserService : IUserService
    {
        private readonly AppDbContext _context;
        private readonly IAccountService _accountService;
        private readonly IJwtService _jwtService;
        private readonly INotificationService _notificationService;

        public UserService(AppDbContext context, IAccountService accountService, INotificationService notificationService, IJwtService jwtService)
        {
            _context = context;
            _accountService = accountService;
            _notificationService = notificationService;
            _jwtService = jwtService;
        }


        private bool IsPasswordStrong(string password)
        {
            if (string.IsNullOrEmpty(password))
                return false;

            // Yêu cầu: ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt
            var regex = new Regex(@"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&#^])[A-Za-z\d@$!%*?&#^]{8,}$");
            return regex.IsMatch(password);
        }
        public async Task<SignUpResponse> CreateUserAsync(SignUpRequest request)
        {
            var userDto = request.UserDto;

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

            if (userDto.Password != request.ConfirmPassword)
            {
                return new SignUpResponse
                {
                    Success = false,
                    Message = "Mẩu khẩu xác nhận không chính xác"
                };
            }

            if (!IsPasswordStrong(userDto.Password))
            {
                return new SignUpResponse
                {
                    Success = false,
                    Message = "Mật khẩu phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt"
                };
            }

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
                Content = "Chào mừng bạn! Tài khoản người dùng của bạn đã được tạo thành công "
            });

            return new SignUpResponse
            {
                Success =true,
                Message = "Đăng ký thành công",
                User = userDto
            };
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

                // Update fields
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
                    Content = "Thông tin cá nhân của bạn đã được cập nhật thành công "
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

            // Access Token + Refresh Token
            var accessToken = _jwtService.GenerateAccessToken(user);
            var refreshToken = _jwtService.GenerateRefreshToken();

            user.RefreshToken = refreshToken;
            user.RefreshTokenExpiry = DateTime.UtcNow.AddDays(30);
            await _context.SaveChangesAsync();

            response.Success = true;
            response.Message = "Đăng nhập thành công.";
            response.AccessToken = accessToken;
            response.RefreshToken = refreshToken;
            response.User = new UserDto
            {
                UserId = user.UserId,
                Name = user.Name,
                Email = user.Email,
                PhoneNumber = user.PhoneNumber,
                UrlImage = user.UrlImage
            };

            return response;
        }

        public async Task<ChangePasswordResponse> ChangePasswordAsync(ChangePasswordRequest request)
        {
            var response = new ChangePasswordResponse();

            try
            {
                if (string.IsNullOrEmpty(request.UserId) ||
                    string.IsNullOrEmpty(request.OldPassword) ||
                    string.IsNullOrEmpty(request.NewPassword))
                {
                    response.Success = false;
                    response.Message = "Vui lòng nhập đầy đủ thông tin.";
                    return response;
                }

                var user = await _context.Users.FirstOrDefaultAsync(u => u.UserId == request.UserId);
                if (user == null)
                {
                    response.Success = false;
                    response.Message = "Không tìm thấy người dùng.";
                    return response;
                }

                if (user.Password != request.OldPassword)
                {
                    response.Success = false;
                    response.Message = "Mật khẩu cũ không chính xác.";
                    return response;
                }

                if (request.NewPassword != request.ConfirmPassword)
                {
                    response.Success = false;
                    response.Message = "Mật khẩu xác nhận không khớp.";
                    return response;
                }

                if (!IsPasswordStrong(request.NewPassword))
                {
                    response.Success = false;
                    response.Message = "Mật khẩu mới phải có ít nhất 8 ký tự, gồm chữ hoa, chữ thường, số và ký tự đặc biệt.";
                    return response;
                }

                user.Password = request.NewPassword;
                user.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");
                _context.Users.Update(user);
                await _context.SaveChangesAsync();

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = user.UserId,
                    Content = "Mật khẩu của bạn đã được thay đổi thành công."
                });

                response.Success = true;
                response.Message = "Đổi mật khẩu thành công.";
                return response;
            }
            catch (Exception ex)
            {
                response.Success = false;
                response.Message = $"Lỗi khi đổi mật khẩu: {ex.Message}";
                return response;
            }
        }

        public async Task<User?> GetUserByRefreshTokenAsync(string refreshToken)
        {
            return await _context.Users.FirstOrDefaultAsync(u => u.RefreshToken == refreshToken);
        }


    }
}
