using BEMobile.Data.Entities;
using BEMobile.Models.DTOs;


using BEMobile.Models.RequestResponse.AccountRR.CreateAccount;

using BEMobile.Services;
using Microsoft.EntityFrameworkCore;


namespace BEMobile.Services
{
    public interface IUserService
    {

        Task<UserDto> CreateUserAsync(UserDto userDto);

        Task<UpdateUserResponse> UpdateUserAsync(UpdateUserRequest request);
        Task<UserDto> IsLogin(string email, string password);

    }

    public class UserService : IUserService
    {
        private readonly AppDbContext _context;

        private readonly IAccountService _accountService;

        public UserService(AppDbContext context, IAccountService accountService)
        {
            _context = context;
            _accountService = accountService;
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
                Job = userDto.Job,
                Google = userDto.Google,
                DateOfBirth = userDto.DateOfBirth,
                CreatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss")
            };

            _context.Users.Add(user);
            await _context.SaveChangesAsync();

            // Return the created user without password
            userDto.UserId = user.UserId;
            userDto.Password = null; // Ensure password is not returned

            await _accountService.CreateAccountAsync(new CreateAccountRequest
            {
                Account = new AccountDto
                {
                    UserId = user.UserId,
                    Balance = "0"
                }
            });

            return userDto;
        }


        public async Task<UpdateUserResponse> UpdateUserAsync(UpdateUserRequest request)
        {
            var response = new UpdateUserResponse();

            try
            {

                if (request == null)
                {
                    response.Success = false;
                    response.Message = "Request body is null.";
                    return response;
                }

                if (string.IsNullOrEmpty(request.UserId))
                {
                    response.Success = false;
                    response.Message = "UserId is missing or empty in request body.";
                    return response;
                }

                var existingUser = await _context.Users
                    .FirstOrDefaultAsync(u => u.UserId == request.UserId);

                if (existingUser == null)
                {
                    response.Success = false;
                    response.Message = $"User with ID {request.UserId} not found.";
                    return response;
                }
                // Update fields
                existingUser.Name = request.Name;
                existingUser.PhoneNumber = request.PhoneNumber;
                existingUser.Facebook = request.Facebook;
                existingUser.Twitter = request.Twitter;
                existingUser.Email = request.Email;
                existingUser.Job = request.Job;
                existingUser.Google = request.Google;
                existingUser.DateOfBirth = request.DateOfBirth;
                existingUser.UpdatedDate = DateTime.UtcNow.ToString("dd/MM/yyyy HH:mm:ss");

                await _context.SaveChangesAsync();

                // Prepare response
                response.Success = true;
                response.Message = "User updated successfully.";
                response.User = new UserDto
                {
                    UserId = existingUser.UserId,
                    Name = existingUser.Name,
                    PhoneNumber = existingUser.PhoneNumber,
                    Facebook = existingUser.Facebook,
                    Twitter = existingUser.Twitter,
                    Email = existingUser.Email,
                    Job = existingUser.Job,
                    Google = existingUser.Google,
                    DateOfBirth = existingUser.DateOfBirth,
                    CreatedDate = existingUser.CreatedDate,
                    UpdatedDate = existingUser.UpdatedDate
                };
            }
            catch (Exception ex)
            {
                response.Success = false;
                response.Message = $"Error updating user: {ex.Message}";
            }

            return response;
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
                Job = user.Job,
                Google = user.Google,
                DateOfBirth = user.DateOfBirth,
                CreatedDate = user.CreatedDate,
                UpdatedDate = user.UpdatedDate

            };
        }
    }
}