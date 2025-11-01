using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class UserDto
    {

        [Required] // Boş geçilemez
        [MaxLength(50)] // Maksimum uzunluk 50 karakter
        public string Username { get; set; } = null!;

        [Required]
        [MaxLength(255)] // Şifre için maksimum uzunluk
        public string Password { get; set; } = null!; // Şifreyi burada tutacağız

        [Required]
        [MaxLength(20)] // Rol: Admin, User veya Employee
        public string Role { get; set; } = "User"; // Varsayılan: User

        public bool IsActive { get; set; } = true; // Kullanıcı aktif mi?

        // Kullanıcının alabileceği randevular (1-N İlişkisi)
        public ICollection<Appointment>? Appointments { get; set; }

        // Çalışan bilgileri (1-1 İlişki)
        //public Employee? EmployeeDetails { get; set; }

    }
}
