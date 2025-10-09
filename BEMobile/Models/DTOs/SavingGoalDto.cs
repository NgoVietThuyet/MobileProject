using BEMobile.Data.Entities;
using System.Text.Json.Serialization;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace BEMobile.Models.DTOs
{
    public class SavingGoalDto
    {
        public string GoalId { get; set; }
        public string UserId { get; set; }
        public string CategoryId { get; set; }
        public string? Title { get; set; }
        public float TargetAmount { get; set; }
        public float CurrentAmount { get; set; }
        public string Deadline { get; set; }
        public string CreatedDate { get; set; }
        public string? UpdatedDate { get; set; }
    }
}
