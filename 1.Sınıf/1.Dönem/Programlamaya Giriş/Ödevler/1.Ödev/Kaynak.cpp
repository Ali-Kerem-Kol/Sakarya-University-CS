
/******************************************************************************************************************************************
*                                                                                                                                         *
*                                                            SAKARYA ÜNÝVERSÝTESÝ                                                         *
*                                                  BÝLGÝSAYAR VE BÝLÝÞÝM BÝLÝMLERÝ FAKÜLTESÝ                                              *
*                                                       BÝLGÝSAYAR MÜHENDÝSLÝÐÝ BÖLÜMÜ                                                    *
*                                                          PROGRAMLAMAYA GÝRÝÞ DERSÝ                                                      *
*                                                                                                                                         *
*                                                              ÖDEV NUMARASI : 1                                                          *
*                                                                                                                                         *
*                                                         ÖÐRENCÝ ADI : ALÝ KEREM KOL                                                     *
*                                                        ÖÐRENCÝ NUMARASI : B221210042                                                    *
*                                                       DERS GRUBU : 1. ÖÐRETÝM A GRUBU                                                   *
*                                                                                                                                         *
******************************************************************************************************************************************/

#include <iostream>
#include <cmath>
#include <stdio.h>
#include <string>
#include <cstdlib>
#include <iomanip>
#include <time.h>
#include <Windows.h>
#include <math.h>
#include <conio.h>

using namespace std;

struct Tarih
{
	string Gün;
	string Ay;
	string Yýl;
};

struct ogrenci
{
	string Adi;
	string Soyadi;
	string No;
	string ksýnav1;
	string ksýnav2;
	string ödev1;
	string ödev2;
	string proje;
	string vize;
	string final;
	float yilicinot;
	float basarinot;
	Tarih t1;
};

bool isNumber(const string& s);

string Notcevir(string notgel);

bool kontrol(string notk);

void menu();

bool ogrnokontrol(string nok);

bool ogrtarihkontrol(string gun, string ay, string yil);

