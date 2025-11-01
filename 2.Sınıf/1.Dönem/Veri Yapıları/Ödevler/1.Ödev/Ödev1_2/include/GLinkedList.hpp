#include "GNode.hpp"

#include <iostream>
using namespace std;


class GLinkedList
{
private:
	GNode* head;
	int size;

	GNode* FindPrevByPosition(int position);


public:
	GLinkedList();
	
	// Kopya kurucu
    GLinkedList(const GLinkedList& other);

	GNode* GetHead() const;

	void setHead(GNode* veri);

	bool isEmpty()const;

	int count()const;

	const string& first();

	const string& last();

	void add(const string& item);

	void insert(int index, const string& item);

	void remove(const string& item);

	void removeAt(int index);

	int indexOf(const string& item);

	bool find(const string& item);

	GNode* GetNodeAt(int index) const;

	void mutate(int index);

	friend ostream& operator<<(ostream& screen, GLinkedList& right);

	void clear();

	~GLinkedList();

	void print() const;

	void prints();

};

