/*****************************************************************************************************
*                                                                                                    *
*                                          SAKARYA ÜNİVERSİTESİ                                      *
*                                BİLGİSAYAR VE BİLİŞİM BİLİMLERİ FAKÜLTESİ                           *
*                                     BİLGİSAYAR MÜHENDİSLİĞİ BÖLÜMÜ                                 *
*                                    NESNEYE DAYALI PROGRAMLAMA DERSİ                                *
*                                                                                                    *
*                                       ÖDEV (PROJE) NUMARASI : 1                                    *
*                                       ÖĞRENCİ ADI : ALİ KEREM KOL                                  *
*                                       ÖĞRENCİ NUMARASI : B221210042                                *
*                                       DERS GRUBU : 1.ÖĞRETİM B GRUBU                               *
*                              YOUTUBE LİNKİ :  https://youtu.be/3f8URattUBQ                         *
*                                                                                                    *
******************************************************************************************************/

using System.Diagnostics.CodeAnalysis;
using System.Drawing;
using System.Runtime.InteropServices;
using System.Windows.Forms;
using System.Drawing;
using System.Numerics;
using System.Security.Cryptography.X509Certificates;
using System.Drawing.Drawing2D;
using System.Runtime.CompilerServices;
using System.Drawing.Design;

namespace NDP_1.Proje_1.Bahar_Yarıyılı
{
    public partial class Form1 : Form
    {
        public static class Metodlar
        {
            //
            public static bool NoktaDortgenCarpmaKontrol(double noktaX, double noktaY, double dortgenSolUstX, double dortgenSolUstY, double dortgenGenislik, double dortgenYukseklik)
            {
                double dortgenSagAltX = dortgenSolUstX + dortgenGenislik;
                double dortgenSagAltY = dortgenSolUstY + dortgenYukseklik;

                if (noktaX >= dortgenSolUstX && noktaX <= dortgenSagAltX && noktaY >= dortgenSolUstY && noktaY <= dortgenSagAltY)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            //
            public static bool NoktaCemberCarpmaKontrol(double noktaX, double noktaY, double cemberx, double cembery, double cemberyaricap)
            {
                double uzunluk = Math.Sqrt(Math.Pow(noktaX - cemberx, 2) + Math.Pow(noktaY - cembery, 2));

                if (uzunluk <= cemberyaricap)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            //
            public static bool DikdortgenDikdortgenCarpmaKontrol(double dortgen1SolUstX, double dortgen1SolUstY, double dortgen1Genislik, double dortgen1Yukseklik, double dortgen2SolUstX, double dortgen2SolUstY, double dortgen2Genislik, double dortgen2Yukseklik)
            {
                double dortgen1SagAltX = dortgen1SolUstX + dortgen1Genislik;
                double dortgen1SagAltY = dortgen1SolUstY + dortgen1Yukseklik;

                double dortgen2SagAltX = dortgen2SolUstX + dortgen2Genislik;
                double dortgen2SagAltY = dortgen2SolUstY + dortgen2Yukseklik;

                if (dortgen1SolUstX < dortgen2SagAltX && dortgen1SagAltX > dortgen2SolUstX && dortgen1SolUstY < dortgen2SagAltY && dortgen1SagAltY > dortgen2SolUstY)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            //
            public static bool DikdortgenCemberCarpmaKontrol(double ddortgenX, double ddortgenY, double ddortgengenislik, double ddortgenyukseklik, double cemberX, double cemberY, double cemberyaricapi)
            {
                double cembermesafesiX = Math.Abs(cemberX - (ddortgenX + ddortgengenislik / 2));
                double cembermesafesiY = Math.Abs(cemberY - (ddortgenY + ddortgenyukseklik / 2));

                if (cembermesafesiX > (ddortgengenislik / 2 + cemberyaricapi))
                {
                    return false;
                }
                if (cembermesafesiY > (ddortgenyukseklik / 2 + cemberyaricapi))
                {
                    return false;
                }
                if (cembermesafesiX <= (ddortgengenislik / 2))
                {
                    return true;
                }
                if (cembermesafesiY <= (ddortgenyukseklik / 2))
                {
                    return true;
                }

                double kosemesafesi = Math.Pow(cembermesafesiX - ddortgengenislik / 2, 2) + Math.Pow(cembermesafesiY - ddortgenyukseklik / 2, 2);
                return (kosemesafesi <= Math.Pow(cemberyaricapi, 2));
            }
            //
            public static bool CemberCemberCarpmaKontrol(double cember1X, double cember1Y, double cember1yaricap, double cember2X, double cember2Y, double cember2yaricap)
            {
                double merkezlerarasimesafe = Math.Sqrt(Math.Pow(cember1X - cember2X, 2) + Math.Pow(cember1Y - cember2Y, 2));

                if (merkezlerarasimesafe <= cember1yaricap + cember2yaricap)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            //
            public static bool NoktaKureCarpmaKontrol(double noktaX, double noktaY, double noktaZ, double kureX, double kureY, double kureZ, double kureyaricap)
            {
                double merkezlerarasimesafe = Math.Sqrt(Math.Pow(noktaX - kureX, 2) + Math.Pow(noktaY - kureY, 2) + Math.Pow(noktaZ - kureZ, 2));

                if (merkezlerarasimesafe <= kureyaricap)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            //
            public static bool NoktaDikdortgenPrizmaCarpmaKontrol(float noktaX, float noktaY, float noktaZ, float dprizmaX, float dprizmaY, float dprizmaZ, float dprizmaGenislik, float dprizmaYukseklik, float dprizmaDerinlik)
            {
                float dprizmaMinX = dprizmaX;
                float dprizmaMaxX = dprizmaX + dprizmaGenislik;
                float dprizmaMinY = dprizmaY;
                float dprizmaMaxY = dprizmaY + dprizmaYukseklik;
                float dprizmaMinZ = dprizmaZ;
                float dprizmaMaxZ = dprizmaZ + dprizmaDerinlik;

                if (noktaX < dprizmaMinX || noktaX > dprizmaMaxX)
                {
                    return false;
                }
                if (noktaY < dprizmaMinY || noktaY > dprizmaMaxY)
                {
                    return false;
                }
                if (noktaZ < dprizmaMinZ || noktaZ > dprizmaMaxZ)
                {
                    return false;
                }
                else
                {
                    return true;
                }
            }
            //
            public static bool NoktaSilindirCarpmaKontrol(float noktaX, float noktaY, float noktaZ, float silindirSolUstX, float silindirSolUstY, float silindirSolUstZ, float silindirRadius, float silindirYukseklik)
            {
                float silindirMerkezX = silindirSolUstX + silindirRadius;
                float silindirMerkezY = silindirSolUstY + silindirYukseklik / 2;
                float silindirMerkezZ = silindirSolUstZ;

                if (noktaZ >= silindirMerkezZ - silindirRadius && noktaZ <= silindirMerkezZ + silindirRadius && silindirSolUstX < noktaX && noktaX < (silindirSolUstX + silindirRadius * 2) && noktaY > silindirSolUstY && noktaY < silindirSolUstY + silindirYukseklik)
                {
                    return true;
                }

                return false;
            }
            //
            public static bool SilindirSilindirCarpmaKontrol(float silindir1SolUstX, float silindir1SolUstY, float silindir1SolUstZ, float silindir1Yaricap, float silindir1Yukseklik, float silindir2SolUstX, float silindir2SolUstY, float silindir2SolUstZ, float silindir2Yaricap, float silindir2Yukseklik)
            {
                float mesafe = Math.Abs(silindir1SolUstZ - silindir2SolUstZ);
                float minimumMesafe = silindir1Yaricap + silindir2Yaricap;

                if (mesafe <= minimumMesafe && silindir1SolUstX + silindir1Yaricap * 2 >= silindir2SolUstX && silindir2SolUstX + silindir2Yaricap * 2 >= silindir1SolUstX && silindir1SolUstY <= silindir2SolUstY + silindir2Yukseklik && silindir2SolUstY <= silindir1SolUstY + silindir1Yukseklik)
                {
                    return true;
                }

                return false;
            }
            //
            public static bool KureKureCarpisma(float x1, float y1, float z1, float r1, float x2, float y2, float z2, float r2)
            {

                float dx = x2 - x1;
                float dy = y2 - y1;
                float dz = z2 - z1;
                float mesafe = (float)Math.Sqrt(dx * dx + dy * dy + dz * dz);


                float toplamyaricap = r1 + r2;


                if (mesafe <= toplamyaricap)
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            //
            public static bool KureSilindirCarpmaKontrol(double kureX, double kureY, double kureZ, double kureyaricapi, double silindirX, double silindirY, double silindirZ, double silindiryaricapi, double silindiryukseklik)
            {
                double cembermesafesiX = Math.Abs(kureX - (silindirX + silindiryaricapi / 2));
                double cembermesafesiY = Math.Abs(kureY - (silindirY + silindiryukseklik / 2));

                if (Math.Abs(silindirZ - kureZ) <= (silindiryaricapi + kureyaricapi))
                {
                    if (cembermesafesiX > (silindiryaricapi / 2 + kureyaricapi))
                    {
                        return false;
                    }
                    if (cembermesafesiY > (silindiryukseklik / 2 + kureyaricapi))
                    {
                        return false;
                    }
                    if (cembermesafesiX <= (silindiryaricapi / 2))
                    {
                        return true;
                    }
                    if (cembermesafesiY <= (silindiryukseklik / 2))
                    {
                        return true;
                    }
                }
                else
                {
                    return false;
                }
                double kosemesafesi = Math.Pow(cembermesafesiX - silindiryaricapi / 2, 2) + Math.Pow(cembermesafesiY - silindiryukseklik / 2, 2);
                return (kosemesafesi <= Math.Pow(kureyaricapi, 2));
            }
            //
            public static bool YuzeyKureCarpmaKontrol(double yuzeyX, double yuzeyY, double yuzeyZ, double yuzeygenislik, double yuzeyyukseklik, double kureX, double kureY, double kureZ, double kureyaricapi)
            {
                double cembermesafesiX = Math.Abs(kureX - (yuzeyX + yuzeygenislik / 2));
                double cembermesafesiY = Math.Abs(kureY - (yuzeyY + yuzeyyukseklik / 2));


                if (Math.Abs(yuzeyZ - kureZ) <= kureyaricapi)
                {
                    if (cembermesafesiX > (yuzeygenislik / 2 + kureyaricapi))
                    {
                        return false;
                    }
                    if (cembermesafesiY > (yuzeyyukseklik / 2 + kureyaricapi))
                    {
                        return false;
                    }
                    if (cembermesafesiX <= (yuzeygenislik / 2))
                    {
                        return true;
                    }
                    if (cembermesafesiY <= (yuzeyyukseklik / 2))
                    {
                        return true;
                    }
                }
                else
                {
                    return false;
                }
                double kosemesafesi = Math.Pow(cembermesafesiX - yuzeygenislik / 2, 2) + Math.Pow(cembermesafesiY - yuzeyyukseklik / 2, 2);
                return (kosemesafesi <= Math.Pow(kureyaricapi, 2));
            }
            //
            public static bool YuzeyDikdortgenPrizmaCarpmaKontrol(double yuzeySolUstX, double yuzeySolUstY, double yuzeySolUstZ, double yuzeyGenislik, double yuzeyYukseklik, double prizmaSolUstX, double prizmaSolUstY, double prizmaSolUstZ, double prizmaGenislik, double prizmaYukseklik, double prizmaDerinlik)
            {
                double dikdortgenSagAltX = yuzeySolUstX + yuzeyGenislik;
                double dikdortgenSagAltY = yuzeySolUstY + yuzeyYukseklik;
                double dikdortgenSagAltZ = yuzeySolUstZ;

                double prizmaSagAltX = prizmaSolUstX + prizmaGenislik;
                double prizmaSagAltY = prizmaSolUstY + prizmaYukseklik;
                double prizmaSagAltZ = prizmaSolUstZ + prizmaDerinlik;


                if (yuzeySolUstX > prizmaSagAltX || dikdortgenSagAltX < prizmaSolUstX)
                    return false;


                if (yuzeySolUstY > prizmaSagAltY || dikdortgenSagAltY < prizmaSolUstY)
                    return false;


                if (yuzeySolUstZ > prizmaSagAltZ || dikdortgenSagAltZ < prizmaSolUstZ)
                    return false;

                return true; // Çarpışma var
            }
            //
            public static bool YuzeySilindirCarpmaKontrol(float dikdortgenSolUstX, float dikdortgenSolUstY, float dikdortgenSolUstZ, float dikdortgenGenislik, float dikdortgenYukseklik, float silindirSolUstX, float silindirSolUstY, float silindirSolUstZ, float silindirYaricap, float silindirYukseklik)
            {

                float dikdortgenSagAltX = dikdortgenSolUstX + dikdortgenGenislik;
                float dikdortgenSagAltY = dikdortgenSolUstY + dikdortgenYukseklik;


                float silindirSagAltX = silindirSolUstX + 2 * silindirYaricap;
                float silindirSagAltY = silindirSolUstY + silindirYukseklik;


                if (dikdortgenSolUstX > silindirSagAltX || dikdortgenSagAltX < silindirSolUstX || dikdortgenSolUstY > silindirSagAltY || dikdortgenSagAltY < silindirSolUstY)
                {
                    return false;
                }


                float dikdortgenAltZ = dikdortgenSolUstZ;
                float dikdortgenUstZ = dikdortgenSolUstZ;


                float silindirAltZ = silindirSolUstZ - silindirYaricap;
                float silindirUstZ = silindirSolUstZ + silindirYaricap;


                if (dikdortgenAltZ > silindirUstZ || dikdortgenSolUstZ < silindirAltZ)
                {
                    return false;
                }

                return true;
            }
            //
            public static bool KureDikPrizmaCarpmaKontrol(double kureMerkezX, double kureMerkezY, double kureMerkezZ, double kureYaricap, double prizmaSolUstX, double prizmaSolUstY, double prizmaSolUstZ, double prizmaGenislik, double prizmaYukseklik, double prizmaDerinlik)
            {
                double prizmaSagAltX = prizmaSolUstX + prizmaGenislik;
                double prizmaSagAltY = prizmaSolUstY + prizmaYukseklik;
                double prizmaSagAltZ = prizmaSolUstZ + prizmaDerinlik;


                double enYakinX = Math.Max(prizmaSolUstX, Math.Min(kureMerkezX, prizmaSagAltX));
                double enYakinY = Math.Max(prizmaSolUstY, Math.Min(kureMerkezY, prizmaSagAltY));
                double enYakinZ = Math.Max(prizmaSolUstZ, Math.Min(kureMerkezZ, prizmaSagAltZ));


                double mesafeX = enYakinX - kureMerkezX;
                double mesafeY = enYakinY - kureMerkezY;
                double mesafeZ = enYakinZ - kureMerkezZ;
                double mesafeKare = mesafeX * mesafeX + mesafeY * mesafeY + mesafeZ * mesafeZ;


                return mesafeKare <= (kureYaricap * kureYaricap);
            }
            //
            public static bool DikPrizmaDikPrizmaCarpmaKontrol(float x1, float y1, float z1, float genislik1, float yukseklik1, float derinlik1, float x2, float y2, float z2, float genislik2, float yukseklik2, float derinlik2)
            {

                float yarimgenislik1 = genislik1 / 2f;
                float yarimyukseklik1 = yukseklik1 / 2f;
                float yarimderinlik1 = derinlik1 / 2f;
                float yarimgenislik2 = genislik2 / 2f;
                float yarimyukseklik2 = yukseklik2 / 2f;
                float yarimderinlik2 = derinlik2 / 2f;


                float merkezX1 = x1 + yarimgenislik1;
                float merkezY1 = y1 + yarimyukseklik1;
                float merkezZ1 = z1 + yarimderinlik1;
                float merkezX2 = x2 + yarimgenislik2;
                float merkezY2 = y2 + yarimyukseklik2;
                float merkezZ2 = z2 + yarimderinlik2;


                float mesfX = Math.Abs(merkezX2 - merkezX1);
                float mesfY = Math.Abs(merkezY2 - merkezY1);
                float mesfZ = Math.Abs(merkezZ2 - merkezZ1);


                if (mesfX > (yarimgenislik1 + yarimgenislik2)) return false;
                if (mesfY > (yarimyukseklik1 + yarimyukseklik2)) return false;
                if (mesfZ > (yarimderinlik1 + yarimderinlik2)) return false;


                return true;
            }

        }


        class Nokta
        {
            public float X { get; set; }
            public float Y { get; set; }

            public float Z { get; set; }
            public Nokta()
            {
                X = 0;
                Y = 0;
                Z = 0;
            }

            public Nokta(float x, float y, float z)
            {
                X = x;
                Y = y;
                Z = z;
            }
        }
        class Dikdortgen
        {
            public int X { get; set; }
            public int Y { get; set; }
            public int genislik { get; set; }
            public int yukseklik { get; set; }

            public Dikdortgen(int x, int y, int width, int height)
            {
                X = x;
                Y = y;
                genislik = width;
                yukseklik = height;
            }
        }
        class Cember
        {
            public int X { get; set; }
            public int Y { get; set; }
            public int Radius { get; set; }

            public Cember(int x, int y, int radius)
            {
                X = x;
                Y = y;
                Radius = radius;
            }
        }
        class Silindir
        {
            public int X { get; set; }
            public int Y { get; set; }
            public int Z { get; set; }
            public int Radius { get; set; }
            public int Height { get; set; }

            public Silindir(int x, int y, int z, int radius, int height)
            {
                X = x;
                Y = y;
                Z = z;
                Radius = radius;
                Height = height;
            }
        }
        class Kure
        {
            public int X { get; set; }
            public int Y { get; set; }
            public int Z { get; set; }
            public int Radius { get; set; }

            public Kure(int x, int y, int z, int radius)
            {
                X = x;
                Y = y;
                Z = z;
                Radius = radius;
            }
        }
        class DikdortgenPrizma
        {
            public int X { get; set; }
            public int Y { get; set; }
            public int Z { get; set; }
            public int Width { get; set; }
            public int Height { get; set; }
            public int Depth { get; set; }

            public DikdortgenPrizma(int x, int y, int z, int width, int height, int depth)
            {
                X = x;
                Y = y;
                Z = z;
                Width = width;
                Height = height;
                Depth = depth;
            }
        }
        class Yuzey
        {
            public float NoktaX { get; set; }
            public float NoktaY { get; set; }
            public float NoktaZ { get; set; }
            public float Genislik { get; set; }
            public float Yukseklik { get; set; }

            public Yuzey(float noktaX, float noktaY, float noktaZ, float genislik, float yukseklik)
            {
                NoktaX = noktaX;
                NoktaY = noktaY;
                NoktaZ = noktaZ;
                Genislik = genislik;
                Yukseklik = yukseklik;
            }
        }



        Dictionary<int, object> Cisimler = new Dictionary<int, object>();



        Nokta kalipnokta = new Nokta(0, 0, 0);
        Dikdortgen kalipdikdortgen = new Dikdortgen(0, 0, 0, 0);
        Cember kalipcember = new Cember(0, 0, 0);
        Silindir kalipsilindir = new Silindir(0, 0, 0, 0, 0);
        Kure kalipkure = new Kure(0, 0, 0, 0);
        DikdortgenPrizma kalipdikdörtgenprizma = new DikdortgenPrizma(0, 0, 0, 0, 0, 0);
        Yuzey kalipyuzey = new Yuzey(0, 0, 0, 0, 0);


        public Form1()
        {
            InitializeComponent();
        }

        int secilenindex = -1;

        private void btn_clear_Click(object sender, EventArgs e)
        {
            pctbox_cizimalani.Refresh();
            pctbox_kontrolisigi.BackColor = default;
            num_cisim1x.Value = 0;
            num_cisim1y.Value = 0;
            num_cisim1z.Value = 0;
            num_cisim1genislik.Value = 0;
            num_cisim1yukseklik.Value = 0;
            num_cisim1derinlik.Value = 0;
            num_cisim1r.Value = 0;
            /////////////////////////////////
            num_cisim2x.Value = 0;
            num_cisim2y.Value = 0;
            num_cisim2z.Value = 0;
            num_cisim2genislik.Value = 0;
            num_cisim2yukseklik.Value = 0;
            num_cisim2derinlik.Value = 0;
            num_cisim2r.Value = 0;
            /////////////////////////////////
            checkBox_cisim1hareket.Checked = false;
            checkBox_cisim2hareket.Checked = false;
        }

        private void btn_draw_Click(object sender, EventArgs e)
        {
            pctbox_cizimalani.Refresh();
            if (secilenindex == 0)
            {
                Nokta tempnokta = (Nokta)Cisimler[0];
                tempnokta.X = Convert.ToInt32(num_cisim1x.Value);
                tempnokta.Y = Convert.ToInt32(num_cisim1y.Value);
                using (Graphics grafik = Graphics.FromHwnd(pctbox_cizimalani.Handle))
                {
                    using (SolidBrush kalem = new SolidBrush(Color.DarkBlue))
                    {
                        grafik.FillRectangle(kalem, tempnokta.X, tempnokta.Y, 1, 1);
                    }
                }

                Dikdortgen tempdikdortgen = (Dikdortgen)Cisimler[1];
                tempdikdortgen.X = Convert.ToInt32(num_cisim2x.Value);
                tempdikdortgen.Y = Convert.ToInt32(num_cisim2y.Value);
                tempdikdortgen.genislik = Convert.ToInt32(num_cisim2genislik.Value);
                tempdikdortgen.yukseklik = Convert.ToInt32(num_cisim2yukseklik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgen.X, tempdikdortgen.Y, tempdikdortgen.genislik, tempdikdortgen.yukseklik);
                }
                if (Metodlar.NoktaDortgenCarpmaKontrol(tempnokta.X, tempnokta.Y, tempdikdortgen.X, tempdikdortgen.Y, tempdikdortgen.genislik, tempdikdortgen.yukseklik))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Nokta - Dikdörtgen


            if (secilenindex == 1)
            {

                Nokta tempnokta = (Nokta)Cisimler[0];
                tempnokta.X = Convert.ToInt32(num_cisim1x.Value);
                tempnokta.Y = Convert.ToInt32(num_cisim1y.Value);
                using (Graphics grafik = Graphics.FromHwnd(pctbox_cizimalani.Handle))
                {
                    using (SolidBrush kalem = new SolidBrush(Color.DarkBlue))
                    {
                        grafik.FillRectangle(kalem, tempnokta.X, tempnokta.Y, 1, 1);
                    }
                }

                Cember tempcember = (Cember)Cisimler[2];
                tempcember.X = Convert.ToInt32(num_cisim2x.Value);
                tempcember.Y = Convert.ToInt32(num_cisim2y.Value);
                tempcember.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawEllipse(Pens.DarkBlue, tempcember.X - tempcember.Radius, tempcember.Y - tempcember.Radius, tempcember.Radius * 2, tempcember.Radius * 2);

                }
                if (Metodlar.NoktaCemberCarpmaKontrol(tempnokta.X, tempnokta.Y, tempcember.X, tempcember.Y, tempcember.Radius))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Nokta - Çember 


            if (secilenindex == 2)
            {
                Dikdortgen tempdikdortgen = (Dikdortgen)Cisimler[1];
                tempdikdortgen.X = Convert.ToInt32(num_cisim1x.Value);
                tempdikdortgen.Y = Convert.ToInt32(num_cisim1y.Value);
                tempdikdortgen.genislik = Convert.ToInt32(num_cisim1genislik.Value);
                tempdikdortgen.yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgen.X, tempdikdortgen.Y, tempdikdortgen.genislik, tempdikdortgen.yukseklik);

                }

                int ddortgenx = Convert.ToInt32(num_cisim1x.Value);
                int ddortgeny = Convert.ToInt32(num_cisim1y.Value);
                int ddortgengenislik = Convert.ToInt32(num_cisim1genislik.Value);
                int ddortgenyukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);

                Dikdortgen tempdikdortgen2 = (Dikdortgen)Cisimler[1];
                tempdikdortgen2.X = Convert.ToInt32(num_cisim2x.Value);
                tempdikdortgen2.Y = Convert.ToInt32(num_cisim2y.Value);
                tempdikdortgen2.genislik = Convert.ToInt32(num_cisim2genislik.Value);
                tempdikdortgen2.yukseklik = Convert.ToInt32(num_cisim2yukseklik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgen2.X, tempdikdortgen2.Y, tempdikdortgen2.genislik, tempdikdortgen2.yukseklik);
                }
                if (Metodlar.DikdortgenDikdortgenCarpmaKontrol(ddortgenx, ddortgeny, ddortgengenislik, ddortgenyukseklik, tempdikdortgen2.X, tempdikdortgen2.Y, tempdikdortgen2.genislik, tempdikdortgen2.yukseklik))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Dikdörtgen - Dikdörtgen


            if (secilenindex == 3)
            {
                Dikdortgen tempdikdortgen = (Dikdortgen)Cisimler[1];
                tempdikdortgen.X = Convert.ToInt32(num_cisim1x.Value);
                tempdikdortgen.Y = Convert.ToInt32(num_cisim1y.Value);
                tempdikdortgen.genislik = Convert.ToInt32(num_cisim1genislik.Value);
                tempdikdortgen.yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgen.X, tempdikdortgen.Y, tempdikdortgen.genislik, tempdikdortgen.yukseklik);
                }

                Cember tempcember = (Cember)Cisimler[2];
                tempcember.X = Convert.ToInt32(num_cisim2x.Value);
                tempcember.Y = Convert.ToInt32(num_cisim2y.Value);
                tempcember.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawEllipse(Pens.DarkBlue, tempcember.X - tempcember.Radius, tempcember.Y - tempcember.Radius, tempcember.Radius * 2, tempcember.Radius * 2);

                }
                if (Metodlar.DikdortgenCemberCarpmaKontrol(tempdikdortgen.X, tempdikdortgen.Y, tempdikdortgen.genislik, tempdikdortgen.yukseklik, tempcember.X, tempcember.Y, tempcember.Radius))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Dikdörtgen - Çember


            if (secilenindex == 4)
            {
                Cember tempcember = (Cember)Cisimler[2];
                tempcember.X = Convert.ToInt32(num_cisim1x.Value);
                tempcember.Y = Convert.ToInt32(num_cisim1y.Value);
                tempcember.Radius = Convert.ToInt32(num_cisim1r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawEllipse(Pens.DarkBlue, tempcember.X - tempcember.Radius, tempcember.Y - tempcember.Radius, tempcember.Radius * 2, tempcember.Radius * 2);

                }
                //dikdörtgen - dikdörtgendeki aynı mevzu
                int cemberx = Convert.ToInt32(num_cisim1x.Value);
                int cembery = Convert.ToInt32(num_cisim1y.Value);
                int cemberr = Convert.ToInt32(num_cisim1r.Value);

                Cember tempcember2 = (Cember)Cisimler[2];
                tempcember2.X = Convert.ToInt32(num_cisim2x.Value);
                tempcember2.Y = Convert.ToInt32(num_cisim2y.Value);
                tempcember2.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawEllipse(Pens.DarkBlue, tempcember2.X - tempcember2.Radius, tempcember2.Y - tempcember2.Radius, tempcember2.Radius * 2, tempcember2.Radius * 2);

                }
                if (Metodlar.CemberCemberCarpmaKontrol(cemberx, cembery, cemberr, tempcember2.X, tempcember2.Y, tempcember2.Radius))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Çember - Çember


            if (secilenindex == 5)
            {
                Nokta tempnokta = (Nokta)Cisimler[0];
                tempnokta.X = Convert.ToInt32(num_cisim1x.Value);
                tempnokta.Y = Convert.ToInt32(num_cisim1y.Value);
                using (Graphics grafik = Graphics.FromHwnd(pctbox_cizimalani.Handle))
                {
                    using (SolidBrush kalem = new SolidBrush(Color.DarkBlue))
                    {
                        grafik.FillRectangle(kalem, tempnokta.X, tempnokta.Y, 1, 1);
                    }
                }

                Kure tempkure = (Kure)Cisimler[4];
                tempkure.X = Convert.ToInt32(num_cisim2x.Value);
                tempkure.Y = Convert.ToInt32(num_cisim2y.Value);
                tempkure.Z = Convert.ToInt32(num_cisim2z.Value);
                tempkure.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    Brush brush = new SolidBrush(Color.DarkBlue);
                    g.FillEllipse(brush, tempkure.X - tempkure.Radius, tempkure.Y - tempkure.Radius, tempkure.Radius * 2, tempkure.Radius * 2);

                }
                if (Metodlar.NoktaKureCarpmaKontrol(tempnokta.X, tempnokta.Y, 0, tempkure.X, tempkure.Y, tempkure.Z, tempkure.Radius))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Nokta - Küre


            if (secilenindex == 6)
            {
                Nokta tempnokta = (Nokta)Cisimler[0];
                tempnokta.X = Convert.ToInt32(num_cisim1x.Value);
                tempnokta.Y = Convert.ToInt32(num_cisim1y.Value);
                using (Graphics grafik = Graphics.FromHwnd(pctbox_cizimalani.Handle))
                {
                    using (SolidBrush kalem = new SolidBrush(Color.DarkBlue))
                    {
                        grafik.FillRectangle(kalem, tempnokta.X, tempnokta.Y, 1, 1);
                    }
                }

                DikdortgenPrizma tempdikdortgenPrizma = (DikdortgenPrizma)Cisimler[5];
                tempdikdortgenPrizma.X = Convert.ToInt32(num_cisim2x.Value);
                tempdikdortgenPrizma.Y = Convert.ToInt32(num_cisim2y.Value);
                tempdikdortgenPrizma.Z = Convert.ToInt32(num_cisim2z.Value);
                tempdikdortgenPrizma.Width = Convert.ToInt32(num_cisim2genislik.Value);
                tempdikdortgenPrizma.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempdikdortgenPrizma.Depth = Convert.ToInt32(num_cisim2derinlik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height);
                    g.FillRectangle(Brushes.Aqua, tempdikdortgenPrizma.X + 1, tempdikdortgenPrizma.Y + 1, tempdikdortgenPrizma.Width - 1, tempdikdortgenPrizma.Height - 1);

                }
                if (Metodlar.NoktaDikdortgenPrizmaCarpmaKontrol(tempnokta.X, tempnokta.Y, 0, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Z, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height, tempdikdortgenPrizma.Depth))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Nokta - Dikdörtgen Prizma 


            if (secilenindex == 7)
            {
                Nokta tempnokta = (Nokta)Cisimler[0];
                tempnokta.X = Convert.ToInt32(num_cisim1x.Value);
                tempnokta.Y = Convert.ToInt32(num_cisim1y.Value);
                tempnokta.Z = Convert.ToInt32(num_cisim1z.Value);
                using (Graphics grafik = Graphics.FromHwnd(pctbox_cizimalani.Handle))
                {
                    using (SolidBrush kalem = new SolidBrush(Color.DarkBlue))
                    {
                        grafik.FillRectangle(kalem, tempnokta.X, tempnokta.Y, 1, 1);
                    }
                }

                Silindir tempsilindir = (Silindir)Cisimler[3];
                tempsilindir.X = Convert.ToInt32(num_cisim2x.Value);
                tempsilindir.Y = Convert.ToInt32(num_cisim2y.Value);
                tempsilindir.Z = Convert.ToInt32(num_cisim2z.Value);
                tempsilindir.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempsilindir.Radius = Convert.ToInt32(num_cisim2r.Value);

                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawRectangle(Pens.DarkBlue, tempsilindir.X, tempsilindir.Y, tempsilindir.Radius * 2, tempsilindir.Height);

                    for (int i = tempsilindir.X, artishizi = 1; i < tempsilindir.X + tempsilindir.Radius; i += artishizi, i++)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artishizi++;
                    }
                    for (int i = tempsilindir.X + tempsilindir.Radius * 2, artismiktari = 1; i > tempsilindir.X + tempsilindir.Radius; i -= artismiktari)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artismiktari++;
                        if (i - artismiktari < tempsilindir.X + tempsilindir.Radius)
                        {
                            break;
                        }
                    }

                }
                if (Metodlar.NoktaSilindirCarpmaKontrol(tempnokta.X, tempnokta.Y, tempnokta.Z, tempsilindir.X, tempsilindir.Y, tempsilindir.Z, tempsilindir.Radius, tempsilindir.Height))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Nokta - Silindir


