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
        public async Task<ActionResult<Budget>> CreateUser([FromBody] Request request)
        {
            try
            {
                var Budget = await _BudgetService.CreateBudgetByUserAsync(request);
                if (User == null)
                {
                    return Unauthorized(new Response
                    {
                        Success = false,
                        Message = "Đăng ký thất bại"
                    });
                }
                else
                {
                    var response = new Response
                    {
                        Success = true,
                        Message = "Đăng ký thành công",
                        Budget = new BudgetDto
                        {
                            BudgetId = Budget.BudgetId,
                            Initial_Amount = Budget.Initial_Amount,
                            Current_Amount = Budget.Current_Amount,
                            UserId = Budget.UserId,

                            StartDate = Budget.StartDate,

                            CategoryId = Budget.CategoryId,
                            CreatedDate = Budget.CreatedDate,
                            UpdatedDate = Budget.UpdatedDate
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
        public async Task<ActionResult<Budget>> UpdateUser(UpdateAmountRequest request)
        {
            try
            {
                await _BudgetService.UpdateAmountByUserIdAsync(request);

                return Ok(new UpdateAmountResponse
                {
                    Success = true,
                    Message = "Cập nhật số tiền thành công"
                });
            }
            catch (InvalidOperationException ex)
            {
                return Ok(new UpdateAmountResponse
                {
                    Success = false,
                    Message = "Cập nhật số tiền không thành công"
                });
            }
        }

        [HttpDelete("DeleteById")]
        public async Task<ActionResult> DeleteBudget(string id)
        {
            try
            {
                var result = await _BudgetService.DeleteBudgetAsync(id);
                if (result)
                {
                    return Ok(new DeleteBudgetResponse
                    {
                        Success = true,
                        Message = "Xóa thành công"
                    });
                }
                return Ok(new DeleteBudgetResponse
                {
                    Success = false,
                    Message = "Xóa thất bại"
                });
            }
            catch (InvalidOperationException ex)
            {
                return BadRequest(ex.Message);
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