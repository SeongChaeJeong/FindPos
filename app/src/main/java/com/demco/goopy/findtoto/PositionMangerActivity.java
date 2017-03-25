package com.demco.goopy.findtoto;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.demco.goopy.findtoto.Data.ToToPosition;

import java.util.List;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionMangerActivity extends Activity{

    public static String ROW_POS = "rowPos";
    public static String CUL_POS = "curPos";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PositionAdapter mAdapter;
    private List<ToToPosition> dataset;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position_list);
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
            ToToPosition feedItem = feedItemList.get(i);

            //Render image using Picasso library
//            if (!TextUtils.isEmpty(feedItem.getThumbnail())) {
//                Picasso.with(mContext).load(feedItem.getThumbnail())
//                        .error(R.drawable.placeholder)
//                        .placeholder(R.drawable.placeholder)
//                        .into(customViewHolder.imageView);
//            }
            //Setting text view title
//            customViewHolder.textView.setText(Html.fromHtml(feedItem.getTitle()));
        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            protected ImageView imageView;
            protected TextView textView;

            public CustomViewHolder(View view) {
                super(view);
//                this.imageView = (ImageView) view.findViewById(R.id.thumbnail);
                this.textView = (TextView) view.findViewById(R.id.title);
            }
        }
    }

}
