using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("CATEGORIES")]
    public class Categories
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("CATEGORY_ID")]
        public string Id { get; set; }

        [Required]
        [Column("NAME")]
        public string Name { get; set; }

        

        

        [Column("CREATE_DATE")]
        public string CreatedDate { get; set; }
        [Column("UPDATE_DATE")]
        public string? UpdatedDate { get; set; }

    }
}