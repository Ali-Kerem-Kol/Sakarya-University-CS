using Microsoft.AspNetCore.Mvc;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;
using System.Globalization;
using Microsoft.EntityFrameworkCore;

namespace Web_Programlama_Projesi.Controllers
{
    public class SalonController : Controller
    {
        private readonly KuaferContext _context;

        public SalonController(KuaferContext context)
        {
            _context = context;
        }

        // Salonun mevcut boş zaman dilimlerini döndürme
        // Tüm zaman dilimlerini döndürmek için filtreyi kaldırıyoruz
        public List<TimeSlot> GetAvailableTimeSlots(int salonId)
        {
            // Salon verisini al
            var salon = _context.Salons.FirstOrDefault(s => s.Id == salonId);
            if (salon == null) return new List<TimeSlot>();

            // Zaman dilimlerini Id'ye göre sırala
            var allSlots = _context.TimeSlots
                                   .Where(ts => ts.SalonId == salonId)
                                   .OrderBy(ts => ts.Id) // Id'ye göre sırala
                                   .ToList();

            return allSlots;
        }



        // Salonlar sayfasını görüntülemek için
        public IActionResult Index()
        {
            var salons = _context.Salons.ToList();
            var employees = _context.Employees.Include(e => e.User).ToList();

            var salonWithAvailableSlots = new List<dynamic>();
            foreach (var salon in salons)
            {
                var sortedSlots = _context.TimeSlots
                                          .Where(ts => ts.SalonId == salon.Id)
                                          .OrderBy(ts => ts.Id)
                                          .ToList();

                salonWithAvailableSlots.Add(new { Salon = salon, AvailableSlots = sortedSlots });
            }

            TempData["SalonWithAvailableSlots"] = salonWithAvailableSlots;
            TempData["Employees"] = employees;

            // Kullanıcının giriş yapıp yapmadığını kontrol et ve ViewData'ya aktar
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            ViewData["IsLoggedIn"] = username != null; // true/false olarak aktar

            return View();
        }


        // Randevu alabileceğiniz salon sayfası
        [HttpPost]
        public IActionResult BookAppointment(int timeSlotId, int employeeId)
        {
            var username = HttpContext.Session.GetString("Username");
            if (username == null)
            {
                return RedirectToAction("Login", "Home");
            }

            var user = _context.Users.FirstOrDefault(u => u.Username == username);
            if (user == null)
            {
                return RedirectToAction("Login", "Home");
            }

            var timeSlot = _context.TimeSlots.FirstOrDefault(ts => ts.Id == timeSlotId);
            if (timeSlot == null)
            {
                TempData["ErrorMessage"] = "Seçilen zaman dilimi mevcut değil.";
                return RedirectToAction("Index", "Salon");
            }

            if (!timeSlot.IsAvailable)
            {
                TempData["ErrorMessage"] = "Seçilen zaman dilimi dolu.";
                return RedirectToAction("Index", "Salon");
            }

            // Çalışanın başka bir randevusu var mı kontrol et
            var existingAppointment = _context.Appointments
                .Include(a => a.TimeSlot)
                .FirstOrDefault(a => a.EmployeeId == employeeId && a.TimeSlot.StartTime == timeSlot.StartTime);

            if (existingAppointment != null)
            {
                TempData["ErrorMessage"] = "Bu çalışan zaten bu zaman diliminde başka bir salonda randevulu.";
                return RedirectToAction("Index", "Salon");
            }

            // Randevu oluşturma
            var appointment = new Appointment
            {
                CustomerId = user.Id,
                EmployeeId = employeeId,
                TimeSlotId = timeSlotId,
                Price = timeSlot.Salon.AppointmentPrice,
                IsApproved = true,
            };

            // Randevuyu veritabanına kaydet
            _context.Appointments.Add(appointment);
            _context.SaveChanges();

            // Zaman dilimini dolu olarak işaretle
            timeSlot.IsAvailable = false;
            _context.SaveChanges();

            return RedirectToAction("Index", "Home");
        }

    }


}
