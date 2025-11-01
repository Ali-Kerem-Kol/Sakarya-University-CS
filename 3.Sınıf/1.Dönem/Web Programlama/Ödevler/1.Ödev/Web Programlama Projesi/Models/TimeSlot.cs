using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class TimeSlot
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public int SalonId { get; set; }  // Hangi salona ait olduğu
        public Salon Salon { get; set; }

        [Required]
        public string StartTime { get; set; }  // Başlangıç saati, string formatında
        public string EndTime { get; set; }    // Bitiş saati, string formatında

        public bool IsAvailable { get; set; } = true;  // Saat dilimi boş mu dolu mu? (Randevu alındığında false olacak)

        public ICollection<Appointment> Appointments { get; set; }
    }



}
