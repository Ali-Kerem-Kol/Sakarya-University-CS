/**
* @file Odev <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
* @description Program bir txt dosyasından sayıları okur ve bu sayıları bellekte(heap bölgesi) tutarak üzerinde işlemler yapar.
* @course MFA YouTube Kanalı ve Kayhan Ayar YouTube Kanalı
* @assignment 1.Ödev
* @date 28.10.2023
* @author Ali Kerem Kol / ali_.kerem@hotmail.com
*/
#include <iostream>
#include <fstream>
#include <sstream>
#include "SayilarListesi.hpp"
#include <iostream>
#include <limits>
using namespace std;


void sayilariYazdir(const SayilarListesi& sayilarListesi)
{
	Dugum* itrDugum = sayilarListesi.GetHead();
	int sira = 1;

	while (itrDugum != nullptr)
	{
		cout << sira << ". Sayi = ";

		Basamak* itrBasamak = itrDugum->sayi->GetHead();
		int basamakSira = 1;

		while (itrBasamak != nullptr)
		{
			cout << itrBasamak->rakam;
			itrBasamak = itrBasamak->next;
		}

		cout << " || " << sira << ". Sayi Adresi = " << itrDugum->sayi << endl;

		itrBasamak = itrDugum->sayi->GetHead();

		while (itrBasamak != nullptr)
		{
			cout << basamakSira << ". Basamak Degeri = " << itrBasamak->rakam << " || ";
			cout << basamakSira << ". Basamak Adresi = " << itrBasamak << endl;

			itrBasamak = itrBasamak->next;
			basamakSira++;
		}

		cout << endl;
		itrDugum = itrDugum->next;
		sira++;
	}
}

void tekRakamlariBasaAl(SayilarListesi& sayilarListesi)
{
	Dugum* itrDugum = sayilarListesi.GetHead();

	while (itrDugum != nullptr)
	{
		Sayi* itrSayi = itrDugum->sayi;
		Basamak* itrBasamak = itrSayi->GetHead();
		Basamak* prevBasamak = nullptr;

		while (itrBasamak != nullptr)
		{
			if (itrBasamak->rakam % 2 == 1)
			{
				if (prevBasamak != nullptr)
				{

					prevBasamak->next = itrBasamak->next;

					itrBasamak->next = itrSayi->GetHead();

					itrSayi->setHead(itrBasamak);

					itrBasamak = prevBasamak->next;
				}
				else
				{

					prevBasamak = itrBasamak;
					itrBasamak = itrBasamak->next;
				}
			}
			else
			{

				prevBasamak = itrBasamak;
				itrBasamak = itrBasamak->next;
			}
		}
		if (prevBasamak != nullptr)
		{
			prevBasamak->next = nullptr;
		}

		itrDugum = itrDugum->next;
	}
}

void tersCevir(SayilarListesi& sayilarListesi)
{
	Dugum* itrDugum = sayilarListesi.GetHead();

	while (itrDugum != nullptr)
	{
		Basamak* prev = nullptr;
		Basamak* itrBasamak = itrDugum->sayi->GetHead();

		while (itrBasamak != nullptr)
		{

			Basamak* tempNext = itrBasamak->next;

			itrBasamak->next = prev;

			prev = itrBasamak;

			itrBasamak = tempNext;
		}
		itrDugum->sayi->setHead(prev);

		itrDugum = itrDugum->next;
	}
}

void enBuyuguCikar(SayilarListesi& sayilarListesi)
{
	Dugum* itrDugum = sayilarListesi.GetHead();

	if (itrDugum == NULL)
	{
		cout << "Sayi kalmadi." << endl;
		return;
	}

	Sayi* EBsayi = itrDugum->sayi;
	int EBsayiSize = itrDugum->sayi->count();

	while (itrDugum != nullptr)
	{
		Basamak* itrBasamak = itrDugum->sayi->GetHead();
		Basamak* EBsayiBasamak = EBsayi->GetHead();
		int tempSize = itrDugum->sayi->count();
		if (tempSize > EBsayiSize)
		{
			EBsayi = itrDugum->sayi;
			EBsayiSize = tempSize;
		}
		else if (tempSize == EBsayiSize)
		{

			while (itrBasamak != nullptr && itrBasamak->rakam == 0)
			{
				itrBasamak = itrBasamak->next;
			}

			while (EBsayiBasamak != nullptr && EBsayiBasamak->rakam == 0)
			{
				EBsayiBasamak = EBsayiBasamak->next;
			}

			while (itrBasamak != nullptr)
			{
				int tempDeger = itrBasamak->rakam;
				int EBDeger = EBsayiBasamak->rakam;

				if (tempDeger > EBDeger)
				{
					EBsayi = itrDugum->sayi;
					EBsayiSize = tempSize;
					break;
				}
				else if (tempDeger < EBDeger)
				{
					break;
				}

				itrBasamak = itrBasamak->next;
				EBsayiBasamak = EBsayiBasamak->next;
			}
		}

		itrDugum = itrDugum->next;
	}

	sayilarListesi.remove(*EBsayi);
}


int main()
{
	system("COLOR A");

	ifstream dosya("Sayilar.txt");
	if (!dosya)
	{
		cout << "Dosya açılamadı..." << endl;
		return 1;
	}

	SayilarListesi* sayilarListesi = new SayilarListesi();

	Sayi* yeniSayi;

	int sayi;

	while (dosya >> sayi)
	{
		yeniSayi = new Sayi();
		int temp = sayi;
		while (temp > 0)
		{
			int basamak = temp % 10;
			yeniSayi->insert(0, basamak);
			temp /= 10;
		}
		sayilarListesi->add(*yeniSayi);

	}
	
	sayilariYazdir(*sayilarListesi);

	int secim;
	do
	{

		cout << "1. Tek basamaklari basa al" << endl;
		cout << "2. Basamaklari tersle" << endl;
		cout << "3. En buyuk sayiyi listeden cikar" << endl;
		cout << "4. Cikis" << endl;


		while (true) // Kullanicidan alinan degerin integer olup olmadigi kontrol ediliyor
		{
			cin >> secim;

			if(cin.fail())
			{
				cin.clear();
				cin.ignore(std::numeric_limits<std::streamsize>::max(), '\n');
				
				system("CLS");

				cout << "Gecersiz bir giris yaptiniz. Lutfen bir tam sayi giriniz." << endl;

				sayilariYazdir(*sayilarListesi);

				cout << "1. Tek basamaklari basa al" << endl;
				cout << "2. Basamaklari tersle" << endl;
				cout << "3. En buyuk sayiyi listeden cikar" << endl;
				cout << "4. Cikis" << endl;
			}
			else
			{
				break;
			}
		}

		system("CLS");

		switch (secim)
		{
		case 1:
			tekRakamlariBasaAl(*sayilarListesi);
			sayilariYazdir(*sayilarListesi);

			break;
		case 2:
			tersCevir(*sayilarListesi);
			sayilariYazdir(*sayilarListesi);
			break;
		case 3:
			enBuyuguCikar(*sayilarListesi);
			sayilariYazdir(*sayilarListesi);
			break;
		case 4:
			cout << "Cikis Yapiliyor..." << endl;
			break;
		default:
			cout << "Gecersiz secim. Tekrar deneyin." << endl;
			sayilariYazdir(*sayilarListesi);
		}

	} while (secim != 4);

	delete sayilarListesi;

	return 0;
}