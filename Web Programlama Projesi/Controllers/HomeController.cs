using Microsoft.AspNetCore.Mvc;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Controllers
{
    public class HomeController : Controller
    {
        // Selamun aleykum beyler
        private readonly KuaferContext _context;

        public HomeController(KuaferContext context)
        {
            _context = context;
        }

        public IActionResult Index()
        {
            return View();
        }

        public IActionResult Privacy()
        {
            return View();
        }

    }
}