int main()
{
	system("color a");

	srand(time(NULL));

	string isim[30] = { "Kerim","Yener","Zahide","Atakan","Umut","Tufan","Seda","Velat","Ali","Ecenur","Ozan","Idris",
	"Ata","Mehmet","Osman","Ahsen","Sevda","Rasit","Ekrem","Murat","Unalcan","Gokce","Cýhat","Safiye","Ayse","Nurgul",
	"Mahmut","Erol","Recep","Hatice" };

	string soyisim[30] = { "Kara","Top","Duman","Gorgulu","Akay","Kilavuz","Tayyar","Senol","Coskun","Inanir","Topal",
	"Tarakci","Pinar","Arhan","Omay","Yilmaz","Sahin","Camurcu","Cirak","Caglar","Kose","Kumbul","Delen","Bayhan",
	"Turhal","Donma","Mentes","Bozkurt","Tas","Dogan" };

	ogrenci Ogrenci[100];

	ogrenci o1;

	string secim;
	
	cout << "Hos Geldiniz... Hangi Islemi Yapmak Istiyorsunuz ?" << endl;
	cout << "[1] : Ogrenci Degiskeni Uyeleri Rasgele Belirleme" << endl;
	cout << "[2] : Ogrenci Degiskeni Uyelerini Manuel Olarak Belirleme" << endl;
	cout << "[3] : Cikis Yap" << endl;
	

	//Menude Kullanýcý Istemedikce Cikmamasini Saglamak Icin Do-While Dongusu Kullanildi.
	do
	{
		cin >> secim;
		
		//Ogrencilerin Bilgilerinin Rasgele Doldurulmasi Icin Kullanildi.
		if (secim == "1")
		{
			//Tum Ogrencilerin Notlar Haric Bilgileri Rasgele Ataniyor.
			for (int i = 0; i < 100; i++)
			{
				Ogrenci[i].Adi = isim[rand() % 30];
				Ogrenci[i].Soyadi = soyisim[rand() % 30];
				Ogrenci[i].No = to_string(rand() % 899999999 + 100000000);
				Ogrenci[i].t1.Yýl = to_string(rand() % 24 + 1980);
				Ogrenci[i].t1.Ay = to_string(rand() % 12 + 1);
				if (Ogrenci[i].t1.Ay == "1" || "3" || "5" || "7" || "8" || "10" || "12")
				{
					Ogrenci[i].t1.Gün = to_string(rand() % 30 + 1);
				}
				else if (Ogrenci[i].t1.Ay == "4" || "6" || "9" || "11")
				{
					Ogrenci[i].t1.Gün = to_string(rand() % 29 + 1);
				}
				else if (Ogrenci[i].t1.Ay == "2")
				{
					Ogrenci[i].t1.Gün = to_string(rand() % 27 + 1);
				}
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].ksýnav1 = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].ksýnav1 = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].ksýnav1 = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].ksýnav1 = to_string(rand() % 20 + 80);
			}
			//////////////////////////////////////////////////////////////////////////////
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].ksýnav2 = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].ksýnav2 = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].ksýnav2 = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].ksýnav2 = to_string(rand() % 20 + 80);
			}
			//////////////////////////////////////////////////////////////////////////////
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].ödev1 = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].ödev1 = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].ödev1 = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].ödev1 = to_string(rand() % 20 + 80);
			}
			//////////////////////////////////////////////////////////////////////////////
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].ödev2 = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].ödev2 = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].ödev2 = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].ödev2 = to_string(rand() % 20 + 80);
			}
			//////////////////////////////////////////////////////////////////////////////
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].proje = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].proje = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].proje = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].proje = to_string(rand() % 20 + 80);
			}
			//////////////////////////////////////////////////////////////////////////////
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].vize = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].vize = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].vize = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].vize = to_string(rand() % 20 + 80);
			}
			//////////////////////////////////////////////////////////////////////////////
			//Notlar Rasgele Ataniyor.
			for (int i = 0; i < 10; i++)
			{
				Ogrenci[i].final = to_string(rand() % 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 10; i < 60; i++)
			{
				Ogrenci[i].final = to_string(rand() % 30 + 40);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 60; i < 75; i++)
			{
				Ogrenci[i].final = to_string(rand() % 10 + 70);
			}
			//Notlar Rasgele Ataniyor.
			for (int i = 75; i < 100; i++)
			{
				Ogrenci[i].final = to_string(rand() % 20 + 80);
			}
			//Tum Ogrencilerin Basari Notu Hesaplaniyor.
			for (int i = 0; i < 100; i++)
			{
				Ogrenci[i].yilicinot = (stof(Ogrenci[i].vize) * 0.5) + (stof(Ogrenci[i].ksýnav1) * 7 / 100) +
					(stof(Ogrenci[i].ksýnav2) * 7 / 100) + (stof(Ogrenci[i].ödev1) * 0.1) + (stof(Ogrenci[i].ödev2) * 0.1) +
					(stof(Ogrenci[i].proje) * 16 / 100);
				/////////////////////////////////////////////////////////////////////////////////////////
				Ogrenci[i].basarinot = (Ogrenci[i].yilicinot * 55 / 100) + (stof(Ogrenci[i].final) * 45 / 100);
			}

			break;
		}
		//Ogrencilerin Bilgilerinin Manuel Olarak Doldurulmasi Icin Kullanildi.
		else if (secim == "2")
		{
			//Tum Ogrencilerin Bilgileri Manuel Olarak Giriliyor.
			for (int i = 0; i < 100; i++)
			{

				cout << i + 1 << ". Ogrencinin Adini Giriniz :";
				cin >> Ogrenci[i].Adi;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "Soyadini Giriniz :";
				cin >> Ogrenci[i].Soyadi;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "Numarasini Giriniz (9 Haneli) :";
				cin >> Ogrenci[i].No;
				while (ogrnokontrol(Ogrenci[i].No) == false)
				{
					cin >> Ogrenci[i].No;
				}
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "Dogum Tarihini Giriniz (Gun) : ";
				cin >> Ogrenci[i].t1.Gün;
				cout << endl;
				cout << "Dogum Tarihini Giriniz (Ay) : ";
				cin >> Ogrenci[i].t1.Ay;
				cout << endl;
				cout << "Dogum Tarihini Giriniz (Yil) : ";
				cin >> Ogrenci[i].t1.Yýl;
				cout << endl;
				//Ogrenci Dogum Tarihindeki Hatalari Kontrol Etme Bolgesi.
				while (ogrtarihkontrol(Ogrenci[i].t1.Gün, Ogrenci[i].t1.Ay, Ogrenci[i].t1.Yýl) == false)
				{
					cout << "Dogum Tarihini Giriniz (Gun) : ";
					cin >> Ogrenci[i].t1.Gün;
					cout << endl;
					cout << "Dogum Tarihini Giriniz (Ay) : ";
					cin >> Ogrenci[i].t1.Ay;
					cout << endl;
					cout << "Dogum Tarihini Giriniz (Yil) : ";
					cin >> Ogrenci[i].t1.Yýl;
					cout << endl;
				}
				cout << Ogrenci[i].t1.Gün << "/" << Ogrenci[i].t1.Ay << "/" << Ogrenci[i].t1.Yýl;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "1. Kisa Sinav Notunu Giriniz :";
				cin >> Ogrenci[i].ksýnav1;
				//Kisa Sinav 1 Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].ksýnav1) == false)
				{
					cin >> Ogrenci[i].ksýnav1;
				}
				cout << Ogrenci[i].ksýnav1;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "2. Kisa Sinav Notunu Giriniz :";
				cin >> Ogrenci[i].ksýnav2;
				//Kisa Sinav 2 Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].ksýnav2) == false)
				{
					cin >> Ogrenci[i].ksýnav2;
				}
				cout << Ogrenci[i].ksýnav2;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "1. Odev Notunu Giriniz :";
				cin >> Ogrenci[i].ödev1;
				//Odev 1 Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].ödev1) == false)
				{
					cin >> Ogrenci[i].ödev1;
				}
				cout << Ogrenci[i].ödev1;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "2. Odev Notunu Giriniz :";
				cin >> Ogrenci[i].ödev2;
				//Odev 1 Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].ödev2) == false)
				{
					cin >> Ogrenci[i].ödev2;
				}
				cout << Ogrenci[i].ödev2;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "Proje Notunu Giriniz :";
				cin >> Ogrenci[i].proje;
				//Proje Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].proje) == false)
				{
					cin >> Ogrenci[i].proje;
				}
				cout << Ogrenci[i].proje;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "Vize Notunu Giriniz :";
				cin >> Ogrenci[i].vize;
				//Vize Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].vize) == false)
				{
					cin >> Ogrenci[i].vize;
				}
				cout << Ogrenci[i].vize;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				cout << "Final Notunu Giriniz :";
				cin >> Ogrenci[i].final;
				//Final Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor.
				while (kontrol(Ogrenci[i].final) == false)
				{
					cin >> Ogrenci[i].final;
				}
				cout << Ogrenci[i].final;
				cout << endl;
				/////////////////////////////////////////////////////////////////////////////////////////
				Ogrenci[i].yilicinot = (stoi(Ogrenci[i].vize) * 0.5) + (stoi(Ogrenci[i].ksýnav1) * 7 / 100) +
					(stoi(Ogrenci[i].ksýnav2) * 7 / 100) + (stoi(Ogrenci[i].ödev1) * 0.1) + (stoi(Ogrenci[i].ödev2) * 0.1) +
					(stoi(Ogrenci[i].proje) * 16 / 100);
				/////////////////////////////////////////////////////////////////////////////////////////
				Ogrenci[i].basarinot = (Ogrenci[i].yilicinot * 55 / 100) + (stoi(Ogrenci[i].final) * 45 / 100);

				system("cls");

			}
			break;
		}
		//Menuden Cikis Yapma Komutu.
		else if (secim == "3")
		{
		system("cls");
		cout << "Cikis Yapiliyor...";
		Sleep(1000);
		return 0;
		}
		//Uyari Mesaji Ve Dongunun Basina Donus.
		else
		{
		cout << "Lutfen Gecerli Bir Komut Giriniz..." << endl;
		}
	} while (secim != "3");
	
	string menusecim;
	
	system("cls");

	menu();

	//Menude Kullanýcý Istemedikce Cikmamasini Saglamak Icin Do-While Dongusu Kullanildi.
	do
	{

		cin >> menusecim;

	//Sinif Listesini 20'ser Sekilde Yazdirma Komutu.
	if (menusecim == "1")
	{
	sýfýryirmi:
		system("cls");
		//0 - 20 Arasindakileri Yazdirma.
		for (int i = 0; i < 20; i++)
		{
			cout << left;
			cout << setw(13) << "Adi";
			cout << setw(16) << "Soyadi";
			cout << setw(19) << "Dog.Tarih";
			cout << setw(20) << "No";
			cout << setw(21) << "Kisa Sinav1";
			cout << setw(21) << "Kisa Sinav2";
			cout << setw(15) << "Odev1";
			cout << setw(15) << "Odev2";
			cout << setw(15) << "Proje";
			cout << setw(14) << "Vize";
			cout << setw(15) << "Final";
			cout << setw(21) << "Basari Notu";
			cout << endl;
			cout << setw(13) << Ogrenci[i].Adi;
			cout << setw(16) << Ogrenci[i].Soyadi;
			cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
			cout << setw(20) << "B" + Ogrenci[i].No;
			cout << setw(21) << Ogrenci[i].ksýnav1 + "(" + Notcevir(Ogrenci[i].ksýnav1) + ")";
			cout << setw(21) << Ogrenci[i].ksýnav2 + "(" + Notcevir(Ogrenci[i].ksýnav2) + ")";;
			cout << setw(15) << Ogrenci[i].ödev1 + "(" + Notcevir(Ogrenci[i].ödev1) + ")";;
			cout << setw(15) << Ogrenci[i].ödev2 + "(" + Notcevir(Ogrenci[i].ödev2) + ")";;
			cout << setw(15) << Ogrenci[i].proje + "(" + Notcevir(Ogrenci[i].proje) + ")";;
			cout << setw(14) << Ogrenci[i].vize + "(" + Notcevir(Ogrenci[i].vize) + ")";;
			cout << setw(15) << Ogrenci[i].final + "(" + Notcevir(Ogrenci[i].final) + ")";;
			cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
			cout << endl << endl;
		}
		cout << "Sonraki 20 Kisiyi Gormek Icin 'd' Tusunu,Cikis Yapmak Icin Herhangi Bir Tusu Kullanin...";
		string kisi20secim;
		cin >> kisi20secim;
		//20 - 40 Arasindakileri Yazdirma
		if (kisi20secim == "d")
		{
		yirmikýrk:
			system("cls");
			//20 - 40 Arasindakileri Yazdirma
			for (int i = 20; i < 40; i++)
			{
				cout << left;
				cout << setw(13) << "Adi";
				cout << setw(16) << "Soyadi";
				cout << setw(19) << "Dog.Tarih";
				cout << setw(20) << "No";
				cout << setw(21) << "Kisa Sinav1";
				cout << setw(21) << "Kisa Sinav2";
				cout << setw(15) << "Odev1";
				cout << setw(15) << "Odev2";
				cout << setw(15) << "Proje";
				cout << setw(14) << "Vize";
				cout << setw(15) << "Final";
				cout << setw(21) << "Basari Notu";
				cout << endl;
				cout << setw(13) << Ogrenci[i].Adi;
				cout << setw(16) << Ogrenci[i].Soyadi;
				cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
				cout << setw(20) << "B" + Ogrenci[i].No;
				cout << setw(21) << Ogrenci[i].ksýnav1 + "(" + Notcevir(Ogrenci[i].ksýnav1) + ")";
				cout << setw(21) << Ogrenci[i].ksýnav2 + "(" + Notcevir(Ogrenci[i].ksýnav2) + ")";;
				cout << setw(15) << Ogrenci[i].ödev1 + "(" + Notcevir(Ogrenci[i].ödev1) + ")";;
				cout << setw(15) << Ogrenci[i].ödev2 + "(" + Notcevir(Ogrenci[i].ödev2) + ")";;
				cout << setw(15) << Ogrenci[i].proje + "(" + Notcevir(Ogrenci[i].proje) + ")";;
				cout << setw(14) << Ogrenci[i].vize + "(" + Notcevir(Ogrenci[i].vize) + ")";;
				cout << setw(15) << Ogrenci[i].final + "(" + Notcevir(Ogrenci[i].final) + ")";;
				cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
				cout << endl << endl;
			}
			cout << "Sonraki 20 Kisiyi Gormek Icin 'd' Tusunu,Onceki 20 Kisiyi Gormek Icin 'a' Tusunu,"
				<< "Cikis Yapmak Icin Herhangi Bir Tusu Kullanin...";
			string kisi20secim;
			cin >> kisi20secim;
			//40 - 60 Arasindakileri Yazdirma
			if (kisi20secim == "d")
			{
			kýrkatmýs:
				system("cls");
				//40 - 60 Arasindakileri Yazdirma
				for (int i = 40; i < 60; i++)
				{
					cout << left;
					cout << setw(13) << "Adi";
					cout << setw(16) << "Soyadi";
					cout << setw(19) << "Dog.Tarih";
					cout << setw(20) << "No";
					cout << setw(21) << "Kisa Sinav1";
					cout << setw(21) << "Kisa Sinav2";
					cout << setw(15) << "Odev1";
					cout << setw(15) << "Odev2";
					cout << setw(15) << "Proje";
					cout << setw(14) << "Vize";
					cout << setw(15) << "Final";
					cout << setw(21) << "Basari Notu";
					cout << endl;
					cout << setw(13) << Ogrenci[i].Adi;
					cout << setw(16) << Ogrenci[i].Soyadi;
					cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
					cout << setw(20) << "B" + Ogrenci[i].No;
					cout << setw(21) << Ogrenci[i].ksýnav1 + "(" + Notcevir(Ogrenci[i].ksýnav1) + ")";
					cout << setw(21) << Ogrenci[i].ksýnav2 + "(" + Notcevir(Ogrenci[i].ksýnav2) + ")";;
					cout << setw(15) << Ogrenci[i].ödev1 + "(" + Notcevir(Ogrenci[i].ödev1) + ")";;
					cout << setw(15) << Ogrenci[i].ödev2 + "(" + Notcevir(Ogrenci[i].ödev2) + ")";;
					cout << setw(15) << Ogrenci[i].proje + "(" + Notcevir(Ogrenci[i].proje) + ")";;
					cout << setw(14) << Ogrenci[i].vize + "(" + Notcevir(Ogrenci[i].vize) + ")";;
					cout << setw(15) << Ogrenci[i].final + "(" + Notcevir(Ogrenci[i].final) + ")";;
					cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
					cout << endl << endl;
				}
				cout << "Sonraki 20 Kisiyi Gormek Icin 'd' Tusunu,Onceki 20 Kisiyi Gormek Icin 'a' Tusunu,"
					<< "Cikis Yapmak Icin Herhangi Bir Tusu Kullanin...";
				string kisi20secim;
				cin >> kisi20secim;
				//60 - 80 Arasindakileri Yazdirma
				if (kisi20secim == "d")
				{
				atmýsseksen:
					system("cls");
					//60 - 80 Arasindakileri Yazdirma
					for (int i = 60; i < 80; i++)
					{
						cout << left;
						cout << setw(13) << "Adi";
						cout << setw(16) << "Soyadi";
						cout << setw(19) << "Dog.Tarih";
						cout << setw(20) << "No";
						cout << setw(21) << "Kisa Sinav1";
						cout << setw(21) << "Kisa Sinav2";
						cout << setw(15) << "Odev1";
						cout << setw(15) << "Odev2";
						cout << setw(15) << "Proje";
						cout << setw(14) << "Vize";
						cout << setw(15) << "Final";
						cout << setw(21) << "Basari Notu";
						cout << endl;
						cout << setw(13) << Ogrenci[i].Adi;
						cout << setw(16) << Ogrenci[i].Soyadi;
						cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
						cout << setw(20) << "B" + Ogrenci[i].No;
						cout << setw(21) << Ogrenci[i].ksýnav1 + "(" + Notcevir(Ogrenci[i].ksýnav1) + ")";
						cout << setw(21) << Ogrenci[i].ksýnav2 + "(" + Notcevir(Ogrenci[i].ksýnav2) + ")";;
						cout << setw(15) << Ogrenci[i].ödev1 + "(" + Notcevir(Ogrenci[i].ödev1) + ")";;
						cout << setw(15) << Ogrenci[i].ödev2 + "(" + Notcevir(Ogrenci[i].ödev2) + ")";;
						cout << setw(15) << Ogrenci[i].proje + "(" + Notcevir(Ogrenci[i].proje) + ")";;
						cout << setw(14) << Ogrenci[i].vize + "(" + Notcevir(Ogrenci[i].vize) + ")";;
						cout << setw(15) << Ogrenci[i].final + "(" + Notcevir(Ogrenci[i].final) + ")";;
						cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
						cout << endl << endl;
					}
					cout << "Sonraki 20 Kisiyi Gormek Icin 'd' Tusunu,Onceki 20 Kisiyi Gormek Icin 'a' Tusunu,";
					cout << "Cikis Yapmak Icin Herhangi Bir Tusu Kullanin...";
					string kisi20secim;
					cin >> kisi20secim;
					//80 - 100 Arasindakileri Yazdirma
					if (kisi20secim == "d")
					{
						system("cls");
						//80 - 100 Arasindakileri Yazdirma
						for (int i = 80; i < 100; i++)
						{
							cout << left;
							cout << setw(13) << "Adi";
							cout << setw(16) << "Soyadi";
							cout << setw(19) << "Dog.Tarih";
							cout << setw(20) << "No";
							cout << setw(21) << "Kisa Sinav1";
							cout << setw(21) << "Kisa Sinav2";
							cout << setw(15) << "Odev1";
							cout << setw(15) << "Odev2";
							cout << setw(15) << "Proje";
							cout << setw(14) << "Vize";
							cout << setw(15) << "Final";
							cout << setw(21) << "Basari Notu";
							cout << endl;
							cout << setw(13) << Ogrenci[i].Adi;
							cout << setw(16) << Ogrenci[i].Soyadi;
							cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
							cout << setw(20) << "B" + Ogrenci[i].No;
							cout << setw(21) << Ogrenci[i].ksýnav1 + "(" + Notcevir(Ogrenci[i].ksýnav1) + ")";
							cout << setw(21) << Ogrenci[i].ksýnav2 + "(" + Notcevir(Ogrenci[i].ksýnav2) + ")";;
							cout << setw(15) << Ogrenci[i].ödev1 + "(" + Notcevir(Ogrenci[i].ödev1) + ")";;
							cout << setw(15) << Ogrenci[i].ödev2 + "(" + Notcevir(Ogrenci[i].ödev2) + ")";;
							cout << setw(15) << Ogrenci[i].proje + "(" + Notcevir(Ogrenci[i].proje) + ")";;
							cout << setw(14) << Ogrenci[i].vize + "(" + Notcevir(Ogrenci[i].vize) + ")";;
							cout << setw(15) << Ogrenci[i].final + "(" + Notcevir(Ogrenci[i].final) + ")";;
							cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
							cout << endl << endl;
						}
						cout << "Onceki 20 Kisiyi Gormek Icin 'a' Tusunu," << "Cikis Yapmak Icin Herhangi Bir Tusu Kullanin...";
						string kisi20secim;
						cin >> kisi20secim;
						//60 - 80 Arasindakilere Geri Donme Komutu.
						if (kisi20secim == "a")
						{
							goto atmýsseksen;
						}
						//Cikis.
						else
						{
							system("cls");
						}
					}
					//40 - 60 Arasindakilere Geri Donme Komutu.
					else if (kisi20secim == "a")
					{
						goto kýrkatmýs;
					}
					//Cikis.
					else
					{
						system("cls");
					}
				}
				//20 - 40 Arasindakilere Geri Donme Komutu.
				else if (kisi20secim == "a")
				{
					goto yirmikýrk;
				}
				//Cikis.
				else
				{
				system("cls");
				}
			}
			//0 - 20 Arasindakilere Geri Donme Komutu.
			else if (kisi20secim == "a")
			{
				goto sýfýryirmi;
			}
			//Cikis.
			else
			{
			system("cls");
			}
		}
		//Cikis.
		else
		{
		system("cls");
		}
	}
	//Sinifin En Yuksek Basari Notunu Hesaplama Komutu.
	else if(menusecim == "2")
	{
	system("cls");
	float eb = 0;
	string adsoyad;
	//100 Ogrencinin Basari Notu Inceleniyor.
	for (int i = 0; i < 100; i++)
	{
		//100 Ogrencinin Basari Notu Inceleniyor.
		if (Ogrenci[i].basarinot > eb)
		{
			eb = Ogrenci[i].basarinot;
			adsoyad = Ogrenci[i].Adi + " " + Ogrenci[i].Soyadi;
		}
	}
	cout << "En Yuksek Basari Notlu Ogrenci : " << adsoyad << " | Basari Notu : " << eb << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");
	}
	//Sinifin En Dusuk Basari Notunu Hesaplama Komutu.
	else if (menusecim == "3")
	{
	system("cls");
	float ek = 0;
	string adsoyad;
	ek = Ogrenci[0].basarinot;
	//100 Ogrencinin Basari Notu Inceleniyor.
	for (int i = 0; i < 100; i++)
	{
		//100 Ogrencinin Basari Notu Inceleniyor.
		if (Ogrenci[i].basarinot <= ek)
		{
			ek = Ogrenci[i].basarinot;
			adsoyad = Ogrenci[i].Adi + " " + Ogrenci[i].Soyadi;
		}
	}
	cout << "En Dusuk Basari Notlu Ogrenci : " << adsoyad << " | Basari Notu : " << ek << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");
	}
	//Sinifin Ortalamasini Hesaplama Komutu.
	else if (menusecim == "4")
	{
	system("cls");
	float tp = 0;
	float ort = 0;
	//Tum Ogrencilerin Basari Notlari Toplaniyor,Gerekli Islemler Yapilip Ortalama Bulunuyor.
	for (int i = 0; i < 100; i++)
	{
		tp += Ogrenci[i].basarinot;
	}
	ort = tp / 100;
	cout << "Sinif'in Basari Ortalamasi : " << ort << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");
	}
	//Sinifin Standart Sapmasi Hesaplaniyor.
	else if (menusecim == "5")
	{
	system("cls");
	float tp = 0;
	float ort = 0;
	float varyans = 0;
	float ssapma = 0;
	//Ogrencilerin Basari Notlari Toplaniyor.
	for (int i = 0; i < 100; i++)
	{
		tp += Ogrenci[i].basarinot;
	}
	ort = tp / 100;
	//Varyans Bulunuyor,Gerekli Islemler Yapilip Standart Sapma Bulunuyor.
	for (int i = 0; i < 100; i++)
	{
		varyans += ((Ogrenci[i].basarinot - ort)*(Ogrenci[i].basarinot - ort))/99;
	}
	ssapma = sqrt(varyans);
	cout << "Sinifin Standart Sapmasi : " << ssapma << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");
	}
	//Basari Notu Belirli Bir Aralikta Olanlari Listeleme Komutu.
	else if (menusecim == "6")
	{
	system("cls");
	string altara;
	string ustara;
	bool dogru;
	//Girilen Alt Ve Ust Degerlerin Dogrulugunu Kontrol Etmek Icin Do-While Dongusu Kullanildi.
	do
	{
		cout << "Istediginiz Araligin Alt Degerini Giriniz : ";
		cin >> altara;
		cout << "Istediginiz Araligin Ust Degerini Giriniz : ";
		cin >> ustara;
		//Alt Ve Ust Aralik Sayi Mi Diye Kontrol Ediliyor.
		if (isNumber(altara) && isNumber(ustara))
		{
			//Alt Aralik Ust Araliktan Buyuk Mu Diye Kontrol Ediliyor.
			if (stof(altara) > stof(ustara))
			{
				system("cls");
				cout << "Alt Deger Ust Degerden Fazla Olamaz..." << endl;
				dogru = false;
			}
			//Alt Aralik Ust Araliga Esit Mi Diye Kontrol Ediliyor.
			else if (stof(altara) == stof(ustara))
			{
				system("cls");
				cout << "Alt Deger Ust Degere Esit Olamaz..." << endl;
				dogru = false;
			}
			//Alt Aralik 0'dan Kucuk Veya Ust Aralik 100'den Buyuk Mu Diye Kontrol Ediliyor.
			else if (stof(altara) < 0 || stof(ustara) > 100)
			{
				system("cls");
				cout << "Alt Aralik 0'dan Kucuk , Ust Aralik 100'den Buyuk Olamaz..." << endl;
				dogru = false;
			}
			//Diger Durumlar Icin Atanacak "dogru" Degeri Ayarlaniyor.
			else
			{
				dogru = true;
			}
		}
		//Diger Durumlar Icin Yapilacak Kosul.
		else
		{
			system("cls");
			cout << "Alt Ve Ust Deger Rakamlardan Olusmalidir..." << endl;
			dogru = false;
		}
	} while (dogru == false);

	system("cls");

	cout << "Basari Notu " << altara << " - " << ustara << " Arasinda Olanlar..." << endl << endl;

	//Belirli Bir Basari Notu Araligindaki Ogrenciler Kontrol Edilip,Listeleniyor.
	for (int i = 0; i < 100; i++)
	{
		//Belirli Bir Basari Notu Araligindaki Ogrenciler Kontrol Edilip,Listeleniyor.
		if (Ogrenci[i].basarinot >= stof(altara) && Ogrenci[i].basarinot < stof(ustara))
		{
			cout << left;
			cout << setw(13) << "Adi";
			cout << setw(16) << "Soyadi";
			cout << setw(19) << "Dog.Tarih";
			cout << setw(20) << "No";
			cout << setw(21) << "Basari Notu";
			cout << endl;
			cout << setw(13) << Ogrenci[i].Adi;
			cout << setw(16) << Ogrenci[i].Soyadi;
			cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
			cout << setw(20) << "B" + Ogrenci[i].No;
			cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
			cout << endl << endl;
		}
		//...
		else
		{

		}
	}

	cout << "Basari Notu " << altara << " - " << ustara << " Arasinda Olanlar..." << endl << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");

	}
	//Basari Notu Belirli Bir Degerin Altinda Olan Ogrencileri Listeleme Komutu.
	else if (menusecim == "7")
	{
	system("cls");

	string degeraltinot;
	bool degeraltidogru;

	//Girilen Deger Uygun Kosullarda Mi Diye Kontrol Ediliyor.
	do
	{
		cout << "Hangi Basari Not Altindaki Ogrencileri Gormek Istediginizi Yaziniz : ";
		cin >> degeraltinot;

		//Girilen Deger Sayi Mi Diye Kontrol Ediliyor.
		if (isNumber(degeraltinot))
		{
			//Girilen Deger 0'dan Kucuk Mu Diye Kontrol Ediliyor.
			if (stof(degeraltinot) < 0)
			{
				system("cls");
				cout << "Girilen Deger Sifirdan Kucuk Olamaz..." << endl;
				degeraltidogru = false;
			}
			//Girilen Deger 100'den Buyuk Mu Diye Kontrol Ediliyor.
			else if (stof(degeraltinot) > 100)
			{
				system("cls");
				cout << "Girilen Deger Yuzden Buyuk Olamaz..." << endl;
				degeraltidogru = false;
			}
			//Diger Durumlar Icin Atanacak "degeraltidogru" Degeri Ayarlaniyor.
			else
			{
				degeraltidogru = true;
			}
		}
		//Diger Durumlar Icin Yapilacak Kosul.
		else
		{
			system("cls");
			cout << "Girilen Deger Rakamlardan Olusmalidir..." << endl;
			degeraltidogru = false;
		}
	} while (degeraltidogru == false);

	system("cls");

	cout << "Basari Notu " << degeraltinot << " Altinda Olanlar..." << endl << endl;

	//Belirli Bir Basari Notunun Altindaki Ogrenciler Kontrol Edilip,Listeleniyor.
	for (int i = 0; i < 100; i++)
	{
		//Belirli Bir Basari Notunun Altindaki Ogrenciler Kontrol Edilip,Listeleniyor.
		if (Ogrenci[i].basarinot < stof(degeraltinot))
		{
			cout << left;
			cout << setw(13) << "Adi";
			cout << setw(16) << "Soyadi";
			cout << setw(19) << "Dog.Tarih";
			cout << setw(20) << "No";
			cout << setw(21) << "Basari Notu";
			cout << endl;
			cout << setw(13) << Ogrenci[i].Adi;
			cout << setw(16) << Ogrenci[i].Soyadi;
			cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
			cout << setw(20) << "B" + Ogrenci[i].No;
			cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
			cout << endl << endl;
		}
		//...
		else
		{

		}
	}

	cout << "Basari Notu " << degeraltinot << " Altinda Olanlar..." << endl << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");

	}
	//Basari Notu Belirli Bir Degerin Ustunde Olan Ogrencileri Listeleme Komutu.
	else if (menusecim == "8")
	{
	system("cls");

	string degerustunot;
	bool degerustudogru;

	//Girilen Deger Uygun Kosullarda Mi Diye Kontrol Ediliyor.
	do
	{
		cout << "Hangi Basari Not Uzerindeki Ogrencileri Gormek Istediginizi Yaziniz : ";
		cin >> degerustunot;
		//Girilen Deger Sayi Mi Diye Kontrol Ediliyor.
		if (isNumber(degerustunot))
		{
			//Girilen Deger 0'dan Kucuk Mu Diye Kontrol Ediliyor.
			if (stof(degerustunot) < 0)
			{
				system("cls");
				cout << "Girilen Deger Sifirdan Kucuk Olamaz..." << endl;
				degerustudogru = false;
			}
			//Girilen Deger 100'den Buyuk Mu Diye Kontrol Ediliyor.
			else if (stof(degerustunot) > 100)
			{
				system("cls");
				cout << "Girilen Deger Yuzden Buyuk Olamaz..." << endl;
				degerustudogru = false;
			}
			//Diger Durumlar Icin Atanacak "degerustudogru" Degeri Ayarlaniyor.
			else
			{
				degerustudogru = true;
			}
		}
		//Diger Durumlar Icin Yapilacak Kosul.
		else
		{
			system("cls");
			cout << "Girilen Deger Rakamlardan Olusmalidir..." << endl;
			degerustudogru = false;
		}
	} while (degerustudogru == false);

	system("cls");
	
	cout << "Basari Notu " << degerustunot << " Ustunde Olanlar..." << endl << endl;

	//Belirli Bir Basari Notunun Ustundeki Ogrenciler Kontrol Edilip,Listeleniyor.
	for (int i = 0; i < 100; i++)
	{
		//Belirli Bir Basari Notunun Ustundeki Ogrenciler Kontrol Edilip,Listeleniyor.
		if (Ogrenci[i].basarinot > stof(degerustunot))
		{
			cout << left;
			cout << setw(13) << "Adi";
			cout << setw(16) << "Soyadi";
			cout << setw(19) << "Dog.Tarih";
			cout << setw(20) << "No";
			cout << setw(21) << "Basari Notu";
			cout << endl;
			cout << setw(13) << Ogrenci[i].Adi;
			cout << setw(16) << Ogrenci[i].Soyadi;
			cout << setw(19) << Ogrenci[i].t1.Gün + "/" + Ogrenci[i].t1.Ay + "/" + Ogrenci[i].t1.Yýl;
			cout << setw(20) << "B" + Ogrenci[i].No;
			cout << setw(21) << to_string(Ogrenci[i].basarinot) + "(" + Notcevir(to_string(int(Ogrenci[i].basarinot))) + ")";
			cout << endl << endl;
		}
	}
	
	cout << "Basari Notu " << degerustunot << " Ustunde Olanlar..." << endl << endl;
	cout << "Menuye Gitmek Icin Herhangi Bir Tusa Basiniz...";
	_getch();
	cout << endl;
	system("cls");

	}
	//Menuden Cikis Yapma Komutu.
	else if (menusecim == "9")
	{
	
	system("cls");
	cout << "Cikis Yapiliyor...";
	Sleep(1000);
	return 0;
	
	}
	//Uyari Mesaji Ve Dongunun Basina Donus.
	else
	{

	system("cls");
	
	cout << "Lutfen Gecerli Bir Komut Giriniz..." << endl;
	
	}

	menu();

	} while (menusecim != "9");
	return 0;
}

