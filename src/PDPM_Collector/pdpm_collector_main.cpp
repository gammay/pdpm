/*
 * PDPM Application
 * (C) MindTree - Not for external distribution
 *
 * Author: Girish Managoli
 *
 */

/*
 * Change Log:
 * 19-Jan-2008: Girish:
 * If the machine was put on hibernate, PDPM continued logging to
 * previous day's log file itself. Now, day change is checked every
 * second to avoid missing any day change notification. There is no
 * performance overhead because of this. And the earlier logic of
 * checking for day change alert after 11 PM does not work if the
 * machine is put on hibernate before this time. Hence this logic is
 * deleted.
 *
 * 29-Dec-2008: Girish:
 * Report Generation on startup does not take -yesterday parameter now.
 */
 
// #define WINVER 0x0500

#include "pdpm_collector_internal.h"
#include "pdpm_collector_debug.h"

#include <time.h>
#include <psapi.h>

#ifdef _CONSOLE
#include <signal.h>
#endif
/* ---- Globals ---- */
PDPM_COLLECTOR_STATE g_eState = STATE_NONE;

PDPM_COLLECTOR_DAY_LOG_STATE g_eDayState = STATE_DAY_LOG_NONE;

BOOL g_bStopSignal = FALSE;

TCHAR filename[MAX_MODULE_NAME*2];
CHAR c_filename[MAX_MODULE_NAME];
TCHAR appltitle[MAX_WINDOW_TITLE*2];
CHAR c_appltitle[MAX_WINDOW_TITLE];

CHAR g_active_appl_name[MAX_MODULE_NAME];
CHAR g_active_appl_title[MAX_WINDOW_TITLE];

FILE* pdpm_log_fp = NULL;
CHAR debug_time_str[STR_TIME_LEN];

/* ---- Macros ---- */
#ifdef MACRO_AS_FUNCTION
void FORMATDATE(CHAR* s)
{                                                   \
    for(unsigned int si = 0; si < strlen((s)); si++) \
    {                                       \
        if((s)[si] == '/')                \
        {                                   \
            (s)[si] = '_';                \
        }                                   \
    }                                           \
}
#else
#define FORMATDATE(s)                               \
{                                                   \
    for(unsigned int si = 0; si < strlen((s)); si++) \
    {                                       \
        if((s)[si] == '/')                  \
        {                                   \
            (s)[si] = '_';                \
        }                                   \
    }                                       \
}
#endif

#define SET_ACTIVE_APPL(appl_name, appl_title)          \
{                                           \
    RESET_ACTIVE_APPL();                    \
    strcpy(g_active_appl_name, (appl_name));\
    strcpy(g_active_appl_title, (appl_title));\
}

#define RESET_ACTIVE_APPL()                     \
{                                               \
    memset(g_active_appl_name, 0, MAX_MODULE_NAME); \
    memset(g_active_appl_title, 0, MAX_WINDOW_TITLE); \
}

#define COMPARE_ACTIVE_APPL(appl_name, appl_title) \
(appl_name != NULL) && (appl_title != NULL) && \
(strcmp(g_active_appl_name, (appl_name)) == 0) && \
(strcmp(g_active_appl_title, (appl_title)) == 0)

/* ---- Functions ---- */
CHAR* GetWindowProcess(HWND hwnd)
{
	DWORD processId;

	GetWindowThreadProcessId(hwnd, &processId);

	HANDLE hProcess = 
        OpenProcess(PROCESS_QUERY_INFORMATION | PROCESS_VM_READ,
                    FALSE,
                    processId);

	HMODULE hModuleArray[MAX_MODULES];

	DWORD num;

	EnumProcessModules(hProcess, hModuleArray, MAX_MODULES, &num);

	GetModuleBaseName(hProcess, hModuleArray[0], filename, MAX_MODULE_NAME*2);

	wcstombs(c_filename, filename, MAX_MODULE_NAME);

	return c_filename;
}

CHAR* GetWindowTitle(HWND hwnd)
{
    int ret = GetWindowText(hwnd, appltitle, MAX_WINDOW_TITLE*2);

    if(ret == 0)
    {
        return NULL;

    }

	wcstombs(c_appltitle, appltitle, MAX_WINDOW_TITLE);

	// Replace any commas in the title with another character
	int i = 0;
	while(1)
	{
		if(c_appltitle[i] == ',')
		{
			c_appltitle[i] = '_';
		}

		if(c_appltitle[i] == '\0')
		{
			break;
		}

		i++;
	}

	return c_appltitle;
}

DWORD __stdcall generate_pending_reports_thread(void* args)
{
	Sleep(1000);

	PDPM_LOG(pdpm_log_fp, "generate_pending_reports_thread started\n");

    static char* generate_pending_reports_cmd = "Generate-All-Reports.bat";
	int ret = 0;
    ret = system(generate_pending_reports_cmd);

    PDPM_LOG(pdpm_log_fp,
        "System command %s return value: %d\n",
        generate_pending_reports_cmd, ret);

    return 0;
}

