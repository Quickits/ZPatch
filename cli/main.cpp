#include <iostream>
#include <string>
#include "zpatch/core.h"

void printVersion() {
    std::cout << "ZPatch " << zpatch::getVersion() << std::endl;
}

void printUsage(const char* prog) {
    std::cout << "Usage: " << prog << " <command> [options]\n\n"
              << "Commands:\n"
              << "  create <old> <new> <patch>  Create a patch file\n"
              << "  apply <old> <patch> <new>    Apply a patch file\n"
              << "  version                       Show version\n";
}

int main(int argc, char* argv[]) {
    if (argc < 2) {
        printUsage(argv[0]);
        return 1;
    }

    std::string cmd = argv[1];

    if (cmd == "version") {
        printVersion();
        return 0;
    }

    if (cmd == "create" && argc == 5) {
        zpatch::Result result = zpatch::createPatch(argv[2], argv[3], argv[4]);
        if (result.code == 0) {
            std::cout << "Patch created: " << argv[4]
                      << " (" << result.originalSize << " bytes)" << std::endl;
            return 0;
        } else {
            std::cerr << "Error: " << result.message << std::endl;
            return 1;
        }
    }

    if (cmd == "apply" && argc == 5) {
        zpatch::Result result = zpatch::applyPatch(argv[2], argv[3], argv[4]);
        if (result.code == 0) {
            std::cout << "Patch applied: " << argv[4]
                      << " (" << result.originalSize << " bytes)" << std::endl;
            return 0;
        } else {
            std::cerr << "Error: " << result.message << std::endl;
            return 1;
        }
    }

    printUsage(argv[0]);
    return 1;
}
