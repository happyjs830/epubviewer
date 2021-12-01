package com.folioreader.ui.view

import android.app.AlertDialog
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.ActionMode.Callback
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.widget.PopupWindow
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.view.GestureDetectorCompat
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.DisplayUnit
import com.folioreader.model.HighLight
import com.folioreader.model.HighlightImpl.HighlightStyle
import com.folioreader.model.sqlite.HighLightTable
import com.folioreader.ui.activity.FolioActivity
import com.folioreader.ui.activity.FolioActivityCallback
import com.folioreader.ui.fragment.FolioPageFragment
import com.folioreader.util.AppUtil
import com.folioreader.util.HighlightUtil
import com.folioreader.util.UiUtil
import dalvik.system.PathClassLoader
import kotlinx.android.synthetic.main.text_selection.view.*
import org.json.JSONObject
import org.springframework.util.ReflectionUtils
import java.lang.ref.WeakReference
import kotlin.math.floor

/**
 * @author by mahavir on 3/31/16.
 */
class FolioWebView : WebView {
    companion object {
        val LOG_TAG: String = FolioWebView::class.java.simpleName
        private const val IS_SCROLLING_CHECK_TIMER = 100
        private const val IS_SCROLLING_CHECK_MAX_DURATION = 10000

        @JvmStatic
        fun onWebViewConsoleMessage(cm: ConsoleMessage, LOG_TAG: String, msg: String): Boolean {
            when (cm.messageLevel()) {
                ConsoleMessage.MessageLevel.LOG -> {
                    Log.v(LOG_TAG, msg)
                    return true
                }
                ConsoleMessage.MessageLevel.DEBUG, ConsoleMessage.MessageLevel.TIP -> {
                    Log.d(LOG_TAG, msg)
                    return true
                }
                ConsoleMessage.MessageLevel.WARNING -> {
                    Log.w(LOG_TAG, msg)
                    return true
                }
                ConsoleMessage.MessageLevel.ERROR -> {
                    Log.e(LOG_TAG, msg)
                    return true
                }
                else -> return false
            }
        }
    }

    private var horizontalPageCount = 0
    private var displayMetrics: DisplayMetrics? = null
    private var density: Float = 0.toFloat()
    private var mScrollListener: ScrollListener? = null
    private var mSeekBarListener: SeekBarListener? = null
    private lateinit var gestureDetector: GestureDetectorCompat
    private var eventActionDown: MotionEvent? = null
    private var pageWidthCssDp: Int = 0
    private var pageWidthCssPixels: Float = 0.toFloat()
    private lateinit var webViewPager: WebViewPager
    private lateinit var uiHandler: Handler
    private lateinit var folioActivityCallback: FolioActivityCallback
    private lateinit var parentFragment: FolioPageFragment

    private var actionMode: ActionMode? = null
    private var textSelectionCb: TextSelectionCb? = null
    private var textSelectionCb2: TextSelectionCb2? = null
    private var selectionRect = Rect()
    private val popupRect = Rect()
    private var popupWindow = PopupWindow()
    private lateinit var viewTextSelection: View
    private var isScrollingCheckDuration: Int = 0
    private var isScrollingRunnable: Runnable? = null
    private var oldScrollX: Int = 0
    private var oldScrollY: Int = 0
    private var lastTouchAction: Int = 0
    private var destroyed: Boolean = false
    private var handleHeight: Int = 0

    private var isAlreadyHighlighted: Boolean = false

    private var lastScrollType: LastScrollType? = null

    val contentHeightVal: Int
        get() = floor((this.contentHeight * this.scale).toDouble()).toInt()

    val webViewHeight: Int
        get() = this.measuredHeight

    private enum class LastScrollType {
        USER, PROGRAMMATIC
    }

    @JavascriptInterface
    fun getDirection(): String {
        return folioActivityCallback.direction.toString()
    }

    @JavascriptInterface
    fun getTopDistraction(unitString: String): Int {
        val unit = DisplayUnit.valueOf(unitString)
        return folioActivityCallback.getTopDistraction(unit)
    }

    @JavascriptInterface
    fun getBottomDistraction(unitString: String): Int {
        val unit = DisplayUnit.valueOf(unitString)
        return folioActivityCallback.getBottomDistraction(unit)
    }

    @JavascriptInterface
    fun getViewportRect(unitString: String): String {
        val unit = DisplayUnit.valueOf(unitString)
        val rect = folioActivityCallback.getViewportRect(unit)
        return UiUtil.rectToDOMRectJson(rect)
    }

