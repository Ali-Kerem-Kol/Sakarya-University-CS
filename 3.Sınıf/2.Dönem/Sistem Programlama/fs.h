//ALI KEREK KOL - B221210042 
//OMER ELMAS - B221210582
#ifndef FS_H
#define FS_H

#include <time.h>

#define DISK_NAME "disk.sim"
#define DISK_SIZE (1024 * 1024) // 1MB
#define METADATA_SIZE sizeof(Metadata)
#define MAX_FILES 128
#define FILENAME_MAX_LENGTH 32
#define BLOCK_SIZE 512
#define LOG_FILE "fs_log.txt"

typedef struct {
    char filename[FILENAME_MAX_LENGTH];
    int size;
    int start_block;
    time_t created_time;
    int is_active; // 1: active, 0: deleted
} FileEntry;

typedef struct {
    int total_files;
    FileEntry files[MAX_FILES];
} Metadata;

int fs_format();
int fs_create(const char *filename);
int fs_delete(const char *filename);
int fs_write(const char *filename, const char *data, int size);
int fs_read(const char *filename, int offset, int size, char *buffer);
void fs_ls();
int fs_rename(const char *old_name, const char *new_name);
int fs_exists(const char *filename);
int fs_size(const char *filename);
int fs_append(const char *filename, const char *data, int size);
int fs_truncate(const char *filename, int new_size);
int fs_copy(const char *src_filename, const char *dest_filename);
int fs_mv(const char *old_name, const char *new_name);
int fs_defragment();
int fs_check_integrity();
int fs_backup(const char *backup_filename);
int fs_restore(const char *backup_filename);
void fs_cat(const char *filename);
int fs_diff(const char *file1, const char *file2);
void fs_log(const char *operation);

#endif