            if (secilenindex == 8)
            {
                Silindir tempsilindir = (Silindir)Cisimler[3];

                tempsilindir.X = Convert.ToInt32(num_cisim1x.Value);
                tempsilindir.Y = Convert.ToInt32(num_cisim1y.Value);
                tempsilindir.Z = Convert.ToInt32(num_cisim1z.Value);
                tempsilindir.Height = Convert.ToInt32(num_cisim1yukseklik.Value);
                tempsilindir.Radius = Convert.ToInt32(num_cisim1r.Value);

                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawRectangle(Pens.DarkBlue, tempsilindir.X, tempsilindir.Y, tempsilindir.Radius * 2, tempsilindir.Height);

                    for (int i = tempsilindir.X, artishizi = 1; i < tempsilindir.X + tempsilindir.Radius; i += artishizi, i++)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artishizi++;
                    }
                    for (int i = tempsilindir.X + tempsilindir.Radius * 2, artismiktari = 1; i > tempsilindir.X + tempsilindir.Radius; i -= artismiktari)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artismiktari++;
                        if (i - artismiktari < tempsilindir.X + tempsilindir.Radius)
                        {
                            break;
                        }
                    }
                }

                int silindir1x = Convert.ToInt32(num_cisim1x.Value);
                int silindir1y = Convert.ToInt32(num_cisim1y.Value);
                int silindir1z = Convert.ToInt32(num_cisim1z.Value);
                int silindir1yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                int silindir1yaricap = Convert.ToInt32(num_cisim1r.Value);

                Silindir tempsilindir2 = (Silindir)Cisimler[3];

                tempsilindir2.X = Convert.ToInt32(num_cisim2x.Value);
                tempsilindir2.Y = Convert.ToInt32(num_cisim2y.Value);
                tempsilindir2.Z = Convert.ToInt32(num_cisim2z.Value);
                tempsilindir2.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempsilindir2.Radius = Convert.ToInt32(num_cisim2r.Value);

                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawRectangle(Pens.DarkBlue, tempsilindir2.X, tempsilindir2.Y, tempsilindir2.Radius * 2, tempsilindir2.Height);

                    for (int i = tempsilindir2.X, artishizi = 1; i < tempsilindir2.X + tempsilindir2.Radius; i += artishizi, i++)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir2.Y, i, tempsilindir2.Y + tempsilindir2.Height);
                        artishizi++;
                    }
                    for (int i = tempsilindir2.X + tempsilindir2.Radius * 2, artismiktari = 1; i > tempsilindir2.X + tempsilindir2.Radius; i -= artismiktari)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir2.Y, i, tempsilindir2.Y + tempsilindir2.Height);
                        artismiktari++;
                        if (i - artismiktari < tempsilindir2.X + tempsilindir2.Radius)
                        {
                            break;
                        }
                    }
                }
                if (Metodlar.SilindirSilindirCarpmaKontrol(silindir1x, silindir1y, silindir1z, silindir1yaricap, silindir1yukseklik, tempsilindir2.X, tempsilindir2.Y, tempsilindir2.Z, tempsilindir2.Radius, tempsilindir2.Height))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Silindir - Silindir


            if (secilenindex == 9)
            {

                Kure tempkure = (Kure)Cisimler[4];
                tempkure.X = Convert.ToInt32(num_cisim1x.Value);
                tempkure.Y = Convert.ToInt32(num_cisim1y.Value);
                tempkure.Z = Convert.ToInt32(num_cisim1z.Value);
                tempkure.Radius = Convert.ToInt32(num_cisim1r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    Brush brush = new SolidBrush(Color.DarkBlue);
                    g.FillEllipse(brush, tempkure.X - tempkure.Radius, tempkure.Y - tempkure.Radius, tempkure.Radius * 2, tempkure.Radius * 2);

                }
                int kure1x = Convert.ToInt32(num_cisim1x.Value);
                int kure1y = Convert.ToInt32(num_cisim1y.Value);
                int kure1z = Convert.ToInt32(num_cisim1z.Value);
                int kure1yaricap = Convert.ToInt32(num_cisim1r.Value);


                Kure tempkure2 = (Kure)Cisimler[4];
                tempkure2.X = Convert.ToInt32(num_cisim2x.Value);
                tempkure2.Y = Convert.ToInt32(num_cisim2y.Value);
                tempkure2.Z = Convert.ToInt32(num_cisim2z.Value);
                tempkure2.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    Brush brush = new SolidBrush(Color.DarkBlue);
                    g.FillEllipse(brush, tempkure2.X - tempkure2.Radius, tempkure2.Y - tempkure2.Radius, tempkure2.Radius * 2, tempkure2.Radius * 2);

                }
                if (Metodlar.KureKureCarpisma(kure1x, kure1y, kure1z, kure1yaricap, tempkure2.X, tempkure2.Y, tempkure2.Z, tempkure2.Radius))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Küre - Küre


            if (secilenindex == 10)
            {

                Kure tempkure = (Kure)Cisimler[4];
                tempkure.X = Convert.ToInt32(num_cisim1x.Value);
                tempkure.Y = Convert.ToInt32(num_cisim1y.Value);
                tempkure.Z = Convert.ToInt32(num_cisim1z.Value);
                tempkure.Radius = Convert.ToInt32(num_cisim1r.Value);
                tempkure.Radius += tempkure.Z;
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    Brush brush = new SolidBrush(Color.DarkBlue);
                    g.FillEllipse(brush, tempkure.X - tempkure.Radius, tempkure.Y - tempkure.Radius, tempkure.Radius * 2, tempkure.Radius * 2);

                }

                Silindir tempsilindir = (Silindir)Cisimler[3];

                tempsilindir.X = Convert.ToInt32(num_cisim2x.Value);
                tempsilindir.Y = Convert.ToInt32(num_cisim2y.Value);
                tempsilindir.Z = Convert.ToInt32(num_cisim2z.Value);
                tempsilindir.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempsilindir.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawRectangle(Pens.DarkBlue, tempsilindir.X, tempsilindir.Y, tempsilindir.Radius * 2, tempsilindir.Height);


                    for (int i = tempsilindir.X, artishizi = 1; i < tempsilindir.X + tempsilindir.Radius; i += artishizi, i++)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artishizi++;
                    }
                    for (int i = tempsilindir.X + tempsilindir.Radius * 2, artismiktari = 1; i > tempsilindir.X + tempsilindir.Radius; i -= artismiktari)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artismiktari++;
                        if (i - artismiktari < tempsilindir.X + tempsilindir.Radius)
                        {
                            break;
                        }
                    }
                }
                if (Metodlar.KureSilindirCarpmaKontrol(tempkure.X, tempkure.Y, tempkure.Z, tempkure.Radius, tempsilindir.X, tempsilindir.Y, tempsilindir.Z, tempsilindir.Radius * 2, tempsilindir.Height))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Küre - Silindir


            if (secilenindex == 11)
            {
                Yuzey tempyuzey = (Yuzey)Cisimler[6];
                tempyuzey.NoktaX = Convert.ToInt32(num_cisim1x.Value);
                tempyuzey.NoktaY = Convert.ToInt32(num_cisim1y.Value);
                tempyuzey.NoktaZ = Convert.ToInt32(num_cisim1z.Value);
                tempyuzey.Genislik = Convert.ToInt32(num_cisim1genislik.Value);
                tempyuzey.Yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.Black, tempyuzey.NoktaX, tempyuzey.NoktaY, tempyuzey.Genislik, tempyuzey.Yukseklik);
                    g.FillRectangle(Brushes.Orange, tempyuzey.NoktaX + 1, tempyuzey.NoktaY + 1, tempyuzey.Genislik - 1, tempyuzey.Yukseklik - 1);

                }


                Kure tempkure = (Kure)Cisimler[4];
                tempkure.X = Convert.ToInt32(num_cisim2x.Value);
                tempkure.Y = Convert.ToInt32(num_cisim2y.Value);
                tempkure.Z = Convert.ToInt32(num_cisim2z.Value);
                tempkure.Radius = Convert.ToInt32(num_cisim2r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    Brush brush = new SolidBrush(Color.DarkBlue);
                    g.FillEllipse(brush, tempkure.X - tempkure.Radius, tempkure.Y - tempkure.Radius, tempkure.Radius * 2, tempkure.Radius * 2);

                }
                if (Metodlar.YuzeyKureCarpmaKontrol(tempyuzey.NoktaX, tempyuzey.NoktaY, tempyuzey.NoktaZ, tempyuzey.Genislik, tempyuzey.Yukseklik, tempkure.X, tempkure.Y, tempkure.Z, tempkure.Radius))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Yüzey - Küre


            if (secilenindex == 12)
            {
                Yuzey tempyuzey = (Yuzey)Cisimler[6];
                tempyuzey.NoktaX = Convert.ToInt32(num_cisim1x.Value);
                tempyuzey.NoktaY = Convert.ToInt32(num_cisim1y.Value);
                tempyuzey.NoktaZ = Convert.ToInt32(num_cisim1z.Value);
                tempyuzey.Genislik = Convert.ToInt32(num_cisim1genislik.Value);
                tempyuzey.Yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                tempyuzey.Genislik += tempyuzey.NoktaZ;
                tempyuzey.Yukseklik += tempyuzey.NoktaZ;
                tempyuzey.NoktaX -= tempyuzey.NoktaZ / 2;
                tempyuzey.NoktaY -= tempyuzey.NoktaZ / 2;
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {


                    g.DrawRectangle(Pens.Black, tempyuzey.NoktaX, tempyuzey.NoktaY, tempyuzey.Genislik, tempyuzey.Yukseklik);
                    g.FillRectangle(Brushes.Orange, tempyuzey.NoktaX + 1, tempyuzey.NoktaY + 1, tempyuzey.Genislik - 1, tempyuzey.Yukseklik - 1);

                }



                DikdortgenPrizma tempdikdortgenPrizma = (DikdortgenPrizma)Cisimler[5];
                tempdikdortgenPrizma.X = Convert.ToInt32(num_cisim2x.Value);
                tempdikdortgenPrizma.Y = Convert.ToInt32(num_cisim2y.Value);
                tempdikdortgenPrizma.Z = Convert.ToInt32(num_cisim2z.Value);
                tempdikdortgenPrizma.Width = Convert.ToInt32(num_cisim2genislik.Value);
                tempdikdortgenPrizma.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempdikdortgenPrizma.Depth = Convert.ToInt32(num_cisim2derinlik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height);
                    g.FillRectangle(Brushes.Aqua, tempdikdortgenPrizma.X + 1, tempdikdortgenPrizma.Y + 1, tempdikdortgenPrizma.Width - 1, tempdikdortgenPrizma.Height - 1);

                }

                if (Metodlar.YuzeyDikdortgenPrizmaCarpmaKontrol(tempyuzey.NoktaX, tempyuzey.NoktaY, tempyuzey.NoktaZ, tempyuzey.Genislik, tempyuzey.Yukseklik, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Z, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height, tempdikdortgenPrizma.Depth))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }

            } // Yüzey - Dikdörtgen Prizma


            if (secilenindex == 13)
            {
                Yuzey tempyuzey = (Yuzey)Cisimler[6];
                tempyuzey.NoktaX = Convert.ToInt32(num_cisim1x.Value);
                tempyuzey.NoktaY = Convert.ToInt32(num_cisim1y.Value);
                tempyuzey.NoktaZ = Convert.ToInt32(num_cisim1z.Value);
                tempyuzey.Genislik = Convert.ToInt32(num_cisim1genislik.Value);
                tempyuzey.Yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.Black, tempyuzey.NoktaX, tempyuzey.NoktaY, tempyuzey.Genislik, tempyuzey.Yukseklik);
                    g.FillRectangle(Brushes.Orange, tempyuzey.NoktaX + 1, tempyuzey.NoktaY + 1, tempyuzey.Genislik - 1, tempyuzey.Yukseklik - 1);

                }

                Silindir tempsilindir = (Silindir)Cisimler[3];

                tempsilindir.X = Convert.ToInt32(num_cisim2x.Value);
                tempsilindir.Y = Convert.ToInt32(num_cisim2y.Value);
                tempsilindir.Z = Convert.ToInt32(num_cisim2z.Value);
                tempsilindir.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempsilindir.Radius = Convert.ToInt32(num_cisim2r.Value);

                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    g.DrawRectangle(Pens.DarkBlue, tempsilindir.X, tempsilindir.Y, tempsilindir.Radius * 2, tempsilindir.Height);


                    for (int i = tempsilindir.X, artishizi = 1; i < tempsilindir.X + tempsilindir.Radius; i += artishizi, i++)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artishizi++;
                    }
                    for (int i = tempsilindir.X + tempsilindir.Radius * 2, artismiktari = 1; i > tempsilindir.X + tempsilindir.Radius; i -= artismiktari)
                    {
                        g.DrawLine(Pens.DarkBlue, i, tempsilindir.Y, i, tempsilindir.Y + tempsilindir.Height);
                        artismiktari++;
                        if (i - artismiktari < tempsilindir.X + tempsilindir.Radius)
                        {
                            break;
                        }
                    }
                }
                if (Metodlar.YuzeySilindirCarpmaKontrol(tempyuzey.NoktaX, tempyuzey.NoktaY, tempyuzey.NoktaZ, tempyuzey.Genislik, tempyuzey.Yukseklik, tempsilindir.X, tempsilindir.Y, tempsilindir.Z, tempsilindir.Radius, tempsilindir.Height))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Yüzey - Silindir


            if (secilenindex == 14)
            {

                Kure tempkure = (Kure)Cisimler[4];
                tempkure.X = Convert.ToInt32(num_cisim1x.Value);
                tempkure.Y = Convert.ToInt32(num_cisim1y.Value);
                tempkure.Z = Convert.ToInt32(num_cisim1z.Value);
                tempkure.Radius = Convert.ToInt32(num_cisim1r.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {
                    Brush brush = new SolidBrush(Color.DarkBlue);
                    g.FillEllipse(brush, tempkure.X - tempkure.Radius, tempkure.Y - tempkure.Radius, tempkure.Radius * 2, tempkure.Radius * 2);

                }

                DikdortgenPrizma tempdikdortgenPrizma = (DikdortgenPrizma)Cisimler[5];
                tempdikdortgenPrizma.X = Convert.ToInt32(num_cisim2x.Value);
                tempdikdortgenPrizma.Y = Convert.ToInt32(num_cisim2y.Value);
                tempdikdortgenPrizma.Z = Convert.ToInt32(num_cisim2z.Value);
                tempdikdortgenPrizma.Width = Convert.ToInt32(num_cisim2genislik.Value);
                tempdikdortgenPrizma.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempdikdortgenPrizma.Depth = Convert.ToInt32(num_cisim2derinlik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height);
                    g.FillRectangle(Brushes.Aqua, tempdikdortgenPrizma.X + 1, tempdikdortgenPrizma.Y + 1, tempdikdortgenPrizma.Width - 1, tempdikdortgenPrizma.Height - 1);

                }
                if (Metodlar.KureDikPrizmaCarpmaKontrol(tempkure.X, tempkure.Y, tempkure.Z, tempkure.Radius, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Z, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height, tempdikdortgenPrizma.Depth))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Küre - Dikdörtgen Prizma


            if (secilenindex == 15)
            {
                DikdortgenPrizma tempdikdortgenPrizma = (DikdortgenPrizma)Cisimler[5];
                tempdikdortgenPrizma.X = Convert.ToInt32(num_cisim1x.Value);
                tempdikdortgenPrizma.Y = Convert.ToInt32(num_cisim1y.Value);
                tempdikdortgenPrizma.Z = Convert.ToInt32(num_cisim1z.Value);
                tempdikdortgenPrizma.Width = Convert.ToInt32(num_cisim1genislik.Value);
                tempdikdortgenPrizma.Height = Convert.ToInt32(num_cisim1yukseklik.Value);
                tempdikdortgenPrizma.Depth = Convert.ToInt32(num_cisim1derinlik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgenPrizma.X, tempdikdortgenPrizma.Y, tempdikdortgenPrizma.Width, tempdikdortgenPrizma.Height);
                    g.FillRectangle(Brushes.Aqua, tempdikdortgenPrizma.X + 1, tempdikdortgenPrizma.Y + 1, tempdikdortgenPrizma.Width - 1, tempdikdortgenPrizma.Height - 1);

                }


                int prizma1X = Convert.ToInt32(num_cisim1x.Value);
                int prizma1Y = Convert.ToInt32(num_cisim1y.Value);
                int prizma1Z = Convert.ToInt32(num_cisim1z.Value);
                int prizma1genislik = Convert.ToInt32(num_cisim1genislik.Value);
                int prizma1yukseklik = Convert.ToInt32(num_cisim1yukseklik.Value);
                int prizma1derinlik = Convert.ToInt32(num_cisim1derinlik.Value);

                DikdortgenPrizma tempdikdortgenPrizma2 = (DikdortgenPrizma)Cisimler[5];
                tempdikdortgenPrizma2.X = Convert.ToInt32(num_cisim2x.Value);
                tempdikdortgenPrizma2.Y = Convert.ToInt32(num_cisim2y.Value);
                tempdikdortgenPrizma2.Z = Convert.ToInt32(num_cisim2z.Value);
                tempdikdortgenPrizma2.Width = Convert.ToInt32(num_cisim2genislik.Value);
                tempdikdortgenPrizma2.Height = Convert.ToInt32(num_cisim2yukseklik.Value);
                tempdikdortgenPrizma2.Depth = Convert.ToInt32(num_cisim2derinlik.Value);
                using (Graphics g = pctbox_cizimalani.CreateGraphics())
                {

                    g.DrawRectangle(Pens.DarkBlue, tempdikdortgenPrizma2.X, tempdikdortgenPrizma2.Y, tempdikdortgenPrizma2.Width, tempdikdortgenPrizma2.Height);
                    g.FillRectangle(Brushes.Aqua, tempdikdortgenPrizma2.X + 1, tempdikdortgenPrizma2.Y + 1, tempdikdortgenPrizma2.Width - 1, tempdikdortgenPrizma2.Height - 1);

                }
                if (Metodlar.DikPrizmaDikPrizmaCarpmaKontrol(prizma1X, prizma1Y, prizma1Z, prizma1genislik, prizma1yukseklik, prizma1derinlik, tempdikdortgenPrizma2.X, tempdikdortgenPrizma2.Y, tempdikdortgenPrizma2.Z, tempdikdortgenPrizma2.Width, tempdikdortgenPrizma2.Height, tempdikdortgenPrizma2.Depth))
                {
                    pctbox_kontrolisigi.BackColor = Color.Green;
                }
                else
                {
                    pctbox_kontrolisigi.BackColor = Color.Red;
                }
            } // Dikdörtgen Prizma - Dikdörtgen Prizma


        }

        private void Form1_Load(object sender, EventArgs e)
        {
            num_cisim1x.InterceptArrowKeys = false;
            num_cisim1y.InterceptArrowKeys = false;
            num_cisim1z.InterceptArrowKeys = false;
            num_cisim1genislik.InterceptArrowKeys = false;
            num_cisim1yukseklik.InterceptArrowKeys = false;
            num_cisim1derinlik.InterceptArrowKeys = false;
            num_cisim1r.InterceptArrowKeys = false;
            ///////////////////////////////////////////////
            num_cisim2x.InterceptArrowKeys = false;
            num_cisim2y.InterceptArrowKeys = false;
            num_cisim2z.InterceptArrowKeys = false;
            num_cisim2genislik.InterceptArrowKeys = false;
            num_cisim2yukseklik.InterceptArrowKeys = false;
            num_cisim2derinlik.InterceptArrowKeys = false;
            num_cisim2r.InterceptArrowKeys = false;
            //////////////////////////////////////////////
            num_cisim1x.Enabled = false;
            num_cisim1y.Enabled = false;
            num_cisim1z.Enabled = false;
            num_cisim1genislik.Enabled = false;
            num_cisim1yukseklik.Enabled = false;
            num_cisim1derinlik.Enabled = false;
            num_cisim1r.Enabled = false;
            ////////////////////////////////////////
            num_cisim2x.Enabled = false;
            num_cisim2y.Enabled = false;
            num_cisim2z.Enabled = false;
            num_cisim2genislik.Enabled = false;
            num_cisim2yukseklik.Enabled = false;
            num_cisim2derinlik.Enabled = false;
            num_cisim2r.Enabled = false;

            ////////////////////////////////////////
            Cisimler.Add(0, kalipnokta);
            Cisimler.Add(1, kalipdikdortgen);
            Cisimler.Add(2, kalipcember);
            Cisimler.Add(3, kalipsilindir);
            Cisimler.Add(4, kalipkure);
            Cisimler.Add(5, kalipdikdörtgenprizma);
            Cisimler.Add(6, kalipyuzey);

        }

        private void comboBox1_SelectedIndexChanged(object sender, EventArgs e)
        {
            num_cisim1x.Value = 0;
            num_cisim1y.Value = 0;
            num_cisim1z.Value = 0;
            num_cisim1genislik.Value = 0;
            num_cisim1yukseklik.Value = 0;
            num_cisim1derinlik.Value = 0;
            num_cisim1r.Value = 0;
            /////////////////////////////////
            num_cisim2x.Value = 0;
            num_cisim2y.Value = 0;
            num_cisim2z.Value = 0;
            num_cisim2genislik.Value = 0;
            num_cisim2yukseklik.Value = 0;
            num_cisim2derinlik.Value = 0;
            num_cisim2r.Value = 0;
            /////////////////////////////////

            pctbox_cizimalani.Refresh();
            pctbox_kontrolisigi.BackColor = default;
            secilenindex = comboBox1.SelectedIndex;
            if (secilenindex == 0)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = false;
                num_cisim2genislik.Enabled = true;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = false;
                /////////////////////////////////////////
            } // Nokta - Dikdörtgen
            else if (secilenindex == 1)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = false;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = false;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Nokta - Çember 
            else if (secilenindex == 2)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = true;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = false;
                num_cisim2genislik.Enabled = true;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = false;
                /////////////////////////////////////////
            } // Dikdörtgen - Dikdörtgen
            else if (secilenindex == 3)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = true;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = false;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = false;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Dikdörtgen - Çember
            else if (secilenindex == 4)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = true;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = false;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = false;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Çember - Çember
            else if (secilenindex == 5)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = false;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Nokta - Küre
            else if (secilenindex == 6)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = false;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = true;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = true;
                num_cisim2r.Enabled = false;
                /////////////////////////////////////////
            } // Nokta - Dikdörtgen Prizma
            else if (secilenindex == 7)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////  
            } // Nokta - Silindir
            else if (secilenindex == 8)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = true;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Silindir - Silindir
            else if (secilenindex == 9)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = true;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = false;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Küre - Küre
            else if (secilenindex == 10)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = true;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Küre - Silindir
            else if (secilenindex == 11)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = true;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = false;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Yüzey - Küre
            else if (secilenindex == 12)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = true;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = true;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = true;
                num_cisim2r.Enabled = false;
                /////////////////////////////////////////
            } // Yüzey - Dikdörtgen Prizma
            else if (secilenindex == 13)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = true;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = false;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = false;
                num_cisim2r.Enabled = true;
                /////////////////////////////////////////
            } // Yüzey - Silindir
            else if (secilenindex == 14)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = false;
                num_cisim1yukseklik.Enabled = false;
                num_cisim1derinlik.Enabled = false;
                num_cisim1r.Enabled = true;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = true;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = true;
                num_cisim2r.Enabled = false;
                /////////////////////////////////////////
            } // Küre - Dikdörtgen Prizma
            else if (secilenindex == 15)
            {
                num_cisim1x.Enabled = true;
                num_cisim1y.Enabled = true;
                num_cisim1z.Enabled = true;
                num_cisim1genislik.Enabled = true;
                num_cisim1yukseklik.Enabled = true;
                num_cisim1derinlik.Enabled = true;
                num_cisim1r.Enabled = false;
                ////////////////////////////////////////
                num_cisim2x.Enabled = true;
                num_cisim2y.Enabled = true;
                num_cisim2z.Enabled = true;
                num_cisim2genislik.Enabled = true;
                num_cisim2yukseklik.Enabled = true;
                num_cisim2derinlik.Enabled = true;
                num_cisim2r.Enabled = false;
                /////////////////////////////////////////
            } // Dikdörtgen Prizma - Dikdörtgen Prizma
        }

        private void Form1_KeyDown(object sender, KeyEventArgs e)
        {
            if (num_cisim1x.Value >= 0 && num_cisim1x.Value <= 604 && num_cisim1y.Value >= 0 && num_cisim1y.Value <= 436)
            {
                if (e.KeyCode == Keys.Right && checkBox_cisim1hareket.Checked && num_cisim1x.Value < 604)
                {
                    num_cisim1x.Value++;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
                if (e.KeyCode == Keys.Left && checkBox_cisim1hareket.Checked && num_cisim1x.Value > 0)
                {
                    num_cisim1x.Value--;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
                if (e.KeyCode == Keys.Up && checkBox_cisim1hareket.Checked && num_cisim1y.Value > 0)
                {
                    num_cisim1y.Value--;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
                if (e.KeyCode == Keys.Down && checkBox_cisim1hareket.Checked && num_cisim1y.Value < 436)
                {
                    num_cisim1y.Value++;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
            }
            /////////////////////////////////////////////////////
            if (num_cisim2x.Value >= 0 && num_cisim2x.Value <= 604 && num_cisim2y.Value >= 0 && num_cisim2y.Value <= 436)
            {
                if (e.KeyCode == Keys.Right && checkBox_cisim2hareket.Checked && num_cisim2x.Value < 604)
                {
                    num_cisim2x.Value++;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
                if (e.KeyCode == Keys.Left && checkBox_cisim2hareket.Checked && num_cisim2x.Value > 0)
                {
                    num_cisim2x.Value--;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
                if (e.KeyCode == Keys.Up && checkBox_cisim2hareket.Checked && num_cisim2y.Value > 0)
                {
                    num_cisim2y.Value--;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
                if (e.KeyCode == Keys.Down && checkBox_cisim2hareket.Checked && num_cisim2y.Value < 436)
                {
                    num_cisim2y.Value++;
                    pctbox_cizimalani.Refresh();
                    btn_draw.PerformClick();
                }
            }
        }

        private void checkBox_cisim1hareket_CheckedChanged(object sender, EventArgs e)
        {
            if (checkBox_cisim1hareket.Checked)
            {
                this.KeyPreview = true;
                checkBox_cisim2hareket.Checked = false;
                comboBox1.Enabled = false;
            }
            else if (checkBox_cisim1hareket.Checked == false && checkBox_cisim2hareket.Checked == false)
            {
                comboBox1.Enabled = true;
                this.KeyPreview = false;
            }
        }

        private void checkBox_cisim2hareket_CheckedChanged(object sender, EventArgs e)
        {
            if (checkBox_cisim2hareket.Checked)
            {
                this.KeyPreview = true;
                checkBox_cisim1hareket.Checked = false;
                comboBox1.Enabled = false;
            }
            else if (checkBox_cisim1hareket.Checked == false && checkBox_cisim2hareket.Checked == false)
            {
                comboBox1.Enabled = true;
                this.KeyPreview = false;
            }
        }

    }
}