//Alinan String Degerinin Sayi Olup Olmadigini Kontrol Eden,Bool Deger Donduren Bir Fonksiyon.
bool isNumber(const string& s)
{
	for (char const& ch : s) {
		if (std::isdigit(ch) == 0)
			return false;
	}
	return true;
}

//Alinan String Degerinin Sayi Olup Olmadigini Kontrol Eden Daha Sonrasinda O String Degerine Baska Deger Atayip Geriye String Donduren Fonk.
string Notcevir(string notgel)
{
	string notal;
	//Parametredeki String Degerinin Sayi Olup Olmadigi Kontrol Ediliyor.
	if (isNumber(notgel))
	{
		stoi(notgel);
		//Alinan String Degerinin Sayisal Degeri 90 - 100 Arasinda Ise Uygulanacak Islem.
		if (stoi(notgel) <= 100 && stoi(notgel) >= 90)
		{
			notal = "AA";
		}
		//Alinan String Degerinin Sayisal Degeri 85 - 89 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 85 && stoi(notgel) <= 89)
		{
			notal = "BA";
		}
		//Alinan String Degerinin Sayisal Degeri 80 - 84 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 80 && stoi(notgel) <= 84)
		{
			notal = "BB";
		}
		//Alinan String Degerinin Sayisal Degeri 70 - 79 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 70 && stoi(notgel) <= 79)
		{
			notal = "CB";
		}
		//Alinan String Degerinin Sayisal Degeri 60 - 69 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 60 && stoi(notgel) <= 69)
		{
			notal = "CC";
		}
		//Alinan String Degerinin Sayisal Degeri 55 - 59 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 55 && stoi(notgel) <= 59)
		{
			notal = "DC";
		}
		//Alinan String Degerinin Sayisal Degeri 50 - 54 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 50 && stoi(notgel) <= 54)
		{
			notal = "DD";
		}
		//Alinan String Degerinin Sayisal Degeri 40 - 49 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 40 && stoi(notgel) <= 49)
		{
			notal = "FD";
		}
		//Alinan String Degerinin Sayisal Degeri 0 - 39 Arasinda Ise Uygulanacak Islem.
		else if (stoi(notgel) >= 0 && stoi(notgel) <= 39)
		{
			notal = "FF";
		}
	}
	return notal;
}

