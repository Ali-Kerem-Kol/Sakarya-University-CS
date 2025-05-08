/**
* @file B221210042
* @description Verilerin Depolandığı AVL Ağaçlarının Başlık Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/

#ifndef AVLAGACI_HPP
#define AVLAGACI_HPP

#include <algorithm>
#include "Dugum.hpp"
#include "Yigin.hpp"

class AVLAgaci
{
private:
	Dugum* root;
    void temizle(Dugum* aktifDugum);

public:

    AVLAgaci();
	
	Dugum* getHead();
	
	void setHead(Dugum* veri);

    int yapraksizToplam(Dugum* aktifDugum);

    int minDeger(Dugum* aktif);

    void postOrder(Dugum* aktif, Yigin& yigin);

    int yukseklik(Dugum* aktifDugum);

    int dengesizlikYonu(Dugum* aktif);

    Dugum* sagaDondur(Dugum* buyukEbeveyn);

    Dugum* solaDondur(Dugum* buyukEbeveyn);

    Dugum* ekle(int veri, Dugum* aktifDugum);

    Dugum* sil(int veri, Dugum* aktif);

    void temizle();

    ~AVLAgaci();
};


#endif