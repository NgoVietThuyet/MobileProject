using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.AccountRR.DetailAccount
{
    // GET /api/accounts/{id}
    public class DetailAccountRequest
    {
        [Required(ErrorMessage = "AccountId là bắt buộc")]
        public string AccountId { get; set; }
        public string UserId { get; set; }
    }
}
