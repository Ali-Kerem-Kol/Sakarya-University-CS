using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Options;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Data
{
    public class KuaforContext : DbContext
    {
        public DbSet<Salon> Salons { get; set; }
        public DbSet<Employee> Employees { get; set; }
        public DbSet<Appointment> Appointments { get; set; }
        public DbSet<User> Users { get; set; }


        public KuaforContext(DbContextOptions<KuaforContext> options) : base(options)
        {
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            // Admin kullanıcısını seed etmek
            modelBuilder.Entity<User>().HasData(new User
            {
                UserId = 1,
                Username = "Admin",
                Password = "1234",
                Role = "Admin"
            });
        }


    }
}
