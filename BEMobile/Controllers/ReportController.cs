using BEMobile.Models.RequestResponse.ReportRR;
using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;

namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ReportsController : ControllerBase
    {
        private readonly IReportService _reportService;

        public ReportsController(IReportService reportService)
        {
            _reportService = reportService;
        }

        [HttpGet("export-template")]
        public async Task<IActionResult> ExportReportByTemplate([FromQuery] GenerateExcelReportRequest reportRequest)
        {
            try
            {
                var excelBytes = await _reportService.GenerateExcelReportByTemplateAsync(reportRequest);

                return File(excelBytes,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    $"BaoCaoTaiChinh_{reportRequest.StartDate:ddMMyyyy}_{reportRequest.EndDate:ddMMyyyy}.xlsx");
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Lỗi khi tạo báo cáo: {ex.Message}");
            }
        }
    }
    
}
