/*
 * PDPM Application
 * (C) MindTree - Not for external distribution
 *
 * Author: Girish Managoli
 *
 */

#ifndef _PDPM_COMMON_H_
#define _PDPM_COMMON_H_

#define _WIN32_WINNT 0x0500

#include <windows.h>

typedef unsigned short UINT16;

/* ------- Configurable Parameters ---------- */

#define PDPM_COLLECTOR_DELAY 1000

#define PDPM_COLLECTOR_MAX_LOG_ACCUMULATE 900

#define PDPM_COLLECTOR_IDLE_DELAY_SECONDS 60

/* ------- Configurable Parameters END---------- */

#define PDPM_FILE_SEPARATOR "\\"

#define PDPM_DAT_FILE_PREFIX "dat-"

#define PDPM_FILE_EXTENSION ".pdp"

#define PDPM_CONFIG_FILE_NAME "cfg-pdpm.pdp"

#define PDPM_CONFIG_FILE_VALUE_STR "VALUE"

#define PDPM_DAY_END_TIME_HOUR        23
#define PDPM_DAY_END_TIME_MINUTE      59
#define PDPM_DAY_END_TIME_SECOND      59

#define PDPM_DAY_START_TIME_HOUR      00
#define PDPM_DAY_START_TIME_MINUTE    00
#define PDPM_DAY_START_TIME_SECOND    00

#define PDPM_COLLECTOR_IDLE_DELAY_TICKS \
               (PDPM_COLLECTOR_IDLE_DELAY_SECONDS*1000)

#endif /* #ifndef _PDPM_COMMON_H_ */