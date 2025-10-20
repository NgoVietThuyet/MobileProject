using System;
using System.IO;
using System.Threading.Tasks;
using System.Collections.Generic;
using System.Text;
using System.Net.Http;
using Microsoft.AspNetCore.Http;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;
using BEMobile.Models.DTOs;

namespace BEMobile.Services
{
    public class GeminiOptions
    {
        public string ApiKey { get; set; } = "AIzaSyC2R9aILsvNeEripP7Wcy6E6Pt5UQYmwTE";
        public string Model { get; set; } = "gemini-2.0-flash";
        public string BaseUrl { get; set; } = "https://generativelanguage.googleapis.com/v1beta/models";
        public bool UseBearerToken { get; set; } = false; // Sửa thành false để dùng API Key
        public string ServiceAccountFile { get; set; } // Thêm property này
    }

    public interface IImageService
    {
        Task<IList<TransactionDto>> ProcessReceiptToTransactionsAsync(IFormFile file, string? userId = null, bool embedBase64 = false);
    }

    public class ImageService : IImageService
    {
        private readonly IHttpClientFactory _httpFactory;
        private readonly GeminiOptions _opts;
        private readonly ILogger<ImageService> _logger;

        // Temporary DTO used to parse AI response
        private class ExpenseItemTemp
        {
            [JsonProperty("category")]
            public string Category { get; set; } = string.Empty;

            [JsonProperty("amount")]
            public decimal Amount { get; set; }

            [JsonProperty("note")]
            public string? Note { get; set; }
        }

        public ImageService(IHttpClientFactory httpFactory, IOptions<GeminiOptions> opts, ILogger<ImageService>? logger = null)
        {
            _httpFactory = httpFactory ?? throw new ArgumentNullException(nameof(httpFactory));
            _opts = opts?.Value ?? throw new ArgumentNullException(nameof(opts));
            _logger = logger ?? Microsoft.Extensions.Logging.Abstractions.NullLogger<ImageService>.Instance;
        }

        public async Task<IList<TransactionDto>> ProcessReceiptToTransactionsAsync(IFormFile file, string? userId = null, bool embedBase64 = false)
        {
            if (file == null || file.Length == 0)
                throw new ArgumentException("file is null or empty", nameof(file));

            // 1) Read bytes from IFormFile
            byte[] fileBytes;
            using (var ms = new MemoryStream())
            {
                await file.CopyToAsync(ms);
                fileBytes = ms.ToArray();
            }

            // 2) Build prompt
            var categories = new[]
            {
                "Ăn uống","Đi lại","Mua sắm","Giải trí",
                "Giáo dục","Ý tế","Nhà ở","Điện nước","Khác"
            };
            string prompt = BuildPromptForImage(categories);

            // 3) Call Gemini with image + prompt
            string modelResponse;
            try
            {
                modelResponse = await CallGeminiWithImageAsync(prompt, fileBytes, file.ContentType ?? "image/jpeg");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "CallGeminiWithImageAsync failed");
                throw;
            }

            // 4) Parse response
            var expenses = ParseJsonArrayFromText<ExpenseItemTemp>(modelResponse);

            // 5) Map to TransactionDto
            var transactions = new List<TransactionDto>();
            foreach (var e in expenses)
            {
                var tx = new TransactionDto
                {
                    TransactionId = "",
                    UserId = userId ?? string.Empty,
                    CategoryId = e.Category ?? string.Empty,
                    Type = "expense",
                    Amount = (float)e.Amount,
                    Note = string.IsNullOrWhiteSpace(e.Note) ? null : e.Note,
                    CreatedDate = DateTime.UtcNow.ToString("o"),
                    UpdatedDate = null
                };
                transactions.Add(tx);
            }

            return transactions;
        }

        private string BuildPromptForImage(string[] categories)
        {
            var catsText = string.Join(", ", categories);
            return $@"
Hãy phân tích hình ảnh hóa đơn và trả về mảng JSON các đối tượng với các trường:
- category (một trong: {catsText})
- amount (số nguyên, đơn vị VND)
- note (chuỗi mô tả mặt hàng)

Yêu cầu:
1. Xác định các mặt hàng và giá tiền (đọc giá tiền cho mỗi món nhưng lưu ý thêm nếu có thuế hoặc giảm giá)
2. Gán mỗi mặt hàng vào một danh mục phù hợp
3. Tính tổng số tiền cho từng danh mục
4. Nếu danh mục không có mặt hàng, hãy bỏ qua

Ví dụ đầu ra:
[
  {{""category"": ""Ăn uống"", ""amount"": 20000, ""note"": ""Bánh đa cua""}},
  {{""category"": ""Khác"", ""amount"": 15000, ""note"": ""Túi nilon""}}
]

Chỉ trả về mảng JSON, không thêm bất kỳ văn bản nào khác. Phân tích kỹ để đảm bảo tính chính xác về số liệu. Nếu ảnh không thể đọc hoặc không phải hoá đơn, trả về mảng rỗng []
";
        }

