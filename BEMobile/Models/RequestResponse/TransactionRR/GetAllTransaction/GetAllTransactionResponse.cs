using BEMobile.Models.DTOs;
namespace BEMobile.Models.RequestResponse.Transaction.GetAllTransaction;

public class GetAllTransactionResponse
{
    public bool Success { get; set; }
    public string Message { get; set; } = string.Empty;
    public IEnumerable<TransactionDto>? Transactions { get; set; }
}
