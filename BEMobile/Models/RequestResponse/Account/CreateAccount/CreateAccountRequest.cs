using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.Account.CreateAccount
{
    public class CreateAccountRequest
    {
        [Required(ErrorMessage = "UserId là bắt buộc")]
        public string UserId { get; set; }

        [Required, StringLength(100)]
        public string AccountName { get; set; }

        // cash | bank | ewallet (có thể đổi sang enum)
        [Required, RegularExpression("^(cash|bank|ewallet)$",
            ErrorMessage = "Type phải là cash, bank hoặc ewallet")]
        public string Type { get; set; }

        [Range(0, double.MaxValue, ErrorMessage = "Balance phải >= 0")]
        public decimal Balance { get; set; }
    }
}
