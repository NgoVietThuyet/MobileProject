using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.SavingGoal.Create
{
    public class CreateSavingGoalResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public SavingGoalDto SavingGoal { get; set; }
    }
}
