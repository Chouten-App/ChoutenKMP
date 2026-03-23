#include "relay_host_functions.h"

m3ApiRawFunction(logFunc) {
    m3ApiGetArgMem(const char*, msg);
    m3ApiGetArg(i32, len);
    host_log(msg, len);
    m3ApiSuccess();
}
