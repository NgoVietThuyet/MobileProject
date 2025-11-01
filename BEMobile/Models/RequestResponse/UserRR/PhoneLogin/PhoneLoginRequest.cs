
namespace BEMobile.Models.RequestResponse.UserRR.PhoneLogin;
public class PhoneLoginRequest
{
    public string PhoneNumber { get; set; } = string.Empty; // định dạng +84xxx
    public string FirebaseIdToken { get; set; } = string.Empty;
}