﻿using Azure.Core;
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
        Task UpdateAmountByUserIdAsync(UpdateAmountRequest request);
        //Task UpdateBudgetAsync(BudgetDto BudgetDto);
        Task<DeleteBudgetResponse> DeleteBudgetAsync(DeleteBudgetRequest request);
        //Task<IEnumerable<BudgetDto>> SearchBudgetsAsync(string? name, string? email, string? phoneNumber);
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

            return new CreateBudgetResponse { 
                Success = true, 
                Message = "Tạo ngân sách OK", 
                Budget = new BudgetDto { 
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

                await _notificationService.PushNotificationAsync(new PushNotificationRequest
                {
                    UserId = request.UserId,
                    Content = "Cập nhật số tiền mới cho ngân sách"
                });

            }
            catch (Exception ex)
            {
                throw new Exception("Bị lỗi",ex);
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