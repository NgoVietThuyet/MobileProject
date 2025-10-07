using BEMobile.Data.Entities;
using Microsoft.EntityFrameworkCore;
using BEMobile.Models.DTOs;

namespace BEMobile.Services
{
    public interface IUserService
    {
        Task<IEnumerable<UserDto>> GetAllUsersAsync();
        Task<UserDto> CreateUserAsync(UserDto UserDto);
        Task UpdateUserAsync( UserDto UserDto);
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
            var Users = await _context.Users
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

            return Users;
        }

        
        public async Task<UserDto> CreateUserAsync(UserDto UserDto)
        {

            var User = new User
            {
                UserId = Guid.NewGuid().ToString(),
                Name = UserDto.Name,
                PhoneNumber = UserDto.PhoneNumber,
                Facebook = UserDto.Facebook,
                Twitter = UserDto.Twitter,
                Email = UserDto.Email,
                Password = UserDto.Password,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };

            _context.Users.Add(User);
            await _context.SaveChangesAsync();

            // Return the created User without password
            UserDto.UserId = User.UserId;
            UserDto.Password = null; // Ensure password is not returned
            return UserDto;
        }

        public async Task UpdateUserAsync(UserDto UserDto)
        {
            try
            {
                // 1. Kiểm tra đầu vào
                if (UserDto == null || string.IsNullOrEmpty(UserDto.UserId))
                    throw new ArgumentException("UserDto cannot be null.");

                // 2. Tìm User hiện có trong database
                var existingUser = await _context.Users
                    .FirstOrDefaultAsync(u => u.UserId == UserDto.UserId);

                if (existingUser == null) throw new KeyNotFoundException($"User with ID {UserDto.UserId} not found.");

                // 3. Cập nhật thông tin (chỉ các trường cho phép)
                existingUser.Name = UserDto.Name;
                existingUser.PhoneNumber = UserDto.PhoneNumber;
                existingUser.Facebook = UserDto.Facebook;
                existingUser.Twitter = UserDto.Twitter;
                existingUser.Email = UserDto.Email;
                existingUser.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                // 5. Lưu thay đổi
                await _context.SaveChangesAsync();

            }
            catch (Exception ex)
            {
                throw new Exception($"Error updating User {UserDto?.UserId}", ex);
            }
        }

        public async Task<bool> DeleteUserAsync(string id)
        {
            var User = await _context.Users
                .FirstOrDefaultAsync(u => u.UserId == id);

            if (User == null) return false;

            _context.Users.Remove(User); // Xóa vật lý khỏi database
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

            var Users = await query
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

            return Users;
        }
        public async Task<UserDto> IsLogin(string email, string password)
        {
            var User = await _context.Users
                .FirstOrDefaultAsync(u => u.Email == email && u.Password ==  password);
            if(User == null) return null;
            return  new UserDto
            {
                UserId = User.UserId,
                Name = User.Name,
                PhoneNumber = User.PhoneNumber,
                Password = User.Password,
                Facebook = User.Facebook,
                Twitter = User.Twitter,
                Email = User.Email,
                CreatedDate = User.CreatedDate,
                UpdatedDate = User.UpdatedDate
                // Lưu ý: Password không được map vì DTO có [JsonIgnore] nhưng nếu muốn có thể bỏ qua
            };
        }
    }
}