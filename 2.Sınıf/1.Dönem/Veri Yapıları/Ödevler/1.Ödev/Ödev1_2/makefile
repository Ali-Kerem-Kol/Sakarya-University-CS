calistir:Kur
	./bin/program.exe
	
derle:
	g++  -o ./lib/KLinkedList.o -I ./include -c ./src\KLinkedList.cpp
	g++  -o ./lib/KNode.o -I ./include -c ./src/KNode.cpp
	g++  -o ./lib\GLinkedList.o -I ./include -c ./src\GLinkedList.cpp
	g++ -o ./lib/GNode.o -I ./include -c ./src/GNode.cpp
	g++  -o ./lib\main.o -I ./include -c ./src\main.cpp


Kur: derle
	g++  ./lib/KLinkedList.o ./lib/KNode.o ./lib/GLinkedList.o ./lib/GNode.o .\lib/main.o -o ./bin/program.exe

