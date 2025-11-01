#include "SayilarListesi.hpp"

	Dugum* SayilarListesi::FindPrevByPosition(int position)
	{
		if (position < 0 || position > size) throw "Index out of range";
		int index = 1;
		for (Dugum* itr = head;itr != NULL;itr = itr->next, index++)
		{
			if (position == index) return itr;
		}
		return NULL;
	}

	Dugum* SayilarListesi::GetHead() const
	{
		return head;
	}

	void SayilarListesi::setHead(Dugum* veri)
	{
		head = veri;
	}

	SayilarListesi::SayilarListesi()
	{
		head = NULL;
		size = 0;
	}

	bool SayilarListesi::isEmpty()const
	{
		return size == 0;
	}

	int SayilarListesi::count()const
	{
		return size;
	}

	const Sayi* SayilarListesi::first()
{
    if (isEmpty()) throw "List is empty";
    return head->sayi;
}
	const Sayi* SayilarListesi::last()
{
    if (isEmpty()) throw "List is empty";
    return FindPrevByPosition(size)->sayi;
}

	void SayilarListesi::add(const Sayi& item)
	{
		insert(size, item);
	}

	void SayilarListesi::insert(int index, const Sayi& item)
	{
		if (index == 0) head = new Dugum(new Sayi(item), head);
		else
		{
			Dugum* prev = FindPrevByPosition(index);
			prev->next = new Dugum(new Sayi(item), prev->next);
		}
		size++;
	}

	void SayilarListesi::remove(const Sayi& item)
	{
		int index = indexOf(&item);
		removeAt(index);
	}

	void SayilarListesi::removeAt(int index)
	{
		if (size == 0) throw "Empty list";
		Dugum* del;
		if (index == 0)
		{
			del = head;
			head = head->next;
		}
		else
		{
			Dugum* prev = FindPrevByPosition(index);
			del = prev->next;
			prev->next = prev->next->next;
		}
		delete del;
		size--;
	}

	int SayilarListesi::indexOf(const Sayi* item)
	{
		int index = 0;
		for (Dugum* itr = head;itr != NULL;itr = itr->next, index++)
		{
			if (itr->sayi == item) return index;
		}
		throw "Index out of range";
	}

	bool SayilarListesi::find(const Sayi* item)
	{
		for (Dugum* itr = head;itr != NULL;itr = itr->next)
		{
			if (itr->sayi == item)  return true;
		}
		return false;
	}

	ostream& operator<<(ostream& screen, SayilarListesi& right)
{
    if (right.isEmpty()) screen << "Empty list" << endl;
    else
    {
        for (Dugum* itr = right.head; itr != NULL; itr = itr->next)
        {
            screen << itr->sayi << " ";
        }
    }
    return screen;
}
	void SayilarListesi::clear()
	{
		while (!isEmpty())
		{
			const Sayi* temp = first();

			Basamak* currentBasamak = temp->GetHead();
			while (currentBasamak != nullptr)
			{
				Basamak* nextBasamak = currentBasamak->next;
				delete currentBasamak;
				currentBasamak = nextBasamak;
			}


			remove(*temp);

		}
	}

	SayilarListesi::~SayilarListesi()
	{
		clear();
		for (int i = 0; i<size;i++)
		{
			cout << i << ". sayi silindi" << endl;
		}
	}