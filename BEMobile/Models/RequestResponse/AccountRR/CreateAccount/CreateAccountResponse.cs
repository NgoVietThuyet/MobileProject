using BEMobile.Models.DTOs;
using BEMobile.Data.Entities;
namespace BEMobile.Models.RequestResponse.AccountRR.CreateAccount
{
    public class CreateAccountResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public Account? Account { get; set; }
    }
}
