namespace NDP_1.Proje_1.Bahar_Yarıyılı
{
    partial class Form1
    {
        /// <summary>
        ///  Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        ///  Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        ///  Required method for Designer support - do not modify
        ///  the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            btn_draw = new Button();
            pctbox_cizimalani = new PictureBox();
            comboBox1 = new ComboBox();
            btn_clear = new Button();
            lbl_carpismatürü = new Label();
            lbl_cisim1 = new Label();
            label1 = new Label();
            label2 = new Label();
            label3 = new Label();
            label4 = new Label();
            label5 = new Label();
            label6 = new Label();
            label7 = new Label();
            label8 = new Label();
            label9 = new Label();
            label10 = new Label();
            label11 = new Label();
            num_cisim1x = new NumericUpDown();
            num_cisim1y = new NumericUpDown();
            num_cisim1genislik = new NumericUpDown();
            num_cisim1yukseklik = new NumericUpDown();
            num_cisim1derinlik = new NumericUpDown();
            num_cisim2x = new NumericUpDown();
            num_cisim2y = new NumericUpDown();
            num_cisim2genislik = new NumericUpDown();
            num_cisim2yukseklik = new NumericUpDown();
            num_cisim2derinlik = new NumericUpDown();
            label12 = new Label();
            num_cisim1z = new NumericUpDown();
            label13 = new Label();
            num_cisim2z = new NumericUpDown();
            label14 = new Label();
            num_cisim1r = new NumericUpDown();
            label15 = new Label();
            num_cisim2r = new NumericUpDown();
            checkBox_cisim1hareket = new CheckBox();
            checkBox_cisim2hareket = new CheckBox();
            pctbox_kontrolisigi = new PictureBox();
            label16 = new Label();
            ((System.ComponentModel.ISupportInitialize)pctbox_cizimalani).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1x).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1y).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1genislik).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1yukseklik).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1derinlik).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2x).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2y).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2genislik).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2yukseklik).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2derinlik).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1z).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2z).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1r).BeginInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2r).BeginInit();
            ((System.ComponentModel.ISupportInitialize)pctbox_kontrolisigi).BeginInit();
            SuspendLayout();
            // 
            // btn_draw
            // 
            btn_draw.Location = new Point(612, 397);
            btn_draw.Name = "btn_draw";
            btn_draw.Size = new Size(140, 41);
            btn_draw.TabIndex = 0;
            btn_draw.Text = "Çizim Yap";
            btn_draw.UseVisualStyleBackColor = true;
            btn_draw.Click += btn_draw_Click;
            // 
            // pctbox_cizimalani
            // 
            pctbox_cizimalani.BorderStyle = BorderStyle.FixedSingle;
            pctbox_cizimalani.Location = new Point(0, 0);
            pctbox_cizimalani.Name = "pctbox_cizimalani";
            pctbox_cizimalani.Size = new Size(606, 438);
            pctbox_cizimalani.TabIndex = 1;
            pctbox_cizimalani.TabStop = false;
            // 
            // comboBox1
            // 
            comboBox1.FormattingEnabled = true;
            comboBox1.Items.AddRange(new object[] { "[1]Nokta ile Dörtgen", "[2]Noka ile Çember", "[3]Dikdörtgen ile Dikdörtgen", "[4]Dikdörtgen ile Çember", "[5]Çember ile Çember", "[6]Nokta ile Küre", "[7]Nokta ile Dikdört. Prizma", "[8]Nokta ile Silindir", "[9]Silindir ile Silindir", "[10]Küre ile Küre", "[11]Küre ile Silindir", "[12]Bir Yüzey ile Küre", "[13]Bir Yüzey ile Dikdört. Prizma", "[14]Bir Yüzey ile Silindir", "[15]Küre ile Dikdört. Prizma", "[16]Dikdört. Prizma ile Dikdört. Prizma" });
            comboBox1.Location = new Point(612, 18);
            comboBox1.Name = "comboBox1";
            comboBox1.Size = new Size(176, 23);
            comboBox1.TabIndex = 2;
            comboBox1.SelectedIndexChanged += comboBox1_SelectedIndexChanged;
            // 
            // btn_clear
            // 
            btn_clear.Location = new Point(611, 353);
            btn_clear.Name = "btn_clear";
            btn_clear.Size = new Size(141, 38);
            btn_clear.TabIndex = 3;
            btn_clear.Text = "Temizle";
            btn_clear.UseVisualStyleBackColor = true;
            btn_clear.Click += btn_clear_Click;
            // 
            // lbl_carpismatürü
            // 
            lbl_carpismatürü.AutoSize = true;
            lbl_carpismatürü.Location = new Point(624, 0);
            lbl_carpismatürü.Name = "lbl_carpismatürü";
            lbl_carpismatürü.Size = new Size(137, 15);
            lbl_carpismatürü.TabIndex = 4;
            lbl_carpismatürü.Text = "Çarpışma Türünü Seçiniz";
            // 
            // lbl_cisim1
            // 
            lbl_cisim1.AutoSize = true;
            lbl_cisim1.Location = new Point(612, 44);
            lbl_cisim1.Name = "lbl_cisim1";
            lbl_cisim1.Size = new Size(46, 15);
            lbl_cisim1.TabIndex = 8;
            lbl_cisim1.Text = "1.Cisim";
            // 
            // label1
            // 
            label1.AutoSize = true;
            label1.Location = new Point(612, 67);
            label1.Name = "label1";
            label1.Size = new Size(17, 15);
            label1.TabIndex = 9;
            label1.Text = "X:";
            // 
            // label2
            // 
            label2.AutoSize = true;
            label2.Location = new Point(693, 67);
            label2.Name = "label2";
            label2.Size = new Size(17, 15);
            label2.TabIndex = 10;
            label2.Text = "Y:";
            // 
            // label3
            // 
            label3.AutoSize = true;
            label3.Location = new Point(612, 199);
            label3.Name = "label3";
            label3.Size = new Size(46, 15);
            label3.TabIndex = 12;
            label3.Text = "2.Cisim";
            // 
            // label4
            // 
            label4.AutoSize = true;
            label4.Location = new Point(612, 221);
            label4.Name = "label4";
            label4.Size = new Size(17, 15);
            label4.TabIndex = 13;
            label4.Text = "X:";
            // 
            // label5
            // 
            label5.AutoSize = true;
            label5.Location = new Point(693, 221);
            label5.Name = "label5";
            label5.Size = new Size(17, 15);
            label5.TabIndex = 14;
            label5.Text = "Y:";
            // 
            // label6
            // 
            label6.AutoSize = true;
            label6.Location = new Point(611, 95);
            label6.Name = "label6";
            label6.Size = new Size(51, 15);
            label6.TabIndex = 18;
            label6.Text = "Genişlik:";
            // 
            // label7
            // 
            label7.AutoSize = true;
            label7.Location = new Point(612, 121);
            label7.Name = "label7";
            label7.Size = new Size(59, 15);
            label7.TabIndex = 19;
            label7.Text = "Yükseklik:";
            // 
            // label8
            // 
            label8.AutoSize = true;
            label8.Location = new Point(612, 147);
            label8.Name = "label8";
            label8.Size = new Size(50, 15);
            label8.TabIndex = 21;
            label8.Text = "Derinlik:";
            // 
            // label9
            // 
            label9.AutoSize = true;
            label9.Location = new Point(612, 248);
            label9.Name = "label9";
            label9.Size = new Size(51, 15);
            label9.TabIndex = 23;
            label9.Text = "Genişlik:";
            // 
            // label10
            // 
            label10.AutoSize = true;
            label10.Location = new Point(612, 276);
            label10.Name = "label10";
            label10.Size = new Size(59, 15);
            label10.TabIndex = 24;
            label10.Text = "Yükseklik:";
            // 
            // label11
            // 
            label11.AutoSize = true;
            label11.Location = new Point(612, 302);
            label11.Name = "label11";
            label11.Size = new Size(50, 15);
            label11.TabIndex = 25;
            label11.Text = "Derinlik:";
            // 
            // num_cisim1x
            // 
            num_cisim1x.Location = new Point(633, 64);
            num_cisim1x.Maximum = new decimal(new int[] { 606, 0, 0, 0 });
            num_cisim1x.Name = "num_cisim1x";
            num_cisim1x.Size = new Size(40, 23);
            num_cisim1x.TabIndex = 26;
            // 
            // num_cisim1y
            // 
            num_cisim1y.Location = new Point(712, 64);
            num_cisim1y.Maximum = new decimal(new int[] { 438, 0, 0, 0 });
            num_cisim1y.Name = "num_cisim1y";
            num_cisim1y.Size = new Size(40, 23);
            num_cisim1y.TabIndex = 27;
            // 
            // num_cisim1genislik
            // 
            num_cisim1genislik.Location = new Point(668, 93);
            num_cisim1genislik.Name = "num_cisim1genislik";
            num_cisim1genislik.Size = new Size(40, 23);
            num_cisim1genislik.TabIndex = 28;
            // 
            // num_cisim1yukseklik
            // 
            num_cisim1yukseklik.Location = new Point(668, 119);
            num_cisim1yukseklik.Name = "num_cisim1yukseklik";
            num_cisim1yukseklik.Size = new Size(40, 23);
            num_cisim1yukseklik.TabIndex = 29;
            // 
            // num_cisim1derinlik
            // 
            num_cisim1derinlik.Location = new Point(668, 145);
            num_cisim1derinlik.Minimum = new decimal(new int[] { 100, 0, 0, int.MinValue });
            num_cisim1derinlik.Name = "num_cisim1derinlik";
            num_cisim1derinlik.Size = new Size(40, 23);
            num_cisim1derinlik.TabIndex = 30;
            // 
            // num_cisim2x
            // 
            num_cisim2x.Location = new Point(633, 217);
            num_cisim2x.Maximum = new decimal(new int[] { 606, 0, 0, 0 });
            num_cisim2x.Name = "num_cisim2x";
            num_cisim2x.Size = new Size(40, 23);
            num_cisim2x.TabIndex = 31;
            // 
            // num_cisim2y
            // 
            num_cisim2y.Location = new Point(712, 219);
            num_cisim2y.Maximum = new decimal(new int[] { 438, 0, 0, 0 });
            num_cisim2y.Name = "num_cisim2y";
            num_cisim2y.Size = new Size(40, 23);
            num_cisim2y.TabIndex = 32;
            // 
            // num_cisim2genislik
            // 
            num_cisim2genislik.Location = new Point(668, 248);
            num_cisim2genislik.Name = "num_cisim2genislik";
            num_cisim2genislik.Size = new Size(40, 23);
            num_cisim2genislik.TabIndex = 33;
            // 
            // num_cisim2yukseklik
            // 
            num_cisim2yukseklik.Location = new Point(668, 274);
            num_cisim2yukseklik.Name = "num_cisim2yukseklik";
            num_cisim2yukseklik.Size = new Size(40, 23);
            num_cisim2yukseklik.TabIndex = 34;
            // 
            // num_cisim2derinlik
            // 
            num_cisim2derinlik.Location = new Point(668, 300);
            num_cisim2derinlik.Minimum = new decimal(new int[] { 100, 0, 0, int.MinValue });
            num_cisim2derinlik.Name = "num_cisim2derinlik";
            num_cisim2derinlik.Size = new Size(40, 23);
            num_cisim2derinlik.TabIndex = 35;
            // 
            // label12
            // 
            label12.AutoSize = true;
            label12.Location = new Point(771, 69);
            label12.Name = "label12";
            label12.Size = new Size(17, 15);
            label12.TabIndex = 38;
            label12.Text = "Z:";
            // 
            // num_cisim1z
            // 
            num_cisim1z.Location = new Point(794, 65);
            num_cisim1z.Minimum = new decimal(new int[] { 100, 0, 0, int.MinValue });
            num_cisim1z.Name = "num_cisim1z";
            num_cisim1z.Size = new Size(40, 23);
            num_cisim1z.TabIndex = 39;
            // 
            // label13
            // 
            label13.AutoSize = true;
            label13.Location = new Point(771, 221);
            label13.Name = "label13";
            label13.Size = new Size(17, 15);
            label13.TabIndex = 40;
            label13.Text = "Z:";
            // 
            // num_cisim2z
            // 
            num_cisim2z.Location = new Point(794, 217);
            num_cisim2z.Minimum = new decimal(new int[] { 100, 0, 0, int.MinValue });
            num_cisim2z.Name = "num_cisim2z";
            num_cisim2z.Size = new Size(40, 23);
            num_cisim2z.TabIndex = 41;
            // 
            // label14
            // 
            label14.AutoSize = true;
            label14.Location = new Point(612, 174);
            label14.Name = "label14";
            label14.Size = new Size(48, 15);
            label14.TabIndex = 42;
            label14.Text = "Yarıçap:";
            // 
            // num_cisim1r
            // 
            num_cisim1r.Location = new Point(668, 172);
            num_cisim1r.Name = "num_cisim1r";
            num_cisim1r.Size = new Size(40, 23);
            num_cisim1r.TabIndex = 43;
            // 
            // label15
            // 
            label15.AutoSize = true;
            label15.Location = new Point(612, 331);
            label15.Name = "label15";
            label15.Size = new Size(48, 15);
            label15.TabIndex = 44;
            label15.Text = "Yarıçap:";
            // 
            // num_cisim2r
            // 
            num_cisim2r.Location = new Point(668, 329);
            num_cisim2r.Name = "num_cisim2r";
            num_cisim2r.Size = new Size(40, 23);
            num_cisim2r.TabIndex = 45;
            // 
            // checkBox_cisim1hareket
            // 
            checkBox_cisim1hareket.AutoSize = true;
            checkBox_cisim1hareket.Location = new Point(728, 97);
            checkBox_cisim1hareket.Name = "checkBox_cisim1hareket";
            checkBox_cisim1hareket.Size = new Size(133, 19);
            checkBox_cisim1hareket.TabIndex = 46;
            checkBox_cisim1hareket.Text = "1.Cismi Hareket Ettir";
            checkBox_cisim1hareket.UseVisualStyleBackColor = true;
            checkBox_cisim1hareket.CheckedChanged += checkBox_cisim1hareket_CheckedChanged;
            // 
            // checkBox_cisim2hareket
            // 
            checkBox_cisim2hareket.AutoSize = true;
            checkBox_cisim2hareket.Location = new Point(728, 249);
            checkBox_cisim2hareket.Name = "checkBox_cisim2hareket";
            checkBox_cisim2hareket.Size = new Size(133, 19);
            checkBox_cisim2hareket.TabIndex = 47;
            checkBox_cisim2hareket.Text = "2.Cismi Hareket Ettir";
            checkBox_cisim2hareket.UseVisualStyleBackColor = true;
            checkBox_cisim2hareket.CheckedChanged += checkBox_cisim2hareket_CheckedChanged;
            // 
            // pctbox_kontrolisigi
            // 
            pctbox_kontrolisigi.BorderStyle = BorderStyle.FixedSingle;
            pctbox_kontrolisigi.Location = new Point(775, 376);
            pctbox_kontrolisigi.Name = "pctbox_kontrolisigi";
            pctbox_kontrolisigi.Size = new Size(59, 50);
            pctbox_kontrolisigi.TabIndex = 48;
            pctbox_kontrolisigi.TabStop = false;
            // 
            // label16
            // 
            label16.AutoSize = true;
            label16.Location = new Point(757, 353);
            label16.Name = "label16";
            label16.Size = new Size(104, 15);
            label16.TabIndex = 49;
            label16.Text = "Çarpışma Durumu";
            // 
            // Form1
            // 
            AutoScaleDimensions = new SizeF(7F, 15F);
            AutoScaleMode = AutoScaleMode.Font;
            ClientSize = new Size(862, 438);
            Controls.Add(label16);
            Controls.Add(pctbox_kontrolisigi);
            Controls.Add(checkBox_cisim2hareket);
            Controls.Add(checkBox_cisim1hareket);
            Controls.Add(num_cisim2r);
            Controls.Add(label15);
            Controls.Add(num_cisim1r);
            Controls.Add(label14);
            Controls.Add(num_cisim2z);
            Controls.Add(label13);
            Controls.Add(num_cisim1z);
            Controls.Add(label12);
            Controls.Add(num_cisim2derinlik);
            Controls.Add(num_cisim2yukseklik);
            Controls.Add(num_cisim2genislik);
            Controls.Add(num_cisim2y);
            Controls.Add(num_cisim2x);
            Controls.Add(num_cisim1derinlik);
            Controls.Add(num_cisim1yukseklik);
            Controls.Add(num_cisim1genislik);
            Controls.Add(num_cisim1y);
            Controls.Add(num_cisim1x);
            Controls.Add(label11);
            Controls.Add(label10);
            Controls.Add(label9);
            Controls.Add(label8);
            Controls.Add(label7);
            Controls.Add(label6);
            Controls.Add(label5);
            Controls.Add(label4);
            Controls.Add(label3);
            Controls.Add(label2);
            Controls.Add(label1);
            Controls.Add(lbl_cisim1);
            Controls.Add(lbl_carpismatürü);
            Controls.Add(btn_clear);
            Controls.Add(comboBox1);
            Controls.Add(pctbox_cizimalani);
            Controls.Add(btn_draw);
            MaximumSize = new Size(878, 477);
            MinimumSize = new Size(878, 477);
            Name = "Form1";
            StartPosition = FormStartPosition.CenterScreen;
            Text = "Cisimlerin Çarpışma Simülasyonu";
            Load += Form1_Load;
            KeyDown += Form1_KeyDown;
            ((System.ComponentModel.ISupportInitialize)pctbox_cizimalani).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1x).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1y).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1genislik).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1yukseklik).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1derinlik).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2x).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2y).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2genislik).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2yukseklik).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2derinlik).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1z).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2z).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim1r).EndInit();
            ((System.ComponentModel.ISupportInitialize)num_cisim2r).EndInit();
            ((System.ComponentModel.ISupportInitialize)pctbox_kontrolisigi).EndInit();
            ResumeLayout(false);
            PerformLayout();
        }

        #endregion

        private Button btn_draw;
        private PictureBox pctbox_cizimalani;
        private ComboBox comboBox1;
        private Button btn_clear;
        private Label lbl_carpismatürü;
        private Label lbl_cisim1;
        private Label label1;
        private Label label2;
        private Label label3;
        private Label label4;
        private Label label5;
        private Label label6;
        private Label label7;
        private Label label8;
        private Label label9;
        private Label label10;
        private Label label11;
        private NumericUpDown num_cisim1x;
        private NumericUpDown num_cisim1y;
        private NumericUpDown num_cisim1genislik;
        private NumericUpDown num_cisim1yukseklik;
        private NumericUpDown num_cisim1derinlik;
        private NumericUpDown num_cisim2x;
        private NumericUpDown num_cisim2y;
        private NumericUpDown num_cisim2genislik;
        private NumericUpDown num_cisim2yukseklik;
        private NumericUpDown num_cisim2derinlik;
        private Label label12;
        private NumericUpDown num_cisim1z;
        private Label label13;
        private NumericUpDown num_cisim2z;
        private Label label14;
        private NumericUpDown num_cisim1r;
        private Label label15;
        private NumericUpDown num_cisim2r;
        private CheckBox checkBox_cisim1hareket;
        private CheckBox checkBox_cisim2hareket;
        private PictureBox pctbox_kontrolisigi;
        private Label label16;
    }
}