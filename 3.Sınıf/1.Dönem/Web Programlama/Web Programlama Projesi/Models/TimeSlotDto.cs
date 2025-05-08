using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class TimeSlotDto
    {

        [Required]
        public int SalonId { get; set; }  // Hangi salona ait olduğu

        //public string SalonName { get; set; } // bu alan TimeSlot modelinde "Salon" objesi olarak tanımlanmış

        [Required(ErrorMessage = "Başlangıç zamanı zorunludur.")]
        public string StartTime { get; set; }  // Başlangıç saati, string formatında
        [Required(ErrorMessage = "Bitiş zamanı zorunludur.")]
        public string EndTime { get; set; }    // Bitiş saati, string formatında

        public bool IsAvailable { get; set; } = true;  // Saat dilimi boş mu dolu mu? (Randevu alındığında false olacak)

        //public ICollection<Appointment> Appointments { get; set; }

    }
}
