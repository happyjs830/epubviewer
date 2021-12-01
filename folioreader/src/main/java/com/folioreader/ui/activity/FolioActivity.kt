/*
 * Copyright (C) 2016 Pedro Paulo de Amorim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.folioreader.ui.activity

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.AbsoluteSizeSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.folioreader.Config
import com.folioreader.Constants.*
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.ThemeConfig
import com.folioreader.model.BookmarkImpl
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighlightImpl
import com.folioreader.model.event.ReloadDataEvent
import com.folioreader.model.locators.ReadLocator
import com.folioreader.model.locators.SearchLocator
import com.folioreader.model.sqlite.BookmarkTable
import com.folioreader.ui.adapter.AdapterFragmentStateChapterList
import com.folioreader.ui.adapter.AdapterLayerSpinner
import com.folioreader.ui.adapter.FolioPageFragmentAdapter
import com.folioreader.ui.adapter.SearchAdapter
import com.folioreader.ui.fragment.*
import com.folioreader.ui.view.DirectionalViewpager
import com.folioreader.ui.view.FolioAppBarLayout
import com.folioreader.util.AppUtil
import com.folioreader.util.FileUtil
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_record.*
import kotlinx.android.synthetic.main.folio_activity.*
import kotlinx.android.synthetic.main.view_loading.*
import org.greenrobot.eventbus.EventBus
import org.readium.r2.shared.Link
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.CbzParser
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.parser.PubBox
import org.readium.r2.streamer.server.Server
import java.lang.ref.WeakReference
import java.util.*
import javax.security.auth.login.LoginException
import kotlin.math.ceil

class FolioActivity : AppCompatActivity(), FolioActivityCallback {

    private var bookFileName: String? = null

    private var mFolioPageViewPager: DirectionalViewpager? = null
    private var actionBar: ActionBar? = null
    private var appBarLayout: FolioAppBarLayout? = null
    private var toolbar: Toolbar? = null
    private var toolbar_title: TextView? = null
    private var distractionFreeMode: Boolean = false
    private var handler: Handler? = null

    // Bottom //
    private var fl_fragment_page: FrameLayout? = null
    private var ll_bottom_bar: ConstraintLayout? = null
    private var ll_bottom_seekbar: ConstraintLayout? = null
    private var btnList: Button? = null
    private var btnRecord: Button? = null
    private var btnComment: Button? = null
    private var btnReview: Button? = null
    private var pageSeekBar: SeekBar? = null
    private var pageTextView: TextView? = null
    private var btnPrevBook: Button? = null
    private var btnNextBook: Button? = null
    private var devideLine: View? = null

    private var currentChapterIndex: Int = 0
    private var mFolioPageFragmentAdapter: FolioPageFragmentAdapter? = null
    private var entryReadLocator: ReadLocator? = null
    private var lastReadLocator: ReadLocator? = null
    private var bookmarkReadLocator: ReadLocator? = null
    private var outState: Bundle? = null
    private var savedInstanceState: Bundle? = null

    private var r2StreamerServer: Server? = null
    private var pubBox: PubBox? = null
    private var spine: List<Link>? = null

    private var mBookId: String? = null
    private var mEpubFilePath: String? = null
    private var mEpubSourceType: EpubSourceType? = null
    private var mEpubRawId = 0
    private var direction: Config.Direction = Config.Direction.VERTICAL
    private var portNumber: Int = DEFAULT_PORT_NUMBER
    private var streamerUri: Uri? = null

    // Search //
    private var mSearchMenu: ConstraintLayout? = null
    private var mSearchBG: LinearLayoutCompat? = null
    private var mSearchEditBack: LinearLayoutCompat? = null
    private var mSearchIcon: ImageView? = null
    private var mSearchButton: Button? = null
    private var mSearchEdit: EditText? = null

    private var searchUri: Uri? = null
    private var searchAdapterDataBundle: Bundle? = null
    private var searchQuery: CharSequence? = null
    private var searchLocator: SearchLocator? = null

    private var leftLayout: ConstraintLayout? = null
    private var leftBackground: ConstraintLayout? = null
    private var leftLayoutTitle: TextView? = null
    private var leftLayoutAuthor: TextView? = null
    private var leftLayoutDesc: TextView? = null
    private var infoViewPager: ViewPager2? = null
    private var btn_more: Button? = null

    private var displayMetrics: DisplayMetrics? = null
    private var density: Float = 0.toFloat()
    private var topActivity: Boolean? = null
    private var taskImportance: Int = 0

    private var llSetting: ConstraintLayout? = null
    private var llTopSetting: RelativeLayout? = null
    private var llSettingDetail: RelativeLayout? = null
    private var llTopSettingBottom: RelativeLayout? = null
    private var btnLayerExpand: Button? = null
    private var iconSettingClose: ImageView? = null

    // Setting //
    private var params: WindowManager.LayoutParams? = null
    private lateinit var mConfig: Config
    // FontStyle //
    private var txtSettingFontStyle: TextView? = null
    private var fontSpinner: Spinner? = null
    // FontSize //
    private var txtSettingFontSize: TextView? = null
    private var btnFontSizeMinus: ImageButton? = null
    private var tvFontSizeMinus: Button? = null
    private var btnFontSizePlus: ImageButton? = null
    private var tvFontSizePlus: Button? = null
    private var tvFontSizeCenter: TextView? = null
    // FontLineSpace //
    private var txtSettingFontLineSpace: TextView? = null
    private var btnFontLineSpaceMinus: ImageButton? = null
    private var tvFontLineSpaceMinus: Button? = null
    private var btnFontLineSpacePlus: ImageButton? = null
    private var tvFontLineSpacePlus: Button? = null
    private var tvFontLineSpaceCenter: TextView? = null
    // FontWhiteSpace //
    private var txtSettingFontWhiteSpace: TextView? = null
    private var btnFontWhiteSpaceMinus: ImageButton? = null
    private var tvFontWhiteSpaceMinus: Button? = null
    private var btnFontWhiteSpacePlus: ImageButton? = null
    private var tvFontWhiteSpacePlus: Button? = null
    private var tvFontWhiteSpaceCenter: TextView? = null
    // Alignment //
    private var txtSettingAlignment: TextView? = null
    private var alignment_left: Button? = null
    private var alignment_both: Button? = null
    // Page //
    private var txtSettingPage: TextView? = null
    private var page_tb: Button? = null
    private var page_lr: Button? = null
    private var page_scroll: Button? = null
    // Theme //
    private var txtSettingTheme: TextView? = null
    private var btnThemeWhite: ImageView? = null
    private var btnThemeGray: ImageView? = null
    private var btnThemeGreen: ImageView? = null
    private var btnThemeWood: ImageView? = null
    private var btnThemeBlack: ImageView? = null
    // Brightness //
    private var txtBrightness: TextView? = null
    private var brightnessSeekbar: SeekBar? = null
    // Screen Filter //
    private var iv_screen_fliter: ImageView? = null
    private var btnBlueFilter: ImageButton? = null

    companion object {
        @JvmField
        val LOG_TAG: String = FolioActivity::class.java.simpleName

        const val INTENT_EPUB_SOURCE_PATH = "com.folioreader.epub_asset_path"
        const val INTENT_EPUB_SOURCE_TYPE = "epub_source_type"
        const val EXTRA_READ_LOCATOR = "com.folioreader.extra.READ_LOCATOR"
        private const val BUNDLE_READ_LOCATOR_CONFIG_CHANGE = "BUNDLE_READ_LOCATOR_CONFIG_CHANGE"
        private const val BUNDLE_DISTRACTION_FREE_MODE = "BUNDLE_DISTRACTION_FREE_MODE"
        const val EXTRA_SEARCH_ITEM = "EXTRA_SEARCH_ITEM"
        const val ACTION_SEARCH_CLEAR = "ACTION_SEARCH_CLEAR"
        private const val HIGHLIGHT_ITEM = "highlight_item"
        private const val BOOKMARK_ITEM = "bookmark_item"
    }

    private val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0)
                result = resources.getDimensionPixelSize(resourceId)
            return result
        }

    private val currentFragment: FolioPageFragment?
        get() = if (mFolioPageFragmentAdapter != null && mFolioPageViewPager != null) {
            mFolioPageFragmentAdapter!!.getItem(mFolioPageViewPager!!.currentItem) as FolioPageFragment
        } else {
            null
        }

    enum class EpubSourceType {
        RAW,
        ASSETS,
        SD_CARD
    }

    private enum class RequestCode(val value: Int) {
        CONTENT_HIGHLIGHT(77),
        SEARCH(101)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        Log.e(LOG_TAG, "-> onNewIntent")

        val action = getIntent().action
        if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {

            if (topActivity == null || topActivity == false) {
                finish()

                // To determine if app in background or foreground
                var appInBackground = false
                if (Build.VERSION.SDK_INT < 26) {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_BACKGROUND == taskImportance)
                        appInBackground = true
                } else {
                    if (ActivityManager.RunningAppProcessInfo.IMPORTANCE_CACHED == taskImportance)
                        appInBackground = true
                }
                if (appInBackground)
                    moveTaskToBack(true)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Log.e(LOG_TAG, "-> onResume")
        topActivity = true

        val action = intent.action
        if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {
            finish()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.e(LOG_TAG, "-> onStop")
        topActivity = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Need to add when vector drawables support library is used.
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        handler = Handler()
        val display = windowManager.defaultDisplay
        displayMetrics = resources.displayMetrics
        display.getRealMetrics(displayMetrics)
        density = displayMetrics!!.density

        // Fix for screen get turned off while reading
        // getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setConfig(savedInstanceState)
        initDistractionFreeMode()

        setContentView(R.layout.folio_activity)
        this.savedInstanceState = savedInstanceState

        if (savedInstanceState != null) {
            searchAdapterDataBundle = savedInstanceState.getBundle(SearchAdapter.DATA_BUNDLE)
            searchQuery = savedInstanceState.getCharSequence(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY)
        }

        mBookId = intent.getStringExtra(FolioReader.EXTRA_BOOK_ID)
        mEpubSourceType = intent.extras!!.getSerializable(INTENT_EPUB_SOURCE_TYPE) as EpubSourceType
        if (mEpubSourceType == EpubSourceType.RAW) {
            mEpubRawId = intent.extras!!.getInt(INTENT_EPUB_SOURCE_PATH)
        } else {
            mEpubFilePath = intent.extras!!.getString(INTENT_EPUB_SOURCE_PATH)
        }

        initActionBar()
        initBottomBar()
        initLeftLayout()
        initSetting()

        if (ContextCompat.checkSelfPermission(this@FolioActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@FolioActivity, getWriteExternalStoragePerms(), WRITE_EXTERNAL_STORAGE_REQUEST)
        } else {
            setupBook()
        }
    }

    private fun initActionBar() {
        Log.e("<TAG>", "initActionBar")

        appBarLayout = findViewById(R.id.appBarLayout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.elevation = 0f
        toolbar_title = findViewById(R.id.toolbar_title)

        // Setting //
        llSetting = findViewById(R.id.ll_setting_layer)
        llTopSetting = findViewById(R.id.ll_top_setting)
        llSettingDetail = findViewById(R.id.ll_top_setting_detail)
        llTopSettingBottom = findViewById(R.id.ll_top_setting_bottom)
        btnLayerExpand = findViewById(R.id.btnLayerExpand)
        btnLayerExpand!!.setOnClickListener {
            llSettingDetail!!.visibility = if (llSettingDetail!!.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            if (llSettingDetail!!.visibility == View.VISIBLE) {
                btnLayerExpand!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingBtnClosePressIcon, 0, 0, 0)
                btnLayerExpand!!.setText(R.string.layout_setting_detail_close)
            } else {
                btnLayerExpand!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingBtnCloseNormalIcon, 0, 0, 0)
                btnLayerExpand!!.setText(R.string.layout_setting_detail_open)
                hideDetailSetting()
            }
        }
        iconSettingClose = findViewById(R.id.iconSettingClose)

        // Search //
        mSearchMenu = findViewById(R.id.ll_menu_search)
        mSearchBG = findViewById(R.id.ll_search_background)
        mSearchEditBack = findViewById(R.id.ll_search_bar)
        mSearchIcon = findViewById(R.id.iv_search_icon)
        mSearchEdit = findViewById(R.id.et_search)
        mSearchEdit!!.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    search()
                    return true
                }
                return false
            }
        })

        mSearchButton = findViewById(R.id.bt_search)
        mSearchButton!!.setOnClickListener {
            Log.e(LOG_TAG, "on click search button ::: ${mSearchEdit!!.text}")
            if (mSearchEdit!!.text.isEmpty()) {
                Log.e(LOG_TAG, "검색어를 입력해야지~?")
                val builder = AlertDialog.Builder(this)
                builder.setMessage("검색어를 입력해주세요.")
                builder.setPositiveButton("확인") { _, _ -> }
                builder.show()
            } else {
                Log.e(LOG_TAG, "검색 페이지로 이동")
                search()
            }
        }

        val config = AppUtil.getSavedConfig(applicationContext)!!

        if (config.isNightMode) {
            setNightMode()
        } else {
            setDayMode()
        }
    }

    private var tlIndex: TabLayout? = null
    private fun initBottomBar() {
        Log.e("<TAG>", "initBottomBar")

        ll_bottom_bar = findViewById(R.id.ll_bottom_bar)
        ll_bottom_seekbar = findViewById(R.id.ll_bottom_seekbar)
        fl_fragment_page = findViewById(R.id.fl_fragment_page)

        btnList = findViewById(R.id.btn_list)
        btnList!!.setOnClickListener {
            setMenuForFragment(false)

            val bundle = Bundle()
            bundle.putSerializable(PUBLICATION, pubBox!!.publication)
            bundle.putString(CHAPTER_SELECTED, spine!![currentChapterIndex].href)
            bundle.putString(FolioReader.EXTRA_BOOK_ID, mBookId)
            bundle.putString(BOOK_TITLE, bookFileName)

            val adapter = AdapterFragmentStateChapterList(this, bundle)
            infoViewPager!!.adapter = adapter

            // TabLayout //
            tlIndex = findViewById(R.id.tl_index)
            tlIndex!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    infoViewPager!!.currentItem = tab!!.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {}

                override fun onTabReselected(tab: TabLayout.Tab?) {}

            })

            ll_left_layer.visibility = View.VISIBLE

            ll_left_layer.setBackgroundColor(ThemeConfig._leftLayerBackgroundColor)
            infoViewPager!!.setBackgroundColor(ThemeConfig._leftLayerBackgroundColor)
            leftLayoutTitle!!.setTextColor(ThemeConfig._leftLayerTitleTextColor)
            leftLayoutAuthor!!.setTextColor(ThemeConfig._leftLayerAuthorTextColor)
            leftLayoutDesc!!.setTextColor(ThemeConfig._leftLayerDescTextColor)
            btn_more!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, ThemeConfig._leftLayerMoreIcon, 0)
            btn_more!!.setTextColor(ThemeConfig._leftLayerMoreTextColor)

            leftLayoutTitle!!.text = pubBox!!.publication.metadata.title

            for ((index, item) in pubBox!!.publication.metadata.authors.withIndex()) {
                Log.e(LOG_TAG,"$index = ${item.name}")
                leftLayoutAuthor!!.text = item.name
            }
            leftLayoutDesc!!.text = if (pubBox!!.publication.metadata.description != "") pubBox!!.publication.metadata.description else ""

            updateTheme()
        }

        btnRecord = findViewById(R.id.btn_record)
        btnRecord!!.setOnClickListener {
            Log.e(LOG_TAG, "itemRecord")
            setMenuForFragment(true)

            btnRecord!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuRecordPressIcon, 0, 0)
            btnRecord!!.setTextColor(ThemeConfig._mainViewerColor)

            val fragment = RecordFragment()

            val bundle = Bundle()
            bundle.putSerializable(PUBLICATION, pubBox!!.publication)
            bundle.putString(CHAPTER_SELECTED, spine!![currentChapterIndex].href)
            bundle.putString(FolioReader.EXTRA_BOOK_ID, mBookId)
            bundle.putString(BOOK_TITLE, bookFileName)
            fragment.arguments = bundle

            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_page, fragment).commit()
            hideSetting()
            if (ll_bottom_seekbar != null) {
                ll_bottom_seekbar!!.visibility = View.GONE
            }
        }

        btnComment = findViewById(R.id.btn_comment)
        btnComment!!.setOnClickListener {
            Log.e(LOG_TAG, "itemComment")
            setMenuForFragment(true)

            btnComment!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuCommentPressIcon, 0, 0)
            btnComment!!.setTextColor(ThemeConfig._mainViewerColor)

            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_page, CommentFragment()).commit()
            hideSetting()
            if (ll_bottom_seekbar != null) {
                ll_bottom_seekbar!!.visibility = View.GONE
            }
        }

        btnReview = findViewById(R.id.btn_review)
        btnReview!!.setOnClickListener {
            Log.e(LOG_TAG, "itemReview")
            setMenuForFragment(true)

            btnReview!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuReviewPressIcon, 0, 0)
            btnReview!!.setTextColor(ThemeConfig._mainViewerColor)

            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_page, ReviewFragment()).commit()
            hideSetting()
            if (ll_bottom_seekbar != null) {
                ll_bottom_seekbar!!.visibility = View.GONE
            }
        }

        // About page //
        pageSeekBar = findViewById(R.id.page_seekbar)
        pageSeekBar!!.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                Log.e(LOG_TAG, "onStopTrackingTouch : ${seekBar!!.progress}")

                // seekbar를 놓으면 해당 페이지로 이동
                if (currentFragment != null) {
                    currentFragment!!.moveSeekbarPosition(seekBar.progress)
                }
            }
        })

        pageTextView = findViewById(R.id.page_textView)

        // 이전, 다음 버튼
        btnPrevBook = findViewById(R.id.btn_prev_book)
        btnNextBook = findViewById(R.id.btn_next_book)
        devideLine = findViewById(R.id.devideLine)
    }

    private fun initLeftLayout() {
        Log.e("<TAG>", "initLeftLayout")

        leftLayout = findViewById(R.id.ll_left_layer)
        leftLayout!!.isClickable = true

        leftBackground = findViewById(R.id.ll_left_background)
        leftLayoutTitle = findViewById(R.id.tv_sub_book_title)
        leftLayoutAuthor = findViewById(R.id.tv_sub_book_author)
        leftLayoutDesc = findViewById(R.id.tv_book_desc)
        btn_more = findViewById(R.id.btn_more)
        btn_more!!.setOnClickListener {
            val dlg: AlertDialog.Builder = AlertDialog.Builder(this)
            dlg.setMessage("준비중입니다.")
            dlg.setPositiveButton("확인") { _, _ -> }
            dlg.show()
        }

        infoViewPager = findViewById(R.id.infoViewPager)
        infoViewPager!!.isUserInputEnabled = false
    }

    private fun initSetting() {
        Log.e("<TAG>", "initSetting")

        mConfig = AppUtil.getSavedConfig(this)!!

        // FontStyle //
        txtSettingFontStyle = findViewById(R.id.txtSettingFontStyle)
        fontSpinner = findViewById(R.id.spinner_font)

        val adapter = AdapterLayerSpinner(this, resources.getStringArray(R.array.array_font_style), mConfig)
        fontSpinner!!.adapter = adapter
        fontSpinner!!.setSelection(0, false)
        fontSpinner!!.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e(LOG_TAG, "position: $position, id : $id")

                mConfig.font = position
                AppUtil.saveConfig(this@FolioActivity, mConfig)

                if (currentFragment != null) {
                    currentFragment!!.setFont(mConfig.font)
                }
//                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        fontSpinner!!.setBackgroundResource(ThemeConfig._settingFontSpinnerStyle)

        // FontSize //
        txtSettingFontSize = findViewById(R.id.txtSettingFontSize)
        btnFontSizeMinus = findViewById(R.id.btnFontSizeMinus)
        btnFontSizeMinus!!.setOnClickListener {
            setFontSize(-1)
        }
        tvFontSizeMinus = findViewById(R.id.tvFontSizeMinus)
        tvFontSizeMinus!!.setOnClickListener {
            setFontSize(-1)
        }
        btnFontSizePlus = findViewById(R.id.btnFontSizePlus)
        btnFontSizePlus!!.setOnClickListener {
            setFontSize(1)
        }
        tvFontSizePlus = findViewById(R.id.tvFontSizePlus)
        tvFontSizePlus!!.setOnClickListener {
            setFontSize(1)
        }
        tvFontSizeCenter = findViewById(R.id.tvFontSizeCenter)
        setFontSize()
        // FontLineSpace //
        txtSettingFontLineSpace = findViewById(R.id.txtSettingFontLineSpace)
        btnFontLineSpaceMinus = findViewById(R.id.btnFontLineSpaceMinus)
        btnFontLineSpaceMinus!!.setOnClickListener {
            setFontLineSpace(-1)
        }
        tvFontLineSpaceMinus = findViewById(R.id.tvFontLineSpaceMinus)
        tvFontLineSpaceMinus!!.setOnClickListener {
            setFontLineSpace(-1)
        }
        btnFontLineSpacePlus = findViewById(R.id.btnFontLineSpacePlus)
        btnFontLineSpacePlus!!.setOnClickListener {
            setFontLineSpace(1)
        }
        tvFontLineSpacePlus = findViewById(R.id.tvFontLineSpacePlus)
        tvFontLineSpacePlus!!.setOnClickListener {
            setFontLineSpace(1)
        }
        tvFontLineSpaceCenter = findViewById(R.id.tvFontLineSpaceCenter)
        setFontLineSpace()
        // FontWhiteSpace //
        txtSettingFontWhiteSpace = findViewById(R.id.txtSettingFontWhiteSpace)
        btnFontWhiteSpaceMinus = findViewById(R.id.btnFontWhiteSpaceMinus)
        btnFontWhiteSpaceMinus!!.setOnClickListener {
            setFontWhiteSpace(-1)
        }
        tvFontWhiteSpaceMinus = findViewById(R.id.tvFontWhiteSpaceMinus)
        tvFontWhiteSpaceMinus!!.setOnClickListener {
            setFontWhiteSpace(-1)
        }
        btnFontWhiteSpacePlus = findViewById(R.id.btnFontWhiteSpacePlus)
        btnFontWhiteSpacePlus!!.setOnClickListener {
            setFontWhiteSpace(1)
        }
        tvFontWhiteSpacePlus = findViewById(R.id.tvFontWhiteSpacePlus)
        tvFontWhiteSpacePlus!!.setOnClickListener {
            setFontWhiteSpace(1)
        }
        tvFontWhiteSpaceCenter = findViewById(R.id.tvFontWhiteSpaceCenter)
        setFontWhiteSpace()
        // Alignment //
        txtSettingAlignment = findViewById(R.id.txtSettingAlignment)
        alignment_left = findViewById(R.id.alignment_left)
        alignment_left!!.setOnClickListener {
            setAlignment("LEFT")
        }
        alignment_both = findViewById(R.id.alignment_both)
        alignment_both!!.setOnClickListener {
            setAlignment("BOTH")
        }
        setAlignment()
        // Page //
        txtSettingPage = findViewById(R.id.txtSettingPage)
        page_tb = findViewById(R.id.page_tb)
        page_tb!!.setOnClickListener {
            setPage("TB")
            onDirectionChange(Config.Direction.VERTICAL)
        }
        page_lr = findViewById(R.id.page_lr)
        page_lr!!.setOnClickListener {
            setPage("LR")
            onDirectionChange(Config.Direction.HORIZONTAL)
        }
        page_scroll = findViewById(R.id.page_scroll)
        page_scroll!!.setOnClickListener {
            setPage("SCROLL")
            onDirectionChange(Config.Direction.VERTICAL)
        }
        setPage()
        // Theme //
        txtSettingTheme = findViewById(R.id.txtSettingTheme)
        btnThemeWhite = findViewById(R.id.btnThemeWhite)
        btnThemeWhite!!.setOnClickListener {
            setTheme("WHITE")
        }
        btnThemeGray = findViewById(R.id.btnThemeGray)
        btnThemeGray!!.setOnClickListener {
            setTheme("GRAY")
        }
        btnThemeGreen = findViewById(R.id.btnThemeGreen)
        btnThemeGreen!!.setOnClickListener {
            setTheme("GREEN")
        }
        btnThemeWood = findViewById(R.id.btnThemeWood)
        btnThemeWood!!.setOnClickListener {
            setTheme("WOOD")
        }
        btnThemeBlack = findViewById(R.id.btnThemeBlack)
        btnThemeBlack!!.setOnClickListener {
            setTheme("BLACK")
        }
        // Brightness //
        txtBrightness = findViewById(R.id.txtBrightness)
        params = window.attributes
        brightnessSeekbar = findViewById(R.id.brightness_seekbar)
        brightnessSeekbar!!.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.e(LOG_TAG, "")
                if (fromUser) {
                    Log.e(LOG_TAG, "progress : $progress")

                    var pt: Int = progress
                    if (progress < 20) {
                        pt = 20
                    } else if (progress > 100) {
                        pt = 100
                    }

                    params!!.screenBrightness = pt.toFloat() / 100
                    window.attributes = params
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        iv_screen_fliter = findViewById(R.id.iv_screen_fliter)
        btnBlueFilter = findViewById(R.id.btnBlueFilter)
        btnBlueFilter!!.setOnClickListener {
            Log.e(LOG_TAG, "setOnClickListener : btnBlueFilter")
            btnBlueFilter!!.isSelected = !btnBlueFilter!!.isSelected
            if (btnBlueFilter!!.isSelected) {
                mConfig.screenFilter = 1
            } else {
                mConfig.screenFilter = 0
            }
            AppUtil.saveConfig(this, mConfig)
            iv_screen_fliter!!.visibility = if (btnBlueFilter!!.isSelected) View.VISIBLE else View.GONE
        }
        setTheme()
    }

    private fun setFontSize(type:Int = 0) {
        var change: Boolean = false
        if (type == -1) {
            // 크기 감소
            if (mConfig.fontSize > 9) {
                mConfig.fontSize = mConfig.fontSize - 1
                change = true
            }
        } else if (type == 1) {
            // 크기 증가
            if (mConfig.fontSize < 24) {
                mConfig.fontSize = mConfig.fontSize + 1
                change = true
            }
        }

        if (change) {
            currentFragment!!.setFontSize(mConfig.fontSize)
            AppUtil.saveConfig(this, mConfig)
//            EventBus.getDefault().post(ReloadDataEvent())
        }

        tvFontSizeMinus!!.text = if (mConfig.fontSize <= 9) "" else (mConfig.fontSize-1).toString() + "px"
        tvFontSizeCenter!!.text = (mConfig.fontSize).toString() + "px"
        tvFontSizePlus!!.text = if (mConfig.fontSize > 23) "" else (mConfig.fontSize+1).toString() + "px"
    }

    private fun setFontLineSpace(type:Int = 0) {
        var change: Boolean = false
        if (type == -1) {
            // 크기 감소
            if (mConfig.fontLineSpace > 0) {
                mConfig.fontLineSpace = mConfig.fontLineSpace - 10
                change = true
            }
        } else if (type == 1) {
            // 크기 증가
            if (mConfig.fontLineSpace < 200) {
                mConfig.fontLineSpace = mConfig.fontLineSpace + 10
                change = true
            }
        }

        if (change) {
            currentFragment!!.setFontLineSpace(mConfig.fontLineSpace)
            AppUtil.saveConfig(this, mConfig)
//            EventBus.getDefault().post(ReloadDataEvent())
        }

        tvFontLineSpaceMinus!!.text = if (mConfig.fontLineSpace <= 0) "" else (mConfig.fontLineSpace-10).toString() + "%"
        tvFontLineSpaceCenter!!.text = (mConfig.fontLineSpace).toString() + "%"
        tvFontLineSpacePlus!!.text = if (mConfig.fontLineSpace > 190) "" else (mConfig.fontLineSpace+10).toString() + "%"
    }

    private fun setFontWhiteSpace(type:Int = 0) {
        var change: Boolean = false
        if (type == -1) {
            // 크기 감소
            if (mConfig.fontWhiteSpace > 0) {
                mConfig.fontWhiteSpace = mConfig.fontWhiteSpace - 1
                change = true
            }
        } else if (type == 1) {
            // 크기 증가
            if (mConfig.fontWhiteSpace < 6) {
                mConfig.fontWhiteSpace = mConfig.fontWhiteSpace + 1
                change = true
            }
        }

        if (change) {
            currentFragment!!.setFontWhiteSpace(mConfig.fontWhiteSpace)
            AppUtil.saveConfig(this, mConfig)
//            EventBus.getDefault().post(ReloadDataEvent())
        }

        tvFontWhiteSpaceMinus!!.text = if (mConfig.fontWhiteSpace <= 0) "" else (mConfig.fontWhiteSpace-1).toString()
        tvFontWhiteSpaceCenter!!.text = (mConfig.fontWhiteSpace).toString()
        tvFontWhiteSpacePlus!!.text = if (mConfig.fontWhiteSpace > 5) "" else (mConfig.fontWhiteSpace+1).toString()
    }

    private fun setAlignment(type:String = "") {
        alignment_left!!.isSelected = false
        alignment_both!!.isSelected = false

        var t: String = type
        if (t == "") {
            t = mConfig.alignment
            if (t == "") {
                t = "LEFT"
            }
        }

        mConfig.alignment = t
        AppUtil.saveConfig(this, mConfig)

        if (currentFragment != null) {
            currentFragment!!.setAlignment(mConfig.alignment)
        }

        if (mConfig.alignment == "LEFT") {
            alignment_left!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentLeftPressIcon, 0, 0, 0)
            alignment_both!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentBothNormalIcon, 0, 0, 0)
            alignment_left!!.setTextColor(ThemeConfig._mainViewerColor)
            alignment_both!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
        } else {
            alignment_left!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentLeftNormalIcon, 0, 0, 0)
            alignment_both!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentBothPressIcon, 0, 0, 0)
            alignment_left!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            alignment_both!!.setTextColor(ThemeConfig._mainViewerColor)
        }

//        EventBus.getDefault().post(ReloadDataEvent())
    }

    private fun setPage(type:String = "") {
        page_tb!!.isSelected = false
        page_lr!!.isSelected = false
        page_scroll!!.isSelected = false

        var t: String = type
        if (t == "") {
            t = mConfig.pageType
            if (t == "") {
                t = "TB"
            }
        }

        mConfig.pageType = t
        AppUtil.saveConfig(this, mConfig)

        if (mConfig.pageType == "TB") {
            page_tb!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageTbPressIcon, 0, 0, 0)
            page_lr!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageLrNormalIcon, 0, 0, 0)
            page_scroll!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageScrollNormalIcon, 0, 0, 0)
            page_tb!!.setTextColor(ThemeConfig._mainViewerColor)
            page_lr!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_scroll!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
        } else if (mConfig.pageType == "LR") {
            page_tb!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageTbNormalIcon, 0, 0, 0)
            page_lr!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageLrPressIcon, 0, 0, 0)
            page_scroll!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageScrollNormalIcon, 0, 0, 0)
            page_tb!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_lr!!.setTextColor(ThemeConfig._mainViewerColor)
            page_scroll!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
        } else {
            page_tb!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageTbNormalIcon, 0, 0, 0)
            page_lr!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageLrNormalIcon, 0, 0, 0)
            page_scroll!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageScrollPressIcon, 0, 0, 0)
            page_tb!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_lr!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_scroll!!.setTextColor(ThemeConfig._mainViewerColor)
        }
        EventBus.getDefault().post(ReloadDataEvent())
    }

    private fun setTheme(type:String = "") {
        btnThemeWhite!!.isSelected = false
        btnThemeGray!!.isSelected = false
        btnThemeGreen!!.isSelected = false
        btnThemeWood!!.isSelected = false
        btnThemeBlack!!.isSelected = false

        var t: String = type
        if (t == "") {
            t = mConfig.currentTheme
            if (t == "") {
                t = "WHITE"
            }
        }

        mConfig.currentTheme = t
        AppUtil.saveConfig(this, mConfig)

        if (currentFragment != null) {
            currentFragment!!.setTheme(mConfig.currentTheme)
            updateTheme()
        }
        EventBus.getDefault().post(ReloadDataEvent())
    }

    private fun showSetting() {
        llSetting!!.visibility = View.VISIBLE
        llSetting!!.isClickable = true
    }

    private fun hideSetting() {
        llSetting!!.visibility = View.GONE
        hideDetailSetting()
    }

    private fun hideDetailSetting() {
        llSettingDetail!!.visibility = View.GONE
        btnLayerExpand!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingBtnCloseNormalIcon, 0, 0, 0)
        btnLayerExpand!!.setText(R.string.layout_setting_detail_open)
    }

    private fun updateTheme() {
        Log.e(LOG_TAG, "updateTheme : " + mConfig.currentTheme)
        ThemeConfig.setTheme(mConfig)

        // Top //
        actionBar!!.setBackgroundDrawable(ColorDrawable(ThemeConfig._baseBackgroundColor))

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_nav_back_gr)
        toolbar!!.navigationIcon = drawable
        toolbar_title!!.setTextColor(ThemeConfig._mainTitleTextColor)

        // Bottom //
        ll_bottom_bar!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
        ll_bottom_seekbar!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
        btnList!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuListNormalIcon, 0, 0)
        btnList!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnRecord!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuRecordNormalIcon, 0, 0)
        btnRecord!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnComment!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuCommentNormalIcon, 0, 0)
        btnComment!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnReview!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuReviewNormalIcon, 0, 0)
        btnReview!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnPrevBook!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._bottomMenuPrevBookIcon, 0, 0, 0)
        btnPrevBook!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnNextBook!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, ThemeConfig._bottomMenuNextBookIcon, 0)
        btnNextBook!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        devideLine!!.setBackgroundColor(ThemeConfig._bottomDevideLineColor)

        // Bottom Seekbar //
        ll_bottom_seekbar!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
        pageSeekBar!!.background = null
        pageSeekBar!!.progressDrawable.setTint(ThemeConfig._seekBarProgressColor)
        pageSeekBar!!.thumb.setTint(ThemeConfig._seekBarThumbColor)

        // Setting Layer //
        llTopSetting!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
        llSettingDetail!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
        llTopSettingBottom!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
        btnLayerExpand!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingBtnCloseNormalIcon, 0, 0, 0)
        btnLayerExpand!!.setTextColor(ThemeConfig._settingBtnCloseTextColor)
        iconSettingClose!!.setBackgroundColor(ThemeConfig._settingIconCloseColor)

        txtSettingFontStyle!!.setTextColor(ThemeConfig._settingTitleTextColor)
        fontSpinner!!.setBackgroundColor(ThemeConfig._settingFontStyleSpinnerBackgroundColor)
        fontSpinner!!.setBackgroundResource(ThemeConfig._settingFontSpinnerStyle)

        txtSettingFontSize!!.setTextColor(ThemeConfig._settingTitleTextColor)
        btnFontSizeMinus!!.setImageResource(ThemeConfig._settingFontSmallIcon)
        btnFontSizeMinus!!.setBackgroundResource(ThemeConfig._settingFontSmallBox)
        tvFontSizeMinus!!.setTextColor(ThemeConfig._settingFontSmallTextColor)
        tvFontSizeCenter!!.setTextColor(ThemeConfig._settingFontCenterTextColor)
        tvFontSizePlus!!.setTextColor(ThemeConfig._settingFontSmallTextColor)
        btnFontSizePlus!!.setImageResource(ThemeConfig._settingFontBigIcon)
        btnFontSizePlus!!.setBackgroundResource(ThemeConfig._settingFontBigBox)

        txtSettingFontLineSpace!!.setTextColor(ThemeConfig._settingTitleTextColor)
        btnFontLineSpaceMinus!!.setImageResource(ThemeConfig._settingFontSmallIcon)
        btnFontLineSpaceMinus!!.setBackgroundResource(ThemeConfig._settingFontSmallBox)
        tvFontLineSpaceMinus!!.setTextColor(ThemeConfig._settingFontSmallTextColor)
        tvFontLineSpaceCenter!!.setTextColor(ThemeConfig._settingFontCenterTextColor)
        tvFontLineSpacePlus!!.setTextColor(ThemeConfig._settingFontSmallTextColor)
        btnFontLineSpacePlus!!.setImageResource(ThemeConfig._settingFontBigIcon)
        btnFontLineSpacePlus!!.setBackgroundResource(ThemeConfig._settingFontBigBox)

        txtSettingFontWhiteSpace!!.setTextColor(ThemeConfig._settingTitleTextColor)
        btnFontWhiteSpaceMinus!!.setImageResource(ThemeConfig._settingFontSmallIcon)
        btnFontWhiteSpaceMinus!!.setBackgroundResource(ThemeConfig._settingFontSmallBox)
        tvFontWhiteSpaceMinus!!.setTextColor(ThemeConfig._settingFontSmallTextColor)
        tvFontWhiteSpaceCenter!!.setTextColor(ThemeConfig._settingFontCenterTextColor)
        tvFontWhiteSpacePlus!!.setTextColor(ThemeConfig._settingFontSmallTextColor)
        btnFontWhiteSpacePlus!!.setImageResource(ThemeConfig._settingFontSmallIcon)
        btnFontWhiteSpacePlus!!.setBackgroundResource(ThemeConfig._settingFontBigBox)

        txtSettingAlignment!!.setTextColor(ThemeConfig._settingTitleTextColor)
        if (mConfig.alignment == "LEFT") {
            alignment_left!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentLeftPressIcon, 0, 0, 0)
            alignment_both!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentBothNormalIcon, 0, 0, 0)
            alignment_left!!.setTextColor(ThemeConfig._mainViewerColor)
            alignment_both!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
        } else {
            alignment_left!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentLeftNormalIcon, 0, 0, 0)
            alignment_both!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingAlignmentBothPressIcon, 0, 0, 0)
            alignment_left!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            alignment_both!!.setTextColor(ThemeConfig._mainViewerColor)
        }

        txtSettingPage!!.setTextColor(ThemeConfig._settingTitleTextColor)
        if (mConfig.pageType == "TB") {
            page_tb!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageTbPressIcon, 0, 0, 0)
            page_lr!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageLrNormalIcon, 0, 0, 0)
            page_scroll!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageScrollNormalIcon, 0, 0, 0)
            page_tb!!.setTextColor(ThemeConfig._mainViewerColor)
            page_lr!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_scroll!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
        } else if (mConfig.pageType == "LR") {
            page_tb!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageTbNormalIcon, 0, 0, 0)
            page_lr!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageLrPressIcon, 0, 0, 0)
            page_scroll!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageScrollNormalIcon, 0, 0, 0)
            page_tb!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_lr!!.setTextColor(ThemeConfig._mainViewerColor)
            page_scroll!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
        } else {
            page_tb!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageTbNormalIcon, 0, 0, 0)
            page_lr!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageLrNormalIcon, 0, 0, 0)
            page_scroll!!.setCompoundDrawablesWithIntrinsicBounds(ThemeConfig._settingPageScrollPressIcon, 0, 0, 0)
            page_tb!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_lr!!.setTextColor(ThemeConfig._settingMenuTextNormalColor)
            page_scroll!!.setTextColor(ThemeConfig._mainViewerColor)
        }

        txtSettingTheme!!.setTextColor(ThemeConfig._settingTitleTextColor)
        if (mConfig.currentTheme == "WHITE") {
            btnThemeWhite!!.setBackgroundResource(ThemeConfig._settingThemeWhitePressIcon)
            btnThemeGray!!.setBackgroundResource(ThemeConfig._settingThemeGrayNormalIcon)
            btnThemeGreen!!.setBackgroundResource(ThemeConfig._settingThemeGreenNormalIcon)
            btnThemeWood!!.setBackgroundResource(ThemeConfig._settingThemeWoodNormalIcon)
            btnThemeBlack!!.setBackgroundResource(ThemeConfig._settingThemeBlackNormalIcon)
        } else if (mConfig.currentTheme == "GRAY") {
            btnThemeWhite!!.setBackgroundResource(ThemeConfig._settingThemeWhiteNormalIcon)
            btnThemeGray!!.setBackgroundResource(ThemeConfig._settingThemeGrayPressIcon)
            btnThemeGreen!!.setBackgroundResource(ThemeConfig._settingThemeGreenNormalIcon)
            btnThemeWood!!.setBackgroundResource(ThemeConfig._settingThemeWoodNormalIcon)
            btnThemeBlack!!.setBackgroundResource(ThemeConfig._settingThemeBlackNormalIcon)
        } else if (mConfig.currentTheme == "GREEN") {
            btnThemeWhite!!.setBackgroundResource(ThemeConfig._settingThemeWhiteNormalIcon)
            btnThemeGray!!.setBackgroundResource(ThemeConfig._settingThemeGrayNormalIcon)
            btnThemeGreen!!.setBackgroundResource(ThemeConfig._settingThemeGreenPressIcon)
            btnThemeWood!!.setBackgroundResource(ThemeConfig._settingThemeWoodNormalIcon)
            btnThemeBlack!!.setBackgroundResource(ThemeConfig._settingThemeBlackNormalIcon)
        } else if (mConfig.currentTheme == "WOOD") {
            btnThemeWhite!!.setBackgroundResource(ThemeConfig._settingThemeWhiteNormalIcon)
            btnThemeGray!!.setBackgroundResource(ThemeConfig._settingThemeGrayNormalIcon)
            btnThemeGreen!!.setBackgroundResource(ThemeConfig._settingThemeGreenNormalIcon)
            btnThemeWood!!.setBackgroundResource(ThemeConfig._settingThemeWoodPressIcon)
            btnThemeBlack!!.setBackgroundResource(ThemeConfig._settingThemeBlackNormalIcon)
        } else {
            btnThemeWhite!!.setBackgroundResource(ThemeConfig._settingThemeWhiteNormalIcon)
            btnThemeGray!!.setBackgroundResource(ThemeConfig._settingThemeGrayNormalIcon)
            btnThemeGreen!!.setBackgroundResource(ThemeConfig._settingThemeGreenNormalIcon)
            btnThemeWood!!.setBackgroundResource(ThemeConfig._settingThemeWoodNormalIcon)
            btnThemeBlack!!.setBackgroundResource(ThemeConfig._settingThemeBlackPressIcon)
        }

        txtBrightness!!.setTextColor(ThemeConfig._settingTitleTextColor)

        brightnessSeekbar!!.background = null
        brightnessSeekbar!!.progressDrawable.setTint(ThemeConfig._seekBarProgressColor)
        brightnessSeekbar!!.thumb.setTint(ThemeConfig._seekBarThumbColor)

        if (mConfig.screenFilter == 1) {
            btnBlueFilter!!.setBackgroundResource(ThemeConfig._settingScreenFilterPressBox)
            btnBlueFilter!!.setImageResource(ThemeConfig._settingScreenFilterPressIcon)
        } else {
            btnBlueFilter!!.setBackgroundResource(ThemeConfig._settingScreenFilterNormalBox)
            btnBlueFilter!!.setImageResource(ThemeConfig._settingScreenFilterNormalIcon)
        }

        // Search Bar //
        mSearchBG!!.setBackgroundResource(ThemeConfig._searchBarEditBox)
        mSearchEditBack!!.setBackgroundResource(ThemeConfig._searchBarEditTextStyle)
        mSearchIcon!!.setImageResource(ThemeConfig._searchBarSearchIcon)
        mSearchEdit!!.setTextColor(ThemeConfig._searchBarEditTextColor)
        mSearchButton!!.setBackgroundResource(ThemeConfig._searchBarButtonStyle)
        mSearchButton!!.setTextColor(ThemeConfig._searchBarButtonTextColor)

        if (tlIndex != null) {
            tlIndex!!.setBackgroundColor(ThemeConfig._baseBackgroundColor)
            tlIndex!!.setSelectedTabIndicatorColor(ThemeConfig._mainViewerColor)
            tlIndex!!.setTabTextColors(
                ThemeConfig._leftLayerTabNormalTextColor,
                ThemeConfig._leftLayerTabSelectedTextColor
            )
        }

        // Left Layer //
//        leftBackground!!.setBackgroundColor(ThemeConfig._leftLayerBackgroundColor)
//        leftLayoutTitle!!.setTextColor(ThemeConfig._leftLayerTitleTextColor)
//        leftLayoutAuthor!!.setTextColor(ThemeConfig._leftLayerAuthorTextColor)
//        leftLayoutDesc!!.setTextColor(ThemeConfig._leftLayerDescTextColor)
//        btn_more!!.setCompoundDrawablesWithIntrinsicBounds(0, 0, ThemeConfig._leftLayerMoreIcon, 0)
//        btn_more!!.setTextColor(ThemeConfig._leftLayerMoreTextColor)
    }

    override fun setPageInfo(currentPage: Int, totalPage: Int) {
        pageSeekBar!!.max = totalPage
        pageSeekBar!!.progress = currentPage+1

        val spannableString = SpannableString("${currentPage+1} / $totalPage")
        val from = 0
        val to = from + currentPage.toString().length
        spannableString.setSpan(StyleSpan(Typeface.BOLD), from, to, 0)
        spannableString.setSpan(ForegroundColorSpan(ThemeConfig._seekBarCurrentPageTextColor), from, to, 0)
        pageTextView!!.text = spannableString
    }

    fun setMenuForFragment(flag: Boolean) {
        btnRecord!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuRecordNormalIcon, 0, 0)
        btnRecord!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnComment!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuCommentNormalIcon, 0, 0)
        btnComment!!.setTextColor(ThemeConfig._bottomMenuTextColor)
        btnReview!!.setCompoundDrawablesWithIntrinsicBounds(0, ThemeConfig._bottomMenuReviewNormalIcon, 0, 0)
        btnReview!!.setTextColor(ThemeConfig._bottomMenuTextColor)

        if (fl_fragment_page != null) {
            if (flag) {
                fl_fragment_page!!.visibility = View.VISIBLE
            } else {
                fl_fragment_page!!.visibility = View.GONE
            }
        }

        if (!flag) {
            if (ll_bottom_seekbar != null) {
                ll_bottom_seekbar!!.visibility = View.VISIBLE
            }
        }
    }

    private fun search() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra(SearchActivity.BUNDLE_SPINE_SIZE, spine?.size ?: 0)
        intent.putExtra(SearchActivity.BUNDLE_SEARCH_URI, searchUri)
        intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
        intent.putExtra(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY, searchQuery)
        intent.putExtra("TEXT_FOR_SEARCH", mSearchEdit!!.text.toString())
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivityForResult(intent, RequestCode.SEARCH.value)
        overridePendingTransition(0,0)
        mSearchMenu!!.visibility = View.GONE
        mSearchEdit!!.setText("")
    }

    override fun setDayMode() {
        Log.e(LOG_TAG, "-> setDayMode")

        actionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.layout_top_background)))
        toolbar!!.setTitleTextColor(ContextCompat.getColor(this, R.color.layout_top_text_title))
    }

    override fun setNightMode() {
        Log.e(LOG_TAG, "-> setNightMode")

        actionBar!!.setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this, R.color.layout_top_background)))
        toolbar!!.setTitleTextColor(ContextCompat.getColor(this, R.color.layout_top_text_title))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        if (itemId == android.R.id.home) {
            finish()
            return true
        } else if (itemId == R.id.itemSetting) {
            if (mSearchMenu!!.visibility == View.VISIBLE) mSearchMenu!!.visibility = View.GONE

            if (llSetting!!.visibility == View.VISIBLE) {
                hideSetting()
            } else {
                showSetting()
            }
            return true
        } else if (itemId == R.id.itemSearch) {
            if (llSetting!!.visibility == View.VISIBLE) {
                hideSetting()
            }

            if (mSearchMenu!!.visibility == View.VISIBLE) {
                mSearchMenu!!.visibility = View.GONE
            } else {
                mSearchMenu!!.visibility = View.VISIBLE
                mSearchMenu!!.isClickable = true
            }
            return true
        } else if (itemId == R.id.itemBookmark) {
            val readLocator = currentFragment!!.getLastReadLocator()
            bookmarkReadLocator = readLocator

            if (currentFragment != null) {
                currentFragment!!.getCurrentText()
            }

            Log.e(LOG_TAG, "CONTENT : ${readLocator!!.toJson().toString()}" )


            val contents = "TestBookMark"

            var idx: Int = mFolioPageViewPager!!.currentItem-1
            if (idx < 0) idx = 0
            Log.e(LOG_TAG, "CURRENT : $idx" )

            BookmarkTable().insertBookmark(mBookId, contents, idx, bookmarkReadLocator!!.toJson().toString())
        }

        return super.onOptionsItemSelected(item)
    }

    fun setBookmarkPage(data : Intent) {
        val bookmarkImpl = data.getParcelableExtra<BookmarkImpl>(BOOKMARK_ITEM)
        val locator: ReadLocator? = ReadLocator.fromJson(bookmarkImpl.locator)
        currentChapterIndex = bookmarkImpl.chapterNo+1
        mFolioPageViewPager!!.currentItem = currentChapterIndex
        val folioPageFragment = currentFragment ?: return
        folioPageFragment.scrollToCFI(locator!!.locations.cfi!!)
    }

    fun setHighlightPage(data : Intent) {
        val highlightImpl = data.getParcelableExtra<HighlightImpl>(HIGHLIGHT_ITEM)
        currentChapterIndex = highlightImpl.pageNumber+1
        mFolioPageViewPager!!.currentItem = currentChapterIndex
        val folioPageFragment = currentFragment ?: return
        folioPageFragment.scrollToHighlightId(highlightImpl.rangy)
    }

    private fun setupBook() {
        Log.e(LOG_TAG, "-> setupBook")
        try {
            initBook()
            onBookInitSuccess()
        } catch (e: Exception) {
            Log.e(LOG_TAG, "-> Failed to initialize book", e)
            onBookInitFailure()
        }
    }

    @Throws(Exception::class)
    private fun initBook() {
        Log.e(LOG_TAG, "-> initBook")

        bookFileName = FileUtil.getEpubFilename(this, mEpubSourceType!!, mEpubFilePath, mEpubRawId)

        val path = FileUtil.saveEpubFileAndLoadLazyBook(this, mEpubSourceType, mEpubFilePath, mEpubRawId, bookFileName)
        Log.e(LOG_TAG, "path -> $path")
        val extension: Publication.EXTENSION
        var extensionString: String? = null
        try {
            extensionString = FileUtil.getExtensionUppercase(path)
            extension = Publication.EXTENSION.valueOf(extensionString)
        } catch (e: IllegalArgumentException) {
            throw Exception("-> Unknown book file extension ```$extensionString`", e)
        }

        pubBox = when (extension) {
            Publication.EXTENSION.EPUB -> {
                val epubParser = EpubParser()
                epubParser.parse(path!!, "")
            }
            Publication.EXTENSION.CBZ -> {
                val cbzParser = CbzParser()
                cbzParser.parse(path!!, "")
            }
            else -> {
                null
            }
        }

        portNumber = intent.getIntExtra(FolioReader.EXTRA_PORT_NUMBER, DEFAULT_PORT_NUMBER)
        portNumber = AppUtil.getAvailablePortNumber(portNumber)

        r2StreamerServer = Server(portNumber)
        r2StreamerServer!!.addEpub(pubBox!!.publication, pubBox!!.container, "/" + bookFileName!!, null)

        r2StreamerServer!!.start()

        FolioReader.initRetrofit(streamerUrl)
    }

    private fun onBookInitFailure() {
        //TODO -> Fail gracefully
    }

    private fun onBookInitSuccess() {
        val publication = pubBox!!.publication
        spine = publication.readingOrder
        title = publication.metadata.title
        toolbar_title!!.text = title
//        Log.e(LOG_TAG, "cover : ${publication.coverLink!!.href}")
//        Log.e(LOG_TAG, "cover : ${publication.coverLink!!.title}")
//        Log.e(LOG_TAG, "cover : ${publication.coverLink!!.toString()}")

        Log.e(LOG_TAG, "cover : ${spine!!.size}, $spine")
        for (li in spine as MutableList<Link>) {
            Log.e(LOG_TAG, "link : ${li.href}")
        }


        if (mBookId == null) {
            if (!publication.metadata.identifier.isEmpty()) {
                mBookId = publication.metadata.identifier
            } else {
                if (!publication.metadata.title.isEmpty()) {
                    mBookId = publication.metadata.title.hashCode().toString()
                } else {
                    mBookId = bookFileName!!.hashCode().toString()
                }
            }
        }

        // searchUri currently not in use as it's uri is constructed through Retrofit,
        // code kept just in case if required in future.
        for (link in publication.links) {
            if (link.rel.contains("search")) {
                searchUri = Uri.parse("http://" + link.href!!)
                break
            }
        }
        if (searchUri == null)
            searchUri = Uri.parse(streamerUrl + "search")

        configFolio()
        updateTheme()
    }

    override fun getStreamerUrl(): String {
        if (streamerUri == null) {
            streamerUri = Uri.parse(String.format(STREAMER_URL_TEMPLATE, LOCALHOST, portNumber, bookFileName))
        }
        return streamerUri.toString()
    }

    override fun onDirectionChange(newDirection: Config.Direction) {
        Log.e(LOG_TAG, "-> onDirectionChange")

        var folioPageFragment: FolioPageFragment? = currentFragment ?: return
        entryReadLocator = folioPageFragment!!.getLastReadLocator()
        val searchLocatorVisible = folioPageFragment.searchLocatorVisible

        direction = newDirection

        mFolioPageViewPager!!.setDirection(newDirection)
        mFolioPageFragmentAdapter = FolioPageFragmentAdapter(supportFragmentManager, spine, bookFileName, mBookId)
        mFolioPageViewPager!!.adapter = mFolioPageFragmentAdapter
        mFolioPageViewPager!!.currentItem = currentChapterIndex

        folioPageFragment = currentFragment ?: return
        searchLocatorVisible?.let {
            folioPageFragment.highlightSearchLocator(searchLocatorVisible)
        }
    }

    private fun initDistractionFreeMode() {
        Log.e(LOG_TAG, "-> initDistractionFreeMode")
        
        distractionFreeMode = false
        toggleSystemUI()
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        Log.e(LOG_TAG, "-> onPostCreate")

        if (distractionFreeMode) {
            handler!!.post { hideSystemUI() }
        }
    }
    
    override fun getTopDistraction(unit: DisplayUnit): Int {
        var topDistraction = 0
        if (!distractionFreeMode) {
            topDistraction = statusBarHeight
            if (actionBar != null)
                topDistraction += actionBar!!.height
        }

        when (unit) {
            DisplayUnit.PX -> return topDistraction

            DisplayUnit.DP -> {
                topDistraction /= density.toInt()
                return topDistraction
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }
    
    override fun getBottomDistraction(unit: DisplayUnit): Int {
        var bottomDistraction = 0
        if (!distractionFreeMode)
            bottomDistraction = appBarLayout!!.navigationBarHeight

        return when (unit) {
            DisplayUnit.PX -> bottomDistraction

            DisplayUnit.DP -> {
                bottomDistraction /= density.toInt()
                bottomDistraction
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }
    
    private fun computeViewportRect(): Rect {
        val viewportRect = Rect(appBarLayout!!.insets)
        if (distractionFreeMode)
            viewportRect.left = 0
        viewportRect.top = getTopDistraction(DisplayUnit.PX)
        if (distractionFreeMode) {
            viewportRect.right = displayMetrics!!.widthPixels
        } else {
            viewportRect.right = displayMetrics!!.widthPixels - viewportRect.right
        }
        viewportRect.bottom = displayMetrics!!.heightPixels - getBottomDistraction(DisplayUnit.PX)

        return viewportRect
    }

    override fun getViewportRect(unit: DisplayUnit): Rect {
        val viewportRect = computeViewportRect()
        when (unit) {
            DisplayUnit.PX -> return viewportRect

            DisplayUnit.DP -> {
                viewportRect.left /= density.toInt()
                viewportRect.top /= density.toInt()
                viewportRect.right /= density.toInt()
                viewportRect.bottom /= density.toInt()
                return viewportRect
            }

            DisplayUnit.CSS_PX -> {
                viewportRect.left = ceil((viewportRect.left / density).toDouble()).toInt()
                viewportRect.top = ceil((viewportRect.top / density).toDouble()).toInt()
                viewportRect.right = ceil((viewportRect.right / density).toDouble()).toInt()
                viewportRect.bottom = ceil((viewportRect.bottom / density).toDouble()).toInt()
                return viewportRect
            }

            else -> throw IllegalArgumentException("-> Illegal argument -> unit = $unit")
        }
    }

    override fun getActivity(): WeakReference<FolioActivity> {
        return WeakReference(this)
    }

    override fun toggleSystemUI() {
        if (mSearchMenu != null && mSearchMenu!!.visibility == View.VISIBLE) {
            mSearchMenu!!.visibility = View.GONE
        } else if (ll_left_layer != null && ll_left_layer!!.visibility == View.VISIBLE) {
            setMenuForFragment(false)
            ll_left_layer!!.visibility = View.GONE
        } else if (llSetting != null && llSetting!!.visibility == View.VISIBLE) {
            llSetting!!.visibility = View.GONE
            hideDetailSetting()
        } else {
            if (distractionFreeMode) {
                showSystemUI()
            } else {
                hideSystemUI()
            }

            distractionFreeMode = !distractionFreeMode
        }
    }

    private fun showSystemUI() {
        Log.e(LOG_TAG, "-> showSystemUI")

        if (appBarLayout != null) {
            appBarLayout!!.visibility = View.VISIBLE
        }

        if (ll_bottom_bar != null) {
            ll_bottom_bar!!.visibility = View.VISIBLE
        }

        if (ll_bottom_seekbar != null) {
            ll_bottom_seekbar!!.visibility = View.VISIBLE
        }
    }

    private fun hideSystemUI() {
        Log.e(LOG_TAG, "-> hideSystemUI")

        if (appBarLayout != null) {
            appBarLayout!!.visibility = View.GONE
        }

        if (ll_bottom_bar != null) {
            ll_bottom_bar!!.visibility = View.GONE
        }

        if (ll_bottom_seekbar != null) {
            ll_bottom_seekbar!!.visibility = View.GONE
        }
    }

    override fun getEntryReadLocator(): ReadLocator? {
        if (entryReadLocator != null) {
            val tempReadLocator = entryReadLocator
            entryReadLocator = null
            return tempReadLocator
        }
        return null
    }
    
    override fun goToChapter(href: String): Boolean {
        for (link in spine!!) {
            if (href.contains(link.href!!)) {
                currentChapterIndex = spine!!.indexOf(link)
                mFolioPageViewPager!!.currentItem = currentChapterIndex
                val folioPageFragment = currentFragment
                folioPageFragment!!.scrollToFirst()
                folioPageFragment.scrollToAnchorId(href)
                return true
            }
        }
        return false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode.SEARCH.value) {
            Log.e(LOG_TAG, "-> onActivityResult -> " + RequestCode.SEARCH)

            if (resultCode == Activity.RESULT_CANCELED)
                return

            searchAdapterDataBundle = data!!.getBundleExtra(SearchAdapter.DATA_BUNDLE)
            searchQuery = data.getCharSequenceExtra(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY)

            if (resultCode == SearchActivity.ResultCode.ITEM_SELECTED.value) {
                if (mSearchMenu != null && mSearchMenu!!.visibility == View.VISIBLE) {
                    mSearchMenu!!.visibility = View.GONE
                }
                hideSystemUI()

                searchLocator = data.getParcelableExtra(EXTRA_SEARCH_ITEM)

                if (mFolioPageViewPager == null) return
                currentChapterIndex = getChapterIndex(HREF, searchLocator!!.href)
                mFolioPageViewPager!!.currentItem = currentChapterIndex
                val folioPageFragment = currentFragment ?: return
                folioPageFragment.highlightSearchLocator(searchLocator!!)
                searchLocator = null
            }
        } else if (requestCode == RequestCode.CONTENT_HIGHLIGHT.value && resultCode == Activity.RESULT_OK && data!!.hasExtra(TYPE)) {
            val type = data.getStringExtra(TYPE)

            if (type == CHAPTER_SELECTED) {
                goToChapter(data.getStringExtra(SELECTED_CHAPTER_POSITION))
            } else if (type == HIGHLIGHT_SELECTED) {
                val highlightImpl = data.getParcelableExtra<HighlightImpl>(HIGHLIGHT_ITEM)
                currentChapterIndex = highlightImpl.pageNumber
                mFolioPageViewPager!!.currentItem = currentChapterIndex
                val folioPageFragment = currentFragment ?: return
                folioPageFragment.scrollToHighlightId(highlightImpl.rangy)
            } else if(type == BOOKMARK_SELECTED){
                val bookmark = data.getSerializableExtra(BOOKMARK_ITEM) as HashMap<*, *>
                bookmarkReadLocator = ReadLocator.fromJson(bookmark.get("readlocator").toString())
                currentChapterIndex = getChapterIndex(bookmarkReadLocator)
                mFolioPageViewPager!!.currentItem = currentChapterIndex
                val folioPageFragment = currentFragment
                val handler_time = Handler()
                handler_time.postDelayed({
                    folioPageFragment!!.scrollToCFI(bookmarkReadLocator!!.locations.cfi.toString());
                }, 1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (outState != null)
            outState!!.putSerializable(BUNDLE_READ_LOCATOR_CONFIG_CHANGE, lastReadLocator)

        if (r2StreamerServer != null)
            r2StreamerServer!!.stop()

        if (isFinishing) {
            FolioReader.get().retrofit = null
            FolioReader.get().r2StreamerApi = null
        }
    }

    override fun getCurrentChapterIndex(): Int {
        return currentChapterIndex
    }

    private fun configFolio() {
        mFolioPageViewPager = findViewById(R.id.folioPageViewPager)
        mFolioPageViewPager!!.setOnPageChangeListener(object : DirectionalViewpager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                Log.e(LOG_TAG, "-> onPageSelected -> DirectionalViewpager -> position = $position")
                currentChapterIndex = position
            }

            override fun onPageScrollStateChanged(state: Int) {
                if (state == DirectionalViewpager.SCROLL_STATE_IDLE) {
                    val position = mFolioPageViewPager!!.currentItem
                    Log.e(
                        LOG_TAG, "-> onPageScrollStateChanged -> DirectionalViewpager -> " +
                                "position = " + position
                    )

                    var folioPageFragment = mFolioPageFragmentAdapter!!.getItem(position - 1) as FolioPageFragment?
                    if (folioPageFragment != null) {
                        folioPageFragment.scrollToLast()
                        if (folioPageFragment.mWebview != null)
                            folioPageFragment.mWebview!!.dismissPopupWindow()
                    }

                    folioPageFragment = mFolioPageFragmentAdapter!!.getItem(position + 1) as FolioPageFragment?
                    if (folioPageFragment != null) {
                        folioPageFragment.scrollToFirst()
                        if (folioPageFragment.mWebview != null)
                            folioPageFragment.mWebview!!.dismissPopupWindow()
                    }
                }
            }
        })

        mFolioPageViewPager!!.setDirection(direction)
        mFolioPageFragmentAdapter = FolioPageFragmentAdapter(
            supportFragmentManager,
            spine, bookFileName, mBookId
        )
        mFolioPageViewPager!!.adapter = mFolioPageFragmentAdapter

        // In case if SearchActivity is recreated due to screen rotation then FolioActivity
        // will also be recreated, so searchLocator is checked here.
        if (searchLocator != null) {
            currentChapterIndex = getChapterIndex(HREF, searchLocator!!.href)
            mFolioPageViewPager!!.currentItem = currentChapterIndex
            val folioPageFragment = currentFragment ?: return
            folioPageFragment.highlightSearchLocator(searchLocator!!)
            searchLocator = null
        } else {
            val readLocator: ReadLocator?
            if (savedInstanceState == null) {
                readLocator = intent.getParcelableExtra(EXTRA_READ_LOCATOR)
                entryReadLocator = readLocator
            } else {
                readLocator = savedInstanceState!!.getParcelable(BUNDLE_READ_LOCATOR_CONFIG_CHANGE)
                lastReadLocator = readLocator
            }
            currentChapterIndex = getChapterIndex(readLocator)
            mFolioPageViewPager!!.currentItem = currentChapterIndex
        }
    }

    private fun getChapterIndex(readLocator: ReadLocator?): Int {
        if (readLocator == null) {
            return 0
        } else if (!TextUtils.isEmpty(readLocator.href)) {
            return getChapterIndex(HREF, readLocator.href)
        }

        return 0
    }

    private fun getChapterIndex(caseString: String, value: String): Int {
        for (i in spine!!.indices) {
            when (caseString) {
                HREF -> if (spine!![i].href == value)
                    return i
            }
        }
        return 0
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        Log.e(LOG_TAG, "-> onSaveInstanceState")
        this.outState = outState

        outState.putBoolean(BUNDLE_DISTRACTION_FREE_MODE, distractionFreeMode)
        outState.putBundle(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
        outState.putCharSequence(SearchActivity.BUNDLE_SAVE_SEARCH_QUERY, searchQuery)
    }

    override fun storeLastReadLocator(lastReadLocator: ReadLocator) {
        Log.e(LOG_TAG, "-> storeLastReadLocator")
        this.lastReadLocator = lastReadLocator
    }

    private fun setConfig(savedInstanceState: Bundle?) {
        var config: Config?
        val intentConfig = intent.getParcelableExtra<Config>(Config.INTENT_CONFIG)
        val overrideConfig = intent.getBooleanExtra(Config.EXTRA_OVERRIDE_CONFIG, false)
        val savedConfig = AppUtil.getSavedConfig(this)

        if (savedInstanceState != null) {
            config = savedConfig

        } else if (savedConfig == null) {
            if (intentConfig == null) {
                config = Config()
            } else {
                config = intentConfig
            }

        } else {
            if (intentConfig != null && overrideConfig) {
                config = intentConfig
            } else {
                config = savedConfig
            }
        }

        if (config == null)
            config = Config()

        AppUtil.saveConfig(this, config)
        direction = config.direction
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_EXTERNAL_STORAGE_REQUEST -> if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupBook()
            } else {
                Toast.makeText(this, getString(R.string.cannot_access_epub_message), Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    override fun getDirection(): Config.Direction {
        return direction
    }

    private fun clearSearchLocator() {
        Log.e(LOG_TAG, "-> clearSearchLocator")

        val fragments = mFolioPageFragmentAdapter!!.fragments
        for (i in fragments.indices) {
            val folioPageFragment = fragments[i] as FolioPageFragment?
            folioPageFragment?.clearSearchLocator()
        }

        val savedStateList = mFolioPageFragmentAdapter!!.savedStateList
        if (savedStateList != null) {
            for (i in savedStateList.indices) {
                val savedState = savedStateList[i]
                val bundle = FolioPageFragmentAdapter.getBundleFromSavedState(savedState)
                bundle?.putParcelable(FolioPageFragment.BUNDLE_SEARCH_LOCATOR, null)
            }
        }
    }

    fun onMoveCheckedChapter(href: String) {
        goToChapter(href)
        setMenuForFragment(false)
        ll_left_layer!!.visibility = View.GONE
    }
}