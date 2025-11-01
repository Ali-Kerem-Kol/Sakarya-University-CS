//ALI KEREK KOL - B221210042 
//OMER ELMAS - B221210582

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include <sys/stat.h>
#include <time.h>
#include "fs.h"

static int load_metadata(Metadata *metadata) {
    int fd = open(DISK_NAME, O_RDWR, 0666);
    if (fd < 0) return -1;
    if (read(fd, metadata, sizeof(Metadata)) < 0) {
        close(fd);
        return -1;
    }
    close(fd);
    return 0;
}

static int save_metadata(Metadata *metadata) {
    int fd = open(DISK_NAME, O_RDWR, 0666);
    if (fd < 0) return -1;
    if (write(fd, metadata, sizeof(Metadata)) < 0) {
        close(fd);
        return -1;
    }
    close(fd);
    return 0;
}

static int find_file(const char *filename, Metadata *metadata) {
    for (int i = 0; i < MAX_FILES; i++) {
        if (metadata->files[i].is_active && strcmp(metadata->files[i].filename, filename) == 0) {
            return i;
        }
    }
    return -1;
}

static int allocate_space(Metadata *metadata, int size) {
    int occupied[DISK_SIZE - METADATA_SIZE] = {0};

    for (int i = 0; i < MAX_FILES; i++) {
        if (metadata->files[i].is_active) {
            int start = metadata->files[i].start_block;
            int end = start + metadata->files[i].size;
            for (int j = start; j < end; j++) {
                occupied[j] = 1;
            }
        }
    }

    for (int i = 0; i < (DISK_SIZE - METADATA_SIZE); i++) {
        int found = 1;
        for (int j = 0; j < size; j++) {
            if (i + j >= (DISK_SIZE - METADATA_SIZE) || occupied[i + j]) {
                found = 0;
                break;
            }
        }
        if (found) {
            return i;
        }
    }
    return -1;
}

void fs_log(const char *operation) {
    int fd = open(LOG_FILE, O_WRONLY | O_CREAT | O_APPEND, 0666);
    if (fd < 0) return;
    write(fd, operation, strlen(operation));
    write(fd, "\n", 1);
    close(fd);
}

int fs_format() {
    int fd = open(DISK_NAME, O_CREAT | O_RDWR | O_TRUNC, 0666);
    if (fd < 0) {
        perror("Failed to create disk");
        return -1;
    }
    ftruncate(fd, DISK_SIZE);

    Metadata metadata = {0};
    write(fd, &metadata, sizeof(Metadata));
    close(fd);

    fs_log("Format disk");
    return 0;
}

int fs_create(const char *filename) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    if (metadata.total_files >= MAX_FILES) {
        printf("Maximum file limit reached.\n");
        return -1;
    }

    if (find_file(filename, &metadata) >= 0) {
        printf("File already exists.\n");
        return -1;
    }

    int index = -1;
    for (int i = 0; i < MAX_FILES; i++) {
        if (!metadata.files[i].is_active) {
            index = i;
            break;
        }
    }
    if (index == -1) {
        printf("No space for new file.\n");
        return -1;
    }

    strcpy(metadata.files[index].filename, filename);
    metadata.files[index].size = 0;
    metadata.files[index].start_block = allocate_space(&metadata, 1);
    metadata.files[index].created_time = time(NULL);
    metadata.files[index].is_active = 1;
    metadata.total_files++;

    save_metadata(&metadata);

    fs_log("Create file");
    return 0;
}

int fs_delete(const char *filename) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(filename, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return -1;
    }

    metadata.files[idx].is_active = 0;
    metadata.total_files--;

    save_metadata(&metadata);

    fs_log("Delete file");
    return 0;
}

int fs_write(const char *filename, const char *data, int size) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(filename, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return -1;
    }

    int fd = open(DISK_NAME, O_RDWR);
    if (fd < 0) return -1;

    int start = sizeof(Metadata) + metadata.files[idx].start_block;
    lseek(fd, start, SEEK_SET);
    write(fd, data, size);

    metadata.files[idx].size = size;
    save_metadata(&metadata);
    close(fd);

    fs_log("Write to file");
    return 0;
}

int fs_read(const char *filename, int offset, int size, char *buffer) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(filename, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return -1;
    }

    if (offset + size > metadata.files[idx].size) {
        printf("Read exceeds file size.\n");
        return -1;
    }

    int fd = open(DISK_NAME, O_RDONLY);
    if (fd < 0) return -1;

    int start = sizeof(Metadata) + metadata.files[idx].start_block + offset;
    lseek(fd, start, SEEK_SET);
    read(fd, buffer, size);
    close(fd);

    fs_log("Read from file");
    return 0;
}

void fs_ls() {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) {
        printf("Could not load metadata.\n");
        return;
    }

    printf("Filename\tSize (bytes)\n");
    printf("-----------------------------\n");
    for (int i = 0; i < MAX_FILES; i++) {
        if (metadata.files[i].is_active) {
            printf("%s\t\t%d\n", metadata.files[i].filename, metadata.files[i].size);
        }
    }

    fs_log("List files");
}

int fs_rename(const char *old_name, const char *new_name) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(old_name, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return -1;
    }

    strcpy(metadata.files[idx].filename, new_name);
    save_metadata(&metadata);

    fs_log("Rename file");
    return 0;
}

