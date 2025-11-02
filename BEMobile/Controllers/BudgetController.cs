using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;

using BEMobile.Models.RequestResponse.BudgetRR.CreateBudget;
using BEMobile.Models.RequestResponse.BudgetRR.DeleteBudget;
using BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount;
using BEMobile.Models.RequestResponse.BudgetRR.GetAllBudget;

using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Build.Framework;


namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class BudgetsController : ControllerBase
    {
        private readonly IBudgetService _BudgetService;

        public BudgetsController(IBudgetService BudgetService)
        {
            _BudgetService = BudgetService;
        }

        [HttpGet("GetAllBudgets")]

        public async Task<ActionResult<IEnumerable<BudgetDto>>> GetAllBudgets(string userId)
        {
            try
            {
                var Budgets = await _BudgetService.GetAllBudgetsAsync(userId);

                return Ok(Budgets);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);

            }
        }


        [HttpPost("Create")]
        public async Task<ActionResult<CreateBudgetResponse>> CreateBudget([FromBody] CreatBudgetRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(new CreateBudgetResponse
                {
                    Success = false,
                    Message = "Dữ liệu gửi lên không hợp lệ"
                });

            try
            {
                var result = await _BudgetService.CreateBudgetByUserAsync(request);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new CreateBudgetResponse
                {
                    Success = false,
                    Message = $"Đã xảy ra lỗi khi tạo ngân sách: {ex.Message}"
                });
            }
        }

        [HttpPut("Update")]
        public async Task<ActionResult<UpdateAmountResponse>> UpdateAmount([FromBody] UpdateAmountRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(new UpdateAmountResponse
                {
                    Success = false,
                    Message = "Dữ liệu gửi lên không hợp lệ"
                });

            try
            {
                await _BudgetService.UpdateInitAmountByUserIdAsync(request);
                return Ok(new UpdateAmountResponse
                {
                    Success = true,
                    Message = "Cập nhật số tiền thành công"
                });
            }
            catch (ArgumentException ex)
            {
                return Ok(new UpdateAmountResponse
                {
                    Success = false,
                    Message = ex.Message
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new UpdateAmountResponse
                {
                    Success = false,
                    Message = ex.Message
                });
            }
            
        }

        [HttpDelete("Delete")]
        public async Task<ActionResult<DeleteBudgetResponse>> DeleteBudget([FromBody] DeleteBudgetRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(new DeleteBudgetResponse
                {
                    Success = false,
                    Message = "Dữ liệu gửi lên không hợp lệ"
                });

            try
            {
                var result = await _BudgetService.DeleteBudgetAsync(request);
                return Ok(result);
            }
            catch (Exception ex)
            {
                return StatusCode(500, new DeleteBudgetResponse
                {
                    Success = false,
                    Message = $"Lỗi khi xóa ngân sách: {ex.Message}"
                });
            }
        }

        //[HttpGet("search")]
        //public async Task<ActionResult<IEnumerable<UserDto>>> SearchUsers([FromQuery] string? name, [FromQuery] string? email, [FromQuery] string? phoneNumber)
        //{

        //    var Users = await _UserService.SearchUsersAsync(name, email, phoneNumber);
        //    return Ok(Users);
        //}
        //[HttpPost("login")]
        //[ProducesResponseType(typeof(LoginResponse), 200)]
        //[ProducesResponseType(typeof(LoginResponse), 400)]
        //public async Task<IActionResult> Login([FromBody] LoginRequest request)
        //{
        //    try
        //    {

        //        // Gọi service để xác thực
        //        var User = await _UserService.IsLogin(request.Email, request.Password);

        //        if (User == null)
        //        {
        //            return Unauthorized(new LoginResponse
        //            {
        //                Success = false,
        //                Message = "Email hoặc mật khẩu không đúng"
        //            });
        //        }

        //        // Tạo response
        //        var response = new LoginResponse
        //        {
        //            Success = true,
        //            Message = "Đăng nhập thành công",
        //            User = new UserDto
        //            {
        //                UserId = User.UserId,
        //                Name = User.Name,
        //                Email = User.Email,
        //                PhoneNumber = User.PhoneNumber,
        //                Facebook = User.Facebook,
        //                Twitter = User.Twitter,
        //                CreatedDate = User.CreatedDate,
        //                UpdatedDate = User.UpdatedDate
        //            }
        //            // Có thể thêm Token nếu triển khai JWT
        //        };

        //        return Ok(response);
        //    }
        //    catch (Exception ex)
        //    {
        //        return StatusCode(500, new LoginResponse
        //        {
        //            Success = false,
        //            Message = "Đã xảy ra lỗi trong quá trình đăng nhập"
        //        });
        //    }
        //}
    }
}