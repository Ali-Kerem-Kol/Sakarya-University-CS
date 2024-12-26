#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#include <fcntl.h>
#include "shell.h"

#define MAX_INPUT_SIZE 1024
#define MAX_ARG_SIZE 100

int main() {
    char input[MAX_INPUT_SIZE];

    while (1) {
        // Prompt
        printf("> ");
        fflush(stdout);

        // Kullanıcıdan giriş al
        if (!fgets(input, MAX_INPUT_SIZE, stdin)) {
            perror("Hata: Girdi alınamadı");
            continue;
        }

        // Satır sonunu kaldır
        input[strcspn(input, "\n")] = '\0';

        // Çıkış komutu
        if (strcmp(input, "quit") == 0) {
            printf("Shell sonlandırılıyor...\n");
            break;
        }

        // Komut icrası
        executeCommand(input);
    }

    return 0;
}
