using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Budget.CreateBudget;
using BEMobile.Models.RequestResponse.Budget.DeleteBudget;
using BEMobile.Models.RequestResponse.Budget.UpdateAmount;
using BEMobile.Models.RequestResponse.Category;
using BEMobile.Models.RequestResponse.Login;
using BEMobile.Models.RequestResponse.SignUp;
using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Build.Framework;


namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class CategoryController : ControllerBase
    {
        private readonly ICategoryService _CategoryService;

        public CategoryController(ICategoryService CategoryService)
        {
            _CategoryService = CategoryService;
        }

        [HttpGet("GetAllCategories")]
        public async Task<ActionResult<IEnumerable<CategoryDto>>> GetAllCategory()
        {
            try
            {
                var Categories = await _CategoryService.GetAllCategorysAsync();
                return Ok(Categories);
            }
            catch (Exception ex)
            {
                return BadRequest(ex.Message);

            }
        }


        [HttpPost("Create")]
        public async Task<ActionResult<Category>> CreateCategoryAsync([FromBody] CategoryRequest request)
        {
            try
            {
                var category = await _CategoryService.CreateCategoryAsync(request);
                if (User == null)
                {
                    return Unauthorized(new CategoryResponse
                    {
                        Success = false,
                        Message = "Đăng ký thất bại"
                    });
                }
                else
                {
                    var response = new CategoryResponse
                    {
                        Success = true,
                        Message = "Đăng ký thành công",
                        Category = new CategoryDto
                        {
                            Id = category.Id,
                            Icon = category.Icon,
                            CreatedDate = category.CreatedDate,
                            UpdatedDate = category.UpdatedDate
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
        public async Task<ActionResult<Budget>> UpdateUser(CategoryRequest request)
        {
            try
            {
                await _CategoryService.UpdateCategoryAsync(request);

                return Ok(new CategoryResponse
                {
                    Success = true,
                    Message = "Cập nhật thư mục thành công"
                });
            }
            catch (InvalidOperationException ex)
            {
                return Ok(new CategoryResponse
                {
                    Success = false,
                    Message = "Cập nhật thư mục không thành công"
                });
            }
        }


    }
}