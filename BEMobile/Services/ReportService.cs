using BEMobile.Data.Entities;
using Google;
using Microsoft.EntityFrameworkCore;
using OfficeOpenXml;
using OfficeOpenXml.Style;
using System.Drawing;
using System.Globalization;

namespace BEMobile.Services
{
    public interface IReportService
    {
        Task<byte[]> GenerateExcelReportByTemplateAsync(string userId, DateTime startDate, DateTime endDate);
    }

    public class ReportService : IReportService
    {
        private readonly AppDbContext _context;


        public ReportService(AppDbContext context)
        {
            _context = context;
            ExcelPackage.LicenseContext = LicenseContext.NonCommercial;
        }
        public async Task<byte[]> GenerateExcelReportByTemplateAsync(string userId, DateTime startDate, DateTime endDate)
        {
            //Lấy toàn bộ transactions của user
                var allTransactions = await _context.Transactions
                    .Where(t => t.UserId == userId)
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
                           createdDate.Value.Date >= startDate.Date &&
                           createdDate.Value.Date <= endDate.Date;
                })
                .OrderBy(t =>
                {
                    var updatedDate = ParseDate(t.UpdatedDate);
                    var createdDate = ParseDate(t.CreatedDate);
                    return updatedDate ?? createdDate ?? DateTime.MinValue;
                })
                .ToList();

            // Tách income và expense
            var incomeTransactions = transactions.Where(t => t.Type == "INCOME").ToList();
            var expenseTransactions = transactions.Where(t => t.Type == "EXPENSE").ToList();

            using var package = new ExcelPackage();
            var worksheet = package.Workbook.Worksheets.Add("Báo cáo");

            // Tạo header theo template
            CreateTemplateHeader(worksheet);

            // Điền dữ liệu income
            FillIncomeData(worksheet, incomeTransactions);

            // Điền dữ liệu expense - TRUYỀN THÊM incomeCount để xác định vị trí bắt đầu
            FillExpenseData(worksheet, expenseTransactions, incomeTransactions.Count);

            // Tính tổng và công thức - TRUYỀN THÊM incomeCount
            //CalculateTotals(worksheet, incomeTransactions.Count, expenseTransactions.Count, incomeTransactions.Count);

            // Định dạng - TRUYỀN THÊM incomeCount
            FormatWorksheet(worksheet, incomeTransactions.Count, expenseTransactions.Count, incomeTransactions.Count);

