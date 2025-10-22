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

            var response = await _service.GetAllNotificationsAsync(userId);

            return Ok(response);
        }

        // PUT /api/notifications/{id}/read
        [HttpPut("{id}/read")]
        [ProducesResponseType(typeof(ReadNotificationResponse), 200)]
        public async Task<IActionResult> MarkAsRead([FromRoute] string id)
        {
            var response = await _service.MarkAsReadAsync(id);

            return Ok(response);
        }

        // DELETE /api/notifications/{id}
        [HttpDelete("Delete/{id}")]
        [ProducesResponseType(typeof(DeleteNotificationResponse), 200)]
        public async Task<IActionResult> Delete([FromRoute] string id)
        {
            var response = await _service.DeleteNotificationAsync(id);

            return Ok(response);
        }

        // POST /api/notifications/push
        // 🟣 Internal use only (not in Swagger)
        [ApiExplorerSettings(IgnoreApi = true)]
        [HttpPost("push")]
        [ProducesResponseType(typeof(PushNotificationResponse), 200)]
        public async Task<IActionResult> Push([FromBody] PushNotificationRequest request)
        {
            var response = await _service.PushNotificationAsync(request);
            return Ok(response);
        }
    }
}