//Gelen String Degerinin Uygun Kosullarda Olup Olmadigini Kontrol Eden,Geriye Bool Deger Donduren Bir Fonksiyon.
bool kontrol(string notk)
{
	bool nkontrol;
	//Gelen Deger Sayi Mi Diye Kontrol Ediliyor.
	if (isNumber(notk))
	{
		//Alinan String Degerinin Sayisal Degeri 0-100 Arasinda Mi Diye Kontrol Ediliyor.
		if (stoi(notk) >= 0 && stoi(notk) <= 100)
		{
			nkontrol = true;
		}
		//Diger Durumlar Icin Calisacak Uyari Mesaji.
		else
		{
			cout << "Notlar 0 Ile 100 Arasinda Olmalidir..." << endl;
			nkontrol = false;
		}
	}
	//Diger Durumlar Icin Calisacak Uyari Mesaji.
	else
	{
		cout << "Notlar Rakamlardan Olusmalidir..." << endl;
		nkontrol = false;
	}
	return nkontrol;
}

//Menuyu Listeleyen Geriye Deger Dondurmeyen (void) Bir Fonksiyon.
void menu()
{
	cout << "Yapmak Istediginiz Islemi Seciniz..." << endl;
	cout << "[1] : Sinif Listesi Yazdirma (Her Seferde 20 Ogrenci)" << endl;
	cout << "[2] : Sinifin En Yuksek Notunu Gorme" << endl;
	cout << "[3] : Sinifin En Dusuk Notunu Gorme" << endl;
	cout << "[4] : Sinifin Ortalamasini Gorme" << endl;
	cout << "[5] : Sinifin Standart Sapmasini Gorme" << endl;
	cout << "[6] : Basari Notu Belirli Bir Aralikta Olanlari Gorme (Orn:50-80 Arasinda Olanlari Gorme)" << endl;
	cout << "[7] : Basari Notu Belirli Bir Notun Altinda Olanlari Gorme (Orn:70'in Altinda Olanlari Gorme)" << endl;
	cout << "[8] : Basari Notu Belirli Bir Notun Uzerinde Olanlari Gorme (Orn:70'in Ustunde Olanlari Gorme)" << endl;
	cout << "[9] : Cikis Yap" << endl;
}

