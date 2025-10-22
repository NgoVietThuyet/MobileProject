using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.SavingGoalRR.UpdateAmount
{
    public class UpdateAmountGoalRequest
    {
        [Required]
        public string GoalId { get; set; }
        public string UpdateAmount { get; set; }
        public bool isAddAmount { get; set; }
    }
}
