using BEMobile.Models.DTOs;
using Microsoft.AspNetCore.Mvc;

using BEMobile.Services;
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
        public async Task<ActionResult<UserDto>> CreateUser(UserDto userDto)
        {
            try
            {
                var user = await _userService.CreateUserAsync(userDto);
                return Ok(user);
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
            var result = await _userService.DeleteUserAsync(id);
            if (!result)
            {
                return NotFound();
            }
            return NoContent();
        }

        [HttpGet("search")]
        public async Task<ActionResult<IEnumerable<UserDto>>> SearchUsers([FromQuery] string? name, [FromQuery] string? email, [FromQuery] string? phoneNumber)
        {
            var users = await _userService.SearchUsersAsync(name, email, phoneNumber);
            return Ok(users);
        }
    }
}