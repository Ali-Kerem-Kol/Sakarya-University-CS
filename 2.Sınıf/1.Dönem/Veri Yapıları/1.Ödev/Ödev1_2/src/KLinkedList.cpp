#include "KLinkedList.hpp"

KNode *KLinkedList::FindPrevByPosition(int position)
{
	if (position < 0 || position > size)
		throw "Index out of range";
	int index = 1;
	for (KNode *itr = head; itr != NULL; itr = itr->next, index++)
	{
		if (position == index)
			return itr;
	}
	return NULL;
}

KNode *KLinkedList::GetHead() const
{
	return head;
}

void KLinkedList::setHead(KNode *veri)
{
	head = veri;
}

KLinkedList::KLinkedList()
{
	head = NULL;
	size = 0;
}

bool KLinkedList::isEmpty() const
{
	return size == 0;
}

int KLinkedList::count() const
{
	return size;
}

const GLinkedList *KLinkedList::first()
{
	if (isEmpty())
		throw "List is empty";
	return head->sayi;
}
const GLinkedList *KLinkedList::last()
{
	if (isEmpty())
		throw "List is empty";
	return FindPrevByPosition(size)->sayi;
}

void KLinkedList::add(const GLinkedList &item)
{
	insert(size, item);
}

void KLinkedList::insert(int index, const GLinkedList &item)
{
	if (index == 0)
		head = new KNode(new GLinkedList(item), head);
	else
	{
		KNode *prev = FindPrevByPosition(index);
		prev->next = new KNode(new GLinkedList(item), prev->next);
	}
	size++;
}

void KLinkedList::remove(const GLinkedList &item)
{
	int index = indexOf(&item);
	removeAt(index);
}

void KLinkedList::removeAt(int index)
{
	if (size == 0)
		throw "Empty list";
	KNode *del;
	if (index == 0)
	{
		del = head;
		head = head->next;
	}
	else
	{
		KNode *prev = FindPrevByPosition(index);
		del = prev->next;
		prev->next = prev->next->next;
	}
	delete del;
	size--;
}

int KLinkedList::indexOf(const GLinkedList *item)
{
	int index = 0;
	for (KNode *itr = head; itr != NULL; itr = itr->next, index++)
	{
		if (itr->sayi == item)
			return index;
	}
	throw "Index out of range";
}

bool KLinkedList::find(const GLinkedList *item)
{
	for (KNode *itr = head; itr != NULL; itr = itr->next)
	{
		if (itr->sayi == item)
			return true;
	}
	return false;
}

KNode* KLinkedList::GetNodeAt(int index) {
    if (index < 0 || index >= size) {
        throw "Index out of range";
    }

    KNode* current = head;
    for (int i = 0; i < index; i++) {
        current = current->next;
    }
    return current;
}

void KLinkedList::mutate(int kindex,int gindex) {
    if (kindex < 0 || kindex >= count()) {
        throw "Index out of range";
    }
    GLinkedList* listToMutate = GetNodeAt(kindex)->sayi;  // Mutasyon yapılacak listeyi al
    if (listToMutate != nullptr) {
        listToMutate->mutate(gindex);  // Gen mutasyonunu uygula
    }
}

void KLinkedList::crossover(const GLinkedList& list1, const GLinkedList& list2) {

	if (list1.count() <= 1 || list2.count() <= 1) {
		cout << "Eleman sayisi bir veya daha az bir kromozom tespit edildi, caprazlama yapilamaz !" << endl;
		return;
	}

    // İlk ve ikinci kromozomun ortalarını belirliyoruz
    int mid1 = list1.count() / 2;
    int mid2 = list2.count() / 2;

    GLinkedList newKromozom1, newKromozom2;

    // İlk kromozom için: list1'in sol yarısı + list2'nin sağ yarısı
    for (int i = 0; i < mid1; i++) {
        newKromozom1.add(list1.GetNodeAt(i)->data);
    }
    // list2'nin sağ yarısını eklerken tek-çift kontrolü
    for (int i = (list2.count() % 2 == 0 ? mid2 : mid2 + 1); i < list2.count(); i++) {
        newKromozom1.add(list2.GetNodeAt(i)->data);
    }

    // Yeni kromozom 1'i listeye ekle (referans yerine kopya nesne kullanıyoruz)
    this->add(newKromozom1);

    // list1'in sağ yarısını eklerken tek-çift kontrolü
    for (int i = (list1.count() % 2 == 0 ? mid1 : mid1 + 1); i < list1.count(); i++) {
        newKromozom2.add(list1.GetNodeAt(i)->data);
    }
	// İkinci kromozom için: list2'nin sol yarısı + list1'in sağ yarası
	for (int i = 0; i < mid2; i++) {
        newKromozom2.add(list2.GetNodeAt(i)->data);
    }

    // Yeni kromozom 2'yi listeye ekle (referans yerine kopya nesne kullanıyoruz)
    this->add(newKromozom2);

    cout << "Caprazlama islemi tamamlandi ve iki yeni kromozom populasyona eklendi." << endl;
}


void KLinkedList::clear()
{
	while (!isEmpty())
	{
		removeAt(0);
	}
}

KLinkedList::~KLinkedList()
{
	clear();
}

void KLinkedList::print() const {
    KNode* current = head;
    int index = 1;
    while (current != nullptr) {
        cout << "Kromozom " << index << ": ";
        current->sayi->print();  // GLinkedList'in print fonksiyonunu çağırır
        current = current->next;
        index++;
    }
    cout << endl;
}

void KLinkedList::prints() const {
    KNode* current = head;
    int index = 1;
    
    while (current != nullptr) {
        current->sayi->prints();  // GLinkedList'in print fonksiyonunu çağırır
        current = current->next;
        index++;
    }
    //cout << endl;
}
