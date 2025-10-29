using BEMobile.Models.DTOs;

using BEMobile.Models.RequestResponse.UserRR.Login;
using BEMobile.Models.RequestResponse.UserRR.SignUp;
using BEMobile.Models.RequestResponse.UserRR.UpdateUser;
using BEMobile.Models.RequestResponse.UserRR.UploadUserImage;
using BEMobile.Models.RequestResponse.UserRR.ChangePassword;
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

        [HttpPost("UploadProfileImage")]
        [ProducesResponseType(typeof(UploadUserImageResponse), 200)]
        [ProducesResponseType(typeof(UploadUserImageResponse), 400)]
        public async Task<IActionResult> UploadProfileImage([FromForm] UploadUserImageRequest request)
        {
            try
            {
                var result = await _UserService.UploadUserImageAsync(request);

                if (!result.Success)
                    return BadRequest(result);

                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new UploadUserImageResponse
                {
                    Success = false,
                    Message = $"Đã xảy ra lỗi khi tải ảnh: {ex.Message}"
                });
            }
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

    }
}