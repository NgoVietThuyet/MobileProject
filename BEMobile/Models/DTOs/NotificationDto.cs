using BEMobile.Data.Entities;
using System.Text.Json.Serialization;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace BEMobile.Models.DTOs
{
    public class NotificationDto
    {
        public string NotificationId { get; set; }
        public string UserId { get; set; }

        public bool IsRead { get; set; }
        public string Content { get; set; }
        public string CreatedDate { get; set; }
        public string? UpdatedDate { get; set; }
    }
}
