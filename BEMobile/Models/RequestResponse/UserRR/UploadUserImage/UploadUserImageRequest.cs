using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using System.ComponentModel.DataAnnotations;


namespace BEMobile.Models.RequestResponse.UserRR.UploadUserImage
{
    public class UploadUserImageRequest
    {
        [Required]
        [FromForm(Name = "UserId")]
        public string UserId { get; set; }

        [Required]
        [FromForm(Name = "ImageFile")]
        public IFormFile ImageFile { get; set; }
    }
}
