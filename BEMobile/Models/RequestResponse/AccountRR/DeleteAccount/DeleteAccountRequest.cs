using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.AccountRR.DeleteAccount
{
    // Dùng cho DELETE /api/accounts/{id}?userId=...
    public class DeleteAccountRequest
    {
        [Required(ErrorMessage = "AccountId là bắt buộc")]
        public string AccountId { get; set; }

        // có thể dùng để xác thực chủ sở hữu
        [Required(ErrorMessage = "UserId là bắt buộc")]
        public string UserId { get; set; }
    }
}
