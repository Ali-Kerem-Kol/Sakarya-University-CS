/**
* @file B221210042
* @description Bağlı Listenin Düğüm Sınıfının Kaynak Dosyası,Herbir Düğümde Bir AVL Ağacı ve O Ağaçla Eşleşmiş Bir Yığıt Bulunmakta.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/





#include "Node.hpp"
#include "AVLAgaci.hpp"
#include "Dugum.hpp"
#include "Yigin.hpp"

#include <iostream>
#include <fstream>
#include <algorithm>
#include <sstream> ///
#include <climits> ///
#include <iomanip> ///

Node::Node(const AVLAgaci* agac, const Yigin* yigin, int avlNo, Node* next)
    : agac(const_cast<AVLAgaci*>(agac)), yigin(const_cast<Yigin*>(yigin)), avlNo(avlNo), next(next) {}

Node::~Node()
{
    delete agac;
    delete yigin;
}
