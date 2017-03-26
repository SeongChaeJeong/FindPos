package com.demco.goopy.findtoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.Utils.AddressConvert;
import com.demco.goopy.findtoto.Utils.KoreanTextMatch;
import com.demco.goopy.findtoto.Utils.KoreanTextMatcher;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS1;
import static com.demco.goopy.findtoto.Data.ToToPosition.BUSINESS;
import static com.demco.goopy.findtoto.Data.ToToPosition.LAST_INDEX;
import static com.demco.goopy.findtoto.Data.ToToPosition.NAME;
import static com.demco.goopy.findtoto.MapsActivity.RESULT_ITEM_SELECT;
import static com.demco.goopy.findtoto.MapsActivity.defaultLatitude;
import static com.demco.goopy.findtoto.MapsActivity.defaultLongitude;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionMangerActivity extends Activity
        implements View.OnClickListener {

    public static String LATITUDE_POS = "latitudePos";
    public static String LONGITUDE_POS = "longitudePos";

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PositionAdapter mAdapter;
    protected Spinner s;
    private ArrayAdapter<String> spinnerAdapter;
    private List<ToToPosition> dataset;
    private List<ToToPosition> searchResultdataset = new ArrayList<>();
    private List<String> bizCategoryList = new ArrayList<>();
    private double focusLatitude = defaultLatitude;
    private double focusLongitude = defaultLongitude;

    private int selectItemUniqeId = 0;

    EditText searchText = null;
    EditText titleText = null;
    EditText bizText = null;
    EditText addressText = null;

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

        bizCategoryList.clear();
        bizCategoryList.add(getResources().getString(R.string.none));
        for(ToToPosition position: dataset) {
            String bizCategory = position.rawData[BUSINESS];
            if(-1 == bizCategoryList.indexOf(bizCategory)) {
                bizCategoryList.add(bizCategory);
            }
        }

        setContentView(R.layout.activity_position_list);
        Intent dataIntent = getIntent();
        if(null != dataIntent) {
            Bundle bundle = dataIntent.getExtras();
            if(null != bundle) {
                focusLatitude = bundle.getDouble(LATITUDE_POS, defaultLatitude);
                focusLongitude = bundle.getDouble(LONGITUDE_POS, defaultLongitude);
            }
        }

        searchText = (EditText)findViewById(R.id.search_text);
        titleText = (EditText)findViewById(R.id.edit_title);
        bizText = (EditText)findViewById(R.id.market_category);
        addressText = (EditText)findViewById(R.id.market_address);

        s = (Spinner) findViewById(R.id.biz_category_spinner);
        spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, bizCategoryList);
        spinnerAdapter.notifyDataSetChanged();
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s.setAdapter(spinnerAdapter);
        s.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedBiz = bizCategoryList.get(position);
                bizText.setText(selectedBiz);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        Button searchBtn = (Button)findViewById(R.id.search_btn);
        Button addBtn = (Button)findViewById(R.id.add_btn);
        Button modifyBtn = (Button)findViewById(R.id.modify_btn);
        Button delBtn = (Button)findViewById(R.id.delete_btn);
        Button focusMapBtn = (Button)findViewById(R.id.focus_map_btn);

        searchBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        modifyBtn.setOnClickListener(this);
        delBtn.setOnClickListener(this);
        focusMapBtn.setOnClickListener(this);

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

    @Override
    public void onClick(View v) {
        ToToPosition selectedItem = null;
        for(ToToPosition position: dataset) {
            if(position.uniqueId == selectItemUniqeId) {
                selectedItem = position;
                break;
            }
        }
        switch (v.getId()) {
            case R.id.search_btn:
                searchResultdataset.clear();
                String titleData = searchText.getText().toString();
                KoreanTextMatcher matcher = new KoreanTextMatcher(titleData);
                for(ToToPosition positionData : dataset) {
                    KoreanTextMatch match = matcher.match(positionData.rawData[NAME]);
                    if (match.success()) {
                        searchResultdataset.add(positionData);
                    }
                }
                if(searchResultdataset.isEmpty()) {
                    Toast.makeText(this, R.string.empty_search_result, Toast.LENGTH_SHORT).show();
                }
                else {
                    mAdapter = new PositionAdapter(this, searchResultdataset);
                    mRecyclerView.setAdapter(mAdapter);
                    mAdapter.notifyDataSetChanged();
                }
                break;
            case R.id.add_btn:
                ToToPosition newPosition = new ToToPosition();
                newPosition.addressData = addressText.getText().toString();
                // 토큰해서 각각주소에 넣기
                String[] splitAddress = TextUtils.split(selectedItem.addressData, " ");
                int maxIndex = splitAddress.length > LAST_INDEX ? LAST_INDEX : splitAddress.length;
                for(int i = 0; i < maxIndex; ++i) {
                    selectedItem.rawData[ADDRESS1 + i] = splitAddress[i];
                }
                newPosition.rawData[NAME] = titleText.getText().toString();
                newPosition.rawData[BUSINESS] = bizText.getText().toString();

                String bizCategory = bizText.getText().toString();
                if(bizCategoryList.indexOf(bizCategory) == -1) {
                    bizCategoryList.add(bizCategory);
                }

                dataset.add(newPosition);
                mAdapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.add_ok, Toast.LENGTH_SHORT).show();
                break;
            case R.id.modify_btn:
                if(selectedItem == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    break;
                }
                selectedItem.addressData = addressText.getText().toString();
                // 토큰해서 각각주소에 넣기
                String[] splitAddressModify = TextUtils.split(selectedItem.addressData, " ");
                int maxIndexModify = splitAddressModify.length > LAST_INDEX ? LAST_INDEX : splitAddressModify.length;
                for(int i = 0; i < maxIndexModify; ++i) {
                    selectedItem.rawData[ADDRESS1 + i] = splitAddressModify[i];
                }
                selectedItem.rawData[NAME] = titleText.getText().toString();
                selectedItem.rawData[BUSINESS] = bizText.getText().toString();
                Toast.makeText(this, R.string.modify_ok, Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.delete_btn:
                if(selectedItem == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    break;
                }
                dataset.remove(selectedItem);
                titleText.setText("");
                bizText.setText("");
                s.setSelection(0);
                addressText.setText("");
                mAdapter.notifyDataSetChanged();
                Toast.makeText(this, R.string.delete_ok, Toast.LENGTH_SHORT).show();
                break;
            case R.id.focus_map_btn:
                if(selectedItem == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    break;
                }
                LatLng targetLatLng = AddressConvert.getLatLng(PositionMangerActivity.this, selectedItem.addressData);
                if(targetLatLng == null) {
                    targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                }
                Intent intent = new Intent();
                intent.putExtra(LATITUDE_POS, targetLatLng.latitude);
                intent.putExtra(LONGITUDE_POS, targetLatLng.longitude);
                setResult(RESULT_ITEM_SELECT, intent);
                Toast.makeText(this, R.string.focus_ok, Toast.LENGTH_SHORT).show();
                finish();
        }
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
            final ToToPosition toToPosition = feedItemList.get(i);
            if(TextUtils.isEmpty(toToPosition.rawData[NAME]) == false) {
                customViewHolder.marketTitle.setText(toToPosition.rawData[NAME]);
            }
            if(TextUtils.isEmpty(toToPosition.rawData[BUSINESS]) == false) {
                customViewHolder.marketCategory.setText(toToPosition.rawData[BUSINESS]);
            }
            if(TextUtils.isEmpty(toToPosition.addressData) == false) {
                customViewHolder.marketAddress.setText(toToPosition.addressData);
            }

            customViewHolder.selectItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItemUniqeId = toToPosition.uniqueId;
                    titleText.setText(toToPosition.rawData[NAME]);
                    bizText.setText(toToPosition.rawData[BUSINESS]);
                    addressText.setText(toToPosition.addressData);
                    int categoryIndex = bizCategoryList.indexOf(toToPosition.rawData[BUSINESS]);
                    if(-1 != categoryIndex) {
                        s.setSelection(categoryIndex);
                    }
                }
            });

//            customViewHolder.positionFocusBtn.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    LatLng targetLatLng = AddressConvert.getLatLng(PositionMangerActivity.this, toToPosition.addressData);
//                    if(targetLatLng == null) {
//                        targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
//                    }
//                    Intent intent = new Intent();
//                    intent.putExtra(LATITUDE_POS, targetLatLng.latitude);
//                    intent.putExtra(LONGITUDE_POS, targetLatLng.longitude);
//                    setResult(RESULT_ITEM_SELECT, intent);
//                }
//            });
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
            protected Button selectItemBtn;
            protected Button positionFocusBtn;

            public CustomViewHolder(View view) {
                super(view);
                this.marketTitle = (TextView) view.findViewById(R.id.market_title);
                this.marketCategory = (TextView) view.findViewById(R.id.market_category);
                this.marketAddress = (TextView) view.findViewById(R.id.market_address);
                this.selectItemBtn = (Button) view.findViewById(R.id.select_item_btn);
//                this.positionFocusBtn = (Button) view.findViewById(R.id.focus_map_btn);
            }
        }
    }

}
