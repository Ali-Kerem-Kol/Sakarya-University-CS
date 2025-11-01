#include "Sayi.hpp"

	Basamak* Sayi::FindPrevByPosition(int position)
	{
		if (position < 0 || position > size) throw "Index out of range";
		int index = 1;
		for (Basamak* itr = head;itr != NULL;itr = itr->next, index++)
		{
			if (position == index) return itr;
		}
		return NULL;
	}

	Basamak* Sayi::GetHead() const
	{
		return head;
	}

	void Sayi::setHead(Basamak* veri)
	{
		head = veri;
	}

	Sayi::Sayi()
	{
		head = NULL;
		size = 0;
	}

	bool Sayi::isEmpty()const
	{
		return size == 0;
	}

	int Sayi::count()const
	{
		return size;
	}

	const int& Sayi::first()
	{
    if (isEmpty()) throw "List is empty";
    return head->rakam;
	}

	const int& Sayi::last()
{
    if (isEmpty()) throw "List is empty";
    return FindPrevByPosition(size)->rakam;
}

	void Sayi::add(const int& item)
	{
		insert(size, item);
	}

	void Sayi::insert(int index, const int& item)
	{
		if (index == 0) head = new Basamak(item, head);
		else
		{
			Basamak* prev = FindPrevByPosition(index);
			prev->next = new Basamak(item, prev->next);
		}
		size++;
	}

	void Sayi::remove(const int& item)
	{
		int index = indexOf(item);
		removeAt(index);
	}

	void Sayi::removeAt(int index)
	{
		if (size == 0) throw "Empty list";
		Basamak* del;
		if (index == 0)
		{
			del = head;
			head = head->next;
		}
		else
		{
			Basamak* prev = FindPrevByPosition(index);
			del = prev->next;
			prev->next = prev->next->next;
		}
		delete del;
		size--;
	}

	int Sayi::indexOf(const int& item)
	{
		int index = 0;
		for (Basamak* itr = head;itr != NULL;itr = itr->next, index++)
		{
			if (itr->rakam == item) return index;
		}
		throw "Index out of range";
	}

	bool Sayi::find(const int& item)
	{
		for (Basamak* itr = head;itr != NULL;itr = itr->next)
		{
			if (itr->rakam == item) return true;
		}
		return false;
	}

	ostream& operator<<(ostream& screen, const Sayi& right)
{
    if (right.isEmpty()) 
    {
        screen << "Empty list" << endl;
    }
    else
    {
        Basamak* itr = right.GetHead();
        while (itr != NULL)
        {
            screen << itr->rakam << " ";
            itr = itr->next;
        }
    }
    return screen;
}
	void Sayi::clear()
	{
		while (!isEmpty())
		{
			removeAt(0);
		}
	}

	Sayi::~Sayi()
	{
		clear();
		for (int i = 0; i < size;i++)
		{
			cout << i << ". basamak silindi" << endl;
		}
	}