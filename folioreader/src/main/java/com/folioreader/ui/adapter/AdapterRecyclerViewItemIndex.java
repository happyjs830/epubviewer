package com.folioreader.ui.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.Config;
import com.folioreader.R;
import com.folioreader.ThemeConfig;
import com.folioreader.ui.activity.FolioActivity;
import com.folioreader.util.AppUtil;

import org.readium.r2.shared.Link;
import org.readium.r2.shared.Publication;

import java.util.Objects;

public class AdapterRecyclerViewItemIndex extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String LOG_TAG = "AdaptIndexFragment!";

    private final Context mContext;
    private final Publication mPublication;
    private final String mBookId;

    public AdapterRecyclerViewItemIndex(Context context, Publication publication, String bookId) {
        this.mContext = context;
        this.mPublication = publication;
        this.mBookId = bookId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        view = inflater.inflate(R.layout.item_chapter_index, parent, false);
        return new IndexHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        IndexHolder hd = (IndexHolder)holder;

        hd.textViewIndex.setText(String.valueOf(position+1));

        final Link tocLink = mPublication.getTableOfContents().get(position);
        hd.textViewTitle.setText(tocLink.getTitle());

        hd.item_index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOG_TAG, "onClick List");
                ((FolioActivity)mContext).onMoveCheckedChapter(Objects.requireNonNull(tocLink.getHref()));
            }
        });

        if (Objects.equals(tocLink.getHref(), mBookId)) {
            hd.item_index.setBackgroundColor(ThemeConfig._leftLayerRecyclerViewItemBackgroundColor);
        } else {
            hd.item_index.setBackgroundColor(ThemeConfig._baseBackgroundColor);
        }

        hd.textViewIndex.setTextColor(ThemeConfig._leftLayerRecyclerViewItemTextColor);
        hd.textViewTitle.setTextColor(ThemeConfig._leftLayerRecyclerViewItemTextColor);
        hd.vDevider.setBackgroundColor(ThemeConfig._leftLayerRecyclerViewItemDeviderColor);
    }

    @Override
    public int getItemCount() {
        return mPublication.getTableOfContents().size();
    }

    public static class IndexHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout item_index;
        private final TextView textViewIndex, textViewTitle;
        private final View vDevider;

        public IndexHolder(@NonNull final View itemView) {
            super(itemView);

            item_index = itemView.findViewById(R.id.item_index);
            textViewIndex = itemView.findViewById(R.id.textViewIndex);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            vDevider = itemView.findViewById(R.id.devide);
        }
    }
}
