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
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.androidbolts.topsheet.TopSheetBehavior
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.folioreader.Config
import com.folioreader.Constants
import com.folioreader.Constants.*
import com.folioreader.FolioReader
import com.folioreader.R
import com.folioreader.model.BookmarkImpl
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighlightImpl
import com.folioreader.model.event.MediaOverlayPlayPauseEvent
import com.folioreader.model.event.ReloadDataEvent
import com.folioreader.model.locators.ReadLocator
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.adapter.AdapterFragmentStateChapterList
import com.folioreader.ui.adapter.FolioPageFragmentAdapter
import com.folioreader.ui.adapter.SearchAdapter
import com.folioreader.ui.fragment.*
import com.folioreader.ui.view.ConfigBottomSheetDialogFragment
import com.folioreader.ui.view.DirectionalViewpager
import com.folioreader.ui.view.FolioAppBarLayout
import com.folioreader.ui.view.MediaControllerCallback
import com.folioreader.util.AppUtil
import com.folioreader.util.FileUtil
import com.folioreader.util.UiUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.folio_activity.*
import org.greenrobot.eventbus.EventBus
import org.readium.r2.shared.Contributor
import org.readium.r2.shared.Link
import org.readium.r2.shared.Publication
import org.readium.r2.streamer.parser.CbzParser
import org.readium.r2.streamer.parser.EpubParser
import org.readium.r2.streamer.parser.PubBox
import org.readium.r2.streamer.server.Server
import java.lang.ref.WeakReference
import kotlin.math.ceil

class FolioActivity : AppCompatActivity(), FolioActivityCallback, MediaControllerCallback{

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
    private var btnList: Button? = null
    private var btnRecord: Button? = null
    private var btnComment: Button? = null
    private var btnReview: Button? = null

    private var currentChapterIndex: Int = 0
    private var mFolioPageFragmentAdapter: FolioPageFragmentAdapter? = null
    private var entryReadLocator: ReadLocator? = null
    private var lastReadLocator: ReadLocator? = null
    private var outState: Bundle? = null
    private var savedInstanceState: Bundle? = null

    private var r2StreamerServer: Server? = null
    private var pubBox: PubBox? = null
    private var spine: List<Link>? = null

    private var mBookId: String? = null
    private var mEpubFilePath: String? = null
    private var mEpubSourceType: EpubSourceType? = null
    private var mEpubRawId = 0
    private var mediaControllerFragment: MediaControllerFragment? = null
    private var direction: Config.Direction = Config.Direction.VERTICAL
    private var portNumber: Int = Constants.DEFAULT_PORT_NUMBER
    private var streamerUri: Uri? = null

    // Search //
    private var mSearchMenu: ConstraintLayout? = null
    private var mSearchButton: Button? = null
    private var mSearchEdit: EditText? = null

    private var searchUri: Uri? = null
    private var searchAdapterDataBundle: Bundle? = null
    private var searchQuery: CharSequence? = null
    private var searchLocator: SearchLocator? = null

    private var topBar: TopSheetBehavior<View>? = null
    private var leftLayout: ConstraintLayout? = null
    private var leftLayoutTitle: TextView? = null
    private var leftLayoutAuthor: TextView? = null
    private var leftLayoutDesc: TextView? = null
    private var viewPager: ViewPager2? = null
    private var btn_more: Button? = null

    private var displayMetrics: DisplayMetrics? = null
    private var density: Float = 0.toFloat()
    private var topActivity: Boolean? = null
    private var taskImportance: Int = 0

    private var llSetting: ConstraintLayout? = null
    private var llSettingDetail: RelativeLayout? = null
    private var btnLayerExpand: Button? = null

