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
        // TimeSlotları Listeleme
        public IActionResult TimeSlotDashboard(int Id)
        {
            SessionControl();

            var salon = _context.Salons.Include(s => s.TimeSlots).FirstOrDefault(s => s.Id == Id);

            if (salon == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            ViewData["SalonId"] = Id;
            ViewData["SalonName"] = salon.Name;

            return View(salon.TimeSlots.ToList());
        }

        // TimeSlot Ekleme Sayfası
        public IActionResult CreateTimeSlot(int salonId)
        {
            SessionControl();
            var salon = _context.Salons.Include(s => s.TimeSlots).FirstOrDefault(s => s.Id == salonId);
            ViewData["SalonId"] = salonId;
            ViewData["SalonName"] = salon.Name; // sonradan eklendi

            //return View();
            return View(new TimeSlotDto());
        }

        [HttpPost]
        public IActionResult CreateTimeSlot(int salonId, TimeSlotDto timeSlotDto)
        {
            SessionControl();

            var salon = _context.Salons.Include(s => s.TimeSlots).FirstOrDefault(s => s.Id == salonId);
            //var salons = _context.Salons.Include(s => s.TimeSlots).ToList();

            /*
            var salon = _context.Salons.Where(s => s.Id == salonId).Select(s => new
            {
                s.Id,
                s.Name,
                s.TimeSlots
            }).FirstOrDefault();
            */

            if (salon == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            timeSlotDto.SalonId = salon.Id;
            //string isim = timeSlotDto.SalonName;
            timeSlotDto.IsAvailable = true;
            string start = timeSlotDto.StartTime;
            string end = timeSlotDto.EndTime;

            if (!ModelState.IsValid)
            {
                ViewData["SalonId"] = salonId;
                return View(timeSlotDto);
            }

            TimeSlot newTimeSlot = new TimeSlot
            {
                StartTime = timeSlotDto.StartTime,
                EndTime = timeSlotDto.EndTime,
                IsAvailable = timeSlotDto.IsAvailable,
                SalonId = salonId
            };

            _context.TimeSlots.Add(newTimeSlot);
            _context.SaveChanges();

            return RedirectToAction("TimeSlotDashboard", new { salonId });
        }

        // TimeSlot Düzenleme
        public IActionResult EditTimeSlot(int id)
        {
            SessionControl();

            var timeSlot = _context.TimeSlots.FirstOrDefault(ts => ts.Id == id);

            if (timeSlot == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            var timeSlotDto = new TimeSlotDto
            {
                StartTime = timeSlot.StartTime,
                EndTime = timeSlot.EndTime,
                IsAvailable = timeSlot.IsAvailable
            };

            ViewData["TimeSlotId"] = id;
            ViewData["SalonId"] = timeSlot.SalonId;

            return View(timeSlotDto);
        }

        [HttpPost]
        public IActionResult EditTimeSlot(int id, TimeSlotDto timeSlotDto)
        {
            SessionControl();

            var timeSlot = _context.TimeSlots.FirstOrDefault(ts => ts.Id == id);

            if (timeSlot == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            if (!ModelState.IsValid)
            {
                ViewData["TimeSlotId"] = id;
                ViewData["SalonId"] = timeSlot.SalonId;
                return View(timeSlotDto);
            }

            timeSlot.StartTime = timeSlotDto.StartTime;
            timeSlot.EndTime = timeSlotDto.EndTime;
            timeSlot.IsAvailable = timeSlotDto.IsAvailable;

            _context.SaveChanges();

            return RedirectToAction("TimeSlotDashboard", new { salonId = timeSlot.SalonId });
        }

        // TimeSlot Silme
        public IActionResult DeleteTimeSlot(int id)
        {
            SessionControl();

            var timeSlot = _context.TimeSlots.FirstOrDefault(ts => ts.Id == id);

            if (timeSlot == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            int salonId = timeSlot.SalonId;

            _context.TimeSlots.Remove(timeSlot);
            _context.SaveChanges();

            return RedirectToAction("TimeSlotDashboard", new { salonId });
        }
        //==========================TimeSlot====================================

        //==========================Appointment====================================
        public IActionResult AppointmentDashboard()
        {
            SessionControl();
            var appointments = _context.Appointments
                                       .Include(a => a.Customer)  // Customer'ı dahil et
                                       .Include(a => a.Employee)  // Employee'ı dahil et
                                       .ThenInclude(e => e.User)  // Employee'ın User'ını dahil et
                                       .ToList();
            ViewData["Appointments"] = appointments;
            return View(appointments);
        }

        public IActionResult EditAppointment(int id)
        {
            SessionControl();
            /*
            var appointment = _context.Appointments
                                      .Include(a => a.TimeSlot)
                                      .Include(a => a.Customer)
                                      .Include(a => a.Employee)
                                      .FirstOrDefault(a => a.Id == id);
            */

            var appointment = _context.Appointments.Find(id);

            if (appointment == null)
            {
                return RedirectToAction("AppointmentDashboard", "Admin");
            }

            var appointmentDto = new AppointmentDto()
            {
                TimeSlotId = appointment.TimeSlotId,
                CustomerId = appointment.CustomerId,
                EmployeeId = appointment.EmployeeId,
                Price = appointment.Price,
                IsApproved = appointment.IsApproved
            };

            ViewData["TimeSlots"] = _context.TimeSlots.ToList();
            ViewData["Customers"] = _context.Users.ToList();
            ViewData["Employees"] = _context.Employees.ToList();
            ViewData["AppointmentId"] = appointment.Id;

            return View(appointmentDto);
        }

        [HttpPost]
        public IActionResult EditAppointment(int id, AppointmentDto appointmentDto)
        {
            SessionControl();

            var appointment = _context.Appointments.Find(id);

            if (appointment == null)
            {
                return RedirectToAction("AppointmentDashboard", "Admin");
            }

            if (!ModelState.IsValid)
            {
                ViewData["TimeSlots"] = _context.TimeSlots.ToList();
                ViewData["Customers"] = _context.Users.ToList();
                ViewData["Employees"] = _context.Employees.ToList();
                return View(appointmentDto);
            }

            appointment.TimeSlotId = appointmentDto.TimeSlotId;
            appointment.CustomerId = appointmentDto.CustomerId;
            appointment.EmployeeId = appointmentDto.EmployeeId;
            appointment.Price = appointmentDto.Price;
            appointment.IsApproved = appointmentDto.IsApproved;

            _context.SaveChanges();

            return RedirectToAction("AppointmentDashboard", "Admin");
        }


        public IActionResult DeleteAppointment(int id)
        {
            SessionControl();

            var appointment = _context.Appointments
                .Include(a => a.TimeSlot) // TimeSlot'u yükle
                .FirstOrDefault(a => a.Id == id);

            if (appointment != null)
            {
                // Randevuyu sil
                _context.Appointments.Remove(appointment);

                // Zaman dilimini yeniden uygun hale getir
                appointment.TimeSlot.IsAvailable = true;

                _context.SaveChanges();
            }

            return RedirectToAction("AppointmentDashboard","Admin");
        }

        //==========================Appointment====================================

    }


}
