# Ali Kerem Kol - B221210042
# Muhammed Baha Bakan - B221210050
# Engin Cavak - B221210056
# Berat Alpsar - B221210044
# Egemen Bozca - B221210037
CC = gcc
CFLAGS = -Wall -g
SRC_DIR = src
OBJ_DIR = build
BIN_DIR = bin
INCLUDE_DIR = include

# Dosya kaynakları
SRC = $(SRC_DIR)/shell.c $(SRC_DIR)/shell_utils.c
OBJ = $(OBJ_DIR)/shell.o $(OBJ_DIR)/shell_utils.o
EXEC = $(BIN_DIR)/shell

# Derleme komutları
all: $(EXEC)
	./$(EXEC)  # Derleme tamamlandığında shell dosyasını çalıştır

$(EXEC): $(OBJ)
	$(CC) $(OBJ) -o $(EXEC)

$(OBJ_DIR)/%.o: $(SRC_DIR)/%.c
	$(CC) $(CFLAGS) -I$(INCLUDE_DIR) -c $< -o $@

# Temizlik komutu
clean:
	rm -f $(OBJ) $(EXEC)

.PHONY: all clean
