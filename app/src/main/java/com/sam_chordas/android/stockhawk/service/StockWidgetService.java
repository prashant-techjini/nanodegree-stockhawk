package com.sam_chordas.android.stockhawk.service;

import android.content.Intent;
import android.database.Cursor;
import android.os.Binder;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

public class StockWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsFactory() {
            private Cursor mCursor = null;

            @Override
            public void onCreate() {
            }

            @Override
            public void onDataSetChanged() {
                if (mCursor != null) {
                    mCursor.close();
                }

                long token = Binder.clearCallingIdentity();

                mCursor = getContentResolver().query(QuoteProvider.Quotes.CONTENT_URI, new String[]{QuoteColumns._ID, QuoteColumns.SYMBOL, QuoteColumns.BIDPRICE,
                                QuoteColumns.PERCENT_CHANGE, QuoteColumns.CHANGE, QuoteColumns.ISUP},
                        QuoteColumns.ISCURRENT + " = ?", new String[]{"1"}, null);

                Binder.restoreCallingIdentity(token);
            }

            @Override
            public void onDestroy() {
                if (mCursor != null) {
                    mCursor.close();
                    mCursor = null;
                }
            }

            @Override
            public int getCount() {
                if (mCursor != null) {
                    return mCursor.getCount();
                } else {
                    return 0;
                }
            }

            @Override
            public RemoteViews getViewAt(int position) {
                if (position == AdapterView.INVALID_POSITION ||
                        mCursor == null || !mCursor.moveToPosition(position)) {
                    return null;
                }

                RemoteViews views = new RemoteViews(getPackageName(), R.layout.list_item_quote);
                String symbol = mCursor.getString(mCursor.getColumnIndex("symbol"));
                views.setTextViewText(R.id.stock_symbol, symbol);
                views.setTextViewText(R.id.change, mCursor.getString(mCursor.getColumnIndex("percent_change")));
                views.setTextViewText(R.id.bid_price, mCursor.getString(mCursor.getColumnIndex("bid_price")));

                if (mCursor.getInt(mCursor.getColumnIndex("is_up")) == 1) {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_green);
                } else {
                    views.setInt(R.id.change, "setBackgroundResource", R.drawable.percent_change_pill_red);
                }
                return views;
            }

            @Override
            public RemoteViews getLoadingView() {
                return null;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public long getItemId(int position) {
                if (mCursor.moveToPosition(position))
                    return mCursor.getLong(mCursor.getColumnIndexOrThrow(QuoteColumns._ID));
                return position;
            }

            @Override
            public boolean hasStableIds() {
                return true;
            }
        };
    }
}
