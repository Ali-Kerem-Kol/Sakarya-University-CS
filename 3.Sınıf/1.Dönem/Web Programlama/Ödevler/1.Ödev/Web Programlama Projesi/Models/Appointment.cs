using System.ComponentModel.DataAnnotations.Schema;
using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class Appointment
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public int TimeSlotId { get; set; }  // Hangi zaman dilimine ait
        public TimeSlot TimeSlot { get; set; }

        [Required]
        public int CustomerId { get; set; }  // Randevu sahibinin id'si
        public User Customer { get; set; }

        [Required]
        public int EmployeeId { get; set; }  // Çalışanın id'si
        public Employee Employee { get; set; }

        [Required]
        public decimal Price { get; set; }  // Fiyat bilgisi

        public bool IsApproved { get; set; } = true;  // Randevu onaylı mı?
    }


}
