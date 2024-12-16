using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Controllers
{



    public class UserController : Controller
    {
        private readonly KuaferContext _context;

        public UserController(KuaferContext context)
        {
            _context = context;
        }

        public void SessionInfos()
        {
            // Kullanıcının giriş yapıp yapmadığını kontrol et ve ViewData'ya aktar
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            var id = HttpContext.Session.GetInt32("Id");
            ViewData["Id"] = id;

            ViewData["IsLoggedIn"] = username != null; // true/false olarak aktar
        }

        // Kullanıcı Paneli
        public IActionResult Dashboard()
        {
            SessionInfos();

            var currentUserId = HttpContext.Session.GetInt32("Id");
            if (currentUserId == null) return RedirectToAction("Login", "Home");

            // Kullanıcının randevularını al
            var appointments = _context.Appointments
                .Include(a => a.TimeSlot)
                .Where(a => a.CustomerId == currentUserId)
                .ToList();

            ViewData["Appointments"] = appointments;

            return View();
        }

        [HttpPost]
        public IActionResult UpdateUser(string Username, string Password)
        {
            SessionInfos();

            var currentUserId = HttpContext.Session.GetInt32("Id"); // Kullanıcı ID'sini session'dan al
            if (currentUserId == null) return RedirectToAction("Login", "Home");

            var user = _context.Users.Find(currentUserId);
            if (user != null)
            {
                // Şifre boş geçilemez
                if (string.IsNullOrEmpty(Password))
                {
                    // Şifre boş girilmişse kullanıcıya hata mesajı gösteriyoruz
                    ViewData["PasswordErrorMessage"] = "Şifre boş bırakılamaz.";
                    ViewData["Username"] = user.Username; // Kullanıcı adını da forma geri gönderiyoruz
                    return View("Dashboard"); // Hata mesajı ile tekrar Dashboard sayfasını yükle
                }

                // Şifreyi değiştirmek
                user.Password = Password; // Şifreyi doğrudan değiştiriyoruz

                _context.SaveChanges();
            }

            return RedirectToAction("Dashboard");
        }






        [HttpPost]
        public IActionResult DeleteAppointment(int AppointmentId)
        {
            SessionInfos();

            var appointment = _context.Appointments
                .Include(a => a.TimeSlot) // TimeSlot'u yükle
                .FirstOrDefault(a => a.Id == AppointmentId);

            if (appointment != null)
            {
                // Randevuyu sil
                _context.Appointments.Remove(appointment);

                // Zaman dilimini yeniden uygun hale getir
                appointment.TimeSlot.IsAvailable = true;

                _context.SaveChanges();
            }

            return RedirectToAction("Dashboard");
        }




    }

}
