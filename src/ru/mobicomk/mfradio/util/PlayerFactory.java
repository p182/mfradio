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
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;


/**
 * Player factory class.
 * <p>Create new {@link javax.microedition.media.Player} object to play back
 * media from {@link InputStream}.</p>
 *
 * @author  Roman Bondarenko
 */
public class PlayerFactory {
    
    /**
     * <p>Create {@link javax.microedition.media.Player} object to play back 
     * media from {@link InputStream} and assign listener to it.</p>
     *
     * @param is Input stream object which well be used for audio stream data read.
     * @param type Audio stream data type. If <b>null</b> is given, 
     * PlayerFactory will attempt to determine the type.
     * @param listener Player listener object.
     * @throws java.io.IOException if I/O error occurence.
     * @throws javax.microedition.media.MediaException if a Player cannot be 
     * created for the given stream and type.
     * @return Created Player.
     */
    public static Player createPlayer(InputStream is, String type, PlayerListener listener) 
        throws IOException, MediaException {
        
        Player p = Manager.createPlayer(is, type);
        p.addPlayerListener(listener);
        p.realize();
        p.prefetch();
        
        return p;
    }
    
}
