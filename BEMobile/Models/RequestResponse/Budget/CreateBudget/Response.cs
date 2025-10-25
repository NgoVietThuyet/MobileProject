using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.Budget.CreateBudget
{
    public class Response
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public BudgetDto Budget { get; set; }
    }
}
