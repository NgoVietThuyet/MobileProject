
using System.ComponentModel.DataAnnotations;

using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("SAVING_GOALS")]
    public class SavingGoal
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("GOAL_ID")]
        public string GoalId { get; set; }

        [Column("USER_ID")]
        public string? UserId { get; set; }
        [Column("CATEGORY_ID")]
        public string? CategoryId { get; set; }
        [Column("TITLE")]
        public string? Title { get; set; }
        [Column("TARGET_AMOUNT")]
        public string? TargetAmount { get; set; }
        [Column("CURRENT_AMOUNT")]
        public string? CurrentAmount { get; set; }
        [Column("DEADLINE")]
        public string? Deadline { get; set; }
        [Column("CREATED_DATE")]
        public string? CreatedDate { get; set; }
        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }
    }
}

