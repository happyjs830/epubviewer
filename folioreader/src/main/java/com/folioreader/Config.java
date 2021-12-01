package com.folioreader;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import org.json.JSONObject;

import java.util.Objects;

/**
 * Configuration class for FolioReader.
 */
@SuppressWarnings("PMD.AvoidDuplicateLiterals")
public class Config implements Parcelable {

    private static final String LOG_TAG = Config.class.getSimpleName();
    public static final String INTENT_CONFIG = "config";
    public static final String EXTRA_OVERRIDE_CONFIG = "com.folioreader.extra.OVERRIDE_CONFIG";
    public static final String CONFIG_FONT = "font";
    public static final String CONFIG_FONT_SIZE = "font_size";
    public static final String CONFIG_FONT_LINE_SPACE = "font_line_space";
    public static final String CONFIG_FONT_WHITE_SPACE = "font_white_space";
    public static final String CONFIG_IS_ALIGNMENT = "is_alignment";
    public static final String CONFIG_IS_PAGE = "is_page";
    public static final String CONFIG_IS_THEME = "is_theme";
    public static final String CONFIG_SCREEN_FILTER = "screen_filter";
    public static final String CONFIG_IS_NIGHT_MODE = "is_night_mode";
    public static final String CONFIG_THEME_COLOR_INT = "theme_color_int";
    public static final String CONFIG_ALLOWED_DIRECTION = "allowed_direction";
    public static final String CONFIG_DIRECTION = "direction";
    private static final AllowedDirection DEFAULT_ALLOWED_DIRECTION = AllowedDirection.ONLY_VERTICAL;
    private static final Direction DEFAULT_DIRECTION = Direction.VERTICAL;
    private static final int DEFAULT_THEME_COLOR_INT =
            ContextCompat.getColor(AppContext.get(), R.color.layout_top_background);

    private int font;
    private int fontSize;
    private int fontLineSpace;
    private int fontWhiteSpace;
    private String alignment;
    private String page;
    private String theme;
    private int screenFilter;
    private boolean nightMode;

    @ColorInt
    private int themeColor = DEFAULT_THEME_COLOR_INT;
    private AllowedDirection allowedDirection = DEFAULT_ALLOWED_DIRECTION;
    private Direction direction = DEFAULT_DIRECTION;

    /**
     * Reading modes available
     */
    public enum AllowedDirection {
        ONLY_VERTICAL, ONLY_HORIZONTAL, VERTICAL_AND_HORIZONTAL
    }

    /**
     * Reading directions
     */
    public enum Direction {
        VERTICAL, HORIZONTAL
    }

