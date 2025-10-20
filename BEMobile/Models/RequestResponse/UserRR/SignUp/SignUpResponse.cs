using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.User.SignUp
{
    public class SignUpResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public UserDto User { get; set; }
    }
}
