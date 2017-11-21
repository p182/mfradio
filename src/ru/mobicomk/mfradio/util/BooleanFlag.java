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
 * Boolean flag with synchronized access.
 *
 * <p>This class provides synchronized access to boolean value in 
 * multi-threaded application.</p>
 *
 * @author  Roman Bondarenko
 */
public class BooleanFlag {
    
    private Object flagSync_;
    private boolean flag_;
    
    /**
     * Creates and initialize a new instance of BooleanFlag.
     * @param value Initial flag value.
     */
    public BooleanFlag(boolean value) {
        flagSync_ = new Object();
        flag_ = value;
    }
    
    /**
     * Set flag (flag value after this call is <b>true</b>).
     */
    public void set() {
        synchronized (flagSync_) {
            flag_ = true;
        }
    }

    /**
     * Reset flag (flag value after this call is <b>false</b>).
     */
    public void reset() {
        synchronized (flagSync_) {
            flag_ = false;
        }
    }

    /**
     * Get flag value.
     * @return Current flag value.
     */
    public boolean value() {
        synchronized (flagSync_) {
            return flag_;
        }
    }
    
}

