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

import javax.microedition.media.Player;

import ru.mobicomk.mfradio.util.PlayerQueueException;

/**
 * Player queue interface.
 * @author  Roman Bondarenko
 */
public interface PlayerQueue {
    
    /**
     * Add player to tail of the queue.
     * @param p Player object.
     * @throws ru.mobicomk.mfradio.util.PlayerQueueException 
     */
    public void pushTail(Player p) throws PlayerQueueException;
    
    /**
     * Remove player from head of the queue.
     * @throws ru.mobicomk.mfradio.util.PlayerQueueException 
     * @return Removed player object.
     */
    public Player popHead() throws PlayerQueueException;
    
    /** 
     * Remove all player objects from the queue. 
     */
    public void clear();
}
