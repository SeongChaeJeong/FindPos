package com.demco.goopy.findtoto;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.Data.ToToPositionRealmObj;
import com.demco.goopy.findtoto.Utils.AddressConvert;
import com.demco.goopy.findtoto.Utils.FileManager;
import com.demco.goopy.findtoto.Utils.KoreanTextMatch;
import com.demco.goopy.findtoto.Utils.KoreanTextMatcher;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import io.realm.Realm;

import static com.demco.goopy.findtoto.Data.ToToPosition.DELETE;
import static com.demco.goopy.findtoto.Data.ToToPosition.MODIFY;
import static com.demco.goopy.findtoto.Data.ToToPosition.NONE;
import static com.demco.goopy.findtoto.MapsActivity.RESULT_ITEM_SELECT;
import static com.demco.goopy.findtoto.MapsActivity.defaultLatitude;
import static com.demco.goopy.findtoto.MapsActivity.defaultLongitude;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionMangerActivity extends AppCompatActivity
        implements View.OnClickListener {

    public static String MARKER_TYPE = "markerType";
    public static String MARKER_ID = "markerID";
    public static String LATITUDE_POS = "latitudePos";
    public static String LONGITUDE_POS = "longitudePos";

    public static int MARKER_TEMP = 0;
    public static int MARKER_LOAD = 1;
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
    private int markerType = -1;
    private String focusMarkerId = "";
    private ToToPosition selectedItem = null;
    private String selectItemUniqeId;
    Toolbar myToolbar = null;
    EditText searchText = null;
    EditText titleText = null;
    EditText bizText = null;
    EditText addressText = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataset = PositionDataSingleton.getInstance().getMarkerPositions();
        PositionDataSingleton.getInstance().setGPSRecevie(false);

        bizCategoryList.clear();
        bizCategoryList.add(getResources().getString(R.string.none));
        for(ToToPosition position: dataset) {
            String bizCategory = position.biz;
            if(-1 == bizCategoryList.indexOf(bizCategory)) {
                bizCategoryList.add(bizCategory);
            }
        }

        setContentView(R.layout.activity_position_list);
        Intent dataIntent = getIntent();
        if(null != dataIntent) {
            Bundle bundle = dataIntent.getExtras();
            if(null != bundle) {
                markerType = bundle.getInt(MARKER_TYPE);
                focusLatitude = bundle.getDouble(LATITUDE_POS, defaultLatitude);
                focusLongitude = bundle.getDouble(LONGITUDE_POS, defaultLongitude);
                if(markerType == MARKER_LOAD) {
                    selectItemUniqeId = bundle.getString(MARKER_ID);
                }
            }
        }

        if(isDefaultLatLng() && dataset.isEmpty()) {
            setContentView(R.layout.empty_list_view);
            myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
            setSupportActionBar(myToolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setTitle(R.string.position_list_title);
            getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.toolbarColor)));

            String folderPath = getExternalFilesDir(null).getAbsolutePath();
            String filePath = folderPath + "/address.xls" +  getString(R.string.check_file);
            ((TextView)findViewById(R.id.empty_alert)).setText(filePath);
            return;
        }

        myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.position_list_title);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.toolbarColor)));

        searchText = (EditText)findViewById(R.id.search_text);
        titleText = (EditText)findViewById(R.id.edit_title);
        bizText = (EditText)findViewById(R.id.market_category);
        addressText = (EditText)findViewById(R.id.market_address);



        if(focusLongitude != defaultLongitude && focusLatitude != defaultLatitude) {
            String targetAddress = AddressConvert.getAddress(this, focusLatitude, focusLongitude);
            addressText.setText(targetAddress);
        }

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

        if(markerType == MARKER_LOAD) {
            for(ToToPosition position: dataset) {
                if(position.uniqueId.compareTo(selectItemUniqeId) == 0) {
                    selectedItem = position;
                    break;
                }
            }

            if(null != selectedItem) {
                titleText.setText(selectedItem.name);
                bizText.setText(selectedItem.biz);
                addressText.setText(selectedItem.addressData);
                s.setSelection(spinnerAdapter.getPosition(selectedItem.biz));
            }
        }

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
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PositionAdapter(this, dataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    private boolean isDefaultLatLng() {
        return focusLatitude == defaultLatitude && focusLongitude == defaultLongitude;
    }

    @Override
    public void onClick(View v) {
        selectedItem = null;
        if(null != selectItemUniqeId ) {
            for(ToToPosition position: dataset) {
                if(position.uniqueId.compareTo(selectItemUniqeId) == 0) {
                    selectedItem = position;
                    break;
                }
            }
        }
        switch (v.getId()) {
            case R.id.search_btn:
                if(searchText.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.empty_search_request, Toast.LENGTH_SHORT).show();
                    return;
                }
                searchResultdataset.clear();
                String titleData = searchText.getText().toString();
                KoreanTextMatcher matcher = new KoreanTextMatcher(titleData);
                for(ToToPosition positionData : dataset) {
                    KoreanTextMatch match = matcher.match(positionData.name);
                    if(0 == titleData.compareTo(positionData.addressData.toString())) {
                        searchResultdataset.add(positionData);
                    }
                    else if (match.success()) {
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
                if(addressText.getText().toString().isEmpty()) {
                    Toast.makeText(this, R.string.empty_address_result, Toast.LENGTH_SHORT).show();
                    return;
                }

                new MaterialDialog.Builder(PositionMangerActivity.this)
                        .content(R.string.add_content)
                        .positiveText(R.string.agree)
                        .negativeText(R.string.disagree)
                        .backgroundColorRes(R.color.white)
                        .positiveColorRes(R.color.dialogBtnColor)
                        .negativeColorRes(R.color.dialogBtnColor)
                        .contentColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ToToPosition newPosition = new ToToPosition();
                                // 토큰해서 각각주소에 넣기
                                String[] splitAddress = TextUtils.split(addressText.getText().toString(), " ");
                                newPosition.state = NONE;
                                newPosition.addressList.clear();
                                for(int i = 0; i < splitAddress.length; ++i) {
                                    if(getString(R.string.korea).compareToIgnoreCase(splitAddress[i]) == 0) {
                                        continue;
                                    }
                                    newPosition.addressList.add(splitAddress[i]);
                                }
                                newPosition.addressData = TextUtils.join(" ", newPosition.addressList);
                                newPosition.name = titleText.getText().toString();
                                newPosition.biz = bizText.getText().toString();

                                String bizCategory = bizText.getText().toString();
                                if(bizCategoryList.indexOf(bizCategory) == -1) {
                                    bizCategoryList.add(bizCategory);
                                }

                                newPosition.uniqueId = UUID.randomUUID().toString();

                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                ToToPositionRealmObj obj = realm.createObject(ToToPositionRealmObj.class);
                                obj.setUniqueId(newPosition.uniqueId);
                                obj.setBizState(newPosition.bizState);
                                obj.setTargetName(newPosition.name);
                                obj.setTargetBiz(newPosition.biz);
                                obj.setPhone(newPosition.phone);
                                obj.setAddressData(newPosition.addressData);

                                LatLng targetLatLng = null;
                                try {
                                    targetLatLng = AddressConvert.getLatLng(PositionMangerActivity.this, newPosition.addressData);
                                }
                                catch(TimeoutException e) {
                                    Toast.makeText(PositionMangerActivity.this, R.string.map_address_timeout, Toast.LENGTH_LONG).show();
                                    targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                                }
                                if(targetLatLng == null) {
                                    targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                                }
                                newPosition.latLng = targetLatLng;
                                obj.setLatitude(targetLatLng.latitude);
                                obj.setLongtitude(targetLatLng.longitude);

                                realm.commitTransaction();
                                dataset.add(newPosition);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(PositionMangerActivity.this, R.string.add_ok, Toast.LENGTH_SHORT).show();
                                initEditText();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        })
                        .show();

                break;
            case R.id.modify_btn:
                if(selectedItem == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    break;
                }

                new MaterialDialog.Builder(PositionMangerActivity.this)
                        .content(R.string.modify_content)
                        .positiveText(R.string.agree)
                        .negativeText(R.string.disagree)
                        .backgroundColorRes(R.color.white)
                        .positiveColorRes(R.color.dialogBtnColor)
                        .negativeColorRes(R.color.dialogBtnColor)
                        .contentColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                // 토큰해서 각각주소에 넣기
                                String[] splitAddressModify = TextUtils.split(addressText.getText().toString(), " ");
                                selectedItem.state = MODIFY;
                                selectedItem.addressList.clear();
                                for(int i = 0; i < splitAddressModify.length; ++i) {
                                    if(getString(R.string.korea).compareToIgnoreCase(splitAddressModify[i]) == 0) {
                                        continue;
                                    }
                                    selectedItem.addressList.add(splitAddressModify[i]);
                                }
                                selectedItem.state = MODIFY;
                                selectedItem.addressData = TextUtils.join(" ", selectedItem.addressList);
                                selectedItem.name = titleText.getText().toString();
                                selectedItem.biz = bizText.getText().toString();
                                Toast.makeText(PositionMangerActivity.this, R.string.modify_ok, Toast.LENGTH_SHORT).show();
                                initEditText();
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                ToToPositionRealmObj obj = realm.where(ToToPositionRealmObj.class).equalTo("uniqueId", selectedItem.uniqueId).findFirst();
                                if(null != obj) {
                                    obj.setUniqueId(selectedItem.uniqueId);
                                    obj.setBizState(selectedItem.bizState);
                                    obj.setTargetName(selectedItem.name);
                                    obj.setTargetBiz(selectedItem.biz);
                                    obj.setPhone(selectedItem.phone);
                                    obj.setAddressData(selectedItem.addressData);
                                    LatLng targetLatLng = null;
                                    try {
                                        targetLatLng = AddressConvert.getLatLng(PositionMangerActivity.this, selectedItem.addressData);
                                    }
                                    catch(TimeoutException e) {
                                        Toast.makeText(PositionMangerActivity.this, R.string.map_address_timeout, Toast.LENGTH_LONG).show();
                                        targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                                    }
                                    if(targetLatLng == null) {
                                        targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                                    }
                                    selectedItem.latLng = targetLatLng;
                                    obj.setLatitude(targetLatLng.latitude);
                                    obj.setLongtitude(targetLatLng.longitude);
                                }
                                realm.commitTransaction();
                                mAdapter.notifyDataSetChanged();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        })
                        .show();

                break;
            case R.id.delete_btn:
                if(selectedItem == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    break;
                }

                new MaterialDialog.Builder(PositionMangerActivity.this)
                        .content(R.string.del_content)
                        .positiveText(R.string.agree)
                        .negativeText(R.string.disagree)
                        .backgroundColorRes(R.color.white)
                        .positiveColorRes(R.color.dialogBtnColor)
                        .negativeColorRes(R.color.dialogBtnColor)
                        .contentColorRes(R.color.black)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                initEditText();
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                ToToPositionRealmObj obj = realm.where(ToToPositionRealmObj.class).equalTo("uniqueId", selectedItem.uniqueId).findFirst();
                                if(null != obj) {
                                    obj.deleteFromRealm();
                                }
                                realm.commitTransaction();
                                selectedItem.state = DELETE;
