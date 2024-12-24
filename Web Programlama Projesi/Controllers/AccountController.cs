using Microsoft.AspNetCore.Mvc;

namespace Web_Programlama_Projesi.Controllers
{
    public class AccountController : Controller
    {
        // Yetkisiz bir erişim olduğunda, kullanıcı bu sayfaya yönlendirilecek.
        public IActionResult AccessDenied()
        {
            return RedirectToAction("Index","Home");
        }
    }
}
