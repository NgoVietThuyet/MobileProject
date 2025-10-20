using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.Notification.PushNotification
{
    public class PushNotificationRequest
    {
        [Required(ErrorMessage = "UserId là bắt buộc")]
        public string UserId { get; set; }

        [Required(ErrorMessage = "Content là bắt buộc")]
        public string Content { get; set; }
    }
}
