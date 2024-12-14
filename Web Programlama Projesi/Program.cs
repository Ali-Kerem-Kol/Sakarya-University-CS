using Microsoft.EntityFrameworkCore;
using Web_Programlama_Projesi.Data;

var builder = WebApplication.CreateBuilder(args);

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

app.UseHttpsRedirection();
app.UseStaticFiles();

app.UseRouting();

// Session middleware'ini kullanýyoruz
app.UseSession();

app.UseAuthorization();

app.MapControllerRoute(
    name: "default",
    pattern: "{controller=Home}/{action=Index}/{id?}");

app.Run();
