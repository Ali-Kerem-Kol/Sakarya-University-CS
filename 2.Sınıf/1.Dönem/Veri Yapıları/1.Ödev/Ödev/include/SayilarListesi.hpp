
#include "Dugum.hpp"

#include <iostream>
using namespace std;


class SayilarListesi
{
private:
	Dugum* head;
	int size;

	Dugum* FindPrevByPosition(int position);


public:
	
	Dugum* GetHead() const;

	void setHead(Dugum* veri);

	SayilarListesi();

	bool isEmpty()const;
	
	int count()const;

	const Sayi* first();

	const Sayi* last();

	void add(const Sayi& item);

	void insert(int index, const Sayi& item);

	void remove(const Sayi& item);

	void removeAt(int index);

	int indexOf(const Sayi* item);

	bool find(const Sayi* item);

	friend ostream& operator<<(ostream& screen, SayilarListesi& right);

	void clear();

	~SayilarListesi();

};

