using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using BEMobile.Models.RequestResponse.SavingGoalRR;
using BEMobile.Models.RequestResponse.SavingGoalRR.Delete;
using BEMobile.Models.RequestResponse.SavingGoalRR.UpdateAmount;
using Microsoft.EntityFrameworkCore;
namespace BEMobile.Services
{
    public interface ISavingGoalService
    {

        Task<IEnumerable<SavingGoalDto>> GetAllSavingGoalAsync(string userId);
        Task<SavingGoal> CreateSavingGoalAsync(CreateSavingGoalRequest request);
        Task UpdateAmountAsync(UpdateAmountGoalRequest request);
        ////Task UpdateBudgetAsync(BudgetDto BudgetDto);
        Task DeleteSavingGoalAsync(DeleteSavingRequest deleteSavingRequest);

    }
    public class SavingGoalService : ISavingGoalService
    {
        private readonly AppDbContext _context;
        private readonly INotificationService _notificationService;
        private readonly IAccountService _accountService;

        public SavingGoalService(AppDbContext context, INotificationService notificationService, IAccountService accountService)
        {
            _context = context;
            _notificationService = notificationService;
            _accountService = accountService;
        }


        public async Task<IEnumerable<SavingGoalDto>> GetAllSavingGoalAsync(string userId)

        {
            if (_context.SavingGoals == null)
                throw new Exception("SavingGoal DbSet is null in AppDbContext");
            var SavingGoal = await _context.SavingGoals
                .Where(x => x.UserId == userId)

                .Select(u => new SavingGoalDto
                {
                    UserId = u.UserId,
                    GoalId = u.GoalId,
                    Title = u.Title,
                    CategoryId = u.CategoryId,
                    TargetAmount = u.TargetAmount,
                    CurrentAmount = u.CurrentAmount,
                    Deadline   = u.Deadline,
                    CreatedDate = u.CreatedDate,
                    UpdatedDate = u.UpdatedDate,

                })
                .ToListAsync();

            return SavingGoal;
        }

        public async Task<SavingGoal> CreateSavingGoalAsync(CreateSavingGoalRequest request)
        {

            var savingGoal = new SavingGoal
            {
                GoalId = Guid.NewGuid().ToString(),
                UserId = request.savingGoalDto.UserId,
                CategoryId = request.savingGoalDto.CategoryId,
                Deadline = request.savingGoalDto.Deadline,
                Title = request.savingGoalDto.Title,
                TargetAmount = request.savingGoalDto.TargetAmount,
                CurrentAmount = request.savingGoalDto.CurrentAmount,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss"),
            };

            _context.SavingGoals.Add(savingGoal);
            await _context.SaveChangesAsync();

            string formattedAmount = long.TryParse(savingGoal.TargetAmount, out var amount)
        ? amount.ToString("N0")
        : savingGoal.TargetAmount;

            string deadlineText = string.IsNullOrEmpty(savingGoal.Deadline)
                ? "không có hạn chót"
                : $"hạn đến {savingGoal.Deadline}";

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = savingGoal.UserId,
                Content = $" Bạn đã tạo mục tiêu tiết kiệm **'{savingGoal.Title}'** " +
                          $"với số tiền mục tiêu **{formattedAmount} VNĐ**, {deadlineText}."
            });
            return savingGoal;
        }
        public async Task UpdateAmountAsync(UpdateAmountGoalRequest request)
        {
            try
            {
                var savingGoal = await _context.SavingGoals
                            .FirstOrDefaultAsync(b => b.GoalId == request.GoalId);

                if (savingGoal == null)
                {
                    throw new Exception("Không tìm thấy SavingGoal");
                }

                if (request.isAddAmount)
                {
                    savingGoal.CurrentAmount =
                        (long.Parse(savingGoal.CurrentAmount) + long.Parse(request.UpdateAmount)).ToString();
                    
                    await _accountService.UpdateBalanceAsync(
                    savingGoal.UserId,
                    long.Parse(request.UpdateAmount),
                    isIncrease: false // trừ tiền tài khoản khi thêm vào saving goal
                    );
                }
                else
                {
                    savingGoal.CurrentAmount =
                        (long.Parse(savingGoal.CurrentAmount) - long.Parse(request.UpdateAmount)).ToString();

                    await _accountService.UpdateBalanceAsync(
                    savingGoal.UserId,
                    long.Parse(request.UpdateAmount),
                    isIncrease: true // trừ tiền tài khoản khi thêm vào saving goal
                    );
                }

                savingGoal.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                _context.SavingGoals.Update(savingGoal);
                await _context.SaveChangesAsync();

                string actionText = request.isAddAmount ? "thêm" : "rút";
                string formattedUpdate = request.UpdateAmount;
                string formattedTotal = long.Parse(savingGoal.CurrentAmount).ToString("N0");

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = savingGoal.UserId,
                    Content = $"💰 Bạn vừa **{actionText} {formattedUpdate} VNĐ** " +
                              $"cho mục tiêu **'{savingGoal.Title}'**. " +
                              $"Số tiền hiện tại: **{formattedTotal} VNĐ**."
                });



            }
            catch (Exception ex)
            {
                throw new Exception("Bị lỗi", ex);
            }
        }
        public async Task DeleteSavingGoalAsync(DeleteSavingRequest deleteSavingRequest)
        {
            try
            {
     
                var saving = await _context.SavingGoals
                    .FirstOrDefaultAsync(b => b.GoalId == deleteSavingRequest.id);

                string formattedAmount = long.TryParse(saving.CurrentAmount, out var amount)
            ? amount.ToString("N0")
            : saving.CurrentAmount;

            
                _context.SavingGoals.Remove(saving);

          
                await _context.SaveChangesAsync();

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = saving.UserId,
                    Content = $"🗑️ Bạn đã xóa mục tiêu tiết kiệm **'{saving.Title}'** " +
                      $"với số tiền hiện có là **{formattedAmount} VNĐ**."
                });
            }
            catch (Exception ex)
            {
               
                throw new Exception($"Lỗi khi xóa Budget có Id", ex);
            }
        }


        //public async Task<IEnumerable<BudgetDto>> SearchBudgetsAsync(string? name, string? email, string? phoneNumber)
        //{
        //    var query = _context.Budgets.AsQueryable();

        //    if (!string.IsNullOrEmpty(name))
        //        query = query.Where(u => u.Name.Contains(name));

        //    if (!string.IsNullOrEmpty(email))
        //        query = query.Where(u => u.Email.Contains(email));

        //    if (!string.IsNullOrEmpty(phoneNumber))
        //        query = query.Where(u => u.PhoneNumber.Contains(phoneNumber));

        //    var Budgets = await query
        //        .Select(u => new BudgetDto
        //        {
        //            BudgetId = u.BudgetId,
        //            Name = u.Name,
        //            PhoneNumber = u.PhoneNumber,
        //            Facebook = u.Facebook,
        //            Twitter = u.Twitter,
        //            Email = u.Email
        //        })

        //        .ToListAsync();

        //    return Budgets;
        //}

    }
}