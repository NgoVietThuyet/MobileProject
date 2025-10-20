using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.Transaction.UpdateTransaction
{
    public class UpdateTransactionResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public TransactionDto? UpdatedTransaction { get; set; }
    }
}
