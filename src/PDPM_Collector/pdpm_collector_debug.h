
#ifndef _PDPM_COLLECTOR_DEBUG_H_
#define _PDPM_COLLECTOR_DEBUG_H_

#include <assert.h>

#define PDPM_ERR_CRITICAL(message, error)       \
{                                               \
    assert(0);                                  \
}

extern FILE* pdpm_log_fp;
extern CHAR debug_time_str[];

#define PDPM_LOG_INIT() \
{ \
    pdpm_log_fp = fopen("pdpm.log", "a"); \
    fprintf(pdpm_log_fp, "\n---- PDPM LOG START ---- \n"); \
}

#define PDPM_LOG \
    _strtime(debug_time_str); \
    fprintf(pdpm_log_fp, "[%s][%s][%d] ", debug_time_str, __FILE__, __LINE__); \
    fflush(pdpm_log_fp); \
    fprintf

// #define MACRO_AS_FUNCTION

// #define CUSTOM_FILE_NAME

#endif /* #ifndef _PDPM_COLLECTOR_DEBUG_H_ */