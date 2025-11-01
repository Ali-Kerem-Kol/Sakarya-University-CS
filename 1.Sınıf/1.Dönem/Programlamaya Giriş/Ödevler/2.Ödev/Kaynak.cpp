
/******************************************************************************************************************************************
*                                                                                                                                         *
*                                                            SAKARYA ÜNÝVERSÝTESÝ                                                         *
*                                                  BÝLGÝSAYAR VE BÝLÝÞÝM BÝLÝMLERÝ FAKÜLTESÝ                                              *
*                                                       BÝLGÝSAYAR MÜHENDÝSLÝÐÝ BÖLÜMÜ                                                    *
*                                                          PROGRAMLAMAYA GÝRÝÞ DERSÝ                                                      *
*                                                                                                                                         *
*                                                              ÖDEV NUMARASI : 2                                                          *
*                                                                                                                                         *
*                                                         ÖÐRENCÝ ADI : ALÝ KEREM KOL                                                     *
*                                                        ÖÐRENCÝ NUMARASI : B221210042                                                    *
*                                                       DERS GRUBU : 1. ÖÐRETÝM A GRUBU                                                   *
*                                                                                                                                         *
******************************************************************************************************************************************/

#include <iostream>
#include <Windows.h>
#include <string>
#include <cmath>

using namespace std;

class Karmasiksayi
{
public:
	Karmasiksayi(float r = 0, float s = 0)
	{
		reel = r;
		sanal = s;
	}

	int getReel()
	{
		return reel;
	}

	int getSanal()
	{
		return sanal;
	}

	void setReel(float r)
	{
		reel = r;
	}

	void setSanal(float s)
	{
		sanal = s;
	}
	void print()
	{
		//Sayinin Sanal Kisminin Sifirdan Buyuk Olup Olmadýgý Kontrol Ediliyor.
		if (sanal > 0)
		{
			cout << reel << " + " << sanal << "i" << endl;
		}
		//Sayinin Sanal Kisminin Sifirdan Kucuk Olup Olmadýgý Kontrol Ediliyor.
		else if (sanal < 0)
		{
			cout << reel << " - " << -sanal << "i" << endl;
		}
		//Diger Durumlar Icin Uygulanacak Kosul.
		else
		{
			cout << reel << endl;
		}
	}

	void kutupsalgosterim()
	{
		double r = sqrt(reel * reel + sanal * sanal);
		double theta = atan2(sanal, reel);
		cout << "r(Yaricap Uzunlugu) = " << r << ", theta Acisi = " << theta << endl;
	}

	Karmasiksayi operator+(Karmasiksayi obj)
	{
		Karmasiksayi sonuc;
		sonuc.reel = reel + obj.reel;
		sonuc.sanal = sanal + obj.sanal;
		return sonuc;
	}
	Karmasiksayi operator-(Karmasiksayi obj)
	{
		Karmasiksayi sonuc;
		sonuc.reel = reel - obj.reel;
		sonuc.sanal = sanal - obj.sanal;
		return sonuc;
	}
	Karmasiksayi operator*(Karmasiksayi obj)
	{
		Karmasiksayi sonuc;
		sonuc.reel = reel * obj.reel;
		sonuc.sanal = sanal * obj.sanal;
		return sonuc;
	}
	Karmasiksayi operator/(Karmasiksayi obj)
	{
		Karmasiksayi sonuc;
		sonuc.reel = reel / obj.reel;
		sonuc.sanal = sanal / obj.sanal;
		return sonuc;
	}
	Karmasiksayi operator/=(Karmasiksayi obj)
	{
		Karmasiksayi sonuc;
		sonuc.reel = reel / obj.reel;
		sonuc.sanal = sanal / obj.sanal;
		return sonuc;
	}

private:
	float reel;
	float sanal;
};

Karmasiksayi operator+=(Karmasiksayi obj1,Karmasiksayi obj2)
{
	Karmasiksayi sonuc;
	sonuc.setReel(obj1.getReel() + obj2.getReel());
	sonuc.setSanal(obj1.getSanal() + obj2.getSanal());
	return sonuc;
}
Karmasiksayi operator-=(Karmasiksayi obj1, Karmasiksayi obj2)
{
	Karmasiksayi sonuc;
	sonuc.setReel(obj1.getReel() - obj2.getReel());
	sonuc.setSanal(obj1.getSanal() - obj2.getSanal());
	return sonuc;
}
Karmasiksayi operator*=(Karmasiksayi obj1, Karmasiksayi obj2)
{
	Karmasiksayi sonuc;
	sonuc.setReel(obj1.getReel() * obj2.getReel());
	sonuc.setSanal(obj1.getSanal() * obj2.getSanal());
	return sonuc;
}

bool sayimi(string metin);

bool kontrol(string sayi);