//Ogrenci Numarasini Uygun Kosullarda Mi Diye Kontrol Eden,Geriye Bool Deger Donduren Fonksiyon.
bool ogrnokontrol(string nok)
{
	bool ogrnkontrol = true;
	//Alinan String Degeri Sayi Mi Diye Kontrol Ediliyor.
	if (isNumber(nok))
	{
		//Alinan String Degerinin Sayisal Degeri Uygun Kosullarda Mi Diye Kontrol Ediliyor Ve "ogrnkontrol" Degeri Ataniyor.
		if (stoi(nok) >= 100000000 && stoi(nok) <= 999999999)
		{
			ogrnkontrol = true;
			//Ogrencilerin Numaralari Ayni Gelebilir Ama Bunu Engeleyen Bir Fonksiyon Yapmam Odev Dosyasinda Soylenmedi
		}
		//Diger Durumlar Icin Uyari Mesaji Ve "ogrnkontrol" Deger Atamasi.
		else
		{
			cout << "Ogrenci Numarasi 9 Haneli Olmalidir..." << endl;
			ogrnkontrol = false;
		}
	}
	//Diger Durumlar Icin Uyari Mesaji Ve "ogrnkontrol" Deger Atamasi.
	else
	{
		cout << "Ogrenci Numarasi Rakamlardan Olusmalidir..." << endl;
		ogrnkontrol = false;
	}
	return ogrnkontrol;
}

