#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/wait.h>
#include <fcntl.h>
#include "../include/shell.h"

void executeCommand(char *cmd) {
    char *args[MAX_ARG_SIZE];
    int background = 0;
    int pipeIndex = -1;

    // Arka plan işlemi kontrolü
    if (cmd[strlen(cmd) - 1] == '&') {
        background = 1;
        cmd[strlen(cmd) - 1] = '\0'; // '&' işaretini kaldır
    }

    // Boru kontrolü
    char *pipePos = strchr(cmd, '|');
    if (pipePos) {
        pipeIndex = pipePos - cmd;
        cmd[pipeIndex] = '\0'; // Boruyu ayır
    }

    // Giriş ve çıkış yönlendirme kontrolü
    int inRedirect = -1, outRedirect = -1;
    char *inputFile = NULL, *outputFile = NULL;

    char *inPos = strchr(cmd, '<');
    if (inPos) {
        *inPos = '\0'; // '<' işaretini ayır
        inputFile = strtok(inPos + 1, " ");
        inRedirect = open(inputFile, O_RDONLY);
        if (inRedirect == -1) {
            perror("Giriş dosyası açılamadı");
            return;
        }
    }

    char *outPos = strchr(cmd, '>');
    if (outPos) {
        *outPos = '\0'; // '>' işaretini ayır
        outputFile = strtok(outPos + 1, " ");
        outRedirect = open(outputFile, O_WRONLY | O_CREAT | O_TRUNC, 0644);
        if (outRedirect == -1) {
            perror("Çıkış dosyası açılamadı");
            return;
        }
    }

    // Argümanları ayrıştır
    char *token = strtok(cmd, " ");
    int i = 0;
    while (token != NULL && i < MAX_ARG_SIZE - 1) {
        args[i++] = token;
        token = strtok(NULL, " ");
    }
    args[i] = NULL;

    // Alt işlem oluştur
    pid_t pid = fork();
    if (pid == 0) { // Çocuk işlem
        // Giriş yönlendirme
        if (inRedirect != -1) {
            dup2(inRedirect, STDIN_FILENO);
            close(inRedirect);
        }

        // Çıkış yönlendirme
        if (outRedirect != -1) {
            dup2(outRedirect, STDOUT_FILENO);
            close(outRedirect);
        }

        // Boru işlemi
        if (pipeIndex != -1) {
            int pipefd[2];
            pipe(pipefd);

            if (fork() == 0) {
                close(pipefd[0]); // Okuma ucunu kapat
                dup2(pipefd[1], STDOUT_FILENO);
                close(pipefd[1]);
                execlp(args[0], args[0], (char *)NULL);
                perror("Komut çalıştırılamadı");
                exit(EXIT_FAILURE);
            } else {
                close(pipefd[1]); // Yazma ucunu kapat
                dup2(pipefd[0], STDIN_FILENO);
                close(pipefd[0]);
                wait(NULL);
            }
        }

        // Komut çalıştır
        if (execvp(args[0], args) == -1) {
            perror("Komut çalıştırılamadı");
            exit(EXIT_FAILURE);
        }
    } else if (pid > 0) { // Ebeveyn işlem
        if (!background) {
            wait(NULL); // Ön planda çalıştır
        } else {
            printf("[Arka plan] PID: %d\n", pid);
        }
    } else {
        perror("Fork hatası");
    }
}
