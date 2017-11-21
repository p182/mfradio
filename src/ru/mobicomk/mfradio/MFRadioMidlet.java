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

package ru.mobicomk.mfradio;

import javax.microedition.midlet.*;

import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.model.Model;

/**
 * Application MIDlet class.
 * 
 * @author Roman Bondarenko
 * @see UIController
 */
public class MFRadioMidlet extends MIDlet {

    private UIController controller_;

    /**
     * Creates a new instance of MFRadioMidlet.
     */
    public MFRadioMidlet() {
    }

    /**
     * UI components initialization.
     */
    private void initialize() {
        controller_ = new UIController(this, new Model(this));
        controller_.startUI();
    }

    /**
     * This method should exit the midlet.
     */
    public void exitMIDlet() {
        // go(null);
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * Actively finish this MIDlet
     */
    public void exit() {
        destroyApp(false);
        notifyDestroyed();
    }

    // MIDlet interface ////////////////////////////////////////////////////////

    /**
     * Implements method {@link javax.microedition.midlet.MIDlet#startApp}
     * 
     * @see javax.microedition.midlet.MIDlet
     * @see javax.microedition.midlet.MIDlet#startApp
     */
    public void startApp() {
        if (controller_ == null) {
            initialize();
        } else {
            controller_.playerUIRequested();
        }
    }

    /**
     * Implements method {@link javax.microedition.midlet.MIDlet#pauseApp}
     * 
     * @see javax.microedition.midlet.MIDlet
     * @see javax.microedition.midlet.MIDlet#pauseApp
     */
    public void pauseApp() {
        controller_.pauseUI();
    }

    /**
     * Implements method {@link javax.microedition.midlet.MIDlet#destroyApp}
     * 
     * @see javax.microedition.midlet.MIDlet
     * @see javax.microedition.midlet.MIDlet#destroyApp
     */
    public void destroyApp(boolean unconditional) {
        controller_.stopUI();
    }
}
