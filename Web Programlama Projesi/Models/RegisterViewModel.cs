using System.ComponentModel.DataAnnotations;

namespace Web_Programlama_Projesi.Models
{
    public class RegisterViewModel
    {
        [Required, MaxLength(50)]
        public string Username { get; set; } // Kullanıcı adı

        [Required, MaxLength(100), DataType(DataType.Password)]
        public string Password { get; set; } // Şifre

        [Required, MaxLength(100), DataType(DataType.Password)]
        [Compare("Password", ErrorMessage = "Şifreler eşleşmiyor.")]
        public string ConfirmPassword { get; set; } // Şifre onayı
    }

}