    public static final Creator<Config> CREATOR = new Creator<Config>() {
        @Override
        public Config createFromParcel(Parcel in) {
            return new Config(in);
        }

        @Override
        public Config[] newArray(int size) {
            return new Config[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(font);
        dest.writeInt(fontSize);
        dest.writeInt(fontLineSpace);
        dest.writeInt(fontWhiteSpace);
        dest.writeString(alignment);
        dest.writeString(page);
        dest.writeString(theme);
        dest.writeInt(screenFilter);
        dest.writeByte((byte) (nightMode ? 1 : 0));
        dest.writeInt(themeColor);
        dest.writeString(allowedDirection.toString());
        dest.writeString(direction.toString());
    }

    protected Config(Parcel in) {
        font = in.readInt();
        fontSize = in.readInt();
        fontLineSpace = in.readInt();
        fontWhiteSpace = in.readInt();
        alignment = in.readString();
        page = in.readString();
        theme = in.readString();
        screenFilter = in.readInt();
        nightMode = in.readByte() != 0;
        themeColor = in.readInt();
        allowedDirection = getAllowedDirectionFromString(LOG_TAG, Objects.requireNonNull(in.readString()));
        direction = getDirectionFromString(LOG_TAG, Objects.requireNonNull(in.readString()));
    }

    public Config() {
        font = 0;
        fontSize = 12;
        fontLineSpace = 100;
        fontWhiteSpace = 0;
        alignment = "LEFT";
        page = "TB";
        theme = "WHITE";
        screenFilter = 0;
    }

    public Config(JSONObject jsonObject) {
        font = jsonObject.optInt(CONFIG_FONT);
        fontSize = jsonObject.optInt(CONFIG_FONT_SIZE);
        fontLineSpace = jsonObject.optInt(CONFIG_FONT_LINE_SPACE);
        fontWhiteSpace = jsonObject.optInt(CONFIG_FONT_WHITE_SPACE);
        alignment = jsonObject.optString(CONFIG_IS_ALIGNMENT);
        page = jsonObject.optString(CONFIG_IS_PAGE);
        theme = jsonObject.optString(CONFIG_IS_THEME);
        screenFilter = jsonObject.optInt(CONFIG_SCREEN_FILTER);
        nightMode = jsonObject.optBoolean(CONFIG_IS_NIGHT_MODE);
        themeColor = getValidColorInt(jsonObject.optInt(CONFIG_THEME_COLOR_INT));
        allowedDirection = getAllowedDirectionFromString(LOG_TAG,
                jsonObject.optString(CONFIG_ALLOWED_DIRECTION));
        direction = getDirectionFromString(LOG_TAG, jsonObject.optString(CONFIG_DIRECTION));
    }

    public static Direction getDirectionFromString(final String LOG_TAG, String directionString) {
        switch (directionString) {
            case "VERTICAL":
                return Direction.VERTICAL;
            case "HORIZONTAL":
                return Direction.HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument directionString = `" + directionString
                        + "`, defaulting direction to " + DEFAULT_DIRECTION);
                return DEFAULT_DIRECTION;
        }
    }

    public static AllowedDirection getAllowedDirectionFromString(final String LOG_TAG, String allowedDirectionString) {
        switch (allowedDirectionString) {
            case "ONLY_VERTICAL":
                return AllowedDirection.ONLY_VERTICAL;
            case "ONLY_HORIZONTAL":
                return AllowedDirection.ONLY_HORIZONTAL;
            case "VERTICAL_AND_HORIZONTAL":
                return AllowedDirection.VERTICAL_AND_HORIZONTAL;
            default:
                Log.w(LOG_TAG, "-> Illegal argument allowedDirectionString = `"
                        + allowedDirectionString + "`, defaulting allowedDirection to "
                        + DEFAULT_ALLOWED_DIRECTION);
                return DEFAULT_ALLOWED_DIRECTION;
        }
    }

    public int getFont() {
        return font;
    }

    public Config setFont(int font) {
        this.font = font;
        return this;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Config setFontSize(int fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public int getFontLineSpace() { return fontLineSpace; }

    public Config setFontLineSpace(int fontLineSpace) {
        this.fontLineSpace = fontLineSpace;
        return this;
    }

    public int getFontWhiteSpace() { return fontWhiteSpace; }

    public Config setFontWhiteSpace(int fontWhiteSpace) {
        this.fontWhiteSpace = fontWhiteSpace;
        return this;
    }

    public String getAlignment() { return alignment; }

    public Config setAlignment(String alignment) {
        this.alignment = alignment;
        return this;
    }

    public String getPageType() { return page; }

    public Config setPageType(String page) {
        this.page = page;
        return this;
    }

    public String getCurrentTheme() { return theme; }

    public Config setCurrentTheme(String theme) {
        this.theme = theme;
        return this;
    }

    public int getScreenFilter() { return screenFilter; }

    public Config setScreenFilter(int filter) {
        this.screenFilter = filter;
        return this;
    }

    public boolean isNightMode() {
        return nightMode;
    }

    @ColorInt
    private int getValidColorInt(@ColorInt int colorInt) {
        if (colorInt >= 0) {
            Log.w(LOG_TAG, "-> getValidColorInt -> Invalid argument colorInt = " + colorInt +
                    ", Returning DEFAULT_THEME_COLOR_INT = " + DEFAULT_THEME_COLOR_INT);
            return DEFAULT_THEME_COLOR_INT;
        }
        return colorInt;
    }

    @ColorInt
    public int getThemeColor() {
        return themeColor;
    }

    public AllowedDirection getAllowedDirection() {
        return allowedDirection;
    }

    /**
     * Set reading direction mode options for users. This method should be called before
     * {@link #setDirection(Direction)} as it has higher preference.
     *
     * @param allowedDirection reading direction mode options for users. Setting to
     *                         {@link AllowedDirection#VERTICAL_AND_HORIZONTAL} users will have
     *                         choice to select the reading direction at runtime.
     */
    public Config setAllowedDirection(AllowedDirection allowedDirection) {

        this.allowedDirection = allowedDirection;

        if (allowedDirection == null) {
            this.allowedDirection = DEFAULT_ALLOWED_DIRECTION;
            direction = DEFAULT_DIRECTION;
            Log.w(LOG_TAG, "-> allowedDirection cannot be null, defaulting " +
                    "allowedDirection to " + DEFAULT_ALLOWED_DIRECTION + " and direction to " +
                    DEFAULT_DIRECTION);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL &&
                direction != Direction.VERTICAL) {
            direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> allowedDirection is " + allowedDirection +
                    ", defaulting direction to " + direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL &&
                direction != Direction.HORIZONTAL) {
            direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> allowedDirection is " + allowedDirection
                    + ", defaulting direction to " + direction);
        }

        return this;
    }

    public Direction getDirection() {
        return direction;
    }

    /**
     * Set reading direction. This method should be called after
     * {@link #setAllowedDirection(AllowedDirection)} as it has lower preference.
     *
     * @param direction reading direction.
     */
    public Config setDirection(Direction direction) {

        if (allowedDirection == AllowedDirection.VERTICAL_AND_HORIZONTAL && direction == null) {
            this.direction = DEFAULT_DIRECTION;
            Log.w(LOG_TAG, "-> direction cannot be `null` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_VERTICAL &&
                direction != Direction.VERTICAL) {
            this.direction = Direction.VERTICAL;
            Log.w(LOG_TAG, "-> direction cannot be `" + direction + "` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else if (allowedDirection == AllowedDirection.ONLY_HORIZONTAL &&
                direction != Direction.HORIZONTAL) {
            this.direction = Direction.HORIZONTAL;
            Log.w(LOG_TAG, "-> direction cannot be `" + direction + "` when allowedDirection is " +
                    allowedDirection + ", defaulting direction to " + this.direction);

        } else {
            this.direction = direction;
        }

        return this;
    }

    @NonNull
    @Override
    public String toString() {
        return "Config{" +
                "font=" + font +
                ", fontSize=" + fontSize +
                ", fontLineSpace=" + fontLineSpace +
                ", fontWhiteSpace=" + fontWhiteSpace +
                ", alignment=" + alignment +
                ", page=" + page +
                ", theme=" + theme +
                ", screenFIlter=" + screenFilter +
                ", nightMode=" + nightMode +
                ", themeColor=" + themeColor +
                ", allowedDirection=" + allowedDirection +
                ", direction=" + direction +
                '}';
    }
}


