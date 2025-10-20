using Microsoft.Identity.Client;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.Budget.UpdateAmount
{
    public class UpdateAmountRequest
    {
        [Required]
        public string BudgetId { get; set; }
        public string UpdateAmount { get; set; }
        public bool isAddAmount { get; set; }
    }
}
