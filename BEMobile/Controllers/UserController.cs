using BEMobile.Models.DTOs;

using BEMobile.Models.RequestResponse.UserRR.Login;
using BEMobile.Models.RequestResponse.UserRR.SignUp;
using BEMobile.Models.RequestResponse.UserRR.UpdateUser;
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



    }
}