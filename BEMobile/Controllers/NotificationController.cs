using BEMobile.Models.RequestResponse.Notification.GetAllNotification;
using BEMobile.Models.RequestResponse.Notification.ReadNotification;
using BEMobile.Models.RequestResponse.Notification.DeleteNotification;
using BEMobile.Models.RequestResponse.Notification.PushNotification;
using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;

namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class NotificationsController : ControllerBase
    {
        private readonly INotificationService _service;

        public NotificationsController(INotificationService service)
        {
            _service = service;
        }

        // GET /api/notifications?userId=...
        [HttpGet("GetAll")]
        [ProducesResponseType(typeof(GetAllNotificationResponse), 200)]
        public async Task<IActionResult> GetAll([FromQuery] string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return BadRequest(new GetAllNotificationResponse
                {
                    Success = false,
                    Message = "UserId là bắt buộc"
                });

            var notifications = await _service.GetAllNotificationsAsync(userId);

            return Ok(new GetAllNotificationResponse
            {
                Success = true,
                Message = "Lấy danh sách thông báo thành công",
                Notifications = notifications
            });
        }

        // PUT /api/notifications/{id}/read
        [HttpPut("{id}/read")]
        [ProducesResponseType(typeof(ReadNotificationResponse), 200)]
        public async Task<IActionResult> MarkAsRead([FromRoute] string id)
        {
            var result = await _service.MarkAsReadAsync(id);

            return Ok(new ReadNotificationResponse
            {
                Success = result,
                Message = result ? "Đánh dấu đã đọc thành công" : "Không tìm thấy thông báo"
            });
        }

        // DELETE /api/notifications/{id}
        [HttpDelete("Delete")]
        [ProducesResponseType(typeof(DeleteNotificationResponse), 200)]
        public async Task<IActionResult> Delete([FromRoute] string id)
        {
            var result = await _service.DeleteNotificationAsync(id);

            return Ok(new DeleteNotificationResponse
            {
                Success = result,
                Message = result ? "Xóa thông báo thành công" : "Không tìm thấy thông báo"
            });
        }

        // POST /api/notifications/push
        // 🟣 Internal use only (not in Swagger)
        [ApiExplorerSettings(IgnoreApi = true)]
        [HttpPost("push")]
        [ProducesResponseType(typeof(PushNotificationResponse), 200)]
        public async Task<IActionResult> Push([FromBody] PushNotificationRequest request)
        {
            var result = await _service.PushNotificationAsync(request.UserId, request.Content);

            return Ok(new PushNotificationResponse
            {
                Success = result,
                Message = result ? "Gửi thông báo thành công" : "Gửi thông báo thất bại"
            });
        }
    }
}
