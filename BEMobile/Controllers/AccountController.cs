using BEMobile.Models.RequestResponse.Account.CreateAccount;
using BEMobile.Models.RequestResponse.Account.DeleteAccount;
using BEMobile.Models.RequestResponse.Account.DetailAccount;
using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;

namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class AccountsController : ControllerBase
    {
        private readonly IAccountService _svc;
        public AccountsController(IAccountService svc) => _svc = svc;

        // POST /api/accounts
        [HttpPost("CreateAccount")]
        public async Task<ActionResult<CreateAccountResponse>> Create([FromBody] CreateAccountRequest req)
        {
            var res = await _svc.CreateAccountAsync(req);
            return Ok(res);
        }

        // GET /api/accounts/{id}
        [HttpGet("GetAccountByUserId")]
        public async Task<ActionResult<DetailAccountResponse>> GetById(string id)
        {
            var res = await _svc.GetAccountByUserIdAsync(new DetailAccountRequest { UserId = id });
            if (!res.Success) return NotFound(res);
            return Ok(res);
        }

        // DELETE /api/accounts/{id}
        [HttpDelete("DeleteAccountById")]
        public async Task<ActionResult<DeleteAccountResponse>> Delete(string id, [FromQuery] string userId)
        {
            var res = await _svc.DeleteAccountAsync(new DeleteAccountRequest { AccountId = id, UserId = userId });
            if (!res.Success) return NotFound(res);
            return Ok(res);
        }
    }
}
