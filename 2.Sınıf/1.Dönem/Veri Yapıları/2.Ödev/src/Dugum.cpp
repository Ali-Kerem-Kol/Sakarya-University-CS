/**
* @file B221210042
* @description AVL Ağaçlarının Düğüm Sınıfının Kaynak Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/




#include "Dugum.hpp"

#include <iostream>
#include <fstream>
#include <algorithm>
#include <sstream> ///
#include <climits> ///
#include <iomanip> ///



Dugum::Dugum(const int& data, Dugum* left, Dugum* right)
    : veri(data), sol(left), sag(right) {}
