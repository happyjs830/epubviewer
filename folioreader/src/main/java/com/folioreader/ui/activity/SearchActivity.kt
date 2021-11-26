package com.folioreader.ui.activity

import android.app.AlertDialog
import android.app.SearchManager
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
//import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.folioreader.Config
import com.folioreader.R
import com.folioreader.model.locators.SearchLocator
import com.folioreader.ui.adapter.ListViewType
import com.folioreader.ui.adapter.OnItemClickListener
import com.folioreader.ui.adapter.SearchAdapter
import com.folioreader.ui.view.FolioSearchView
import com.folioreader.util.AppUtil
import com.folioreader.viewmodels.SearchViewModel
import kotlinx.android.synthetic.main.activity_search.*

class SearchActivity : AppCompatActivity(), OnItemClickListener {

    companion object {
        @JvmField
        val LOG_TAG: String = SearchActivity::class.java.simpleName
        const val BUNDLE_SPINE_SIZE = "BUNDLE_SPINE_SIZE"
        const val BUNDLE_SEARCH_URI = "BUNDLE_SEARCH_URI"
        const val BUNDLE_SAVE_SEARCH_QUERY = "BUNDLE_SAVE_SEARCH_QUERY"
        const val BUNDLE_IS_SOFT_KEYBOARD_VISIBLE = "BUNDLE_IS_SOFT_KEYBOARD_VISIBLE"
        const val BUNDLE_FIRST_VISIBLE_ITEM_INDEX = "BUNDLE_FIRST_VISIBLE_ITEM_INDEX"
    }

    enum class ResultCode(val value: Int) {
        ITEM_SELECTED(2),
        BACK_BUTTON_PRESSED(3)
    }

    private var mBackButton: LinearLayoutCompat? = null
    private var mSearchEdit: EditText? = null
    private var mSearchButton: Button? = null

    private var spineSize: Int = 0
    private lateinit var searchUri: Uri
    private lateinit var searchView: FolioSearchView
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var searchAdapter: SearchAdapter
    private lateinit var searchAdapterDataBundle: Bundle
    private var savedInstanceState: Bundle? = null
    private var softKeyboardVisible: Boolean = true
    private lateinit var searchViewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val config: Config = AppUtil.getSavedConfig(this)!!
        if (config.isNightMode) {
            Log.e(LOG_TAG, "night mode")
            setTheme(R.style.FolioNightTheme)
        } else {
            Log.e(LOG_TAG, "day mode")
            setTheme(R.style.FolioDayTheme)
        }

