using BEMobile.Connectors;
using BEMobile.Data.Entities;
using BEMobile.Services;
using DocumentFormat.OpenXml.Wordprocessing;
using Microsoft.Build.Framework;
using Microsoft.CodeAnalysis;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Neo4j.Driver;
using Newtonsoft.Json;
using OpenCvSharp;
using System;
using System.Collections.Generic;
using System.Globalization;
using System.Net.Http;
using System.Text;
using System.Text.Json.Serialization;
using System.Threading.Tasks;
using static OpenCvSharp.ML.DTrees;

namespace BEMobile.Services
{
    public interface IKnowledgeGraphService
    {
        Task<string> ProcessUserQueryAsync(string userQuestion, string userId);
        Task<string> Rep_add_transaction(string text);

        Task<string> Rep_single_query(string text, string userId, string fixQuestion = null, bool reQuestion = false);

    }
    public class KnowledgeGraphService : IKnowledgeGraphService
    {
        private readonly INeo4jConnector _connector;
        private readonly IHttpClientFactory _httpFactory;
        private readonly GeminiOptions _opts;
        private readonly ILogger<KnowledgeGraphService> _logger;

        public KnowledgeGraphService(
            INeo4jConnector connector,
            IHttpClientFactory httpFactory,
            IOptions<GeminiOptions> opts,
            ILogger<KnowledgeGraphService> logger)
        {
            _connector = connector ?? throw new ArgumentNullException(nameof(connector));
            _httpFactory = httpFactory ?? throw new ArgumentNullException(nameof(httpFactory));
            _opts = opts?.Value ?? throw new ArgumentNullException(nameof(opts));
            _logger = logger ?? throw new ArgumentNullException(nameof(logger));
        }

        public class QueryAnalysisResult
        {
            [JsonPropertyName("classification")]
            public string Classification { get; set; }

            [JsonPropertyName("rephrasedQuery")]
            public string RephrasedQuery { get; set; }
        }

        public async Task<string> Rep_add_transaction(string text)
        {
            var categories = new[]
            {
                "1:Khác", "2: Việc tự do", "3: Điện nước", "4: Đầu tư", "5: Giáo dục", "6:Y yế",
                "7: Lương", "8: Ăn uống", "9: Mua sắm", "10: Thưởng", "11: Đi lại", "12: Giải trí",
                "13: Bán hàng"
            };
            var catsText = string.Join(", ", categories);
            var prompt = $@"
Sau đây là yêu cầu thêm một giao dịch mới trong app quản lý tài chính cá nhân: ""{text}""
Các category có thể có là: {catsText}.

Hãy trả về thông tin như sau:
Ví dụ: ""hôm nay tôi ăn trưa hết 40k""
[
  {{ ""category"": ""8"", ""amount"": 40000, ""note"": ""Ăn trưa""}}
]

Chỉ trả lại mảng JSON, không thêm bất kỳ văn bản nào khác. Nếu không thể xác định được category hoặc amount, hãy trả về mảng rỗng []
"
    ;
            string modelResponse = await CallGeminiWithTextAsync(prompt, 1);
            return modelResponse;

        }



