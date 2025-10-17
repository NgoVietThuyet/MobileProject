using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.Account.CreateAccount
{
    public class CreateAccountResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; } = string.Empty;
        public AccountDto? Account { get; set; }
    }
}
