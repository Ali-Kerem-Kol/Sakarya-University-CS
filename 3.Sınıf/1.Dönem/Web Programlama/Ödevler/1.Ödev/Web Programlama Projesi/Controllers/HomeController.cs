using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.CodeAnalysis.Scripting;
using Microsoft.EntityFrameworkCore;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Controllers
{
    public class HomeController : Controller
    {

        private readonly KuaferContext _context;

        public HomeController(KuaferContext context)
        {
            _context = context;
        }


        // Ana sayfa
        public IActionResult Index()
        {
            // Kullanýcýnýn giriþ yapýp yapmadýðýný kontrol et
            var username = HttpContext.Session.GetString("Username");
            var role = HttpContext.Session.GetString("Role");

            // Giriþ yapmýþsa, bilgiyi ViewData'ya gönder
            if (username != null)
            {
                ViewData["Username"] = username;
                ViewData["Role"] = role;
            }
            else
            {
                ViewData["Username"] = null;
                ViewData["Role"] = null;
            }

            return View();
        }

        public IActionResult Privacy()
        {
            return View();
        }


        //----------------------------Register--------------------------------------------------------
        // Kullanýcý Kayýt Sayfasýna Yönlendiren GET Aksiyonu
        public IActionResult Register()
        {
            return View();
        }

        // Kullanýcý Kayýt Ýþlemi Gerçekleþtiren POST Aksiyonu
        [HttpPost]
        public async Task<IActionResult> Register(User user)
        {
            // Kullanýcý adý kontrolü
            if (_context.Users.Any(u => u.Username == user.Username))
            {
                ModelState.AddModelError("Username", "Bu kullanýcý adý zaten alýnmýþ.");
                return View(user);  // Hata varsa, formu tekrar gösteriyoruz
            }

            // Þifreyi ve diðer verileri veritabanýna kaydediyoruz
            if (ModelState.IsValid)
            {
                try
                {
                    _context.Users.Add(user);  // Yeni kullanýcýyý ekliyoruz
                    await _context.SaveChangesAsync();  // Veritabanýna kaydediyoruz
                    return RedirectToAction("Index", "Home");  // Ana sayfaya yönlendiriyoruz
                }
                catch (Exception ex)
                {
                    // Hata mesajý ekliyoruz
                    ModelState.AddModelError("", "Veritabanýna kaydetme iþlemi baþarýsýz oldu: " + ex.Message);
                }
            }

            // Eðer model geçerli deðilse, formu tekrar gösteriyoruz
            return View(user);
        }
        //----------------------------Register--------------------------------------------------------
        
        
        //----------------------------Login--------------------------------------------------------
        // Giriþ sayfasýna yönlendiren GET aksiyonu
        public IActionResult Login()
        {
            return View();
        }

        // Kullanýcý giriþ iþlemi
        [HttpPost]
        public async Task<IActionResult> Login(string username, string password)
        {
            var user = await _context.Users.FirstOrDefaultAsync(u => u.Username == username);

            // Kullanýcý kontrolü ve þifre doðrulama
            if (user == null || user.Password != password)
            {
                ModelState.AddModelError(string.Empty, "Geçersiz kullanýcý adý veya þifre.");
                return View();
            }

            if (user.IsActive == false)
            {
                ModelState.AddModelError(string.Empty, "Hesabýnýz Askýya Alýndý.");
                return View();
            }

            // Kullanýcý bilgilerini session'da saklýyoruz
            HttpContext.Session.SetString("Username", user.Username);
            HttpContext.Session.SetString("Role", user.Role); // Rolü sakla
            HttpContext.Session.SetInt32("Id", user.Id);

            // ViewData'ya aktarým
            ViewData["Username"] = user.Username;
            ViewData["Role"] = user.Role;
            ViewData["Id"] = user.Id;

            return RedirectToAction("Index","Home");

        }

        //----------------------------Login--------------------------------------------------------


        //----------------------------Logout--------------------------------------------------------
        // Çýkýþ metodu
        public IActionResult Logout()
        {
            // Session temizleme
            HttpContext.Session.Clear();

            // ViewData'yý sýfýrla
            ViewData["Username"] = null;
            ViewData["Role"] = null;

            // Ana sayfaya yönlendirme
            return RedirectToAction("Index");
        }
        //----------------------------Logout--------------------------------------------------------

    }

}
