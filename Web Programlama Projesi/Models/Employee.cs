using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class Employee
    {
        [Key] // Birincil anahtar
        public int Id { get; set; }

        [Required]
        [MaxLength(100)] // Uzmanlık alanı (örn: Saç kesimi)
        public string Expertise { get; set; } = null!;

        public bool IsActive { get; set; } = true;

        // User tablosuyla bire bir ilişki
        [ForeignKey("User")]
        public int UserId { get; set; }
        public User User { get; set; } = null!;

        // Çalışanın sahip olabileceği randevular (1-N İlişkisi)
        public ICollection<Appointment>? Appointments { get; set; }
    }
}
