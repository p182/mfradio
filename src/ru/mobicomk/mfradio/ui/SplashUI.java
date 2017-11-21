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

package ru.mobicomk.mfradio.ui;

import java.io.IOException;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.iface.ProgressObserver;

/**
 * Splash screen form. This form displays during application start.
 * @author  Roman Bondarenko
 */
public class SplashUI 
    extends Canvas
    implements ProgressObserver {
    
    private Image screen_;
    private String message_;
    
    private Font font_ = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    
    private int messageLeft_;
    private int messageBottom_;
    private int maxMessageWidth_;

    /*
    private int progressStep_;
    private int progressHeight_;
    private int progressLeft_;
    private int progressTop_;
     **/

    private static final int PROGRESS_MAX = 10;
    
    /** 
     * Creates a new instance of <i>Splash</i> form.
     */
    public SplashUI() {
        super();
    }
    
    /**
     * Second-level initialization. 
     * @param message Status message.
     */
    public void init(String message) {
        setMessage(message);
    }

    // ProgressObserver implementation /////////////////////////////////////////
    
    /**
     * Get <i>stoppable</i> flag value.
     * @return <b>true</b> if <i>stoppable</i> flag is set, <b>false</b> othrwise.
     * @see ru.mobicomk.mfradio.ui.ProgressBarUI
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#isStoppable
     */
    public boolean isStoppable() {
        return false;
    }

    /**
     * Set <i>stoppable</i> flag to new state. (NOTE: do nothing in this case).
     * @param stoppable New value for <i>stoppable</i> flag.
     * @see ru.mobicomk.mfradio.ui.ProgressBarUI
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#setStoppable
     */
    public void setStoppable(boolean stoppable) {
        // do nothing
    }

    /**
     * Check if user select <i>Stop</i> command.
     * @return <b>true</b> if user selects a <i>Stop</i> command, 
     * <b>false</b> in other case.
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#isStopped
     */
    public boolean isStopped() {
        return false;
    }

    /**
     * This method called for update long-time operation progress.
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#updateProgress
     */
    public void updateProgress() {
    }

    /**
     * Set new progress bar status message.
     * @param message New status message value.
     * @see ru.mobicomk.mfradio.iface.ProgressObserver
     * @see ru.mobicomk.mfradio.iface.ProgressObserver#setMessage
     */
    public void setMessage(String message) {
        message_ = message;
        repaint();
    }

    // Canvas implementation ///////////////////////////////////////////////////
    
    /*
     * {@link Canvas} interface implementaion.
     * @see javax.microedition.lcdui.Canvas
     * @see javax.microedition.lcdui.Canvas#paint
     */
    protected void paint(Graphics graphics) {
        if (screen_ == null) {
            buildScreen(graphics); // prepare not mutable parts
            calcMessageCoords(0, 0, screen_.getWidth(), screen_.getHeight());
        }
        
        // draw not mutable parts
        graphics.drawImage(screen_, 0, 0, Graphics.TOP | Graphics.LEFT);        
        
        // draw mutable parts
        paintMessage(graphics, 0, 0, screen_.getWidth(), screen_.getHeight());
        
        // progress
        //graphics.setColor(Constants.COLOR_GRAY);
        //graphics.fillRect(progressLeft_, progressTop_, progressStep_ * progress_, progressHeight_);
    }

    // Privates ////////////////////////////////////////////////////////////////
    
    private void calcMessageCoords(int x, int y, int width, int height) {
        int x0 = x + Constants.UI_ITEM_MARGIN;
        int y0 = y + height - Constants.UI_ITEM_MARGIN;
        
        messageLeft_ = x0 + Constants.UI_ITEM_SPAN;
        messageBottom_ = y0;
        maxMessageWidth_ = width - ((Constants.UI_ITEM_MARGIN + Constants.UI_ITEM_SPAN) * 2);
    }

    private void buildScreen(Graphics graphics) {
        screen_ = Image.createImage(graphics.getClipWidth(), graphics.getClipHeight());

        int width = screen_.getWidth();
        int height = screen_.getHeight();
        Graphics g = screen_.getGraphics();

        // background
        g.setColor(Constants.COLOR_WHITE);
        g.fillRect(0, 0, width, height);
        
        // logo
        Image imgLogo = null;
        try {
            g.drawImage(Image.createImage("/i/mflogo.png"), width/2, height/2, Graphics.HCENTER | Graphics.VCENTER);
        } catch (IOException ex) { }
        
        // progress
        /*
        progressHeight_ = 2;
        progressLeft_ = Constants.UI_ITEM_MARGIN;
        progressTop_ = height - 2 - progressHeight_;
        progressStep_ = (width - (Constants.UI_ITEM_MARGIN * 2)) / PROGRESS_MAX;
        
        g.setColor(Constants.COLOR_GRAY);
        g.fillRect(progressLeft_, progressTop_, width - (Constants.UI_ITEM_MARGIN * 2) , progressHeight_);
         **/
    }

    private void paintMessage(Graphics g, int x, int y, int width, int height) {
        char[] chars = message_.toCharArray();
        int length = message_.length();

        while (font_.charsWidth(chars, 0, length) > maxMessageWidth_) {
           length--;
        }

        g.setColor(Constants.COLOR_BLACK);
        g.setFont(font_);
        g.drawSubstring(message_, 0, length, messageLeft_, messageBottom_, Graphics.LEFT | Graphics.BOTTOM);
    }
}


