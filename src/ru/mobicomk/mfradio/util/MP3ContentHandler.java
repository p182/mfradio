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

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.ContentHandler;


/**
 * Content handler for MP3-files.
 * <p>Handler check and store given link in Player's playlist.</p>
 *
 * @author  Roman Bondarenko
 */
public class MP3ContentHandler 
    implements ContentHandler {
   
    /**
     * Suffix of MP3 file.
     */
    public static final String FILE_NAME_SUFFIX = ".mp3";
    
    private UIController controller_;
    private String url_ = "";
    private String name_ = "";
    
    /**
     * Creates a new instance of handler.
     * @param controller Application controller object.
     */
    public MP3ContentHandler(UIController controller) {
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
     * "<i>.mp3</i>" (case insensitive).</p>
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#canHandle
     */
    public boolean canHandle(String url) {
        return url.toLowerCase().endsWith(FILE_NAME_SUFFIX);
    }
    
    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.ContentHandler#handle}
     * <p>For detail see {@link MP3ContentHandler}.
     *
     * @param name Text of link to handle.
     * @param url URL of link to handle.
     * @see MP3ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler
     * @see ru.mobicomk.mfradio.iface.ContentHandler#handle
     */
    public void handle(String name, String url) {
	final Locale locale = controller_.getLocale();
        if (isValidURL(url)){
            controller_.addStation(new StationInfo(url, name));
            controller_.showInfo(name + " " + locale.getString(Constants.STR_Saved_in_playlist), null);
        } else {
            controller_.showError(locale.getString(Constants.STR_Invalid_URL), null);
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
}