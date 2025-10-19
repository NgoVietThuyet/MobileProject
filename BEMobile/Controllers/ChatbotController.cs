using BEMobile.Services;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;

namespace BEMobile.Controllers
{
    public class ChatRequest
    {
        public string Text { get; set; }
    }

    [Route("api/[controller]")]
    [ApiController]

    public class ChatbotController : ControllerBase
    {
        private readonly IKnowledgeGraphService _kgService;

        public ChatbotController(IKnowledgeGraphService kgService)
        {
            _kgService = kgService;
        }

        [HttpPost("extract")]
        public async Task<IActionResult> ExtractGraph([FromBody] ChatRequest request)
        {
            if (string.IsNullOrWhiteSpace(request?.Text))
            {
                return BadRequest("Đầu vào không hợp lệ!");
            }

            var extractedData = await _kgService.ExtractGraphFromTextAsync(request.Text);

            // Trả về kết quả dạng text thô từ Gemini
            return Ok(extractedData);
        }

    }
}
