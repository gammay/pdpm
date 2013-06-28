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

import com.utils.pdpm.PDPM_GLOBALS;
import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_DEBUG;

interface PDPM_Report_Types
{
    public interface PDPM_REPORT_TYPE
    {
        public static final int PDPM_REPORT_TYPE_ALL                  = 100;

        public static final int PDPM_REPORT_TYPE_CATEGORY             = 101;
        public static final int PDPM_REPORT_TYPE_APPLICATIONS         = 102;
        public static final int PDPM_REPORT_TYPE_APPLICATION_DETAILED = 103;

        public static final String PDPM_REPORT_NAME_CATEGORY             = "By Category";
        public static final String PDPM_REPORT_NAME_APPLICATIONS         = "By Applications";
        public static final String PDPM_REPORT_NAME_APPLICATION_DETAILED = "By Application Detailed";

        public static final int PDPM_REPORT_TYPE_DEFAULT = 
            PDPM_REPORT_TYPE_CATEGORY;
    }

    public class PDPM_Details_List
    {
        private int num_columns;

        private String column[];

        public static final int TYPE_ALPHANUMERIC = 501;
        public static final int TYPE_NUMERIC      = 502;
        public static final int TYPE_NUMERIC_FLOAT= 503;

        private static final boolean DEBUG = false;

        private static void DEBUG(String str)
        {
            if(DEBUG)
            {
                PDPM_DEBUG.PDPM_REPORTER_LOG(str);
            }
        }

        public PDPM_Details_List(int columns)
        {
            num_columns = columns;

            column = new String[num_columns];
        }

        public String get(int n)
        {
            if(n >= num_columns)
            {
                throw new ArrayIndexOutOfBoundsException(n);
            }

            return column[n];
        }

        public void set(int n, String str)
        {
            if(n >= num_columns)
            {
                throw new ArrayIndexOutOfBoundsException(n);
            }

            // column[n] = str;
            if(column[n] != null)
            {
                column[n] = null;
            }

            column[n] = new String(str);
        }

        private void set_internal(int n, String str)
        {
            if(n >= num_columns)
            {
                throw new ArrayIndexOutOfBoundsException(n);
            }

            column[n] = str;
            /*
            if(column[n] != null)
            {
                column[n] = null;
            }

            column[n] = new String(str);
            */
        }

        public int size()
        {
            return num_columns;
        }

        public void copy(PDPM_Details_List to_list)
        {
            for(int n = 0; n < num_columns; n++)
            {
                to_list.set(n, this.get(n));
            }
        }

        private void copy_internal(PDPM_Details_List to_list)
        {
            for(int n = 0; n < num_columns; n++)
            {
                to_list.set_internal(n, this.get(n));
            }
        }

