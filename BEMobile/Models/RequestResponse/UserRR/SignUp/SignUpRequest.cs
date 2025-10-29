using BEMobile.Models.DTOs;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.UserRR.SignUp
{
    public class SignUpRequest
    {
        public UserDto UserDto { get; set; }


        [Required(ErrorMessage = "Mật khẩu là bắt buộc")]
        [MinLength(6, ErrorMessage = "Mật khẩu phải có ít nhất 6 ký tự")]
        public string ConfirmPassword { get; set; }
    }
}
