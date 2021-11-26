package com.folioreader.model;

import java.util.Date;

public interface BookMark {
    String getBookId();
    String getContent();
    Date getDate();
    long getTimeStamp();
    int getPageNumber();
    String getPageId();
    String getRangy();
    String getUUID();
    String getNote();
}