#ifdef _CONSOLE
int main()
#else
int __stdcall WinMain(          HINSTANCE hInstance,
    HINSTANCE hPrevInstance,
    LPSTR lpCmdLine,
    int nCmdShow
)
#endif /* #ifdef _CONSOLE */
{
#ifdef _CONSOLE
	void sig_handler(int sig);
	signal(SIGINT, sig_handler);
#endif /* #ifdef _CONSOLE */

    /* Initialisation */

    PDPM_LOG_INIT();

    // Set time environment variable
    _tzset();

    loadConfigParams();

    RESET_ACTIVE_APPL();

    /* Generate Old Reports */
    HANDLE hThread = 
		CreateThread(NULL, 0, generate_pending_reports_thread, NULL, 0, NULL);

	PDPM_LOG(pdpm_log_fp,
		"Thread create result: %d\n", ::GetLastError());

    /* Open Control Port to Controller */
    /* TODO */

    PDPM_LOG(pdpm_log_fp, "PDPM Collector Started\n");

    // Start today's day logging
    while(1)
    {
        g_eState = STATE_STARTED;

        pdpm_day_logger();

        // Is it time to stop?
        if(g_bStopSignal)
        {
            PDPM_LOG(pdpm_log_fp, "PDPM Collector Stop Signal received\n");

            g_eState = STATE_STOPPED;
            break;
        }
    }

    PDPM_LOG(pdpm_log_fp, "PDPM Collector Ending\n");

    if(pdpm_log_fp != NULL)
    {
        fclose(pdpm_log_fp);
    }

    return 0;
}

