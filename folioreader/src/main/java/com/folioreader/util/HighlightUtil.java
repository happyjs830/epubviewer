package com.folioreader.util;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.folioreader.model.HighLight;
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.sqlite.HighLightTable;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class HighlightUtil {

    private static final String TAG = "HighlightUtil";

    public static String createHighlightRangy(Context context, String content, String bookId, String pageId, int pageNo, String oldRangy) {
        try {
            JSONObject jObject = new JSONObject(content);

            String rangy = jObject.getString("rangy");
            String textContent = jObject.getString("content");
            String color = jObject.getString("color");

            // 저장될 하이라이트 텍스트 1,000자 체크 //
            String result = "";
            if (textContent.length() >= 1000) {
                result = textContent.substring(0, 1000);
            } else {
                result = textContent;
            }
            Log.e(TAG, "result => " + result);

            String rangyHighlightElement = getRangyString(rangy, oldRangy);

            HighlightImpl highlightImpl = new HighlightImpl();
            highlightImpl.setContent(result);
            highlightImpl.setType(color);
            highlightImpl.setPageNumber(pageNo);
            highlightImpl.setBookId(bookId);
            highlightImpl.setPageId(pageId);
            highlightImpl.setRangy(rangyHighlightElement);
            highlightImpl.setDate(Calendar.getInstance().getTime());
            highlightImpl.setTimeStamp(System.currentTimeMillis() / 1000);

            // save highlight to database
            long id = HighLightTable.insertHighlight(highlightImpl);
            if (id != -1) {
                highlightImpl.setId((int) id);
                sendHighlightBroadcastEvent(context, highlightImpl, HighLight.HighLightAction.NEW);
            }
            return rangy;
        } catch (JSONException e) {
            Log.e(TAG, "createHighlightRangy failed", e);
        }
        return "";
    }

    /**
     * function extracts rangy element corresponding to latest highlight.
     *
     * @param rangy    new rangy string generated after adding new highlight.
     * @param oldRangy rangy string before new highlight.
     * @return rangy element corresponding to latest element.
     */
    private static String getRangyString(String rangy, String oldRangy) {
        List<String> rangyList = getRangyArray(rangy);
        for (String firs : getRangyArray(oldRangy)) {
            rangyList.remove(firs);
        }
        if (rangyList.size() >= 1) {
            return rangyList.get(0);
        } else {
            return "";
        }
    }

    /**
     * function converts Rangy text into each individual element
     * splitting with '|'.
     *
     * @param rangy rangy test with format: type:textContent|start$end$id$class$containerId
     * @return ArrayList of each rangy element corresponding to each highlight
     */
    private static List<String> getRangyArray(String rangy) {
        List<String> rangyElementList = new ArrayList<>(Arrays.asList(rangy.split("\\|")));
        if (rangyElementList.contains("type:textContent")) {
            rangyElementList.remove("type:textContent");
        } else if (rangyElementList.contains("")) {
            return new ArrayList<>();
        }
        return rangyElementList;
    }

    public static String generateRangyString(String pageId) {
        List<String> rangyList = HighLightTable.getHighlightsForPageId(pageId);
        StringBuilder builder = new StringBuilder();
        if (!rangyList.isEmpty()) {
            builder.append("type:textContent");
            for (String rangy : rangyList) {
                builder.append('|');
                builder.append(rangy);
            }
        }
        return builder.toString();
    }

    public static void sendHighlightBroadcastEvent(Context context,
                                                   HighlightImpl highlightImpl,
                                                   HighLight.HighLightAction action) {

    }

}
