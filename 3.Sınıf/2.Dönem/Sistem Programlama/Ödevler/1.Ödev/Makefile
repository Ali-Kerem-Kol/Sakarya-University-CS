CC=gcc
CFLAGS=-Wall
TARGET=SimpleFS

all: $(TARGET)

$(TARGET): main.o fs.o
	$(CC) $(CFLAGS) -o $(TARGET) main.o fs.o

main.o: main.c fs.h
	$(CC) $(CFLAGS) -c main.c

fs.o: fs.c fs.h
	$(CC) $(CFLAGS) -c fs.c

clean:
	rm -f *.o $(TARGET)