            return package.GetAsByteArray();
        }
        private void CreateTemplateHeader(ExcelWorksheet worksheet)
        {
            // Header THU NHẬP (giữ nguyên)
            worksheet.Cells["A1"].Value = "THU NHẬP";
            worksheet.Cells["A1"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
            worksheet.Cells["A1:D1"].Merge = true;

            worksheet.Cells["A2"].Value = "STT";
            worksheet.Cells["B2"].Value = "Tên thu nhập";
            worksheet.Cells["C2"].Value = "Giá tiền";
            worksheet.Cells["D2"].Value = "Thời gian";

            // Định dạng header thu nhập
            var headerCells = new[] { "A1:D1", "A2", "B2", "C2", "D2" };
            foreach (var cell in headerCells)
            {
                worksheet.Cells[cell].Style.Font.Bold = true;
                worksheet.Cells[cell].Style.Fill.PatternType = ExcelFillStyle.Solid;
                worksheet.Cells[cell].Style.Fill.BackgroundColor.SetColor(Color.LightGray);
                worksheet.Cells[cell].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            }

            // KHÔNG tạo header chi tiêu ở đây nữa, vì đã tạo trong FillExpenseData
        }
        private void FillIncomeData(ExcelWorksheet worksheet, List<Transaction> incomeTransactions)
        {
            int startRow = 3;

            for (int i = 0; i < incomeTransactions.Count; i++)
            {
                var transaction = incomeTransactions[i];
                var row = startRow + i;
                var displayDate = transaction.UpdatedDate ?? transaction.CreatedDate;

                worksheet.Cells[$"A{row}"].Value = i + 1; // STT
                worksheet.Cells[$"B{row}"].Value = transaction.Note; // Tên thu nhập
                if (decimal.TryParse(transaction.Amount, out decimal amount))
                {
                    worksheet.Cells[$"C{row}"].Value = amount;
                }
                worksheet.Cells[$"D{row}"].Value = displayDate; // Thời gian
            }

            // Tổng thu nhập
            if (incomeTransactions.Any())
            {
                int totalRow = startRow + incomeTransactions.Count;
                worksheet.Cells[$"F{startRow}"].Value = "Tổng thu nhập";
                worksheet.Cells[$"G{startRow}"].Formula = $"SUM(C{startRow}:C{totalRow - 1})";
                worksheet.Cells[$"F{startRow}"].Style.Font.Bold = true;
                worksheet.Cells[$"G{startRow}"].Style.Font.Bold = true;
            }
        }
        private void FillExpenseData(ExcelWorksheet worksheet, List<Transaction> expenseTransactions, int incomeCount)
        {
            // Bắt đầu từ dòng cuối cùng của bảng thu nhập + 3 dòng (2 dòng trống + 1 dòng header)
            int startRow = 3 + incomeCount + 3;

            // Header CHI TIÊU - đặt ở vị trí mới
            worksheet.Cells[$"A{startRow - 1}"].Value = "CHI TIÊU";
            worksheet.Cells[$"A{startRow - 1}:D{startRow - 1}"].Merge = true;
            worksheet.Cells[$"A{startRow-1}"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;

            worksheet.Cells[$"A{startRow}"].Value = "STT";
            worksheet.Cells[$"B{startRow}"].Value = "Tên giao dịch";
            worksheet.Cells[$"C{startRow}"].Value = "Giá tiền";
            worksheet.Cells[$"D{startRow}"].Value = "Thời gian";

            // Định dạng header chi tiêu
            var expenseHeaderCells = new[] { $"A{startRow - 1}:D{startRow-1}", $"A{startRow}", $"B{startRow}", $"C{startRow}", $"D{startRow}" };
            foreach (var cell in expenseHeaderCells)
            {
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
                worksheet.Cells[$"B{row}"].Value = transaction.Note;

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
            }

            // Tổng chi tiêu - đặt ở vị trí mới
            if (expenseTransactions.Any())
            {
                int totalRow = startRow + 1 + expenseTransactions.Count;
                worksheet.Cells[$"F{startRow + 1}"].Value = "Tổng chi tiêu";

                // Tính tổng trực tiếp
                decimal totalExpense = 0;
                foreach (var transaction in expenseTransactions)
                {
                    if (decimal.TryParse(transaction.Amount, out decimal amount))
                    {
                        totalExpense += amount;
                    }
                }
                worksheet.Cells[$"G{startRow + 1}"].Value = totalExpense;

                worksheet.Cells[$"F{startRow + 1}"].Style.Font.Bold = true;
                worksheet.Cells[$"G{startRow + 1}"].Style.Font.Bold = true;
            }
        }
        private void FormatWorksheet(ExcelWorksheet worksheet, int incomeCount, int expenseCount, int totalIncomeCount)
        {
            // Định dạng cột số tiền THU NHẬP
            if (incomeCount > 0)
            {
                worksheet.Cells[$"C3:C{2 + incomeCount}"].Style.Numberformat.Format = "#,##0";
            }
            worksheet.Cells["G3"].Style.Numberformat.Format = "#,##0";

            // Định dạng cột số tiền CHI TIÊU
            if (expenseCount > 0)
            {
                int expenseDataStartRow = 3 + totalIncomeCount + 3 + 1;
                worksheet.Cells[$"C{expenseDataStartRow}:C{expenseDataStartRow + expenseCount - 1}"].Style.Numberformat.Format = "#,##0";
            }

            int expenseTotalRow = 3 + totalIncomeCount + 3 + 1;
            worksheet.Cells[$"G{expenseTotalRow}"].Style.Numberformat.Format = "#,##0";

            // Định dạng border cho dữ liệu THU NHẬP
            if (incomeCount > 0)
            {
                worksheet.Cells[$"A3:D{2 + incomeCount}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
                // ... [các định dạng border khác cho thu nhập] ...
            }

            // Định dạng border cho dữ liệu CHI TIÊU
            if (expenseCount > 0)
            {
                int expenseDataStartRow = 3 + totalIncomeCount + 3 + 1;
                int expenseDataEndRow = expenseDataStartRow + expenseCount - 1;

                // Border cho dữ liệu
                worksheet.Cells[$"A{expenseDataStartRow}:D{expenseDataEndRow}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
                // ... [các định dạng border khác cho chi tiêu] ...

                // Border cho header chi tiêu
                worksheet.Cells[$"A{expenseDataStartRow - 1}:D{expenseDataStartRow - 1}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
                worksheet.Cells[$"A{expenseDataStartRow}:D{expenseDataStartRow}"].Style.Border.BorderAround(ExcelBorderStyle.Thin);
            }

            // Căn giữa các cột STT
            if (incomeCount > 0)
            {
                worksheet.Cells[$"A3:A{2 + incomeCount}"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
            }

            if (expenseCount > 0)
            {
                int expenseDataStartRow = 3 + totalIncomeCount + 3 + 1;
                worksheet.Cells[$"A{expenseDataStartRow}:A{expenseDataStartRow + expenseCount - 1}"].Style.HorizontalAlignment = ExcelHorizontalAlignment.Center;
            }

            // ... [các định dạng căn chỉnh khác] ...

            // Tự động điều chỉnh độ rộng cột
            if (worksheet.Dimension != null)
            {
                worksheet.Cells[worksheet.Dimension.Address].AutoFitColumns();
            }
        }
    }
}