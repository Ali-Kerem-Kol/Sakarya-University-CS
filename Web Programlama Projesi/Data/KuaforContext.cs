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

            // Varsayılan Admin Kullanıcısı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 1,
                Username = "admin",
                PasswordHash = "admin123", // Gerçek projede hashlenmiş bir şifre kullanın
                Role = "Admin",
                IsActive = true,
                CreatedDate = DateTime.Now
            });

            // Varsayılan Salon
            modelBuilder.Entity<Salon>().HasData(new Salon
            {
                Id = 1,
                Name = "Varsayılan Salon",
                WorkingHours = "09:00-17:00",
                Interval = 60
            });
            //--------------------------------------------------------------
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

            //--------------------------------------------------------------
            base.OnModelCreating(modelBuilder);
        }
    }

}
