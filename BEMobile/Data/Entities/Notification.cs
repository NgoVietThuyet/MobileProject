using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("NOTIFICATIONS")]
    public class Notification
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("NOTIFICATION_ID")]
        public string NotificationId { get; set; }

        [Required]
        [Column("USER_ID")]
        public string UserId { get; set; }

        [Column("IS_READ")]
        public bool IsRead { get; set; } = false;


        [Column("CONTENT")]
        public string Content { get; set; }

        [Column("CREATED_DATE")]
        public string CreatedDate { get; set; }

        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }

    }
}