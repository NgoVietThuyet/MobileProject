using BEMobile.Models.DTOs;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.BudgetRR.CreateBudget
{
    public class Request
    {
        [Required]
        public string UserId { get; set; }
        public string CategoryId { get; set; }
        
        public string Initial_Amount { get; set; }
        public string Current_Amount { get; set; } = "0";
        
        public string StartDate { get; set; }
        public string EndDate { get; set; }
    }
}
