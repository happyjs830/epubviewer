package com.folioreader.model;

import java.util.Date;

public interface BookMark {
    String getBookId();
    String getContent();
    Date getDate();
    long getTimeStamp();
    int getChapterNo();
    String getNote();
    String getLocator();
}
