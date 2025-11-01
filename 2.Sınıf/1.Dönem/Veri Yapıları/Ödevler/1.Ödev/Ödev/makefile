calistir:Kur
	./bin/program.exe
	
derle:
	g++  -o ./lib/SayilarListesi.o -I ./include -c ./src\SayilarListesi.cpp
	g++  -o ./lib/Dugum.o -I ./include -c ./src/Dugum.cpp
	g++  -o ./lib\Sayi.o -I ./include -c ./src\Sayi.cpp
	g++ -o ./lib/Basamak.o -I ./include -c ./src/Basamak.cpp
	g++  -o ./lib\Main.o -I ./include -c ./src\Main.cpp


Kur: derle
	g++  ./lib/Basamak.o ./lib/Dugum.o ./lib/Main.o ./lib/Sayi.o .\lib/SayilarListesi.o -o ./bin/program.exe

