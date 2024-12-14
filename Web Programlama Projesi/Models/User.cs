namespace Web_Programlama_Projesi.Models
{
    using System.ComponentModel.DataAnnotations;
    using System.Collections.Generic; // ICollection için

    /*
    public enum UserRole
    {
        Admin,
        Employee,
        Customer
    }
    */

    public class User
    {
        [Key]
        public int UserId { get; set; }

        [Required, MaxLength(50)]
        public string Username { get; set; } // Kullanıcı adı (email değil)

        [Required, MaxLength(100)]
        public string Password { get; set; } // Şifre

        [Required]
        public string Role { get; set; } // "Admin" veya "User"
    }
}
