package com.folioreader.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.folioreader.R;
import com.folioreader.model.HighlightImpl;

import org.readium.r2.shared.Publication;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AdapterRecyclerViewItemHighlight extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String LOG_TAG = "AdaptHighlightFragment";

    private final Context mContext;
    private final Publication mPublication;
    private final List<HighlightImpl> mHighlightList;
    private HighlightCallback mCallback;
    private final HighlightItemControllCallback mItemControllCallback;

    private int mListMode = 0;
    private boolean isAllChecked = false;

    public AdapterRecyclerViewItemHighlight(Context context, Publication publication, ArrayList<HighlightImpl> list, HighlightCallback callback, HighlightItemControllCallback itemCallback) {
        this.mContext = context;
        this.mPublication = publication;
        this.mHighlightList = list;
        this.mCallback = callback;
        this.mItemControllCallback = itemCallback;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        Context context = parent.getContext();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(viewType == 1) {
            view = inflater.inflate(R.layout.item_record_highlight_edit, parent, false);
            return new HighlightEditHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_record_highlight, parent, false);
            return new HighlightHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final HighlightImpl i = getItem(holder.getAdapterPosition());

        if(holder instanceof HighlightEditHolder) {
            final HighlightEditHolder hd = ((HighlightEditHolder) holder);

            hd.textViewContent.setText(Html.fromHtml(i.getContent()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(i.getDate());
            hd.textViewDate.setText(date);

            hd.textViewPage.setText(String.format(mContext.getString(R.string.layout_record_page_text), i.getPageNumber()));
            hd.textViewChapter.setText(mPublication.getTableOfContents().get(i.getPageNumber()).getTitle());
            hd.btnCheck.setSelected(isAllChecked);

            hd.item_highlight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    hd.btnCheck.setSelected(!hd.btnCheck.isSelected());
                    if (mItemControllCallback != null) {
                        mItemControllCallback.itemCheck(holder.getAdapterPosition(), i, hd.btnCheck.isSelected());
                    }
                }
            });

            hd.btnCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    hd.btnCheck.setSelected(!hd.btnCheck.isSelected());
                    if (mItemControllCallback != null) {
                        mItemControllCallback.itemCheck(holder.getAdapterPosition(), i, hd.btnCheck.isSelected());
                    }
                }
            });
        } else {
            final HighlightHolder hd = ((HighlightHolder) holder);

            hd.textViewContent.setText(Html.fromHtml(i.getContent()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(i.getDate());
            hd.textViewDate.setText(date);

            hd.textViewPage.setText(String.format(mContext.getString(R.string.layout_record_page_text), i.getPageNumber()));
            hd.textViewChapter.setText(mPublication.getTableOfContents().get(i.getPageNumber()).getTitle());

            hd.item_highlight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 하이라이트 된 페이지로 이동
                    Log.e(LOG_TAG, "Go Highlight View !!!");
                    if (mCallback != null) {
                        mCallback.onItemClick(i);
                    }
                }
            });

            hd.item_highlight.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // bottom sheet dialog 호출
                    Log.e(LOG_TAG, "Call BottomSheetDialog");
                    if (mItemControllCallback != null) {
                        mItemControllCallback.itemClick(holder.getAdapterPosition(), i);
                    }
                    return true;
                }
            });

            hd.btn_more.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // bottom sheet dialog 호출
                    Log.e(LOG_TAG, "Call BottomSheetDialog");
                    if (mItemControllCallback != null) {
                        mItemControllCallback.itemClick(holder.getAdapterPosition(), i);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mHighlightList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mListMode;
    }

    private HighlightImpl getItem(int position) {
        return mHighlightList.get(position);
    }

    public void setItemViewType(int viewType) {
        mListMode = viewType;
    }

    public void removeList(int pos) {
        Log.e(LOG_TAG, "pos => " + pos);
        mCallback.deleteHighlight(getItem(pos).getId());
        mHighlightList.remove(pos);
        notifyItemRemoved(pos);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setAllItemState(boolean flag) {
        Log.e(LOG_TAG, "setAllItemState flag ::: " + flag);
        isAllChecked = flag;
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void sortList(final String type) {
        Collections.sort(mHighlightList, new Comparator<HighlightImpl>() {
            @Override
            public int compare(HighlightImpl o1, HighlightImpl o2) {
                switch (type) {
                    case "PAGE":
                        return Integer.compare(o1.getPageNumber(), o2.getPageNumber());
                    case "REGISTER":
                        return Long.compare(o2.getTimeStamp(), o1.getTimeStamp());
                    default:
                        return 0;
                }
            }
        });
        notifyDataSetChanged();
    }

    public static class HighlightHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout item_highlight;
        private final TextView textViewContent, textViewDate, textViewPage, textViewChapter;
        private final Button btn_more;

        public HighlightHolder(@NonNull final View itemView) {
            super(itemView);

            item_highlight = itemView.findViewById(R.id.item_highlight);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewPage = itemView.findViewById(R.id.textViewPage);
            textViewChapter = itemView.findViewById(R.id.textViewChapter);
            btn_more = itemView.findViewById(R.id.btn_more);
        }
    }

    public static class HighlightEditHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout item_highlight;
        private final CheckBox btnCheck;
        private final TextView textViewContent, textViewDate, textViewPage, textViewChapter;

        public HighlightEditHolder(@NonNull final View itemView) {
            super(itemView);

            item_highlight = itemView.findViewById(R.id.item_highlight);
            btnCheck = itemView.findViewById(R.id.btn_check);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewPage = itemView.findViewById(R.id.textViewPage);
            textViewChapter = itemView.findViewById(R.id.textViewChapter);
        }
    }

    public interface HighlightItemControllCallback {
        void itemClick(int position, HighlightImpl highlight);
        void itemCheck(int position, HighlightImpl highlight, boolean checked);
    }

    public interface HighlightCallback {
        void onItemClick(HighlightImpl highlightImpl);
        void deleteHighlight(int id);
        void editNote(HighlightImpl highlightImpl, int position);
    }
}
