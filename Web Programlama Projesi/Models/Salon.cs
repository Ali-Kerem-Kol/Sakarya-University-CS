using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class Salon
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public string Name { get; set; }  // Salon adı

        [Required]
        public string WorkingHours { get; set; }  // Salon çalışma saatleri (örn: "09:00-17:00")

        [Required]
        public decimal AppointmentPrice { get; set; }  // Fiyat bilgisi

        public string Expertise { get; set; }

        // Salon ile ilişkilendirilmiş zaman dilimlerini tutan koleksiyon
        public ICollection<TimeSlot> TimeSlots { get; set; }
    }

}