int fs_exists(const char *filename) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return 0;

    return (find_file(filename, &metadata) >= 0);
}

int fs_size(const char *filename) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(filename, &metadata);
    if (idx < 0) return -1;

    return metadata.files[idx].size;
}

int fs_append(const char *filename, const char *data, int size) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(filename, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return -1;
    }

    int fd = open(DISK_NAME, O_RDWR);
    if (fd < 0) return -1;

    int start = METADATA_SIZE + metadata.files[idx].start_block + metadata.files[idx].size;
    lseek(fd, start, SEEK_SET);
    write(fd, data, size);

    metadata.files[idx].size += size;
    save_metadata(&metadata);
    close(fd);

    fs_log("Append to file");
    return 0;
}

int fs_truncate(const char *filename, int new_size) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(filename, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return -1;
    }

    if (new_size > metadata.files[idx].size) {
        printf("New size is larger than current size.\n");
        return -1;
    }

    metadata.files[idx].size = new_size;
    save_metadata(&metadata);

    fs_log("Truncate file");
    return 0;
}

int fs_copy(const char *src_filename, const char *dest_filename) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int idx = find_file(src_filename, &metadata);
    if (idx < 0) {
        printf("Source file not found.\n");
        return -1;
    }

    char *buffer = malloc(metadata.files[idx].size);
    if (!buffer) return -1;

    fs_read(src_filename, 0, metadata.files[idx].size, buffer);
    fs_create(dest_filename);
    fs_write(dest_filename, buffer, metadata.files[idx].size);
    free(buffer);

    fs_log("Copy file");
    return 0;
}

int fs_mv(const char *old_name, const char *new_name) {
    int res = fs_rename(old_name, new_name);
    fs_log("Move file");
    return res;
}

int fs_defragment() {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    int fd = open(DISK_NAME, O_RDWR);
    if (fd < 0) return -1;

    int pos = 0;
    for (int i = 0; i < MAX_FILES; i++) {
        if (metadata.files[i].is_active) {
            char *buffer = malloc(metadata.files[i].size);
            lseek(fd, METADATA_SIZE + metadata.files[i].start_block, SEEK_SET);
            read(fd, buffer, metadata.files[i].size);

            metadata.files[i].start_block = pos;
            lseek(fd, METADATA_SIZE + pos, SEEK_SET);
            write(fd, buffer, metadata.files[i].size);

            pos += metadata.files[i].size;
            free(buffer);
        }
    }
    save_metadata(&metadata);
    close(fd);

    fs_log("Defragment disk");
    return 0;
}

int fs_check_integrity() {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return -1;

    for (int i = 0; i < MAX_FILES; i++) {
        if (metadata.files[i].is_active && metadata.files[i].start_block + metadata.files[i].size > DISK_SIZE - METADATA_SIZE) {
            printf("File %s corrupted.\n", metadata.files[i].filename);
        }
    }

    fs_log("Check integrity");
    return 0;
}

int fs_backup(const char *backup_filename) {
    int src = open(DISK_NAME, O_RDONLY);
    int dst = open(backup_filename, O_WRONLY | O_CREAT | O_TRUNC, 0666);

    if (src < 0 || dst < 0) return -1;

    char buffer[4096];
    ssize_t n;
    while ((n = read(src, buffer, sizeof(buffer))) > 0) {
        write(dst, buffer, n);
    }

    close(src);
    close(dst);

    fs_log("Backup disk");
    return 0;
}

int fs_restore(const char *backup_filename) {
    int src = open(backup_filename, O_RDONLY);
    int dst = open(DISK_NAME, O_WRONLY | O_CREAT | O_TRUNC, 0666);

    if (src < 0 || dst < 0) return -1;

    char buffer[4096];
    ssize_t n;
    while ((n = read(src, buffer, sizeof(buffer))) > 0) {
        write(dst, buffer, n);
    }

    close(src);
    close(dst);

    fs_log("Restore disk");
    return 0;
}

void fs_cat(const char *filename) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return;

    int idx = find_file(filename, &metadata);
    if (idx < 0) {
        printf("File not found.\n");
        return;
    }

    char *buffer = malloc(metadata.files[idx].size);
    fs_read(filename, 0, metadata.files[idx].size, buffer);
    write(STDOUT_FILENO, buffer, metadata.files[idx].size);
    printf("\n");
    free(buffer);

    fs_log("Cat file");
}

int fs_diff(const char *file1, const char *file2) {
    Metadata metadata;
    if (load_metadata(&metadata) < 0) return 1;

    int idx1 = find_file(file1, &metadata);
    int idx2 = find_file(file2, &metadata);

    if (idx1 < 0 || idx2 < 0) return 1;

    if (metadata.files[idx1].size != metadata.files[idx2].size) return 1;

    char *buf1 = malloc(metadata.files[idx1].size);
    char *buf2 = malloc(metadata.files[idx2].size);

    fs_read(file1, 0, metadata.files[idx1].size, buf1);
    fs_read(file2, 0, metadata.files[idx2].size, buf2);

    int diff = memcmp(buf1, buf2, metadata.files[idx1].size);

    free(buf1);
    free(buf2);

    fs_log("Diff files");
    return diff;
}

