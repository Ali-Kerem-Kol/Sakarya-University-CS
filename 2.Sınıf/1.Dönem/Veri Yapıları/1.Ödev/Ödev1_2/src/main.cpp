#include <iostream>
#include <fstream>
#include <sstream>
#include <limits>
#include "KLinkedList.hpp"  // DNA ve kromozomların yönetileceği sınıf

using namespace std;

// Geçerli indeks kontrol fonksiyonu
bool validIndex(KLinkedList* dna, int index) {
    return index >= 0 && index < dna->count();
}

// Geçerli giriş kontrol fonksiyonu
bool validInput(int& input) {
    cin >> input;
    if (cin.fail()) {
        cin.clear();
        cin.ignore(numeric_limits<streamsize>::max(), '\n');
        cout << "Gecersiz giris! Lutfen bir sayi girin." << endl;
        return false;
    }
    return true;
}

// Otomatik işlemler fonksiyonu
void otomatikIslemler(KLinkedList* dna) {
    ifstream islemDosyasi("Islemler.txt");
    if (!islemDosyasi.is_open()) {
        cerr << "Islemler.txt dosyasi acilamadi!" << endl;
        return;
    }

    string line;
    while (getline(islemDosyasi, line)) {
        stringstream ss(line);
        char islem;
        int k1, k2;

        ss >> islem >> k1 >> k2;

        // İşlem tipi ve indeks geçerliliğini kontrol ediyoruz
        if (islem == 'C') {
            if (validIndex(dna, k1) && validIndex(dna, k2)) {
                dna->crossover(*dna->GetNodeAt(k1)->sayi, *dna->GetNodeAt(k2)->sayi);
                cout << "Caprazlama islemi tamamlandi. (" << k1 << ", " << k2 << ")" << endl;
            } else {
                cout << "Gecersiz kromozom indexleri! (" << k1 << ", " << k2 << ")" << endl;
            }
        } else if (islem == 'M') {
            if (validIndex(dna, k1)) {
                dna->GetNodeAt(k1)->sayi->mutate(k2);
                cout << "Mutasyon islemi tamamlandi. (" << k1 << ", " << k2 << ")" << endl;
            } else {
                cout << "Gecersiz kromozom indexi! (" << k1 << ")" << endl;
            }
        } else {
            cout << "Gecersiz islem türü: " << islem << endl;
        }
    }

    islemDosyasi.close();
    cout << "Otomatik islemler tamamlandi." << endl;
}


// DNA'yı ekrana yazdıran fonksiyon
void ekranaYaz(KLinkedList* dna) {
    dna->print();
}

int main() {
    system("cls");
    KLinkedList* dna = new KLinkedList();

    ifstream file("Dna.txt");
    if (!file.is_open()) {
        cerr << "Dna.txt dosyasi acilamadi!" << endl;
        delete dna;
        return 1;
    }

    // Dosyadan DNA okuma
    string line;
    while (getline(file, line)) {
        stringstream ss(line);
        string gen;
        GLinkedList* kromozom = new GLinkedList();
        while (ss >> gen) {
            kromozom->add(gen);
        }
        dna->add(*kromozom);
    }
    file.close();

    int secim;
    do {
        cout << "1- Caprazlama\n2- Mutasyon\n3- Otomatik Islemler\n4- Ekrana Yaz\n5- Kromozom Haritasi\n6- Cikis\nSeciminizi girin: ";
        while (!validInput(secim)) {
            system("cls");
            cout << "Gecersiz giris! Lutfen bir sayi girin." << endl;
            cout << "1- Caprazlama\n2- Mutasyon\n3- Otomatik Islemler\n4- Ekrana Yaz\n5- Kromozom Haritasi\n6- Cikis\nSeciminizi girin: ";
        }

        if (secim == 1) {
            int k1, k2;
            cout << "Caprazlama icin iki kromozom satir numarasini girin: ";
            while (!validInput(k1) || !validInput(k2)) {
                cout << "Caprazlama icin iki kromozom satir numarasini girin: ";
            }
            system("cls");
            if (validIndex(dna, k1) && validIndex(dna, k2)) {
                dna->crossover(*dna->GetNodeAt(k1)->sayi, *dna->GetNodeAt(k2)->sayi);
                //ekranaYaz(dna);
            } else {
                cout << "Gecersiz kromozom indexleri! (" << k1 << ", " << k2 << ")" << endl;
            }

        } else if (secim == 2) {
            int kromozomNo, genNo;
            cout << "Mutasyon icin kromozom satir numarasi ve gen sutun numarasini girin: ";
            while (!validInput(kromozomNo) || !validInput(genNo)) {
                cout << "Mutasyon icin kromozom satir numarasi ve gen sutun numarasini girin: ";
            }
            system("cls");
            if (validIndex(dna, kromozomNo)) {
                dna->GetNodeAt(kromozomNo)->sayi->mutate(genNo);
                cout << "Mutasyon islemi tamamlandi." << endl;
                //ekranaYaz(dna);
            } else {
                cout << "Gecersiz kromozom indexi! (" << kromozomNo << ")" << endl;
            }

        } else if (secim == 3) {
            system("cls");
            otomatikIslemler(dna);

        } else if (secim == 4) {
            system("cls");
            dna->prints();
            cout << endl;

        } else if (secim == 5) {
            system("cls");
            ekranaYaz(dna);
        } else if (secim != 6) {
            system("cls");
            cout << "Gecersiz secim, tekrar deneyin." << endl;
        }
        
    } while (secim != 6);

    cout << "Programdan cikiliyor." << endl;
    delete dna;
    return 0;
}

/*
C 1 3
C 0 6
M 5 1
----------
A C F Y U D K R
D E V U
O L Z E R Q W X A C
M U A D T R
 */