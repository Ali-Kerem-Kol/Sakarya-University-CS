#include "GNode.hpp"

GNode::GNode(const string& data, GNode* next /*= nullptr*/)
{
    this->data = data;
    this->next = next;
}
