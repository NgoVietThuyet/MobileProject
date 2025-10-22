using BEMobile.Data.Entities;
using Microsoft.EntityFrameworkCore;
using BEMobile.Models.DTOs;

using BEMobile.Models.RequestResponse.SavingGoal.UpdateAmount;
using BEMobile.Models.RequestResponse.SavingGoal;

namespace BEMobile.Services
{
    public interface ISavingGoalService
    {
        Task<IEnumerable<SavingGoalDto>> GetAllSavingGoalAsync();
        Task<SavingGoal> CreateSavingGoalAsync(CreateSavingGoalRequest request);
        Task UpdateAmountAsync(UpdateAmountGoalRequest request);
        ////Task UpdateBudgetAsync(BudgetDto BudgetDto);
        //Task<bool> DeleteBudgetAsync(string id);

    }
    public class SavingGoalService : ISavingGoalService
    {
        private readonly AppDbContext _context;

        public SavingGoalService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<SavingGoalDto>> GetAllSavingGoalAsync()
        {
            if (_context.SavingGoals == null)
                throw new Exception("SavingGoal DbSet is null in AppDbContext");
            var SavingGoal = await _context.SavingGoals
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
                }
                else
                {
                    savingGoal.CurrentAmount =
                        (long.Parse(savingGoal.CurrentAmount) - long.Parse(request.UpdateAmount)).ToString();
                }

                savingGoal.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                _context.SavingGoals.Update(savingGoal);
                await _context.SaveChangesAsync();

            }
            catch (Exception ex)
            {
                throw new Exception("Bị lỗi", ex);
            }
        }
        //public async Task<bool> DeleteBudgetAsync(string id)
        //{
        //    try
        //    {
        //        // 1️⃣ Tìm Budget theo ID
        //        var budget = await _context.Budgets
        //            .FirstOrDefaultAsync(b => b.BudgetId == id);

        //        // 2️⃣ Kiểm tra nếu không tồn tại
        //        if (budget == null)
        //        {
        //            return false; // Không có budget nào trùng ID
        //        }

        //        // 3️⃣ Xóa entity
        //        _context.Budgets.Remove(budget);

        //        // 4️⃣ Lưu thay đổi
        //        await _context.SaveChangesAsync();

        //        return true; // Xóa thành công
        //    }
        //    catch (Exception ex)
        //    {
        //        // 5️⃣ Ném lỗi có thông tin chi tiết
        //        throw new Exception($"Lỗi khi xóa Budget có ID = {id}", ex);
        //    }
        //}


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