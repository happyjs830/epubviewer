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
import com.folioreader.model.BookmarkImpl;

import org.readium.r2.shared.Publication;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class AdapterRecyclerViewItemBookmark extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final String LOG_TAG = "AdaptBookmarkFragment";

    private final Context mContext;
    private final Publication mPublication;
    private final ArrayList<BookmarkImpl> mBookmarkList;
    private final BookmarkCallback mCallback;
    private final BookmarkItemControllCallback mItemControllCallback;

    private int mListMode = 0;
    private boolean isAllChecked = false;

    public AdapterRecyclerViewItemBookmark(Context context, Publication publication, ArrayList<BookmarkImpl> list, BookmarkCallback callback, BookmarkItemControllCallback itemCallback) {
        this.mContext = context;
        this.mPublication = publication;
        this.mBookmarkList = list;
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
            view = inflater.inflate(R.layout.item_record_bookmark_edit, parent, false);
            return new BookmarkEditHolder(view);
        } else {
            view = inflater.inflate(R.layout.item_record_bookmark, parent, false);
            return new BookmarkHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final BookmarkImpl i = getItem(holder.getAdapterPosition());

        if(holder instanceof BookmarkEditHolder) {
            final BookmarkEditHolder hd = ((BookmarkEditHolder) holder);

            hd.textViewContent.setText(Html.fromHtml(i.getContent()));
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(i.getDate());
            hd.textViewDate.setText(date);

            hd.textViewPage.setText(String.format(mContext.getString(R.string.layout_record_page_text), i.getChapterNo()));
            hd.textViewChapter.setText(mPublication.getTableOfContents().get(i.getChapterNo()).getTitle());
            hd.btnCheck.setSelected(isAllChecked);

            hd.item_bookmark.setOnClickListener(new View.OnClickListener() {
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
            final BookmarkHolder hd = ((BookmarkHolder) holder);

            hd.textViewContent.setText(Html.fromHtml(i.getContent()));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String date = sdf.format(i.getDate());
            hd.textViewDate.setText(date);

            hd.textViewPage.setText(String.format(mContext.getString(R.string.layout_record_page_text), i.getChapterNo()));
            hd.textViewChapter.setText(mPublication.getTableOfContents().get(i.getChapterNo()).getTitle());

            hd.item_bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 하이라이트 된 페이지로 이동
                    Log.e(LOG_TAG, "Go Bookmark View !!!");
                    if (mCallback != null) {
                        mCallback.onItemClick(i);
                    }
                }
            });

            hd.item_bookmark.setOnLongClickListener(new View.OnLongClickListener() {
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
        return mBookmarkList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mListMode;
    }

    private BookmarkImpl getItem(int position) {
        return mBookmarkList.get(position);
    }

    public void setItemViewType(int viewType) {
        mListMode = viewType;
    }

    public void removeList(int pos) {
        Log.e(LOG_TAG, "pos => " + pos);
        mCallback.deleteBookmark(getItem(pos).getId());
        mBookmarkList.remove(pos);
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
        Collections.sort(mBookmarkList, new Comparator<BookmarkImpl>() {
            @Override
            public int compare(BookmarkImpl o1, BookmarkImpl o2) {
                switch (type) {
                    case "PAGE":
                        return Integer.compare(o1.getChapterNo(), o2.getChapterNo());
                    case "REGISTER":
                        return Long.compare(o2.getTimeStamp(), o1.getTimeStamp());
                    default:
                        return 0;
                }
            }
        });
        notifyDataSetChanged();
    }

    public static class BookmarkHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout item_bookmark;
        private final TextView textViewContent, textViewDate, textViewPage, textViewChapter;
        private final Button btn_more;

        public BookmarkHolder(@NonNull final View itemView) {
            super(itemView);

            item_bookmark = itemView.findViewById(R.id.item_bookmark);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewPage = itemView.findViewById(R.id.textViewPage);
            textViewChapter = itemView.findViewById(R.id.textViewChapter);
            btn_more = itemView.findViewById(R.id.btn_more);
        }
    }

    public static class BookmarkEditHolder extends RecyclerView.ViewHolder {
        private final ConstraintLayout item_bookmark;
        private final CheckBox btnCheck;
        private final TextView textViewContent, textViewDate, textViewPage, textViewChapter;

        public BookmarkEditHolder(@NonNull final View itemView) {
            super(itemView);

            item_bookmark = itemView.findViewById(R.id.item_bookmark);
            btnCheck = itemView.findViewById(R.id.btn_check);
            textViewContent = itemView.findViewById(R.id.textViewContent);
            textViewDate = itemView.findViewById(R.id.textViewDate);
            textViewPage = itemView.findViewById(R.id.textViewPage);
            textViewChapter = itemView.findViewById(R.id.textViewChapter);
        }
    }

    public interface BookmarkItemControllCallback {
        void itemClick(int position, BookmarkImpl bookmark);
        void itemCheck(int position, BookmarkImpl bookmark, boolean checked);
    }

    public interface BookmarkCallback {
        void onItemClick(BookmarkImpl bookmark);
        void deleteBookmark(int id);
        void editNote(BookmarkImpl bookmark, int position);
    }
}
