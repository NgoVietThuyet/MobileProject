using BEMobile.Data.Entities;
using BEMobile.Models.RequestResponse.ReportRR;

using Microsoft.EntityFrameworkCore;
using OfficeOpenXml;
using OfficeOpenXml.Style;
using System.Drawing;
using System.Globalization;
using Spire.Xls;
using Font = System.Drawing.Font;
using ExcelHorizontalAlignment = OfficeOpenXml.Style.ExcelHorizontalAlignment;
namespace BEMobile.Services
{
    public interface IReportService
    {
        Task<byte[]> GenerateExcelReportByTemplateAsync(GenerateExcelReportRequest reportRequest);
        Task<byte[]> GeneratePdfReportByTemplateAsync(GenerateExcelReportRequest reportRequest);

    }

    public class ReportService : IReportService
    {
        private readonly AppDbContext _context;
        private readonly ICategoryService _categoryService;

        public ReportService(AppDbContext context, ICategoryService categoryService)
        {
            _context = context;
            _categoryService = categoryService;
            ExcelPackage.LicenseContext = LicenseContext.NonCommercial;
        }
        public async Task<byte[]> GenerateExcelReportByTemplateAsync(GenerateExcelReportRequest reportRequest)
        {
            //Lấy toàn bộ transactions của user 
                var allTransactions = await _context.Transactions
                    .Where(t => t.UserId == reportRequest.UserId)
                    .AsNoTracking()
                    .ToListAsync();
            DateTime? ParseDate(string dateString)
            {
                if (string.IsNullOrEmpty(dateString)) return null;

                string[] formats = {
                "dd/MM/yyyy HH:mm:ss",
                "yyyy-MM-dd HH:mm:ss",
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "MM/dd/yyyy HH:mm:ss",
                "MM/dd/yyyy"
            };

                if (DateTime.TryParseExact(dateString, formats, CultureInfo.InvariantCulture, DateTimeStyles.None, out DateTime result))
                {
                    return result;
                }

                return null;
            }

            // Lọc các bản ghi theo khoảng thời gian
            var transactions = allTransactions
                .Where(t =>
                {
                    var createdDate = ParseDate(t.CreatedDate);
                    return createdDate.HasValue &&
                           createdDate.Value.Date >= reportRequest.StartDate.Date &&
                           createdDate.Value.Date <= reportRequest.EndDate.Date;
                })
                .OrderBy(t =>
                {
                    var updatedDate = ParseDate(t.UpdatedDate);
                    var createdDate = ParseDate(t.CreatedDate);
                    return updatedDate ?? createdDate ?? DateTime.MinValue;
                })
                .ToList();
            // Lấy tất cả các categories từ DB
            var categoryDict = await _context.Categories.ToDictionaryAsync(c => c.Id, c => c.Name);

            // Tách income và expense
            var incomeTransactions = transactions.Where(t => t.Type == "INCOME").ToList();
            var expenseTransactions = transactions.Where(t => t.Type == "EXPENSE").ToList();

            using var package = new ExcelPackage();
            var worksheet = package.Workbook.Worksheets.Add("Báo cáo");

            // Tạo header theo template
            CreateTemplateHeader(worksheet);

            // Điền dữ liệu income
            FillIncomeData(worksheet, incomeTransactions, categoryDict);

            // Điền dữ liệu expense - TRUYỀN THÊM incomeCount để xác định vị trí bắt đầu
            FillExpenseData(worksheet, expenseTransactions, incomeTransactions.Count, categoryDict);

            // Tính tổng và công thức - TRUYỀN THÊM incomeCount
            //CalculateTotals(worksheet, incomeTransactions.Count, expenseTransactions.Count, incomeTransactions.Count);

            // Định dạng - TRUYỀN THÊM incomeCount
            FormatWorksheet(worksheet, incomeTransactions.Count, expenseTransactions.Count, incomeTransactions.Count);
            worksheet.Workbook.Worksheets[worksheet.Index].PrinterSettings.HorizontalCentered = true;
            worksheet.PrinterSettings.FitToPage = true;
            worksheet.PrinterSettings.FitToWidth = 1;   // 1 trang theo chiều rộng
            worksheet.PrinterSettings.FitToHeight = 0;  // chiều cao tự do

            return package.GetAsByteArray();
        }
        private void CreateTemplateHeader(ExcelWorksheet worksheet)
        {

            // Header THU NHẬP (giữ nguyên)
            worksheet.Cells["A1"].Value = "THU NHẬP";
            worksheet.Cells["A1"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
            worksheet.Cells["A1:E1"].Merge = true;

            worksheet.Cells["A2"].Value = "STT";
            worksheet.Cells["B2"].Value = "Tên thu nhập";
            worksheet.Cells["C2"].Value = "Giá tiền";
            worksheet.Cells["D2"].Value = "Thời gian";
            worksheet.Cells["E2"].Value = "Ghi chú";

            // Định dạng header thu nhập
            var headerCells = new[] { "A1:E1", "A2", "B2", "C2", "D2", "E2"};
            foreach (var cell in headerCells)
            {
                worksheet.Cells[cell].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                worksheet.Cells[cell].Style.Font.Bold = true;
                worksheet.Cells[cell].Style.Fill.PatternType = ExcelFillStyle.Solid;
                worksheet.Cells[cell].Style.Fill.BackgroundColor.SetColor(Color.LightGray);
                worksheet.Cells[cell].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            }

            // KHÔNG tạo header chi tiêu ở đây nữa, vì đã tạo trong FillExpenseData
        }
        private void FillIncomeData(ExcelWorksheet worksheet, List<Transaction> incomeTransactions, Dictionary<string, string> categoryDict)
        {
            int startRow = 3;
            worksheet.Cells[$"A{3}:A{incomeTransactions.Count + 2}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"B{3}:B{incomeTransactions.Count + 2}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"C{3}:C{incomeTransactions.Count + 2}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"D{3}:D{incomeTransactions.Count + 2}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"E{3}:E{incomeTransactions.Count + 2}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            for (int i = 0; i < incomeTransactions.Count; i++)
            {
                var transaction = incomeTransactions[i];
                var row = startRow + i;
                var displayDate = transaction.UpdatedDate ?? transaction.CreatedDate;

                worksheet.Cells[$"A{row}"].Value = i + 1; // STT
                worksheet.Cells[$"A{row}:E{row}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);


                if (transaction.CategoryId != null && categoryDict.TryGetValue(transaction.CategoryId, out string name))
                {
                    worksheet.Cells[$"B{row}"].Value = name; // Tên thu nhập
                }

                if (decimal.TryParse(transaction.Amount, out decimal amount))
                {
                    worksheet.Cells[$"C{row}"].Value = amount;
                }
                worksheet.Cells[$"D{row}"].Value = displayDate; // Thời gian
                worksheet.Cells[$"E{row}"].Value = transaction.Note; // Ghi chú
            }

            // Tổng thu nhập
            if (incomeTransactions.Any())
            {
                int totalRow = startRow + incomeTransactions.Count;
                worksheet.Cells[$"A{incomeTransactions.Count + 4}"].Value = "Tổng thu nhập";
                worksheet.Cells[$"B{incomeTransactions.Count + 4} "].Formula = $"SUM(C{startRow}:C{totalRow - 1})";
                worksheet.Cells[$"B{incomeTransactions.Count + 4} "].Style.Numberformat.Format = "#,##0";
                worksheet.Cells[$"A{incomeTransactions.Count + 4}"].Style.Font.Bold = true;
                worksheet.Cells[$"B{incomeTransactions.Count+ 4 }"].Style.Font.Bold = true;
            }
        }
        private void FillExpenseData(ExcelWorksheet worksheet, List<Transaction> expenseTransactions, int incomeCount, Dictionary<string, string> categoryDict)
        {
            // Bắt đầu từ dòng cuối cùng của bảng thu nhập + 3 dòng (2 dòng trống + 1 dòng header)
            int startRow = 3 + incomeCount + 5;

            // Header CHI TIÊU - đặt ở vị trí mới
            worksheet.Cells[$"A{startRow - 1}"].Value = "CHI TIÊU";
            worksheet.Cells[$"A{startRow - 1}:E{startRow - 1}"].Merge = true;
            worksheet.Cells[$"A{startRow-1}"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;

            worksheet.Cells[$"A{startRow}"].Value = "STT";
            worksheet.Cells[$"B{startRow}"].Value = "Tên giao dịch";
            worksheet.Cells[$"C{startRow}"].Value = "Giá tiền";
            worksheet.Cells[$"D{startRow}"].Value = "Thời gian";
            worksheet.Cells[$"E{startRow}"].Value = "Ghi chú";
            worksheet.Cells[$"A{startRow}:A{expenseTransactions.Count + startRow }"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"B{startRow}:B{expenseTransactions.Count + startRow}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"C{startRow}:C{expenseTransactions.Count + startRow}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"D{startRow}:D{expenseTransactions.Count + startRow}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            worksheet.Cells[$"E{startRow}:E{expenseTransactions.Count + startRow}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);

            // Định dạng header chi tiêu
            var expenseHeaderCells = new[] { $"A{startRow - 1}:E{startRow - 1}", $"A{startRow}", $"B{startRow}", $"C{startRow}", $"D{startRow}", $"E{ startRow }" };
            foreach (var cell in expenseHeaderCells)
            {
                worksheet.Cells[cell].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
                worksheet.Cells[cell].Style.Font.Bold = true;
                worksheet.Cells[cell].Style.Fill.PatternType = ExcelFillStyle.Solid;
                worksheet.Cells[cell].Style.Fill.BackgroundColor.SetColor(Color.LightGray);
                worksheet.Cells[cell].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            }

            // Điền dữ liệu chi tiêu
            for (int i = 0; i < expenseTransactions.Count; i++)
            {
                var transaction = expenseTransactions[i];
                var row = startRow + 1 + i; // Bắt đầu từ dòng sau header
                var displayDate = transaction.UpdatedDate ?? transaction.CreatedDate;

                worksheet.Cells[$"A{row}"].Value = i + 1;
                worksheet.Cells[$"A{row}:E{row}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
                if (transaction.CategoryId != null && categoryDict.TryGetValue(transaction.CategoryId, out string name))
                {
                    worksheet.Cells[$"B{row}"].Value = name;
                }

                // CHUYỂN ĐỔI string sang số
                if (decimal.TryParse(transaction.Amount, out decimal amount))
                {
                    worksheet.Cells[$"C{row}"].Value = amount;
                }
                else
                {
                    worksheet.Cells[$"C{row}"].Value = 0;
                }

                worksheet.Cells[$"D{row}"].Value = displayDate;
                worksheet.Cells[$"E{row}"].Value = transaction.Note;    
            }

            // Tổng chi tiêu - đặt ở vị trí mới
            if (expenseTransactions.Any())
            {
                int totalRow = startRow + 1 + expenseTransactions.Count;
                worksheet.Cells[$"A{totalRow + 1}"].Value = "Tổng chi tiêu";

                
                worksheet.Cells[$"B{totalRow + 1}"].Formula = $"SUM(C{startRow + 1}:C{totalRow - 1})";
                worksheet.Cells[$"B{totalRow + 1} "].Style.Numberformat.Format = "#,##0";

                worksheet.Cells[$"A{totalRow + 1}"].Style.Font.Bold = true;
                worksheet.Cells[$"B{totalRow + 1}"].Style.Font.Bold = true;
            }
        }
        private void FormatWorksheet(ExcelWorksheet worksheet, int incomeCount, int expenseCount, int totalIncomeCount)
        {
            // Định dạng cột số tiền THU NHẬP
            if (incomeCount > 0)
            {
                worksheet.Cells[$"C3:C{2 + incomeCount}"].Style.Numberformat.Format = "#,##0";
                worksheet.Cells[$"A3:E{2 + incomeCount}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            }
            worksheet.Cells["B3"].Style.Numberformat.Format = "#,##0";

            // Định dạng cột số tiền CHI TIÊU
            if (expenseCount > 0)
            {
                int expenseDataStartRow = 3 + totalIncomeCount + 5 + 1;
                worksheet.Cells[$"C{expenseDataStartRow}:C{expenseDataStartRow + expenseCount - 1}"].Style.Numberformat.Format = "#,##0";
                worksheet.Cells[$"A{expenseDataStartRow}:E{expenseDataStartRow + expenseCount - 1}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            }

            int expenseTotalRow = 3 + totalIncomeCount + 5 + 1;
            worksheet.Cells[$"B{expenseTotalRow}"].Style.Numberformat.Format = "#,##0";

            
            // Căn giữa các cột STT
            if (incomeCount > 0)
            {
                worksheet.Cells[$"A3:A{2 + incomeCount}"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
            }

            if (expenseCount > 0)
            {
                int expenseDataStartRow = 3 + totalIncomeCount + 5 + 1;
                worksheet.Cells[$"A{expenseDataStartRow}:A{expenseDataStartRow + expenseCount - 1}"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
            }

            // ... [các định dạng căn chỉnh khác] ...

            // Tự động điều chỉnh độ rộng cột
            if (worksheet.Dimension != null)
            {
                worksheet.Cells[worksheet.Dimension.Address].AutoFitColumns();
            }
        }

        // Convert excel to pdf

        public async Task<byte[]> GeneratePdfReportByTemplateAsync(GenerateExcelReportRequest reportRequest)
        {
            // Step 1: Generate Excel as byte[]
            var excelBytes = await GenerateExcelReportByTemplateAsync(reportRequest);

            // Step 2: Convert Excel bytes to PDF using Spire.XLS
            using var workbook = new Spire.Xls.Workbook();

            // Load from memory stream
            using var excelStream = new MemoryStream(excelBytes);
            workbook.LoadFromStream(excelStream);

            // Export to PDF
            using var pdfStream = new MemoryStream();
            workbook.SaveToStream(pdfStream, Spire.Xls.FileFormat.PDF);

            return pdfStream.ToArray();
        }

    }
}