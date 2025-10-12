using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Neo4j.Driver;
using System;
using System.Threading.Tasks;
using Microsoft.Extensions.Configuration;

namespace BEMobile.Connectors
{
    public interface INeo4jConnector : IDisposable
    {
        Task<IResultCursor> ExecuteWriteAsync(string query, object parameters = null);
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

        public void Dispose()
        {
            _driver?.Dispose();
        }
    }
}