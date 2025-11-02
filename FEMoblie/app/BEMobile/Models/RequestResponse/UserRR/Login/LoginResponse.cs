using BEMobile.Models.DTOs;


namespace BEMobile.Models.RequestResponse.UserRR.Login

{
    public class LoginResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }
        public UserDto User { get; set; }
        public string? AccessToken { get; set; }
        public string? RefreshToken { get; set; }
        //public string Token { get; set; } // Nếu có JWT
    }
}
