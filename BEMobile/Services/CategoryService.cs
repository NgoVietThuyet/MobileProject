using Azure.Core;
using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Budget.CreateBudget;
using BEMobile.Models.RequestResponse.Budget.UpdateAmount;
using BEMobile.Models.RequestResponse.Category;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface ICategoryService
    {
        Task<IEnumerable<CategoryDto>> GetAllCategorysAsync();
        
        Task<Category> CreateCategoryAsync(CategoryRequest request);
        Task UpdateCategoryAsync(CategoryRequest request);
    }
    public class CategoryService : ICategoryService
    {
        private readonly AppDbContext _context;

        public CategoryService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<CategoryDto>> GetAllCategorysAsync()
        {
            if (_context.Categories == null)
                throw new Exception("Category DbSet is null in AppDbContext");
            var Categories = await _context.Categories
                .Select(u => new CategoryDto
                {
                    Id = u.Id,
                    Icon = u.Icon,
                    Name = u.Name,
                    CreatedDate = u.CreatedDate,
                    UpdatedDate = u.UpdatedDate,

                })
                .ToListAsync();

            return Categories;
        }
        

        public async Task<Category> CreateCategoryAsync(CategoryRequest request)
        {
            Console.WriteLine("📦 Request Name: " + request.categoryDto?.Name);

            var Category = new Category
            {
                Id = Guid.NewGuid().ToString(),
                Icon = request.categoryDto.Icon,
                Name = request.categoryDto.Name,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss"),
                UpdatedDate= null
            };

            _context.Categories.Add(Category);
            await _context.SaveChangesAsync();
            return Category;
        }
        public async Task UpdateCategoryAsync(CategoryRequest request)
        {
            try
            {
                var Category = await _context.Categories
                            .FirstOrDefaultAsync(b => b.Id == request.categoryDto.Id);

                if (Category == null)
                {
                    throw new Exception("Không tìm thấy Category");
                }
                Category.Icon = request.categoryDto.Icon;
                Category.Name = request.categoryDto.Name;


                Category.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                _context.Categories.Update(Category);
                await _context.SaveChangesAsync();

            }
            catch (Exception ex)
            {
                throw new Exception("Bị lỗi", ex);
            }
        }
        
    }
}