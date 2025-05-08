/**
* @file B221210042
* @description Yığın Sınıfının Kaynak Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/




#include "Yigin.hpp"

#include <iostream>
#include <fstream>
#include <algorithm>
#include <sstream> ///
#include <climits> ///
#include <iomanip> ///



Yigin::Yigin()
{
    tepe = -1;
    kapasite = 4;
    veriler = new int[kapasite];
}

void Yigin::genislet(int boyut)
{
    int* yenialan = new int[kapasite + boyut];

    for (int i = 0; i <= tepe; i++)
    {
        yenialan[i] = veriler[i];
    }
    delete[] veriler;

    veriler = yenialan;

    kapasite = kapasite + boyut;
}

void Yigin::Push(int veri)
{
    if (tepe == kapasite - 1)
    {
        genislet(kapasite);
    }
    tepe++;
    veriler[tepe] = veri;
}

void Yigin::Pop()
{
    if (tepe != -1)
    {
        tepe--;
    }
}

bool Yigin::BosMu()
{
    return (tepe >= 0);
}

int Yigin::getir()
{
    return veriler[tepe];
}

void Yigin::clear()
{
    tepe = -1;
}

Yigin::~Yigin()
{
    clear();
    delete[] veriler;
}
