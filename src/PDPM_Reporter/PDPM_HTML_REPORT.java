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

import java.io.*;

import com.utils.pdpm.PDPM_GLOBALS;
import com.utils.pdpm.PDPM_ERROR;
import com.utils.pdpm.PDPM_DEBUG;

import com.utils.pdpm.PDPM_Report_Types;
import com.utils.pdpm.PDPM_UI_Plugin;
import com.utils.pdpm.PDPM_Report_View;

public class PDPM_HTML_REPORT extends PDPM_UI_Plugin
{
    public static final int BAR_CHART_MULTIPLIER = 4;

    private PrintStream out = null;

    private String report_file_name = null;
    private String report_file_full_path = null;

    /*
    private int details_required[] =
    {
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_CATEGORY,
        PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_APPLICATIONS,
    };

    private String details_description[] =
    {
        "By Category",
        "By Application",
    };

    private int details_index = 0;
    */

    public static void main(String[] args)
    {
        PDPM_DEBUG.PDPM_DEBUG_INIT();

        PDPM_HTML_REPORT ui = new PDPM_HTML_REPORT();

        PDPM_Report_View report = null;

        try
        {
        if(args != null && args.length >= 1)
        {
            report = new PDPM_Report_View(ui, args[0]);
        }
        else
        {
            report = new PDPM_Report_View(ui, null);
        }

        report.start(PDPM_Report_Types.PDPM_REPORT_TYPE.PDPM_REPORT_TYPE_ALL);
        /*
        for(int i = 0; i < ui.details_required.length; i++)
        {
            report.start(ui.details_required[i]);
        }
        */
        }
        catch(Exception e)
        {
            System.out.println(e);
            e.printStackTrace();
        }
    }

    public String get_report_file_name()
    {
        // return report_file_name;
        return report_file_full_path;
    }
    
