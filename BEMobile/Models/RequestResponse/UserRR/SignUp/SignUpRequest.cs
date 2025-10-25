using BEMobile.Models.DTOs;
using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.UserRR.SignUp
{
    public class SignUpRequest
    {
        public UserDto UserDto { get; set; }
    }
}
