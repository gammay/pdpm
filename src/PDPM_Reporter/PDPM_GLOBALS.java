/*
 * <one line to give the program's name and a brief idea of what it does.>
 *
 * Copyright (C) 2008 Girish Managoli
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.utils.pdpm;

public interface PDPM_GLOBALS
{
    public static final Integer PDPM_MAX_APPLICATIONS = 1024;

    public static final String PDPM_MASTER_CONFIG_FILE = 
        "cfg-pdpm.pdp";

    public static final String PDPM_APPLICATIONS_CONFIG_FILE = 
        "cfg-applications.pdp";

    public static final String PDPM_BUILD_INFO_FILE = 
        "bld-info.pdp";

    public static final String PDPM_CONFIG_PATTERN_TYPE = 
        "TYPE";

    public static final String PDPM_CONFIG_PATTERN_VALUE = 
        "VALUE";

    public static final String PDPM_CONFIG_TYPE_STRING = 
        "STRING";

    public static final String PDPM_CONFIG_TYPE_FOLDER_SELECTION =
        "FOLDER_SELECTION";

    public static final String PDPM_CONFIG_TYPE_YES_NO =
        "YES_NO";

    public static final String PDPM_CONFIG_PARAM_PDPM_FILES_PATH =
        "PDPM_FILES_PATH";

    public static final String PDPM_CONFIG_PARAM_PDPM_REPORTS_PATH =
        "PDPM_REPORTS_PATH";

    public static final String PDPM_CONFIG_PARAM_PDPM_DELETE_OLD_LOGS =
        "PDPM_DELETE_OLD_LOGS";

    public static final String PDPM_LOCKED_APPLICATION_NAME =
        "Locked or Idle";

    public static final String PDPM_LOCKED_CATEGORY_UNKNOWN =
        "Unknown";

    public static final String PDPM_LOCKED_APPLICATION_UNKNOWN =
        "Unknown";

    public static final String PDPM_APPLICATION_TITLE_UNKNOWN =
        "No Details";

    public static final String PDPM_PATTERN_START     = "!START";
    public static final String PDPM_PATTERN_USER      = "!USER";
    public static final String PDPM_PATTERN_COMPUTER  = "!COMPUTER";
    public static final String PDPM_PATTERN_LOGSTART  = "!LOGSTART";
    public static final String PDPM_PATTERN_LOGEND    = "!LOGEND";
    public static final String PDPM_PATTERN_DAYLOGIN  = "!DAYLOGIN";
    public static final String PDPM_PATTERN_DAYLOGOUT = "!DAYLOGOUT";

    public static final String PDPM_PATTERN_DAYLOGIN_FRIENDLY  
                                                   = "Day Login At";
    public static final String PDPM_PATTERN_DAYLOGOUT_FRIENDLY 
                                                   = "Day Logoff At";
    public static final String PDPM_PATTERN_DAY_TOTAL_FRIENDLY 
                                                   = "Day Total Time";

    // First field starts from 1
    public static final int REPORT_FILE_FIELD_APPL_NAME     = 1;
    public static final int REPORT_FILE_FIELD_FRIENDLY_NAME = 2;
    public static final int REPORT_FILE_FIELD_APPL_TITLE    = 3;
    public static final int REPORT_FILE_FIELD_CATEGORY      = 4;
    public static final int REPORT_FILE_FIELD_TIME          = 5;

    public static final String STANDARD_DAY_START = "09:00:00";
    public static final String STANDARD_DAY_END   = "18:30:00";
    public static final String STANDARD_DAY_LOGSTART = "00:00:00";
    public static final String STANDARD_DAY_LOGEND   = "23:59:59";

    public static final int REPORT_FORMAT_APPL_TITLE_MAX_LEN = 45;
    public static final String REPORT_FORMAT_CATEGORY_VARIOUS = "Various";

    public static final String WRITE_REPORT_NAME_FILE_TO = 
                                                   "Open-Current-Report.bat";

}
