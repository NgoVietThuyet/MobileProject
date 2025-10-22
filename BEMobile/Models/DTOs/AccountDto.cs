using BEMobile.Data.Entities;
using System.Text.Json.Serialization;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace BEMobile.Models.DTOs
{
    public class AccountDto
    {
        public string AccountId { get; set; }
        public string UserId { get; set; }

        public string Balance { get; set; }
        public string CreatedDate { get; set; }
        public string UpdatedDate { get; set; }


    }
}
