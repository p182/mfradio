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

import java.util.Vector;
import javax.microedition.media.Player;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.iface.PlayerQueue;


/**
 * Queue for {@link Player} objects.
 *
 * @author  Roman Bondarenko
 */
public class PlayerQueueImpl 
    implements PlayerQueue {
    
    private Vector players_;
    private UIController controller_;
    
    /**
     * Creates a new instance of queue.
     * @param controller Application controller object.
     */
    public PlayerQueueImpl(UIController controller) {
        controller_ = controller;
        players_ = new Vector(3, 3);
    }
    
    // PlayerQueue implementation //////////////////////////////////////////////
    
    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.PlayerQueue#clear}
     * <p>Remove all Players from the queue.</p>
     *
     * @see PlayerQueue
     * @see PlayerQueue#clear
     */
    public void clear() {
        Player p = null;
        synchronized (players_) {
            //controller_.log("clear >> remove all players (size: "+players_.size()+")");
            while (!players_.isEmpty()) {
                p = (Player)players_.firstElement();
                players_.removeElement(p);
                p.close();
            }
            players_.notifyAll();
            //controller_.log("clear >> notify!");              
        }
    }

    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.PlayerQueue#pushTail}
     * <p>Append Player object to tail of the queue.</p>
     *
     * @param player Player object to place in the queue.
     * @throws ru.mobicomk.mfradio.util.PlayerQueueException if the given object 
     * already in queue.
     * @see PlayerQueue
     * @see PlayerQueue#pushTail
     */
    public void pushTail(Player player) throws PlayerQueueException {
        synchronized (players_) {
            if (players_.contains(player)) {
                throw new PlayerQueueException("Element already in queue.");
            }
            
            players_.addElement(player);
            //controller_.log("pushTail >> add element (size: "+players_.size()+")");
            
            if (players_.size() == 2) {
                //controller_.log("pushTail >> notify!");
                players_.notifyAll();
                controller_.queueIsNotEmpty();
            }
        }
    }
    
    /**
     * Implements method {@link ru.mobicomk.mfradio.iface.PlayerQueue#popHead}
     * <p>Remove Player object from head of the queue and return it to caller.</p>
     *
     * @throws ru.mobicomk.mfradio.util.PlayerQueueException if queue is empty 
     * after 1 minute of waiting.
     * @return Player object from head of the queue.
     * @see PlayerQueue
     * @see PlayerQueue#popHead
     */
    public Player popHead() throws PlayerQueueException {
        synchronized (players_) {
            if (players_.isEmpty()) {
                //controller_.log("popHead >> queue is empty. waiting...");
                controller_.queueIsEmpty();
                try {
                    players_.wait(60000); // 60 sec
                } catch (InterruptedException ex) {
                    //controller_.log("popHead >> InterruptedException >> " + ex.getMessage());
                    throw new PlayerQueueException(ex.getMessage());
                } 
                if (players_.isEmpty()) { // timeout
                    //controller_.log("popHead >> queue is empty again. throw exception!");
                    throw new PlayerQueueException(controller_.getLocale().getString(Constants.STR_Queue_is_empty));
                }
            }
            
            Object o = players_.firstElement();
            players_.removeElement(o);
            
            //controller_.log("popHead >> pop element (size: "+players_.size()+")");
            return (Player)o;
        }
    }
    
}
