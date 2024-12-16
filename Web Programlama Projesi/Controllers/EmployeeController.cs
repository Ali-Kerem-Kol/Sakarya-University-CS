using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Web_Programlama_Projesi.Data;

namespace Web_Programlama_Projesi.Controllers
{
    public class EmployeeController : Controller
    {
        private readonly KuaferContext _context;

        public EmployeeController(KuaferContext context)
        {
            _context = context;
        }

        // Çalışanlar sayfasını görüntülemek için
        public IActionResult Index()
        {
            // Employee'lerle birlikte User bilgilerini de alıyoruz
            var employees = _context.Employees
                .Include(e => e.User) // User tablosunu dahil et
                .ToList();

            // Kullanıcının giriş yapıp yapmadığını kontrol et ve ViewData'ya aktar
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            ViewData["IsLoggedIn"] = username != null; // true/false olarak aktar

            return View(employees);
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

    }
}
