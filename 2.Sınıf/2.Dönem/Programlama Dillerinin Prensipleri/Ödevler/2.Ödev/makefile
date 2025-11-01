calistir: bagKurMake
	./bin/program.exe
	
derleMake:
	gcc  -o ./lib\Canli.o -I ./include -c ./src\Canli.c
	gcc -o ./lib/Bitki.o -I ./include -c ./src/Bitki.c
	gcc  -o ./lib/Bocek.o -I ./include -c ./src/Bocek.c
	gcc  -o ./lib/Pire.o -I ./include -c ./src\Pire.c
	gcc  -o ./lib/Sinek.o -I ./include -c ./src\Sinek.c
	gcc  -o ./lib/Habitat.o -I ./include -c ./src\Habitat.c
	gcc  -o ./lib\Main.o -I ./include -c ./src\Main.c


bagKurMake: derleMake
	gcc  ./lib/Canli.o ./lib/Bitki.o ./lib/Bocek.o ./lib/Sinek.o .\lib/Pire.o .\lib/Habitat.o .\lib/Main.o -o ./bin/program.exe