    @JavascriptInterface
    fun toggleSystemUI() {
        uiHandler.post {
            folioActivityCallback.toggleSystemUI()
        }
    }

    @JavascriptInterface
    fun isPopupShowing(): Boolean {
        return popupWindow.isShowing
    }

    private inner class HorizontalGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            lastScrollType = LastScrollType.USER
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            if (!webViewPager.isScrolling) {
                uiHandler.postDelayed({
                    scrollTo(getScrollXPixelsForPage(webViewPager!!.currentItem), 0)
                }, 100)
            }

            lastScrollType = LastScrollType.USER
            return true
        }

        override fun onDown(event: MotionEvent?): Boolean {
            eventActionDown = MotionEvent.obtain(event)
            super@FolioWebView.onTouchEvent(event)
            return true
        }
    }

    @JavascriptInterface
    fun dismissPopupWindow(): Boolean {
        val wasShowing = popupWindow.isShowing
        if (Looper.getMainLooper().thread == Thread.currentThread()) {
            popupWindow.dismiss()
        } else {
            uiHandler.post { popupWindow.dismiss() }
        }
        selectionRect = Rect()
        uiHandler.removeCallbacks(isScrollingRunnable)
        isScrollingCheckDuration = 0
        return wasShowing
    }

    override fun destroy() {
        super.destroy()
        dismissPopupWindow()
        destroyed = true
    }

    private inner class VerticalGestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onScroll(e1: MotionEvent?, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            lastScrollType = LastScrollType.USER
            return false
        }

        override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
            lastScrollType = LastScrollType.USER
            return false
        }
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    private fun init() {
        uiHandler = Handler()
        displayMetrics = resources.displayMetrics
        density = displayMetrics!!.density

        gestureDetector = if (folioActivityCallback.direction == Config.Direction.HORIZONTAL) {
            GestureDetectorCompat(context, HorizontalGestureListener())
        } else {
            GestureDetectorCompat(context, VerticalGestureListener())
        }

        initViewTextSelection()
    }

    fun initViewTextSelection() {
        val textSelectionMiddleDrawable = ContextCompat.getDrawable(context, R.drawable.abc_text_select_handle_middle_mtrl_dark)
        handleHeight = textSelectionMiddleDrawable?.intrinsicHeight ?: (24 * density).toInt()

        val config = AppUtil.getSavedConfig(context)!!
        val ctw = if (config.isNightMode) {
            ContextThemeWrapper(context, R.style.FolioNightTheme)
        } else {
            ContextThemeWrapper(context, R.style.FolioDayTheme)
        }

        viewTextSelection = LayoutInflater.from(ctw).inflate(R.layout.text_selection, null)
        viewTextSelection.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED)

        viewTextSelection.highlight.setOnClickListener {
            dismissPopupWindow()

            if (isAlreadyHighlighted) {
                // 하이라이트 된 내역 삭제
                loadUrl("javascript:onTextSelectionItemClicked(${it.id})")
            } else {
                // 신규 하이라이트 저장
                onHighlightColorItemsClicked(HighlightStyle.Yellow, false)
            }
        }

        viewTextSelection.bookmark.setOnClickListener {
            Log.e(LOG_TAG, " bookmark setOnClickListener ")
            dismissPopupWindow()
            onBookmarkItemsClicked()
        }

        viewTextSelection.memo.setOnClickListener {
//            dismissPopupWindow()
//            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")

            val dlg: AlertDialog.Builder = AlertDialog.Builder(context)
            dlg.setMessage("준비중입니다.")
            dlg.setPositiveButton("확인") { _, _ -> }
            dlg.show()
        }

        viewTextSelection.share.setOnClickListener {
//            dismissPopupWindow()
//            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")

            val dlg: AlertDialog.Builder = AlertDialog.Builder(context)
            dlg.setMessage("준비중입니다.")
            dlg.setPositiveButton("확인") { _, _ -> }
            dlg.show()
        }

        viewTextSelection.declare.setOnClickListener {
//            dismissPopupWindow()
//            loadUrl("javascript:onTextSelectionItemClicked(${it.id})")

            val dlg: AlertDialog.Builder = AlertDialog.Builder(context)
            dlg.setMessage("준비중입니다.")
            dlg.setPositiveButton("확인") { _, _ -> }
            dlg.show()
        }
    }

    @JavascriptInterface
    fun onTextSelectionItemClicked(id: Int, selectedText: String?) {
        uiHandler.post { loadUrl("javascript:clearSelection()") }

        when (id) {
            R.id.highlight -> {
                uiHandler.post {
                    loadUrl("javascript:deleteThisHighlight()")
                }
            }
            R.id.bookmark -> {
                // 북마크
//                onBookmarkItemsClicked(selectedText!!)
            }
            R.id.memo -> {
                // 메모
//                UiUtil.share(context, selectedText)
            }
            R.id.share -> {
                // 공유
                UiUtil.share(context, selectedText)
            }
            R.id.declare -> {
                // 신고 페이지로 이동
                uiHandler.post {
//                    showDictDialog(selectedText)
                }
            }
            else -> {
                Log.w(LOG_TAG, "-> onTextSelectionItemClicked -> unknown id = $id")
            }
        }
    }

    private fun onBookmarkItemsClicked() {
        Log.e(LOG_TAG, " onBookmarkItemsClicked ")
        parentFragment.bookmark()
        dismissPopupWindow()
    }

    private fun onHighlightColorItemsClicked(style: HighlightStyle, isAlreadyCreated: Boolean) {
        parentFragment.highlight(style, isAlreadyCreated)
        dismissPopupWindow()
    }

    @JavascriptInterface
    fun deleteThisHighlight(id: String?) {
        if (id.isNullOrEmpty())
            return

        val highlightImpl = HighLightTable.getHighlightForRangy(id)
        if (HighLightTable.deleteHighlight(id)) {
            val rangy = HighlightUtil.generateRangyString(parentFragment.pageName)
            uiHandler.post { parentFragment.loadRangy(rangy) }
            if (highlightImpl != null) {
                HighlightUtil.sendHighlightBroadcastEvent(context, highlightImpl, HighLight.HighLightAction.DELETE)
            }
        }
    }

    fun setParentFragment(parentFragment: FolioPageFragment) {
        this.parentFragment = parentFragment
    }

    fun setFolioActivityCallback(folioActivityCallback: FolioActivityCallback) {
        this.folioActivityCallback = folioActivityCallback
        init()
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        pageWidthCssDp = Math.ceil((measuredWidth / density).toDouble()).toInt()
        pageWidthCssPixels = pageWidthCssDp * density
    }

    fun setScrollListener(listener: ScrollListener) {
        mScrollListener = listener
    }

    fun setSeekBarListener(listener: SeekBarListener) {
        mSeekBarListener = listener
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null)
            return false

        lastTouchAction = event.action

        return if (folioActivityCallback.direction == Config.Direction.HORIZONTAL) {
            computeHorizontalScroll(event)
        } else {
            computeVerticalScroll(event)
        }
    }

    private fun computeVerticalScroll(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    private fun computeHorizontalScroll(event: MotionEvent): Boolean {
        if (!::webViewPager.isInitialized)
            return super.onTouchEvent(event)

        webViewPager.dispatchTouchEvent(event)
        val gestureReturn = gestureDetector.onTouchEvent(event)
        return if (gestureReturn) true else super.onTouchEvent(event)
    }

    fun getScrollXDpForPage(page: Int): Int {
        return page * pageWidthCssDp
    }

    fun getScrollXPixelsForPage(page: Int): Int {
        return Math.ceil((page * pageWidthCssPixels).toDouble()).toInt()
    }

    fun setHorizontalPageCount(horizontalPageCount: Int) {
        this.horizontalPageCount = horizontalPageCount

        uiHandler.post {
            webViewPager = (parent as View).findViewById(R.id.webViewPager)
            webViewPager.setHorizontalPageCount(this@FolioWebView.horizontalPageCount)
        }
    }

    override fun scrollTo(x: Int, y: Int) {
        super.scrollTo(x, y)
        lastScrollType = LastScrollType.PROGRAMMATIC
    }

    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        if (mScrollListener != null) mScrollListener!!.onScrollChange(t)
        super.onScrollChanged(l, t, oldl, oldt)

        if (lastScrollType == LastScrollType.USER) {
            parentFragment.searchLocatorVisible = null
        }

        lastScrollType = null
    }

    interface ScrollListener {
        fun onScrollChange(percent: Int)
    }

    interface SeekBarListener {
        fun fadeInSeekBarIfInvisible()
    }

    private inner class TextSelectionCb : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom"),
                    false
                )
            }
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            dismissPopupWindow()
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private inner class TextSelectionCb2 : ActionMode.Callback2() {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menu.clear()
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode) {
            dismissPopupWindow()
        }

        override fun onGetContentRect(mode: ActionMode, view: View, outRect: Rect) {
            evaluateJavascript("javascript:getSelectionRect()") { value ->
                val rectJson = JSONObject(value)
                setSelectionRect(
                    rectJson.getInt("left"), rectJson.getInt("top"),
                    rectJson.getInt("right"), rectJson.getInt("bottom"),
                    false
                )
            }
        }
    }

    override fun startActionMode(callback: Callback): ActionMode {
        textSelectionCb = TextSelectionCb()
        actionMode = super.startActionMode(textSelectionCb)
        actionMode?.finish()

        return actionMode as ActionMode
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun startActionMode(callback: Callback, type: Int): ActionMode {
        textSelectionCb2 = TextSelectionCb2()
        actionMode = super.startActionMode(textSelectionCb2, type)
        actionMode?.finish()

        return actionMode as ActionMode
    }

    private fun applyThemeColorToHandles() {
        if (Build.VERSION.SDK_INT < 23) {
            val folioActivityRef: WeakReference<FolioActivity> = folioActivityCallback.activity
            val mWindowManagerField = ReflectionUtils.findField(FolioActivity::class.java, "mWindowManager")
            mWindowManagerField.isAccessible = true
            val mWindowManager = mWindowManagerField.get(folioActivityRef.get())

            val windowManagerImplClass = Class.forName("android.view.WindowManagerImpl")
            val mGlobalField = ReflectionUtils.findField(windowManagerImplClass, "mGlobal")
            mGlobalField.isAccessible = true
            val mGlobal = mGlobalField.get(mWindowManager)

            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val mViewsField = ReflectionUtils.findField(windowManagerGlobalClass, "mViews")
            mViewsField.isAccessible = true
            val mViews = mViewsField.get(mGlobal) as ArrayList<View>
            val config = AppUtil.getSavedConfig(context)!!

            for (view in mViews) {
                val handleViewClass = Class.forName("com.android.org.chromium.content.browser.input.HandleView")

                if (handleViewClass.isInstance(view)) {
                    val mDrawableField = ReflectionUtils.findField(handleViewClass, "mDrawable")
                    mDrawableField.isAccessible = true
                    val mDrawable = mDrawableField.get(view) as BitmapDrawable
                    UiUtil.setColorIntToDrawable(config.themeColor, mDrawable)
                }
            }
        } else {
            val folioActivityRef: WeakReference<FolioActivity> = folioActivityCallback.activity
            val mWindowManagerField = ReflectionUtils.findField(FolioActivity::class.java, "mWindowManager")
            mWindowManagerField.isAccessible = true
            val mWindowManager = mWindowManagerField.get(folioActivityRef.get())

            val windowManagerImplClass = Class.forName("android.view.WindowManagerImpl")
            val mGlobalField = ReflectionUtils.findField(windowManagerImplClass, "mGlobal")
            mGlobalField.isAccessible = true
            val mGlobal = mGlobalField.get(mWindowManager)

            val windowManagerGlobalClass = Class.forName("android.view.WindowManagerGlobal")
            val mViewsField = ReflectionUtils.findField(windowManagerGlobalClass, "mViews")
            mViewsField.isAccessible = true
            val mViews = mViewsField.get(mGlobal) as ArrayList<View>
            val config = AppUtil.getSavedConfig(context)!!

            for (view in mViews) {
                val popupDecorViewClass = Class.forName("android.widget.PopupWindow\$PopupDecorView")

                if (!popupDecorViewClass.isInstance(view))
                    continue

                val mChildrenField = ReflectionUtils.findField(popupDecorViewClass, "mChildren")
                mChildrenField.isAccessible = true
                val mChildren = mChildrenField.get(view) as kotlin.Array<View>

                val pathClassLoader =
                    PathClassLoader("/system/app/Chrome/Chrome.apk", folioActivityRef.get()?.classLoader)

                val popupTouchHandleDrawableClass = Class.forName(
                    "org.chromium.android_webview.PopupTouchHandleDrawable",
                    true, pathClassLoader
                )

                val mDrawableField = ReflectionUtils.findField(popupTouchHandleDrawableClass, "mDrawable")
                mDrawableField.isAccessible = true
                val mDrawable = mDrawableField.get(mChildren[0]) as Drawable
                UiUtil.setColorIntToDrawable(config.themeColor, mDrawable)
            }
        }
    }

    @JavascriptInterface
    fun setSelectionRect(left: Int, top: Int, right: Int, bottom: Int, already: Boolean) {
        val currentSelectionRect = Rect()
        currentSelectionRect.left = (left * density).toInt()
        currentSelectionRect.top = (top * density).toInt()
        currentSelectionRect.right = (right * density).toInt()
        currentSelectionRect.bottom = (bottom * density).toInt()
        Log.d(LOG_TAG, "-> setSelectionRect -> $currentSelectionRect")

        computeTextSelectionRect(currentSelectionRect)
        uiHandler.post { showTextSelectionPopup(already) }
    }

    private fun computeTextSelectionRect(currentSelectionRect: Rect) {
        val viewportRect = folioActivityCallback.getViewportRect(DisplayUnit.PX)

        if (!Rect.intersects(viewportRect, currentSelectionRect)) {
            uiHandler.post {
                popupWindow.dismiss()
                uiHandler.removeCallbacks(isScrollingRunnable)
            }
            return
        }

        if (selectionRect == currentSelectionRect) {
            return
        }

        selectionRect = currentSelectionRect

        val aboveSelectionRect = Rect(viewportRect)
        aboveSelectionRect.bottom = selectionRect.top - (8 * density).toInt()
        val belowSelectionRect = Rect(viewportRect)
        belowSelectionRect.top = selectionRect.top - 15

        //popupRect initialisation for belowSelectionRect
        popupRect.left = viewportRect.left
        popupRect.top = belowSelectionRect.top
        popupRect.right = popupRect.left + viewTextSelection.measuredWidth
        popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight
        val popupY: Int
        if (belowSelectionRect.contains(popupRect)) {
            popupY = belowSelectionRect.top
        } else {
            // popupRect initialisation for aboveSelectionRect
            popupRect.top = aboveSelectionRect.top
            popupRect.bottom = popupRect.top + viewTextSelection.measuredHeight

            if (aboveSelectionRect.contains(popupRect)) {
                popupY = aboveSelectionRect.bottom - popupRect.height()
            } else {
                val popupYDiff = (viewTextSelection.measuredHeight - selectionRect.height()) / 2
                popupY = selectionRect.top - popupYDiff
            }
        }

        val popupXDiff = (viewTextSelection.measuredWidth - selectionRect.width()) / 2
        val popupX = selectionRect.left - popupXDiff

        popupRect.offsetTo(popupX, popupY)

        if (popupRect.left < viewportRect.left) {
            popupRect.right += 0 - popupRect.left
            popupRect.left = 0
        }

        // Check if popupRect right side is going outside of the viewportRect
        if (popupRect.right > viewportRect.right) {
            val dx = popupRect.right - viewportRect.right
            popupRect.left -= dx
            popupRect.right -= dx
        }
    }

    private fun showTextSelectionPopup(already: Boolean) {
        isAlreadyHighlighted = already

        popupWindow.dismiss()
        oldScrollX = scrollX
        oldScrollY = scrollY

        isScrollingRunnable = Runnable {
            uiHandler.removeCallbacks(isScrollingRunnable)
            val currentScrollX = scrollX
            val currentScrollY = scrollY
            val inTouchMode = lastTouchAction == MotionEvent.ACTION_DOWN ||
                    lastTouchAction == MotionEvent.ACTION_MOVE

            if (oldScrollX == currentScrollX && oldScrollY == currentScrollY && !inTouchMode) {
                if (!already) {
                    viewTextSelection.highlight.text =
                        resources.getString(R.string.layout_text_selection_highlight_add)
                } else {
                    viewTextSelection.highlight.text =
                        resources.getString(R.string.layout_text_selection_highlight_remove)
                }

                popupWindow.dismiss()
                popupWindow = PopupWindow(viewTextSelection, WRAP_CONTENT, WRAP_CONTENT)
                popupWindow.isClippingEnabled = false
                popupWindow.showAtLocation(this@FolioWebView, Gravity.NO_GRAVITY, popupRect.left, popupRect.top)
            } else {
                oldScrollX = currentScrollX
                oldScrollY = currentScrollY
                isScrollingCheckDuration += IS_SCROLLING_CHECK_TIMER
                if (isScrollingCheckDuration < IS_SCROLLING_CHECK_MAX_DURATION && !destroyed)
                    uiHandler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER.toLong())
            }
        }

        uiHandler.removeCallbacks(isScrollingRunnable)
        isScrollingCheckDuration = 0
        if (!destroyed)
            uiHandler.postDelayed(isScrollingRunnable, IS_SCROLLING_CHECK_TIMER.toLong())
    }
}
