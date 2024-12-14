using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Diagnostics;
using Microsoft.Extensions.Options;
using Web_Programlama_Projesi.Data;
using Web_Programlama_Projesi.Models;

namespace Web_Programlama_Projesi.Data
{
    public class KuaferContext : DbContext
    {
        public KuaferContext(DbContextOptions<KuaferContext> options) : base(options) { }

        public DbSet<User> Users { get; set; } = null!;
        public DbSet<Employee> Employees { get; set; } = null!;
        public DbSet<Salon> Salons { get; set; } = null!;
        public DbSet<Appointment> Appointments { get; set; } = null!;

        protected override void OnConfiguring(DbContextOptionsBuilder optionsBuilder)
        {
            optionsBuilder.ConfigureWarnings(warnings =>
                warnings.Ignore(RelationalEventId.PendingModelChangesWarning));
        }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {

            // User ve Employee arasında bire bir ilişki
            modelBuilder.Entity<User>()
                .HasOne(u => u.EmployeeDetails)
                .WithOne(e => e.User)
                .HasForeignKey<Employee>(e => e.UserId);

            // Varsayılan Admin Kullanıcısı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 1,
                Username = "admin",
                Password = "123",
                Role = "Admin",
                IsActive = true,
            });

            // Varsayılan Normal Kullanıcı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 2,
                Username = "user",
                Password = "123",
                Role = "User",
                IsActive = true,
            });

            // Varsayılan Çalışan Kullanıcısı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 3,
                Username = "ahmet",
                Password = "123",
                Role = "Employee",
                IsActive = true,
            });

            // Varsayılan Çalışan (Employee Tablosu)
            modelBuilder.Entity<Employee>().HasData(new Employee
            {
                Id = 1,
                Expertise = "Saç Kesimi",
                IsActive = true,
                SalonId = 1, // Varsayılan salonla ilişkilendirilmiş
                UserId = 3, // Varsayılan çalışan kullanıcısıyla ilişkilendirilmiş
            });

            // Varsayılan Salon
            modelBuilder.Entity<Salon>().HasData(new Salon
            {
                Id = 1,
                Name = "Sac kesim salonu",
                WorkingHours = "09:00-17:00",
                Interval = 60
            });



            // Username alanına unique constraint ekliyoruz
            modelBuilder.Entity<User>()
                .HasIndex(u => u.Username)  // Username alanını index'liyoruz
                .IsUnique();  // Benzersiz olmasını sağlıyoruz

            /*
            // Fluent API ile ilişkileri tanımlayabiliriz
            modelBuilder.Entity<Appointment>()
                .Property(a => a.Price)
                .HasColumnType("decimal(18,2)"); // SQL Server decimal(18,2)

            modelBuilder.Entity<User>()
                .HasMany(u => u.Appointments)
                .WithOne(a => a.Customer)
                .HasForeignKey(a => a.CustomerId);

            modelBuilder.Entity<Employee>()
                .HasMany(e => e.Appointments)
                .WithOne(a => a.Employee)
                .HasForeignKey(a => a.EmployeeId);

            modelBuilder.Entity<Salon>()
                .HasMany(s => s.Employees)
                .WithOne(e => e.Salon)
                .HasForeignKey(e => e.SalonId);

            */

            base.OnModelCreating(modelBuilder);
        }
    }

}