        public static void sort(PDPM_Details_List table[],
                                int column,
                                int column_type)
        {
            DEBUG("--> PDPM_Details_List.sort");

            // DEBUG("Before sorting:");
            // debug_print(table);

            if(column_type == TYPE_ALPHANUMERIC)
            {
                // Sort using simple bubble sort
                for(int outer = 0; outer < table.length; outer++)
                {
                    if(table[outer] == null)
                    {
                        break;
                    }

                    String compare_str = table[outer].get(column);

                    for(int inner = outer; inner < table.length; inner++)
                    {
                        if(table[inner] == null)
                        {
                            break;
                        }

                        String compare_to = table[inner].get(column);

                        DEBUG("Comparing " +
                                compare_str + "with value: " +
                                compare_to);

                        int compare_val = 
                            compare_str.compareToIgnoreCase(compare_to);

                        if(compare_val < 0)
                        {
                            // Do nothing
                            DEBUG("Smaller");
                        }
                        else if(compare_val > 0)
                        {
                            // Swap
                            // Swap both the numeric array and actual list

                            DEBUG("Greater");

                            DEBUG("Swapping with: " +
                                compare_to);

                            // Numeric Array
                            String temp_swap = compare_to;
                            compare_to = compare_str;
                            compare_str = temp_swap;

                            // Actual list
                            DEBUG("Swapping the rows " +
                                    table[outer].get(0) + " with: " +
                                    table[inner].get(0) + 
                                    " (showing first column only)");

                            // - Copy to temporary location
                            PDPM_Details_List temp_row = null;
                            temp_row = 
                                new PDPM_Details_List(table[inner].size());

                            // table[inner].copy(temp_row);
                            table[inner].copy_internal(temp_row);
                            // - swap
                            // table[outer].copy(table[inner]);
                            table[outer].copy_internal(table[inner]);
                            // temp_row.copy(table[outer]);
                            temp_row.copy_internal(table[outer]);

                            // debug_print(table);
                        }
                        else
                        {
                            // Equal. Do nothing
                            DEBUG("Equal");
                        }
                    } // END: for inner
                } // END: for outer
            }
            else if(column_type == TYPE_NUMERIC)
            {
                // First convert all numbers in string form to integers
                //  for easy sorting

                // DEBUG(
                        // "Table length is: " + table.length);

                int numeric_array[] = new int[table.length];
                for(int n = 0; n < table.length - 1; n++)
                {
                    if(table[n] == null)
                    {
                        break;
                    }

                    DEBUG("n = " + n);
                    DEBUG("Read integer in column: " +
                            table[n].get(column));

                    int nn = 0;

                    try
                    {
                        nn = Integer.parseInt(table[n].get(column));
                    }
                    catch(NumberFormatException nfe)
                    {
                        // Exception is possible if the column is not integer
                        // Just take the number as zero;
                        DEBUG("Not an int");
                    }

                    numeric_array[n] = nn;
                }

                // Now sort using simple bubble sort
                for(int outer = 0; outer < table.length; outer++)
                {
                    if(table[outer] == null)
                    {
                        break;
                    }

                    int compare_num = numeric_array[outer];

                    for(int inner = outer; inner < table.length; inner++)
                    {
                        if(table[inner] == null)
                        {
                            break;
                        }

                        DEBUG("Comparing " +
                                compare_num + "with value: " +
                                numeric_array[inner]);

                        if(compare_num > numeric_array[inner])
                        {
                            // Do nothing
                            DEBUG("Greater");
                        }
                        else if(compare_num < numeric_array[inner])
                        {
                            // Swap
                            // Swap both the numeric array and actual list

                            DEBUG("Smaller");

                            DEBUG("Swapping with: " +
                                numeric_array[inner]);

                            // Numeric Array
                            int temp_swap = numeric_array[inner];
                            numeric_array[inner] = compare_num;
                            numeric_array[outer] = temp_swap;
                            compare_num = temp_swap;

                            // Actual list
                            DEBUG("Swapping the rows " +
                                    table[outer].get(0) + " with: " +
                                    table[inner].get(0) + 
                                    " (showing first column only)");

                            // - Copy to temporary location
                            PDPM_Details_List temp_row = null;
                            temp_row = 
                                new PDPM_Details_List(table[inner].size());

                            // table[inner].copy(temp_row);
                            table[inner].copy_internal(temp_row);
                            // - swap
                            // table[outer].copy(table[inner]);
                            table[outer].copy_internal(table[inner]);
                            // temp_row.copy(table[outer]);
                            temp_row.copy_internal(table[outer]);

                            // debug_print(table);
                        }
                        else
                        {
                            // Equal. Do nothing
                            DEBUG("Equal");
                        }
                    } // END: for inner
                } // END: for outer
            } // END: if
            else if(column_type == TYPE_NUMERIC_FLOAT)
            {
                // First convert all numbers in string form to integers
                //  for easy sorting

                // DEBUG(
                        // "Table length is: " + table.length);

                float numeric_array[] = new float[table.length];
                for(int n = 0; n < table.length - 1; n++)
                {
                    if(table[n] == null)
                    {
                        break;
                    }

                    DEBUG("n = " + n);
                    DEBUG("Read float in column: " +
                            table[n].get(column));

                    float nn = 0;

                    try
                    {
                        nn = Float.parseFloat(table[n].get(column));
                    }
                    catch(NumberFormatException nfe)
                    {
                        // Exception is possible if the column is not integer
                        // Just take the number as zero;
                        DEBUG("Not a float");
                    }

                    numeric_array[n] = nn;
                }

                // Now sort using simple bubble sort
                for(int outer = 0; outer < table.length; outer++)
                {
                    if(table[outer] == null)
                    {
                        break;
                    }

                    float compare_num = numeric_array[outer];

                    for(int inner = outer; inner < table.length; inner++)
                    {
                        if(table[inner] == null)
                        {
                            break;
                        }

                        DEBUG("Comparing " +
                                compare_num + "with value: " +
                                numeric_array[inner]);

                        if(compare_num > numeric_array[inner])
                        {
                            // Do nothing
                            DEBUG("Greater");
                        }
                        else if(compare_num < numeric_array[inner])
                        {
                            // Swap
                            // Swap both the numeric array and actual list

                            DEBUG("Smaller");

                            DEBUG("Swapping with: " +
                                numeric_array[inner]);

                            // Numeric Array
                            float temp_swap = numeric_array[inner];
                            numeric_array[inner] = compare_num;
                            numeric_array[outer] = temp_swap;
                            compare_num = temp_swap;

                            // Actual list
                            DEBUG("Swapping the rows " +
                                    table[outer].get(0) + " with: " +
                                    table[inner].get(0) + 
                                    " (showing first column only)");

                            // - Copy to temporary location
                            PDPM_Details_List temp_row = null;
                            temp_row = 
                                new PDPM_Details_List(table[inner].size());

                            // table[inner].copy(temp_row);
                            table[inner].copy_internal(temp_row);
                            // - swap
                            // table[outer].copy(table[inner]);
                            table[outer].copy_internal(table[inner]);
                            // temp_row.copy(table[outer]);
                            temp_row.copy_internal(table[outer]);

                            // debug_print(table);
                        }
                        else
                        {
                            // Equal. Do nothing
                            DEBUG("Equal");
                        }
                    } // END: for inner
                } // END: for outer
            } // END: if

            // DEBUG("After sorting:");
            // debug_print(table);

            DEBUG("<-- PDPM_Details_List.sort");
        } // END: sort

        public static void debug_print(PDPM_Details_List[] table)
        {
            DEBUG("-> PDPM_Details_List.debug_print()");

            for(int row = 0; row < table.length; row++)
            {
                if(table[row] == null)
                {
                    break;
                }
                
                String srow = "Row [" + row + "]:";

                for(int col = 0; col < table[row].size(); col++)
                {
                    srow = srow + "    " + table[row].get(col);
                }

                DEBUG(srow);
            }

            DEBUG("<- PDPM_Details_List.debug_print()");
        } // END: debug_print

    } // END: PDPM_Details_List
} // END: PDPM_Report_Types

