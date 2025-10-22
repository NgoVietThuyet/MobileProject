using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.Notification.PushNotification
{
    public class PushNotificationResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public NotificationDto? Notification { get; set; }
    }
}