//Gelen 3 String Degerinide Uygun Kosullardami Diye Kontrol Edip Geriye Bool Deger Donduren Fonksiyon.
bool ogrtarihkontrol(string gun, string ay, string yil)
{
	bool tkontrol;
	//"yil" Degeri Sayi Mi Diye Kontrol Ediliyor.
	if (isNumber(yil))
	{
		//"yil" Degerinin Sayisal Degeri Belirtilen Aralikta Mi Diye Kontrol Ediliyor.
		if (stoi(yil) >= 1000 && stoi(yil) <= 9999)
		{
			//"ay" Degeri Sayi Mi Diye kontol Ediliyor.
			if (isNumber(ay))
			{
				//"ay" Degerinin Sayisal Degeri Belirtilen Aralikta Mi Diye Kontrol Ediliyor.
				if (stoi(ay) >= 1 && stoi(ay) <= 12)
				{
					//"gun" Degeri Sayi Mi Diye Kontol Ediliyor.
					if (isNumber(gun))
					{
						//"ay" Degerinin Farkli Durumlardaki Halleri Degerlendiriyor
						if (ay == "1" || ay == "3" || ay == "5" || ay == "7" || ay == "8" || ay == "10" || ay == "12")
						{
							//"gun" Degerinin Sayisal Degeri Belirtilen Aralikta Mi Diye Kontrol Ediliyor.
							if (stoi(gun) >= 1 && stoi(gun) <= 31)
							{
								tkontrol = true;
							}
							//Diger Durumlarda Gonderilecek Uyari Mesaji.
							else
							{
								cout << "Lutfen O Ay Icinde Olan Bir Gunu Seciniz..." << endl;
								tkontrol = false;
							}
						}
						//"ay" Degerinin Farkli Durumlardaki Halleri Degerlendiriyor
						else if (ay == "2")
						{
							//"gun" Degerinin Sayisal Degeri Belirtilen Aralikta Mi Diye Kontrol Ediliyor.
							if (stoi(gun) >= 1 && stoi(gun) <= 28)
							{
								tkontrol = true;
							}
							//Diger Durumlarda Gonderilecek Uyari Mesaji.
							else
							{
								cout << "Lutfen O Ay Icinde Olan Bir Gunu Seciniz..." << endl;
								tkontrol = false;
							}
						}
						//"ay" Degerinin Farkli Durumlardaki Halleri Degerlendiriyor
						else if (ay == "4" || ay == "6" || ay == "9" || ay == "11")
						{
							//"gun" Degerinin Sayisal Degeri Belirtilen Aralikta Mi Diye Kontrol Ediliyor.
							if (stoi(gun) >= 1 && stoi(gun) <= 30)
							{
								tkontrol = true;
							}
							//Diger Durumlarda Gonderilecek Uyari Mesaji.
							else
							{
								cout << "Lutfen O Ay Icinde Olan Bir Gunu Seciniz..." << endl;
								tkontrol = false;
							}
						}
					}
					//Diger Durumlarda Gonderilecek Uyari Mesaji.
					else
					{
						cout << "Dogum Gunu Rakamlardan Olusmalidir...." << endl;
						tkontrol = false;
					}
				}
				//Diger Durumlarda Gonderilecek Uyari Mesaji.
				else
				{
					cout << "Dogum Ayi 1-12 Arasinda Olmalidir" << endl;
					tkontrol = false;
				}
			}
			//Diger Durumlarda Gonderilecek Uyari Mesaji.
			else
			{
				cout << "Dogum Ayi Rakamlardan Olusmalidir...." << endl;
				tkontrol = false;
			}
		}
		//Diger Durumlarda Gonderilecek Uyari Mesaji.
		else
		{
			cout << "Dogum Yili 4 Basamakli Olmalidir" << endl;
			tkontrol = false;
		}
	}
	//Diger Durumlarda Gonderilecek Uyari Mesaji.
	else
	{
		cout << "Dogum Yili Rakamlardan Olusmalidir...." << endl;
		tkontrol = false;
	}
	return tkontrol;
}
