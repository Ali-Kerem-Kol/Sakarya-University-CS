#include "KNode.hpp"

KNode::KNode(GLinkedList *sayi, KNode* next /*= nullptr*/)
{
    this->sayi = sayi;
    this->next = next;
}

KNode::~KNode(){
    delete sayi;
}