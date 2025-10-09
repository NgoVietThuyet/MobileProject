using BEMobile.Data.Entities;
using Microsoft.EntityFrameworkCore;
using BEMobile.Models.DTOs;

namespace BEMobile.Services
{
    public interface IUserService
    {
        Task<IEnumerable<UserDto>> GetAllUsersAsync();
        Task<UserDto> CreateUserAsync(UserDto userDto);
        Task UpdateUserAsync( UserDto userDto);
        Task<bool> DeleteUserAsync(string id);
        Task<IEnumerable<UserDto>> SearchUsersAsync(string? name, string? email, string? phoneNumber);
        Task<UserDto> IsLogin(string email, string password);

    }

    public class UserService : IUserService
    {
        private readonly AppDbContext _context;
        
        public UserService(AppDbContext context)
        {
            _context = context;
        }

        public async Task<IEnumerable<UserDto>> GetAllUsersAsync()
        {
            var users = await _context.Users
                .Select(u => new UserDto
                {
                    UserId = u.UserId,
                    Name = u.Name,
                    PhoneNumber = u.PhoneNumber,
                    Facebook = u.Facebook,
                    Twitter = u.Twitter,
                    Email = u.Email,
                    CreatedDate = u.CreatedDate,
                    UpdatedDate = u.UpdatedDate,
                    
                })
                .ToListAsync();

            return users;
        }

        
        public async Task<UserDto> CreateUserAsync(UserDto userDto)
        {

            var user = new User
            {
                UserId = Guid.NewGuid().ToString(),
                Name = userDto.Name,
                PhoneNumber = userDto.PhoneNumber,
                Facebook = userDto.Facebook,
                Twitter = userDto.Twitter,
                Email = userDto.Email,
                Password = userDto.Password,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            // Return the created user without password
            userDto.UserId = user.UserId;
            userDto.Password = null; // Ensure password is not returned
            return userDto;
        }

        public async Task UpdateUserAsync(UserDto userDto)
        {
            try
            {
                // 1. Kiểm tra đầu vào
                if (userDto == null || string.IsNullOrEmpty(userDto.UserId))
                    throw new ArgumentException("UserDto cannot be null.");

                // 2. Tìm user hiện có trong database
                var existingUser = await _context.Users
                    .FirstOrDefaultAsync(u => u.UserId == userDto.UserId);

                if (existingUser == null) throw new KeyNotFoundException($"User with ID {userDto.UserId} not found.");

                // 3. Cập nhật thông tin (chỉ các trường cho phép)
                existingUser.Name = userDto.Name;
                existingUser.PhoneNumber = userDto.PhoneNumber;
                existingUser.Facebook = userDto.Facebook;
                existingUser.Twitter = userDto.Twitter;
                existingUser.Email = userDto.Email;
                existingUser.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                // 5. Lưu thay đổi
                await _context.SaveChangesAsync();

            }
            catch (Exception ex)
            {
                throw new Exception($"Error updating user {userDto?.UserId}", ex);
            }
        }

        public async Task<bool> DeleteUserAsync(string id)
        {
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.UserId == id);

            if (user == null) return false;

            _context.Users.Remove(user); // Xóa vật lý khỏi database
            await _context.SaveChangesAsync();
            return true;
        }

        public async Task<IEnumerable<UserDto>> SearchUsersAsync(string? name, string? email, string? phoneNumber)
        {
            var query = _context.Users.AsQueryable();

            if (!string.IsNullOrEmpty(name))
                query = query.Where(u => u.Name.Contains(name));

            if (!string.IsNullOrEmpty(email))
                query = query.Where(u => u.Email.Contains(email));

            if (!string.IsNullOrEmpty(phoneNumber))
                query = query.Where(u => u.PhoneNumber.Contains(phoneNumber));

            var users = await query
                .Select(u => new UserDto
                {
                    UserId = u.UserId,
                    Name = u.Name,
                    PhoneNumber = u.PhoneNumber,
                    Facebook = u.Facebook,
                    Twitter = u.Twitter,
                    Email = u.Email
                })

                .ToListAsync();

            return users;
        }
        public async Task<UserDto> IsLogin(string email, string password)
        {
            var user = await _context.Users
                .FirstOrDefaultAsync(u => u.Email == email && u.Password ==  password);
            if(user == null) return null;
            return  new UserDto
            {
                UserId = user.UserId,
                Name = user.Name,
                PhoneNumber = user.PhoneNumber,
                Password = user.Password,
                Facebook = user.Facebook,
                Twitter = user.Twitter,
                Email = user.Email,
                CreatedDate = user.CreatedDate,
                UpdatedDate = user.UpdatedDate
                // Lưu ý: Password không được map vì DTO có [JsonIgnore] nhưng nếu muốn có thể bỏ qua
            };
        }
    }
}