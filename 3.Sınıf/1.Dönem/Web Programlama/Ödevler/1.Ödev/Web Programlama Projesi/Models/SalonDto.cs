using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class SalonDto
    {
        [Required]
        public string Name { get; set; }  // Salon adı

        [Required]
        public string WorkingHours { get; set; }  // Salon çalışma saatleri (örn: "09:00-17:00")

        [Required]
        public decimal AppointmentPrice { get; set; }  // Fiyat bilgisi

        [Required]
        public string Expertise { get; set; }

        // Salon ile ilişkilendirilmiş zaman dilimlerini tutan koleksiyon
        //public ICollection<TimeSlot>? TimeSlots { get; set; }
    }
}
