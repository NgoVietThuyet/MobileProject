using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.Category
{
    public class CategoryResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public CategoryDto Category { get; set; }
    }
}
