namespace Web_Programlama_Projesi.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.Collections.Generic;

    public class Salon
    {
        [Key]
        public int SalonId { get; set; }

        [Required, MaxLength(100)]
        public string Name { get; set; } // Salon adı

        [Required, MaxLength(200)]
        public string Location { get; set; } // Salonun adresi

        [Required]
        public TimeSpan StartHour { get; set; } // Çalışma başlangıç saati

        [Required]
        public TimeSpan EndHour { get; set; } // Çalışma bitiş saati

        [Required, Range(15, 120)]
        public int TimeSlotDuration { get; set; } // Randevu aralığı (dakika cinsinden)

        public ICollection<Employee> Employees { get; set; } // Salondaki çalışanlar


        public IEnumerable<TimeSpan> GenerateTimeSlots()
        {
            var slots = new List<TimeSpan>();
            var currentTime = StartHour;
            while (currentTime < EndHour)
            {
                slots.Add(currentTime);
                currentTime = currentTime.Add(TimeSpan.FromMinutes(TimeSlotDuration));
            }
            return slots;
        }

    }
}