    // Setting //
    private var params: WindowManager.LayoutParams? = null
    private lateinit var mConfig: Config
    // FontSize //
    private var btnFontSizeMinus: ImageButton? = null
    private var tvFontSizeMinus: Button? = null
    private var btnFontSizePlus: ImageButton? = null
    private var tvFontSizePlus: Button? = null
    private var tvFontSizeCenter: TextView? = null
    // FontLineSpace //
    private var btnFontLineSpaceMinus: ImageButton? = null
    private var tvFontLineSpaceMinus: Button? = null
    private var btnFontLineSpacePlus: ImageButton? = null
    private var tvFontLineSpacePlus: Button? = null
    private var tvFontLineSpaceCenter: TextView? = null
    // FontWhiteSpace //
    private var btnFontWhiteSpaceMinus: ImageButton? = null
    private var tvFontWhiteSpaceMinus: Button? = null
    private var btnFontWhiteSpacePlus: ImageButton? = null
    private var tvFontWhiteSpacePlus: Button? = null
    private var tvFontWhiteSpaceCenter: TextView? = null
    // Alignment //
    private var alignment_left: Button? = null
    private var alignment_both: Button? = null
    // Page //
    private var page_tb: Button? = null
    private var page_lr: Button? = null
    private var page_scroll: Button? = null
    // Theme //
    private var btnThemeWhite: ImageView? = null
    private var btnThemeGray: ImageView? = null
    private var btnThemeGreen: ImageView? = null
    private var btnThemeWood: ImageView? = null
    private var btnThemeBlack: ImageView? = null
    // Screen Filter //
    private var iv_screen_fliter: ImageView? = null

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
        private const val BOOKMARK_ITEM = "bookmark_item"
        private const val HIGHLIGHT_ITEM = "highlight_item"
    }

    private val closeBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(LOG_TAG, "-> closeBroadcastReceiver -> onReceive -> " + intent.action!!)

            val action = intent.action
            if (action != null && action == FolioReader.ACTION_CLOSE_FOLIOREADER) {

                try {
                    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                    val tasks = activityManager.runningAppProcesses
                    taskImportance = tasks[0].importance
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "-> ", e)
                }

                val closeIntent = Intent(applicationContext, FolioActivity::class.java)
                closeIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                closeIntent.action = FolioReader.ACTION_CLOSE_FOLIOREADER
                this@FolioActivity.startActivity(closeIntent)
            }
        }
    }

    val statusBarHeight: Int
        get() {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0)
                result = resources.getDimensionPixelSize(resourceId)
            return result
        }

    private val searchReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.e(LOG_TAG, "-> searchReceiver -> onReceive -> " + intent.action!!)

            val action = intent.action ?: return
            when (action) {
                ACTION_SEARCH_CLEAR -> clearSearchLocator()
            }
        }
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

    private enum class RequestCode private constructor(internal val value: Int) {
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
                // FolioActivity was already left, so no need to broadcast ReadLocator again.
                // Finish activity without going through onPause() and onStop()
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
            // FolioActivity is topActivity, so need to broadcast ReadLocator.
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
        initMediaController()
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
        appBarLayout = findViewById(R.id.appBarLayout)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        actionBar = supportActionBar
        actionBar!!.elevation = 0f
        toolbar_title = findViewById(R.id.toolbar_title)

        // Setting //
        llSetting = findViewById(R.id.ll_setting_layer)
        llSettingDetail = findViewById(R.id.ll_top_setting_detail)
        btnLayerExpand = findViewById(R.id.btnLayerExpand)
        btnLayerExpand!!.setOnClickListener {
            llSettingDetail!!.visibility = if (llSettingDetail!!.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            btnLayerExpand!!.isSelected = llSettingDetail!!.visibility == View.VISIBLE

            btnLayerExpand!!.setText(R.string.layout_setting_detail_open)
        }

        // Search //
        mSearchMenu = findViewById(R.id.ll_menu_search)
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

        val drawable = ContextCompat.getDrawable(this, R.drawable.ic_nav_back)
        UiUtil.setColorIntToDrawable(resources.getColor(R.color.layout_top_button_back), drawable!!)
        toolbar!!.navigationIcon = drawable

        if (config.isNightMode) {
            setNightMode()
        } else {
            setDayMode()
        }

//        val color: Int = if (config.isNightMode) {
//            ContextCompat.getColor(this, R.color.black)
//        } else {
//            val attrs = intArrayOf(android.R.attr.navigationBarColor)
//            val typedArray = theme.obtainStyledAttributes(attrs)
//            typedArray.getColor(0, ContextCompat.getColor(this, R.color.white))
//        }
//        window.navigationBarColor = Color.BLACK
    }

    private fun initBottomBar() {
        ll_bottom_bar = findViewById(R.id.ll_bottom_bar)
        fl_fragment_page = findViewById(R.id.fl_fragment_page)

        btnList = findViewById(R.id.btn_list)
        btnList!!.setOnClickListener {
            setMenuForFragment(false)
            btnList!!.isSelected = true

            val bundle = Bundle()
            bundle.putSerializable(PUBLICATION, pubBox!!.publication)
            bundle.putString(CHAPTER_SELECTED, spine!![currentChapterIndex].href)
            bundle.putString(FolioReader.EXTRA_BOOK_ID, mBookId)
            bundle.putString(BOOK_TITLE, bookFileName)

            val adapter = AdapterFragmentStateChapterList(this, bundle)
            viewPager!!.adapter = adapter

            // TabLayout //
            val tlIndex: TabLayout = findViewById(R.id.tl_index)
            TabLayoutMediator(tlIndex, viewPager!!) { tab, position ->
                if (position == 0) {
                    tab.text = getString(R.string.layout_left_info_index)
                } else {
                    tab.text = getString(R.string.layout_left_info_list)
                }
            }.attach()

            ll_left_layer.visibility = View.VISIBLE

            leftLayoutTitle!!.text = pubBox!!.publication.metadata.title

            for ((index, item) in pubBox!!.publication.metadata.authors.withIndex()) {
                Log.e(LOG_TAG,"$index = ${item.name}")
                leftLayoutAuthor!!.text = item.name
            }
            leftLayoutDesc!!.text = if (pubBox!!.publication.metadata.description != "") pubBox!!.publication.metadata.description else ""
        }

        btnRecord = findViewById(R.id.btn_record)
        btnRecord!!.setOnClickListener {
            Log.e(LOG_TAG, "itemRecord")
            setMenuForFragment(true)
            btnRecord!!.isSelected = true

            val fragment = RecordFragment()

            val bundle = Bundle()
            bundle.putSerializable(PUBLICATION, pubBox!!.publication)
            bundle.putString(CHAPTER_SELECTED, spine!![currentChapterIndex].href)
            bundle.putString(FolioReader.EXTRA_BOOK_ID, mBookId)
            bundle.putString(BOOK_TITLE, bookFileName)
            fragment.arguments = bundle

            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_page, fragment).commit()
        }

        btnComment = findViewById(R.id.btn_comment)
        btnComment!!.setOnClickListener {
            Log.e(LOG_TAG, "itemComment")
            setMenuForFragment(true)
            btnComment!!.isSelected = true

            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_page, CommentFragment()).commit()
        }

        btnReview = findViewById(R.id.btn_review)
        btnReview!!.setOnClickListener {
            Log.e(LOG_TAG, "itemReview")
            setMenuForFragment(true)
            btnReview!!.isSelected = true

            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment_page, ReviewFragment()).commit()
        }
    }

    private fun initLeftLayout() {
        leftLayout = findViewById(R.id.ll_left_layer)
        leftLayout!!.isClickable = true

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

        viewPager = findViewById(R.id.viewPager)
        viewPager!!.isUserInputEnabled = false
    }

    private fun initSetting() {
        mConfig = AppUtil.getSavedConfig(this)!!

        // FontStyle //
        val fontSpinner: Spinner = findViewById(R.id.spinner_font)

        val adapter: ArrayAdapter<String> = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item,
            resources.getStringArray(R.array.array_font_style)
        )
        fontSpinner.adapter = adapter
        fontSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                Log.e(LOG_TAG, "position: $position, id : $id")

                mConfig.font = position+5
                AppUtil.saveConfig(this@FolioActivity, mConfig)
                EventBus.getDefault().post(ReloadDataEvent())
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}

        }

        // FontSize //
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
        setTheme()
        // Brightness //
        params = window.attributes
        val brightnessSeekbar: SeekBar = findViewById(R.id.brightness_seekbar)
        brightnessSeekbar.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                Log.e(LOG_TAG, "")
                if (fromUser) {
                    Log.e(LOG_TAG, "progress : $progress")

                    var pt: Int = progress
                    if (progress < 10) {
                        pt = 10
                    } else if (progress > 100) {
                        pt = 100
                    }
                    params!!.screenBrightness = (pt/100).toFloat()
                    window.attributes = params
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        iv_screen_fliter = findViewById(R.id.iv_screen_fliter)
        val btnBlueFilter: ImageButton = findViewById(R.id.btnBlueFilter)
        btnBlueFilter.setOnClickListener {
            Log.e(LOG_TAG, "setOnClickListener : btnBlueFilter")
            btnBlueFilter.isSelected = !btnBlueFilter.isSelected

            iv_screen_fliter!!.visibility = if (btnBlueFilter.isSelected) View.VISIBLE else View.GONE
        }
    }

    private fun setFontSize(type:Int = 0) {
        var change: Boolean = false
        if (type == -1) {
            // 크기 감소
            if (mConfig.fontSize > 1) {
                mConfig.fontSize = mConfig.fontSize - 1
                change = true
            }
        } else if (type == 1) {
            // 크기 증가
            if (mConfig.fontSize < 19) {
                mConfig.fontSize = mConfig.fontSize + 1
                change = true
            }
        }
        Log.e(LOG_TAG, "type =>>> $type")

        if (change) {
            AppUtil.saveConfig(this, mConfig)
            EventBus.getDefault().post(ReloadDataEvent())
        }

        tvFontSizeMinus!!.text = if (mConfig.fontSize <= 1) "" else (mConfig.fontSize-1).toString()
        tvFontSizeCenter!!.text = (mConfig.fontSize).toString()
        tvFontSizePlus!!.text = if (mConfig.fontSize > 18) "" else (mConfig.fontSize+1).toString()
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
            AppUtil.saveConfig(this, mConfig)
            EventBus.getDefault().post(ReloadDataEvent())
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
            AppUtil.saveConfig(this, mConfig)
            EventBus.getDefault().post(ReloadDataEvent())
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
        Log.e(LOG_TAG, "t =>>> $t")

        mConfig.alignment = t
        AppUtil.saveConfig(this, mConfig)
        EventBus.getDefault().post(ReloadDataEvent())

        if (t == "LEFT") {
            alignment_left!!.isSelected = true
        } else if (t == "BOTH") {
            alignment_both!!.isSelected = true
        }
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
        EventBus.getDefault().post(ReloadDataEvent())

        if (t == "TB") {
            page_tb!!.isSelected = true
        } else if (t == "LR") {
            page_lr!!.isSelected = true
        } else if (t == "SCROLL") {
            page_scroll!!.isSelected = true
        }
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

        if (t == "WHITE" || t == "GRAY") {
//            setTheme(R.style.FolioDayTheme)
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else {
//            setTheme(R.style.FolioNightTheme)
//            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        mConfig.currentTheme = t
        AppUtil.saveConfig(this, mConfig)
        EventBus.getDefault().post(ReloadDataEvent())

        when (t) {
            "WHITE" -> btnThemeWhite!!.isSelected = true
            "GRAY" -> btnThemeGray!!.isSelected = true
            "GREEN" -> btnThemeGreen!!.isSelected = true
            "WOOD" -> btnThemeWood!!.isSelected = true
            "BLACK" -> btnThemeBlack!!.isSelected = true
            else -> { // Note the block
                print("x is neither 1 nor 2")
            }
        }
    }

    fun setMenuForFragment(flag: Boolean) {
        btnList!!.isSelected = false
        btnRecord!!.isSelected = false
        btnComment!!.isSelected = false
        btnReview!!.isSelected = false

        if (fl_fragment_page != null) {
            if (flag) {
                fl_fragment_page!!.visibility = View.VISIBLE
            } else {
                fl_fragment_page!!.visibility = View.GONE
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

    private fun initMediaController() {
        Log.e(LOG_TAG, "-> initMediaController")

        mediaControllerFragment = MediaControllerFragment.getInstance(supportFragmentManager, this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val itemId = item.itemId

        if (itemId == android.R.id.home) {
            Log.e(LOG_TAG, "-> onOptionsItemSelected -> drawer")
            finish()
//            startContentHighlightActivity()
            return true
        } else if (itemId == R.id.itemSetting) {
            Log.e(LOG_TAG, "-> onOptionsItemSelected -> " + item.title)

            if (llSetting!!.visibility == View.VISIBLE) {
                llSetting!!.visibility = View.GONE
                llSettingDetail!!.visibility = View.GONE
                btnLayerExpand!!.isSelected = false
            } else {
                llSetting!!.visibility = View.VISIBLE
                llSetting!!.isClickable = true
            }

//            showConfigBottomSheetDialogFragment()
            return true
        } else if (itemId == R.id.itemSearch) {
            Log.e(LOG_TAG, "-> onOptionsItemSelected -> " + item.title)
            if (mSearchMenu!!.visibility == View.VISIBLE) {
                mSearchMenu!!.visibility = View.GONE
            } else {
                mSearchMenu!!.visibility = View.VISIBLE
                mSearchMenu!!.isClickable = true
            }
            return true
        } else if (itemId == R.id.itemBookmark) {
            Log.e(LOG_TAG, "-> onOptionsItemSelected -> " + item.title)
            Log.e(LOG_TAG, "mFolioPageViewPager!!.currentItem :::::" + mFolioPageViewPager!!.currentItem.toString())
            showConfigBottomSheetDialogFragment()

        }

        return super.onOptionsItemSelected(item)
    }

    fun startContentHighlightActivity() {
        val intent = Intent(this@FolioActivity, ContentHighlightActivity::class.java)

        intent.putExtra(Constants.PUBLICATION, pubBox!!.publication)
        try {
            intent.putExtra(CHAPTER_SELECTED, spine!![currentChapterIndex].href)
        } catch (e: NullPointerException) {
            Log.w(LOG_TAG, "-> ", e)
            intent.putExtra(CHAPTER_SELECTED, "")
        } catch (e: IndexOutOfBoundsException) {
            Log.w(LOG_TAG, "-> ", e)
            intent.putExtra(CHAPTER_SELECTED, "")
        }

        intent.putExtra(FolioReader.EXTRA_BOOK_ID, mBookId)
        intent.putExtra(Constants.BOOK_TITLE, bookFileName)

        startActivityForResult(intent, RequestCode.CONTENT_HIGHLIGHT.value)
        overridePendingTransition(0, 0)
    }

    fun setBookmarkPage(data : Intent) {
        val bookmarkImpl = data.getParcelableExtra<BookmarkImpl>(BOOKMARK_ITEM)
        Log.e(LOG_TAG, "bookmarkImpl : $bookmarkImpl")
        currentChapterIndex = bookmarkImpl.pageNumber
        mFolioPageViewPager!!.currentItem = currentChapterIndex
        val folioPageFragment = currentFragment ?: return
        folioPageFragment.scrollToHighlightId(bookmarkImpl.rangy)
    }

    fun setHighlightPage(data : Intent) {
        val highlightImpl = data.getParcelableExtra<HighlightImpl>(HIGHLIGHT_ITEM)
        Log.e(LOG_TAG, "highlightImpl : $highlightImpl")
        currentChapterIndex = highlightImpl.pageNumber
        mFolioPageViewPager!!.currentItem = currentChapterIndex
        val folioPageFragment = currentFragment ?: return
        folioPageFragment.scrollToHighlightId(highlightImpl.rangy)
    }

    fun showConfigBottomSheetDialogFragment() {
        ConfigBottomSheetDialogFragment().show(
            supportFragmentManager,
            ConfigBottomSheetDialogFragment.LOG_TAG
        )
    }

    fun showMediaController() {
        mediaControllerFragment!!.show(supportFragmentManager)
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
        val extension: Publication.EXTENSION
        var extensionString: String? = null
        try {
            extensionString = FileUtil.getExtensionUppercase(path)
            extension = Publication.EXTENSION.valueOf(extensionString)
        } catch (e: IllegalArgumentException) {
            throw Exception("-> Unknown book file extension `$extensionString`", e)
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

        portNumber = intent.getIntExtra(FolioReader.EXTRA_PORT_NUMBER, Constants.DEFAULT_PORT_NUMBER)
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

        when (unit) {
            DisplayUnit.PX -> return bottomDistraction

            DisplayUnit.DP -> {
                bottomDistraction /= density.toInt()
                return bottomDistraction
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
        Log.e(LOG_TAG, "-> toggleSystemUI -> distractionFreeMode = $distractionFreeMode")

        if (mSearchMenu != null && mSearchMenu!!.visibility == View.VISIBLE) {
            mSearchMenu!!.visibility = View.GONE
        } else if (ll_left_layer != null && ll_left_layer!!.visibility == View.VISIBLE) {
            setMenuForFragment(false)
            ll_left_layer!!.visibility = View.GONE
        } else if (llSetting != null && llSetting!!.visibility == View.VISIBLE) {
            llSetting!!.visibility = View.GONE
            llSettingDetail!!.visibility = View.GONE
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
    }

    private fun hideSystemUI() {
        Log.e(LOG_TAG, "-> hideSystemUI")

        if (appBarLayout != null) {
            appBarLayout!!.visibility = View.GONE
        }

        if (ll_bottom_bar != null) {
            ll_bottom_bar!!.visibility = View.GONE
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
        // Replacing with addOnPageChangeListener(), onPageSelected() is not invoked
        mFolioPageViewPager!!.setOnPageChangeListener(object : DirectionalViewpager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override fun onPageSelected(position: Int) {
                Log.e(LOG_TAG, "-> onPageSelected -> DirectionalViewpager -> position = $position")

                EventBus.getDefault().post(
                    MediaOverlayPlayPauseEvent(
                        spine!![currentChapterIndex].href, false, true
                    )
                )
                mediaControllerFragment!!.setPlayButtonDrawable()
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

    override fun play() {
        EventBus.getDefault().post(
            MediaOverlayPlayPauseEvent(
                spine!![currentChapterIndex].href, true, false
            )
        )
    }

    override fun pause() {
        EventBus.getDefault().post(
            MediaOverlayPlayPauseEvent(
                spine!![currentChapterIndex].href, false, false
            )
        )
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