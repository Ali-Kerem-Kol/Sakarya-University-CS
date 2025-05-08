/**
* @file B221210042
* @description AVL Ağaçlarının ve Yığıtların Aynı Anda Depolandığı Bağlı Listenin Başlık Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/



#ifndef BAGLILISTE_HPP
#define BAGLILISTE_HPP

#include "AVLAgaci.hpp"
#include "Dugum.hpp"
#include "Node.hpp"
#include "Yigin.hpp"

class BagliListe
{
private:
    Node* head;
    int size;

    Node* FindPrevByPosition(int position);

public:
    BagliListe();

    Node* getHead();

    bool isEmpty() const;

    int count() const;

    const Node* first();

    const Node* last();

    void add(const AVLAgaci* agac, const Yigin* yigin, int avlNo);

    void insert(int index, const AVLAgaci* agac, const Yigin* yigin, int avlNo);

    void remove(const int& avlNo);

    void removeAt(int index);

    int indexOf(const int& avlNo);

    bool find(const int& avlNo);

    void clear();

    Node* findSmallestNode();

    Node* findLargestNode();

    bool bosYiginVarMi();

    Node* bosYiginiBul();

    ~BagliListe();
};

#endif