        public async Task<string> Rep_single_query(string userQuestion, string userId, string fixQuestion = null, bool reQuestion = false)
        {
            if (string.IsNullOrEmpty(fixQuestion))
                fixQuestion = userQuestion;
            fixQuestion = $"{userId} " + fixQuestion;

            var promptForCypher = $@"
Bạn là một chuyên gia về Neo4j, có nhiệm vụ chuyển đổi câu hỏi của người dùng thành một câu lệnh Cypher **chỉ đọc (read-only)**.

**QUY TẮC:**
1. **Chỉ trả về DUY NHẤT** câu lệnh Cypher. Không thêm giải thích, markdown (```), hoặc bất kỳ văn bản nào khác.
2. Chỉ sử dụng các loại node, thuộc tính và mối quan hệ có trong schema. Không được tự ý suy diễn ra các thuộc tính hoặc mối quan hệ không tồn tại.
3. **Nghiêm cấm** tạo ra các câu lệnh có thể thay đổi dữ liệu (như CREATE, MERGE, SET, DELETE).
4. Chỉ trả về cypher truy vấn bảng (tối thiểu 5 cột như ví dụ), không tính toán tổng hợp (aggregation) hoặc các phép toán phức tạp khác.
5. Tất cả các giá trị trong đồ thị là chuỗi (string). Hãy đảm bảo rằng bạn so sánh chúng đúng cách trong câu lệnh Cypher.

**VÍ DỤ:**
Ví dụ 1: ""{{1}}Tôi đã tiêu gì trong ngày 1/6?""

""MATCH (u:`Mã người dùng` {{name: ""1""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd)
WHERE cd.name = ""2025/06/01""
OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
RETURN 
    dm_name.name AS TenDanhMuc, 
    ud.name AS NgayCapNhat, 
    amt.name AS SoTien, 
    note.name AS GhiChu, 
    type.name AS Loai
""

Ví dụ 2: ""{{1}} Chi tiêu tháng 9""

""
MATCH (u:`Mã người dùng` {{name: ""1""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd)
WHERE cd.name >= ""2025/09/01"" AND cd.name <= ""2025/09/30""
OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
RETURN 
    dm_name.name AS TenDanhMuc, 
    ud.name AS NgayCapNhat, 
    amt.name AS SoTien, 
    note.name AS GhiChu, 
    type.name AS Loai
""

**YÊU CẦU:**
---
Câu hỏi của người dùng: ""{fixQuestion}""

Cypher Query:
";

            string cypherQuery = await CallGeminiWithTextAsync(promptForCypher, 1);

            cypherQuery = cypherQuery.Replace("```cypher", "").Replace("```", "").Trim();
            cypherQuery = cypherQuery.Trim('"');
            //return cypherQuery;

            List<IRecord> records;

            string fallbackQuery = $@"
    MATCH (u:`Mã người dùng` {{name: ""{userId}""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
    MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd)
    OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
    OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
    OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
    OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
    OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
    OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
    RETURN 
        dm_name.name AS TenDanhMuc, 
        ud.name AS NgayCapNhat, 
        amt.name AS SoTien, 
        note.name AS GhiChu, 
        type.name AS Loai
    ORDER BY NgayCapNhat DESC
    LIMIT 100
";

            try
            {
                records = await _connector.ExecuteReadAsync(cypherQuery);

                //if (records == null || records.Count == 0)
                //{
                //    records = await _connector.ExecuteReadAsync(fallbackQuery);
                //}
            }
            catch (Exception ex)
            {
                records = await _connector.ExecuteReadAsync(fallbackQuery);

            }


            var databaseResultsJson = JsonConvert.SerializeObject(records.Select(r => r.Values));

            var promptForAnswer = $@"
            Bạn là một trợ lý tài chính thân thiện và bạn đang quản lý ứng dụng, người dùng đã cập nhật dữ liệu tài chính cá nhân của họ trong một cơ sở dữ liệu và bạn được cung cấp kết quả truy vấn từ cơ sở dữ liệu đó dưới dạng JSON.
            Dựa vào câu hỏi gốc của người dùng và dữ liệu JSON được cung cấp, hãy tạo ra một câu trả lời tự nhiên bằng tiếng Việt. Nếu dữ liệu quá nhiều, hãy tóm tắt.
            Câu trả lời cần chính xác nhất có thể, từ chối các yêu cầu không cung cấp như  gửi mail, tìm kiếm giúp, ...
            Chuẩn hoá định dạng. Trả lời đúng trọng tâm, nếu minh chứng dữ liệu.
        
            Câu hỏi gốc: ""{userQuestion}""
        
            Dữ liệu từ cơ sở dữ liệu (định dạng JSON):
            {databaseResultsJson}

            Câu trả lời của bạn:
        ";

            string finalAnswer = await CallGeminiWithTextAsync(promptForAnswer, 1);

            return finalAnswer;
        }

        public async Task<string> ProcessUserQueryAsync(string userQuestion, string userId)
        {
            string currentDateTime = DateTime.Now.ToString("F", new CultureInfo("vi-VN"));

            string combinedPrompt = $@"
Bạn là một trợ lý AI chuyên về tài chính cá nhân. Nhiệm vụ của bạn là phân loại truy vấn của người dùng và chuẩn hóa các truy vấn liên quan đến thời gian.

**Ngày giờ hiện tại để tham chiếu: {currentDateTime}**

**1. Phân loại truy vấn:**
Phân loại truy vấn của người dùng thành MỘT trong các loại sau:
- **OFF_TOPIC**: Các yêu cầu không hỏi về tài chính cá nhân (ví dụ: thời tiết, tin tức).
- **ADD_TRANSACTION**: Yêu cầu thêm giao dịch mới (Key: mua, bán, tiêu, dùng, chi, thêm khoản...).
- **SINGLE_QUERY**: Các truy vấn đơn giản, hỏi thông tin cá nhân hoặc một thông tin cụ thể (ví dụ: hôm nay tôi tiêu gì?).
- **MULTI_QUERY**: Yêu cầu truy vấn nhiều hơn 1 tác vụ, truy vấn phức tạp, hoặc truy vấn với lượng lớn dữ liệu (ví dụ: tổng hợp, phân tích, so sánh tháng này và tháng trước, đề xuất tiết kiệm).
- **HATE**: Các truy vấn mang nghĩa tiêu cực, phủ định câu hỏi phía trước như ""Sai rồi!"", ""Không đúng!"", ""Bậy bạ!"",...
- **TRAIN**: Các truy vấn với mong muốn giúp đỡ như ""Ứng dụng này có chức năng gì?"", ""Thêm giao dịch ở đâu?"",...

**2. Chuẩn hóa truy vấn (Nếu cần):**
- **Nếu** phân loại là `SINGLE_QUERY` hoặc `MULTI_QUERY`, Chỉ trả về dạng ""Tổng chi tiêu từ ngày YYYY/MM/DD đến ngày YYYY/MM/DD"" hoặc ""Tôi tiêu những gì vào ngày YYYY/MM/DD"".
- **Nếu** phân loại là `OFF_TOPIC`, `ADD_TRANSACTION`, `HATE` hoặc `TRAIN` giá trị `rephrasedQuery` phải là `null`.

**3. Định dạng trả về:**
Chỉ trả về một đối tượng JSON duy nhất, không giải thích gì thêm. 

**Ví dụ (với ngày hiện tại là {currentDateTime}):**

- **Câu hỏi:** ""Tôi đã tiêu gì trong tháng 6?""
  **JSON:** {{ ""classification"": ""MULTI_QUERY"", ""rephrasedQuery"": ""Tôi đã tiêu gì từ ngày 2025/06/01 đến 2025/06/30?"" }}

- **Câu hỏi:** ""Tôi đã chi tiêu những gì trong tuần trước?""
  **JSON:** {{ ""classification"": ""MULTI_QUERY"", ""rephrasedQuery"": ""Tôi đã chi tiêu những gì từ ngày 2025/10/13 đến 2025/10/19?"" }} (Giả sử tuần là Thứ 2 - Chủ Nhật)

- **Câu hỏi:** ""Hôm nay tôi tiêu những gì?""
  **JSON:** {{ ""classification"": ""SINGLE_QUERY"", ""rephrasedQuery"": ""Tôi tiêu những gì vào ngày 2025/10/25?"" }}

- **Câu hỏi:** ""Tổng chi tháng này là bao nhiêu?""
  **JSON:** {{ ""classification"": ""MULTI_QUERY"", ""rephrasedQuery"": ""Tổng chi tiêu từ ngày 2025/10/01 đến 2025/10/25?"" }}

- **Câu hỏi:** ""Tổng chi tiêu cho ăn uống tháng này và có mấy lần ăn đồ ăn nhanh"".
    **JSON:** {{ ""classification"": ""MULTI_QUERY"", ""rephrasedQuery"": ""Tổng chi tiêu từ ngày 2025/10/01 đến 2025/10/25"" }}
---
**Truy vấn của người dùng:** ""{userQuestion}""
**JSON trả về:**
";

            string jsonResponse;
            try
            {
                jsonResponse = await CallGeminiWithTextAsync(combinedPrompt, 0);
            }
            catch (Exception ex)
            {
                return null;
            }

            QueryAnalysisResult analysisResult;
            try
            {
                string responseData = jsonResponse.Trim();
                int jsonStart = responseData.IndexOf('{');
                int jsonEnd = responseData.LastIndexOf('}');

                if (jsonStart != -1 && jsonEnd != -1 && jsonEnd > jsonStart)
                {
                    string cleanJson = responseData.Substring(jsonStart, jsonEnd - jsonStart + 1);
                    analysisResult = JsonConvert.DeserializeObject<QueryAnalysisResult>(cleanJson);
                }
                else
                {
                    throw new JsonException($"Không tìm thấy đối tượng JSON hợp lệ trong phản hồi: {jsonResponse}");
                }
            }
            catch (Exception ex)
            {
                return null;
            }

            string train = @"
📱 Hướng dẫn sử dụng ứng dụng Quản lý Chi tiêu Cá nhân

Chào mừng bạn đến với MyFinance — trợ lý tài chính thông minh giúp bạn quản lý chi tiêu dễ dàng hơn mỗi ngày!

💰 Theo dõi chi tiêu
- Ghi lại các khoản chi tiêu hằng ngày.
- Xem biểu đồ thống kê để biết bạn đang chi tiêu nhiều nhất ở đâu.

🎯 Lập ngân sách
- Tạo ngân sách cho từng danh mục.
- Nhận cảnh báo khi sắp vượt giới hạn chi tiêu.

🤖 Trợ lý Chatbot
- Trò chuyện với chatbot để xem báo cáo nhanh hoặc hỏi về thói quen chi tiêu.

📊 Báo cáo & thống kê
- Xem tổng quan thu – chi theo tuần, tháng, hoặc năm.

☁️ Đồng bộ & bảo mật
- Dữ liệu được lưu an toàn và đồng bộ trên nhiều thiết bị.
";


            string classification = analysisResult?.Classification;
            switch (classification)
            {
                case "SINGLE_QUERY":
                case "MULTI_QUERY":
                    return await Rep_single_query(userQuestion, userId, analysisResult.RephrasedQuery);

                case "ADD_TRANSACTION":
                    return await Rep_add_transaction(userQuestion);

                case "OFF_TOPIC":
                    return "Xin lỗi, tôi chỉ có thể giúp bạn về các vấn đề tài chính cá nhân.";
                case "HATE":
                    return await CallGeminiWithTextAsync("Bạn là chatbot của ứng dụng quản lý chi tiêu và bạn vừa trả lời khiến người dùng không vừa ý. Hãy viết một câu xin lỗi ngắn để xoa dịu người dùng ứng dụng. Chỉ gồm câu trả lời và mong muốn họ làm rõ câu hỏi.", 1);
                case "TRAIN":   
                    return await CallGeminiWithTextAsync("Bạn là chatbot của ứng dụng quản lý chi tiêu hãy trả lời câu hỏi dựa trên tài liệu: " + train + ". Câu hỏi: " + userQuestion + ". Trả lời ngắn gọn.", 1);
                default:
                    return null;
            }
        }

        private async Task<string> CallGeminiWithTextAsync(string prompt, int op)
        {
            string api = _opts.GetRandomApiKey();

            var client = _httpFactory.CreateClient("gemini");
            var url = $"{_opts.BaseUrl}/{_opts.Model[op]}:generateContent?key={api}";

            var body = new
            {
                contents = new[]
                {
                    new { parts = new object[] { new { text = prompt } } }
                }
            };

            var jsonContent = JsonConvert.SerializeObject(body);
            var request = new HttpRequestMessage(HttpMethod.Post, url)
            {
                Content = new StringContent(jsonContent, Encoding.UTF8, "application/json")
            };

            _logger.LogInformation("Sending text extraction request to Gemini API...");
            var response = await client.SendAsync(request);
            var responseText = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogError("Gemini API returned error {StatusCode}: {Response}", response.StatusCode, responseText);
                throw new HttpRequestException($"Gemini API error: {response.StatusCode} - {responseText}");
            }

            var responseObj = JsonConvert.DeserializeObject<GeminiResponse>(responseText);
            return responseObj?.Candidates?[0]?.Content?.Parts?[0]?.Text ?? string.Empty;
        }

        private class GeminiResponse { [JsonProperty("candidates")] public List<Candidate>? Candidates { get; set; } }
        private class Candidate { [JsonProperty("content")] public Content? Content { get; set; } }
        private class Content { [JsonProperty("parts")] public List<Part>? Parts { get; set; } }
        private class Part { [JsonProperty("text")] public string? Text { get; set; } }
    }
}
