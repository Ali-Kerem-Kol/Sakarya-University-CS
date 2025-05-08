using Google;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.CodeAnalysis.Scripting;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Internal;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;
using Web_Programlama_Projesi.Security;

namespace Web_Programlama_Projesi.Controllers
{

    public class AdminController : Controller
    {
        private readonly KuaferContext _context;
        private readonly AuthorizeHelper _authorizeHelper;

        public AdminController(KuaferContext context, IHttpContextAccessor httpContextAccessor)
        {
            _context = context;
            _authorizeHelper = new AuthorizeHelper(httpContextAccessor.HttpContext, context);
        }

        private void SetUserInfoToViewData()
        {
            var username = HttpContext.Session.GetString("Username");
            ViewData["Username"] = username;

            var role = HttpContext.Session.GetString("Role");
            ViewData["Role"] = role;

            ViewData["IsLoggedIn"] = username != null;
        }

        public IActionResult Index()
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            return View();
        }

        [HttpPost]
        public IActionResult UpdateAdminCredentials(string Username, string Password)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            // Şifre en az 6 karakter olmalı
            if (string.IsNullOrWhiteSpace(Password) || Password.Length < 6)
            {
                ViewData["PasswordErrorMessage"] = "Şifre en az 6 karakter olmalıdır.";
                return View("Index");
            }

            // Admin kullanıcıyı bul
            var admin = _context.Users.FirstOrDefault(u => u.Role == "Admin");
            if (admin == null)
            {
                ViewData["PasswordErrorMessage"] = "Admin kullanıcı bulunamadı.";
                return View("Index");
            }

            // Kullanıcı adını güncelle
            if (!string.IsNullOrWhiteSpace(Username))
            {
                admin.Username = Username;
            }

            admin.Password = Password;

            _context.SaveChanges();

