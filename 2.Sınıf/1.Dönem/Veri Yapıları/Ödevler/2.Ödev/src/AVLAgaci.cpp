/**
* @file B221210042
* @description Verilerin Depolandığı AVL ağaçlarının Kaynak Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/




#include "Yigin.hpp"
#include "Dugum.hpp"
#include "AVLAgaci.hpp"

#include <algorithm>
#include <iostream>
#include <fstream>
#include <sstream> ///
#include <climits> ///
#include <iomanip> ///


void AVLAgaci::temizle(Dugum* aktifDugum)
{
	if (aktifDugum != nullptr)
	{
		temizle(aktifDugum->sol);
		temizle(aktifDugum->sag);
		delete aktifDugum;
		aktifDugum = nullptr;
	}
}


AVLAgaci::AVLAgaci()
{
    root = 0;
}

Dugum* AVLAgaci::getHead()
{
	return root;
}

void AVLAgaci::setHead(Dugum* veri)
{
	root = veri;
}


int AVLAgaci::yapraksizToplam(Dugum* aktifDugum)
{
    if (aktifDugum == 0)
        return 0;

    if (aktifDugum->sol != nullptr || aktifDugum->sag != nullptr)
    {
        return (aktifDugum->veri) + (yapraksizToplam(aktifDugum->sol)) + (yapraksizToplam(aktifDugum->sag));
    }
    else
    {
        return 0;
    }
}

int AVLAgaci::minDeger(Dugum* aktif)
{
    while (aktif->sol != NULL)
    {
        aktif = aktif->sol;
    }
    return aktif->veri;
}

void AVLAgaci::postOrder(Dugum* aktif, Yigin& yigin)
{
    if (aktif)
    {
        postOrder(aktif->sol, yigin);
        postOrder(aktif->sag, yigin);
        if (aktif->sol == NULL && aktif->sag == NULL)
        {
            yigin.Push(aktif->veri);
        }
    }
}

int AVLAgaci::yukseklik(Dugum* aktifDugum)
{
    if (aktifDugum)
    {
        return 1 + std::max(yukseklik(aktifDugum->sol), yukseklik(aktifDugum->sag));
    }
    return -1;
}

int AVLAgaci::dengesizlikYonu(Dugum* aktif)
{
    if (aktif == 0)
        return 0;

    return yukseklik(aktif->sol) - yukseklik(aktif->sag);
}

Dugum* AVLAgaci::sagaDondur(Dugum* buyukEbeveyn)
{
    Dugum* solCocuk = buyukEbeveyn->sol;
    buyukEbeveyn->sol = solCocuk->sag;
    solCocuk->sag = buyukEbeveyn;
    return solCocuk;
}

Dugum* AVLAgaci::solaDondur(Dugum* buyukEbeveyn)
{
    Dugum* sagCocuk = buyukEbeveyn->sag;
    buyukEbeveyn->sag = sagCocuk->sol;
    sagCocuk->sol = buyukEbeveyn;
    return sagCocuk;
}

Dugum* AVLAgaci::ekle(int veri, Dugum* aktifDugum)
{
    if (aktifDugum == 0)
        return new Dugum(veri);

    if (aktifDugum->veri > veri)
    {
        aktifDugum->sol = ekle(veri, aktifDugum->sol);
        if (yukseklik(aktifDugum->sol) - yukseklik(aktifDugum->sag) > 1)
        {
            if (veri < aktifDugum->sol->veri)
            {
                aktifDugum = sagaDondur(aktifDugum);
            }
            else
            {
                aktifDugum->sol = solaDondur(aktifDugum->sol);
                aktifDugum = sagaDondur(aktifDugum);
            }
        }
    }
    else if (aktifDugum->veri < veri)
    {
        aktifDugum->sag = ekle(veri, aktifDugum->sag);
        if (yukseklik(aktifDugum->sag) - yukseklik(aktifDugum->sol) > 1)
        {
            if (veri > aktifDugum->sag->veri)
            {
                aktifDugum = solaDondur(aktifDugum);
            }
            else
            {
                aktifDugum->sag = sagaDondur(aktifDugum->sag);
                aktifDugum = solaDondur(aktifDugum);
            }
        }
    }

    return aktifDugum;
}

Dugum* AVLAgaci::sil(int veri, Dugum* aktif)
{
    if (aktif == 0)
        return 0;

    if (veri < aktif->veri)
    {
        aktif->sol = sil(veri, aktif->sol);
    }
    else if (veri > aktif->veri)
    {
        aktif->sag = sil(veri, aktif->sag);
    }
    else
    {
        if (aktif->sol == 0 && aktif->sag == 0)
        {
            delete aktif;
            aktif = 0;
        }
        else if (aktif->sol == 0)
        {
            Dugum* sil = aktif->sag;
            *aktif = *aktif->sag;
            delete sil;
        }
        else if (aktif->sag == 0)
        {
            Dugum* sil = aktif->sol;
            *aktif = *aktif->sol;
            delete sil;
        }
        else
        {
            aktif->veri = minDeger(aktif->sag);
            sil(aktif->veri, aktif->sag);
        }
    }

    int denge = dengesizlikYonu(aktif);

    if (denge > 1)
    {
        if (dengesizlikYonu(aktif->sol) >= 0)
        {
            return sagaDondur(aktif);
        }
        if (dengesizlikYonu(aktif->sol) < 0)
        {
            aktif->sol = solaDondur(aktif->sol);
            return sagaDondur(aktif);
        }
    }
    else if (denge < -1)
    {
        if (dengesizlikYonu(aktif->sag) <= 0)
        {
            return solaDondur(aktif);
        }
        if (dengesizlikYonu(aktif->sag) > 0)
        {
            aktif->sag = sagaDondur(aktif->sag);
            return solaDondur(aktif);
        }
    }

    return aktif;
}

void AVLAgaci::temizle()
{
    temizle(root);
    root = nullptr;
}

AVLAgaci::~AVLAgaci()
{
    temizle();
}