        setContentView(R.layout.activity_search)
        procSearch(config)
        init()
    }

    private fun procSearch(config: Config): Boolean {
        searchView = FolioSearchView(this)
        searchView.init(componentName, config)

        if (savedInstanceState != null) {
            searchView.setQuery(savedInstanceState!!.getCharSequence(BUNDLE_SAVE_SEARCH_QUERY), false)
            softKeyboardVisible = savedInstanceState!!.getBoolean(BUNDLE_IS_SOFT_KEYBOARD_VISIBLE)
            if (!softKeyboardVisible)
                AppUtil.hideKeyboard(this)
        } else {
            val searchQuery: CharSequence? = intent.getCharSequenceExtra(BUNDLE_SAVE_SEARCH_QUERY)
            if (!TextUtils.isEmpty(searchQuery)) {
                searchView.setQuery(searchQuery, false)
                AppUtil.hideKeyboard(this)
                softKeyboardVisible = false
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                softKeyboardVisible = false
                searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    searchViewModel.cancelAllSearchCalls()
                    searchViewModel.init()
                }
                return false
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) softKeyboardVisible = true
        }

        return true
    }

    private fun init() {
        mBackButton = findViewById(R.id.btn_back)
        mBackButton!!.setOnClickListener {
            navigateBack()
        }

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
            click()
        }

        intent.hasExtra("TEXT_FOR_SEARCH").also { it ->
            if (it) {
                intent.getStringExtra("TEXT_FOR_SEARCH").also {
                    mSearchEdit!!.setText(it)
                }
            }
        }

        spineSize = intent.getIntExtra(BUNDLE_SPINE_SIZE, 0)
        searchUri = intent.getParcelableExtra(BUNDLE_SEARCH_URI)

        searchAdapter = SearchAdapter(this)
        searchAdapter.onItemClickListener = this
        linearLayoutManager = LinearLayoutManager(this)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = searchAdapter

        searchViewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)
        searchAdapterDataBundle = searchViewModel.liveAdapterDataBundle.value!!

        val bundleFromFolioActivity = intent.getBundleExtra(SearchAdapter.DATA_BUNDLE)
        if (bundleFromFolioActivity != null) {
            searchViewModel.liveAdapterDataBundle.value = bundleFromFolioActivity
            searchAdapterDataBundle = bundleFromFolioActivity
            searchAdapter.changeDataBundle(bundleFromFolioActivity)
            val position = bundleFromFolioActivity.getInt(BUNDLE_FIRST_VISIBLE_ITEM_INDEX)
            recyclerView.scrollToPosition(position)
        }

        searchViewModel.liveAdapterDataBundle.observe(this, Observer<Bundle> { dataBundle ->
            searchAdapterDataBundle = dataBundle
            searchAdapter.changeDataBundle(dataBundle)
        })

        search()
    }

    private fun click() {
        if (mSearchEdit!!.text.isEmpty()) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("검색어를 입력해주세요.")
            builder.setPositiveButton("확인") { _, _ -> }
            builder.show()
        } else {
            search()
        }
    }

    private fun search() {
        val str: String = mSearchEdit!!.text.toString()
        searchView.setQuery(str, true)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        if (intent.hasExtra(BUNDLE_SEARCH_URI)) {
            searchUri = intent.getParcelableExtra(BUNDLE_SEARCH_URI)
        } else {
            intent.putExtra(BUNDLE_SEARCH_URI, searchUri)
            intent.putExtra(BUNDLE_SPINE_SIZE, spineSize)
        }

        setIntent(intent)

        if (Intent.ACTION_SEARCH == intent.action)
            handleSearch()
    }

    private fun handleSearch() {
        val query: String = intent.getStringExtra(SearchManager.QUERY)
        val newDataBundle = Bundle()
        newDataBundle.putString(ListViewType.KEY, ListViewType.PAGINATION_IN_PROGRESS_VIEW.toString())
        newDataBundle.putParcelableArrayList("DATA", ArrayList<SearchLocator>())
        searchViewModel.liveAdapterDataBundle.value = newDataBundle

        searchViewModel.search(spineSize, query)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)
        outState.putBoolean(BUNDLE_IS_SOFT_KEYBOARD_VISIBLE, softKeyboardVisible)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        this.savedInstanceState = savedInstanceState
    }

    private fun navigateBack() {
        val intent = Intent()
        searchAdapterDataBundle.putInt(BUNDLE_FIRST_VISIBLE_ITEM_INDEX, linearLayoutManager.findFirstVisibleItemPosition())
        intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
        intent.putExtra(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)
        setResult(ResultCode.BACK_BUTTON_PRESSED.value, intent)
        finish()
    }

    override fun onBackPressed() {
        navigateBack()
    }

    override fun onItemClick(adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>, viewHolder: RecyclerView.ViewHolder, position: Int, id: Long) {
        if (adapter is SearchAdapter) {
            if (viewHolder is SearchAdapter.NormalViewHolder) {
                val intent = Intent()
                searchAdapterDataBundle.putInt(BUNDLE_FIRST_VISIBLE_ITEM_INDEX, linearLayoutManager.findFirstVisibleItemPosition())
                intent.putExtra(SearchAdapter.DATA_BUNDLE, searchAdapterDataBundle)
                intent.putExtra(FolioActivity.EXTRA_SEARCH_ITEM, viewHolder.searchLocator as Parcelable)
                intent.putExtra(BUNDLE_SAVE_SEARCH_QUERY, searchView.query)
                setResult(ResultCode.ITEM_SELECTED.value, intent)
                finish()
            }
        }
    }
}