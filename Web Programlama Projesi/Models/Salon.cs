using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class Salon
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(100)] // Salon adı için maksimum uzunluk
        public string Name { get; set; } = null!;

        [Required]
        [MaxLength(50)] // Örn: "09:00-17:00" formatında çalışma saatleri
        public string WorkingHours { get; set; } = null!;

        [Required]
        [Range(15, 240)] // Periyot için aralık (15dk ile 240dk arasında)
        public int Interval { get; set; } = 60;

        // Salona bağlı çalışanlar (1-N İlişkisi)
        public ICollection<Employee>? Employees { get; set; }
    }
}
