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

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import ru.mobicomk.mfradio.Constants;
import ru.mobicomk.mfradio.controller.UIController;
import ru.mobicomk.mfradio.util.Locale;
import ru.mobicomk.mfradio.util.StationInfo;

/**
 * Edit station information form class. Used also for new station 
 * information input.
 *
 * @author  Roman Bondarenko
 */
public class EditStationUI 
    extends Form
    implements CommandListener, ItemCommandListener {
    
    private static final int UNDEFINED_MODE = 0;
    private static final int ADD_MODE = 1;
    private static final int EDIT_MODE = 2;
    
    // Controller
    private UIController controller_;
    private int mode_;
        
    // Commands
    private Command okCommand_;
    private Command cancelCommand_;
    private Command helpCommand_;
    private Command toOnline_;    

    // UI Items
    private TextField titleField_;
    private TextField urlField_;

    private StringItem repoLink_;
    private int repoLinkIdx_;
    

    // Constructor /////////////////////////////////////////////////////////////
    
    /**
     * Create new edit station information form.
     * @param controller Application controller object.
     */
    public EditStationUI(UIController controller) {
        super("");
        
        controller_ = controller;
        mode_ = UNDEFINED_MODE;
        repoLinkIdx_ = -1;
        
        initCommands();
        initUI();
    }
    
    /**
     * Second level initialization. Used before show this form for setting up 
     * station information to edit.
     * @param si Station information object. If it is <b>null</b>, then form 
     * interacts as <i>New station</i> form, <i>Edit station</i> othrwise.
     */
    public void init(StationInfo si) {
	Locale locale = controller_.getLocale();
        if (si == null) {
            mode_ = ADD_MODE;
            setTitle(locale.getString(Constants.STR_Add_Station));
            titleField_.setString("");
            urlField_.setString("");
            setOnlineRepository();
        } else {
            mode_ = EDIT_MODE;
            setTitle(locale.getString(Constants.STR_Edit_Station));
            titleField_.setString(si.Title);
            urlField_.setString(si.Url);
            unsetOnlineRepository();
        }
    }
    
    // ItemCommandListener interface ///////////////////////////////////////////
    
    /** 
     * ItemCommandListener interface implementation.
     * @see javax.microedition.lcdui.ItemCommandListener 
     */
    public void commandAction(Command command, Item item) {
        if ((item == getRepositoryLink()) && (command == toOnline_)) {
            controller_.repositoryRequested();
        }
    }
    
    // CommandListener interface ///////////////////////////////////////////////
    
    /** 
     * CommandListener interface implementation.
     * @see javax.microedition.lcdui.CommandListener 
     */
    public void commandAction(Command command, Displayable displayable) {
        if (mode_ == EDIT_MODE) {
            editMode_commandAction(command, displayable);
        } else if (mode_ == ADD_MODE) {
            addMode_commandAction(command, displayable);
        }
    }
    
    private void editMode_commandAction(Command command, Displayable displayable) {
	final Locale locale = controller_.getLocale();
        if (command == okCommand_) {
            // check for empty fields
            if (urlField_.getString().length() == 0) {
                controller_.showWarning(locale.getString(Constants.STR_Fill_URL_field), this);
            } else if (titleField_.getString().length() == 0) {
                controller_.showWarning(locale.getString(Constants.STR_Fill_TITLE_field), this);
            } else {
                controller_.setStation(controller_.getSelectedStationIdx()
                    , new StationInfo(urlField_.getString(), titleField_.getString()));
                controller_.endEditStationRequested();
            }
        } else if (command == cancelCommand_) {
            controller_.endEditStationRequested();
        } else if (command == helpCommand_) {
            controller_.showHelp(HelpUI.HELP_EDITSTATIONUI_TOPIC); // TODO: remove HelpUI import
        }
    }
  
    private void addMode_commandAction(Command command, Displayable displayable) {
	final Locale locale = controller_.getLocale();
        if (command == okCommand_) {
            if (urlField_.getString().length() == 0) {
                controller_.showWarning(locale.getString(Constants.STR_Fill_URL_field), this);
            } else if (titleField_.getString().length() == 0) {
                controller_.showWarning(locale.getString(Constants.STR_Fill_TITLE_field), this);
            } else {
                controller_.addStation(new StationInfo(urlField_.getString(), titleField_.getString()));
                controller_.endAddStationRequested();
            }
        } else if (command == cancelCommand_) {
            controller_.endAddStationRequested();
        } else if (command == helpCommand_) {
            controller_.showHelp(HelpUI.HELP_ADDSTATIONUI_TOPIC);  // TODO: remove HelpUI import
        }
    }

    // Privates ////////////////////////////////////////////////////////////////
    
    private void initUI() {
	final Locale locale = controller_.getLocale();
        // Title
        titleField_ = new TextField(locale.getString(Constants.STR_Title), "", 120, TextField.ANY);
        append(titleField_);
        
        // Link
        urlField_ = new TextField(locale.getString(Constants.STR_Link), "", 120, TextField.URL);
        append(urlField_);
    }

    private void initCommands() {
	final Locale locale = controller_.getLocale();
        // to online
        toOnline_ = new Command(locale.getString(Constants.STR_Goto_online), Command.ITEM, 1);
        
        // OK
        okCommand_ = new Command(locale.getString(Constants.STR_OK), Command.OK, 1);
        addCommand(okCommand_);

        // Cancel
        cancelCommand_ = new Command(locale.getString(Constants.STR_Cancel), Command.CANCEL, 2);
        addCommand(cancelCommand_);
        
        // Help
        helpCommand_ = new Command(locale.getString(Constants.STR_Help), Command.SCREEN, 2);
        addCommand(helpCommand_);
        
        setCommandListener(this);        
    }

    private void setOnlineRepository() {
        if (repoLinkIdx_ == -1) {
            repoLinkIdx_ = append(getRepositoryLink());
        }
    }
    
    private void unsetOnlineRepository() {
        if (repoLinkIdx_ != -1) {
            delete(repoLinkIdx_);
            repoLinkIdx_ = -1;
        }
    }

    private StringItem getRepositoryLink() {
	        if (repoLink_ == null) {
            repoLink_ = new StringItem("", controller_.getLocale().getString(Constants.STR_Online_repository), Item.HYPERLINK);
            repoLink_.setDefaultCommand(toOnline_);
            repoLink_.setItemCommandListener(this);
        }
        return repoLink_;
    }
    
}
