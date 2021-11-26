package com.folioreader.ui.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.folioreader.Constants;
import com.folioreader.FolioReader;
import com.folioreader.ui.fragment.RecordFragment;
import com.folioreader.ui.fragment.FragmentRecordBookmark;
import com.folioreader.ui.fragment.FragmentRecordHighlight;
import com.folioreader.ui.fragment.FragmentRecordMemo;

public class AdapterFragmentStateRecord extends FragmentStateAdapter {
    private final Bundle mBundle;
    private final RecordFragment mParent;

    public AdapterFragmentStateRecord(@NonNull FragmentActivity fragmentActivity, Bundle bundle, RecordFragment parent) {
        super(fragmentActivity);
        mBundle = bundle;
        mParent = parent;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 0) {
            String bookId = (mBundle != null) ?  mBundle.getString(FolioReader.EXTRA_BOOK_ID) : "";
            String bookTitle = (mBundle != null) ? mBundle.getString(Constants.BOOK_TITLE) : "";
            return new FragmentRecordBookmark(bookId, bookTitle, mParent);
        } else if (position == 1) {
            String bookId = (mBundle != null) ?  mBundle.getString(FolioReader.EXTRA_BOOK_ID) : "";
            String bookTitle = (mBundle != null) ? mBundle.getString(Constants.BOOK_TITLE) : "";
            return new FragmentRecordHighlight(bookId, bookTitle, mParent);
        } else {
            return new FragmentRecordMemo();
        }
    }
}