int main()
{
	system("color a");

	string secim;

	string reelk1;
	string sanalk1;
	string reelk2;
	string sanalk2;

	//Kullanicinin Istedigi Zaman Cikmasini Saglamak Icin Do-While Dongusu Kullanildi.
	do
	{
		cout << "Hosgeldiniz Yapmak Istediginiz Islemi Seciniz..." << endl;
		cout << "[1] Toplama(+)" << endl;
		cout << "[2] Cikarma(-)" << endl;
		cout << "[3] Carpma(*)" << endl;
		cout << "[4] Bolme(/)" << endl;
		cout << "[5] Toplama(+=)" << endl;
		cout << "[6] Cikarma(-=)" << endl;
		cout << "[7] Carpma(*=)" << endl;
		cout << "[8] Bolme(/=)" << endl;
		cout << "[9] Kutupsal Gosterim" << endl;
		cout << "[0] Cikis Yap" << endl;

		cin >> secim;

		//Toplama Islemini Yapan Secenek(+).
		if (secim == "1")
		{
			system("cls");
			cout << "Toplamak Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Toplamak Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Toplamak Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Toplamak Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			Karmasiksayi k3 = k1 + k2;
			cout << "Sonuc: ";
			k3.print();
			system("pause");
		}
		//Cikarma Islemini Yapan Secenek(-).
		else if (secim == "2")
		{
			system("cls");
			cout << "Cikarmak Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Cikarmak Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Cikarmak Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Cikarmak Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			Karmasiksayi k3 = k1 - k2;
			cout << "Sonuc: ";
			k3.print();
			system("pause");
		}
		//Carpma Islemini Yapan Secenek(*).
		else if (secim == "3")
		{
			system("cls");
			cout << "Carpmak Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Carpmak Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Carpmak Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Carpmak Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			Karmasiksayi k3 = k1 * k2;
			cout << "Sonuc: ";
			k3.print();
			system("pause");
		}
		//Bolme Islemini Yapan Secenek(/).
		else if (secim == "4")
		{
			system("cls");
			cout << "Bolmek Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Bolmek Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Bolmek Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Bolmek Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			Karmasiksayi k3 = k1 / k2;
			cout << "Sonuc: ";
			k3.print();
			system("pause");
		}
		//Toplama Islemini Yapan Secenek(+=).
		else if (secim == "5")
		{
			system("cls");
			cout << "Toplamak Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Toplamak Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Toplamak Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Toplamak Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			(k1 += k2).print();
			system("pause");
		}
		//Cikarma Islemini Yapan Secenek(-=).
		else if (secim == "6")
		{
			system("cls");
			cout << "Cikarmak Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Cikarmak Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Cikarmak Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Cikarmak Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			(k1 -= k2).print();
			system("pause");
		}
		//Carpma Islemini Yapan Secenek(*=).
		else if (secim == "7")
		{
			system("cls");
			cout << "Carpmak Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Carpmak Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Carpmak Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Carpmak Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			(k1 *= k2).print();
			system("pause");
		}
		//Bolme Islemini Yapan Secenek(/=).
		else if (secim == "8")
		{
			system("cls");
			cout << "Bolmek Istediginiz 1. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk1;

			} while (!kontrol(reelk1));
			cout << "Bolmek Istediginiz 1. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk1;

			} while (!kontrol(sanalk1));
			cout << "Bolmek Istediginiz 2. Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> reelk2;

			} while (!kontrol(reelk2));
			cout << "Bolmek Istediginiz 2. Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
			//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
			do
			{
				cin >> sanalk2;

			} while (!kontrol(sanalk2));
			Karmasiksayi k1(stof(reelk1), stof(sanalk1));
			Karmasiksayi k2(stof(reelk2), stof(sanalk2));
			(k1 /= k2).print();
			system("pause");
		}
		//Kutupsal Gosterim Islemini Yapan Secenek.
		else if (secim == "9")
		{
		system("cls");
		cout << "Kutupsal Gosterimini Gormek Istediginiz Karmasik Sayinin \"Reel\" Kismini Giriniz: ";
		//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
		do
		{
			cin >> reelk1;

		} while (!kontrol(reelk1));
		cout << "Kutupsal Gosterimini Gormek Istediginiz Karmasik Sayinin \"Sanal\" Kismini Giriniz: ";
		//Girilen Deger Uygun Kosullarda mi Diye Kontrol Ediliyor.
		do
		{
			cin >> sanalk1;

		} while (!kontrol(sanalk1));

		Karmasiksayi k1(stof(reelk1), stof(sanalk1));
		k1.kutupsalgosterim();
		system("pause");

		}
		//Cikis Yapan Secenek.
		else if (secim == "0")
		{
			system("cls");
			cout << "Cikis Yapiliyor...";
			Sleep(1000);
			return 0;
		}
		//Diger Durumlar Icin Uygulanacak Kosul ve Uyarý Mesaji.
		else
		{
			cout << "Lutfen Gecerli Bir Komut Giriniz..." << endl;
			system("pause");
		}

		system("cls");

	} while (secim != "0");

	return 0;
}

bool sayimi(string metin) 
{
	//Önceki Ödevde Ýnternetten Hazýr Fonksiyon Kullandým, Orada Pointer Varmýþ Þimdi Fonksiyonu Kendim Yazdým.
	
	/*Parametre Olarak Gelen String Degerinin Her Harfinin Sayi Olup Olmadigini Kontrol Eden ve String Degerinin
	Uzunlugu Kadar Donen Bir Dongu.*/
	for (int i = 0; i < metin.length(); i++)
	{
		//Gelen String Degerinin "i". Harfinin Sayi Olup Olmadigi Kontrol Ediliyor.
		if (isdigit(metin[i]) == false)
		{
			return false;
		}
	}
	return true;
}

bool kontrol(string sayi)
{
	bool tamam;
	//Gelen String Degerinin Uygun Kosullarda Olup Olmadigi Kontrol Ediliyor.
	if (!sayimi(sayi))
	{
		cout << "Lutfen Gecerli Bir Deger Giriniz..." << endl;
		tamam = false;
	}
	//Diger Durumlar Icin Uygulanacak Kosul.
	else
	{
		tamam = true;
	}
	return tamam;
}