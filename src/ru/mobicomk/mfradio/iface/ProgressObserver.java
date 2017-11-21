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

package ru.mobicomk.mfradio.iface;

/**
 * Long-time operation notification observer interface.
 * @author  Roman Bondarenko
 */
public interface ProgressObserver {
    
    /**
     * <i>Stoppable</i> flag accessor.
     * @return <b>true</b> if observer can be set to <i>Stopped</i> state 
     * by the user, <b>false</b> in othrwise.
     */
    public boolean isStoppable();
    
    /**
     * Set <i>Stoppable</i> flag for obeserver.
     * @param stoppable New <i>stoppable</i> flag value.
     */
    public void setStoppable(boolean stoppable);
    
    /**
     * Check observer status.
     * @return <b>true</b> if user set observer to <i>stopped</i> state, 
     * <b>false</b> othrwise.
     */
    public boolean isStopped();
    
    /**
     * Long-time operation progress was changes notification. 
     */
    public void updateProgress();
    
    /**
     * Set progress status message.
     * @param message New progress status message value.
     */
    public void setMessage(String message);
}


