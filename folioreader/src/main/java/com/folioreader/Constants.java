package com.folioreader;

import android.Manifest;

/**
 * Created by mobisys on 10/4/2016.
 */
public class Constants {
    public static final String PUBLICATION = "PUBLICATION";
    public static final String SELECTED_CHAPTER_POSITION = "selected_chapter_position";
    public static final String TYPE = "type";
    public static final String CHAPTER_SELECTED = "chapter_selected";
    public static final String BOOKMARK_SELECTED = "bookmark_selected";
    public static final String HIGHLIGHT_SELECTED = "highlight_selected";
    public static final String BOOK_TITLE = "book_title";

    public static final String LOCALHOST = "http://127.0.0.1";
    public static final int DEFAULT_PORT_NUMBER = 8080;
    public static final String STREAMER_URL_TEMPLATE = "%s:%d/%s/";
    public static final String DEFAULT_STREAMER_URL = LOCALHOST + ":" + DEFAULT_PORT_NUMBER + "/";

    public static final String SELECTED_WORD = "selected_word";
    public static final int FONT_NANUM_GOTHIC = 0;
    public static final int FONT_NANUM_MYEONGJO = 1;
    public static final int FONT_BON_GOTHIC = 2;
    public static final String DATE_FORMAT = "MMM dd, yyyy | HH:mm";
    public static final String ASSET = "file:///android_asset/";
    public static final int WRITE_EXTERNAL_STORAGE_REQUEST = 102;
    public static final String CHAPTER_ID = "id";
    public static final String HREF = "href";

    public static String[] getWriteExternalStoragePerms() {
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }
}
