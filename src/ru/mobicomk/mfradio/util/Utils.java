/*
 *    Copyright (C) 2001 - 2007 Mobicom-Kavkaz, Inc
 *    MFRadio - stream radio client for Java 2 Micro Edition
 *    
 *    Visit the project page at: http://mfradio.sourceforge.net
 *
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *    Java (TM) and all Java (TM)-based marks are a trademark or 
 *    registered trademark of Sun Microsystems, Inc, in the United States 
 *    and other countries.
 */

package ru.mobicomk.mfradio.util;

import javax.microedition.lcdui.Command;
import javax.microedition.media.MediaException;

/**
 * Utility functions for simple WAP browser.
 * @author  Roman Bondarenko
 */
public class Utils {

    // "parameters" passed to commandAction method
    Command commandOccured;
    
    // hide contructor from outside.
    private Utils() {
    }
    
    /**
     * Splits the URL in the parts.
     *
     * <p>E.g: <code>http://www.site.com:8080/streams/MP3/file.mp3#1</code>
     * <pre>
     * 0: protocol  (e.g. http)
     * 1: host      (e.g. www.site.com)
     * 2: port      (e.g. 8080)
     * 3: path      (e.g. /streams/MP3)
     * 4: file      (e.g. file.mp3)
     * 5: anchor    (e.g. 1)
     * </pre>
     * </p>
     * <p>Using:
     * <code><pre>
     * // Split URL to Site and File parts
     * String[] urlParts = Utils.splitURL(url);
     * String site = urlParts[1] + ":" + urlParts[2]; // host:port
     * urlParts[0] = "";
     * urlParts[1] = "";
     * urlParts[2] = "";
     * String file = Utils.mergeURL(urlParts);
     * </pre></code>
     * </p>
     * <p><b>LIMITATION:</b> URL must end with a slash if it is a directory.</p>
     * @param url URL to split.
     * @throws ru.mobicomk.mfradio.util.UrlFormatException if parsing error occuring.
     * @return String array.
     */
    public static String[] splitURL(String url) throws UrlFormatException {
        StringBuffer u=new StringBuffer(url);
        String[] result=new String[6];
        for (int i=0; i<=5; i++) {
            result[i]="";
        }
        // get protocol
        boolean protFound=false;
        int index=url.indexOf(":");
        if (index>0) {
            result[0]=url.substring(0, index);
            u.delete(0, index+1);
            protFound=true;
        } else if (index==0) {
            throw new UrlFormatException("protocol");
        }
        // check for host/port
        if (u.length()>2 && u.charAt(0)=='/' && u.charAt(1)=='/') {
            // found domain part
            u.delete(0, 2);
            int slash=u.toString().indexOf('/');
            if (slash<0) {
                slash=u.length();
            }
            int colon=u.toString().indexOf(':');
            int endIndex=slash;
            if (colon>=0) {
                if (colon>slash) {
                    throw new UrlFormatException("port");
                }
                endIndex=colon;
                result[2]=u.toString().substring(colon+1, slash);
            }
            result[1]=u.toString().substring(0, endIndex);
            u.delete(0, slash);
        }
        // get filename
        if (u.length()>0) {
            url=u.toString();
            int slash=url.lastIndexOf('/');
            if (slash>0) {
                result[3]=url.substring(0, slash);
            }
            if (slash<url.length()-1) {
                String fn = url.substring(slash+1, url.length());
                int anchorIndex = fn.indexOf("#");
                if (anchorIndex>=0) {
                    result[4] = fn.substring(0, anchorIndex);
                    result[5] = fn.substring(anchorIndex+1);
                } else {
                    result[4] = fn;
                }
            }
        }
        return result;
    }
    
    /**
     * Make URL from URL parts. For array structure see {@link #splitURL}.
     * <p>Using:
     * <code><pre>
     * // Split URL to Site and File parts
     * String[] urlParts = Utils.splitURL(url);
     * String site = urlParts[1] + ":" + urlParts[2]; // host:port
     * urlParts[0] = "";
     * urlParts[1] = "";
     * urlParts[2] = "";
     * String file = Utils.mergeURL(urlParts);
     * </pre></code>
     * </p>
     * @param url URL parts.
     * @return Whole URL.
     * @see #splitURL
     */
    public static String mergeURL(String[] url) {
        return ((url[0]=="")?"":url[0]+":/")
        +((url[1]=="")?"":"/"+url[1])
        +((url[2]=="")?"":":"+url[2])
        +url[3]+"/"+url[4]
            +((url[5]=="")?"":"#"+url[5]);
    }
    
    
    /**
     * Make exception information more friendly.
     * <p>Example:
     * <pre>
     * String exeptInfo = friendlyException("javax.microedition.media.MediaException: Some text");
     * // Now exeptInfo is "MediaException: Some text"
     * </pre>
     * </p>
     * @param t Exception object.
     * @return "Friendly" exception string.
     */
    public static String friendlyException(Throwable t) {
        if (t instanceof MediaException && t.getMessage().indexOf(" ")>5) {
            return t.getMessage();
        }
        String s = t.toString();
        while (true) {
            int dot = s.indexOf(".");
            int space = s.indexOf(" "); if (space<0) space = s.length();
            int colon = s.indexOf(":"); if (colon<0) colon = s.length();
            if (dot >= 0 && dot < space && dot < colon) {
                s = s.substring(dot+1);
            } else {
                break;
            }
        }
        return s;
    }
}
