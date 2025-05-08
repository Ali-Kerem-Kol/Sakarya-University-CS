/**
* @file B221210042
* @description Bağlı Listenin Düğüm Sınıfının Başlık Dosyası,Herbir Düğümde Bir AVL Ağacı ve O Ağaçla Eşleşmiş Bir Yığıt Bulunmakta.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/




#ifndef NODE_HPP
#define NODE_HPP

#include "AVLAgaci.hpp"
#include "Yigin.hpp"
#include "Dugum.hpp"

class Node
{
public:
    AVLAgaci* agac;
    Yigin* yigin;
    int avlNo;
    Node* next;

    Node(const AVLAgaci* agac, const Yigin* yigin, int avlNo, Node* next = nullptr);

    ~Node();
};

#endif
