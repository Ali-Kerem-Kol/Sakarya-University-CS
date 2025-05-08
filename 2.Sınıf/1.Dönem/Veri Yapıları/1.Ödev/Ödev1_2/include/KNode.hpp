#include "GLinkedList.hpp"
#include <iostream>
using namespace std;

struct KNode
{
	GLinkedList* sayi; 
	KNode* next;

	KNode(GLinkedList *sayi, KNode* next = nullptr);
	~KNode();
};

