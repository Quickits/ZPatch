#include "FileBuffer.h"
#include <fcntl.h>
#include <unistd.h>
#include <sys/mman.h>
#include <sys/stat.h>
#include <cstring>
#include <cstdlib>

namespace zpatch {

FileBuffer::FileBuffer() : data(nullptr), size(0), isMapped(false) {}

FileBuffer::~FileBuffer() { release(); }

bool FileBuffer::readFromFile(const char *path) {
    release();
    int fd = open(path, O_RDONLY);
    if (fd < 0) return false;

    struct stat st {};
    if (fstat(fd, &st) != 0) { close(fd); return false; }

    size = static_cast<size_t>(st.st_size);
    const size_t MMAP_THRESHOLD = 16 * 1024 * 1024;

    if (size >= MMAP_THRESHOLD) {
        data = mmap(nullptr, size, PROT_READ, MAP_PRIVATE, fd, 0);
        if (data == MAP_FAILED) { data = nullptr; close(fd); size = 0; return false; }
        isMapped = true;
    } else {
        data = malloc(size);
        if (!data) { close(fd); size = 0; return false; }
        isMapped = false;
        ssize_t remaining = static_cast<ssize_t>(size);
        char *ptr = static_cast<char *>(data);
        while (remaining > 0) {
            ssize_t n = read(fd, ptr, remaining);
            if (n <= 0) { free(data); data = nullptr; size = 0; close(fd); return false; }
            remaining -= n;
            ptr += n;
        }
    }
    close(fd);
    return true;
}

bool FileBuffer::writeToFile(const char *path, const void *data, size_t size) {
    int fd = open(path, O_WRONLY | O_CREAT | O_TRUNC, 0644);
    if (fd < 0) return false;

    ssize_t remaining = static_cast<ssize_t>(size);
    const char *ptr = static_cast<const char *>(data);
    while (remaining > 0) {
        ssize_t n = write(fd, ptr, remaining);
        if (n <= 0) { close(fd); return false; }
        remaining -= n;
        ptr += n;
    }
    close(fd);
    return true;
}

void FileBuffer::release() {
    if (data) {
        if (isMapped) munmap(data, size);
        else free(data);
        data = nullptr;
    }
    size = 0;
    isMapped = false;
}

}
