
#include "Sayi.hpp"
#include <iostream>
using namespace std;


struct Dugum
{
	Sayi* sayi;
	Dugum* next;

	Dugum(Sayi* sayi, Dugum* next = nullptr);
};

