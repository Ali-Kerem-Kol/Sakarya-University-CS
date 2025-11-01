calistir:Kur
	./bin/program.exe
	
derle:
	g++  -o ./lib/Dugum.o -I ./include -c ./src\Dugum.cpp
	g++  -o ./lib/Yigin.o -I ./include -c ./src/Yigin.cpp
	g++  -o ./lib\AVLAgaci.o -I ./include -c ./src\AVLAgaci.cpp
	g++ -o ./lib/Node.o -I ./include -c ./src/Node.cpp
	g++  -o ./lib\BagliListe.o -I ./include -c ./src\BagliListe.cpp
	g++ -o ./lib/main.o -I ./include -c ./src/main.cpp

Kur: derle
	g++  ./lib/Dugum.o ./lib/Yigin.o ./lib/AVLAgaci.o ./lib/Node.o .\lib/BagliListe.o ./lib/main.o -o ./bin/program.exe

