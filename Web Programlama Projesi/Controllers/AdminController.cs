using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace Web_Programlama_Projesi.Controllers
{
    public class AdminController : Controller
    {

        public IActionResult Index()
        {
            // Kullanıcının giriş yapıp yapmadığını kontrol et ve ViewData'ya aktar
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            ViewData["IsLoggedIn"] = username != null; // true/false olarak aktar

            return View();
        }

        public IActionResult Dashboard()
        {
            // Kullanıcının giriş yapıp yapmadığını kontrol et ve ViewData'ya aktar
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            ViewData["IsLoggedIn"] = username != null; // true/false olarak aktar

            return View();
        }

        // Adminin yapabileceği diğer işlemler burada olacak, örneğin salon ekleme vs.
    }

}
