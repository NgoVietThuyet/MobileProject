using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Login;
using BEMobile.Models.RequestResponse.SignUp;
using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Build.Framework;


namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;

        public UsersController(IUserService userService)
        {
            _userService = userService;
        }

        [HttpGet("GetAllUsers")]
        public async Task<ActionResult<IEnumerable<UserDto>>> GetAllUsers()
        {
            try
            {
                 var users = await _userService.GetAllUsersAsync();
                return Ok(users);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);

            }
        }

        
        [HttpPost("Create")]
        public async Task<ActionResult<UserDto>> CreateUser([FromBody] SignUpRequest request)
        {
            try
            {
                var user = await _userService.CreateUserAsync(request.userDto);
                if (user == null)
                {
                    return Unauthorized(new SignUpResponse
                    {
                        Success = false,
                        Message = "Đăng ký thất bại"
                    });
                }
                else
                {
                    var response = new SignUpResponse
                    {
                        Success = true,
                        Message = "Đăng ký thành công",
                        User = new UserDto
                        {
                            UserId = user.UserId,
                            Name = user.Name,
                            Email = user.Email,
                            PhoneNumber = user.PhoneNumber,
                            Facebook = user.Facebook,
                            Twitter = user.Twitter,
                            CreatedDate = user.CreatedDate,
                            UpdatedDate = user.UpdatedDate
                        }
                        // Có thể thêm Token nếu triển khai JWT
                    };

                    return Ok(response);
                }
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpPut("Update")]
        public async Task<ActionResult<UserDto>> UpdateUser(UserDto userDto)
        {
            try
            {
                await _userService.UpdateUserAsync(userDto);
                return Ok();
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpDelete("DeleteById{id}")]
        public async Task<ActionResult> DeleteUser(string id)
        {
            try
            {
                var result = await _userService.DeleteUserAsync(id);
                if (!result)
                { return NotFound("Xóa thất bại"); }
                return Ok("Xóa thành công");
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(ex.Message);
            }
        }

        [HttpGet("search")]
        public async Task<ActionResult<IEnumerable<UserDto>>> SearchUsers([FromQuery] string? name, [FromQuery] string? email, [FromQuery] string? phoneNumber)
        {

            var users = await _userService.SearchUsersAsync(name, email, phoneNumber);
            return Ok(users);
        }
        [HttpPost("login")]
        [ProducesResponseType(typeof(LoginResponse), 200)]
        [ProducesResponseType(typeof(LoginResponse), 400)]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            try
            {

                // Gọi service để xác thực
                var user = await _userService.IsLogin(request.Email, request.Password);
                
                if (user == null)
                {
                    return Unauthorized(new LoginResponse
                    {
                        Success = false,
                        Message = "Email hoặc mật khẩu không đúng"
                    });
                }

                // Tạo response
                var response = new LoginResponse
                {
                    Success = true,
                    Message = "Đăng nhập thành công",
                    User = new UserDto
                    {
                        UserId = user.UserId,
                        Name = user.Name,
                        Email = user.Email,
                        PhoneNumber = user.PhoneNumber,
                        Facebook = user.Facebook,
                        Twitter = user.Twitter,
                        CreatedDate = user.CreatedDate,
                        UpdatedDate = user.UpdatedDate
                    }
                    // Có thể thêm Token nếu triển khai JWT
                };

                return Ok(response);
            }
            catch (Exception ex)
            {
                System.Diagnostics.Debug.WriteLine("Login error: " + ex.Message);
                System.Diagnostics.Debug.WriteLine(ex.StackTrace);

                return StatusCode(500, new LoginResponse
                {
                    Success = false,
                    Message = "Đã xảy ra lỗi trong quá trình đăng nhậpppp" + ex.Message
                });
            }
        }
    }
}