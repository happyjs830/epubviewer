package com.folioreader;

import android.graphics.Color;
import android.util.Log;

public class ThemeConfig {

    // 상,하단,좌측 레이어 백그라운드
    public static int _baseBackgroundColor;
    public static int _mainBackButton;
    public static int _mainTitleTextColor;
    public static int _mainViewerColor;

    // 하단메뉴 //
    public static int _bottomMenuListNormalIcon, _bottomMenuListPressIcon;
    public static int _bottomMenuRecordNormalIcon, _bottomMenuRecordPressIcon;
    public static int _bottomMenuCommentNormalIcon, _bottomMenuCommentPressIcon;
    public static int _bottomMenuReviewNormalIcon, _bottomMenuReviewPressIcon;
    public static int _bottomMenuPrevBookIcon, _bottomMenuNextBookIcon;
    public static int _bottomMenuTextColor;
    public static int _bottomDevideLineColor;

    // 프로그레스바 //
    public static int _seekBarBackgroundColor, _seekBarProgressColor, _seekBarThumbColor;
    public static int _seekBarCurrentPageTextColor, _seekBarTotalPageTextColor;

    // 뷰어 설정 //
    public static int _settingFontSpinnerStyle;
    public static int _settingBtnCloseNormalIcon, _settingBtnClosePressIcon, _settingBtnCloseTextColor, _settingIconCloseColor;
    public static int _settingTitleTextColor;
    public static int _settingFontStyleSpinnerBackgroundColor, _settingFontStyleSpinnerBorderColor;
    public static int _settingFontSmallTextColor, _settingFontBigTextColor, _settingFontCenterTextColor;
    public static int _settingFontSmallIcon, _settingFontSmallBox, _settingFontBigIcon, _settingFontBigBox;
    public static int _settingAlignmentLeftNormalIcon, _settingAlignmentLeftPressIcon, _settingAlignmentBothNormalIcon, _settingAlignmentBothPressIcon;
    public static int _settingPageTbNormalIcon, _settingPageTbPressIcon, _settingPageLrNormalIcon, _settingPageLrPressIcon, _settingPageScrollNormalIcon, _settingPageScrollPressIcon;
    public static int _settingMenuTextNormalColor;
    public static int _settingThemeWhiteNormalIcon, _settingThemeWhitePressIcon, _settingThemeGrayNormalIcon, _settingThemeGrayPressIcon, _settingThemeGreenNormalIcon, _settingThemeGreenPressIcon, _settingThemeWoodNormalIcon, _settingThemeWoodPressIcon, _settingThemeBlackNormalIcon, _settingThemeBlackPressIcon;
    public static int _settingScreenFilterNormalBox, _settingScreenFilterPressBox, _settingScreenFilterNormalIcon, _settingScreenFilterPressIcon;

    // 검색 바 //
    public static int _searchBarBackgroundColor, _searchBarEditTextBackgroundColor, _searchBarEditTextBorderColor, _searchBarEditTextColor;
    public static int _searchBarSearchIcon;
    public static int _searchBarEditBox, _searchBarEditTextStyle;
    public static int _searchBarButtonStyle, _searchBarButtonTextColor;

    // 도서 정보 //
    public static int _leftLayerBackgroundColor;
    public static int _leftLayerTitleTextColor, _leftLayerAuthorTextColor, _leftLayerDescTextColor, _leftLayerMoreTextColor;
    public static int _leftLayerMoreIcon;
    public static int _leftLayerTabNormalTextColor, _leftLayerTabSelectedTextColor;
    public static int _leftLayerRecyclerViewItemTextColor, _leftLayerRecyclerViewItemBackgroundColor, _leftLayerRecyclerViewItemDeviderColor;