        private async Task<string> CallGeminiWithImageAsync(string prompt, byte[] imageBytes, string contentType)
        {
            var client = _httpFactory.CreateClient("gemini");

            var url = $"{_opts.BaseUrl}/{_opts.Model}:generateContent";

            var imageBase64 = Convert.ToBase64String(imageBytes);
            var body = new
            {
                contents = new[]
                {
                    new
                    {
                        parts = new object[]
                        {
                            new
                            {
                                inline_data = new
                                {
                                    mime_type = contentType,
                                    data = imageBase64
                                }
                            },
                            new
                            {
                                text = prompt
                            }
                        }
                    }
                },
                generationConfig = new
                {
                    temperature = 0,
                    topK = 10,
                    topP = 0.3,
                    maxOutputTokens = 256
                }
            };

            var json = JsonConvert.SerializeObject(body);
            _logger.LogInformation("Sending request to Gemini API...");

            using var request = new HttpRequestMessage(HttpMethod.Post, url)
            {
                Content = new StringContent(json, Encoding.UTF8, "application/json")
            };

            if (!_opts.UseBearerToken && !string.IsNullOrEmpty(_opts.ApiKey))
            {
                url = $"{url}?key={_opts.ApiKey}";
                request.RequestUri = new Uri(url);
            }
            else if (_opts.UseBearerToken) // Nếu API xu
            {
                var accessToken = await GetAccessTokenAsync();
                request.Headers.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", accessToken);
            }
            else
            {
                throw new InvalidOperationException("No authentication method configured for Gemini API");
            }

            var response = await client.SendAsync(request);
            var responseText = await response.Content.ReadAsStringAsync();

            if (!response.IsSuccessStatusCode)
            {
                _logger.LogError("Gemini API returned {StatusCode}: {Response}", response.StatusCode, responseText);
                throw new HttpRequestException($"Gemini API error: {response.StatusCode} - {responseText}");
            }

            try
            {
                var responseObj = JsonConvert.DeserializeObject<GeminiResponse>(responseText);
                return responseObj?.Candidates?[0]?.Content?.Parts?[0]?.Text ?? string.Empty;
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to parse Gemini response");
                throw;
            }
        }

        private class GeminiResponse
        {
            [JsonProperty("candidates")]
            public List<Candidate> Candidates { get; set; }
        }

        private class Candidate
        {
            [JsonProperty("content")]
            public Content Content { get; set; }
        }

        private class Content
        {
            [JsonProperty("parts")]
            public List<Part> Parts { get; set; }
        }

        private class Part
        {
            [JsonProperty("text")]
            public string Text { get; set; }
        }

        private List<T> ParseJsonArrayFromText<T>(string text)
        {
            var list = new List<T>();
            if (string.IsNullOrWhiteSpace(text))
            {
                _logger.LogWarning("Empty response from Gemini");
                return list;
            }

            _logger.LogInformation("Gemini raw response: {Text}", text);

            int start = text.IndexOf('[');
            int end = text.LastIndexOf(']');

            if (start == -1 || end == -1 || end < start)
            {
                _logger.LogWarning("No JSON array found in response");
                return list;
            }

            var jsonStr = text.Substring(start, end - start + 1);

            try
            {
                var result = JsonConvert.DeserializeObject<List<T>>(jsonStr);
                return result ?? new List<T>();
            }
            catch (JsonException ex)
            {
                _logger.LogWarning(ex, "Failed to parse JSON, trying to fix...");

                var fixedStr = jsonStr.Replace("'", "\"");
                try
                {
                    return JsonConvert.DeserializeObject<List<T>>(fixedStr) ?? new List<T>();
                }
                catch
                {
                    _logger.LogError("Failed to parse JSON even after fixing");
                    return new List<T>();
                }
            }
        }

        private async Task<string> GetAccessTokenAsync()
        {
            // Chỉ gọi nếu dùng Service Account
            if (string.IsNullOrEmpty(_opts.ServiceAccountFile) || !File.Exists(_opts.ServiceAccountFile))
            {
                throw new InvalidOperationException("Service account file not found or not configured");
            }

            try
            {
                // Sử dụng Google.Apis.Auth library
                Google.Apis.Auth.OAuth2.GoogleCredential credential;
                using (var stream = new FileStream(_opts.ServiceAccountFile, FileMode.Open, FileAccess.Read))
                {
                    credential = Google.Apis.Auth.OAuth2.GoogleCredential.FromStream(stream)
                        .CreateScoped("https://www.googleapis.com/auth/cloud-platform");
                }

                var token = await credential.UnderlyingCredential.GetAccessTokenForRequestAsync();
                return token ?? throw new Exception("Failed to obtain access token");
            }
            catch (Exception ex)
            {
                _logger.LogError(ex, "Failed to get access token from service account");
                throw;
            }
        }
    }
}