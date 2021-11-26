package com.folioreader.ui.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.R;
import com.folioreader.ui.activity.FolioActivity;
import com.folioreader.ui.adapter.AdapterFragmentStateRecord;
import com.folioreader.util.UiUtil;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.readium.r2.shared.Contributor;
import org.readium.r2.shared.Publication;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RecordFragment extends Fragment {
    private final String LOG_TAG = "RecordActivity";

    // Main View //
    private ViewGroup mView = null;

    // EditTopBar //
    private ConstraintLayout mEditTopBar = null;
    private Button btnSelect = null;
    private Button btnDelete = null;

    // ViewPager //
    private ViewPager2 viewPager = null;

    // Data //
    private Publication mPublication = null;

    // Contents //
    private ImageView iv_book_bg = null;
    private ImageView iv_book_img = null;

    private int mCurrentViewPager = -1;

    public interface RecordBookmarkListener {
        void onClickBack();
        void onClickSelectAll();
        void onClickDelete();
    }

    public interface RecordHighlightListener {
        void onClickBack();
        void onClickSelectAll();
        void onClickDelete();
    }

    public interface RecordMemoListener {
        void onClickBack();
        void onClickSelectAll();
        void onClickDelete();
    }

    private RecordBookmarkListener mBookmarkListener;
    private RecordHighlightListener mHighlightListener;
    private RecordMemoListener mMemoListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup)inflater.inflate(R.layout.activity_record, container, false);
        mView.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        // Touch Controll //
        mView.setClickable(true);

        if (getArguments() != null) {
            mPublication = (Publication) getArguments().getSerializable(Constants.PUBLICATION);
        }
        Log.e(LOG_TAG, "mPublication : " + mPublication);

        initTop();
        initButtom();
        initBotom();

        return mView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (mEditTopBar.getVisibility() == View.VISIBLE) {
                    if (mBookmarkListener != null) {
                        mBookmarkListener.onClickBack();
                    }

                    if (mHighlightListener != null) {
                        mHighlightListener.onClickBack();
                    }

                    if (mMemoListener != null) {
                        mMemoListener.onClickBack();
                    }

                    setEditMode(false);
                } else {
                    close();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void initTop() {
        Log.e(LOG_TAG, "-> initTop");

        mEditTopBar = mView.findViewById(R.id.ll_edit_top);

        if (getArguments() != null) {
            ((TextView)mView.findViewById(R.id.tv_record_title)).setText(mPublication.getMetadata().getTitle());
        }

        for (Contributor cb : mPublication.getMetadata().getAuthors()) {
            ((TextView)mView.findViewById(R.id.tv_record_author)).setText(cb.getName());
        }

//        iv_book_bg.setBackground();
//        iv_book_img.setImageDrawable(Drawable.createFromPath(mPublication.getCoverLink().getTypeLink()));

        mView.findViewById(R.id.iv_record_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, "iv_record_share SHARE BOOK!");
                AlertDialog.Builder msgBuilder = new AlertDialog.Builder(getActivity())
                        .setMessage("준비중입니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {}
                        });

                AlertDialog dlg = msgBuilder.create();
                dlg.show();
            }
        });
    }

    private void initButtom() {
        mView.findViewById(R.id.btn_edit_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentViewPager == 0) {
                    if (mBookmarkListener != null) {
                        mBookmarkListener.onClickBack();
                    }
                } else if (mCurrentViewPager == 1) {
                    if (mHighlightListener != null) {
                        mHighlightListener.onClickBack();
                    }
                } else {
                    if (mMemoListener != null) {
                        mMemoListener.onClickBack();
                    }
                }
                setEditMode(false);
            }
        });

        btnSelect = mView.findViewById(R.id.btn_select);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentViewPager == 0) {
                    if (mBookmarkListener != null) {
                        mBookmarkListener.onClickSelectAll();
                    }
                } else if (mCurrentViewPager == 1) {
                    if (mHighlightListener != null) {
                        mHighlightListener.onClickSelectAll();
                    }
                } else {
                    if (mMemoListener != null) {
                        mMemoListener.onClickSelectAll();
                    }
                }
            }
        });

        btnDelete = mView.findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCurrentViewPager == 0) {
                    if (mBookmarkListener != null) {
                        mBookmarkListener.onClickDelete();
                    }
                } else if (mCurrentViewPager == 1) {
                    if (mHighlightListener != null) {
                        mHighlightListener.onClickDelete();
                    }
                } else {
                    if (mMemoListener != null) {
                        mMemoListener.onClickDelete();
                    }
                }
            }
        });
    }

    private void initBotom() {
        Log.e(LOG_TAG, "-> initBotom");

        Bundle bundle = new Bundle();
        if (getArguments() != null) {
            String bookID = getArguments().getString(FolioReader.EXTRA_BOOK_ID);
            String bookTitle = getArguments().getString(Constants.BOOK_TITLE);
            Log.e(LOG_TAG, "bookID : " + bookID + ", bookTitle : " + bookTitle);

            bundle.putString(FolioReader.EXTRA_BOOK_ID, bookID);
            bundle.putString(Constants.BOOK_TITLE, bookTitle);
        }

        // ViewPager //
        viewPager = mView.findViewById(R.id.viewPager);
        viewPager.setUserInputEnabled(false);
        FragmentStateAdapter adapter = new AdapterFragmentStateRecord(Objects.requireNonNull(getActivity()), bundle, this);
        viewPager.setAdapter(adapter);

        // TabLayout //
        TabLayout tl_record = mView.findViewById(R.id.tl_record);
        tl_record.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mCurrentViewPager = tab.getPosition();

                if (mEditTopBar.getVisibility() == View.VISIBLE) {
                    if (tab.getPosition() == 0) {
                        if (mBookmarkListener != null) {
                            mBookmarkListener.onClickBack();
                        }
                    } else if (tab.getPosition() == 1) {
                        if (mHighlightListener != null) {
                            mHighlightListener.onClickBack();
                        }
                    } else {
                        if (mMemoListener != null) {
                            mMemoListener.onClickBack();
                        }
                    }
                }
                setEditMode(false);

                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void close() {
        FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        manager.beginTransaction().remove(this).commit();
        manager.popBackStack();

        ((FolioActivity)getActivity()).setMenuForFragment(false);
    }

    public void setEditMode(boolean mode) {
        Log.e(LOG_TAG, "mode : " + mode);
        mEditTopBar.setVisibility(mode ? View.VISIBLE : View.GONE);
        mEditTopBar.setClickable(true);
    }

    public void setListener(RecordBookmarkListener listener) {
        mBookmarkListener = listener;
    }

    public void setListener(RecordHighlightListener listener) {
        mHighlightListener = listener;
    }

    public void setListener(RecordMemoListener listener) {
        mMemoListener = listener;
    }

    public void setCheckString(boolean mode) {
        if (mode) {
            btnSelect.setText(R.string.layout_record_edit_cancel_all);
        } else {
            btnSelect.setText(R.string.layout_record_edit_select_all);
        }
    }

    public void setDeleteString(boolean mode) {
        btnDelete.setSelected(mode);
        btnDelete.setEnabled(mode);
    }

    public void setIntentToFolioActivity(Intent intent) {
        close();

        Log.e(LOG_TAG, " RECORD_TYPE : " + intent.getStringExtra("RECORD_TYPE"));
        if (intent.getStringExtra("RECORD_TYPE").equals("BOOKMARK")) {
            ((FolioActivity) Objects.requireNonNull(getActivity())).setBookmarkPage(intent);
        } else {
            ((FolioActivity) Objects.requireNonNull(getActivity())).setHighlightPage(intent);
        }
    }
}