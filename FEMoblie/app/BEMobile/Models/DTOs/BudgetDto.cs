using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.DTOs
{
    public class BudgetDto
    {
        [Key]
        public string BudgetId { get; set; }
        [Required]
        public string? UserId { get; set; }
        public string? CategoryId { get; set; }
        public string? Initial_Amount { get; set; }
        public string? Current_Amount { get; set; }
        public string? StartDate { get; set; }
        public string? EndDate { get; set; }
        public string? CreatedDate { get; set; }
        public string? UpdatedDate { get; set; }
    }
}
