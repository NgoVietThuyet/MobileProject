using DocumentFormat.OpenXml.Presentation;
using Microsoft.Extensions.Configuration;
using Neo4j.Driver;
using System;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Threading.Tasks;
using BEMobile.Models.DTOs;

namespace BEMobile.Connectors
{
    public interface INeo4jConnector : IDisposable
    {
        Task<IResultCursor> ExecuteWriteAsync(string query, object parameters = null);
        Task<List<IRecord>> ExecuteReadAsync(string query, object parameters = null);
        Task<IResultCursor> AddTransactionAsync(TransactionDto data);
        Task<IResultCursor> UpdateTransactionAsync(TransactionDto data);
        Task<IResultCursor> DeleteTransactionAsync(TransactionDto data);

    }

    public class Neo4jConnector : INeo4jConnector
    {
        private readonly IDriver _driver;

        public Neo4jConnector(IConfiguration configuration)
        {
            var neo4jConfig = configuration.GetSection("Neo4j");
            var uri = neo4jConfig["Uri"];
            var user = neo4jConfig["Username"];
            var password = neo4jConfig["Password"];

            _driver = GraphDatabase.Driver(uri, AuthTokens.Basic(user, password));
        }

        public async Task<IResultCursor> ExecuteWriteAsync(string query, object parameters = null)
        {
            await using var session = _driver.AsyncSession();
            return await session.RunAsync(query, parameters);
        }


        public async Task<List<IRecord>> ExecuteReadAsync(string query, object parameters = null)
        {
            await using var session = _driver.AsyncSession();
            return await session.ExecuteReadAsync(async tx =>
            {
                var cursor = await tx.RunAsync(query, parameters);
                return await cursor.ToListAsync();
            });
        }

        public async Task<IResultCursor> AddTransactionAsync(TransactionDto data)
        {

            string createdIsoDate = DateTime.ParseExact(data.CreatedDate, "dd/MM/yyyy HH:mm:ss", null).ToString("yyyy-MM-dd");
            string updatedIsoDate = DateTime.ParseExact(data.UpdatedDate, "dd/MM/yyyy HH:mm:ss", null).ToString("yyyy-MM-dd");
            string tranId = "GD" + data.TransactionId;
            string cateId = "DM" + data.CategoryId;

            var parameters = new
            {
                userId = data.UserId,
                transactionId = tranId,
                type = data.Type,
                amount = data.Amount,
                note = data.Note,
                createdDate = createdIsoDate,
                updatedDate = updatedIsoDate,
                categoryId = cateId
            };

            var query = @"
        MATCH (user:`Mã người dùng` {name: $userId}) 
        MATCH (category:`Mã danh mục` {name: $categoryId})
        CREATE (t:Transaction {name: $transactionId})

        MERGE (typeNode:`Loại giao dịch' {name: $type})
        
        CREATE (amountNode:`Số tiền` {name: $amount})
        CREATE (noteNode:`Ghi chú` {name: $note})
        CREATE (createDateNode:`Ngày tạo` {name: $createdDate}) 
        CREATE (updateDateNode:`Ngày cập nhật` {name: $updatedDate}) 

        CREATE (user)-[:`CÓ_GIAO_DỊCH`]->(t) 
        CREATE (t)-[:`LÀ_LOẠI`]->(typeNode)
        CREATE (t)-[:`CÓ_SỐ_TIỀN`]->(amountNode)
        CREATE (t)-[:`GHI_CHÚ`]->(noteNode)
        CREATE (t)-[:`CÓ_NGÀY_TẠO`]->(createDateNode)
        CREATE (t)-[:`CÓ_NGÀY_CẬP_NHẬT`]->(updateDateNode)
        CREATE (t)-[:`CÓ_DANH_MỤC`]->(category)
        
        RETURN t, user, category, amountNode, noteNode
    "";
    ";

            return await ExecuteWriteAsync(query, parameters);
        }

        public async Task<IResultCursor> UpdateTransactionAsync(TransactionDto data)
        {
            // Chuyển đổi ngày tháng sang định dạng ISO (yyyy-MM-dd)
            string updatedIsoDate = DateTime.ParseExact(data.UpdatedDate, "dd/MM/yyyy HH:mm:ss", null).ToString("yyyy-MM-dd");
            string tranId = "GD" + data.TransactionId;
            string cateId = "DM" + data.CategoryId;

            var parameters = new
            {
                transactionId = tranId,
                type = data.Type,
                amount = data.Amount,
                note = data.Note,
                updatedDate = updatedIsoDate,
                categoryId = cateId
            };

            var query = @"
        MATCH (t:`Mã giao dịch` {name: $transactionId})
        MATCH (t)-[:CÓ_SỐ_TIỀN]->(amountNode:`Số tiền`)
        MATCH (t)-[:GHI_CHÚ]->(noteNode:`Ghi chú`)
        MATCH (t)-[:CÓ_NGÀY_CẬP_NHẬT]->(updateDateNode:`Ngày cập nhật`)
        
        MATCH (user:`Mã người dùng`)-[:CÓ_GIAO_DỊCH]->(t)
        MATCH (t)-[oldCatRel:CÓ_DANH_MỤC]->(oldCategory)
        
        MERGE (newTypeNode:`Loại giao dịch` {name: $type})
        WITH t, amountNode, noteNode, updateDateNode, oldCategory, oldCatRel, user, newTypeNode
        OPTIONAL MATCH (t)-[oldTypeRel:LÀ_LOẠI]->(oldTypeNode)
        DELETE oldTypeRel
        CREATE (t)-[:LÀ_LOẠI]->(newTypeNode)

        SET amountNode.value = $amount
        SET noteNode.content = $note
        SET updateDateNode.date = $updatedDate 

        MATCH (newCategory:`Mã danh mục` {name: $categoryId})
        WHERE newCategory <> oldCategory
        DELETE oldCatRel

        RETURN t, user, newCategory, amountNode, noteNode
    ";

            return await ExecuteWriteAsync(query, parameters);
        }

        public async Task<IResultCursor> DeleteTransactionAsync(TransactionDto data)
        {
            string tranId = "GD" + data.TransactionId;

            var parameters = new
            {
                transactionId = tranId,
                userId = data.UserId
            };

            var query = @"
        MATCH (t:`Mã giao dịch` {name: $transactionId})
        
        OPTIONAL MATCH (t)-[:CÓ_SỐ_TIỀN]->(amountNode:`Số tiền`)
        OPTIONAL MATCH (user:`Mã người dùng` {name: $userId})-[:CÓ_GIAO_DỊCH]->(t)
        DETACH DELETE t
        RETURN user, amountNode
    ";

            return await ExecuteWriteAsync(query, parameters);
        }
        public void Dispose()
        {
            _driver?.Dispose();
        }
    }
}