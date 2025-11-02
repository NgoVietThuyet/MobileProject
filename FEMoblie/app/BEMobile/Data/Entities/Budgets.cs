using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("BUDGETS")]
    public class Budget
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("BUDGET_ID")]
        public string BudgetId { get; set; }

        [Required]
        [Column("USER_ID")]
        public string? UserId { get; set; }

        [Required]
        [Column("CATEGORY_ID")]
        public string? CategoryId { get; set; }

        
        [Column("INITIAL_AMOUNT")]
        public string? Initial_Amount { get; set; }
        [Column("CURRENT_AMOUNT")]
        public string? Current_Amount { get; set; }

        [Column("START_DATE")]
        public string? StartDate { get; set; }

        [Column("END_DATE")]
        public string? EndDate { get; set; }

        [Column("CREATED_DATE")]
        public string? CreatedDate { get; set; }
        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }

    }
}