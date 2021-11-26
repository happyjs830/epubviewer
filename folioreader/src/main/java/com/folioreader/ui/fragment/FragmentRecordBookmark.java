package com.folioreader.ui.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Constants;
import com.folioreader.R;
import com.folioreader.model.BookmarkImpl;
import com.folioreader.model.sqlite.BookmarkTable;
import com.folioreader.ui.adapter.AdapterRecyclerViewItemBookmark;
import com.folioreader.util.SharedPreferenceUtil;

import java.util.ArrayList;
import java.util.Objects;

public class FragmentRecordBookmark extends Fragment implements AdapterRecyclerViewItemBookmark.BookmarkCallback, AdapterRecyclerViewItemBookmark.BookmarkItemControllCallback {
    private final RecordFragment mParent;

    private RelativeLayout rl_bookmark_empty = null;
    private RelativeLayout rl_bookmark_normal = null;
    private ViewGroup mView = null;

    private Button btn_edit = null;
    private Button btn_sort_page = null;
    private Button btn_sort_register = null;

    private static final String BOOKMARK_ITEM = "bookmark_item";
    private final String LOG_TAG = "BookmarkFragment";
    private String mBookId = "";
    private String mBookTitle = "";

    private RecyclerView mRecyclerView = null;
    private ArrayList<BookmarkImpl> mBookmarkList = null;
    private ArrayList<Integer> mSelectedList = null;
    private boolean isEditMode = false;
    private boolean isAllChecked = false;

    public FragmentRecordBookmark(String bookId, String epubTitle, RecordFragment parent) {
        mBookId = bookId;
        mBookTitle = epubTitle;
        mParent = parent;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup)inflater.inflate(R.layout.fragment_record_bookmark, container,false);
        if (SharedPreferenceUtil.getSharedPreferencesString(getContext(), "SORT_BOOKMARK", "").equals("")) {
            SharedPreferenceUtil.putSharedPreferencesString(getContext(), "SORT_BOOKMARK", "PAGE");
        }

        return mView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(LOG_TAG, "~> onViewCreated");

        mSelectedList = new ArrayList<>();
        mBookmarkList = BookmarkTable.getAllBookmarks(mBookId);
        Log.e(LOG_TAG, "mHighlightList Size : " + mBookmarkList.size());

        // Layout //
        rl_bookmark_empty = mView.findViewById(R.id.rl_bookmark_empty);
        rl_bookmark_normal = mView.findViewById(R.id.rl_bookmark_normal);

        setBookmarkList(mBookmarkList.size() > 0);

