package com.sam_chordas.android.stockhawk.ui;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;

import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;

import java.util.ArrayList;
import java.util.Collections;

public class MyStockDetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    private static final int CURSOR_LOADER_ID = 100;
    private Cursor mCursor;
    private LineSet mLineSet;
    private int mMaxRange, mMinRange;
    private LineChartView mLineChartView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mystock_detail);
        mLineChartView = (LineChartView) findViewById(R.id.lcv_stock);
        mLineSet = new LineSet();
        initLineChartView();

        Intent intent = getIntent();
        Bundle bundle = new Bundle();
        bundle.putString(getResources().getString(R.string.symbol), intent.getStringExtra(getResources().getString(R.string.symbol)));
        getLoaderManager().initLoader(CURSOR_LOADER_ID, bundle, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, QuoteProvider.Quotes.CONTENT_URI,
                new String[]{QuoteColumns.BIDPRICE}, QuoteColumns.SYMBOL + " = ?",
                new String[]{args.getString(getResources().getString(R.string.symbol))}, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursor = data;
        getRange(mCursor);
        populateLineChartView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void getRange(Cursor mCursor) {
        ArrayList<Float> mArrayList = new ArrayList<Float>();
        for (mCursor.moveToFirst(); !mCursor.isAfterLast(); mCursor.moveToNext()) {
            mArrayList.add(Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE))));
        }
        mMaxRange = Math.round(Collections.max(mArrayList));
        mMinRange = Math.round(Collections.min(mArrayList));
        if (mMinRange > 100) {
            mMinRange = mMinRange - 100;
        }
    }

    private void initLineChartView() {
        Paint gridPaint = new Paint();
        gridPaint.setColor(ContextCompat.getColor(this, R.color.material_blue_700));
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(getResources().getDisplayMetrics().density);
        mLineChartView.setBorderSpacing(1)
                .setXAxis(false)
                .setYAxis(false)
                .setXLabels(AxisController.LabelPosition.OUTSIDE)
                .setYLabels(AxisController.LabelPosition.OUTSIDE)
                .setAxisBorderValues(mMinRange - 100, mMaxRange + 100, 50)
                .setBorderSpacing(getResources().getDisplayMetrics().density)
                .setLabelsColor(ContextCompat.getColor(this, R.color.material_blue_700))
                .setGrid(ChartView.GridType.HORIZONTAL, gridPaint);
    }

    private void populateLineChartView() {
        mCursor.moveToFirst();
        for (int i = 0; i < mCursor.getCount(); i++) {
            float price = Float.parseFloat(mCursor.getString(mCursor.getColumnIndex(QuoteColumns.BIDPRICE)));
            mLineSet.addPoint(" " + i, price);
            mCursor.moveToNext();
        }
        mLineSet.setColor(ContextCompat.getColor(this, R.color.material_green_700))
                .setDotsColor(ContextCompat.getColor(this, android.R.color.white))
                .setDotsStrokeThickness(4 * getResources().getDisplayMetrics().density)
                .setDotsStrokeColor(ContextCompat.getColor(this, R.color.material_green_700));

        mLineChartView.addData(mLineSet);
        mLineChartView.show();
    }

}