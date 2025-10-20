
using BEMobile.Connectors;
using BEMobile.Data.Entities;
using BEMobile.Services;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Neo4j.Driver;
using Newtonsoft.Json;
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

        Task<string> Rep_single_query(string text, string userId);
        Task<string> Rep_multi_query(string text);
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
                "Ăn uống","Đi lại","Mua sắm","Giải trí",
                "Giáo dục","Ý tế","Nhà ở","Điện nước","Khác"
            };
            var catsText = string.Join(", ", categories);
            var prompt = $@"
Sau đây là yêu cầu thêm một giao dịch mới trong app quản lý tài chính cá nhân: ""{text}""
Các category có thể có là: {catsText}.

Hãy trả về thông tin như sau:
Ví dụ: ""hôm nay tôi ăn trưa hết 40k""
[
  {{ ""category"": ""Ăn uống"", ""amount"": 40000, ""note"": ""Ăn trưa""}}
]

Chỉ trả lại mảng JSON, không thêm bất kỳ văn bản nào khác. Nếu không thể xác định được category hoặc amount, hãy trả về mảng rỗng []
"
    ;
            string modelResponse = await CallGeminiWithTextAsync(prompt);
            return modelResponse;

        }



    public async Task<string> Rep_single_query(string userQuestion, string userId)
        {
            userQuestion = $"{userId} " + userQuestion;

            string schema = """
    (Node labels)
    - `Mã người dùng`
      - name: string (mã định danh người dùng)
      - email: string?
      - facebook: string?
      - phone: string?
      - createdDate: string?

    - `Tên người dùng`
      - name: string

    - `Mã giao dịch`
      - name: string (mã định danh giao dịch)

    - `Loại giao dịch`
      - name: string (income | expense | transfer, v.v.)

    - `Số tiền`
      - name: string (số tiền dạng chuỗi hoặc số)

    - `Ghi chú`
      - name: string (mô tả chi tiết)

    - `Ngày tạo`
      - name: string (định dạng dd/MM/yyyy)

    - `Ngày cập nhật`
      - name: string (định dạng dd/MM/yyyy)

    - `Mã danh mục`
      - name: string (mã định danh danh mục)

    - `Tên danh mục`
      - name: string (ví dụ: Ăn uống, Di chuyển, Mua sắm, v.v.)

    ---

    (Relationship types)
    - (`Mã người dùng`)-[:CÓ_EMAIL]->(`Email`)
    - (`Mã người dùng`)-[:FACEBOOK]->(`Tài khoản Facebook`)
    - (`Mã người dùng`)-[:PHONE]->(`Số điện thoại`)
    - (`Mã người dùng`)-[:TẠO_TÀI_KHOẢN]->(`Ngày tạo`)
    - (`Mã người dùng`)-[:CÓ_GIAO_DỊCH]->(`Mã giao dịch`)
    - (`Mã giao dịch`)-[:LÀ_LOẠI]->(`Loại giao dịch`)
    - (`Mã giao dịch`)-[:CÓ_SỐ_TIỀN]->(`Số tiền`)
    - (`Mã giao dịch`)-[:GHI_CHÚ]->(`Ghi chú`)
    - (`Mã giao dịch`)-[:CÓ_NGÀY_TẠO]->(`Ngày tạo`)
    - (`Mã giao dịch`)-[:CÓ_NGÀY_CẬP_NHẬT]->(`Ngày cập nhật`)
    - (`Mã giao dịch`)-[:CÓ_DANH_MỤC]->(`Mã danh mục`)
    - (`Mã danh mục`)-[:CÓ_TÊN]->(`Tên danh mục`)
    """;

            var promptForCypher = $@"
Bạn là một chuyên gia về Neo4j, có nhiệm vụ chuyển đổi câu hỏi của người dùng thành một câu lệnh Cypher **chỉ đọc (read-only)**.

Hãy tuân thủ nghiêm ngặt theo cấu trúc đồ thị (schema) được cung cấp dưới đây:
---
{schema}
---

**QUY TẮC:**
1. **Chỉ trả về DUY NHẤT** câu lệnh Cypher. Không thêm giải thích, markdown (```), hoặc bất kỳ văn bản nào khác.
2. Chỉ sử dụng các loại node, thuộc tính và mối quan hệ có trong schema. Không được tự ý suy diễn ra các thuộc tính hoặc mối quan hệ không tồn tại.
3. **Nghiêm cấm** tạo ra các câu lệnh có thể thay đổi dữ liệu (như CREATE, MERGE, SET, DELETE).
4. Nếu câu hỏi không thể trả lời được bằng schema đã cho, hãy trả về chuỗi `UNANSWERABLE`.

**VÍ DỤ:**
Ví dụ 1: ""{{1}}Tôi đã tiêu gì trong ngày 1/6?""

""MATCH (u:`Mã người dùng` {{name: ""1""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd {{name: ""01/06/2025""}}) 
OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
WITH 
  u, t,
  head(collect(DISTINCT type.name)) AS Loai,
  head(collect(DISTINCT amt.name)) AS SoTien,
  head(collect(DISTINCT note.name)) AS GhiChu,
  head(collect(DISTINCT cd.name)) AS NgayTao,
  head(collect(DISTINCT ud.name)) AS NgayCapNhat,
  head(collect(DISTINCT dm.name)) AS MaDanhMuc,
  head(collect(DISTINCT dm_name.name)) AS TenDanhMuc
RETURN {{
  MaNguoiDung: u.name,
  MaGiaoDich: t.name,
  Loai: Loai,
  SoTien: SoTien,
  GhiChu: GhiChu,
  NgayTao: NgayTao,
  NgayCapNhat: NgayCapNhat,
  MaDanhMuc: MaDanhMuc,
  TenDanhMuc: TenDanhMuc
}} AS GiaoDich
ORDER BY NgayTao ASC;
""

Ví dụ 2: ""{{1}} Tôi đã tiêu gì trong hôm nay?""

""MATCH (u:`Mã người dùng` {{name: ""1""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd {{name: ""20/10/2025""}}) 
OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
WITH 
  u, t,
  head(collect(DISTINCT type.name)) AS Loai,
  head(collect(DISTINCT amt.name)) AS SoTien,
  head(collect(DISTINCT note.name)) AS GhiChu,
  head(collect(DISTINCT cd.name)) AS NgayTao,
  head(collect(DISTINCT ud.name)) AS NgayCapNhat,
  head(collect(DISTINCT dm.name)) AS MaDanhMuc,
  head(collect(DISTINCT dm_name.name)) AS TenDanhMuc
RETURN {{
  MaNguoiDung: u.name,
  MaGiaoDich: t.name,
  Loai: Loai,
  SoTien: SoTien,
  GhiChu: GhiChu,
  NgayTao: NgayTao,
  NgayCapNhat: NgayCapNhat,
  MaDanhMuc: MaDanhMuc,
  TenDanhMuc: TenDanhMuc
}} AS GiaoDich
ORDER BY NgayTao ASC;
""

Ví dụ 3: ""{{1}} Chi tiêu 3 ngày gần đây""

""// Lấy ngày hôm nay và 2 ngày trước
WITH date() AS today
WITH [today, today - duration('P1D'), today - duration('P2D')] AS recentDays
WITH [x IN recentDays | toString(x.day) + ""/"" + toString(x.month) + ""/"" + toString(x.year)] AS last3Days

MATCH (u:`Mã người dùng` {{name: ""1""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd)
WHERE cd.name IN last3Days
OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
WITH 
  u, t,
  head(collect(DISTINCT type.name)) AS Loai,
  head(collect(DISTINCT amt.name)) AS SoTien,
  head(collect(DISTINCT note.name)) AS GhiChu,
  head(collect(DISTINCT cd.name)) AS NgayTao,
  head(collect(DISTINCT ud.name)) AS NgayCapNhat,
  head(collect(DISTINCT dm.name)) AS MaDanhMuc,
  head(collect(DISTINCT dm_name.name)) AS TenDanhMuc
RETURN {{
  MaNguoiDung: u.name,
  MaGiaoDich: t.name,
  Loai: Loai,
  SoTien: SoTien,
  GhiChu: GhiChu,
  NgayTao: NgayTao,
  NgayCapNhat: NgayCapNhat,
  MaDanhMuc: MaDanhMuc,
  TenDanhMuc: TenDanhMuc
}} AS GiaoDich
ORDER BY NgayTao ASC;
""

Ví dụ 4: ""{{1}} Chi tiêu trong 4 ngày qua""

""
// Tạo danh sách 5 ngày gần nhất (hôm nay và 4 ngày trước)
WITH date() AS today
WITH [today, today - duration('P1D'), today - duration('P2D'), today - duration('P3D'), today - duration('P4D')] AS recentDays
WITH [x IN recentDays | 
  (CASE 
    WHEN x.day < 10 THEN ""0"" + toString(x.day) 
    ELSE toString(x.day) 
  END) + ""/"" + 
  (CASE 
    WHEN x.month < 10 THEN ""0"" + toString(x.month) 
    ELSE toString(x.month) 
  END) + ""/"" + 
  toString(x.year)
] AS last5Days

MATCH (u:`Mã người dùng` {{name: ""1""}})-[:CÓ_GIAO_DỊCH]->(t:`Mã giao dịch`)
MATCH (t)-[:CÓ_NGÀY_TẠO]->(cd)
WHERE cd.name IN last5Days
OPTIONAL MATCH (t)-[:LÀ_LOẠI]->(type)
OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amt)
OPTIONAL MATCH (t)-[:GHI_CHÚ]->(note)
OPTIONAL MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(ud)
OPTIONAL MATCH (t)-[:CÓ_DANH_MỤC]->(dm:`Mã danh mục`)
OPTIONAL MATCH (dm)-[:CÓ_TÊN]->(dm_name)
WITH 
  u, t,
  head(collect(DISTINCT type.name)) AS Loai,
  head(collect(DISTINCT amt.name)) AS SoTien,
  head(collect(DISTINCT note.name)) AS GhiChu,
  head(collect(DISTINCT cd.name)) AS NgayTao,
  head(collect(DISTINCT ud.name)) AS NgayCapNhat,
  head(collect(DISTINCT dm.name)) AS MaDanhMuc,
  head(collect(DISTINCT dm_name.name)) AS TenDanhMuc
RETURN {{
  MaNguoiDung: u.name,
  MaGiaoDich: t.name,
  Loai: Loai,
  SoTien: SoTien,
  GhiChu: GhiChu,
  NgayTao: NgayTao,
  NgayCapNhat: NgayCapNhat,
  MaDanhMuc: MaDanhMuc,
  TenDanhMuc: TenDanhMuc
}} AS GiaoDich
ORDER BY NgayTao ASC;
""

**YÊU CẦU:**
---
Câu hỏi của người dùng: ""{userQuestion}""

Cypher Query:
";

            string cypherQuery = await CallGeminiWithTextAsync(promptForCypher);

            cypherQuery = cypherQuery.Replace("```cypher", "").Replace("```", "").Trim();
            cypherQuery = cypherQuery.Trim('"');
            //return cypherQuery;

            if (string.IsNullOrWhiteSpace(cypherQuery))
            {
                return "Xin lỗi, tôi không thể hiểu yêu cầu của bạn để truy vấn dữ liệu.";
            }

            List<IRecord> records;
            records = await _connector.ExecuteReadAsync(cypherQuery);
 
            if (records == null || records.Count == 0)
            {
                return "Tôi không tìm thấy thông tin nào phù hợp với yêu cầu của bạn.";
            }



            var databaseResultsJson = JsonConvert.SerializeObject(records.Select(r => r.Values));

            var promptForAnswer = $@"
            Bạn là một trợ lý tài chính thân thiện. Dựa vào câu hỏi gốc của người dùng và dữ liệu JSON được cung cấp, hãy tạo ra một câu trả lời tự nhiên bằng tiếng Việt. Nội dung trả lời chi tiết nhất có thể. Nếu dữ liệu quá nhiều, hãy tóm tắt
        
            Câu hỏi gốc: ""{userQuestion}""
        
            Dữ liệu từ cơ sở dữ liệu (định dạng JSON):
            {databaseResultsJson}

            Câu trả lời của bạn:
        ";

            string finalAnswer = await CallGeminiWithTextAsync(promptForAnswer);

            return finalAnswer;
        }

    public async Task<string> Rep_multi_query(string text)
        {
            return null;
        }

        public async Task<string> Classify_prompt(string text)
        {
            var prompt = $@"
Hãy giúp tôi phân loại prompt sau thành các loại:
OFF_TOPIC: Nếu prompt không liên quan đến tài chính cá nhân, thông tin cá nhân người dùng app.
ADD_TRANSACTION: Nếu prompt yêu cầu thêm giao dịch mới.
SINGLE_QUERY: Nếu prompt yêu cầu truy vấn thông tin cụ thể về tài chính cá nhân.
MULTI_QUERY: Nếu prompt yêu cầu truy vấn nhiều hơn 1 tác vụ về tài chính cá nhân.

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
            if (string.IsNullOrEmpty(_opts.ApiKey))
            {
                throw new InvalidOperationException("Gemini API Key is not configured. Please check your user secrets.");
            }

            var client = _httpFactory.CreateClient("gemini");
            var url = $"{_opts.BaseUrl}/{_opts.Model}:generateContent?key={_opts.ApiKey}";

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