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

        [EmailAddress]
        [Column("EMAIL")]
        public string? Email { get; set; } = string.Empty;

        [Column("GOOGLE")]
        public string? Google { get; set; } = string.Empty;

        [Column("JOB")]
        public string? Job { get; set; } 

        [Column("DATE_OF_BIRTH")]
        public string? DateOfBirth { get; set; } 

        [Required]
        [Column("PASSWORD")]
        public string Password { get; set; } = string.Empty;

        [Column("CREATED_DATE")]
        public string CreatedDate { get; set; } 

        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }
       
    }
}