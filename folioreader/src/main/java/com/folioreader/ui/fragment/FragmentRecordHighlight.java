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
import com.folioreader.model.HighlightImpl;
import com.folioreader.model.event.UpdateHighlightEvent;
import com.folioreader.model.sqlite.HighLightTable;
import com.folioreader.ui.adapter.AdapterRecyclerViewItemHighlight;
import com.folioreader.util.SharedPreferenceUtil;

import org.greenrobot.eventbus.EventBus;
import org.readium.r2.shared.Publication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;

public class FragmentRecordHighlight extends Fragment implements AdapterRecyclerViewItemHighlight.HighlightCallback, AdapterRecyclerViewItemHighlight.HighlightItemControllCallback {
    private final RecordFragment mParent;
    private final Publication mPublication;

    private RelativeLayout rl_highlight_empty = null;
    private RelativeLayout rl_highlight_normal = null;
    private ViewGroup mView = null;

    private Button btn_edit = null;
    private Button btn_sort_page = null;
    private Button btn_sort_register = null;

    private static final String HIGHLIGHT_ITEM = "highlight_item";
    private final String LOG_TAG = "HighlightFragment";
    private String mBookId = "";
    private String mBookTitle = "";

    private RecyclerView mRecyclerView = null;
    private ArrayList<HighlightImpl> mHighlightList = null;
    private ArrayList<Integer> mSelectedList = null;
    private boolean isEditMode = false;
    private boolean isAllChecked = false;

    public FragmentRecordHighlight(String bookId, String epubTitle, RecordFragment parent, Publication publication) {
        mBookId = bookId;
        mBookTitle = epubTitle;
        mParent = parent;
        mPublication = publication;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup)inflater.inflate(R.layout.fragment_record_highlight, container,false);
        if (SharedPreferenceUtil.getSharedPreferencesString(getContext(), "SORT_HIGHLIGHT", "").equals("")) {
            SharedPreferenceUtil.putSharedPreferencesString(getContext(), "SORT_HIGHLIGHT", "PAGE");
        }

        return mView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(LOG_TAG, "~> onViewCreated");

        mSelectedList = new ArrayList<>();
        mHighlightList = HighLightTable.getAllHighlights(mBookId);
        Log.e(LOG_TAG, "mHighlightList Size : " + mHighlightList.size());

        // Layout //
        rl_highlight_empty = mView.findViewById(R.id.rl_highlight_empty);
        rl_highlight_normal = mView.findViewById(R.id.rl_highlight_normal);

