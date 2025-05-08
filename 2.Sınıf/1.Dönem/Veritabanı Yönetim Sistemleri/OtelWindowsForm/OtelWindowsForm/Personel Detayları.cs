using Microsoft.Win32.SafeHandles;
using Npgsql;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace OtelWindowsForm
{
    public partial class Personel_Detayları : Form
    {
        public Personel_Detayları()
        {
            InitializeComponent();
        }

        NpgsqlConnection connection = new NpgsqlConnection("server=localHost;port=5432;Database=DB_Otel;user ID=postgres;password=1234");

        ///-----

        static int personelIDkontrol(string deger)
        {
            if (string.IsNullOrEmpty(deger))
            {
                // Hata durumu: Boş veya null gelen değer
                Console.WriteLine("Hata: Geçersiz değer. Boş veya null değerler kabul edilmez.");
                return -1; // Hata durumunu temsil eden bir değer döndürülebilir.
            }

            // İlk harfi kontrol et ve uygun değeri döndür
            char ilkHarf = char.ToUpper(deger[0]);

            switch (ilkHarf)
            {
                case 'T':
                    return 1;
                case 'G':
                    return 2;
                case 'A':
                    return 3;
                case 'L':
                    return 4;
                case 'Y':
                    return 5;
                default:
                    // Hata durumu: Geçersiz başlangıç harfi
                    Console.WriteLine($"Hata: Geçersiz başlangıç harfi '{ilkHarf}'.");
                    return -1; // Hata durumunu temsil eden bir değer döndürülebilir.
            }
        }

        private bool PersonelIdVarMi(string personelId)
        {
            NpgsqlCommand kontrolKomutu = new NpgsqlCommand("SELECT COUNT(*) FROM \"Personel\" WHERE \"personel_id\" = @personelId", connection);
            kontrolKomutu.Parameters.AddWithValue("@personelId", personelId);

            int kayitSayisi = Convert.ToInt32(kontrolKomutu.ExecuteScalar());

            return kayitSayisi > 0;
        }


        private bool IzinIdVarMi(int izinId)
        {
            NpgsqlCommand kontrolKomutu = new NpgsqlCommand("SELECT COUNT(*) FROM \"PersonelIzinleri\" WHERE \"izin_id\" = @izin_id", connection);
            kontrolKomutu.Parameters.AddWithValue("@izin_id", izinId);

            int kayitSayisi = Convert.ToInt32(kontrolKomutu.ExecuteScalar());

            return kayitSayisi > 0;
        }


        


        ///-----

        private void btn_exit_Personel_Detaylari_Click(object sender, EventArgs e)
        {
            Form1 form1 = new Form1();

            this.Close(); // veya this.Close();

            form1.Show();
        }

        private void btn_guncelle_temizlikci_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            int kontrol = personelIDkontrol(txt_temizlikci_personel_id.Text);

            if (!PersonelIdVarMi(txt_temizlikci_personel_id.Text) && kontrol == 1)
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand temizlikciGuncellemeKomutu = new NpgsqlCommand("UPDATE \"Temizlikci\" SET temizledigi_alan = @p2,calisma_saati = @p3 WHERE personel_id = @p1", connection);

            temizlikciGuncellemeKomutu.Parameters.AddWithValue("@p1", txt_temizlikci_personel_id.Text);
            temizlikciGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_temizlikci_temizlik_alani.Text);
            temizlikciGuncellemeKomutu.Parameters.AddWithValue("@p3", num_temizlikci_calisma_saati.Value);

            temizlikciGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_listele_temizlikci_Click(object sender, EventArgs e)
        {
            string sorgu = "select personel_id,temizledigi_alan,calisma_saati from \"Temizlikci\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_Temizlikci.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void button1_Click(object sender, EventArgs e)
        {
            Temizlik_Alanlari temizlikAlani = new Temizlik_Alanlari();

            this.Hide(); // veya this.Close(); 
            ///// ilk başta "Detaylar..." a tıklayınca ana formu çarpı işaretinen kapatınca program sonlanmıyor ?!

            temizlikAlani.Show();
        }

        private void btn_guncelle_asci_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            int kontrol = personelIDkontrol(txt_asci_personel_id.Text);

            if (!PersonelIdVarMi(txt_asci_personel_id.Text) && kontrol == 3)
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand asciGuncellemeKomutu = new NpgsqlCommand("UPDATE \"Asci\" SET tecrube = @p2 WHERE personel_id = @p1", connection);

            asciGuncellemeKomutu.Parameters.AddWithValue("@p1", txt_asci_personel_id.Text);
            asciGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_asci_tecrube.Text);

            asciGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_listele_asci_Click(object sender, EventArgs e)
        {
            string sorgu = "select personel_id,tecrube from \"Asci\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_Asci.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void btn_guncelle_guvenlik_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            int kontrol = personelIDkontrol(txt_guvenlik_personel_id.Text);

            if (!PersonelIdVarMi(txt_guvenlik_personel_id.Text) && kontrol == 2)
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand guvenlikGuncellemeKomutu = new NpgsqlCommand("UPDATE \"Guvenlik\" SET vardiya = @p2,bolge = @p3 WHERE personel_id = @p1", connection);

            guvenlikGuncellemeKomutu.Parameters.AddWithValue("@p1", txt_guvenlik_personel_id.Text);
            guvenlikGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_guvenlik_vardiya.Text);
            guvenlikGuncellemeKomutu.Parameters.AddWithValue("@p3", txt_guvenlik_calistigi_bolge.Text);

            guvenlikGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_listele_guvenlik_Click(object sender, EventArgs e)
        {
            string sorgu = "select personel_id,vardiya,bolge from \"Guvenlik\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_Guvenlik.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void btn_guncelle_lobi_elemani_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            int kontrol = personelIDkontrol(txt_lobi_elemani_personel_id.Text);

            if (!PersonelIdVarMi(txt_lobi_elemani_personel_id.Text) && kontrol == 4)
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand lobielemaniGuncellemeKomutu = new NpgsqlCommand("UPDATE \"LobiElemani\" SET vardiya = @p2 WHERE personel_id = @p1", connection);

            lobielemaniGuncellemeKomutu.Parameters.AddWithValue("@p1", txt_lobi_elemani_personel_id.Text);
            lobielemaniGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_lobi_elemani_vardiya.Text);

            lobielemaniGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_listele_lobi_elemani_Click(object sender, EventArgs e)
        {
            string sorgu = "select personel_id,vardiya from \"LobiElemani\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_LobiElemani.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void btn_guncelle_yonetici_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            int kontrol = personelIDkontrol(txt_yonetici_personel_id.Text);

            if (!PersonelIdVarMi(txt_yonetici_personel_id.Text) && kontrol == 5)
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand yoneticiGuncellemeKomutu = new NpgsqlCommand("UPDATE \"Yonetici\" SET yonettikleri = @p2 WHERE personel_id = @p1", connection);

            yoneticiGuncellemeKomutu.Parameters.AddWithValue("@p1", txt_yonetici_personel_id.Text);
            yoneticiGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_yonetici_yonettikleri.Text);

            yoneticiGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_listele_yonetici_Click(object sender, EventArgs e)
        {
            string sorgu = "select personel_id,yonettikleri from \"Yonetici\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_Yonetici.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void btn_guncelle_personel_izinleri_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            if (!PersonelIdVarMi(txt_personel_izinleri_personel_id.Text))
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }
            if (!IzinIdVarMi((int)num_personel_izinleri_izin_id.Value))
            {
                MessageBox.Show("İzin Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand personelizinleriGuncellemeKomutu = new NpgsqlCommand("UPDATE \"PersonelIzinleri\" SET izin_baslangic_tarihi = @p3,izin_bitis_tarihi = @p4,aciklama = @p5 WHERE izin_id = @p1 AND personel_id = @p2", connection);

            personelizinleriGuncellemeKomutu.Parameters.AddWithValue("@p1", num_personel_izinleri_izin_id.Value);
            personelizinleriGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_personel_izinleri_personel_id.Text);
            personelizinleriGuncellemeKomutu.Parameters.AddWithValue("@p3", dtp_personel_izinleri_baslangic_tarihi.Value);
            personelizinleriGuncellemeKomutu.Parameters.AddWithValue("@p4", dtp_personel_izinleri_bitis_tarihi.Value);
            personelizinleriGuncellemeKomutu.Parameters.AddWithValue("@p5", txt_personel_izinleri_aciklama.Text);

            personelizinleriGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_listele_personel_izinleri_Click(object sender, EventArgs e)
        {
            string sorgu = "select izin_id,personel_id,izin_baslangic_tarihi,izin_bitis_tarihi,aciklama from \"PersonelIzinleri\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_PersonelIzinleri.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void btn_ekle_personel_izinleri_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            // Aynı personel_id ile kayıt olup olmadığını kontrol et
            if (!PersonelIdVarMi(txt_personel_izinleri_personel_id.Text))
            {
                MessageBox.Show("Geçersiz Personel ID !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }
            if (IzinIdVarMi((int)num_personel_izinleri_izin_id.Value))
            {
                MessageBox.Show("Böyle bir izin zaten mevcut !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            NpgsqlCommand personelEklemeKomutu = new NpgsqlCommand("INSERT INTO \"PersonelIzinleri\" (\"izin_id\",\"personel_id\",\"izin_baslangic_tarihi\",\"izin_bitis_tarihi\",\"aciklama\")\r\nVALUES (@p1,@p2,@p3,@p4,@p5);\r\n", connection);

            personelEklemeKomutu.Parameters.AddWithValue("@p1", num_personel_izinleri_izin_id.Value);
            personelEklemeKomutu.Parameters.AddWithValue("@p2", txt_personel_izinleri_personel_id.Text);
            personelEklemeKomutu.Parameters.AddWithValue("@p3", dtp_personel_izinleri_baslangic_tarihi.Value);
            personelEklemeKomutu.Parameters.AddWithValue("@p4", dtp_personel_izinleri_bitis_tarihi.Value);
            personelEklemeKomutu.Parameters.AddWithValue("@p5", txt_personel_izinleri_aciklama.Text);

            personelEklemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void btn_sil_personel_izinleri_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            NpgsqlCommand personelizinleriSilmeKomutu = new NpgsqlCommand("DELETE FROM \"PersonelIzinleri\" WHERE izin_id = @p1 AND personel_id=@p2 ", connection);
            if (!PersonelIdVarMi(txt_personel_izinleri_personel_id.Text))
            {
                MessageBox.Show("Personel Bulunamadı !", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Error);
                connection.Close();
                return;
            }

            personelizinleriSilmeKomutu.Parameters.AddWithValue("@p1", num_personel_izinleri_izin_id.Value);
            personelizinleriSilmeKomutu.Parameters.AddWithValue("@p2", txt_personel_izinleri_personel_id.Text);

            personelizinleriSilmeKomutu.ExecuteNonQuery();



            connection.Close();
            ///////////////
        }

        private void btn_guncelle_personel_izin_loglari_Click(object sender, EventArgs e)
        {

        }

        private void btn_listele_personel_izin_loglari_Click(object sender, EventArgs e)
        {
            string sorgu = "select * from \"PersonelIzinDegisiklikleri\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_PersonelIzinDegisiklikleri.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void btn_sil_personel_izin_loglari_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();


            NpgsqlCommand personelizinloglariSilmeKomutu = new NpgsqlCommand("DELETE FROM \"PersonelIzinDegisiklikleri\"", connection);

            personelizinloglariSilmeKomutu.ExecuteNonQuery();


            connection.Close();
            ///////////////
        }

        private void Personel_Detayları_Load(object sender, EventArgs e)
        {


        }

    }
}
