
#include "Basamak.hpp"

#include <iostream>
using namespace std;


class Sayi
{
private:
	Basamak* head;
	int size;

	Basamak* FindPrevByPosition(int position);


public:
	Basamak* GetHead() const;

	void setHead(Basamak* veri);

	Sayi();

	bool isEmpty()const;

	int count()const;

	const int& first();

	const int& last();

	void add(const int& item);

	void insert(int index, const int& item);

	void remove(const int& item);

	void removeAt(int index);

	int indexOf(const int& item);

	bool find(const int& item);

	friend ostream& operator<<(ostream& screen, Sayi& right);

	void clear();

	~Sayi();

};