        setHighlightList(mHighlightList.size() > 0);

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
                setEditMode(!isEditMode);
            }
        });

        btn_sort_page = mView.findViewById(R.id.btn_sort_page);
        btn_sort_page.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceUtil.putSharedPreferencesString(getContext(), "SORT_HIGHLIGHT", "PAGE");
                btn_sort_page.setSelected(true);
                btn_sort_register.setSelected(false);
                sortHighlightList("PAGE");
            }
        });

        btn_sort_register = mView.findViewById(R.id.btn_sort_register);
        btn_sort_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferenceUtil.putSharedPreferencesString(getContext(), "SORT_HIGHLIGHT", "REGISTER");
                btn_sort_page.setSelected(false);
                btn_sort_register.setSelected(true);
                sortHighlightList("REGISTER");
            }
        });

        String type = SharedPreferenceUtil.getSharedPreferencesString(getContext(), "SORT_HIGHLIGHT", "");
        if (type.equals("")) {
            type = "PAGE";
        }

        btn_sort_page.setSelected(type.equals("PAGE"));
        btn_sort_register.setSelected(type.equals("REGISTER"));
    }

    private void initListener() {
        mParent.setListener(new RecordFragment.RecordHighlightListener() {
            @Override
            public void onClickBack() {
                Log.e(LOG_TAG, "initListener onClickBack");
                setEditMode(false);
            }

            @Override
            public void onClickSelectAll() {
                isAllChecked = !isAllChecked;
                Log.e(LOG_TAG, "initListener onClickSelectAll ::: " + isAllChecked);

                if (!isAllChecked) {
                    mSelectedList.clear();
                } else {
                    mSelectedList.clear();
                    for (int i=0; i<mHighlightList.size(); i++) {
                        mSelectedList.add(i);
                    }
                }
                Log.e(LOG_TAG, "mSelectedList : " + mSelectedList + ", size : " + mSelectedList.size());
                Log.e(LOG_TAG, "mHighlightList size : " + mHighlightList.size());

                mParent.setCheckString(isAllChecked);
                mParent.setDeleteString(isAllChecked);
                ((AdapterRecyclerViewItemHighlight) Objects.requireNonNull(mRecyclerView.getAdapter())).setAllItemState(isAllChecked);
            }

            @Override
            public void onClickDelete() {
                Log.e(LOG_TAG, "initListener onClickDelete");
                if (mHighlightList.size() > 0) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage(getString(R.string.layout_record_dialog_remove_highlight, mSelectedList.size()));
                    builder.setPositiveButton(getString(R.string.common_dialog_btn_yes), new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int id)
                        {
                            Collections.sort(mSelectedList, new Comparator<Integer>() {
                                @Override
                                public int compare(Integer o1, Integer o2) {
                                    return Integer.compare(o1, o2);
                                }
                            });

                            for(int i=mSelectedList.size()-1; i>=0; i--) {
                                ((AdapterRecyclerViewItemHighlight) Objects.requireNonNull(mRecyclerView.getAdapter())).removeList(mSelectedList.get(i));
                            }
                            setHighlightList(mHighlightList.size() > 0);
                            mSelectedList.clear();
                            setEditMode(false);
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

        AdapterRecyclerViewItemHighlight adapter = new AdapterRecyclerViewItemHighlight(getActivity(), mPublication, mHighlightList, this, this);
        adapter.setItemViewType(0);
        mRecyclerView.setAdapter(adapter);
        sortHighlightList(SharedPreferenceUtil.getSharedPreferencesString(getContext(), "SORT_HIGHLIGHT", ""));
    }

    @Override
    public void onItemClick(HighlightImpl highlightImpl) {
        Log.e(LOG_TAG, "getContent : " + highlightImpl.getContent());
        Log.e(LOG_TAG, "getPageId : " + highlightImpl.getPageId());

        Intent intent = new Intent();
        intent.putExtra("RECORD_TYPE", "HIGHLIGHT");
        intent.putExtra(HIGHLIGHT_ITEM, highlightImpl);
        intent.putExtra(Constants.TYPE, Constants.HIGHLIGHT_SELECTED);

        mParent.setIntentToFolioActivity(intent);
    }

    @Override
    public void deleteHighlight(int id) {
        Log.e(LOG_TAG, "deleteHighlight : " + id);
        if (HighLightTable.deleteHighlight(id)) {
            EventBus.getDefault().post(new UpdateHighlightEvent());
        }
    }

    @Override
    public void editNote(HighlightImpl highlightImpl, int position) {
        Log.e(LOG_TAG, "editNote : " + position);
    }

    @Override
    public void itemClick(int position, HighlightImpl highlight) {
        Log.e(LOG_TAG, "onItemClick : " + position);

        FragmentBottomSheetHighlight bottomSheet = new FragmentBottomSheetHighlight(new FragmentBottomSheetHighlight.ItemCallback() {
            @Override
            public void onDelete(int pos) {
                Log.e(LOG_TAG, "FragmentBottomSheetHighlight onDelete : " + pos);

                ((AdapterRecyclerViewItemHighlight) Objects.requireNonNull(mRecyclerView.getAdapter())).removeList(pos);
                setHighlightList(mHighlightList.size() > 0);
            }
        }, position);
        bottomSheet.show(requireActivity().getSupportFragmentManager(), "FragmentBottomSheetHighlight");
    }

    @Override
    public void itemCheck(int position, HighlightImpl highlight, boolean checked) {
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
        Log.e(LOG_TAG, "mHighlightList size : " + mHighlightList.size());

        isAllChecked = mSelectedList.size() == mHighlightList.size();
        mParent.setCheckString(mSelectedList.size() == mHighlightList.size());
        mParent.setDeleteString(mSelectedList.size() > 0);
    }

    private void setHighlightList(boolean flag) {
        if (flag) {
            rl_highlight_normal.setVisibility(View.VISIBLE);
            rl_highlight_empty.setVisibility(View.GONE);
        } else {
            rl_highlight_normal.setVisibility(View.GONE);
            rl_highlight_empty.setVisibility(View.VISIBLE);
        }
    }

    private void sortHighlightList(String type) {
        if (type.equals("")) {
            type = "PAGE";
        }
        ((AdapterRecyclerViewItemHighlight) Objects.requireNonNull(mRecyclerView.getAdapter())).sortList(type);
    }

    private void setEditMode(boolean flag) {
        isEditMode = flag;
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
            ((AdapterRecyclerViewItemHighlight) Objects.requireNonNull(mRecyclerView.getAdapter())).setAllItemState(isAllChecked);
        }
        mParent.setEditMode(isEditMode);
        btn_sort_page.setVisibility((isEditMode) ? View.GONE : View.VISIBLE);
        btn_sort_register.setVisibility((isEditMode) ? View.GONE : View.VISIBLE);

        ((AdapterRecyclerViewItemHighlight) Objects.requireNonNull(mRecyclerView.getAdapter())).setItemViewType(isEditMode ? 1 : 0);
    }
}
