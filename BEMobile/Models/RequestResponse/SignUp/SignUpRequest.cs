using BEMobile.Models.DTOs;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.SignUp
{
    public class SignUpRequest
    {
        public UserDto userDto { get; set; }
    }
}
