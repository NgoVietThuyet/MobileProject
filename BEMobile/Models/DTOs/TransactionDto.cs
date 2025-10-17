using BEMobile.Data.Entities;
using System.Text.Json.Serialization;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace BEMobile.Models.DTOs
{
    public class TransactionDto
    {
        public string TransactionId { get; set; }
        public string UserId { get; set; }
        public string CategoryId { get; set; }
        public string Type { get; set; }
        public string Amount { get; set; }
        public string? Note { get; set; }
        public string CreatedDate { get; set; }
        public string? UpdatedDate { get; set; }
    }
}
