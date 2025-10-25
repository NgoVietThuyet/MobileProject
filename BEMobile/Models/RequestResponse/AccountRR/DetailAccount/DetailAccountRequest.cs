using System.ComponentModel.DataAnnotations;

namespace BEMobile.Models.RequestResponse.AccountRR.DetailAccount
{
    // GET /api/accounts/{id}
    public class DetailAccountRequest
    {
        [Required(ErrorMessage = "UserId là bắt buộc")]
        public string UserId { get; set; }
    }
}
