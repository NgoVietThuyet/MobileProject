using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Models.DTOs
{
    public class ReportDto
    {
        public string ReportId { get; set; }
        
        public string UserId { get; set; }
       
        public string PeriodType { get; set; }
       
        public string PeriodStart { get; set; }
       
        public string PeriodEnd { get; set; }
     
        public string TotalIncome { get; set; }
      
        public string TotalExpense { get; set; }
        
        public string TotalSaving { get; set; }
       
        public string CreatedDate { get; set; }
      
        public string UpdatedDate { get; set; }
    }
}
