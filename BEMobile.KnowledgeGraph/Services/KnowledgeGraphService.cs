using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

using BEMobile.KnowledgeGraph.Connectors;
using Google.GenerativeAI;

namespace BEMobile.KnowledgeGraph.Services
{
    public class KnowledgeGraphService : IKnowledgeGraphService
    {
        private readonly INeo4jConnector _connector;

        // Cập nhật constructor để inject IConfiguration
        public KnowledgeGraphService(INeo4jConnector connector)
        {
            _connector = connector;
        }

        public async Task<string> ExtractGraphFromTextAsync(string text)
        {
            var apiKey = "hi";

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
                // Khởi tạo model Gemini (gemini-2.0-flash)
                var generativeModel = new GenerativeModel(apiKey: apiKey, model: "gemini-2.0-flash");

                // Gửi yêu cầu tới Gemini
                var response = await generativeModel.GenerateContentAsync(prompt);

                return response.Text;
            }
            catch (Exception e)
            {
                Console.WriteLine($"Error calling Gemini API: {e.Message}");
                return $"Lỗi khi gọi Gemini API: {e.Message}";
            }
        }
    }
}