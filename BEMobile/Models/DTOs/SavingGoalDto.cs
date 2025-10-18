using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Models.DTOs
{
    public class SavingGoalDto
    {
        public string GoalId { get; set; }
      
        public string? UserId { get; set; }
        
        public string? CategoryId { get; set; }
       
        public string? Titile { get; set; }
       
        public string? TargetAmount { get; set; }
    
        public string? CurrentAmount { get; set; }
      
        public string? Deadline { get; set; }
        public string? CreatedDate { get; set; }
        public string? UpdatedDate { get; set; }
    }
}
