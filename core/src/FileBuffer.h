#ifndef ZPATCH_FILEBUFFER_H
#define ZPATCH_FILEBUFFER_H

#include <cstddef>

#ifdef _WIN32
    #include <windows.h>
    typedef HANDLE file_handle_t;
    #define INVALID_HANDLE_VALUE_ INVALID_HANDLE_VALUE
#else
    typedef int file_handle_t;
    #define INVALID_HANDLE_VALUE_ (-1)
#endif

namespace zpatch {

class FileBuffer {
public:
    void *data;
    size_t size;
    bool isMapped;

    FileBuffer();
    ~FileBuffer();

    bool readFromFile(const char *path);
    static bool writeToFile(const char *path, const void *data, size_t size);
    void release();
};

}
#endif
