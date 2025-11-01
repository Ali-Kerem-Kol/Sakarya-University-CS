namespace Web_Programlama_Projesi.Models
{
    using System.ComponentModel.DataAnnotations;

    public class LoginViewModel
    {
        [Required]
        public string Username { get; set; }

        [Required]
        //[DataType(DataType.Password)]
        public string Password { get; set; }

        public bool RememberMe { get; set; }
    }

}
