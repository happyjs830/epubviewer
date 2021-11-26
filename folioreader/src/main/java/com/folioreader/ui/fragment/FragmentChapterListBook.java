package com.folioreader.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.R;
import com.folioreader.ui.adapter.AdapterRecyclerViewItemBook;
import com.folioreader.ui.adapter.AdapterRecyclerViewItemIndex;

import org.readium.r2.shared.Publication;

public class FragmentChapterListBook extends Fragment {
    private final String LOG_TAG = "ChapterListBook";
    private String mBookId = "";

    private ViewGroup mView = null;
    private final Publication mPublication;
    private FragmentActivity mActivity;

    public FragmentChapterListBook(Publication publication, FragmentActivity activity, String bookId) {
        mPublication = publication;
        mBookId = bookId;
        mActivity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup)inflater.inflate(R.layout.fragment_left_book, container,false);
        return mView;
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.e(LOG_TAG, "~> onViewCreated");

        init();
    }

    private void init() {
        // RecyclerView //
        RecyclerView mRecyclerView = mView.findViewById(R.id.recyclerView);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        AdapterRecyclerViewItemBook adapter = new AdapterRecyclerViewItemBook(mActivity, mPublication, mBookId);
        mRecyclerView.setAdapter(adapter);
    }
}
