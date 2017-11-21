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

import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.ContentHandler;

/**
 * Content handler for M3U-files.
 * <p>Handler gets content of given resource through HTTP-request, then make
 * String from this content and interpret it as URL of audio stream and store
 * link in Player's playlist.</p>
 *
 * @author  Roman Bondarenko
 */
public class M3UContentHandler
    implements ContentHandler, Runnable {
    
    /**
     * Suffix of M3U file.
     */
    public static final String FILE_NAME_SUFFIX = ".m3u";
    
    private UIController controller_;
    private String url_ = "";
    private String name_ = "";
    
    /**
     * Creates a new instance of handler.
     * @param controller Application controller object.
     */
    public M3UContentHandler(UIController controller) {
        controller_ = controller;
    }
    
    // ContentHandler implementation ///////////////////////////////////////////
    
    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.ContentHandler#close}
     * <p>NOTE: Do nothing in current class.</p>
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#close
     */
    public void close() {
    }
    
    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.ContentHandler#canHandle}
     * @param url URL to check.
     * @return <b>true</b> if URL is supported, <b>false</b> otherwise.
     * <p>NOTE: current implementation supports URL, which ends with 
     * "<i>.m3u</i>" (case insensitive).</p>
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#canHandle
     */
    public boolean canHandle(String url) {
        return url.toLowerCase().endsWith(FILE_NAME_SUFFIX);
    }
    
    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.ContentHandler#handle}
     * <p>For detail see {@link M3UContentHandler}.
     *
     * @param name Text of link to handle.
     * @param url URL of link to handle.
     * @see M3UContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#handle
     */
    public void handle(String name, String url) {
        name_ = name;
        url_ = url;
        new Thread(this).start();
    }
    
    // Runnable implementation ///////////////////////////////////////////////// 
    
    /** 
     * Implements method {@link java.lang.Runnable#run}
     * <p>{@link #handle} starts this thread.</p>
     *
     * @see #handle
     * @see java.lang.Runnable
     * @see java.lang.Runnable#run
     */
    public void run() {
	Locale locale = controller_.getLocale();
        if (url_ == null || "".equals(url_)) {
            return;
        }
        
        String audioURL;
        try {
            audioURL = getFile(url_);
            if (isValidURL(audioURL)){
                controller_.addStation(new StationInfo(audioURL, name_));
                controller_.showInfo(name_ + locale.getString(Constants.STR_Saved_in_playlist), null);
                name_ = "";
                url_ = "";
            } else {
                controller_.showError(locale.getString(Constants.STR_Invalid_URL), null);
            }
        } catch (IOException ex) {
            controller_.showError(locale.getString(Constants.STR_Error_get_file) + ex.toString(), null);
        }
    }
    
    // Privates ////////////////////////////////////////////////////////////////
    
    private boolean isValidURL(String url) {
        return (url != null
            && !"".equals(url)
            && url.toLowerCase().startsWith("http://")
            && (-1 == url.indexOf("\n"))
            && (255 > url.length())
            );
    }
    
    private String getFile(String url) throws IOException {
        HttpConnection conn = null;
        InputStream is = null;
        StringBuffer sb = new StringBuffer();

        try {
            conn = (HttpConnection)Connector.open(url);
            conn.setRequestMethod(HttpConnection.GET);
            conn.setRequestProperty("User-Agent","Profile/MIDP-2.0 Confirguration/CLDC-1.1");
            //conn.setRequestProperty("Content-Language", "en-CA");
            is = conn.openDataInputStream();
            int ch;
            while ((ch = is.read()) != -1) {
                sb.append((char) ch);
            }
        } finally {
            if(is!= null) {
                is.close();
                is = null;
            }
            if(conn != null) {
                conn.close();
                conn = null;
            }
        }
        return sb.toString().trim();
    }
}