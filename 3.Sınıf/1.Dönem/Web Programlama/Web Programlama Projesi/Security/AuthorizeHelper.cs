using Google;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Web_Programlama_Projesi.Data;

namespace Web_Programlama_Projesi.Security
{
    public class AuthorizeHelper
    {
        private readonly HttpContext _httpContext;
        private readonly KuaferContext _context;

        public AuthorizeHelper(HttpContext httpContext, KuaferContext context)
        {
            _httpContext = httpContext;
            _context = context;
        }

        public IActionResult CheckUserRoles(params string[] requiredRoles)
        {
            // Oturumdaki kullanıcı ID'sini al
            var currentUserId = _httpContext.Session.GetInt32("Id");
            if (currentUserId == null)
            {
                return new RedirectToActionResult("Login", "Home", null); // Giriş sayfasına yönlendir
            }

            // Kullanıcı bilgilerini al
            var user = _context.Users
                .Include(u => u.EmployeeDetails) // Gerekirse ilişkili tablolara erişim için Include kullanabilirsiniz
                .FirstOrDefault(u => u.Id == currentUserId);

            if (user == null || !requiredRoles.Contains(user.Role))
            {
                return new ForbidResult(); // Kullanıcı yetkisizse erişimi engelle
            }

            // Her şey yolunda, erişime izin ver
            return null;
        }


    }

}
