namespace BEMobile.Models.RequestResponse.Account.CreateAccount
{
    public class CreateAccountResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }

        // Thông tin account vừa tạo
        public string AccountId { get; set; }
        public string UserId { get; set; }
        public string AccountName { get; set; }
        public string Type { get; set; }
        public decimal Balance { get; set; }
    }
}
