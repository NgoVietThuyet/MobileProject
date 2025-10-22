using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.Notification.GetAllNotification
{
    public class GetAllNotificationResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public IEnumerable<NotificationDto>? Notifications { get; set; }
    }
}
