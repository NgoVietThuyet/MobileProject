using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
//using BEMobile.Models.DTOs;

namespace BEMobile.KnowledgeGraph.Services
{
    public interface IKnowledgeGraphService
    {
        //Task CreateUserNodeAsync(UserDto user);
        //Task CreateTransactionAsync(TransactionDto transaction);
        // Hàm nhận một câu văn và trả về cấu trúc đồ thị được trích xuất
        Task<string> ExtractGraphFromTextAsync(string text);
    }
}
