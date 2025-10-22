using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("ACCOUNTS")]
    public class Account
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("ACCOUNT_ID")]
        public string AccountId { get; set; }

        [Required]
        [Column("USER_ID")]
        public string UserId { get; set; }

        [Required]
        [Column("BALANCE")]
        public string Balance { get; set; }

        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }

    }
}