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

        jsPath = jsPath + String.format(context.getString(R.string.script_tag_method_call),
                "setMediaOverlayStyleColors('#C0ED72','#C0ED72')") + "\n";

        jsPath = jsPath
                + "<meta name=\"viewport\" content=\"height=device-height, user-scalable=no\" />";

        String toInject = "\n" + cssPath + "\n" + jsPath + "\n</head>";
        htmlContent = htmlContent.replace("</head>", toInject);

        String classes = "";
        switch (config.getFont()) {
            case Constants.FONT_ANDADA:
                classes = "andada";
                break;
            case Constants.FONT_LATO:
                classes = "lato";
                break;
            case Constants.FONT_LORA:
                classes = "lora";
                break;
            case Constants.FONT_RALEWAY:
                classes = "raleway";
                break;
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

        if (config.isNightMode()) {
            classes += " nightMode";
        }

        switch (config.getCurrentTheme()) {
            case "WHITE": classes += " theme-white"; break;
            case "GRAY": classes += " theme-gray"; break;
            case "GREEN": classes += " theme-green"; break;
            case "WOOD": classes += " theme-wood"; break;
            case "BLACK": classes += " theme-black"; break;
        }

        switch (config.getFontSize()) {
            case 0: classes += " textSizeOne"; break;
            case 1: classes += " textSizeOnePlus"; break;
            case 2: classes += " textSizeTwo"; break;
            case 3: classes += " textSizeTwoPlus"; break;
            case 4: classes += " textSizeThree"; break;
            case 5: classes += " textSizeThreePlus"; break;
            case 6: classes += " textSizeFour"; break;
            case 7: classes += " textSizeFourPlus"; break;
            case 8: classes += " textSizeFive"; break;
            case 9: classes += " textSizeFivePlus"; break;
            case 10: classes += " textSizeSix"; break;
            case 11: classes += " textSizeSixPlus"; break;
            case 12: classes += " textSizeSeven"; break;
            case 13: classes += " textSizeSevenPlus"; break;
            case 14: classes += " textSizeEight"; break;
            case 15: classes += " textSizeEightPlus"; break;
            case 16: classes += " textSizeNine"; break;
            case 17: classes += " textSizeNinePlus"; break;
            case 18: classes += " textSizeTen"; break;
            case 19: classes += " textSizeTenPlus"; break;
            default:
                break;
        }

        switch (config.getFontLineSpace()/10) {
            case 0: classes += " textLineHeightOne"; break;
            case 1: classes += " textLineHeightOnePlus"; break;
            case 2: classes += " textLineHeightTwo"; break;
            case 3: classes += " textLineHeightTwoPlus"; break;
            case 4: classes += " textLineHeightThree"; break;
            case 5: classes += " textLineHeightThreePlus"; break;
            case 6: classes += " textLineHeightFour"; break;
            case 7: classes += " textLineHeightFourPlus"; break;
            case 8: classes += " textLineHeightFive"; break;
            case 9: classes += " textLineHeightFivePlus"; break;
            case 10: classes += " textLineHeightSix"; break;
            case 11: classes += " textLineHeightSixPlus"; break;
            case 12: classes += " textLineHeightSeven"; break;
            case 13: classes += " textLineHeightSevenPlus"; break;
            case 14: classes += " textLineHeightEight"; break;
            case 15: classes += " textLineHeightEightPlus"; break;
            case 16: classes += " textLineHeightNine"; break;
            case 17: classes += " textLineHeightNinePlus"; break;
            case 18: classes += " textLineHeightTen"; break;
            case 19: classes += " textLineHeightTenPlus"; break;
            case 20: classes += " textLineHeightLast"; break;
            default:
                break;
        }

        switch (config.getFontWhiteSpace()) {
            case 0: classes += " textMarginOne"; break;
            case 1: classes += " textMarginTwo"; break;
            case 2: classes += " textMarginThree"; break;
            case 3: classes += " textMarginFour"; break;
            case 4: classes += " textMarginFive"; break;
            case 5: classes += " textMarginSix"; break;
            case 6: classes += " textMarginSeven"; break;
            default:
                break;
        }

        if (config.getAlignment().equals("LEFT")) {
            classes += " textAlignmentLeft";
        } else if (config.getAlignment().equals("BOTH")) {
            classes += " textAlignmentBoth";
        }

        htmlContent = htmlContent.replace("<html", "<html class=\"" + classes + "\"" +
                " onclick=\"onClickHtml()\"");
        return htmlContent;
    }
}
