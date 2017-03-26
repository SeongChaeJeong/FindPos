package com.demco.goopy.findtoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;

import java.util.List;

import static com.demco.goopy.findtoto.Data.ToToPosition.BUSINESS;
import static com.demco.goopy.findtoto.Data.ToToPosition.NAME;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionMangerActivity extends Activity{

    public static String LATITUDE_POS = "latitudePos";
    public static String LONGITUDE_POS = "longitudePos";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PositionAdapter mAdapter;
    private List<ToToPosition> dataset;
    private double focusLatitude = MapsActivity.defaultLatitude;
    private double focusLongitude = MapsActivity.defaultLongitude;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataset = PositionDataSingleton.getInstance().getMarkerPositions();
        if(dataset.isEmpty()) {
            setContentView(R.layout.empty_list_view);
            String filePath = getExternalFilesDir(null) + "address.xls" + getString(R.string.check_file);
            ((TextView)findViewById(R.id.empty_alert)).setText(filePath);
            return;
        }

        setContentView(R.layout.activity_position_list);
        Intent dataIntent = getIntent();
        if(null != dataIntent) {
            Bundle bundle = dataIntent.getExtras();
            if(null != bundle) {
                focusLatitude = bundle.getDouble(LATITUDE_POS, MapsActivity.defaultLatitude);
                focusLongitude = bundle.getDouble(LONGITUDE_POS, MapsActivity.defaultLongitude);
            }
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.position_recycler_view);
        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new PositionAdapter(this, dataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.CustomViewHolder> {
        private List<ToToPosition> feedItemList;
        private Context mContext;

        public PositionAdapter(Context context, List<ToToPosition> feedItemList) {
            this.feedItemList = feedItemList;
            this.mContext = context;
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.position_list_row, null);
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
            ToToPosition toToPosition = feedItemList.get(i);
            if(TextUtils.isEmpty(toToPosition.rawData[NAME]) == false) {
                customViewHolder.marketTitle.setText(toToPosition.rawData[NAME]);
            }
            if(TextUtils.isEmpty(toToPosition.rawData[BUSINESS]) == false) {
                customViewHolder.marketCategory.setText(toToPosition.rawData[BUSINESS]);
            }
            if(TextUtils.isEmpty(toToPosition.addressData) == false) {
                customViewHolder.marketAddress.setText(toToPosition.addressData);
            }
        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            protected ImageView imageView;
            protected TextView marketTitle;
            protected TextView marketCategory;
            protected TextView marketAddress;

            public CustomViewHolder(View view) {
                super(view);
                this.marketTitle = (TextView) view.findViewById(R.id.market_title);
                this.marketCategory = (TextView) view.findViewById(R.id.market_category);
                this.marketAddress = (TextView) view.findViewById(R.id.market_address);
            }
        }
    }

}
