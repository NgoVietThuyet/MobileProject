using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.BudgetRR.CreateBudget
{
    public class CreateBudgetResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public BudgetDto? Budget { get; set; }
    }
}
