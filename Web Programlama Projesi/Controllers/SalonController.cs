using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Globalization;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Controllers
{
    public class SalonController : Controller
    {
        private readonly KuaferContext _context;

        public SalonController(KuaferContext context)
        {
            _context = context;
        }

        // Zaman dilimlerini hesaplayan fonksiyon
        public List<DateTime> GetAvailableTimeSlots(string workingHours, int intervalMinutes)
        {
            var times = new List<DateTime>();

            // Çalışma saatlerini alıyoruz (örneğin: "09:00-17:00")
            var startEndTimes = workingHours.Split('-');
            var startTime = DateTime.ParseExact(startEndTimes[0], "HH:mm", CultureInfo.InvariantCulture);
            var endTime = DateTime.ParseExact(startEndTimes[1], "HH:mm", CultureInfo.InvariantCulture);

            // Çalışma saatleri boyunca periyotlarla zaman dilimleri oluşturuyoruz
            while (startTime < endTime)
            {
                times.Add(startTime);  // Mevcut saat dilimini listeye ekle
                startTime = startTime.AddMinutes(intervalMinutes);  // Periyot kadar ileriye git
            }

            return times;
        }

        public List<DateTime> GetAvailableSlotsForSalon(int salonId)
        {
            var salon = _context.Salons.FirstOrDefault(s => s.Id == salonId);
            if (salon == null)
            {
                return new List<DateTime>(); // Salon bulunamadı
            }

            // Salonun çalışma saatlerini ve periyodu kullanarak zaman dilimlerini alıyoruz
            var allSlots = GetAvailableTimeSlots(salon.WorkingHours, salon.Interval);

            // Randevuları kontrol et ve dolu olan saatleri çıkar
            var bookedSlots = _context.Appointments
                .Where(a => a.SalonId == salonId)
                .Select(a => a.Date)
                .ToList();

            // Dolu olan saatleri zaman dilimlerinden çıkarıyoruz
            var availableSlots = allSlots.Where(slot => !bookedSlots.Contains(slot)).ToList();

            return availableSlots;
        }

        // Salonlar sayfasını görüntülemek için
        public IActionResult Index()
        {
            var salons = _context.Salons.Include(s => s.Employees).ToList();  // Salonlar ve çalışanlar dahil

            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            // Salonların çalışanları boş mu kontrol et
            foreach (var salon in salons)
            {
                if (salon.Employees == null)
                {
                    salon.Employees = new List<Employee>(); // Çalışanlar null ise boş bir liste oluştur
                }
            }

            ViewData["SalonWithAvailableSlots"] = salons;

            return View(salons);
        }

        // Randevu alabileceğiniz salon sayfası
        [HttpPost]
        public IActionResult BookAppointment(int salonId, string slotTime, int employeeId)
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

            var salon = _context.Salons.Include(s => s.Employees).FirstOrDefault(s => s.Id == salonId);
            if (salon == null)
            {
                return NotFound();
            }

            if (!salon.Employees.Any())  // Eğer salonun çalışanı yoksa
            {
                TempData["ErrorMessage"] = "Bu salonda çalışan bulunmamaktadır.";
                return RedirectToAction("Index", "Salon");
            }

            var employee = salon.Employees.FirstOrDefault(e => e.Id == employeeId);
            if (employee == null)
            {
                TempData["ErrorMessage"] = "Çalışan bulunamadı.";
                return RedirectToAction("Index", "Salon");
            }

            var appointment = new Appointment
            {
                CustomerId = user.Id,
                EmployeeId = employee.Id,
                SalonId = salonId,
                Date = DateTime.Parse(slotTime),  // Seçilen saati burada kullanıyoruz
                Duration = salon.Interval,
                Price = 100.00m,
                IsApproved = true,  // Onaylı randevu
            };

            _context.Appointments.Add(appointment);
            _context.SaveChanges();

            return RedirectToAction("Index", "Home");
        }
    }
}
