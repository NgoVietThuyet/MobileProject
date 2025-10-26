using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;



using BEMobile.Models.RequestResponse.SavingGoalRR;
using BEMobile.Models.RequestResponse.SavingGoalRR.Create;
using BEMobile.Models.RequestResponse.SavingGoalRR.Delete;
using BEMobile.Models.RequestResponse.SavingGoalRR.UpdateAmount;


using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Filters;
using Microsoft.Build.Framework;


namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class SavingGoalController : ControllerBase
    {
        private readonly ISavingGoalService _SavingGoalService;

        public SavingGoalController(ISavingGoalService SavingGoalService)
        {
                _SavingGoalService = SavingGoalService;
        }

        [HttpGet("GetAllSavingGoals")]

        public async Task<ActionResult<IEnumerable<SavingGoalDto>>> GetAllSavingGoals(string userId)
        {
            try
            {
                var SavingGoals = await _SavingGoalService.GetAllSavingGoalAsync(userId);

                return Ok(SavingGoals);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);

            }
        }


        [HttpPost("Create")]
        public async Task<ActionResult<SavingGoal>> CreateSavingGoal([FromBody] CreateSavingGoalRequest request)
        {
            try
            {
                var SavingGoal = await _SavingGoalService.CreateSavingGoalAsync(request);
                if (User == null)
                {
                    return Unauthorized(new CreateSavingGoalResponse
                    {
                        Success = false,
                        Message = "Đăng ký thất bại"
                    });
                }
                else
                {
                    var response = new CreateSavingGoalResponse
                    {
                        Success = true,
                        Message = "Đăng ký thành công",
                        SavingGoal = new SavingGoalDto
                        {
                            GoalId = SavingGoal.GoalId,
                            CurrentAmount = SavingGoal.CurrentAmount,
                            Title = SavingGoal.Title,
                            TargetAmount = SavingGoal.TargetAmount,
                            Deadline = SavingGoal.Deadline,
                            UserId = SavingGoal.UserId,
                            CategoryId = SavingGoal.CategoryId,
                            CreatedDate = SavingGoal.CreatedDate,
                            UpdatedDate = SavingGoal.UpdatedDate
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
        [HttpPut("UpdateAmount")]
        public async Task<ActionResult<SavingGoal>> UpdateSavingGoal(UpdateAmountGoalRequest request)
        {
            try
            {
                await _SavingGoalService.UpdateAmountAsync(request);

                return Ok(new UpdateAmountGoalResponse
                {
                    Success = true,
                    Message = "Cập nhật số tiền thành công"
                });
            }
            catch (InvalidOperationException ex)
            {
                return Ok(new UpdateAmountGoalResponse
                {
                    Success = false,
                    Message = "Cập nhật số tiền không thành công"
                });
            }
        }
        [HttpPut("DeleteSaving")]
        public async Task<ActionResult<SavingGoal>> DeleteSavingGoalAysnc(DeleteSavingRequest request)
        {
            try
            {
                await _SavingGoalService.DeleteSavingGoalAsync(request);

                return Ok(new DeleteSavingResponse
                {
                    Success = true,
                    Message = "Xóa mục tiêu tiết kiệm thành công"
                });
            }
            catch (InvalidOperationException ex)
            {
                return Ok(new UpdateAmountGoalResponse
                {
                    Success = false,
                    Message = "Xóa mục tiêu tiết kiệm không thành công"
                });
            }
        }
    }
}