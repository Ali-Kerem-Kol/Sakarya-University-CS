/**
* @file B221210042
* @description AVL Ağaçlarının Düğüm Sınıfının Başlık Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/




#ifndef DUGUM_HPP
#define DUGUM_HPP

class Dugum
{
public:
	int veri;
	Dugum* sol;
	Dugum* sag;
	Dugum(const int& data, Dugum* left = nullptr, Dugum* right = nullptr);


};

#endif
