/*
 * PDPM Application
 * (C) MindTree - Not for external distribution
 *
 * Author: Girish Managoli
 *
 */

/*
 * Change Log:
 * 29-Dec-2008: Girish:
 * Removed conflicting INT8 definition
 */
 
#ifndef _PDPM_COLLECTOR_INTERNAL_H_
#define _PDPM_COLLECTOR_INTERNAL_H_

#include "pdpm_common.h"

#include <stdio.h>

#define MAX_PATH_LEN 255

#define MAX_MODULES 50

#define MAX_WINDOW_TITLE 100

#define MAX_MODULE_NAME 100

#define STR_TIME_LEN 10

#define MAX_LINE_WIDTH 255

#define MAX_NAME 100

// In some versions (probably when both Visual Studio 2008 and VC++
// are installed on the same PC, the following line gives a redefinition
// error. Hence this line is commented out by default.
// If you get a INT8 undefined, error, please uncomment this line
// typedef char INT8;

typedef enum
{
    // None, not used
    STATE_NONE,
    // PDPM Collector is running
    STATE_STARTED,
    // Stop signal received, stopping
    STATE_STOPPING,
    // Stopped
    STATE_STOPPED,
} PDPM_COLLECTOR_STATE;

typedef enum
{
    // None, not used
    STATE_DAY_LOG_NONE,
    // Day has begun, but not logged in yet
    STATE_DAY_LOG_DAY_NO_LOGIN,
    // Day has begun, has logged in, PDPM logging is started
    STATE_DAY_LOG_STARTED,
    // Day has begun, has been logged in once, but locked now
    STATE_DAY_LOG_STARTED_LOCKED,
    // Day has ended, probably not required
    STATE_DAY_LOG_END,
} PDPM_COLLECTOR_DAY_LOG_STATE;

typedef struct _PDPM_COLLECTOR_GLOBAL_CONFIG_PARAMS
{
    CHAR PDPM_FILES_PATH[MAX_PATH_LEN];
} PDPM_COLLECTOR_GLOBAL_CONFIG_PARAMS;

void pdpm_day_logger();

BOOL ifFileExists(CHAR* file);
BOOL logToFileAppl(FILE* fp, CHAR* appl_name, CHAR* appl_title);
BOOL logToFileCount(FILE* fp, UINT32 appl_count);
BOOL logToFileHeader(FILE* fp);
CHAR* getCurrentTimeStr();
INT8 getCurrentHour();
INT8 getCurrentMinute();
INT8 getCurrentSecond();

BOOL loadConfigParams();
BOOL loadConfigParam(CHAR* param_name, CHAR* param_value);

extern PDPM_COLLECTOR_GLOBAL_CONFIG_PARAMS g_global_config_params;

#endif/* #ifndef _PDPM_COLLECTOR_INTERNAL_H_ */