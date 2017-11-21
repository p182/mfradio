/*
 * 
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

package ru.mobicomk.mfradio;

/**
 * Application constans.
 * 
 * <p>
 * This class containts:
 * </p>
 * <ul>
 * <li> constants for all texts, messages, labels and titles </li>
 * <li> all color constant and elements size for rendes GUI </li>
 * <li> application property file keys </li>
 * </ul>
 * 
 * @author Roman Bondarenko, Alxey Rybalko
 */
public class Constants {
    public static final int COLOR_WHITE = 0xffffff;

    public static final int COLOR_YELLOW = 0xdddd00;

    public static final int COLOR_GRAY = 0xcccccc;

    public static final int COLOR_DARKGRAY = 0x999999;

    public static final int COLOR_BLACK = 0x000000;

    public static final int COLOR_RED = 0xff6d00;

    public static final int COLOR_GREEN = 0x006d55;

    public static final int COLOR_BLUE = 0x0000ff;

    public static final int COLOR_BURGUNDY = 0xb60055;

    public static final int COLOR_MF_BLUE = 0x0468B6;

    public static final int COLOR_MF_GREEN = 0x468F61;

    public static final int COLOR_BACKGROUND = COLOR_WHITE;

    public static final int COLOR_LIST_BACKGROUND = COLOR_MF_BLUE;

    public static final int COLOR_LIST_TEXT = COLOR_WHITE;

    public static final int COLOR_SELECTED = COLOR_MF_GREEN;

    public static final int COLOR_SELECTED_TEXT = COLOR_WHITE;

    public static final int UI_ITEM_CORNER = 10;

    public static final int UI_ITEM_MARGIN = 5;

    public static final int UI_ITEM_SPAN = 2;

    public static final String APP_RMS_PLAYLIST = "MFRadioPlayList";

    public static final String APP_RMS_SETTINGS = "MFRadioSettings";

    public static final String APP_REPOSITORY_URL_KEY_PREFIX = "RepositoryURL-";

    public static final String APP_RADIO_URL_KEY_PREFIX = "RadioURL-";

    public static final String APP_RADIO_TITLE_KEY_PREFIX = "RadioTitle-";

    public static final String APP_LOCALE_KEY = "Locale";

    public static final String APP_DEFAULT_LOCALE = "en";

    public static final String STR_Player = "Player";

    public static final String STR_Edit_Station = "Edit_Station";

    public static final String STR_Add_Station = "Add_Station";

    public static final String STR_Repository = "Repository";

    public static final String STR_Playlist = "Playlist";

    public static final String STR_Volume = "Volume";

    public static final String STR_Play = "Play";

    public static final String STR_Stop = "Stop";

    public static final String STR_Exit = "Exit";

    public static final String STR_Edit = "Edit";

    public static final String STR_Add = "Add";

    public static final String STR_Delete = "Delete";

    public static final String STR_Title = "Title";

    public static final String STR_Link = "Link";

    public static final String STR_OK = "OK";

    public static final String STR_Cancel = "Cancel";

    public static final String STR_Back = "Back";

    public static final String STR_Select = "Select";

    public static final String STR_Goto_online = "Goto_online";

    public static final String STR_Exit_Browser = "Exit_Browser";

    public static final String STR_Open = "Open";

    public static final String STR_Save_in_playlist = "Save_in_playlist";

    public static final String STR_Menu = "Menu";

    public static final String STR_Refresh = "Refresh";

    public static final String STR_Online_repository = "Online_repository";

    public static final String STR_Connecting = "Connecting";

    public static final String STR_Processing = "Processing";

    public static final String STR_Prefetching = "Prefetching";

    public static final String STR_Buffer = "Buffer";

    public static final String STR_Speed = "Speed";

    public static final String STR_Prefetched = "Prefetched";

    public static final String STR_Loading = "Loading";

    public static final String STR_Saving = "Saving";

    public static final String STR_Stop_playing = "Stop_playing";

    public static final String STR_Initialize = "Initialize";

    public static final String STR_Reconnecting = "Reconnecting";

    public static final String STR_Clearing = "Clearing";

    public static final String STR_Rendering = "Rendering";

    public static final String STR_Reading = "Reading";

    public static final String STR_Warning = "Warning";

    public static final String STR_Error = "Error";

    public static final String STR_Info = "Info";

    public static final String STR_Cant_connect = "Cant_connect";

    public static final String STR_Error_start_player_for = "Error_start_player_for";

    public static final String STR_Operation_interrupted = "Operation_interrupted";

    public static final String STR_Station_in_playlist = "Station_in_playlist";

    public static final String STR_Must_be_stopped = "Must_be_stopped";

    public static final String STR_Must_be_inited = "Must_be_inited";

    public static final String STR_Queue_is_empty = "Queue_is_empty";

    public static final String STR_Saved_in_playlist = "Saved_in_playlist";

    public static final String STR_Invalid_URL = "Invalid_URL";

    public static final String STR_Error_get_file = "Error_get_file";

    public static final String STR_URL_format_error = "URL_format_error";

    public static final String STR_Protocol = "Protocol";

    public static final String STR_Port = "Port";

    public static final String STR_Fill_URL_field = "Fill_URL_field";

    public static final String STR_Fill_TITLE_field = "Fill_TITLE_field";

    public static final String STR_No_links_found = "No_links_found";

    public static final String STR_Incorrect_data_format = "Incorrect_data_format";

    public static final String STR_Unterminated_string = "Unterminated_string";

    public static final String STR_Try_again = "Try_again";

    public static final String STR_Invalid_agument = "Invalid_agument";

    public static final String STR_Help = "Help";

    public static final String STR_Unsupported_content_type_BEGIN = "Unsupported_content_type_BEGIN";

    public static final String STR_Unsupported_content_type_END = "Unsupported_content_type_END";

    public static final String STR_HTTP_response_code = "HTTP_response_code";

    public static final String STR_PlayerUI_help_text = "PlayerUI_help_text";

    public static final String STR_EditUI_Add_help_text = "EditUI_Add_help_text";

    public static final String STR_EditUI_Edit_help_text = "EditUI_Edit_help_text";

    public static final String STR_WAPUI_help_text = "STR_WAPUI_help_text";

    public static final String STR_Lang = "Lang";
}
