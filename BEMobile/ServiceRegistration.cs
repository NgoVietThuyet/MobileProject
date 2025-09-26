using BEMobile.Services;
using Microsoft.EntityFrameworkCore;
using BEMobile.Services;

namespace BEMobile
{
    public static class ServiceRegistration
    {
        public static void AddUserManagementServices(this IServiceCollection services, IConfiguration configuration)
        {
            // Add DbContext
            services.AddDbContext<AppDbContext>(options =>
                options.UseSqlServer(configuration.GetConnectionString("Connection")));

            // Add Services
            services.AddScoped<IUserService, UserService>();
        }
    }
}