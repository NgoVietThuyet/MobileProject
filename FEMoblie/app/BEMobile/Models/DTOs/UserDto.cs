using BEMobile.Data.Entities;
using System.ComponentModel.DataAnnotations.Schema;
using System.Text.Json.Serialization;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace BEMobile.Models.DTOs
{
    public class UserDto
    {
        public string?  UserId { get; set; }
        public string? Name { get; set; }
        public string? PhoneNumber { get; set; }
        public string? Facebook { get; set; }
        public string? Twitter { get; set; }
        public string? Email { get; set; } 
        public string? Job { get; set; }
        public string? Google { get; set; }
        public string? DateOfBirth { get; set; }
        public string? Password { get; set; }

        public string? UrlImage { get; set; }

        public string? CreatedDate { get; set; }
        public string? UpdatedDate { get; set; }

        public string? RefreshToken { get; set; }


        public DateTime? RefreshTokenExpiry { get; set; }

    }
}