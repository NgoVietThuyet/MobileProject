using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("TRANSACTIONS")]
    public class Transaction
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("TRANSACTION_ID")]
        public string TransactionId { get; set; }

        [Required]
        [Column("USER_ID")]
        public string UserId { get; set; }

        [Column("CATEGORY_ID")]
        public string CategoryId { get; set; }

        [Column("TYPE")]
        public string Type { get; set; }

        [Column("AMOUNT")]
        public string Amount { get; set; }

        [Column("NOTE")]
        public string Note { get; set; }

        [Column("CREATED_DATE")]
        public string CreatedDate { get; set; }

        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }

    }
}