    public void Start(String date_short)
    {
        if(out == null)
        {
            String report_file_config_path = 
                (String) PDPM_Config_Reader.get_config_param(
                        PDPM_GLOBALS.PDPM_CONFIG_PARAM_PDPM_REPORTS_PATH);

            if(report_file_config_path == null)
            {
                report_file_config_path = "";
            }
            else
            {
                if(report_file_config_path.endsWith("/") ||
                        report_file_config_path.endsWith("\\"))
                {
                }
                else
                {
                    report_file_config_path = report_file_config_path + "/";
                }
            }

            report_file_name = "PDPM-Report-" + date_short + ".html";
            report_file_full_path = report_file_config_path + report_file_name;

            try
            {
                out = new PrintStream(report_file_full_path);
            }
            catch(Exception e)
            {
                System.out.println("Error in creating report file. " + 
                        report_file_full_path);
                return;
            }
        }
        
        // HTML Header
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">");
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<META HTTP-EQUIV=\"CONTENT-TYPE\" CONTENT=\"text/html; charset=utf-8\">");
    }

    public void EndNote(String note)
    {
        out.println("<FONT FACE=\"Arial\"><FONT SIZE=1><I> *" + note + "</I></FONT>");
    }
    
    public void End(String info)
    {
        out.println("<BR>");
        out.println("<FONT FACE=\"Arial\"><FONT SIZE=1><I>" + info + "</I></FONT>");
        out.println("<BR>");

        out.println("</BODY>");
        out.println("</HTML>");
    }
    
    public void Error(String err)
    {
        out.println("<P>");
        out.println("<P>");
        out.println("<B>>>>>> Error in processing: " + err + "</B>");
        out.println("<P>");
        out.println("<P>");
    }
    
    public void Heading(String heading)
    {
        out.println("<TITLE>" + heading + "</TITLE>");
        out.println("<META NAME=\"GENERATOR\" CONTENT=\"PDPM Application\">");
        out.println("<STYLE>");
        out.println("<!--");
        out.println("@page { size: 8.5in 11in; margin: 0.79in }");
        out.println("P { margin-bottom: 0.08in }");
        out.println("TD P { margin-bottom: 0in }");
        out.println("TH P { margin-bottom: 0in; font-style: italic }");
        out.println("-->");
        out.println("</STYLE>");
        out.println("</HEAD>");
        out.println("<BODY LANG=\"en-US\" DIR=\"LTR\">");
        out.println("<P ALIGN=CENTER STYLE=\"margin-bottom: 0in\"><A NAME=\"Go to Summary\"></A><BR>");
        out.println("</P>");

        out.println("<P ALIGN=CENTER STYLE=\"margin-bottom: 0in\"><FONT FACE=\"Arial\"><FONT SIZE=3><B>" + heading + "</B></FONT></P>");
        // out.println("<P ALIGN=CENTER STYLE=\"margin-bottom: 0in; font-weight: medium\"><FONT SIZE=3>(Generated by PDPM)</FONT></P>");
        out.println("<P STYLE=\"margin-bottom: 0in\"><BR>");
        out.println("</P>");
    }

    public void SummaryStart()
    {
        out.println("<CENTER>");
        out.println("   <TABLE BORDER=1 BORDERCOLOR=\"#ffcc99\" CELLPADDING=4 CELLSPACING=0>");
        out.println("    <COL WIDTH=240>");
        out.println("    <COL WIDTH=170>");

        out.println("    <TBODY>");
    }

    public void SummaryHeader(String header1, String header2)
    {
    }

    public void SummaryRow(String col1, String col2)
    {
        out.println("       <TR VALIGN=TOP>");
        out.println("           <TD BGCOLOR=\"#ffcc99\">");
        out.println("               <P ALIGN=CENTER><FONT FACE=\"Arial\">" + col1 + "</P>");
        out.println("           </TD>");
        out.println("           <TD>");
        if(col2 == null)
        {
            out.println("               <P ALIGN=CENTER><FONT FACE=\"Arial\">None</P>");
        }
        else
        {
            out.println("               <P ALIGN=CENTER><FONT FACE=\"Arial\">" + col2 + "</P>");
        }
        out.println("           </TD>");
        out.println("       </TR>");
    }

    public void SummaryEnd()
    {
        out.println("   </TBODY>");
        out.println("   </TABLE>");
        out.println("</CENTER>");
    }

    public void ProcessingNoteStart()
    {
        out.println("<BR>");
        out.println("<BR>");

        out.println("<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=0\">");

        /*
        out.println("<THEAD>");
        out.println("   <TR>");
        out.println("   <TH VALIGN=TOP BGCOLOR=\"#ffcc99\">");
        out.println("       <P STYLE=\"font-style: normal\"><FONT FACE=\"Arial\"><FONT SIZE=2>Notes</FONT></FONT></P>");
        out.println("   </TH>");
        out.println("   </TR>");
        out.println("</THEAD>");
        */

        out.println("<TBODY>");
    }

    public void ProcessingNoteRow(String note)
    {
        out.println("   <TR>");
        out.println("   <TD VALIGN=TOP>");
        out.println("       <P><FONT SIZE=2><FONT FACE=\"Arial\"><I>" +  note + "</I></A></P>");
        out.println("   </TD>");
        out.println("   </TR>");
    }

    public void ProcessingNoteEnd()
    {
        out.println("</TBODY>");
        out.println("</TABLE>");
        out.println("</CENTER>");
    }

    public void DetailsAll(String[] details_description)
    {
        out.println("<BR>");
        out.println("<BR>");

        out.println("<CENTER>");
        out.println("<TABLE BORDER=1 BORDERCOLOR=\"#ffcc99\" CELLPADDING=1 CELLSPACING=0\">");
        out.println("<COL WIDTH=283>");

        out.println("<THEAD>");
        out.println("   <TR>");
        out.println("   <TH VALIGN=TOP BGCOLOR=\"#ffcc99\">");
        out.println("       <P STYLE=\"font-style: normal\"><FONT FACE=\"Arial\"><FONT SIZE=2>Details</FONT></FONT></P>");
        out.println("   </TH>");
        out.println("   </TR>");
        out.println("</THEAD>");

        out.println("<TBODY>");
        
        for(int i = 0; i < details_description.length; i++)
        {
            out.println("   <TR>");
            out.println("   <TD VALIGN=TOP>");
            out.println("       <P ALIGN=CENTER><A HREF=\"#" +  details_description[i] + "\"><FONT SIZE=2><FONT FACE=\"Arial\">" +  details_description[i] + "</A></P>");
            out.println("   </TD>");
            out.println("   </TR>");
            // out.println("<P ALIGN=CENTER STYLE=\"margin-bottom: 0in\"><A HREF=\"#" + details_description[i] + "\">" + details_description[i] + "</A></P>");
        }

        out.println("</TBODY>");
        out.println("</TABLE>");
        out.println("</CENTER>");

        out.println("<BR>");
        out.println("<BR>");
        out.println("<HR>");
    }
    

    public void DetailsStart(String details_description)
    {
        out.println("<BR>");
        out.println("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=1>");
        
        out.println("   <TR VALIGN=TOP>");

        out.println("       <TD WIDTH=250 NOWRAP>");
        out.println("           <P STYLE=\"margin-bottom: 0in\"><A NAME=\"" + details_description + "\"></A><B><FONT SIZE=2><FONT FACE=\"Arial\">" + details_description + "</B></P>");
        out.println("       </TD>");
        /*
        out.println("       <TD WIDTH=150 NOWRAP>");
        out.println(
            "<P STYLE=\"margin-bottom: 0in\"><FONT SIZE=2><FONT FACE=\"Arial\"><A HREF=\"#Go to Summary\">[Go to Summary]</A>");
        out.println("       </TD>");
        */
        out.println("   </TR>");
    }

    public void DetailsHeading(PDPM_Report_Types.PDPM_Details_List list)
    {
        out.println("<TABLE BORDER=1 BORDERCOLOR=\"#ffe4c9\" CELLPADDING=2 CELLSPACING=0>");
        /*
        out.println("<COL WIDTH=255>");
        out.println("<COL WIDTH=127>");
        */
        
        out.println("<THEAD>");
        out.println("   <TR VALIGN=TOP>");
        for(int i = 0; i < list.size(); i++)
        {
            if(i == 0)
            {
                out.println("       <TH WIDTH=125 BGCOLOR=\"#ffcc99\">");
                out.println("           <P><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("       </TH>");
            }
            else
            {
                out.println("       <TH WIDTH=100 BGCOLOR=\"#ffcc99\">");
                out.println("           <P><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("       </TH>");
            }
        }
        out.println("   </TR>");
        out.println("</THEAD>");

        out.println("<TBODY>");
    }

    public void DetailsRow(PDPM_Report_Types.PDPM_Details_List list)
    {
        out.println("   <TR VALIGN=TOP>");
        for(int i = 0; i < list.size(); i++)
        {
            if(i == 0)
            {
                // For first row only shading and left aligned
                out.println("       <TD NOWRAP WIDTH=150 BGCOLOR=\"#ffcc99\">");
                out.println("           <P><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
            }
            else
            {
                out.println("       <TD NOWRAP WIDTH=100>");
                out.println("           <P ALIGN=CENTER><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
            }

            out.println("       </TD>");
        }
        out.println("   </TR>");
    }

    public void DetailsEnd(String note)
    {
        out.println("</TBODY>");
        out.println("</TABLE>");
        if(note != null)
        {
            out.println("<FONT FACE=\"Arial\"><FONT SIZE=1><I>" + note +
                   "</I></FONT>");
            out.println("<BR>");
        }

        out.println(
            "<FONT SIZE=2><FONT FACE=\"Arial\"><A HREF=\"#Go to Summary\">[Go to Summary]</A>");

        out.println("<BR>");
    }

    public void DetailsType2Start(String details_description)
    {
        /*
        out.println("<BR>");
        out.println("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=1>");
        
        out.println("   <TR VALIGN=TOP>");

        out.println("       <TD WIDTH=500 NOWRAP>");
        out.println("           <P STYLE=\"margin-bottom: 0in\"><A NAME=\"" + details_description + "\"></A><B><FONT SIZE=2><FONT FACE=\"Arial\">" + details_description + "</B></P>");
        out.println("       </TD>");
        out.println("       <TD WIDTH=150 NOWRAP>");
        out.println(
            "<P STYLE=\"margin-bottom: 0in\"><FONT SIZE=2><FONT FACE=\"Arial\"><A HREF=\"#Go to Summary\">[Go to Summary]</A>");
        out.println("       </TD>");
        out.println("   </TR>");
        */
        DetailsStart(details_description);
    }

    public void DetailsType2Heading(PDPM_Report_Types.PDPM_Details_List list)
    {
        out.println("<TABLE BORDER=0 CELLPADDING=2 CELLSPACING=1>");
        
        out.println("<THEAD>");
        out.println("   <TR VALIGN=TOP>");
        for(int i = 0; i < list.size(); i++)
        {
            if(i == 0)
            {
                out.println("       <TH NOWRAP WIDTH=150 BGCOLOR=\"#ffcc99\">");
                out.println("           <P ALIGN=CENTER><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("       </TH>");
            }
            else
            {
                out.println("       <TH NOWRAP WIDTH=100 BGCOLOR=\"#ffcc99\">");
                out.println("           <P ALIGN=CENTER><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("       </TH>");
            }
        }
        out.println("   </TR>");
        out.println("</THEAD>");

        out.println("<TBODY>");
    }

    public void DetailsType2Row(PDPM_Report_Types.PDPM_Details_List list)
    {
        out.println("   <TR VALIGN=TOP>");
        for(int i = 0; i < list.size(); i++)
        {
            if(i == 0)
            {
                // For first column only shading and left aligned
                out.println("       <TD NOWRAP BGCOLOR=\"#ffcc99\">");
                out.println("           <P><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("        </TD>");
            }
            /*
            else if(i == list.size() - 1)
            {
                out.println("       <TD NOWRAP valign=middle>");
                out.println("           <TABLE>");
                out.println("               <TR>");

                out.println("       <TD WIDTH=50>");
                out.println("           <P ALIGN=CENTER><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("        </TD>");

                String str_percent = list.get(list.size() - 1);
                PDPM_DEBUG.PDPM_REPORTER_LOG("str_percent: " + str_percent);
                Float float_percent;
                int percent = 1;
                try
                {
                    float_percent = Float.valueOf(str_percent);
                    float_percent = float_percent * BAR_CHART_MULTIPLIER;
                    percent = float_percent.intValue();
                }
                catch(Exception e)
                {
                }
                PDPM_DEBUG.PDPM_REPORTER_LOG("percent: " + percent);

                out.println("                   <TD bgcolor=darkred><IMG SRC='s.gif' width=" + percent + " height=4>");
                out.println("                   </TD>");
                out.println("               </TR>");
                out.println("           </TABLE>");
                out.println("        </TD>");
            }
            */
            else
            {
                out.println("       <TD>");
                out.println("           <P ALIGN=CENTER><FONT SIZE=2><FONT FACE=\"Arial\">" + list.get(i) + "</P>");
                out.println("       </TD>");
            }
        }
        // Column for chart
        // Get percentage number (It is assumed, the last column contains
        //   percentage
        String str_percent = list.get(list.size() - 1);
        PDPM_DEBUG.PDPM_REPORTER_LOG("str_percent: " + str_percent);
        Float float_percent;
        int percent = 1;
        try
        {
            float_percent = Float.valueOf(str_percent);
            float_percent = float_percent * BAR_CHART_MULTIPLIER;
            percent = float_percent.intValue();
        }
        catch(Exception e)
        {
        }
        PDPM_DEBUG.PDPM_REPORTER_LOG("percent: " + percent);

        out.println("       <TD valign=middle>");
        out.println("           <TABLE>");
        out.println("               <TR>");
        out.println("                   <TD bgcolor=darkred><IMG SRC='s.gif' width=" + percent + " height=4>");
        out.println("                   </TD>");
        out.println("               </TR>");
        out.println("           </TABLE>");
        out.println("        </TD>");

        out.println("   </TR>");
    }

    public void DetailsType2End(String note)
    {
        /*
        out.println("</TBODY>");
        out.println("</TABLE>");
        if(note != null)
        {
            out.println("<FONT FACE=\"Arial\"><FONT SIZE=1><I>" + note +
                   "</I></FONT>");
            out.println("<BR>");
        }

        out.println(
            "<FONT SIZE=2><FONT FACE=\"Arial\"><A HREF=\"#Go to Summary\">[Go to Summary]</A>");

        out.println("<BR>");
        */
        DetailsEnd(note);
    }
}
