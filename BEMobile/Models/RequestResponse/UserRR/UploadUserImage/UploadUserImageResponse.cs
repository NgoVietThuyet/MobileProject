using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.UserRR.UploadUserImage
{
    public class UploadUserImageResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public UserDto? User { get; set; }
    }
}
