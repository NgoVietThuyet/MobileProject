using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("USERS")]
    public class User
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("USER_ID")]
        public string UserId { get; set; }

        [Required]
        [Column("FULL_NAME")]
        public string Name { get; set; }

        [Column("PHONE_NUMBER")]
        public string? PhoneNumber { get; set; }

        [Column("FACEBOOK")]
        public string? Facebook { get; set; }

        [Column("TWITTER")]
        public string? Twitter { get; set; }

        [Required]
        [EmailAddress]
        [Column("EMAIL")]
        public string? Email { get; set; } = string.Empty;

        [Required]
        [Column("PASSWORD")]
        public string Password { get; set; } = string.Empty;

        [Column("CREATE_DATE")]
        public string CreatedDate { get; set; } 
        [Column("UPDATE_DATE")]
        public string? UpdatedDate { get; set; }
       
    }
}