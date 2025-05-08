#include <iostream>
using namespace std;

struct Basamak
{
	int rakam;
	Basamak* next;

	Basamak(const int& rakam, Basamak* next = nullptr);
};
