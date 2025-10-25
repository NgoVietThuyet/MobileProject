namespace BEMobile.Models.RequestResponse.NotificationRR.PushNotification
stResponse/Notification/PushNotification/PushNotificationResponse.cs
{
    public class PushNotificationResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public NotificationDto? Notification { get; set; }
    }
}