//                                dataset.remove(selectedItem);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(PositionMangerActivity.this, R.string.delete_ok, Toast.LENGTH_SHORT).show();
                            }

                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            }
                        })
                        .show();
                break;
            case R.id.focus_map_btn:
                if(selectedItem == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    break;
                }

                Intent intent = new Intent();
                intent.putExtra(LATITUDE_POS, selectedItem.latLng.latitude);
                intent.putExtra(LONGITUDE_POS, selectedItem.latLng.longitude);
                Toast.makeText(PositionMangerActivity.this, R.string.focus_ok, Toast.LENGTH_SHORT).show();
                setResult(RESULT_ITEM_SELECT, intent);
                finish();
        }
    }

    private void syncDBtoFileData() {
        List<ToToPosition> toToPositionList = PositionDataSingleton.getInstance().getMarkerPositions();
        if(toToPositionList.isEmpty() == false) {
            if(FileManager.saveExcelFile(this, "address.xls")) {
            }
            else {
                Toast.makeText(this, R.string.save_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initEditText() {
        titleText.setText("");
        bizText.setText("");
        s.setSelection(0);
        addressText.setText("");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.position_list_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
//            case R.id.action_list_save:
//                List<ToToPosition> toToPositionList = PositionDataSingleton.getInstance().getMarkerPositions();
//                if(toToPositionList.isEmpty()) {
//                    Toast.makeText(this, R.string.list_empty, Toast.LENGTH_SHORT).show();
//                    return true;
//                }
//                if(FileManager.saveExcelFile(this, "address.xls")) {
//                    Toast.makeText(this, R.string.save_ok, Toast.LENGTH_SHORT).show();
//                }
//                else {
//                    Toast.makeText(this, R.string.save_fail, Toast.LENGTH_SHORT).show();
//                }
//                return true;
            case R.id.app_version:
                String version;
                try {
                    PackageInfo i = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
                    version = i.versionName;
                    Toast.makeText(this, String.format(getString(R.string.app_version), version), Toast.LENGTH_SHORT).show();
                } catch(PackageManager.NameNotFoundException e) { }

            default:
                return super.onOptionsItemSelected(item);
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
            if(TextUtils.isEmpty(toToPosition.name) == false) {
                customViewHolder.marketTitle.setText(toToPosition.name);
            }
            if(TextUtils.isEmpty(toToPosition.biz) == false) {
                customViewHolder.marketCategory.setText(toToPosition.biz);
            }
            if(TextUtils.isEmpty(toToPosition.addressData) == false) {
                customViewHolder.marketAddress.setText(toToPosition.addressData);
            }

            customViewHolder.selectItemBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectItemUniqeId = toToPosition.uniqueId;
                    titleText.setText(toToPosition.name);
                    bizText.setText(toToPosition.biz);
                    addressText.setText(toToPosition.addressData);
                    int categoryIndex = bizCategoryList.indexOf(toToPosition.biz);
                    if(-1 != categoryIndex) {
                        s.setSelection(categoryIndex);
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return (null != feedItemList ? feedItemList.size() : 0);
        }

        class CustomViewHolder extends RecyclerView.ViewHolder {
            protected TextView marketTitle;
            protected TextView marketCategory;
            protected TextView marketAddress;
            protected Button selectItemBtn;

            public CustomViewHolder(View view) {
                super(view);
                this.marketTitle = (TextView) view.findViewById(R.id.market_title);
                this.marketCategory = (TextView) view.findViewById(R.id.market_category);
                this.marketAddress = (TextView) view.findViewById(R.id.market_address);
                this.selectItemBtn = (Button) view.findViewById(R.id.select_item_btn);
            }
        }
    }

}
