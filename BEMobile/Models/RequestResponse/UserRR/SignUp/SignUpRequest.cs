using BEMobile.Models.DTOs;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.User.SignUp
{
    public class SignUpRequest
    {
        public UserDto UserDto { get; set; }
    }
}
