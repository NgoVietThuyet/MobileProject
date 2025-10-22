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
        public async Task<IActionResult> ExportReportByTemplate(
            string userId,
            DateTime startDate,
            DateTime endDate)
        {
            try
            {
                var excelBytes = await _reportService.GenerateExcelReportByTemplateAsync(userId, startDate, endDate);

                return File(excelBytes,
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    $"BaoCaoTaiChinh_{startDate:ddMMyyyy}_{endDate:ddMMyyyy}.xlsx");
            }
            catch (Exception ex)
            {
                return StatusCode(500, $"Lỗi khi tạo báo cáo: {ex.Message}");
            }
        }
    }
    
}
