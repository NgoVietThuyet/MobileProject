
﻿using BEMobile.Services;
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

            var extractedData = await _kgService.Classify_prompt(request.Text);
            var cleanedData = extractedData.Trim().ToUpperInvariant();

            switch(cleanedData)
            {
                case "OFF_TOPIC":
                    return Ok("Xin lỗi, mình chỉ hỗ trợ quản lý tài chính thui!");
                case "ADD_TRANSACTION":
                    var responseAdd = await _kgService.Rep_add_transaction(request.Text);
                    return Ok(responseAdd);
                case "SINGLE_QUERY":
                    var responseSing = await _kgService.Rep_multi_query(request.Text, "1");
                    return Ok(responseSing);
                case "MULTI_QUERY":
                    var responseMulti = await _kgService.Rep_multi_query(request.Text, "1");
                    return Ok(responseMulti);
                case null:
                    return StatusCode(StatusCodes.Status500InternalServerError, "Lỗi khi xử lý yêu cầu.");
            }
            return Ok( cleanedData);
        }

    }
}
