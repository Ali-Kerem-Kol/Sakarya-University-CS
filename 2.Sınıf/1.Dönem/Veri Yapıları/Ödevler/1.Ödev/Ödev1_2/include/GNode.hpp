#include <iostream>
using namespace std;

struct GNode
{
	string data;
	GNode* next;

	GNode(const string& rakam, GNode* next = nullptr);
};
