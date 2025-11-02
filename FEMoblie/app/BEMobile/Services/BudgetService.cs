using Azure.Core;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.BudgetRR.CreateBudget;
using BEMobile.Models.RequestResponse.BudgetRR.DeleteBudget;
using BEMobile.Models.RequestResponse.BudgetRR.GetAllBudget;
using BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount;
using BEMobile.Models.RequestResponse.NotificationRR.PushNotification;
using BEMobile.Models.RequestResponse.NotificationRR.ReadNotification;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;


namespace BEMobile.Services
{
    public interface IBudgetService
    {

        Task<IEnumerable<BudgetDto>> GetAllBudgetsAsync(string userId);

        Task<CreateBudgetResponse> CreateBudgetByUserAsync(CreatBudgetRequest request);
        Task UpdateCurrentAmountByUserIdAsync(UpdateAmountRequest request);
        Task<UpdateAmountResponse> UpdateInitAmountByUserIdAsync(UpdateAmountRequest request);

        Task<DeleteBudgetResponse> DeleteBudgetAsync(DeleteBudgetRequest request);

        Task CheckAndCreateMonthlyBudgetsAsync();
    }
    public class BudgetService : IBudgetService
    {
        private readonly AppDbContext _context;
        private readonly INotificationService _notificationService;

        public BudgetService(AppDbContext context, INotificationService notificationService)
        {
            _context = context;
            _notificationService = notificationService;
        }


        public async Task<IEnumerable<BudgetDto>> GetAllBudgetsAsync(string userId)
        {
            if (_context.Budgets == null)
                throw new Exception("Budget DbSet is null in AppDbContext");

            var currentMonth = DateTime.UtcNow.Month;
            var currentYear = DateTime.UtcNow.Year;

            var allBudgets = await _context.Budgets
                .Where(x => x.UserId == userId)
                .ToListAsync();

            var filteredBudgets = allBudgets
                .Where(u =>
                {
                    if (DateTime.TryParseExact(u.CreatedDate,
                        "dd/MM/yyyy HH:mm:ss",
                        System.Globalization.CultureInfo.InvariantCulture,
                        System.Globalization.DateTimeStyles.None,
                        out DateTime created))
                    {
                        return created.Month == currentMonth && created.Year == currentYear;
                    }
                    return false;
                })
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
                .ToList();

            return filteredBudgets;
        }



        public async Task<CreateBudgetResponse> CreateBudgetByUserAsync(CreatBudgetRequest request)
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

            await _notificationService.PushNotificationAsync(new PushNotificationRequest
            {
                UserId = request.UserId,
                Content = "Tạo ngân sách thành công"
            });

            return new CreateBudgetResponse
            {
                Success = true,
                Message = "Tạo ngân sách OK",
                Budget = new BudgetDto
                {
                    BudgetId = Budget.BudgetId,
                    UserId = Budget.UserId,
                    Initial_Amount = Budget.Initial_Amount,
                    Current_Amount = Budget.Current_Amount,
                    CategoryId = Budget.CategoryId,
                    EndDate = Budget.EndDate,
                    CreatedDate = Budget.CreatedDate,
                    StartDate = Budget.StartDate
                }
            };
        }


