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
        public DbSet<TimeSlot> TimeSlots { get; set; } = null!;

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {

            // TimeSlot ve Salon ilişkisini tanımlıyoruz
            modelBuilder.Entity<TimeSlot>()
                .HasOne(ts => ts.Salon)
                .WithMany(s => s.TimeSlots)
                .HasForeignKey(ts => ts.SalonId);

            // Appointment ve TimeSlot ilişkisini tanımlıyoruz
            modelBuilder.Entity<Appointment>()
                .HasOne(a => a.TimeSlot)
                .WithMany(ts => ts.Appointments)
                .HasForeignKey(a => a.TimeSlotId);


            modelBuilder.Entity<User>()
                .HasOne(u => u.EmployeeDetails)
                .WithOne(e => e.User)
                .HasForeignKey<Employee>(e => e.UserId);

            modelBuilder.Entity<User>()
                .HasIndex(u => u.Username)
                .IsUnique();

            // Sonradan eklenenler
            modelBuilder.Entity<User>()
    .HasOne(u => u.EmployeeDetails)
    .WithOne(e => e.User)
    .HasForeignKey<Employee>(e => e.UserId);

            modelBuilder.Entity<User>()
    .HasOne(u => u.EmployeeDetails)
    .WithOne(e => e.User)
    .HasForeignKey<Employee>(e => e.UserId)
    .OnDelete(DeleteBehavior.Cascade);  // Cascade delete ekleniyor

            modelBuilder.Entity<TimeSlot>()
    .HasOne(ts => ts.Salon)
    .WithMany(s => s.TimeSlots)
    .HasForeignKey(ts => ts.SalonId)
    .OnDelete(DeleteBehavior.Cascade);  // Cascade delete

            modelBuilder.Entity<Appointment>()
    .HasOne(a => a.TimeSlot)
    .WithMany(ts => ts.Appointments)
    .HasForeignKey(a => a.TimeSlotId)
    .OnDelete(DeleteBehavior.Cascade);  // Cascade delete

            modelBuilder.Entity<Appointment>()
        .HasOne(a => a.Customer)  // Appointment, User'a bağlı
        .WithMany(u => u.Appointments)  // User birden fazla Appointment'a sahip olabilir
        .HasForeignKey(a => a.CustomerId)  // Appointment'taki UserId ile ilişkilendiriyoruz
        .OnDelete(DeleteBehavior.Cascade);  // User silindiğinde ona ait Appointment'lar da silinsin

            // Sonradan eklenenler


            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 1,
                Username = "B221210042@sakarya.edu.tr",
                Password = "123456",
                Role = "Admin",
                IsActive = true,
            });

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
                Username = "Ahmet",
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
                UserId = 3,  // Kullanıcı Id'si 3 olan ahmet ile ilişkilendirme
            });

            // Varsayılan Çalışan Kullanıcısı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 4,
                Username = "Ayşe",
                Password = "123",
                Role = "Employee",
                IsActive = true,
            });

            // Varsayılan Çalışan (Employee Tablosu)
            modelBuilder.Entity<Employee>().HasData(new Employee
            {
                Id = 2,
                Expertise = "Güzellik Bakımı",
                IsActive = true,
                UserId = 4,  // Kullanıcı Id'si 3 olan ahmet ile ilişkilendirme
            });

            // Varsayılan Çalışan Kullanıcısı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 5,
                Username = "Mehmet",
                Password = "123",
                Role = "Employee",
                IsActive = true,
            });

            // Varsayılan Çalışan (Employee Tablosu)
            modelBuilder.Entity<Employee>().HasData(new Employee
            {
                Id = 3,
                Expertise = "Masaj",
                IsActive = true,
                UserId = 5,  // Kullanıcı Id'si 3 olan ahmet ile ilişkilendirme
            });

            // Varsayılan Çalışan Kullanıcısı
            modelBuilder.Entity<User>().HasData(new User
            {
                Id = 6,
                Username = "Fatma",
                Password = "123",
                Role = "Employee",
                IsActive = true,
            });

            // Varsayılan Çalışan (Employee Tablosu)
            modelBuilder.Entity<Employee>().HasData(new Employee
            {
                Id = 4,
                Expertise = "Pedikür/Manikür",
                IsActive = true,
                UserId = 6,  // Kullanıcı Id'si 3 olan ahmet ile ilişkilendirme
            });

            // Varsayılan Salon
            modelBuilder.Entity<Salon>().HasData(
    new Salon
    {
        Id = 1,
        Name = "Saç Kesim Salonu",
        WorkingHours = "09:00-17:00",
        AppointmentPrice = 100,
        Expertise = "Saç Kesimi"
    },
    new Salon
    {
        Id = 2,
        Name = "Güzellik Salonu",
        WorkingHours = "10:00-18:00",
        AppointmentPrice = 100,
        Expertise = "Güzellik Bakımı"
    }
);

            // Varsayılan TimeSlot'lar ekliyoruz (zamanlar string olarak)
            modelBuilder.Entity<TimeSlot>().HasData(
                // Saç Kesim Salonu için TimeSlot'lar
                new TimeSlot
                {
                    Id = 1,
                    SalonId = 1,
                    StartTime = "1 Ocak 09:00",
                    EndTime = "10:00",
                    IsAvailable = true
                },
                new TimeSlot
                {
                    Id = 2,
                    SalonId = 1,
                    StartTime = "1 Ocak 10:00",
                    EndTime = "11:00",
                    IsAvailable = true
                },
                new TimeSlot
                {
                    Id = 3,
                    SalonId = 1,
                    StartTime = "1 Ocak 11:00",
                    EndTime = "12:00",
                    IsAvailable = true
                }, 
                new TimeSlot
                {
                    Id = 4,
                    SalonId = 1,
                    StartTime = "1 Ocak 12:00",
                    EndTime = "13:00",
                    IsAvailable = true
                }, 
                new TimeSlot
                {
                    Id = 5,
                    SalonId = 1,
                    StartTime = "1 Ocak 13:00",
                    EndTime = "14:00",
                    IsAvailable = true
                }, 
                new TimeSlot
                {
                    Id = 6,
                    SalonId = 1,
                    StartTime = "1 Ocak 14:00",
                    EndTime = "15:00",
                    IsAvailable = true
                }, 
                new TimeSlot
                {
                    Id = 7,
                    SalonId = 1,
                    StartTime = "1 Ocak 15:00",
                    EndTime = "16:00",
                    IsAvailable = true
                }, 
                new TimeSlot
                {
                    Id = 8,
                    SalonId = 1,
                    StartTime = "1 Ocak 16:00",
                    EndTime = "17:00",
                    IsAvailable = true
                },
                // Güzellik Salonu için TimeSlot'lar
                new TimeSlot
                {
                    Id = 9,
                    SalonId = 2,
                    StartTime = "1 Ocak 10:00",
                    EndTime = "11:00",
                    IsAvailable = true
                },
                new TimeSlot
                {
                    Id = 10,
                    SalonId = 2,
                    StartTime = "1 Ocak 11:00",
                    EndTime = "12:00",
                    IsAvailable = true
                },
                new TimeSlot
                {
                    Id = 11,
                    SalonId = 2,
                    StartTime = "1 Ocak 12:00",
                    EndTime = "13:00",
                    IsAvailable = true
                }, new TimeSlot
                {
                    Id = 12,
                    SalonId = 2,
                    StartTime = "1 Ocak 13:00",
                    EndTime = "14:00",
                    IsAvailable = true
                }, new TimeSlot
                {
                    Id = 13,
                    SalonId = 2,
                    StartTime = "1 Ocak 14:00",
                    EndTime = "15:00",
                    IsAvailable = true
                }, new TimeSlot
                {
                    Id = 14,
                    SalonId = 2,
                    StartTime = "1 Ocak 15:00",
                    EndTime = "16:00",
                    IsAvailable = true
                }, new TimeSlot
                {
                    Id = 15,
                    SalonId = 2,
                    StartTime = "1 Ocak 16:00",
                    EndTime = "17:00",
                    IsAvailable = true
                }, new TimeSlot
                {
                    Id = 16,
                    SalonId = 2,
                    StartTime = "1 Ocak 17:00",
                    EndTime = "18:00",
                    IsAvailable = true
                }
            );


            base.OnModelCreating(modelBuilder);
        }
    }


}
