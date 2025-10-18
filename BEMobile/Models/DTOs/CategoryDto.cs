using System.ComponentModel.DataAnnotations.Schema;

namespace BEMobile.Models.DTOs
{
    public class CategoryDto
    {
        public string Id { get; set; }

       
        public string? Name { get; set; }

       
        public string? Icon { get; set; }
       

        public string CreatedDate { get; set; }

        public string? UpdatedDate { get; set; }
    }
}
