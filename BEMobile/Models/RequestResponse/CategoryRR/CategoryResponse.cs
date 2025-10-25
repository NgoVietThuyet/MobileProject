using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.CategoryRR
{
    public class CategoryResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public CategoryDto Category { get; set; }
    }
}
