using Microsoft.AspNetCore.Mvc;

namespace Web_Programlama_Projesi.Controllers
{
    public class AdminController : Controller
    {
        public IActionResult AdminDashboard()
        {
            // Admin için dashboard sayfasına yönlendirme yapılır
            return View();
        }

        // Adminin yapabileceği diğer işlemler burada olacak, örneğin salon ekleme vs.
    }

}
