namespace BEMobile.Models.RequestResponse.Account.DetailAccount
{
    public class DetailAccountResponse
    {
        public bool Success { get; set; }
        public string Message { get; set; }

        public string AccountId { get; set; }
        public string UserId { get; set; }
        public string Balance { get; set; }
    }
}
