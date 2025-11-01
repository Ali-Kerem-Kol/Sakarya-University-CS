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
    public partial class Hizmetler : Form
    {
        public Hizmetler()
        {
            InitializeComponent();
        }

        NpgsqlConnection connection = new NpgsqlConnection("server=localHost;port=5432;Database=DB_Otel;user ID=postgres;password=1234");

        private bool hizmetIdVarMi(int hizmetId)
        {
            NpgsqlCommand kontrolKomutu = new NpgsqlCommand("SELECT COUNT(*) FROM \"Hizmetler\" WHERE \"hizmet_id\" = @hizmet_id", connection);
            kontrolKomutu.Parameters.AddWithValue("@hizmet_id", hizmetId);

            int kayitSayisi = Convert.ToInt32(kontrolKomutu.ExecuteScalar());

            return kayitSayisi > 0;
        }


        private void button1_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            NpgsqlCommand hizmetEklemeKomutu = new NpgsqlCommand("INSERT INTO \"Hizmetler\" (\"hizmet_id\",\"hizmet_adi\", \"ucret\")\r\nVALUES (@p1, @p2, @p3)", connection);

            if (hizmetIdVarMi((int)num_hizmet_id.Value))
            {
                MessageBox.Show("Bu Hizmet ID zaten kullanımda.", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Information);
                connection.Close();
                return;
            }

            hizmetEklemeKomutu.Parameters.AddWithValue("@p1", num_hizmet_id.Value);
            hizmetEklemeKomutu.Parameters.AddWithValue("@p2", txt_hizmet_adi.Text);
            hizmetEklemeKomutu.Parameters.AddWithValue("@p3", num_hizmet_ucret.Value);

            hizmetEklemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void button2_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();


            NpgsqlCommand hizmetSilmeKomutu = new NpgsqlCommand("DELETE FROM \"Hizmetler\" WHERE hizmet_id=@p1 ", connection);
            
            if (!hizmetIdVarMi((int)num_hizmet_id.Value))
            {
                MessageBox.Show("Böyle Bir Hizmet ID Bulunmuyor !!!", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Information);
                connection.Close();
                return;
            }

            hizmetSilmeKomutu.Parameters.AddWithValue("@p1", num_hizmet_id.Value);
            hizmetSilmeKomutu.ExecuteNonQuery();


            connection.Close();
            ///////////////
        }

        private void button3_Click(object sender, EventArgs e)
        {
            ///////////////
            connection.Open();

            if (!hizmetIdVarMi((int)num_hizmet_id.Value))
            {
                MessageBox.Show("Böyle Bir Hizmet ID Bulunmuyor !!!", "Hata", MessageBoxButtons.OK, MessageBoxIcon.Information);
                connection.Close();
                return;
            }

            NpgsqlCommand hizmetGuncellemeKomutu = new NpgsqlCommand("UPDATE \"Hizmetler\" SET hizmet_adi = @p2, ucret = @p3, WHERE hizmet_id = @p1", connection);

            hizmetGuncellemeKomutu.Parameters.AddWithValue("@p1", num_hizmet_id.Value);
            hizmetGuncellemeKomutu.Parameters.AddWithValue("@p2", txt_hizmet_adi.Text);
            hizmetGuncellemeKomutu.Parameters.AddWithValue("@p3", num_hizmet_ucret.Value);

            hizmetGuncellemeKomutu.ExecuteNonQuery();

            connection.Close();
            ///////////////
        }

        private void button4_Click(object sender, EventArgs e)
        {
            string sorgu = "select * from \"Hizmetler\"";
            ///////////////
            connection.Open();

            NpgsqlDataAdapter da = new NpgsqlDataAdapter(sorgu, connection);

            DataSet ds = new DataSet();

            da.Fill(ds);

            dtgw_Hizmetler.DataSource = ds.Tables[0];

            connection.Close();
            ///////////////
        }

        private void button5_Click(object sender, EventArgs e)
        {
            Form1 form1 = new Form1();

            this.Close(); // veya this.Close();

            form1.Show();
        }

    }
}
