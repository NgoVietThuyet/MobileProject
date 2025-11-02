using BEMobile.Models.RequestResponse.TransactionRR.CreateTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.DeleteTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.GetAllTransaction;
using BEMobile.Models.RequestResponse.TransactionRR.UpdateTransaction;
using BEMobile.Models.RequestResponse.TransactionRR;
using BEMobile.Services;
using Microsoft.AspNetCore.Mvc;

namespace BEMobile.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class TransactionsController : ControllerBase
    {
        private readonly ITransactionService _service;

        public TransactionsController(ITransactionService service)
        {
            _service = service;
        }

        // GET /api/transactions/getall?userId={userId}
        [HttpGet("GetAll")]
        [ProducesResponseType(typeof(GetAllTransactionResponse), 200)]
        public async Task<IActionResult> GetAll([FromQuery] string userId)
        {
            if (string.IsNullOrEmpty(userId))
                return BadRequest(new GetAllTransactionResponse
                {
                    Success = false,
                    Message = "UserId là bắt buộc"
                });

            var transactions = await _service.GetAllTransactionsAsync(userId);

            return Ok(new GetAllTransactionResponse
            {
                Success = true,
                Message = "Lấy danh sách giao dịch thành công",
                Transactions = transactions
            });
        }


        // POST /api/transactions/create
        [HttpPost("Create")]
        [ProducesResponseType(typeof(CreateTransactionResponse), 200)]
        public async Task<IActionResult> Create([FromBody] CreateTransactionRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _service.CreateTransactionAsync(request);
            return Ok(result);
        }

        // PUT /api/transactions/update
        [HttpPut("Update")]
        [ProducesResponseType(typeof(UpdateTransactionResponse), 200)]
        public async Task<IActionResult> Update([FromBody] UpdateTransactionRequest request)
        {
            if (!ModelState.IsValid)
                return BadRequest(ModelState);

            var result = await _service.UpdateTransactionAsync(request);
            return Ok(result);
        }

        // DELETE /api/transactions/delete
        [HttpDelete("Delete")]
        [ProducesResponseType(typeof(DeleteTransactionResponse), 200)]
        public async Task<IActionResult> Delete([FromBody] DeleteTransactionRequest request)
        {
            if (string.IsNullOrEmpty(request.TransactionId))
                return BadRequest(new DeleteTransactionResponse
                {
                    Success = false,
                    Message = "TransactionId là bắt buộc"
                });

            var result = await _service.DeleteTransactionAsync(request);
            return Ok(result);
        }
    }
}