void pdpm_day_logger()
{
    CHAR logFileName[MAX_PATH_LEN];
    CHAR logFileName_datePart[MAX_PATH_LEN];
    CHAR today_date[STR_TIME_LEN];
    CHAR check_date[STR_TIME_LEN];

    LASTINPUTINFO liinfo;

	CHAR* c_name;
    CHAR* c_title;
    FILE* pLogFile = NULL;
    // DWORD last_active_tick = 0;

    UINT32 n_active_appl_count = 0;
    BOOL stop_now = FALSE;
    BOOL idle = FALSE;

    /* Initialisation */

    liinfo.cbSize = sizeof(LASTINPUTINFO);

    memset(logFileName, 0, MAX_PATH_LEN);
    memset(logFileName_datePart, 0, MAX_PATH_LEN);

    PDPM_LOG(pdpm_log_fp, " -> pdpm_day_logger\n");

    /* Name of the Log File */

    // Path
    strcpy(logFileName, g_global_config_params.PDPM_FILES_PATH);

    // Separator
    if(strlen(logFileName) != 0)
    {
        strcat(logFileName, PDPM_FILE_SEPARATOR);
    }

    // Actual file name

    // dat qualifier
    strcpy(logFileName_datePart, PDPM_DAT_FILE_PREFIX);

    // Today's date
    _strdate(today_date);
    FORMATDATE(today_date);
    strcat(logFileName_datePart, today_date);

    // Refill the date, for comparison
    _strdate(today_date);

    // Extension
    strcat(logFileName_datePart, PDPM_FILE_EXTENSION);

    strcat(logFileName, logFileName_datePart);

#ifdef CUSTOM_FILE_NAME
    memset(logFileName, 0, sizeof(logFileName));
    strcpy(logFileName, "test-log.pdp");
#endif

    PDPM_LOG(pdpm_log_fp, "Log file name: %s\n", logFileName);

    /* Create or Open Log File */
    if(ifFileExists(logFileName))
    {
        // If its already there, append to it
        pLogFile = fopen(logFileName, "a");
        PDPM_LOG(pdpm_log_fp, "File exists, appending\n");
    }
    else
    {
        // Doesnt exist, create new one
        pLogFile = fopen(logFileName, "w");
        if(pLogFile == NULL)
        {
            CHAR message[MAX_PATH_LEN + 50];
            wchar_t w_message[(MAX_PATH_LEN + 50) * 2];

            memset(message, 0, sizeof(message));
            sprintf(message, "Log file %s could not be created. PDPM is exiting.",
                    logFileName);
            mbstowcs(w_message, message, sizeof(w_message));

            MessageBox(NULL, w_message, L"PDPM Error", MB_ICONSTOP);
            exit(-1);
        }

        logToFileHeader(pLogFile);
        PDPM_LOG(pdpm_log_fp, "File does not exist, creating\n");
    }

    // Initialise tick count value
    GetLastInputInfo(&liinfo);
    // last_active_tick = liinfo.dwTime;

    g_eDayState = STATE_DAY_LOG_DAY_NO_LOGIN;
    PDPM_LOG(pdpm_log_fp, "Checking if logged in...\n");

    /* Logging Loop */
    while(1)
    {
        /* Get current Application */
        HWND hw = GetForegroundWindow();
        if(hw == NULL)
        {
            // We assume PC is locked or idle here
			c_name = "Locked or Idle";
            idle = TRUE;
        }
        else
        {
			c_name = GetWindowProcess(hw);

            // Check if user has been idle, though with some application
            //  in foreground. This can happen if the user is not using PC,
            //  but it is not locked.
            GetLastInputInfo(&liinfo);

            if((GetTickCount() - liinfo.dwTime)
                           >= PDPM_COLLECTOR_IDLE_DELAY_TICKS)
            {
                // PDPM_LOG(pdpm_log_fp, "User is idle\n");
                idle = TRUE;
            }
            else
            {
                // last_active_tick = liinfo.dwTime;
                idle = FALSE;
            }
        }

		if(idle)
		{
            // Set state
            // Possible cases:
            if(g_eDayState == STATE_DAY_LOG_DAY_NO_LOGIN)
            {
                // - has not been logged in yet for the day

                // No action, continue to be in this state
            }
            else if(g_eDayState == STATE_DAY_LOG_STARTED)
            {
                // - has logged in and was working till now
                //   but now has locked

                g_eDayState = STATE_DAY_LOG_STARTED_LOCKED;

                PDPM_LOG(pdpm_log_fp, "Locked or Idle\n");
            }
            else if(g_eDayState == STATE_DAY_LOG_STARTED_LOCKED)
            {
                // - logged in for the day, was locked, now still locked

                // No action, continue to be in this state
            }

            c_title = "No Details";
		}
		else
		{
            // Some application is running, so PC is not locked and user is
            //  working

            // Set state
            // Possible cases:
            if(g_eDayState == STATE_DAY_LOG_DAY_NO_LOGIN)
            {
                // - first login for the day

                g_eDayState = STATE_DAY_LOG_STARTED;

                PDPM_LOG(pdpm_log_fp, "First login of the day\n");
            }
            else if(g_eDayState == STATE_DAY_LOG_STARTED)
            {
                // - already logged in and still working

                // No action
            }
            else if(g_eDayState == STATE_DAY_LOG_STARTED_LOCKED)
            {
                // - had logged in and working but locked for sometime 
                //   and now unlocked and is working

                g_eDayState = STATE_DAY_LOG_STARTED;

                PDPM_LOG(pdpm_log_fp, "Unlocked or not idle\n");
            }

            c_title = GetWindowTitle(hw);
            if(c_title == NULL)
            {
                c_title = "Unknown";
            }
		}

        /* Log to File */
        // Check if the application is the one that has
        //   already been active?
        if(COMPARE_ACTIVE_APPL(c_name, c_title))
        {
            // Yes, increment the count and skip logging
            n_active_appl_count++;

            // Check if max logs that can be accumulated has exceeded
            if(n_active_appl_count >= PDPM_COLLECTOR_MAX_LOG_ACCUMULATE)
            {
                // If yes, log to file
                logToFileCount(pLogFile, n_active_appl_count);
                n_active_appl_count = 0;
            }            
        }
        else
        {
            // No, Log the count of previous active application 
            //  Sometimes n_active_appl_count could be zero, in this case
            //  do not log the count
            if(n_active_appl_count != 0)
            {
                logToFileCount(pLogFile, n_active_appl_count);
                n_active_appl_count = 0;
            }

            //  Set new active application
            //  and start the new application count
            logToFileAppl(pLogFile, c_name, c_title);
            SET_ACTIVE_APPL(c_name, c_title);
            n_active_appl_count = 1;
        }
        
        /* Is it time to stop? - due to stop signal or day end? */        
        if(g_bStopSignal)
        {
            stop_now = TRUE;
        }
        else
        {
            // PDPM_LOG(pdpm_log_fp, "Day Change Alert Checker\n");

            // Check if we have passed and moved into next day
            //  - by checking the date
            _strdate(check_date);
            if(strcmp(today_date, check_date) != 0)
            {
                // Day has changed
                PDPM_LOG(pdpm_log_fp,
                  "Oops missed end of day, but caught date change, so ok.\n");

                stop_now = TRUE;
                goto STOP_NOW;
            }
        } // else case of if(g_bStopSignal)

STOP_NOW:
        if(stop_now)
        {
            // Close file and return
            // Dump remaining log counts
            if(n_active_appl_count != 0) 
            {
                logToFileCount(pLogFile, n_active_appl_count);
                RESET_ACTIVE_APPL();
                n_active_appl_count = 0;
            }
            
            fclose(pLogFile);
            g_eState = STATE_STOPPED;
            break;
        }
        
        Sleep(PDPM_COLLECTOR_DELAY);
    } // while(1)

    // Trigger report generation
    {
        char command_line[50];
        sprintf(command_line, "Generate-Report.bat %s", logFileName_datePart);

        PDPM_LOG(pdpm_log_fp, "Executing system command: %s\n", command_line);

        int ret = system(command_line);

        PDPM_LOG(pdpm_log_fp, "System command return value: %d\n", ret);
    }

    PDPM_LOG(pdpm_log_fp, "<- pdpm_day_logger\n");

    return;
} // pdpm_day_logger()