        public async Task UpdateCurrentAmountByUserIdAsync(UpdateAmountRequest request)
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
                    var checkAdd = long.Parse(budget.Current_Amount) + long.Parse(request.UpdateAmount);
                    if (checkAdd <= long.Parse(budget.Initial_Amount))
                    {
                        budget.Current_Amount = checkAdd.ToString();
                    }
                    else
                    {
                        throw new ArgumentException("Số tiền vượt mức số tiền mặc định");
                    }

                }
                else
                {
                    var checkSub = long.Parse(budget.Current_Amount) - long.Parse(request.UpdateAmount);
                    if (checkSub >= 0)
                    {
                        budget.Current_Amount = checkSub.ToString();
                    }
                    else
                    {
                        throw new ArgumentException("Số tiền âm");
                    }
                }

                budget.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                _context.Budgets.Update(budget);
                await _context.SaveChangesAsync();

            }
            catch (Exception ex)
            {
                throw new Exception("Bị lỗi", ex);
            }
        }

        public async Task<UpdateAmountResponse> UpdateInitAmountByUserIdAsync(UpdateAmountRequest request)
        {
            try
            {
                var budget = await _context.Budgets
                    .FirstOrDefaultAsync(b => b.BudgetId == request.BudgetId);

                if (budget == null)
                {
                    throw new Exception("Không tìm thấy ngân sách (Budget).");

                }

                if (!long.TryParse(request.UpdateAmount, out long newAmount))
                {
                    throw new ArgumentException("Giá trị nhập vào không hợp lệ.");
                }

                if (newAmount < 0)
                {
                    throw new ArgumentException("Số tiền không được âm.");
                }

                if (!long.TryParse(budget.Current_Amount, out long currentAmount))
                {
                    currentAmount = 0; // nếu null hoặc rỗng thì mặc định 0
                }


                if (newAmount < currentAmount)
                {
                    throw new ArgumentException("Số tiền mới thấp hơn số tiền hiện có của ngân sách.");

                }



                budget.Initial_Amount = newAmount.ToString();

                budget.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                _context.Budgets.Update(budget);
                await _context.SaveChangesAsync();

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = request.UserId,
                    Content = $"Đã cập nhật số tiền ban đầu mới của một ngân sách."
                });

                return new UpdateAmountResponse
                {
                    Success = true,
                    Message = "Cập nhật số tiền thành công cho ngân sách"
                };
            }
            catch (Exception ex)
            {
                return new UpdateAmountResponse
                {
                    Success = true,
                    Message = "Cập nhật thất bại"
                };
            }
        }

        public async Task<DeleteBudgetResponse> DeleteBudgetAsync(DeleteBudgetRequest request)
        {
            var response = new DeleteBudgetResponse();

            try
            {
                if (string.IsNullOrEmpty(request.BudgetId))
                {
                    response.Success = false;
                    response.Message = "Thiếu BudgetId trong yêu cầu.";
                    return response;
                }

                var budget = await _context.Budgets
                    .FirstOrDefaultAsync(b => b.BudgetId == request.BudgetId);

                if (budget == null)
                {
                    response.Success = false;
                    response.Message = $"Không tìm thấy ngân sách có ID = {request.BudgetId}";
                    return response;
                }

                _context.Budgets.Remove(budget);
                await _context.SaveChangesAsync();

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = request.UserId,
                    Content = "Bạn đã xóa ngân sách"
                });

                response.Success = true;
                response.Message = "Xóa ngân sách thành công.";

                return response;
            }
            catch (Exception ex)
            {
                response.Success = false;
                response.Message = $"Lỗi khi xóa ngân sách: {ex.Message}";
                return response;
            }
        }

        public async Task CheckAndCreateMonthlyBudgetsAsync()
        {
            var now = DateTime.UtcNow;
            var currentMonth = now.Month;
            var currentYear = now.Year;

            var latestBudget = await _context.Budgets
                .OrderByDescending(b => b.CreatedDate)
                .FirstOrDefaultAsync();

            if (latestBudget == null)
            {
                Console.WriteLine(" Chưa có Budget nào trong DB, bỏ qua kiểm tra tháng mới.");
                return;
            }

            if (!DateTime.TryParseExact(latestBudget.CreatedDate,
                "dd/MM/yyyy HH:mm:ss",
                System.Globalization.CultureInfo.InvariantCulture,
                System.Globalization.DateTimeStyles.None,
                out DateTime latestCreated))
            {
                Console.WriteLine("❌ Không parse được CreatedDate của Budget mới nhất.");
                return;
            }

            if (latestCreated.Month < currentMonth || latestCreated.Year < currentYear)
            {
                Console.WriteLine(" Phát hiện tháng mới → khởi tạo Budget mới...");


                var lastMonthBudgets = _context.Budgets
                    .AsEnumerable()
                    .Where(b =>
                    {
                        if (DateTime.TryParseExact(b.CreatedDate,
                            "dd/MM/yyyy HH:mm:ss",
                            System.Globalization.CultureInfo.InvariantCulture,
                            System.Globalization.DateTimeStyles.None,
                            out DateTime created))
                        {
                            return created.Month == latestCreated.Month && created.Year == latestCreated.Year;
                        }
                    return false;
                    })
                    .ToList();




                if (!lastMonthBudgets.Any())
                {
                    Console.WriteLine(" Không tìm thấy Budget của tháng trước.");
                    return;
                }

                var newBudgets = lastMonthBudgets.Select(old => new Budget
                {
                    BudgetId = Guid.NewGuid().ToString(),
                    UserId = old.UserId,
                    CategoryId = old.CategoryId,
                    Initial_Amount = old.Initial_Amount,
                    Current_Amount = "0",
                    StartDate = old.StartDate,
                    EndDate = old.EndDate,
                    CreatedDate = now.ToString("dd/MM/yyyy HH:mm:ss"),
                    UpdatedDate = now.ToString("dd/MM/yyyy HH:mm:ss")
                }).ToList();

                await _context.Budgets.AddRangeAsync(newBudgets);
                await _context.SaveChangesAsync();

                Console.WriteLine($"Đã tạo {newBudgets.Count} Budget mới cho tháng {currentMonth}/{currentYear}");
            }
            else
            {
                Console.WriteLine(" Vẫn trong cùng tháng, không cần tạo Budget mới.");
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