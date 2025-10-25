using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table ("REPORTS")]
    public class Reports
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("REPORT_ID")]
        public string ReportId { get; set; }
        [Column("USER_ID")]
        public string UserId { get; set;}
        [Column("PERIOD_TYPE")]
        public string PeriodType { get; set; }
        [Column("PERIOD_START")]
        public string PeriodStart { get; set; }
        [Column("PERIOD_END")]
        public string PeriodEnd { get; set; }
        [Column("TOTAL_INCOME")]
        public string TotalIncome { get; set; }
        [Column("TOTAL_EXPENSE")]
        public string TotalExpense { get; set; }
        [Column("TOTAL_SAVING")]
        public string TotalSaving { get; set; }
        [Column("CREATED_DATE")]
        public string CreatedDate { get; set; }
        [Column("UPDATED_DATE")]
        public string UpdatedDate { get; set; }
    }
}
