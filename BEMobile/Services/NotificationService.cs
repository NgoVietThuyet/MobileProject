using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using BEMobile.Models.RequestResponse.Notification.DeleteNotification;
using BEMobile.Models.RequestResponse.Notification.GetAllNotification;
using BEMobile.Models.RequestResponse.Notification.PushNotification;
using BEMobile.Models.RequestResponse.Notification.ReadNotification;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface INotificationService
    {
        Task<GetAllNotificationResponse> GetAllNotificationsAsync(string userId);
        Task<ReadNotificationResponse> MarkAsReadAsync(string notificationId);
        Task<DeleteNotificationResponse> DeleteNotificationAsync(string notificationId);
        Task<PushNotificationResponse> PushNotificationAsync(PushNotificationRequest req);
    }

    public class NotificationService : INotificationService
    {
        private readonly AppDbContext _context;

        public NotificationService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<GetAllNotificationResponse> GetAllNotificationsAsync(string userId)
        {
            var list = await _context.Notifications
                .Where(n => n.UserId == userId)
                .OrderByDescending(n => n.CreatedDate)
                .Select(n => new NotificationDto
                {
<<<<<<< Updated upstream
                    NotificationId = notification.NotificationId,
                    UserId = notification.UserId,
                    Content = notification.Content,
                    CreatedDate = notification.CreatedDate,
                    UpdatedDate = notification.UpdatedDate,
                    IsRead = notification.IsRead
                })
                .ToListAsync();
=======
                    NotificationId = n.NotificationId,
                    UserId = n.UserId,
                    Content = n.Content,
                    CreatedDate = n.CreatedDate,
                    UpdatedDate = n.UpdatedDate,
                    IsRead = n.IsRead 
                }).ToListAsync();

            return new GetAllNotificationResponse
            {
                Success = true,
                Message = "Lấy danh sách thông báo thành công",
                Notifications = list
            };
>>>>>>> Stashed changes
        }


        public async Task<ReadNotificationResponse> MarkAsReadAsync(string notificationId)
        {
            var noti = await _context.Notifications.FirstOrDefaultAsync(n => n.NotificationId == notificationId);
            if (noti == null)
                return new ReadNotificationResponse { Success = false, Message = "Không tìm thấy thông báo" };

            noti.IsRead = true;
            noti.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");
            await _context.SaveChangesAsync();

            return new ReadNotificationResponse { Success = true, Message = "Đã đánh dấu là đã đọc" };
        }


        public async Task<DeleteNotificationResponse> DeleteNotificationAsync(string notificationId)
        {
            var noti = await _context.Notifications.FirstOrDefaultAsync(n => n.NotificationId == notificationId);
            if (noti == null)
                return new DeleteNotificationResponse { Success = false, Message = "Không tìm thấy thông báo để xóa" };

            _context.Notifications.Remove(noti);
            await _context.SaveChangesAsync();

            return new DeleteNotificationResponse { Success = true, Message = "Xóa thông báo thành công" };
        }


        public async Task<PushNotificationResponse> PushNotificationAsync(PushNotificationRequest req)
        {
            if (string.IsNullOrWhiteSpace(req.UserId) || string.IsNullOrWhiteSpace(req.Content))
                return new PushNotificationResponse { Success = false, Message = "Thiếu userId hoặc nội dung" };

            var noti = new Notification
            {
                NotificationId = Guid.NewGuid().ToString(),
                UserId = req.UserId,
                Content = req.Content,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss"),
                IsRead = false
            };

            _context.Notifications.Add(noti);
            await _context.SaveChangesAsync();

            return new PushNotificationResponse
            {
                Success = true,
                Message = "Gửi thông báo thành công",
                Notification = new NotificationDto
                {
                    NotificationId = noti.NotificationId,
                    UserId = noti.UserId,
                    Content = noti.Content,
                    CreatedDate = noti.CreatedDate,
                    UpdatedDate = noti.UpdatedDate,
                    IsRead = noti.IsRead
                }
            };
        }

    }
}
