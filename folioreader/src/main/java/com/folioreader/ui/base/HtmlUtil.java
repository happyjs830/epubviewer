package com.folioreader.ui.base;

import android.content.Context;
import android.util.Log;

import com.folioreader.Config;
import com.folioreader.Constants;
import com.folioreader.R;

/**
 * @author gautam chibde on 14/6/17.
 */

public final class HtmlUtil {

    /**
     * Function modifies input html string by adding extra css,js and font information.
     *
     * @param context     Activity Context
     * @param htmlContent input html raw data
     * @return modified raw html string
     */
    public static String getHtmlContent(Context context, String htmlContent, Config config) {

        String cssPath =
                String.format(context.getString(R.string.css_tag), "file:///android_asset/css/Style.css");

        String jsPath = String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jsface.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/jquery-3.4.1.min.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-core.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-highlighter.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-classapplier.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangy-serializer.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/Bridge.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/rangefix.js") + "\n";

        jsPath = jsPath + String.format(context.getString(R.string.script_tag),
                "file:///android_asset/js/readium-cfi.umd.js") + "\n";

        jsPath = jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />";

        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = getFont(config.getFont());

        classes += getTheme(config.getCurrentTheme());
        classes += getSize(config.getFontSize());
        classes += getLineSpace(config.getFontLineSpace());
        classes += getWhiteSpace(config.getFontWhiteSpace());
        classes += getAlignment(config.getAlignment());

        htmlContent = htmlContent.replace("<html", "<html class=\"" + classes + "\"" +
                " onclick=\"onClickHtml()\"");
        return htmlContent;
    }

    public static String getFont(int font) {
        String classes = "";
        switch (font) {
            case Constants.FONT_NANUM_GOTHIC:
                classes = "nanum_gothic";
                break;
            case Constants.FONT_NANUM_MYEONGJO:
                classes = "nanum_myeongjo";
                break;
            case Constants.FONT_BON_GOTHIC:
                classes = "notoserif_regular";
                break;
            default:
                break;
        }
        return classes;
    }

    public static String getSize(int size) {
        String classes = "";
        switch (size) {
            case 9: classes = " textSizeOne"; break;
            case 10: classes = " textSizeOnePlus"; break;
            case 11: classes = " textSizeTwo"; break;
            case 12: classes = " textSizeTwoPlus"; break;
            case 13: classes = " textSizeThree"; break;
            case 14: classes = " textSizeThreePlus"; break;
            case 15: classes = " textSizeFour"; break;
            case 16: classes = " textSizeFourPlus"; break;
            case 17: classes = " textSizeFive"; break;
            case 18: classes = " textSizeFivePlus"; break;
            case 19: classes = " textSizeSix"; break;
            case 20: classes = " textSizeSixPlus"; break;
            case 21: classes = " textSizeSeven"; break;
            case 22: classes = " textSizeSevenPlus"; break;
            case 23: classes = " textSizeEight"; break;
            case 24: classes = " textSizeEightPlus"; break;
            default:
                break;
        }
        return classes;
    }

    public static String getLineSpace(int lineSpace) {
        String classes = "";
        switch (lineSpace) {
            case 0: classes = " textLineHeightOne"; break;
            case 10: classes = " textLineHeightOnePlus"; break;
            case 20: classes = " textLineHeightTwo"; break;
            case 30: classes = " textLineHeightTwoPlus"; break;
            case 40: classes = " textLineHeightThree"; break;
            case 50: classes = " textLineHeightThreePlus"; break;
            case 60: classes = " textLineHeightFour"; break;
            case 70: classes = " textLineHeightFourPlus"; break;
            case 80: classes = " textLineHeightFive"; break;
            case 90: classes = " textLineHeightFivePlus"; break;
            case 100: classes = " textLineHeightSix"; break;
            case 110: classes = " textLineHeightSixPlus"; break;
            case 120: classes = " textLineHeightSeven"; break;
            case 130: classes = " textLineHeightSevenPlus"; break;
            case 140: classes = " textLineHeightEight"; break;
            case 150: classes = " textLineHeightEightPlus"; break;
            case 160: classes = " textLineHeightNine"; break;
            case 170: classes = " textLineHeightNinePlus"; break;
            case 180: classes = " textLineHeightTen"; break;
            case 190: classes = " textLineHeightTenPlus"; break;
            case 200: classes = " textLineHeightLast"; break;
            default:
                break;
        }
        return classes;
    }

    public static String getWhiteSpace(int whiteSpace) {
        String classes = "";
        switch (whiteSpace) {
            case 0: classes = " textMarginOne"; break;
            case 1: classes = " textMarginTwo"; break;
            case 2: classes = " textMarginThree"; break;
            case 3: classes = " textMarginFour"; break;
            case 4: classes = " textMarginFive"; break;
            case 5: classes = " textMarginSix"; break;
            case 6: classes = " textMarginSeven"; break;
            default:
                break;
        }
        return classes;
    }

    public static String getAlignment(String alignment) {
        String classes = "";
        if (alignment.equals("LEFT")) {
            classes += " textAlignmentLeft";
        } else if (alignment.equals("BOTH")) {
            classes += " textAlignmentBoth";
        }
        return classes;
    }

    public static String getTheme(String theme) {
        String classes = "";
        switch (theme) {
            case "WHITE": classes = " theme-white"; break;
            case "GRAY": classes = " theme-gray"; break;
            case "GREEN": classes = " theme-green"; break;
            case "WOOD": classes = " theme-wood"; break;
            case "BLACK": classes = " theme-black"; break;
        }
        return classes;
    }
}
