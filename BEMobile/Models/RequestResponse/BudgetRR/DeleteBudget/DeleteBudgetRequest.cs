using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.Budget.DeleteBudget
{
    public class DeleteBudgetRequest
    {
        [Required]
        public string BudgetId { get; set; }
    }
}
