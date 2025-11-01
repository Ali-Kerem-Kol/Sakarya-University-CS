#include "GLinkedList.hpp"

GNode *GLinkedList::FindPrevByPosition(int position)
{
	if (position < 0 || position > size)
		throw "Index out of range";
	int index = 1;
	for (GNode *itr = head; itr != NULL; itr = itr->next, index++)
	{
		if (position == index)
			return itr;
	}
	return NULL;
}

GNode *GLinkedList::GetHead() const
{
	return head;
}

void GLinkedList::setHead(GNode *veri)
{
	head = veri;
}

GLinkedList::GLinkedList()
{
	head = NULL;
	size = 0;
}

// Parametreli kopyalama kurucu
GLinkedList::GLinkedList(const GLinkedList &other)
{
	head = nullptr;
	size = 0;

	// Diğer listenin tüm elemanlarını kopyalar
	for (GNode *itr = other.head; itr != nullptr; itr = itr->next)
	{
		add(itr->data);
	}
}

bool GLinkedList::isEmpty() const
{
	return size == 0;
}

int GLinkedList::count() const
{
	return size;
}

const string &GLinkedList::first()
{
	if (isEmpty())
		throw "List is empty";
	return head->data;
}

const string &GLinkedList::last()
{
	if (isEmpty())
		throw "List is empty";
	return FindPrevByPosition(size)->data;
}

void GLinkedList::add(const string &item)
{
	insert(size, item);
}

void GLinkedList::insert(int index, const string &item)
{
	if (index == 0)
		head = new GNode(item, head);
	else
	{
		GNode *prev = FindPrevByPosition(index);
		prev->next = new GNode(item, prev->next);
	}
	size++;
}

void GLinkedList::remove(const string &item)
{
	int index = indexOf(item);
	removeAt(index);
}

void GLinkedList::removeAt(int index)
{
	if (size == 0)
		throw "Empty list";
	GNode *del;
	if (index == 0)
	{
		del = head;
		head = head->next;
	}
	else
	{
		GNode *prev = FindPrevByPosition(index);
		del = prev->next;
		prev->next = prev->next->next;
	}
	delete del;
	size--;
}

int GLinkedList::indexOf(const string &item)
{
	int index = 0;
	for (GNode *itr = head; itr != NULL; itr = itr->next, index++)
	{
		if (itr->data == item)
			return index;
	}
	throw "Index out of range";
}

bool GLinkedList::find(const string &item)
{
	for (GNode *itr = head; itr != NULL; itr = itr->next)
	{
		if (itr->data == item)
			return true;
	}
	return false;
}

GNode *GLinkedList::GetNodeAt(int index) const
{
	if (index < 0 || index >= size)
	{
		throw "Index out of range";
	}

	GNode *current = head;
	for (int i = 0; i < index; i++)
	{
		current = current->next;
	}
	return current;
}

void GLinkedList::mutate(int index)
{
	if (index < 0 || index >= size)
	{
		throw "Index out of range";
	}
	GNode *target = GetNodeAt(index);
	if (target != nullptr)
	{
		target->data = "X";
	}
}
ostream &operator<<(ostream &screen, const GLinkedList &right)
{
	if (right.isEmpty())
	{
		screen << "Empty list" << endl;
	}
	else
	{
		GNode *itr = right.GetHead();
		while (itr != NULL)
		{
			screen << itr->data << " ";
			itr = itr->next;
		}
	}
	return screen;
}
void GLinkedList::clear()
{
	while (!isEmpty())
	{
		removeAt(0);
	}
}

GLinkedList::~GLinkedList()
{
	clear();
}

void GLinkedList::print() const
{
	GNode *current = head;
	while (current != nullptr)
	{
		cout << current->data << " ";
		current = current->next;
	}
	cout << endl;
}

void GLinkedList::prints()
{
    if (isEmpty()) {
        cout << "Empty list" << endl;
        return;
    }

    // İlk gen (başlangıç)
    GNode *firstNode = head;
    char firstGen = firstNode->data[0];  // İlk genin ilk harfini alıyoruz

    // Şimdi, sondan başa doğru ilerleyeceğiz
    // Döngü linked list'in boyutu kadar çalışacak
    for (int i = size - 1; i >= 0; i--) {
        // Listenin son elemanına gitmek için FindPrevByPosition kullanıyoruz
        GNode *current = GetNodeAt(i);
        
        // Eğer şu anki gen, ilk genden küçükse, bu geni yazdır
        if (current->data[0] < firstGen) {
            cout << current->data[0] << " ";  // Bu gen yazdırılır
            return;  // Ve işlem sonlandırılır
        }
    }

    // Eğer küçük bir gen bulunmazsa, ilk gen yazdırılır
    cout << firstGen << " ";
}



