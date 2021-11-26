package com.folioreader.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.folioreader.R;
import com.folioreader.ui.activity.FolioActivity;

import java.util.Objects;

public class CommentFragment extends Fragment {
    private String LOG_TAG = "CommentActivity";

    // Main View //
    private ViewGroup mView = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = (ViewGroup)inflater.inflate(R.layout.activity_comment, container, false);
        mView.findViewById(R.id.btn_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        mView.setClickable(true);

        init();

        return mView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                close();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
    }

    private void init() {
        Log.e(LOG_TAG, "-> init");
    }

    private void close() {
        FragmentManager manager = Objects.requireNonNull(getActivity()).getSupportFragmentManager();
        manager.beginTransaction().remove(this).commit();
        manager.popBackStack();

        ((FolioActivity)getActivity()).setMenuForFragment(false);
    }
}