namespace BEMobile.Models.RequestResponse.ReportRR
{
    public class GenerateExcelReportRequest
    {
        public string UserId { get; set; }
        public DateTime StartDate { get; set; }   
        public DateTime EndDate { get; set; }
    }
}
