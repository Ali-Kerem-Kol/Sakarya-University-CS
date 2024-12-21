using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;
using Web_Programlama_Projesi.Security;

namespace Web_Programlama_Projesi.Controllers
{
    public class EmployeeController : Controller
    {
        private readonly KuaferContext _context;
        private readonly AuthorizeHelper _authorizeHelper;

        public EmployeeController(KuaferContext context, IHttpContextAccessor httpContextAccessor)
        {
            _context = context;
            _authorizeHelper = new AuthorizeHelper(httpContextAccessor.HttpContext, context);
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

        // Çalışan Dashboard Sayfasını Yükle
        public IActionResult Dashboard()
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Employee");
            if (result != null) return result; // Eğer hata varsa, döndür

            var currentUserId = HttpContext.Session.GetInt32("Id");
            if (currentUserId == null) return RedirectToAction("Login", "Home");

            // Kullanıcının rolünü kontrol et
            var employee = _context.Employees
                .Include(e => e.User) // User tablosu ile birleştir
                .Include(e => e.Appointments)
                .ThenInclude(a => a.TimeSlot)
                .FirstOrDefault(e => e.UserId == currentUserId);

            if (employee == null || employee.User.Role != "Employee")
            {
                return Forbid(); // Yetkili değilse erişimi engelle
            }
            ViewData["Username"] = employee.User.Username;
            ViewData["Specialization"] = employee.Expertise;

            // HashSet'ten List'e dönüştür
            var appointmentsList = employee.Appointments?.ToList() ?? new List<Appointment>();

            return View(appointmentsList);
        }

        // Çalışanın Bilgilerini Güncelle
        [HttpPost]
        public IActionResult UpdateEmployee(string Password, string Expertise)
        {

            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Employee");
            if (result != null) return result; // Eğer hata varsa, döndür

            var currentUserId = HttpContext.Session.GetInt32("Id");
            if (currentUserId == null) return RedirectToAction("Login", "Home");

            // User ve Employee bilgilerini güncelle
            var employee = _context.Employees
                .Include(e => e.User)
                .FirstOrDefault(e => e.UserId == currentUserId);

            if (employee != null)
            {
                if (string.IsNullOrEmpty(Password) || string.IsNullOrEmpty(Expertise))
                {
                    ViewData["PasswordErrorMessage"] = "Şifre ve Uzmanlık Alanı boş bırakılamaz.";
                    ViewData["Username"] = employee.User.Username;
                    ViewData["Specialization"] = employee.Expertise;
                    return View("EmployeeDashboard", employee.Appointments);
                }

                // Şifre ve uzmanlık alanını güncelle
                employee.User.Password = Password; // User tablosundaki şifre
                employee.Expertise = Expertise; // Employee tablosundaki uzmanlık alanı

                _context.SaveChanges();
            }

            return RedirectToAction("Dashboard");
        }

        // Randevuyu Tamamla
        [HttpPost]
        public IActionResult CompleteAppointment(int AppointmentId)
        {

            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Employee");
            if (result != null) return result; // Eğer hata varsa, döndür

            var currentUserId = HttpContext.Session.GetInt32("Id");
            if (currentUserId == null) return RedirectToAction("Login", "Home");

            // Kullanıcıya ait Employee kaydını bul
            var employee = _context.Employees
                .Include(e => e.Appointments)
                .ThenInclude(a => a.TimeSlot)
                .FirstOrDefault(e => e.UserId == currentUserId);

            if (employee == null) return Forbid();

            var appointment = employee.Appointments?.FirstOrDefault(a => a.Id == AppointmentId);

            if (appointment != null)
            {
                // TimeSlot'u boş hale getir
                appointment.TimeSlot.IsAvailable = true;

                // Randevuyu kaldır
                _context.Appointments.Remove(appointment);
                _context.SaveChanges();
            }

            return RedirectToAction("Dashboard");
        }



    }
}