            ViewData["SuccessMessage"] = "Kullanıcı adı ve şifre başarıyla güncellendi.";
            return RedirectToAction("Index");
        }


        //==========================Salon====================================
        public IActionResult SalonDashboard()
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var salons = _context.Salons.Include(s => s.TimeSlots).ToList();
            ViewData["Salons"] = salons;
            return View(salons);
        }

        public IActionResult CreateSalon()
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            return View();
        }

        [HttpPost]
        public IActionResult CreateSalon(SalonDto salonDto)
        {

            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            if (!ModelState.IsValid)
            {
                return View(salonDto);
            }

            Salon salon = new Salon()
            {
                Name = salonDto.Name,
                WorkingHours = salonDto.WorkingHours,
                AppointmentPrice = salonDto.AppointmentPrice,
                Expertise = salonDto.Expertise
            };

            _context.Salons.Add(salon);
            _context.SaveChanges();

            return RedirectToAction("SalonDashboard", "Admin");
        }

        public IActionResult EditSalon(int id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

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
                Expertise = salon.Expertise
            };

            ViewData["SalonId"] = id;

            return View(salonDto);
        }

        [HttpPost]
        public IActionResult EditSalon(int id,SalonDto salonDto)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

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
            salon.Expertise = salonDto.Expertise;

            _context.SaveChanges();

            return RedirectToAction("SalonDashboard", "Admin");

        }

        public IActionResult DeleteSalon(int id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

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
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

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
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var salon = _context.Salons.Include(s => s.TimeSlots).FirstOrDefault(s => s.Id == salonId);
            ViewData["SalonId"] = salonId;
            ViewData["SalonName"] = salon.Name;

            return View(new TimeSlotDto());
        }

        [HttpPost]
        public IActionResult CreateTimeSlot(int salonId, TimeSlotDto timeSlotDto)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var salon = _context.Salons.Include(s => s.TimeSlots).FirstOrDefault(s => s.Id == salonId);

            if (salon == null)
            {
                return RedirectToAction("SalonDashboard", "Admin");
            }

            timeSlotDto.SalonId = salon.Id;
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
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var timeSlot = _context.TimeSlots.FirstOrDefault(ts => ts.Id == id);

            var appointmentWithCustomer = _context.Appointments
                .Include(a => a.Customer)
                .FirstOrDefault(a => a.TimeSlotId == id && a.Customer != null);



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
            ViewData["Customer"] = appointmentWithCustomer?.Customer;

            return View(timeSlotDto);
        }

        [HttpPost]
        public IActionResult EditTimeSlot(int id, TimeSlotDto timeSlotDto)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

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
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

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
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var appointments = _context.Appointments
                                       .Include(a => a.Customer)
                                       .Include(a => a.Employee)
                                       .ThenInclude(e => e.User)
                                       .ToList();
            ViewData["Appointments"] = appointments;
            return View(appointments);
        }



        public IActionResult DeleteAppointment(int id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var appointment = _context.Appointments
                .Include(a => a.TimeSlot)
                .FirstOrDefault(a => a.Id == id);

            if (appointment != null)
            {

                // Zaman dilimini yeniden uygun hale getir
                appointment.TimeSlot.IsAvailable = true;

                // Randevuyu sil
                _context.Appointments.Remove(appointment);

                _context.SaveChanges();
            }

            return RedirectToAction("AppointmentDashboard","Admin");
        }

        //==========================Appointment====================================


        //==========================Employee====================================
        public IActionResult EmployeeDashboard(int Id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            // Çalışanları, toplam kazanca göre azalan sırayla sıralıyoruz
            var employees = _context.Employees
                                   .Include(e => e.User)
                                   .Include(e => e.Appointments)
                                   .OrderByDescending(e => e.TotalEarnings)
                                   .ToList();

            return View(employees);
        }

        public IActionResult CreateEmployee()
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            return View();
        }

        [HttpPost]
        public IActionResult CreateEmployee(EmployeeDto employeeDto)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            if (!ModelState.IsValid)
            {
                return View(employeeDto);
            }

            // Kullanıcı adı kontrolü: Aynı kullanıcı adı var mı?
            var existingUser = _context.Users.FirstOrDefault(u => u.Username == employeeDto.Username);
            if (existingUser != null)
            {
                ModelState.AddModelError("User.Username", "Bu kullanıcı adı zaten kullanımda. Lütfen başka bir kullanıcı adı seçin.");
                return View(employeeDto);  // Kullanıcı adı mevcutsa geri döneriz.
            }

            // Önce User oluştur ve kaydet
            User user = new User()
            {
                Username = employeeDto.Username,
                Password = employeeDto.Password,
                Role = employeeDto.Role,
                IsActive = employeeDto.IsActive
            };

            _context.Users.Add(user);
            _context.SaveChanges(); // User kaydedildikten sonra Id'si oluşur

            // Sonra Employee oluştur ve User ile ilişkilendir
            Employee employee = new Employee()
            {
                UserId = user.Id, // Yeni oluşturulan User'ın Id'sini kullan
                User = user, // User nesnesini ilişkilendir
                Expertise = employeeDto.Expertise,
                IsActive = employeeDto.IsActive
            };

            _context.Employees.Add(employee);
            _context.SaveChanges(); // Employee kaydedilir

            return RedirectToAction("EmployeeDashboard", "Admin");
        }

        public IActionResult EditEmployee(int id) // EmployeeId
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var employee = _context.Employees
                .Include(e => e.User) // User tablosunu dahil ediyoruz
                .FirstOrDefault(e => e.Id == id); // Id'ye göre arama

            if (employee == null)
            {
                return RedirectToAction("EmployeeDashboard", "Admin");
            }

            var employeeDto = new EmployeeDto()
            {
                Username = employee.User.Username,
                Password = employee.User.Password,
                Role = employee.User.Role,
                Expertise = employee.Expertise,
                IsActive = employee.IsActive
            };

            ViewData["EmployeeId"] = id;

            return View(employeeDto);
        }

        [HttpPost]
        public IActionResult EditEmployee(int id, EmployeeDto employeeDto) // EmployeeId
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var employee = _context.Employees
                .Include(e => e.User) // User tablosunu dahil ediyoruz
                .FirstOrDefault(e => e.Id == id); // Id'ye göre arama

            if (employee == null)
            {
                return RedirectToAction("EmployeeDashboard", "Admin");
            }

            if (!ModelState.IsValid)
            {
                ViewData["EmployeeId"] = id;
                return View(employeeDto);
            }

            employee.User.Username = employeeDto.Username;
            employee.User.Password = employeeDto.Password;
            employee.User.Role = employeeDto.Role;
            employee.Expertise = employeeDto.Expertise;
            employee.IsActive = employeeDto.IsActive;

            _context.SaveChanges();

            return RedirectToAction("EmployeeDashboard", "Admin");

        }

        public IActionResult DeleteEmployee(int id) // EmployeeId
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var employee = _context.Employees
                .Include(e => e.User) // User tablosunu dahil ediyoruz
                .FirstOrDefault(e => e.Id == id); // Id'ye göre arama

            if (employee == null)
            {
                return RedirectToAction("EmployeeDashboard", "Admin");
            }

            User user = employee.User;
            _context.Users.Remove(user);
            _context.Employees.Remove(employee);
            _context.SaveChanges();

            return RedirectToAction("EmployeeDashboard", "Admin");
        }

        public IActionResult ResetEmployeeStats(int id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            // Çalışanı bul
            var employee = _context.Employees.Include(e => e.Appointments).FirstOrDefault(e => e.Id == id);

            if (employee != null)
            {
                // Çalışanın toplam randevularını ve kazancını sıfırla
                employee.TotalAppointments = 0;
                employee.TotalEarnings = 0;

                // Çalışanın verilerini güncelle
                _context.Update(employee);
                _context.SaveChanges();
            }

            return RedirectToAction("EmployeeDashboard");
        }

        //==========================Employee====================================


        //==========================User====================================

        public IActionResult UserDashboard()
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var users = _context.Users.ToList();
            ViewData["Users"] = users;
            return View(users);
        }

        public IActionResult DeleteUser(int id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var user = _context.Users.Include(u => u.Appointments)
                                      .ThenInclude(a => a.TimeSlot)
                                      .FirstOrDefault(u => u.Id == id);

            if (user == null)
            {
                return RedirectToAction("UserDashboard", "Admin");
            }

            // Kullanıcının randevuları varsa, onları sil
            if (user.Appointments != null && user.Appointments.Any())
            {
                foreach (var appointment in user.Appointments)
                {
                    // TimeSlot'un müsaitlik durumunu true yap
                    if (appointment.TimeSlot != null)
                    {
                        appointment.TimeSlot.IsAvailable = true;
                    }

                    // Randevuyu sil
                    _context.Appointments.Remove(appointment);
                }
            }


            // LINQ kullanımı
            var employee = (from e in _context.Employees
                            where e.UserId == id
                            select e)
                .Include(e => e.Appointments)  // Çalışanın randevularını dahil et
                .ThenInclude(a => a.TimeSlot)  // Randevuların bağlı olduğu TimeSlot'ları dahil et
                .FirstOrDefault();

            if (employee != null)
            {
                foreach (var appointment in employee.Appointments)
                {
                    // TimeSlot'un müsaitlik durumunu true yap
                    if (appointment.TimeSlot != null)
                    {
                        appointment.TimeSlot.IsAvailable = true;
                    }

                    // Randevuyu sil
                    _context.Appointments.Remove(appointment);
                }
            }




            // Kullanıcıyı sil
            _context.Users.Remove(user);
            _context.SaveChanges();

            return RedirectToAction("UserDashboard", "Admin");
        }

        public IActionResult ToggleActiveStatus(int id)
        {
            // Güvenlik kontrolünü çağır
            var result = _authorizeHelper.CheckUserRoles("Admin");
            if (result != null) return result; // Eğer hata varsa, döndür

            SetUserInfoToViewData();

            var user = _context.Users.Include(u => u.Appointments) // Kullanıcının randevularını da dahil et
                          .ThenInclude(a => a.TimeSlot) // Randevularının bağlı olduğu TimeSlot'ları da dahil et
                          .FirstOrDefault(u => u.Id == id);

            if (user == null)
            {
                return RedirectToAction("UserDashboard", "Admin");
            }

            user.IsActive = !user.IsActive;

            // Eğer kullanıcı deaktif yapılıyorsa
            if (!user.IsActive)
            {
                // Kullanıcının randevuları varsa, onları sil
                if (user.Appointments != null && user.Appointments.Any())
                {
                    foreach (var appointment in user.Appointments)
                    {
                        // Randevunun ilişkili olduğu çalışanı getir
                        var employee = _context.Employees
                            .FirstOrDefault(e => e.Id == appointment.EmployeeId); // Appointment'tan EmployeeId alınır

                        if (employee != null)
                        {
                            // Çalışanın toplam kazanç ve randevu sayısını azalt
                            employee.TotalAppointments--;
                            employee.TotalEarnings -= appointment.Price;
                        }

                        // TimeSlot'u boşalt
                        appointment.TimeSlot.IsAvailable = true;

                        // Randevuyu sil
                        _context.Appointments.Remove(appointment);
                    }
                }

                // Sonradan eklenen "Employee" kontrolü
                if (user.Role == "Employee")
                {
                    // Employee nesnesini getir
                    var employee = _context.Employees
                        .Include(e => e.Appointments)
                        .ThenInclude(a => a.TimeSlot)
                        .FirstOrDefault(e => e.UserId == user.Id);

                    if (employee != null && employee.Appointments != null && employee.Appointments.Any())
                    {
                        foreach (var appointment in employee.Appointments)
                        {
                            // Çalışanın toplam kazanç ve randevu sayısını azalt
                            employee.TotalAppointments--;
                            employee.TotalEarnings -= appointment.Price;

                            // TimeSlot'u tekrar müsait hale getir
                            appointment.TimeSlot.IsAvailable = true;

                            // Randevuyu sil
                            _context.Appointments.Remove(appointment);
                        }
                    }
                }
            }



            _context.SaveChanges();

            return RedirectToAction("UserDashboard", "Admin");
        }

        //==========================User====================================
    }


}
