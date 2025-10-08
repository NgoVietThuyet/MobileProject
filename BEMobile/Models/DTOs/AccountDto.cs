using BEMobile.Data.Entities;
using System.Text.Json.Serialization;
using static System.Runtime.InteropServices.JavaScript.JSType;

namespace BEMobile.Models.DTOs
{
    public class AccountDto
    {
        public string AccountId { get; set; }
        public string UserId { get; set; }
        public decimal Balance { get; set; }

        // Có thể giữ dưới dạng JSON string hoặc tách ra List<TransactionDto>
        public string? ListTransactions { get; set; }

    }
}
