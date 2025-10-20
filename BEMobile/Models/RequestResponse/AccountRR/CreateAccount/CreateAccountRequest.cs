using System.ComponentModel.DataAnnotations;
using BEMobile.Models.DTOs;

namespace BEMobile.Models.RequestResponse.AccountRR.CreateAccount
{
    public class CreateAccountRequest
    {
        public AccountDto Account { get; set; }

    }
}
