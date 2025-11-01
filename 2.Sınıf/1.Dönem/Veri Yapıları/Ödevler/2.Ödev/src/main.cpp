/**
* @file B221210042
* @description Main Fonksiyonunun Kaynak Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/





#include "Dugum.hpp"
#include "Yigin.hpp"
#include "AVLAgaci.hpp"
#include "Node.hpp"
#include "BagliListe.hpp"

#include <iostream>
#include <fstream>
#include <algorithm>
#include <sstream>
#include <climits>
#include <iomanip>

using namespace std;


int main()
{
	system("CLS");
	system("COLOR A");

	BagliListe liste;

	ifstream dosya("Veri.txt");
	if (!dosya.is_open()) 
	{
		cerr << "Dosya acilamadi!" << endl;
		return 1;
	}

	string satir;
	int avlNo = 1;

	while (getline(dosya, satir)) {
		stringstream ss(satir);
		AVLAgaci* agac = new AVLAgaci();
		Yigin* yigin = new Yigin();

		int deger;
		while (ss >> deger) 
		{
			agac->setHead(agac->ekle(deger, agac->getHead()));
		}

		agac->postOrder(agac->getHead(), *yigin);

		liste.add(agac, yigin, avlNo);

		avlNo++;
	}


	cout << endl;


	int sonASCII = 0;


	while (liste.count() != 1 && !liste.isEmpty())
	{
		int silinecekAVLNo;
		while (!liste.bosYiginVarMi())
		{
			Node* enKucuk = liste.findSmallestNode();
			enKucuk->yigin->Pop();
			silinecekAVLNo = enKucuk->avlNo;
			if (liste.bosYiginVarMi()) continue;
			Node* enBuyuk = liste.findLargestNode();
			enBuyuk->yigin->Pop();
			silinecekAVLNo = enBuyuk->avlNo;
		}

		liste.remove(silinecekAVLNo);



		for (Node* itr = liste.getHead();itr != NULL;itr = itr->next)
		{
			if (itr->agac == nullptr) continue;
			itr->yigin->clear();
			itr->agac->postOrder(itr->agac->getHead(), *itr->yigin);
		}


		for (Node* itr = liste.getHead();itr != NULL;itr = itr->next)
		{
			if (itr->agac == nullptr) continue;

			int toplam = itr->agac->yapraksizToplam(itr->agac->getHead());

			int ascii = toplam % (90 - 65 + 1) + 65;

			cout << static_cast<char>(ascii);

			sonASCII = ascii;

		}




		system("cls");
	}


	cout << "==============================" << endl;
	cout << setw(6) << left << "|" << setw(24) << right << "|" << endl;
	cout << setw(6) << left << "|" << setw(24) << right << "|" << endl;
	cout << setw(6) << left << "|" << "Son Karakter: " << setw(5) << left << static_cast<char>(sonASCII) << setw(5) << right << "|" << endl;
	cout << setw(6) << left << "|" << "AVL No      : " << setw(5) << left << liste.first()->avlNo << setw(5) << right << "|" << endl;
	cout << setw(6) << left << "|" << setw(24) << right << "|" << endl;
	cout << setw(6) << left << "|" << setw(24) << right << "|" << endl;
	cout << "==============================" << endl;



	dosya.close();

	liste.clear();

	return 0;


}