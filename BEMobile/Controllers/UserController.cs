using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;
using BEMobile.Models.RequestResponse.UserRR.ChangePassword;
using BEMobile.Models.RequestResponse.UserRR.Login;
using BEMobile.Models.RequestResponse.UserRR.PhoneLogin;
using BEMobile.Models.RequestResponse.UserRR.RefreshToken;
using BEMobile.Models.RequestResponse.UserRR.SignUp;
using BEMobile.Models.RequestResponse.UserRR.UpdateUser;
using BEMobile.Services;
using DocumentFormat.OpenXml.InkML;
using FirebaseAdmin.Auth;
using Google.Apis.Auth.OAuth2.Requests;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Build.Framework;
using Microsoft.EntityFrameworkCore;
using RefreshTokenRequest = BEMobile.Models.RequestResponse.UserRR.RefreshToken.RefreshTokenRequest;



namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {

        

        private readonly IUserService _UserService;
        private readonly AppDbContext _context;
        private readonly IJwtService _jwtService;
        private readonly IAccountService _accountService;

        public UsersController(AppDbContext context, IUserService UserService, IJwtService jwtService, IAccountService accountService)
        {
            _UserService = UserService;
            _jwtService = jwtService;
            _context = context;
            _accountService = accountService;

        }



        [HttpPost("Create")]
        [HttpPost("signup")]
        public async Task<ActionResult<SignUpResponse>> CreateUser([FromBody] SignUpRequest request)
        {
            try
            {
                var response = await _UserService.CreateUserAsync(request);

                if (!response.Success)
                {
                    return BadRequest(new SignUpResponse
                    {
                        Success = false,
                        Message = response.Message
                    });
                }

                return Ok(new SignUpResponse
                {
                    Success = true,
                    Message = response.Message,
                    User = response.User
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new SignUpResponse
                {
                    Success = false,
                    Message = $"Lỗi hệ thống: {ex.Message}"
                });
            }
        }


        [HttpPut("Update")]
        public async Task<ActionResult<UpdateUserResponse>> UpdateUser([FromBody] UpdateUserRequest request)
        {
            var response = await _UserService.UpdateUserAsync(request);
            if (!response.Success)
                return BadRequest(response);
            return Ok(response);
        }



        [HttpPost("login")]
        [ProducesResponseType(typeof(LoginResponse), 200)]
        [ProducesResponseType(typeof(LoginResponse), 400)]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            var result = await _UserService.IsLoginAsync(request);

            if (!result.Success)
                return BadRequest(result);

            return Ok(result);
        }

        [HttpPost("ChangePassword")]
        [ProducesResponseType(typeof(ChangePasswordResponse), 200)]
        [ProducesResponseType(typeof(ChangePasswordResponse), 400)]
        public async Task<IActionResult> ChangePassword([FromBody] ChangePasswordRequest request)
        {
            try
            {
                var result = await _UserService.ChangePasswordAsync(request);

                if (!result.Success)
                    return BadRequest(result);

                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new ChangePasswordResponse
                {
                    Success = false,
                    Message = $"Lỗi hệ thống: {ex.Message}"
                });
            }
        }

        [HttpPost("refresh-token")]
        public async Task<IActionResult> RefreshToken([FromBody] RefreshTokenRequest request)
        {
            var user = await _UserService.GetUserByRefreshTokenAsync(request.RefreshToken);
            if (user == null || user.RefreshTokenExpiry < DateTime.UtcNow)
                return Unauthorized(new { Message = "Refresh token không hợp lệ hoặc đã hết hạn." });

            var accessToken = _jwtService.GenerateAccessToken(user);
            var refreshToken = _jwtService.GenerateRefreshToken();

            user.RefreshToken = refreshToken;
            user.RefreshTokenExpiry = DateTime.UtcNow.AddDays(30);
            await _context.SaveChangesAsync();

            return Ok(new
            {
                AccessToken = accessToken,
                RefreshToken = refreshToken
            });
        }

        [HttpPost("phone-login")]
        public async Task<ActionResult<PhoneLoginResponse>> PhoneLogin([FromBody] PhoneLoginRequest request)
        {
            try
            {
                // Xác minh token từ Firebase
                var decoded = await FirebaseAuth.DefaultInstance.VerifyIdTokenAsync(request.FirebaseIdToken);
                var firebasePhone = decoded.Claims["phone_number"].ToString();

                //  So khớp phone giữa Firebase và request để tránh spoof
                if (!string.Equals(firebasePhone, request.PhoneNumber, StringComparison.OrdinalIgnoreCase))
                {
                    return BadRequest(new PhoneLoginResponse
                    {
                        Success = false,
                        Message = "Số điện thoại không khớp với Firebase token."
                    });
                }

                // check user trong DB
                var user = _context.Users.FirstOrDefault(u => u.PhoneNumber == request.PhoneNumber);
                if (user == null)
                {
                    user = new User
                    {
                        UserId = Guid.NewGuid().ToString(),
                        PhoneNumber = request.PhoneNumber
                    };
                    _context.Users.Add(user);
                    await _context.SaveChangesAsync();
                }

                // create access token & refresh token
                var accessToken = _jwtService.GenerateAccessToken(user);
                var refreshToken = _jwtService.GenerateRefreshToken();

                // (Optional) Lưu refresh token lại DB nếu bạn muốn
                 user.RefreshToken = refreshToken;
                await _context.SaveChangesAsync();

                // response
                return Ok(new PhoneLoginResponse
                {
                    Success = true,
                    Message = "Đăng nhập thành công!",
                    AccessToken = accessToken,
                    RefreshToken = refreshToken,
                    User = new UserDto
                    {
                        UserId = user.UserId,
                        Name = user.Name,
                        Email = user.Email,
                        PhoneNumber = user.PhoneNumber
                    }
                });
            }
            catch (FirebaseAuthException ex)
            {
                return BadRequest(new PhoneLoginResponse
                {
                    Success = false,
                    Message = $"Firebase verification failed: {ex.Message}"
                });
            }
            catch (Exception ex)
            {
                return BadRequest(new PhoneLoginResponse
                {
                    Success = false,
                    Message = $"Internal error: {ex.Message}"
                });
            }
        }

    }


}