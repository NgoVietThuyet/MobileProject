using System;
using System.Collections.Generic;
using System.Net.Http;
using System.Text;
using System.Threading.Tasks;
using BEMobile.Connectors;
using BEMobile.Services;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;
using Newtonsoft.Json;

namespace BEMobile.Services
{
    public interface IKnowledgeGraphService
    {
        //Task CreateUserNodeAsync(UserDto user);
        //Task CreateTransactionAsync(TransactionDto transaction);
        // Hàm nhận một câu văn và trả về cấu trúc đồ thị được trích xuất
        Task<string> ExtractGraphFromTextAsync(string text);
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


        // Trích xuất node và đồ thị từ text
        public async Task<string> ExtractGraphFromTextAsync(string text)
        {
            var prompt = $@"
Extract entities (nodes) and their relationships (edges) from the text below.
Entities and relationships MUST be in Vietnamese
Follow this format:

- {{Entity}}: {{Type}}

Relationships:
- ({{Entity1}}, {{RelationshipType}}, {{Entity2}})

Text:
""{text}""

Ouput:
Entities:
- {{Entity}}: {{Type}}
...

Relationships:
- ({{Entity1}}, {{RelationshipType}}, {{Entity2}})
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