        initButton();
        initListener();
        initRecyclerView();
    }

    private void initButton() {
        // Button //
        btn_edit = mView.findViewById(R.id.btn_edit);
        btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEditMode();
            }
        });

        btn_sort_page = mView.findViewById(R.id.btn_sort_page);
        btn_sort_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceUtil.putSharedPreferencesString(getContext(), "SORT_BOOKMARK", "PAGE");
                btn_sort_page.setSelected(true);
                btn_sort_register.setSelected(false);
                sortBookmarkList("PAGE");
            }
        });

        btn_sort_register = mView.findViewById(R.id.btn_sort_register);
        btn_sort_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceUtil.putSharedPreferencesString(getContext(), "SORT_BOOKMARK", "REGISTER");
                btn_sort_page.setSelected(false);
                btn_sort_register.setSelected(true);
                sortBookmarkList("REGISTER");
            }
        });

        String type = SharedPreferenceUtil.getSharedPreferencesString(getContext(), "SORT_BOOKMARK", "");
        if (type.equals("")) {
            type = "PAGE";
        }

        btn_sort_page.setSelected(type.equals("PAGE"));
        btn_sort_register.setSelected(type.equals("REGISTER"));
    }

    private void initListener() {
        mParent.setListener(new RecordFragment.RecordBookmarkListener() {
            @Override
            public void onClickBack() {
                Log.e(LOG_TAG, "initListener onClickBack");
                setEditMode();
            }

            @Override
            public void onClickSelectAll() {
                isAllChecked = !isAllChecked;
                Log.e(LOG_TAG, "initListener onClickSelectAll ::: " + isAllChecked);

                if (!isAllChecked) {
                    mSelectedList.clear();
                } else {
                    mSelectedList.clear();
                    for (int i=0; i<mBookmarkList.size(); i++) {
                        mSelectedList.add(i);
                    }
                }
                Log.e(LOG_TAG, "mSelectedList : " + mSelectedList + ", size : " + mSelectedList.size());
                Log.e(LOG_TAG, "mHighlightList size : " + mBookmarkList.size());

                mParent.setCheckString(isAllChecked);
                mParent.setDeleteString(isAllChecked);
                ((AdapterRecyclerViewItemBookmark) Objects.requireNonNull(mRecyclerView.getAdapter())).setAllItemState(isAllChecked);
            }

            @Override
            public void onClickDelete() {
                Log.e(LOG_TAG, "initListener onClickDelete");
                if (mBookmarkList.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getString(R.string.layout_record_dialog_remove_bookmark, mSelectedList.size()));
                    builder.setPositiveButton(getString(R.string.common_dialog_btn_yes), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            for(int i=mSelectedList.size()-1; i>=0; i--) {
                                ((AdapterRecyclerViewItemBookmark) Objects.requireNonNull(mRecyclerView.getAdapter())).removeList(mSelectedList.get(i));
                            }
                            setBookmarkList(mBookmarkList.size() > 0);
                            setEditMode();
                        }
                    });

                    builder.setNegativeButton(getString(R.string.common_dialog_btn_no), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id) {}
                    });

                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    private void initRecyclerView() {
        // RecyclerView //
        mRecyclerView = mView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        AdapterRecyclerViewItemBookmark adapter = new AdapterRecyclerViewItemBookmark(getActivity(), mBookmarkList, this, this);
        adapter.setItemViewType(0);
        mRecyclerView.setAdapter(adapter);
        sortBookmarkList(SharedPreferenceUtil.getSharedPreferencesString(getContext(), "SORT_BOOKMARK", ""));
    }

    @Override
    public void onItemClick(BookmarkImpl bookmarkImpl) {
        Log.e(LOG_TAG, "getContent : " + bookmarkImpl.getContent());
        Log.e(LOG_TAG, "getPageId : " + bookmarkImpl.getPageId());

        Intent intent = new Intent();
        intent.putExtra("RECORD_TYPE", "BOOKMARK");
        intent.putExtra(BOOKMARK_ITEM, bookmarkImpl);
        intent.putExtra(Constants.TYPE, Constants.BOOKMARK_SELECTED);

        mParent.setIntentToFolioActivity(intent);
    }

    @Override
    public void deleteBookmark(int id) {
        Log.e(LOG_TAG, "deleteBookmark : " + id);
        if (BookmarkTable.deleteBookmark(id)) {
//            EventBus.getDefault().post(new UpdateHighlightEvent());
        }
    }

    @Override
    public void editNote(BookmarkImpl bookmark, int position) {
        Log.e(LOG_TAG, "editNote : " + position);
    }

    @Override
    public void itemClick(int position, BookmarkImpl bookmark) {
        Log.e(LOG_TAG, "onItemClick : " + position);

        FragmentBottomSheetBookmark bottomSheet = new FragmentBottomSheetBookmark(new FragmentBottomSheetBookmark.ItemCallback() {
            @Override
            public void onDelete(int pos) {
                Log.e(LOG_TAG, "FragmentBottomSheetBookmark onDelete : " + pos);

                ((AdapterRecyclerViewItemBookmark) Objects.requireNonNull(mRecyclerView.getAdapter())).removeList(pos);
                setBookmarkList(mBookmarkList.size() > 0);
            }
        }, position);
        bottomSheet.show(requireActivity().getSupportFragmentManager(), "FragmentBottomSheetBookmark");
    }

    @Override
    public void itemCheck(int position, BookmarkImpl bookmark, boolean checked) {
        if (checked) {
            if (!mSelectedList.contains(position)) {
                mSelectedList.add(position);
            }
        } else {
            if (mSelectedList.contains(position)) {
                mSelectedList.remove(Integer.valueOf(position));
            }
        }

        Log.e(LOG_TAG, "mSelectedList : " + mSelectedList + ", size : " + mSelectedList.size());
        Log.e(LOG_TAG, "mBookmarkList size : " + mBookmarkList.size());

        isAllChecked = mSelectedList.size() == mBookmarkList.size();
        mParent.setCheckString(mSelectedList.size() == mBookmarkList.size());
        mParent.setDeleteString(mSelectedList.size() > 0);
    }

    private void setBookmarkList(boolean flag) {
        if (flag) {
            rl_bookmark_normal.setVisibility(View.VISIBLE);
            rl_bookmark_empty.setVisibility(View.GONE);
        } else {
            rl_bookmark_normal.setVisibility(View.GONE);
            rl_bookmark_empty.setVisibility(View.VISIBLE);
        }
    }

    private void sortBookmarkList(String type) {
        if (type.equals("")) {
            type = "PAGE";
        }
        ((AdapterRecyclerViewItemBookmark) Objects.requireNonNull(mRecyclerView.getAdapter())).sortList(type);
    }

    private void setEditMode() {
        isEditMode = !isEditMode;
        btn_edit.setSelected(isEditMode);

        Log.e(LOG_TAG, "onClick : " + isEditMode);

        if (isEditMode) {
            btn_edit.setText(Objects.requireNonNull(getContext()).getString(R.string.layout_record_edit_cancel));
        } else {
            btn_edit.setText(Objects.requireNonNull(getContext()).getString(R.string.layout_record_edit));
            isAllChecked = false;
            mSelectedList.clear();

            mParent.setCheckString(isAllChecked);
            mParent.setDeleteString(isAllChecked);
            ((AdapterRecyclerViewItemBookmark) Objects.requireNonNull(mRecyclerView.getAdapter())).setAllItemState(isAllChecked);
        }
        mParent.setEditMode(isEditMode);
        btn_sort_page.setVisibility((isEditMode) ? View.GONE : View.VISIBLE);
        btn_sort_register.setVisibility((isEditMode) ? View.GONE : View.VISIBLE);

        ((AdapterRecyclerViewItemBookmark) Objects.requireNonNull(mRecyclerView.getAdapter())).setItemViewType(isEditMode ? 1 : 0);
    }

    public boolean getEditMode() {
        return isEditMode;
    }
}
