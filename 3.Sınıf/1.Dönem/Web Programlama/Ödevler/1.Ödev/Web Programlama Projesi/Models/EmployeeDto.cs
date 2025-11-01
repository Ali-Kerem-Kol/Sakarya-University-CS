using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class EmployeeDto
    {

        [Required]
        [MaxLength(100)] // Uzmanlık alanı (örn: Saç kesimi)
        public string Expertise { get; set; } = null!;
        public bool IsActive { get; set; } = true;

        //[Required]
        //public int UserId { get; set; }
        public string Username { get; set; } = null!;
        public string Role { get; set; } = null!;
        public string Password { get; set; } = null!;

        // Çalışanın sahip olabileceği randevular (1-N İlişkisi)
        public ICollection<Appointment>? Appointments { get; set; }
    }
}
