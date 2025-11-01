using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using BEMobile.Data.Entities;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

public class JwtSettings
{
    public string SecretKey { get; set; } = "";
    public string Issuer { get; set; } = "";
    public string Audience { get; set; } = "";
    public int AccessTokenExpireMinutes { get; set; } = 60;
    public int RefreshTokenExpireDays { get; set; } = 30;
}

public interface IJwtService
{
    string GenerateAccessToken(User user);
    string GenerateRefreshToken();
}

public class JwtService : IJwtService
{
    private readonly JwtSettings _settings;
    public JwtService(IOptions<JwtSettings> settings)
    {
        _settings = settings.Value;
    }

    public string GenerateAccessToken(User user)
    {
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(_settings.SecretKey));
        var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha256);

        var claims = new[]
        {
            new Claim("UserId", user.UserId),
            new Claim("Email", user.Email ?? ""),
            new Claim("PhoneNumber", user.PhoneNumber ?? ""),
            new Claim("Name", user.Name ?? "")
        };

        var token = new JwtSecurityToken(
            issuer: _settings.Issuer,
            audience: _settings.Audience,
            claims: claims,
            expires: DateTime.UtcNow.AddMinutes(_settings.AccessTokenExpireMinutes),
            signingCredentials: creds
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }

    public string GenerateRefreshToken() => Guid.NewGuid().ToString("N");
}
