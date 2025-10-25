using Microsoft.Identity.Client;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.BudgetRR.UpdateAmount
{
    public class UpdateAmountRequest
    {
        [Required]
        public string BudgetId { get; set; }
        public string UpdateAmount { get; set; }
        public bool isAddAmount { get; set; }
    }
}
