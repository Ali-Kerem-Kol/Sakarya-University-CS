namespace Web_Programlama_Projesi.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;
    using Web_Programlama_Projesi.Data;

    public class Appointment
    {
        [Key]
        public int AppointmentId { get; set; }

        [Required]
        public int UserId { get; set; }
        public User User { get; set; } // Randevuyu alan kullanıcı

        [Required]
        public int EmployeeId { get; set; }
        public Employee Employee { get; set; } // Randevuyu sağlayan çalışan

        [Required]
        public DateTime StartTime { get; set; } // Randevu başlangıç zamanı

        [Required]
        public DateTime EndTime { get; set; } // Randevu bitiş zamanı

        [Required, MaxLength(200)]
        public string Service { get; set; } // Alınan hizmet (örneğin saç kesimi)

        [Required]
        public decimal Price { get; set; } // Ücret


        public static bool IsTimeSlotAvailable(KuaforContext context, int employeeId, DateTime startTime, DateTime endTime)
        {
            return !context.Appointments.Any(a =>
                a.EmployeeId == employeeId &&
                ((startTime >= a.StartTime && startTime < a.EndTime) ||
                 (endTime > a.StartTime && endTime <= a.EndTime)));
        }

    }
}
