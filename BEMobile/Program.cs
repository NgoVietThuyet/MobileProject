
using BEMobile.Services;
using Microsoft.EntityFrameworkCore;
using BEMobile.Services;
using BEMobile;
using BEMobile.Connectors;




var builder = WebApplication.CreateBuilder(args);

// Đăng ký configuration
builder.Configuration.AddJsonFile("appsettings.json", optional: false, reloadOnChange: true);

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Add DbContext
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("Connection")));

// gemini
builder.Services.Configure<GeminiOptions>(builder.Configuration.GetSection("Gemini"));
builder.Services.AddHttpClient();
builder.Services.AddHttpClient("gemini");




builder.Services.AddScoped<IBudgetService, BudgetService>();
builder.Services.AddScoped<ICategoryService, CategoryService>();
builder.Services.AddScoped<ISavingGoalService, SavingGoalService>();


builder.Services.AddScoped<IAccountService, AccountService>();
builder.Services.AddScoped<ITransactionService, TransactionService>();
builder.Services.AddScoped<INotificationService, NotificationService>();
builder.Services.AddScoped<IReportService, ReportService>();


// gemini
builder.Services.Configure<GeminiOptions>(builder.Configuration.GetSection("Gemini"));
builder.Services.AddHttpClient();
builder.Services.AddHttpClient("gemini");

// Add Services
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IImageService, ImageService>();

// graph
builder.Services.AddScoped<IKnowledgeGraphService, KnowledgeGraphService>();
builder.Services.AddSingleton<INeo4jConnector, Neo4jConnector>();

builder.Services.AddHttpContextAccessor();
var app = builder.Build();

// Configure the HTTP request pipeline
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI(c =>
    {
        c.SwaggerEndpoint("/swagger/v1/swagger.json", "User Management API V1");
        c.RoutePrefix = string.Empty; // Set Swagger UI as homepage
    });
}

app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

// Tự động tạo database nếu chưa tồn tại (chỉ dùng cho development)
using (var scope = app.Services.CreateScope())
{
    var context = scope.ServiceProvider.GetRequiredService<AppDbContext>();
    context.Database.EnsureCreated();
}

app.Lifetime.ApplicationStarted.Register(() =>
{
    Task.Run(async () =>
    {
        try
        {
            using var scope = app.Services.CreateScope();
            var budgetService = scope.ServiceProvider.GetRequiredService<IBudgetService>();

            await budgetService.CheckAndCreateMonthlyBudgetsAsync();

            Console.ForegroundColor = ConsoleColor.Green;
            Console.WriteLine($" [{DateTime.Now:HH:mm:ss}] Budget check done on app start");
            Console.ResetColor();
        }
        catch (Exception ex)
        {
            Console.ForegroundColor = ConsoleColor.Red;
            Console.WriteLine($" [{DateTime.Now:HH:mm:ss}] Error while checking budget on startup: {ex.Message}");
            Console.ResetColor();
        }
    });
});


app.Run();