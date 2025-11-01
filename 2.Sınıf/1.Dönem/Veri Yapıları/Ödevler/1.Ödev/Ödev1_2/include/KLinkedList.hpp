#include "KNode.hpp"

#include <iostream>
using namespace std;


class KLinkedList
{
private:
	KNode* head;
	int size;

	KNode* FindPrevByPosition(int position);


public:
	
	KLinkedList();

	KNode* GetHead() const;

	void setHead(KNode* veri);

	bool isEmpty()const;
	
	int count()const;

	const GLinkedList* first();

	const GLinkedList* last();

	void add(const GLinkedList& item);

	void insert(int index, const GLinkedList& item);

	void remove(const GLinkedList& item);

	void removeAt(int index);

	int indexOf(const GLinkedList* item);

	bool find(const GLinkedList* item);

	KNode* GetNodeAt(int index);

	void mutate(int kindex,int gindex);

	void crossover(const GLinkedList& list1, const GLinkedList& list2);

	friend ostream& operator<<(ostream& screen, KLinkedList& right);

	void clear();

	~KLinkedList();

	void print() const;

	void prints() const;

};

