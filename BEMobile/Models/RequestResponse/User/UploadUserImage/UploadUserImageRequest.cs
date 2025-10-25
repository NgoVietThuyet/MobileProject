using Microsoft.AspNetCore.Http;

namespace BEMobile.Models.RequestResponse.User.UploadUserImage
{
    public class UploadUserImageRequest
    {
        public string UserId { get; set; } = string.Empty;

        public IFormFile? ImageFile { get; set; }
    }
}
