using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.TransactionRR.CreateTransaction
{
    public class CreateTransactionResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public TransactionDto? Transaction { get; set; }
    }
}
