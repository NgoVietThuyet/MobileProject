using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.UserRR.ChangePassword
{
    public class ChangePasswordRequest
    {
        [Required]
        public string UserId { get; set; }

        [Required]
        public string OldPassword { get; set; }

        [Required]
        public string NewPassword { get; set; }

        [Required]
        public string ConfirmPassword { get; set; }
    }
}