BOOL logToFileHeader(FILE* fp)
{
    static CHAR* header = "# PDPM Data File\n"
                    "# This is a generated file, DO NOT edit manually\n"
                    "\n"
                    "\n";

    fprintf(fp, "%s", header);
    fflush(fp);

    TCHAR t_name_buf[MAX_NAME * 2];
    CHAR  name_buf[MAX_NAME];

    unsigned long len = MAX_NAME * 2;

    // Log User Name
    memset(t_name_buf, 0, MAX_NAME*2);
    memset(name_buf, 0, MAX_NAME);
    BOOL b = GetUserName(t_name_buf, &len);
    if(b == FALSE)
    {
        PDPM_LOG(pdpm_log_fp, "GetUserName failed: %d\n", GetLastError());
    }
    else
    {
        wcstombs(name_buf, t_name_buf, MAX_NAME);
        fprintf(fp, "!USER=%s\n", name_buf);
        fflush(fp);
    }

    // Log Computer Name
    len = MAX_NAME*2;
    memset(t_name_buf, 0, MAX_NAME*2);
    memset(name_buf, 0, MAX_NAME);
    b = GetComputerName(t_name_buf, &len);
    if(b == FALSE)
    {
        PDPM_LOG(pdpm_log_fp, "GetComputerName failed: %d\n", GetLastError());
    }
    else
    {
        wcstombs(name_buf, t_name_buf, MAX_NAME);
        fprintf(fp, "!COMPUTER=%s\n", name_buf);
        fflush(fp);
    }

    // Log START mark
    fprintf(fp, "!START\n\n");
    fflush(fp);

    return TRUE;
}

BOOL logToFileAppl(FILE* fp, CHAR* appl_name, CHAR* appl_title)
{
    CHAR* str_time = getCurrentTimeStr();

    fprintf(fp, "%s,%s,%s,", str_time, appl_name, appl_title);
    fflush(fp);

    return TRUE;
}

BOOL logToFileCount(FILE* fp, UINT32 appl_count)
{
    fprintf(fp, "%d\n", appl_count);
    fflush(fp);

    return TRUE;
}

CHAR* getCurrentTimeStr()
{
    static CHAR str_time[STR_TIME_LEN];

    memset(str_time, 0, STR_TIME_LEN);

    _strtime(str_time);

    return str_time;
}

INT8 getCurrentHour()
{
    time_t tm = time(NULL);

    struct tm* tm_struct = localtime(&tm);

    return tm_struct->tm_hour;
}

INT8 getCurrentMinute()
{
    time_t tm = time(NULL);

    struct tm* tm_struct = localtime(&tm);

    return tm_struct->tm_min;
}

INT8 getCurrentSecond()
{
    time_t tm = time(NULL);

    struct tm* tm_struct = localtime(&tm);

    return tm_struct->tm_sec;
}

BOOL ifFileExists(CHAR* file)
{
    TCHAR wfile[MAX_PATH_LEN*2];
    mbstowcs(wfile, file, MAX_PATH_LEN*2);

    HANDLE hFile = CreateFile(
                        wfile, // LPCTSTR lpFileName
                        GENERIC_READ, // DWORD dwDesiredAccess
                        0, // DWORD dwShareMode
                        NULL, // LPSECURITY_ATTRIBUTES lpSecurityAttributes
                        OPEN_EXISTING, // DWORD dwCreationDisposition
                        FILE_ATTRIBUTE_NORMAL, // DWORD dwFlagsAndAttributes
                        NULL // HANDLE hTemplateFile
                    );

    if(hFile != INVALID_HANDLE_VALUE)
    {
        CloseHandle(hFile);
        return TRUE;
    }
    else
    {
        return FALSE;
    }
}

#ifdef _CONSOLE
void sig_handler(int sig)
{
    int retries = 20000;

	g_bStopSignal = TRUE;

	while(g_eState != STATE_STOPPED)
	{
		Sleep(500);
        retries--;
        if(retries <= 0)
        {
	        // fclose(g_pLogFile);
	        exit(0);
        }
	}
}
#endif /* #ifdef _CONSOLE */
