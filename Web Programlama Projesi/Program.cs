using Microsoft.EntityFrameworkCore;
using Microsoft.AspNetCore.Authentication.Cookies;
using Web_Programlama_Projesi.Data;

var builder = WebApplication.CreateBuilder(args);

// IHttpContextAccessor'ý servis olarak ekleyin
builder.Services.AddHttpContextAccessor();

// PostgreSQL veritabaný baðlantýsý için DbContext yapýlandýrmasý
builder.Services.AddDbContext<KuaferContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// Session'ý yapýlandýrýyoruz
builder.Services.AddDistributedMemoryCache(); // Session için bellek tabanlý cache
builder.Services.AddSession(options =>
{
    options.IdleTimeout = TimeSpan.FromMinutes(30); // Oturum süresi (30 dakika)
    options.Cookie.HttpOnly = true; // Sadece HTTP üzerinden eriþilebilir
    options.Cookie.IsEssential = true; // Çerez gerekli
});

// Cookie tabanlý kimlik doðrulama yapýlandýrmasý
builder.Services.AddAuthentication(CookieAuthenticationDefaults.AuthenticationScheme)
    .AddCookie(options =>
    {
        options.LoginPath = "/Account/Login"; // Kullanýcý giriþ yapmamýþsa buraya yönlendirilecek
        options.AccessDeniedPath = "/Account/AccessDenied"; // Yetkisiz eriþim için yönlendirme
        options.ExpireTimeSpan = TimeSpan.FromMinutes(30); // Oturum süresi
        options.SlidingExpiration = true; // Oturum süresi her istekle yenilenecek
    });

// HttpClient ekliyoruz (gerekliyse)
builder.Services.AddHttpClient();

// Add services to the container.
builder.Services.AddControllersWithViews();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseDeveloperExceptionPage();
}
else
{
    app.UseExceptionHandler("/Home/Error");
    app.UseHsts();
}

// HTTPS yönlendirmesi ve statik dosyalar
app.UseHttpsRedirection();
app.UseStaticFiles();

// Kimlik doðrulama middleware'ini ekliyoruz
app.UseAuthentication(); // Kimlik doðrulama iþlemi

app.UseRouting();

// Session middleware'ini kullanýyoruz
app.UseSession();

// Authorization middleware'ini ekliyoruz
app.UseAuthorization();

// MVC route yapýlandýrmasý
app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.Run();
