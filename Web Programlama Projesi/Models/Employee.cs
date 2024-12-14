namespace Web_Programlama_Projesi.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using System.Collections.Generic;

    public class Employee
    {
        [Key]
        public int EmployeeId { get; set; }

        [Required, MaxLength(50)]
        public string Name { get; set; } // Çalışan adı

        [Required, MaxLength(50)]
        public string Username { get; set; } // Çalışan kullanıcı adı

        [Required, MaxLength(100)]
        public string Password { get; set; } // Şifre

        [Required, MaxLength(200)]
        public string Expertise { get; set; } // Uzmanlık alanı

        [Required]
        public int SalonId { get; set; } // Çalışanın bağlı olduğu salon
        public Salon Salon { get; set; }

        public ICollection<Appointment> Appointments { get; set; } // Çalışanın randevuları
    }
}
