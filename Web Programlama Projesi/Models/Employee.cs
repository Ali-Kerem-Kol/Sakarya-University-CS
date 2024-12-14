using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class Employee
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(50)]
        public string Username { get; set; } = null!;

        [Required]
        [MaxLength(255)]
        public string PasswordHash { get; set; } = null!;

        [Required]
        [MaxLength(100)] // Uzmanlık alanı (örn: Saç kesimi)
        public string Expertise { get; set; } = null!;

        public bool IsActive { get; set; } = true;

        // Salon ile ilişki (N-1)
        [ForeignKey("Salon")] // Foreign Key
        public int? SalonId { get; set; }
        public Salon? Salon { get; set; }

        // Çalışanın sahip olabileceği randevular (1-N İlişkisi)
        public ICollection<Appointment>? Appointments { get; set; }
    }

}
