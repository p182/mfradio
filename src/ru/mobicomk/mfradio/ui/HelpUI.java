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

package ru.mobicomk.mfradio.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.ImageItem;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.util.Locale;


/**
 * Form for help topics display.
 *
 * @author  Roman Bondarenko
 */
public class HelpUI 
    extends Form
    implements CommandListener {

    /** Help topic for <i>Player</i> form. */
    public static final int HELP_PLAYERUI_TOPIC = 1;
    
    /** Help topic for <i>Edit station information</i> form. */
    public static final int HELP_EDITSTATIONUI_TOPIC = 2;
    
    /** Help topic for <i>New station information</i> form. */
    public static final int HELP_ADDSTATIONUI_TOPIC = 3;
    
    /** Help topic for <i>WAP browser</i> form. */
    public static final int HELP_WAPBROWSERUI_TOPIC = 4;
    
    // Controller
    private UIController controller_;
    private Displayable nextDisplayable_;
        
    // Commands
    private Command okCommand_;

    // UI Items
    private String text_  = null;
    
    private static final int ICON_LINK                  = 0;
    private static final int ICON_NOT_LINK              = 1;
    private static final int ICON_UNKNOWN               = 2;

    private static final String[] iconPaths_ = { 
          "/i/doc.png"          // 0
        , "/i/doc3.png"    // 1
        , "/i/unknown.png"      // 2
    };
    private Image[] icons_ = new Image[iconPaths_.length];
    
    private Font bigFont_       = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
    private Font smallFont_     = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    private Font smallBoldFont_ = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

    // Ctor ////////////////////////////////////////////////////////////////////
    
    /**
     * Create new <i>Help</i> form object and init it.
     * @param controller Application controller object.
     */
    public HelpUI(UIController controller) {
        super("");
        
        controller_ = controller;
        
        initCommands();
        initUI();
    }

    /**
     * Second-level initialization. Must be called before each show form.
     * @param topic Help topic ID.
     * @param nextDisplayable Object which well be displayed after <i>Help</i> form.
     */
    public void init(int topic, Displayable nextDisplayable) {
        nextDisplayable_ = nextDisplayable;
        final Locale locale = controller_.getLocale();
        switch (topic) {
            case HELP_ADDSTATIONUI_TOPIC: 
                setTitle(locale.getString(Constants.STR_Edit_Station));
                text_ = locale.getString(Constants.STR_EditUI_Add_help_text);
                updateUI();
                break;
            case HELP_EDITSTATIONUI_TOPIC: 
                setTitle(locale.getString(Constants.STR_Edit_Station));
                text_ = locale.getString(Constants.STR_EditUI_Edit_help_text);
                updateUI();
                break;
            case HELP_PLAYERUI_TOPIC:
                setTitle(locale.getString(Constants.STR_Player));
                text_ = locale.getString(Constants.STR_PlayerUI_help_text);
                updateUI();
                break;
            case HELP_WAPBROWSERUI_TOPIC:
                setTitle(locale.getString(Constants.STR_Online_repository));
                text_ = locale.getString(Constants.STR_WAPUI_help_text);
                updateUI();
                break;
        }
    }
    
    // CommandListener interface ///////////////////////////////////////////////
    
    /** 
     * CommandListener interface implementation.
     * @see javax.microedition.lcdui.CommandListener 
     */
    public void commandAction(Command command, Displayable displayable) {
        if (command == okCommand_) {
            controller_.endShowHelp(nextDisplayable_);
        }
    }

    // Privates ////////////////////////////////////////////////////////////////

    private void initUI() {
        for (int i = 0; i < iconPaths_.length; i++) {
            try {
                icons_[i] = Image.createImage(iconPaths_[i]);
            } catch (IOException ioe) {
            }
        }
    }
    
    private void initCommands() {
        okCommand_ = new Command(controller_.getLocale().getString(Constants.STR_OK), Command.OK, 1);
        addCommand(okCommand_);
        setCommandListener(this);        
    }

    private void updateUI() {
        deleteAll();
        
        Vector items = null;
        Object o;
        items = makeItemsFromString(text_); 
        Enumeration e = items.elements();
        while (e.hasMoreElements()) {
            o = e.nextElement();
            if (null != o) {
                append((Item)o);
            }
        }
    }
    
    private Vector makeItemsFromString(String string) {
        Vector items = new Vector(5,5);
        String line = null;
        String token = null;
        Item item = null;
        int spaceIdx = 0;
        int slashIdx = 0;
        
        MultiLineStringParser parser = new MultiLineStringParser(string);
        
        while (!parser.endOfString()) {
            line = parser.nextLine();
            if (line.charAt(0) == '\\') {
                spaceIdx = line.indexOf(' ');
                token = line.substring(0, spaceIdx);
                if (token.equals("\\h1")) {
                    item = header1(line.substring(spaceIdx + 1));
                } else if (token.equals("\\h2")) {
                    item = header2(line.substring(spaceIdx + 1));
                } else if (token.equals("\\p")) {
                    item = normalText(line.substring(spaceIdx + 1));
                } else if (token.equals("\\i")) {
                    item = image(line.substring(spaceIdx + 1));
                }
            } else {
                item = normalText(line); // '\p' by default
            }
            items.addElement(item);
        }
        return items;
    }
    
    // TODO: to separate class (xxDrawer)
    private StringItem header1(String text) {
        if (text == null) {
            return null;
        }
        
        StringItem h = new StringItem(null, text);
        h.setFont(bigFont_);
        h.setLayout(Item.LAYOUT_2
            | Item.LAYOUT_CENTER
            | Item.LAYOUT_EXPAND
            | Item.LAYOUT_NEWLINE_AFTER 
            | Item.LAYOUT_NEWLINE_BEFORE
            );
        return h;
    }

    // TODO: to separate class (xxDrawer)
    private StringItem header2(String text) {
        StringItem h = new StringItem(text, null);
        h.setFont(smallBoldFont_);
        h.setLayout(Item.LAYOUT_2
            | Item.LAYOUT_LEFT
            | Item.LAYOUT_EXPAND
            | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_NEWLINE_BEFORE);
        return h;
    }
    
    // TODO: to separate class (xxDrawer)
    private StringItem normalText(String text) {
        StringItem t = new StringItem(null, text);
        t.setFont(smallFont_);
        t.setLayout(Item.LAYOUT_2
            | Item.LAYOUT_LEFT
            | Item.LAYOUT_EXPAND
            | Item.LAYOUT_NEWLINE_AFTER
            );
        return t;
    }
    
    // TODO: to separate class (xxDrawer)
    private ImageItem image(String text) {
        Image img = null;
        ImageItem imgItem = null;
        
        for (int idx = 0; idx<iconPaths_.length; idx++) {
            if (iconPaths_[idx].equals(text)) {
                img = icons_[idx];
                break;
            }
        }

        if (img != null) {
            imgItem = new ImageItem("\n", img
                , Item.LAYOUT_2 
                | Item.LAYOUT_LEFT 
                //| Item.LAYOUT_NEWLINE_BEFORE
                , "[image]");
        } 
        return imgItem;
    }

    /*
     *
     */
    private class MultiLineStringParser {
        private String string_;
        private int pos_;
        
        public MultiLineStringParser(String string) {
            pos_ = 0;
            string_ = string;
        }
        
        public boolean endOfString() {
            return (string_ == null) || (string_.length() == pos_);
        }
        
        public String nextLine() {
            String line = null;
            if (string_ != null) {
                int eol = string_.indexOf('\n', pos_ + 1);
                if (eol == -1) {
                    line = string_.substring(pos_);
                    pos_ = string_.length();
                } else {
                    line = string_.substring(pos_, eol);
                    pos_ = eol;
                    char ch = string_.charAt(pos_);
                    while ((ch == '\n' || ch == '\r') && (pos_ < (string_.length() - 1))) {
                        pos_++;
                        ch = string_.charAt(pos_);
                    }
                }
            }
            return line;
        }
    }
}
