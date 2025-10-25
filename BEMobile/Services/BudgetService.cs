using BEMobile.Data.Entities;
using Microsoft.EntityFrameworkCore;
using BEMobile.Models.DTOs;


using BEMobile.Models.RequestResponse.BudgetRR.GetAllBudget;
using BEMobile.Models.RequestResponse.BudgetRR.CreateBudget;
using BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount;
using Microsoft.AspNetCore.Mvc;


namespace BEMobile.Services
{
    public interface IBudgetService
    {

        Task<IEnumerable<BudgetDto>> GetAllBudgetsAsync(string userId);

        Task<Budget> CreateBudgetByUserAsync(Request request);
        Task UpdateAmountByUserIdAsync(UpdateAmountRequest request);
        //Task UpdateBudgetAsync(BudgetDto BudgetDto);
        Task<bool> DeleteBudgetAsync(string id);
        //Task<IEnumerable<BudgetDto>> SearchBudgetsAsync(string? name, string? email, string? phoneNumber);
    }
    public class BudgetService : IBudgetService
    {
        private readonly AppDbContext _context;

        public BudgetService(AppDbContext context)
        {
            _context = context;
        }


        public async Task<IEnumerable<BudgetDto>> GetAllBudgetsAsync(string userId)

        {
            if (_context.Budgets == null)
                throw new Exception("Budget DbSet is null in AppDbContext");
            var Budgets = await _context.Budgets

                .Where(x  => x.UserId == userId)

                .Select(u => new BudgetDto
                {
                    UserId = u.UserId,
                    BudgetId = u.BudgetId,
                    CategoryId = u.CategoryId,
                    Initial_Amount = u.Initial_Amount,
                    Current_Amount = u.Current_Amount,
                    StartDate = u.StartDate,
                    EndDate = u.EndDate,
                    CreatedDate = u.CreatedDate,
                    UpdatedDate = u.UpdatedDate,

                })
                .ToListAsync();
            return Budgets;
        }

        public async Task<Budget> CreateBudgetByUserAsync(Request request)
        {

            var Budget = new Budget
            {
                BudgetId = Guid.NewGuid().ToString(),
                UserId = request.UserId,
                Initial_Amount = request.Initial_Amount,
                Current_Amount = "0",
                CategoryId = request.CategoryId,
                EndDate = request.EndDate,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss"),
                StartDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };

            _context.Budgets.Add(Budget);
            await _context.SaveChangesAsync();
            return Budget;
        }
        public async Task UpdateAmountByUserIdAsync(UpdateAmountRequest request)
        {
            try
            {
                var budget = await _context.Budgets
                            .FirstOrDefaultAsync(b => b.BudgetId == request.BudgetId);

                if (budget == null)
                {
                    throw new Exception("Không tìm thấy Budget");
                }

                if (request.isAddAmount)
                {
                    budget.Current_Amount =
                        (long.Parse(budget.Current_Amount) + long.Parse(request.UpdateAmount)).ToString();
                }
                else
                {
                    budget.Current_Amount =
                        (long.Parse(budget.Current_Amount) - long.Parse(request.UpdateAmount)).ToString();
                }

                budget.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");
   
                _context.Budgets.Update(budget);
                    await _context.SaveChangesAsync();
                
            }
            catch (Exception ex)
            {
                throw new Exception("Bị lỗi",ex);
            }
        }
        public async Task<bool> DeleteBudgetAsync(string id)
        {
            try
            {
                // 1️⃣ Tìm Budget theo ID
                var budget = await _context.Budgets
                    .FirstOrDefaultAsync(b => b.BudgetId == id);

                // 2️⃣ Kiểm tra nếu không tồn tại
                if (budget == null)
                {
                    return false; // Không có budget nào trùng ID
                }

                // 3️⃣ Xóa entity
                _context.Budgets.Remove(budget);

                // 4️⃣ Lưu thay đổi
                await _context.SaveChangesAsync();

                return true; // Xóa thành công
            }
            catch (Exception ex)
            {
                // 5️⃣ Ném lỗi có thông tin chi tiết
                throw new Exception($"Lỗi khi xóa Budget có ID = {id}", ex);
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