    public static void setTheme(Config config) {
        String theme = config.getCurrentTheme();
        Log.e("<TAG>", "theme : " + theme);
        if (theme.equals("WHITE") || theme.equals("GRAY")) {
            _baseBackgroundColor = Color.parseColor("#F2F4F5");
            _mainBackButton = R.drawable.ic_nav_back_gr;
            _mainTitleTextColor = Color.parseColor("#697383");
            _mainViewerColor = Color.parseColor("#20459E");
            _bottomMenuListNormalIcon = R.drawable.ic_nav_list_gr;
            _bottomMenuListPressIcon = R.drawable.ic_nav_list_bl;
            _bottomMenuRecordNormalIcon = R.drawable.ic_nav_book_gr;
            _bottomMenuRecordPressIcon = R.drawable.ic_nav_book_bl;
            _bottomMenuCommentNormalIcon = R.drawable.ic_nav_comment_gr;
            _bottomMenuCommentPressIcon = R.drawable.ic_nav_comment_bl;
            _bottomMenuReviewNormalIcon = R.drawable.ic_nav_review_gr;
            _bottomMenuReviewPressIcon = R.drawable.ic_nav_review_bl;
            _bottomMenuPrevBookIcon = R.drawable.ic_arrow_line_left_gr;
            _bottomMenuNextBookIcon = R.drawable.ic_arrow_line_right_gr;
            _bottomMenuTextColor = Color.parseColor("#97A7BC");
            _bottomDevideLineColor = Color.parseColor("#DBDFE4");
            _settingFontSpinnerStyle = R.drawable.custom_spinner_layout_light;
            _seekBarBackgroundColor = Color.parseColor("#DBDFE4");
            _seekBarProgressColor = Color.parseColor("#20459E");
            _seekBarThumbColor = Color.parseColor("#20459E");
            _settingBtnCloseNormalIcon = R.drawable.ic_arrow_down_gr;
            _settingBtnClosePressIcon = R.drawable.ic_arrow_up_gr;
            _settingBtnCloseTextColor = Color.parseColor("#97A7BC");
            _settingIconCloseColor = Color.parseColor("#DBDFE4");
            _settingTitleTextColor = Color.parseColor("#697383");
            _settingFontStyleSpinnerBackgroundColor = Color.parseColor("#F2F4F5");
            _settingFontStyleSpinnerBorderColor = Color.parseColor("#DBDFE4");
            _settingFontSmallTextColor = Color.parseColor("#97A7BC");
            _settingFontBigTextColor = Color.parseColor("#97A7BC");
            _settingFontCenterTextColor = Color.parseColor("#697383");
            _settingFontSmallIcon = R.drawable.ic_minus_gr;
            _settingFontSmallBox = R.drawable.custom_button_round_square_light;
            _settingFontBigIcon = R.drawable.ic_plus_gr;
            _settingFontBigBox = R.drawable.custom_button_round_square_light;
            _settingAlignmentLeftNormalIcon = R.drawable.ic_paragraph_left_gr;
            _settingAlignmentBothNormalIcon = R.drawable.ic_paragraph_justify_gr;
            _settingAlignmentLeftPressIcon = R.drawable.ic_paragraph_left_bl;
            _settingAlignmentBothPressIcon = R.drawable.ic_paragraph_justify_bl;
            _settingPageTbNormalIcon = R.drawable.ic_page_vertical_gr;
            _settingPageLrNormalIcon = R.drawable.ic_page_horizontal_gr;
            _settingPageScrollNormalIcon = R.drawable.ic_page_scroll_gr;
            _settingPageTbPressIcon = R.drawable.ic_page_vertical_bl;
            _settingPageLrPressIcon = R.drawable.ic_page_horizontal_bl;
            _settingPageScrollPressIcon = R.drawable.ic_page_scroll_bl;
            _settingMenuTextNormalColor = Color.parseColor("#97A7BC");
            _settingThemeWhiteNormalIcon = R.drawable.custom_theme_white_off_light;
            _settingThemeWhitePressIcon = R.drawable.custom_theme_white_on_light;
            _settingThemeGrayNormalIcon = R.drawable.custom_theme_gray_off_light;
            _settingThemeGrayPressIcon = R.drawable.custom_theme_gray_on_light;
            _settingThemeGreenNormalIcon = R.drawable.custom_theme_green_off_light;
            _settingThemeWoodNormalIcon = R.drawable.custom_theme_wood_off_light;
            _settingThemeBlackNormalIcon = R.drawable.custom_theme_black_off_light;
            _settingScreenFilterNormalBox = R.drawable.custom_button_screen_filter_off_light;
            _settingScreenFilterPressBox = R.drawable.custom_button_screen_filter_on_light;
            _settingScreenFilterNormalIcon = R.drawable.ic_moon_gr;
            _searchBarBackgroundColor = Color.parseColor("#F2F4F5");
            _searchBarEditTextBackgroundColor = Color.parseColor("#FFFFFF");
            _searchBarEditTextBorderColor = Color.parseColor("#DBDFE4");
            _searchBarEditTextColor = Color.parseColor("#697383");
            _searchBarSearchIcon = R.drawable.ic_nav_search_gr;
            _searchBarEditBox = R.drawable.custom_border_action_bar_bottom_light;
            _searchBarEditTextStyle = R.drawable.custom_border_search_bar_light;
            _searchBarButtonStyle = R.drawable.custom_button_search_light;
            _searchBarButtonTextColor = Color.parseColor("#FFFFFF");
            _leftLayerBackgroundColor = Color.parseColor("#F2F4F5");
            _leftLayerTitleTextColor = Color.parseColor("#697383");
            _leftLayerAuthorTextColor = Color.parseColor("#697383");
            _leftLayerDescTextColor = Color.parseColor("#697383");
            _leftLayerMoreTextColor = Color.parseColor("#97A7BC");
            _leftLayerMoreIcon = R.drawable.ic_arrow_front_gr;
            _leftLayerTabNormalTextColor = Color.parseColor("#697383");
            _leftLayerTabSelectedTextColor = Color.parseColor("#20459E");
            _leftLayerRecyclerViewItemTextColor = Color.parseColor("#697383");
            _leftLayerRecyclerViewItemBackgroundColor = Color.parseColor("#DBDFE4");
            _leftLayerRecyclerViewItemDeviderColor = Color.parseColor("#DBDFE4");
            _seekBarCurrentPageTextColor = Color.parseColor("#697383");
            _seekBarTotalPageTextColor = Color.parseColor("#97A7BC");

        } else {
            _baseBackgroundColor = Color.parseColor("#2A2F39");
            _mainBackButton = R.drawable.ic_nav_back_dk;
            _mainTitleTextColor = Color.parseColor("#97A7BC");
            _mainViewerColor = Color.parseColor("#ACC231");
            _bottomMenuListNormalIcon = R.drawable.ic_nav_list_dk;
            _bottomMenuListPressIcon = R.drawable.ic_nav_list_ge;
            _bottomMenuRecordNormalIcon = R.drawable.ic_nav_book_dk;
            _bottomMenuRecordPressIcon = R.drawable.ic_nav_book_ge;
            _bottomMenuCommentNormalIcon = R.drawable.ic_nav_comment_dk;
            _bottomMenuCommentPressIcon = R.drawable.ic_nav_comment_ge;
            _bottomMenuReviewNormalIcon = R.drawable.ic_nav_review_dk;
            _bottomMenuReviewPressIcon = R.drawable.ic_nav_review_ge;
            _bottomMenuPrevBookIcon = R.drawable.ic_arrow_line_left_dk;
            _bottomMenuNextBookIcon = R.drawable.ic_arrow_line_right_dk;
            _bottomMenuTextColor = Color.parseColor("#697383");
            _bottomDevideLineColor = Color.parseColor("#404551");
            _seekBarBackgroundColor = Color.parseColor("#404551");
            _seekBarProgressColor = Color.parseColor("#ACC231");
            _seekBarThumbColor = Color.parseColor("#ACC231");
            _settingFontSpinnerStyle = R.drawable.custom_spinner_layout_dark;
            _settingBtnCloseNormalIcon = R.drawable.ic_arrow_down_dk;
            _settingBtnClosePressIcon = R.drawable.ic_arrow_up_dk;
            _settingBtnCloseTextColor = Color.parseColor("#697383");
            _settingIconCloseColor = Color.parseColor("#404551");
            _settingTitleTextColor = Color.parseColor("#97A7BC");
            _settingFontStyleSpinnerBackgroundColor = Color.parseColor("#2A2F39");
            _settingFontStyleSpinnerBorderColor = Color.parseColor("#404551");
            _settingFontSmallTextColor = Color.parseColor("#697383");
            _settingFontBigTextColor = Color.parseColor("#697383");
            _settingFontCenterTextColor = Color.parseColor("#97A7BC");
            _settingFontSmallIcon = R.drawable.ic_minus_dk;
            _settingFontSmallBox = R.drawable.custom_button_round_square_dark;
            _settingFontBigIcon = R.drawable.ic_plus_dk;
            _settingFontBigBox = R.drawable.custom_button_round_square_dark;
            _settingAlignmentLeftNormalIcon = R.drawable.ic_paragraph_left_dk;
            _settingAlignmentBothNormalIcon = R.drawable.ic_paragraph_justify_dk;
            _settingAlignmentLeftPressIcon = R.drawable.ic_paragraph_left_ge;
            _settingAlignmentBothPressIcon = R.drawable.ic_paragraph_justify_ge;
            _settingPageTbNormalIcon = R.drawable.ic_page_vertical_dk;
            _settingPageLrNormalIcon = R.drawable.ic_page_horizontal_dk;
            _settingPageScrollNormalIcon = R.drawable.ic_page_scroll_dk;
            _settingPageTbPressIcon = R.drawable.ic_page_vertical_ge;
            _settingPageLrPressIcon = R.drawable.ic_page_horizontal_ge;
            _settingPageScrollPressIcon = R.drawable.ic_page_scroll_ge;
            _settingMenuTextNormalColor = Color.parseColor("#697383");
            _settingThemeWhiteNormalIcon = R.drawable.custom_theme_white_off_dark;
            _settingThemeGrayNormalIcon = R.drawable.custom_theme_gray_off_dark;
            _settingThemeGreenNormalIcon = R.drawable.custom_theme_green_off_dark;
            _settingThemeGreenPressIcon = R.drawable.custom_theme_green_on_dark;
            _settingThemeWoodNormalIcon = R.drawable.custom_theme_wood_off_dark;
            _settingThemeWoodPressIcon = R.drawable.custom_theme_wood_on_dark;
            _settingThemeBlackNormalIcon = R.drawable.custom_theme_black_off_dark;
            _settingThemeBlackPressIcon = R.drawable.custom_theme_black_on_dark;
            _settingScreenFilterNormalBox = R.drawable.custom_button_screen_filter_off_dark;
            _settingScreenFilterPressBox = R.drawable.custom_button_screen_filter_on_dark;
            _settingScreenFilterNormalIcon = R.drawable.ic_moon_dk;
            _searchBarBackgroundColor = Color.parseColor("#2A2F39");
            _searchBarEditTextBackgroundColor = Color.parseColor("#22242A");
            _searchBarEditTextBorderColor = Color.parseColor("#404551");
            _searchBarEditTextColor = Color.parseColor("#97A7BC");
            _searchBarSearchIcon = R.drawable.ic_nav_search_dk;
            _searchBarEditBox = R.drawable.custom_border_action_bar_bottom_dark;
            _searchBarEditTextStyle = R.drawable.custom_border_search_bar_dark;
            _searchBarButtonStyle = R.drawable.custom_button_search_dark;
            _searchBarButtonTextColor = Color.parseColor("#404551");
            _leftLayerBackgroundColor = Color.parseColor("#2A2F39");
            _leftLayerTitleTextColor = Color.parseColor("#97A7BC");
            _leftLayerAuthorTextColor = Color.parseColor("#97A7BC");
            _leftLayerDescTextColor = Color.parseColor("#97A7BC");
            _leftLayerMoreTextColor = Color.parseColor("#697383");
            _leftLayerMoreIcon = R.drawable.ic_arrow_front_dk;
            _leftLayerTabNormalTextColor = Color.parseColor("#97A7BC");
            _leftLayerTabSelectedTextColor = Color.parseColor("#ACC231");
            _leftLayerRecyclerViewItemTextColor = Color.parseColor("#97A7BC");
            _leftLayerRecyclerViewItemBackgroundColor = Color.parseColor("#22242A");
            _leftLayerRecyclerViewItemDeviderColor = Color.parseColor("#404551");
            _seekBarCurrentPageTextColor = Color.parseColor("#97A7BC");
            _seekBarTotalPageTextColor = Color.parseColor("#697383");
        }
        _settingScreenFilterPressIcon = R.drawable.ic_moon_gr;
    }

}
