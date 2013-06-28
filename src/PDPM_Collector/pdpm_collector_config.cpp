
#include "pdpm_collector_internal.h"
#include "pdpm_collector_debug.h"

#include <stdio.h>

typedef struct _PDPM_CONFIG_PARAM_TABLE
{
    CHAR* param_name;
    CHAR* param_value;
} PDPM_CONFIG_PARAM_TABLE;

PDPM_CONFIG_PARAM_TABLE g_pdpm_config_param_table[] =
{
    { "PDPM_FILES_PATH", g_global_config_params.PDPM_FILES_PATH },
};

PDPM_COLLECTOR_GLOBAL_CONFIG_PARAMS g_global_config_params;


#define STRIP_NEWLINE(str)                      \
{                                               \
    for(UINT16 ni = 0; ni < strlen((str)); ni++)   \
    {                                           \
        if((str)[ni] == '\n')                   \
        {                                       \
            (str)[ni] = '\0';                   \
            break;                              \
        }                                       \
    }                                           \
}

BOOL loadConfigParams()
{
    for(int i = 0;
        i < (sizeof(g_pdpm_config_param_table)/sizeof(PDPM_CONFIG_PARAM_TABLE));
        i++)
    {
        if(!loadConfigParam(g_pdpm_config_param_table[i].param_name,
                            g_pdpm_config_param_table[i].param_value))
        {
            PDPM_ERR_CRITICAL("loadConfigParam failed\n", 0);
            // return FALSE;
        }
    }

    return TRUE;
}

BOOL loadConfigParam(CHAR* param_name, CHAR* param_value)
{
    FILE* fp_config_file = NULL;
    CHAR err_msg[50];
    CHAR line_read[MAX_LINE_WIDTH];
    BOOL b_found = FALSE;
    CHAR* search_str;

    if(param_name == NULL || param_value == NULL)
    {
        return FALSE;
    }

    /* Open Config File */
    fp_config_file = fopen(PDPM_CONFIG_FILE_NAME, "r");
    if(fp_config_file == NULL)
    {
        sprintf(err_msg,
            "Opening Config File failed [%s]\n",
            PDPM_CONFIG_FILE_NAME);

        PDPM_ERR_CRITICAL(err_msg, 0);
    }

    /* Search for Config Param */
    while(1)
    {
        if(fgets(line_read, MAX_LINE_WIDTH, fp_config_file) == NULL)
        {
            break;
        }

        // Skip comment lines
        if(line_read[0] == '#')
        {
            continue;
        }

        // Strip new line character
        STRIP_NEWLINE(line_read);

        if(strcmp(line_read, param_name) == 0)
        {
            b_found = TRUE;
            break;
        }
    }

    /* Load its value */
    if(b_found)
    {
        // Value is not found yet
        b_found = FALSE;

        while(1)
        {
            if(fgets(line_read, MAX_LINE_WIDTH, fp_config_file) == NULL)
            {
                break;
            }

            search_str = strstr(line_read, PDPM_CONFIG_FILE_VALUE_STR);

            if(search_str == NULL)
            {
                continue;
            }
            else
            {
                search_str = search_str + strlen(PDPM_CONFIG_FILE_VALUE_STR);

                while(*search_str == ' ' || *search_str == '=')
                {
                    search_str++;
                }

                *param_value = '\0';

                while(1)
                {
                    if(*search_str == '\n' || *search_str == '\0')
                    {
                        break;
                    }

                    if(*search_str == '"')
                    {
	                    *search_str++;
                        continue;
                    }

                    *param_value = *search_str;
                    *param_value++;
                    *search_str++;
                }

                /*
                if(*search_str == '"')
                {
                    search_str++;
                    while(1)
                    {
                        *param_value = *search_str;
                        *param_value++;
                        *search_str++;

                        if((*search_str == '"')  ||
                           (*search_str == '\0') ||
                           (*search_str == '\n'))
                        {
                            break;
                        }
                    }
                }
                else
                {
                    while(1)
                    {
                        *param_value = *search_str;
                        *param_value++;
                        *search_str++;

                        if((*search_str == '\0') ||
                           (*search_str == '\n'))
                        {
                            break;
                        }
                    }
                }
                */

                *param_value ='\0';

                b_found = TRUE;

                break;
            }
        }
    }

    /* Close File */
    if(fp_config_file != NULL)
    {
        fclose(fp_config_file);
    }

    /* Return */
    return b_found;
}
