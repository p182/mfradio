/*
 *    MFRadio - stream radio client for Java 2 Micro Edition
 *    Copyright (C) 2001 - 2007 Mobicom-Kavkaz, Inc
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

package ru.mobicomk.mfradio.iface;

/**
 * URL content handler interface.
 * @author  Roman Bondarenko
 */
public interface ContentHandler {
    
    /** 
     * Close handler. 
     */
    public void close();
    
    /**
     * Check if handler can handle given URL.
     * @param url URL for check.
     * @return <b>true</b> if handler can handle URL, <b>false</b> othrwise.
     */
    public boolean canHandle(String url);
    
    /**
     * Handle URL.
     * @param title Link title.
     * @param url Link URL.
     */
    public void handle(String title, String url);
}

