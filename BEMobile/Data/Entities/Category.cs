using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Data.Entities
{
    [Table("CATEGORIES")]
    public class Category
    {
        [Key]
        [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
        [Column("CATEGORY_ID")]
        public string Id { get; set; }

        [Column("NAME")]
        public string? Name { get; set; }

        [Column("ICON")]
        public string? Icon { get; set; }
       
        [Column("CREATED_DATE")]
        public string? CreatedDate { get; set; }
        [Column("UPDATED_DATE")]
        public string? UpdatedDate { get; set; }

    }
}