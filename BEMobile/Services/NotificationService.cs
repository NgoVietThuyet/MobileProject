using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;
using Microsoft.EntityFrameworkCore;

namespace BEMobile.Services
{
    public interface INotificationService
    {
        Task<IEnumerable<NotificationDto>> GetAllNotificationsAsync(string userId);
        Task<bool> MarkAsReadAsync(string notificationId);
        Task<bool> DeleteNotificationAsync(string notificationId);
        Task<bool> PushNotificationAsync(string userId, string content);
    }

    public class NotificationService : INotificationService
    {
        private readonly AppDbContext _context;

        public NotificationService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<NotificationDto>> GetAllNotificationsAsync(string userId)
        {
            return await _context.Notifications
                .Where(notification => notification.UserId == userId)
                .OrderByDescending(notification => notification.CreatedDate)
                .Select(notification => new NotificationDto
                {
                    NotificationId = notification.NotificationId,
                    UserId = notification.UserId,
                    Content = notification.Content,
                    CreatedDate = notification.CreatedDate,
                    UpdatedDate = notification.UpdatedDate,
                    IsRead = notification.IsRead
                })
                .ToListAsync();
        }

        public async Task<bool> MarkAsReadAsync(string notificationId)
        {
            var notification = await _context.Notifications
                .FirstOrDefaultAsync(notification => notification.NotificationId == notificationId);

            if (notification == null)
                return false;

            notification.IsRead = true;
            notification.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> DeleteNotificationAsync(string notificationId)
        {
            var notification = await _context.Notifications
                .FirstOrDefaultAsync(notification => notification.NotificationId == notificationId);

            if (notification == null)
                return false;

            _context.Notifications.Remove(notification);
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<bool> PushNotificationAsync(string userId, string content)
        {
            if (string.IsNullOrWhiteSpace(userId) || string.IsNullOrWhiteSpace(content))
                return false;

            var notification = new Notification
            {
                NotificationId = Guid.NewGuid().ToString(),
                UserId = userId,
                Content = content,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss"),
                IsRead = false
            };

            _context.Notifications.Add(notification);
            await _context.SaveChangesAsync();
            return true;
        }
    }
}
