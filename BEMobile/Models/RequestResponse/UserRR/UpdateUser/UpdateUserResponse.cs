using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.UserRR.UpdateUser
{
    public class UpdateUserResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public UserDto? User { get; set; }
    }
}
