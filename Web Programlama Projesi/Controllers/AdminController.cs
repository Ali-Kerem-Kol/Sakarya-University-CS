using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Controllers
{

    public class AdminController : Controller
    {
        private readonly KuaferContext _context;

        public AdminController(KuaferContext context)
        {
            _context = context;
        }

        private IActionResult SessionControl()
        {
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;
            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;
            ViewData["IsLoggedIn"] = username != null;

            var currentUserId = HttpContext.Session.GetInt32("Id");
            if (currentUserId == null) return RedirectToAction("Login", "Home");
            return null;
        }

        public IActionResult Index()
        {
            SessionControl();

            return View();
        }


        //==========================Salon====================================
        public IActionResult SalonDashboard()
        {
            SessionControl();
            var salons = _context.Salons.Include(s => s.TimeSlots).ToList();
            //var salons = _context.Salons.ToList();
            ViewData["Salons"] = salons;
            return View(salons);
        }

        public IActionResult CreateSalon()
        {
            SessionControl();
            return View();
        }

        [HttpPost]
        public IActionResult CreateSalon(SalonDto salonDto)
        {
            SessionControl();

            if (!ModelState.IsValid)
            {
                return View(salonDto);
            }

            Salon salon = new Salon()
            {
                Name = salonDto.Name,
                WorkingHours = salonDto.WorkingHours,
                AppointmentPrice = salonDto.AppointmentPrice,
            };

            _context.Salons.Add(salon);
            _context.SaveChanges();

            return RedirectToAction("SalonDashboard", "Admin");
        }

        public IActionResult EditSalon(int id)
        {
            SessionControl();

            var salon = _context.Salons.Find(id);

            if (salon == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            var salonDto = new SalonDto()
            {
                Name=salon.Name,
                WorkingHours = salon.WorkingHours,
                AppointmentPrice = salon.AppointmentPrice,
            };

            ViewData["SalonId"] = id;

            return View(salonDto);
        }

        [HttpPost]
        public IActionResult EditSalon(int id,SalonDto salonDto)
        {
            SessionControl();

            var salon = _context.Salons.Find(id);

            if (salon == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            if (!ModelState.IsValid)
            {
                ViewData["SalonId"] = id;
                return View(salonDto);
            }

            salon.Name = salonDto.Name;
            salon.WorkingHours = salonDto.WorkingHours;
            salon.AppointmentPrice = salonDto.AppointmentPrice;

            _context.SaveChanges();

            return RedirectToAction("SalonDashboard", "Admin");

        }

        public IActionResult DeleteSalon(int id)
        {
            SessionControl();

            var salon = _context.Salons.Find(id);

            if (salon == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            _context.Salons.Remove(salon);
            _context.SaveChanges();

            return RedirectToAction("SalonDashboard", "Admin");
        }
        //==========================Salon====================================


        //==========================TimeSlot====================================
        public IActionResult TimeSlot(int id)
        {
            // 'id' parametresi salonId'yi temsil ediyor.
            var salon = _context.Salons
                                .Include(s => s.TimeSlots) // TimeSlot'ları dahil et
                                .FirstOrDefault(s => s.Id == id);

            if (salon == null)
            {
                return NotFound("Salon bulunamadı!");
            }

            // View'a veri gönder
            ViewData["SalonName"] = salon.Name;
            return View(salon.TimeSlots); // TimeSlot listesini View'a gönder
        }


        //==========================TimeSlot====================================
    }


}
