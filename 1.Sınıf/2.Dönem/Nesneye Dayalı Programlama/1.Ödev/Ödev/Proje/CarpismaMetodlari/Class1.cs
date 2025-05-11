using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CarpismaMetodlari
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
}
