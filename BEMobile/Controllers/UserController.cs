using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Login;
using BEMobile.Models.RequestResponse.SignUp;


using BEMobile.Models.RequestResponse.User.Login;


using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Build.Framework;


namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {

        private readonly IUserService _UserService;

        public UsersController(IUserService UserService)
        {
            _UserService = UserService;
        }



        [HttpGet("GetAllUsers")]
        public async Task<ActionResult<IEnumerable<UserDto>>> GetAllUsers()
        {
            try
            {



                 var Users = await _UserService.GetAllUsersAsync();
                return Ok(Users);


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


                var User = await _UserService.CreateUserAsync(request.UserDto);
                if (User == null)


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


                            UserId = User.UserId,
                            Name = User.Name,
                            Email = User.Email,
                            PhoneNumber = User.PhoneNumber,
                            Facebook = User.Facebook,
                            Twitter = User.Twitter,
                            CreatedDate = User.CreatedDate,
                            UpdatedDate = User.UpdatedDate


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

        public async Task<ActionResult<UserDto>> UpdateUser(UserDto UserDto)
        {
            try
            {
                await _UserService.UpdateUserAsync(UserDto);

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


                var result = await _UserService.DeleteUserAsync(id);


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



            var Users = await _UserService.SearchUsersAsync(name, email, phoneNumber);
            return Ok(Users);
        }
        [HttpPost("login")]
        [ProducesResponseType(typeof(LoginResponse), 200)]
        [ProducesResponseType(typeof(LoginResponse), 400)]
        public async Task<IActionResult> Login([FromBody] LoginRequest request)
        {
            try
            {

                // Gọi service để xác thực
                var User = await _UserService.IsLogin(request.Email, request.Password);
                
                if (User == null)
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
                        UserId = User.UserId,
                        Name = User.Name,
                        Email = User.Email,
                        PhoneNumber = User.PhoneNumber,
                        Facebook = User.Facebook,
                        Twitter = User.Twitter,
                        CreatedDate = User.CreatedDate,
                        UpdatedDate = User.UpdatedDate
                    }
                    // Có thể thêm Token nếu triển khai JWT
                };

                return Ok(response);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new LoginResponse
                {
                    Success = false,
                    Message = "Đã xảy ra lỗi trong quá trình đăng nhập"
                });
            }


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

                return StatusCode(500, new LoginResponse
                {
                    Success = false,
                    Message = "Đã xảy ra lỗi trong quá trình đăng nhập"
                });
            }
        }



    }
}