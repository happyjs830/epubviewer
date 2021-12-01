package com.folioreader.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;
import java.util.Objects;

public class BookmarkImpl implements Parcelable, BookMark {
    private int id;
    private String bookId;
    private String content;
    private Date date;
    private Long date_ts;
    private int chapterNo;
    private String cfi;
    private String note;

    public BookmarkImpl(int id, String bookId, String content, Date date, long date_ts, int chapterNo, String cfi, String note) {
        this.id = id;
        this.bookId = bookId;
        this.content = content;
        this.date = date;
        this.date_ts = date_ts;
        this.chapterNo = chapterNo;
        this.cfi = cfi;
        this.note = note;
    }

    public BookmarkImpl() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getTimeStamp() { return date_ts; }

    public void setTimeStamp(long ts) { this.date_ts = ts; }

    public int getChapterNo() {
        return chapterNo;
    }

    public void setChapterNo(int chapterNo) {
        this.chapterNo = chapterNo;
    }

    public String getLocator() {
        return cfi;
    }

    public void setLocator(String locator) {
        this.cfi = locator;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookmarkImpl impl = (BookmarkImpl) o;

        return id == impl.id
                && (bookId != null ? bookId.equals(impl.bookId) : impl.bookId == null
                && (content != null ? content.equals(impl.content) : impl.content == null
                && (date != null ? date.equals(impl.date) : impl.date == null
                && (Objects.equals(date_ts, impl.date_ts)))));
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (bookId != null ? bookId.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (date_ts != null ? date_ts.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "BookmarkImpl{" +
                "id=" + id +
                ", bookId='" + bookId + '\'' +
                ", content='" + content + '\'' +
                ", date=" + date +
                ", date_ts=" + date_ts +
                ", chapterNo=" + chapterNo +
                ", cfi=" + cfi +
                ", note='" + note + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(bookId);
        dest.writeString(content);
        dest.writeSerializable(date);
        dest.writeLong(date_ts);
        dest.writeInt(chapterNo);
        dest.writeString(cfi);
        dest.writeString(note);
    }

    private void readFromParcel(Parcel in) {
        id = in.readInt();
        bookId = in.readString();
        content = in.readString();
        date = (Date) in.readSerializable();
        date_ts = in.readLong();
        chapterNo = in.readInt();
        cfi = in.readString();
        note = in.readString();
    }

    public static final Creator<HighlightImpl> CREATOR = new Creator<HighlightImpl>() {
        @Override
        public HighlightImpl createFromParcel(Parcel in) {
            return new HighlightImpl(in);
        }

        @Override
        public HighlightImpl[] newArray(int size) {
            return new HighlightImpl[size];
        }
    };
}
