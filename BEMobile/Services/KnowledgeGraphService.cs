
using BEMobile.Connectors;
using BEMobile.Data.Entities;
using BEMobile.Services;
using DocumentFormat.OpenXml.Wordprocessing;
using Microsoft.CodeAnalysis;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Neo4j.Driver;
using Newtonsoft.Json;
using OpenCvSharp;
using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using static OpenCvSharp.ML.DTrees;

namespace BEMobile.Services
{
    public interface IKnowledgeGraphService
    {
        //Task CreateUserNodeAsync(UserDto user);
        //Task CreateTransactionAsync(TransactionDto transaction);
        // Hàm nhận một câu văn và trả về cấu trúc đồ thị được trích xuất
        Task<string> Classify_prompt(string text);
        Task<string> Rep_add_transaction(string text);

        Task<string> Rep_single_query(string text, string userId, string fixQuestion = null);
        Task<string> Rep_multi_query(string userQuestion, string userId);
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
            string modelResponse = await CallGeminiWithTextAsync(prompt);
            return modelResponse;

        }



    public async Task<string> Rep_single_query(string userQuestion, string userId, string fixQuestion = null)
        {
            if (string.IsNullOrEmpty(fixQuestion))
                fixQuestion = userQuestion;
            fixQuestion = $"{userId} " + fixQuestion;

            string schema = """
    "Text1:\n"
    "\"UserId: 1 * Name: Mai Đức Văn * Email: abc123@gmail.com * Facebook: vandeptrai * "
    "PhoneNumber: 0123456789 * CreatedDate: 23/05/2025 * TransactionID: 1, 3, 4, 5, 6\"\n\n"
    "Output:\n"
    "Entities:\n"
    "- 1: Mã người dùng\n"
    "- Mai Đức Văn: Tên người dùng\n"
    "- abc123@gmail.com: Email\n"
    "- vandeptrai: Tài khoản Facebook\n"
    "- 0123456789: Số điện thoại\n"
    "- 23/05/2025: Ngày tạo tài khoản\n"
    "- GD1: Mã giao dịch\n"
    "- GD3: Mã giao dịch\n"
    "- GD4: Mã giao dịch\n"
    "- GD5: Mã giao dịch\n"
    "- GD6: Mã giao dịch\n\n"
    "Relationships:\n"
    "- (1, CÓ_EMAIL, abc123@gmail.com)\n"
    "- (1, FACEBOOK, vandeptrai)\n"
    "- (1, PHONE, 0123456789)\n"
    "- (1, TẠO_TÀI_KHOẢN, 23/05/2025)\n"
    "- (1, CÓ_GIAO_DỊCH, GD1)\n"
    "- (1, CÓ_GIAO_DỊCH, GD3)\n"
    "- (1, CÓ_GIAO_DỊCH, GD4)\n"
    "- (1, CÓ_GIAO_DỊCH, GD5)\n"
    "- (1, CÓ_GIAO_DỊCH, GD6)\n"

    f"Text2:\n"
    f"\"CategoryID: 1 * CategoryName: Ăn uống\"\n\n"

    f"Output:\n"
    f"Entities:\n"
    f"- DM1: Mã danh mục\n"
    f"- Ăn uống: Tên danh mục\n\n"

    f"Relationships:\n"
    f"- (DM1, CÓ_TÊN, Ăn uống)\n\n"

    f"Text3:\n"
    f"\"TransactionId: 3 * Type: expense * Amount: 20000 * Note: Kem đánh răng * "
    f"CreatedDate: 28/05/2025 * UpdatedDate: 28/05/2025 * CategoryID: 3\"\n\n"

    f"Output:\n"
    f"Entities:\n"
    f"- GD3: Mã giao dịch\n"
    f"- expense: Loại giao dịch\n"
    f"- 20000: Số tiền\n"
    f"- Kem đánh răng: Ghi chú\n"
    f"- 28/05/2025: Ngày tạo\n"
    f"- 28/05/2025: Ngày cập nhật\n"
    f"- DM3: Mã danh mục\n\n"

    f"Relationships:\n"
    f"- (GD3, LÀ_LOẠI, expense)\n"
    f"- (GD3, CÓ_SỐ_TIỀN, 20000)\n"
    f"- (GD3, GHI_CHÚ, Kem đánh răng)\n"
    f"- (GD3, CÓ_NGÀY_TẠO, 28/05/2025)\n"
    f"- (GD3, CÓ_NGÀY_CẬP_NHẬT, 28/05/2025)\n"
    f"- (GD3, CÓ_DANH_MỤC, DM3)\n"
    """;

            var promptForCypher = $@"
Bạn là một chuyên gia về Neo4j, có nhiệm vụ chuyển đổi câu hỏi của người dùng thành một câu lệnh Cypher **chỉ đọc (read-only)**.

**QUY TẮC:**
1. **Chỉ trả về DUY NHẤT** câu lệnh Cypher. Không thêm giải thích, markdown (```), hoặc bất kỳ văn bản nào khác.
2. Chỉ sử dụng các loại node, thuộc tính và mối quan hệ có trong schema. Không được tự ý suy diễn ra các thuộc tính hoặc mối quan hệ không tồn tại.
3. **Nghiêm cấm** tạo ra các câu lệnh có thể thay đổi dữ liệu (như CREATE, MERGE, SET, DELETE).
4. Nếu câu hỏi không thể trả lời được bằng schema đã cho, hãy trả về chuỗi `UNANSWERABLE`.
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

            string cypherQuery = await CallGeminiWithTextAsync(promptForCypher);

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
";

            try
            {
                records = await _connector.ExecuteReadAsync(cypherQuery);

                if (records == null || records.Count == 0)
                {
                    records = await _connector.ExecuteReadAsync(fallbackQuery);
                }
            }
            catch (Exception ex)
            {
                records = await _connector.ExecuteReadAsync(fallbackQuery);
    
            }


            var databaseResultsJson = JsonConvert.SerializeObject(records.Select(r => r.Values));

            var promptForAnswer = $@"
            Bạn là một trợ lý tài chính thân thiện và bạn đang quản lý ứng dụng, người dùng đã cập nhật dữ liệu tài chính cá nhân của họ trong một cơ sở dữ liệu và bạn được cung cấp kết quả truy vấn từ cơ sở dữ liệu đó dưới dạng JSON.
            Dựa vào câu hỏi gốc của người dùng và dữ liệu JSON được cung cấp, hãy tạo ra một câu trả lời tự nhiên bằng tiếng Việt. Nội dung trả lời chi tiết nhất có thể. Nếu dữ liệu quá nhiều, hãy tóm tắt.
            Câu trả lời cần chính xác nhất có thể, từ chối các yêu cầu không cung cấp như  gửi mail, tìm kiếm giúp, ...
            Chuẩn hoá định dạng.
        
            Câu hỏi gốc: ""{userQuestion}""
        
            Dữ liệu từ cơ sở dữ liệu (định dạng JSON):
            {databaseResultsJson}

            Câu trả lời của bạn:
        ";

            string finalAnswer = await CallGeminiWithTextAsync(promptForAnswer);

            return finalAnswer;
        }

    public async Task<string> Rep_multi_query(string userQuestion, string userId)
        {
            var promptForAnswer = $@"
            Nhận câu hỏi và trả về nội dùng liên quan đến dữ liệu cần cung cấp để phục vụ cho chuyển đổi câu hỏi thành lệnh cypher. Chỉ trả về nội dung đã được chỉnh sửa, không giải thích gì thêm.    

Ví dụ1: ""Tôi đã tiêu gì trong tháng 6?"" => ""Tôi đã tiêu gì từ ngày 2025/06/01 đến 2025/06/01? ""
Ví dụ2: ""Tôi đã chi tiêu những gì trong tuần trước?"" => ""Tôi đã chi tiêu những gì từ ngày 2025/10/25 đến 2025/10/22? (Tuỳ vào ngày hiện tại)""
Ví dụ3: "" Hôm nay tôi tiêu những gì?"" => ""Hôm nay tôi tiêu những gì vào ngày 2025/10/22? (Tuỳ vào ngày hiện tại)""
Ví dụ 4: ""Hãy giúp tôi tổng hợp số tiền đã tiêu trong tháng và đề xuất cách tiết kiệm cho tháng sau."" => ""Chi tiêu trong tháng hiện tại của tôi?""
Ví dụ 5: ""Cho tôi biết khoản nào lớn nhất hôm nay và tổng chi tháng này là bao nhiêu."" => ""Tổng chi tiêu từ ngày 2025/10/01 đến 2025/10/22? (Tuỳ vào ngày hiện tại)""
Ví dụ 6: ""Tôi chi tiêu / mua gì hôm nay"" => ""Tôi chi tiêu gì vào ngày 2025/10/22? (Tuỳ vào ngày hiện tại)""



            Câu hỏi gốc: ""{userQuestion}""

            Câu trả lời của bạn:
        ";

            string fixQuestion = await CallGeminiWithTextAsync(promptForAnswer);
            return Rep_single_query(userQuestion, userId, fixQuestion).Result;
        }

        public async Task<string> Classify_prompt(string text)
        {
            var prompt = $@"
Hãy giúp tôi phân loại prompt sau thành các loại:
OFF_TOPIC:Các yêu cầu không hỏi về tài chính cá nhân.
ADD_TRANSACTION: Nếu prompt yêu cầu thêm giao dịch mới (Key: mua, bán, tiêu, dùng, ...).
SINGLE_QUERY: Các truy vấn đơn giản, thông tin cá nhân người dùng.
MULTI_QUERY: Nếu prompt yêu cầu truy vấn nhiều hơn 1 tác vụ về tài chính cá nhân hoặc truy vấn với lượng lớn.

Trả về kết quả dưới dạng OFF_TOPIC, ADD_TRANSACTION, SINGLE_QUERY hoặc MULTI_QUERY.

Phân loại prompt sau: ""{text}""
";
            try
            {
                string modelResponse = await CallGeminiWithTextAsync(prompt);
                return modelResponse;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to extract graph from text using Gemini API.");
                return $"Lỗi khi gọi Gemini API: {ex.Message}";
            }
        }

        // Gọi gemini
        private async Task<string> CallGeminiWithTextAsync(string prompt)
        {
            string api = _opts.GetRandomApiKey();

            var client = _httpFactory.CreateClient("gemini");
            var url = $"{_opts.BaseUrl}/{_opts.Model}:generateContent?key={api}";

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
