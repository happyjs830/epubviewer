package com.folioreader.model.sqlite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.fasterxml.jackson.databind.cfg.ContextAttributes;
import com.folioreader.Constants;
import com.folioreader.model.BookMark;
import com.folioreader.model.BookmarkImpl;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;

import org.readium.r2.shared.Link;
import org.readium.r2.shared.Publication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author syed afshan on 24/12/20.
 */

public class BookmarkTable {

    public static final String TABLE_NAME = "bookmark_table";

    public static final String ID = "_id";
    public static final String COL_BOOK_ID = "bookId";
    private static final String COL_CONTENT = "content";
    private static final String COL_DATE = "date";
    private static final String COL_DATE_TS = "date_ts";
    private static final String COL_CHAPTER_NO = "chapter_no";
    private static final String COL_NOTE = "note";
    public static final String COL_LOCATOR = "readlocator";

    public static final String SQL_CREATE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " ( " + ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT" + ","
            + COL_BOOK_ID + " TEXT" + ","
            + COL_CONTENT + " TEXT" + ","
            + COL_DATE + " TEXT" + ","
            + COL_DATE_TS + " TEXT" + ","
            + COL_CHAPTER_NO + " INTEGER" + ","
            + COL_NOTE + " TEXT" + ","
            + COL_LOCATOR + " TEXT" + ")";

    public static final String SQL_DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;

    public static ContentValues getBookmarkContentValues(BookMark bookMark) {
        ContentValues values = new ContentValues();
        values.put(COL_BOOK_ID, bookMark.getBookId());
        values.put(COL_CONTENT, bookMark.getContent());
        values.put(COL_DATE, getDateTimeString((bookMark.getDate())));
        values.put(COL_DATE_TS, bookMark.getTimeStamp());
        values.put(COL_CHAPTER_NO, bookMark.getChapterNo());
        values.put(COL_NOTE, bookMark.getNote());
        values.put(COL_LOCATOR, bookMark.getLocator());
        return values;
    }

    public final boolean insertBookmark(String bookId, String contents, int chapterNo, String cfi) {
        Log.e(TABLE_NAME, " insertBookmark => " + bookId + ", cfi => " + cfi);
        BookmarkImpl impl = new BookmarkImpl();
        impl.setBookId(bookId);
        impl.setContent(contents);
        impl.setChapterNo(chapterNo);
        impl.setDate(Calendar.getInstance().getTime());
        impl.setTimeStamp(System.currentTimeMillis() / 1000);
        impl.setLocator(cfi);
        Log.e(TABLE_NAME, " IMPLE => " + impl);

        long idx = DbAdapter.saveBookMark(getBookmarkContentValues(impl));
        Log.e(TABLE_NAME, " IDX => " + idx);
        if (idx != -1) {
            impl.setId((int) idx);
        }
        return true;
    }

    public static ArrayList<BookmarkImpl> getBookmarksForID(String id) {
        Log.e(TABLE_NAME, "BOOKIDX :::: " + id);
        Cursor cursor = DbAdapter.getBookmarksForBookId(id);

        ArrayList<BookmarkImpl> bookmarks = new ArrayList<>();
        while (cursor.moveToNext()) {
            bookmarks.add(new BookmarkImpl(
                    cursor.getInt(cursor.getColumnIndex(ID))
                    , cursor.getString(cursor.getColumnIndex(COL_BOOK_ID))
                    , cursor.getString(cursor.getColumnIndex(COL_CONTENT))
                    , getDateTime(cursor.getString(cursor.getColumnIndex(COL_DATE)))
                    , cursor.getLong(cursor.getColumnIndex(COL_DATE_TS))
                    , cursor.getInt(cursor.getColumnIndex(COL_CHAPTER_NO))
                    , cursor.getString(cursor.getColumnIndex(COL_LOCATOR))
                    , cursor.getString(cursor.getColumnIndex(COL_NOTE))));
        }
        cursor.close();
        return bookmarks;
    }

    public static boolean deleteBookmark(int highlightId) {
        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(highlightId));
    }

//    public static final boolean deleteBookmark(String arg_date, String arg_name, Context context){
//        if(Bookmarkdatabase == null){
//            FolioDatabaseHelper dbHelper = new FolioDatabaseHelper(context);
//            Bookmarkdatabase = dbHelper.getWritableDatabase();
//        }
//        String query = "SELECT " + ID + " FROM " + TABLE_NAME + " WHERE " + date + " = \"" + arg_date + "\"" + "AND " + name + " = \"" + arg_name + "\"";
//        Cursor c = Bookmarkdatabase.rawQuery(query, null);
//
//        int id = -1;
//        while (c.moveToNext()) {
//            id = c.getInt(c.getColumnIndex(BookmarkTable.ID));
//        }
//        c.close();
//        return DbAdapter.deleteById(TABLE_NAME, ID, String.valueOf(id));
//    }

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
            Log.e("", "Date parsing failed", e);
        }
        return date1;
    }
}
