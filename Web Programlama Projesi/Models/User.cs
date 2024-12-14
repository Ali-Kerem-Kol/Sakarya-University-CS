namespace Web_Programlama_Projesi.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.ComponentModel.DataAnnotations.Schema;

    public class User
    {
        [Key] // Birincil anahtar
        public int Id { get; set; }

        [Required] // Boş geçilemez
        [MaxLength(50)] // Maksimum uzunluk 50 karakter
        public string Username { get; set; } = null!;

        [Required]
        [MaxLength(255)] // Şifre için maksimum uzunluk
        public string PasswordHash { get; set; } = null!;

        [Required]
        [MaxLength(20)] // "Admin" veya "User" rollerini tutar
        public string Role { get; set; } = "User";

        public bool IsActive { get; set; } = true; // Kullanıcı aktif mi?

        [DataType(DataType.DateTime)]
        public DateTime CreatedDate { get; set; } = DateTime.Now;

        // Kullanıcının alabileceği randevular (1-N İlişkisi)
        public ICollection<Appointment>? Appointments { get; set; }
    }

}
