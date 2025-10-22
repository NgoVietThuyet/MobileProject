using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using BEMobile.Services;

namespace BEMobile.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ImageController : ControllerBase
    {
        private readonly IImageService _imageService;

        public ImageController(IImageService imageService)
        {
            _imageService = imageService;
        }

        [HttpPost("imageUpload")]
        public async Task<IActionResult> UploadImage(IFormFile file, [FromQuery] bool embedBase64 = false)
        {
            try
            {
                if (file == null || file.Length == 0)
                    return BadRequest("Không có tệp nào được tải lên.");

                var transactions = await _imageService.ProcessReceiptToTransactionsAsync(file, userId: "omg", embedBase64: embedBase64);

                if (transactions == null || !transactions.Any())
                {
                    return BadRequest(new
                    {
                        Success = false,
                        Message = "Không thể đọc được nội dung hoá đơn. Vui lòng thử lại hoặc chụp rõ hơn."
                    });
                }

                return Ok(new
                {
                    Success = true,
                    Message = "Xử lý hoá đơn thành công",
                    Transactions = transactions
                });
            }
            catch (Exception ex)
            {
                return StatusCode(500, new
                {
                    Success = false,
                    Message = "Lỗi hệ thống",
                    Error = ex.Message
                });
            }
        }

    }
}
