using Microsoft.EntityFrameworkCore;
using Web_Programlama_Projesi.Data;

var builder = WebApplication.CreateBuilder(args);

// Veritabaný baðlantý dizesini doðru þekilde alýyoruz
builder.Services.AddDbContext<KuaforContext>(options =>
    options.UseSqlServer(builder.Configuration.GetConnectionString("DefaultConnection")));

// MVC'yi ekle
builder.Services.AddControllersWithViews();

//----------------------------------------------
// Authentication ve Authorization ekleme
builder.Services.AddAuthentication("CookieAuth")
    .AddCookie("CookieAuth", options =>
    {
        options.LoginPath = "/Account/Login"; // Kimlik doðrulama gerektiren sayfalar için yönlendirme
        options.AccessDeniedPath = "/Account/AccessDenied"; // Yetkisiz eriþimler için yönlendirme
    });

builder.Services.AddAuthorization(options =>
{
    options.AddPolicy("Admin", policy => policy.RequireRole("Admin"));
    options.AddPolicy("Employee", policy => policy.RequireRole("Employee"));
    options.AddPolicy("Customer", policy => policy.RequireRole("Customer"));
});

builder.Services.AddControllersWithViews();
//----------------------------------------------


var app = builder.Build();

//----------------------------------------------
// Middleware ekleme
app.UseAuthentication(); // Authentication mekanizmasýný aktif hale getirir
app.UseAuthorization();  // Authorization mekanizmasýný aktif hale getirir
//----------------------------------------------

if (!app.Environment.IsDevelopment())
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

//app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.Run();
