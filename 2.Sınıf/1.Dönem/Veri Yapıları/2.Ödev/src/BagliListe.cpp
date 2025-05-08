/**
* @file B221210042
* @description AVL Ağaçlarının ve Yığıtların Aynı Anda Depolandığı Bağlı Listenin Kaynak Dosyası.
* @course YouTube : MFA ve YouTube : Kayhan Ayar
* @assignment 2.Ödev
* @date 14/12/2023
* @author Ali Kerem Kol , E-Posta : ali.kol@ogr.sakarya.edu.tr
*/



#include "BagliListe.hpp"
#include "AVLAgaci.hpp"
#include "Dugum.hpp"
#include "Node.hpp"
#include "Yigin.hpp"

#include <iostream>
#include <fstream>
#include <algorithm>
#include <sstream> ///
#include <climits> ///
#include <iomanip> ///


Node* BagliListe::FindPrevByPosition(int position)
{
	if (position < 0 || position > size) throw "Index out of range";
	int index = 1;
	for (Node* itr = head;itr != NULL;itr = itr->next, index++)
	{
		if (position == index) return itr;
	}
	return NULL;
}


BagliListe::BagliListe()
{
    head = nullptr;
    size = 0;
}

Node* BagliListe::getHead()
{
    return head;
}

bool BagliListe::isEmpty() const
{
    return size == 0;
}

int BagliListe::count() const
{
    return size;
}

const Node* BagliListe::first()
{
    if (isEmpty())
        throw "Empty List";
    return head;
}

const Node* BagliListe::last()
{
    if (isEmpty())
        throw "Empty List";
    return FindPrevByPosition(size);
}

void BagliListe::add(const AVLAgaci* agac, const Yigin* yigin, int avlNo)
{
    insert(size, agac, yigin, avlNo);
}

void BagliListe::insert(int index, const AVLAgaci* agac, const Yigin* yigin, int avlNo)
{
    if (index == 0)
        head = new Node(agac, yigin, avlNo, head);
    else
    {
        Node* prev = FindPrevByPosition(index);
        prev->next = new Node(agac, yigin, avlNo, prev->next);
    }
    size++;
}

void BagliListe::remove(const int& avlNo)
{
    int index = indexOf(avlNo);
    removeAt(index);
}

void BagliListe::removeAt(int index)
{
    if (size == 0)
        throw "Empty List";
    Node* del;
    if (index == 0)
    {
        del = head;
        head = head->next;
    }
    else
    {
        Node* prev = FindPrevByPosition(index);
        del = prev->next;
        prev->next = prev->next->next;
    }
    delete del;
    size--;
}

int BagliListe::indexOf(const int& avlNo)
{
    if (avlNo < 1)
        throw "AVL Numaralari 1'den Baslar !!!";
    int index = 0;
    for (Node* itr = head; itr != NULL; itr = itr->next, index++)
    {
        if (itr->avlNo == avlNo)
            return index;
    }
    throw "Index out of range";
}

bool BagliListe::find(const int& avlNo)
{
    for (Node* itr = head; itr != NULL; itr = itr->next)
    {
        if (itr->avlNo == avlNo)
            return true;
    }
    return false;
}

void BagliListe::clear()
{
    while (!isEmpty())
    {
        removeAt(0);
    }
}

Node* BagliListe::findSmallestNode()
{
    if (!head)
        return nullptr;

    Node* current = head;
    Node* smallestNode = nullptr;
    int smallestValue = INT_MAX;

    while (current)
    {
        int currentValue = current->yigin->getir();
        if (currentValue < smallestValue)
        {
            smallestValue = currentValue;
            smallestNode = current;
        }

        current = current->next;
    }

    return smallestNode;
}

Node* BagliListe::findLargestNode()
{
    if (!head)
        return nullptr;

    Node* current = head;
    Node* largestNode = nullptr;
    int largestValue = INT_MIN;

    while (current)
    {
        int currentValue = current->yigin->getir();
        if (currentValue > largestValue)
        {
            largestValue = currentValue;
            largestNode = current;
        }

        current = current->next;
    }

    return largestNode;
}

bool BagliListe::bosYiginVarMi()
{
    Node* current = head;

    while (current)
    {
        if (!current->yigin->BosMu())
        {
            return true;
        }

        current = current->next;
    }

    return false;
}

Node* BagliListe::bosYiginiBul()
{
    Node* current = head;

    while (current)
    {
        if (!current->yigin->BosMu())
        {
            return current;
        }

        current = current->next;
    }

    return nullptr;
}

BagliListe::~BagliListe()
{
    clear();
}
