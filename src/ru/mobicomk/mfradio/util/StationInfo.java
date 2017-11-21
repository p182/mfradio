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

/**
 * Interne-radio station information. Containts title of the station and 
 * station's URL.
 * @author  Roman Bondarenko
 */
public class StationInfo {
    // TODO: hide Url and Title and add getters (RO access).
    
    /** URL of the station. */
    public String Url;
    
    /** Title of the station. */
    public String Title;
    
    /**
     * Creates a new instance of station info and fill it initial
     * @param url URL of the station.
     * @param title Title of the station.
     */
    public StationInfo(String url, String title) {
        this.Url = url;
        this.Title = title;
    }
}
