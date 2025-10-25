using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.BudgetRR.DeleteBudget
{
    public class DeleteBudgetRequest
    {
        [Required]
        public string BudgetId { get; set; }

        public string UserId { get; set; }
    }
}
