using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class AppointmentDto
    {

        [Required]
        public int TimeSlotId { get; set; }  // Hangi zaman dilimine ait
        public string SalonName { get; set; }

        [Required]
        public int CustomerId { get; set; }  // Randevu sahibinin id'si
        public string CustomerName { get; set; }

        [Required]
        public int EmployeeId { get; set; }  // Çalışanın id'si
        public string EmployeeName { get; set; }

        [Required]
        public decimal Price { get; set; }  // Fiyat bilgisi

        public bool IsApproved { get; set; } = true;  // Randevu onaylı mı?
    }
}
