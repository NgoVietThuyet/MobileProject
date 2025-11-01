using BEMobile.Models.DTOs;
namespace BEMobile.Models.RequestResponse.UserRR.PhoneLogin;

public class PhoneLoginResponse
{
    public bool Success { get; set; }
    public string Message { get; set; } = string.Empty;
    public string? AccessToken { get; set; }
    public string? RefreshToken { get; set; }
    public UserDto? User { get; set; }
}
