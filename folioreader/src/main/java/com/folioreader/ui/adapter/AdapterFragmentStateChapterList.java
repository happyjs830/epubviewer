package com.folioreader.ui.adapter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.folioreader.Constants;
import com.folioreader.ui.fragment.FragmentChapterListBook;
import com.folioreader.ui.fragment.FragmentChapterListIndex;

import org.readium.r2.shared.Publication;

public class AdapterFragmentStateChapterList extends FragmentStateAdapter {
    private final Bundle mBundle;
    private final Publication mPublication;
    private FragmentActivity mActivity;

    public AdapterFragmentStateChapterList(@NonNull FragmentActivity fragmentActivity, Bundle bundle) {
        super(fragmentActivity);
        mBundle = bundle;
        mPublication = (Publication) mBundle.getSerializable(Constants.PUBLICATION);
        mActivity = fragmentActivity;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String bookId = (mBundle != null) ?  mBundle.getString(Constants.CHAPTER_SELECTED) : "";
        String bookTitle = (mBundle != null) ? mBundle.getString(Constants.BOOK_TITLE) : "";

        if (position == 0) {
            return new FragmentChapterListIndex(mPublication, mActivity, bookId);
        } else {
            return new FragmentChapterListBook(mPublication, mActivity, bookId);
        }
    }
}
