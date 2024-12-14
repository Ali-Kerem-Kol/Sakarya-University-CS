using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class Appointment
    {
        [Key]
        public int Id { get; set; }

        // Müşteri ile ilişki (N-1)
        [Required]
        [ForeignKey("Customer")]
        public int CustomerId { get; set; }
        public User Customer { get; set; } = null!;

        // Çalışan ile ilişki (N-1)
        [Required]
        [ForeignKey("Employee")]
        public int EmployeeId { get; set; }
        public Employee Employee { get; set; } = null!;

        // Salon ile ilişki (N-1)
        [Required]
        [ForeignKey("Salon")]
        public int SalonId { get; set; }
        public Salon Salon { get; set; } = null!;

        [Required]
        [DataType(DataType.DateTime)] // Randevu başlangıç tarihi
        public DateTime Date { get; set; }

        [Required]
        [Range(15, 240)] // Randevu süresi (15dk ile 240dk arasında)
        public int Duration { get; set; } = 60;

        [Required]
        [Column(TypeName = "decimal(18,2)")] // Hassasiyet ve ölçek belirtildi
        public decimal Price { get; set; }

        // Randevu onay durumu: true = onaylı, false = onaysız (Burada true olarak ayarlıyoruz)
        public bool IsApproved { get; set; } = true;  // Varsayılan olarak randevu onaylı

    }

}
