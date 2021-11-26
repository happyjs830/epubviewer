package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.folioreader.Constants;
import com.folioreader.model.BookMark;
import com.folioreader.model.BookmarkImpl;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class BookmarkTable {
    public static final String TABLE_NAME = "bookmark_table";

    public static final String ID = "_id";
    public static final String COL_BOOK_ID = "bookId";
    private static final String COL_CONTENT = "content";
    private static final String COL_DATE = "date";
    private static final String COL_DATE_TS = "date_ts";
    private static final String COL_PAGE_NUMBER = "page_number";
    private static final String COL_PAGE_ID = "pageId";
    private static final String COL_RANGY = "rangy";
    private static final String COL_NOTE = "note";
    private static final String COL_UUID = "uuid";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_DATE_TS + " TEXT" + ","
            + COL_PAGE_NUMBER + " INTEGER" + ","
            + COL_PAGE_ID + " TEXT" + ","
            + COL_RANGY + " TEXT" + ","
            + COL_UUID + " TEXT" + ","
            + COL_NOTE + " TEXT" + ")";

    public static final String TAG = BookmarkTable.class.getSimpleName();

    public static ContentValues getBookmarkContentValues(BookMark bookmark) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_BOOK_ID, bookmark.getBookId());
        contentValues.put(COL_CONTENT, bookmark.getContent());
        contentValues.put(COL_DATE, getDateTimeString(bookmark.getDate()));
        contentValues.put(COL_DATE_TS, bookmark.getTimeStamp());
        contentValues.put(COL_PAGE_NUMBER, bookmark.getPageNumber());
        contentValues.put(COL_PAGE_ID, bookmark.getPageId());
        contentValues.put(COL_RANGY, bookmark.getRangy());
        contentValues.put(COL_NOTE, bookmark.getNote());
        contentValues.put(COL_UUID, bookmark.getUUID());
        return contentValues;
    }

    public static ArrayList<BookmarkImpl> getAllBookmarks(String bookId) {
        ArrayList<BookmarkImpl> bookmarks = new ArrayList<>();
        Cursor bookmarkCursor = DbAdapter.getBookMarksForBookId(bookId);
        while (bookmarkCursor.moveToNext()) {
            bookmarks.add(new BookmarkImpl(bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(ID)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_BOOK_ID)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_DATE))),
                    bookmarkCursor.getLong(bookmarkCursor.getColumnIndex(COL_DATE_TS)),
                    bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_PAGE_ID)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_RANGY)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_NOTE)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_UUID))));
        }
        return bookmarks;
    }

    public static BookmarkImpl getBookmarkId(int id) {
        Cursor bookmarkCursor = DbAdapter.getBookmarksForId(id);
        BookmarkImpl bookmarkImpl = new BookmarkImpl();
        while (bookmarkCursor.moveToNext()) {
            bookmarkImpl = new BookmarkImpl(bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(ID)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_BOOK_ID)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_CONTENT)),
                    getDateTime(bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_DATE))),
                    bookmarkCursor.getLong(bookmarkCursor.getColumnIndex(COL_DATE_TS)),
                    bookmarkCursor.getInt(bookmarkCursor.getColumnIndex(COL_PAGE_NUMBER)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_PAGE_ID)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_RANGY)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_NOTE)),
                    bookmarkCursor.getString(bookmarkCursor.getColumnIndex(COL_UUID)));
        }
        return bookmarkImpl;
    }

    public static long insertBookmark(BookmarkImpl bookmarkImpl) {
        Log.e(TABLE_NAME, "highlightImpl ::::: " + bookmarkImpl.toString());
        bookmarkImpl.setUUID(UUID.randomUUID().toString());
        return DbAdapter.saveBookMark(getBookmarkContentValues(bookmarkImpl));
    }

    public static boolean deleteBookmark(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        int id = DbAdapter.getIdForQuery(query);
        return id != -1 && deleteBookmark(id);
    }

    public static boolean deleteBookmark(int bookmarkId) {
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(bookmarkId));
    }

    public static List<String> getBookmarksForPageId(String pageId) {
        String query = "SELECT " + COL_RANGY + " FROM " + TABLE_NAME + " WHERE " + COL_PAGE_ID + " = \"" + pageId + "\"";
        Cursor c = DbAdapter.getBookmarksForPageId(query, pageId);
        List<String> rangyList = new ArrayList<>();
        while (c.moveToNext()) {
            rangyList.add(c.getString(c.getColumnIndex(COL_RANGY)));
        }
        c.close();
        return rangyList;
    }

    public static boolean updateBookmark(BookmarkImpl bookmarkImpl) {
        return DbAdapter.updateHighLight(getBookmarkContentValues(bookmarkImpl), String.valueOf(bookmarkImpl.getId()));
    }

    public static String getDateTimeString(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.DATE_FORMAT, Locale.getDefault());
        return dateFormat.format(date);
    }

    public static Date getDateTime(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                Constants.DATE_FORMAT, Locale.getDefault());
        Date date1 = new Date();
        try {
            date1 = dateFormat.parse(date);
        } catch (ParseException e) {
            Log.e(TAG, "Date parsing failed", e);
        }
        return date1;
    }

    public static BookmarkImpl getBookmarkForRangy(String rangy) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_RANGY + " = \"" + rangy + "\"";
        return getBookmarkId(DbAdapter.getIdForQuery(query));
    }

    public static void saveBookmarkIfNotExists(BookMark bookmark) {
        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + COL_UUID + " = \"" + bookmark.getUUID() + "\"";
        Log.e(TABLE_NAME, "saveBookmarkIfNotExists : " + query);
        int id = DbAdapter.getIdForQuery(query);
        if (id == -1) {
            DbAdapter.saveHighLight(getBookmarkContentValues(bookmark));
        }
    }
}
