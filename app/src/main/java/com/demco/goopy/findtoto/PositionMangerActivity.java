package com.demco.goopy.findtoto;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.Data.ToToPositionRealmObj;
import com.demco.goopy.findtoto.Utils.AddressConvert;
import com.demco.goopy.findtoto.Utils.FileManager;
import com.demco.goopy.findtoto.Utils.KoreanTextMatch;
import com.demco.goopy.findtoto.Utils.KoreanTextMatcher;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import io.realm.Realm;
import io.realm.RealmResults;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.*;
import java.util.concurrent.TimeoutException;

import static com.demco.goopy.findtoto.Data.ToToPosition.*;
import static com.demco.goopy.findtoto.MapsActivity.*;
import static com.demco.goopy.findtoto.Utils.FileManager.*;

/**
 * Created by goopy on 2017-03-25.
 */

public class PositionMangerActivity extends AppCompatActivity
        implements View.OnClickListener {

    private AddLoadMarkerTask addLoadMarkerTask;
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
    private List<ToToPosition> dataModifySet;
    private List<ToToPosition> searchResultdataset = new ArrayList<>();
    private Map<String, Float> bizCategoryColorMap = new HashMap<>();
    int markerColorIndex = 0;
    private List<String> bizCategoryList = new ArrayList<>();
    private double focusLatitude = defaultLatitude;
    private double focusLongitude = defaultLongitude;
    private int markerType = -1;
    private String focusMarkerId = "";
    private ToToPosition selectedItem = null;
    private String selectItemUniqeId;
    TextView msiCodeView = null;
    Toolbar myToolbar = null;
    EditText searchText = null;
    EditText titleText = null;
    EditText bizText = null;
    EditText addressText = null;
    ImageButton clearButton = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataset = PositionDataSingleton.getInstance().getMarkerPositions();
        dataModifySet = PositionDataSingleton.getInstance().getMarkerModifyPositions();
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
        msiCodeView = (TextView)findViewById(R.id.market_msi_code) ;
        searchText = (EditText)findViewById(R.id.search_text);
        titleText = (EditText)findViewById(R.id.edit_title);
        bizText = (EditText)findViewById(R.id.market_category);
        addressText = (EditText)findViewById(R.id.market_address);
        clearButton = (ImageButton)findViewById(R.id.clear_text_button);
        s = (Spinner) findViewById(R.id.biz_category_spinner);

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

        selectedItem = null;

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

        if(focusLongitude != defaultLongitude && focusLatitude != defaultLatitude) {
            String address = AddressConvert.getAddress(this, focusLatitude, focusLongitude);
            String countryLabel = getResources().getString(R.string.contry_label);
            String targetAddress = null;
            int index = address.indexOf(countryLabel);
            if(-1 != index) {
                targetAddress = address.substring(index + countryLabel.length());
            }
            else {
                targetAddress = address;
            }
            addressText.setText(targetAddress);
        }
        else {
            loadEditText();
        }


        if(markerType == MARKER_LOAD) {
            for(ToToPosition position: dataset) {
                if(position.uniqueId.compareTo(selectItemUniqeId) == 0) {
                    selectedItem = position;
                    break;
                }
            }

            if(null != selectedItem) {
                selectedItem.state = MODIFY;
                dataModifySet.add(selectedItem);
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
        clearButton.setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.position_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new PositionAdapter(this, dataset);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(addLoadMarkerTask != null) {
            addLoadMarkerTask.onDestroy();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveEditText();
        setResult(RESULT_OK, null);
        finish();
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
                                obj.setMsiCode(newPosition.msiCode);

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
                                dataModifySet.remove(selectedItem);
                                dataModifySet.add(newPosition);
                                mAdapter.notifyDataSetChanged();
                                Toast.makeText(PositionMangerActivity.this, R.string.add_ok, Toast.LENGTH_SHORT).show();
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
                                String bizCategory = bizText.getText().toString();
                                if(bizCategoryList.indexOf(bizCategory) == -1) {
                                    bizCategoryList.add(bizCategory);
                                }
                                Toast.makeText(PositionMangerActivity.this, R.string.modify_ok, Toast.LENGTH_SHORT).show();
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                ToToPositionRealmObj obj = realm.where(ToToPositionRealmObj.class).equalTo("uniqueId", selectedItem.uniqueId).findFirst();
                                if(null != obj) {
                                    obj.setUniqueId(selectedItem.uniqueId);
                                    obj.setBizState(selectedItem.bizState);
                                    obj.setTargetName(selectedItem.name);
                                    obj.setTargetBiz(selectedItem.biz);
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
                                        selectedItem.phone = ERROR_CHANNEL;
                                    }
                                    else {
                                        selectedItem.phone = "";
                                    }
                                    obj.setPhone(selectedItem.phone);
                                    selectedItem.latLng = targetLatLng;
                                    obj.setLatitude(targetLatLng.latitude);
                                    obj.setLongtitude(targetLatLng.longitude);
                                }
                                realm.commitTransaction();
                                selectedItem.state = MODIFY;
                                dataModifySet.remove(selectedItem);
                                dataModifySet.add(selectedItem);
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
                if(selectedItem == null || selectedItem.uniqueId == null) {
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    return;
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
                                if(null == selectedItem.uniqueId) {
                                    Toast.makeText(PositionMangerActivity.this, R.string.empty_id_select_result, Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                Realm realm = Realm.getDefaultInstance();
                                realm.beginTransaction();
                                ToToPositionRealmObj obj = realm.where(ToToPositionRealmObj.class).equalTo("uniqueId", selectedItem.uniqueId).findFirst();
                                if(null != obj) {
                                    obj.deleteFromRealm();
                                }
                                realm.commitTransaction();
                                selectedItem.state = DELETE;
                                dataModifySet.remove(selectedItem);
                                dataModifySet.add(selectedItem);
                                dataset.remove(selectedItem);
                                mAdapter.notifyDataSetChanged();
                                initEditText();
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
                    if(false == addressText.getText().toString().isEmpty()) {
                        focusTempAdress();
                        return;
                    }
                    Toast.makeText(this, R.string.empty_select_result, Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    String address = addressText.getText().toString();
                    if(selectedItem.addressData != null && address != null && address.compareTo(selectedItem.addressData) != 0) {
                        selectedItem = null;
                        selectItemUniqeId = "";
                        focusTempAdress();
                        return;
                    }
                }

                Intent intent = new Intent();
                intent.putExtra(MARKER_ID, selectedItem.uniqueId);
                intent.putExtra(LATITUDE_POS, selectedItem.latLng.latitude);
                intent.putExtra(LONGITUDE_POS, selectedItem.latLng.longitude);
                Toast.makeText(PositionMangerActivity.this, R.string.focus_ok, Toast.LENGTH_SHORT).show();
                setResult(RESULT_ITEM_SELECT, intent);
                saveEditText();
                finish();
                break;
            case R.id.clear_text_button:
                initEditText();
                break;
        }
    }

    private void focusTempAdress() {
        LatLng targetLatLng = null;
        try {
            targetLatLng = AddressConvert.getLatLng(PositionMangerActivity.this, addressText.getText().toString());
        }
        catch(TimeoutException e) {
            Toast.makeText(PositionMangerActivity.this, R.string.map_address_timeout, Toast.LENGTH_LONG).show();
            targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
        }
        if(targetLatLng == null) {
            Toast.makeText(this, R.string.invalid_address_convert, Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(LATITUDE_POS, targetLatLng.latitude);
        intent.putExtra(LONGITUDE_POS, targetLatLng.longitude);
        Toast.makeText(PositionMangerActivity.this, R.string.focus_ok, Toast.LENGTH_SHORT).show();
        setResult(RESULT_TEMP_POSITION, intent);
        saveEditText();
        finish();
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

    private void loadEditText() {
        SharedPreferences prefs = getSharedPreferences("EditSavePref", MODE_PRIVATE);
        String textTitle = prefs.getString(KEY_TITLE_PREF, "");
        String textBiz = prefs.getString(KEY_BIZ_PREF, "");
        String textAddress = prefs.getString(KEY_ADDRESS_PREF, "");
        int category = prefs.getInt(KEY_CATEGORY_PREF, 0);
        selectItemUniqeId = prefs.getString(KEY_SELECT_ID_PREF, "");
        titleText.setText(textTitle);
        bizText.setText(textBiz);
        s.setSelection(category);
        addressText.setText(textAddress);
    }

    private static String KEY_TITLE_PREF = "key_title_pref";
    private static String KEY_BIZ_PREF = "key_biz_pref";
    private static String KEY_ADDRESS_PREF = "key_address_pref";
    private static String KEY_CATEGORY_PREF = "key_category_pref";
    private static String KEY_SELECT_ID_PREF = "key_select_id_pref";

    private void saveEditText() {
        SharedPreferences prefs = getSharedPreferences("EditSavePref", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_TITLE_PREF, titleText.getText().toString());
        editor.putString(KEY_BIZ_PREF, bizText.getText().toString());
        editor.putString(KEY_ADDRESS_PREF, addressText.getText().toString());
        editor.putString(KEY_SELECT_ID_PREF, selectItemUniqeId == null ? "" : selectItemUniqeId);
        editor.putInt(KEY_CATEGORY_PREF, s.getSelectedItemPosition());
        editor.commit();
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
            case R.id.refresh_map_datafile:
                addLoadMarkerTask = new AddLoadMarkerTask(PositionMangerActivity.this, R.string.wait_for_map_reload_title, R.string.wait_for_map_reload);
                addLoadMarkerTask.execute(false);
                break;
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
        return false;
    }

    public class PositionAdapter extends RecyclerView.Adapter<PositionAdapter.CustomViewHolder> {
        private List<ToToPosition> feedItemList;
        private Context mContext;
        private Map<String, Integer> bizCategoryColorIndexs;

        public PositionAdapter(Context context, List<ToToPosition> feedItemList) {
            this.feedItemList = feedItemList;
            this.mContext = context;
            this.bizCategoryColorIndexs = PositionDataSingleton.getInstance().getBizCategoryColorIndexs();
        }

        @Override
        public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.position_list_row, null);
            CustomViewHolder viewHolder = new CustomViewHolder(view);
            return viewHolder;
        }

        private int[] arrayPinColors = new int[] {
            R.color.DodgerBlue,
                R.color.DarkSlateBlue,
                R.color.Cyan,
                R.color.Lime,
                R.color.Magenta,
                R.color.MediumVioletRed,
                R.color.DarkViolet,
                R.color.Yellow
        };

        @Override
        public void onBindViewHolder(CustomViewHolder customViewHolder, int i) {
            final ToToPosition toToPosition = feedItemList.get(i);
            int colorIndex = -1;

            customViewHolder.msiCode.setText(String.format(getString(R.string.market_msi_code_label), toToPosition.msiCode));
            if (bizCategoryColorIndexs.containsKey(toToPosition.biz)) {
                colorIndex = bizCategoryColorIndexs.get(toToPosition.biz);
            }
            int textColor = getResources().getColor(R.color.textNomalColor);
            if(toToPosition.phone != null && ERROR_CHANNEL.compareTo(toToPosition.phone) == 0) {
                textColor = getResources().getColor(R.color.colorAccent);
            }
            if(colorIndex == -1) {
                customViewHolder.selectItemBtn.setBackgroundResource(R.color.Red);
            }
            else {
                customViewHolder.selectItemBtn.setBackgroundResource(arrayPinColors[colorIndex]);
            }
            if(TextUtils.isEmpty(toToPosition.name) == false) {
                customViewHolder.marketTitle.setTextColor(textColor);
                customViewHolder.marketTitle.setText(toToPosition.name);
            }
            else {
                customViewHolder.marketTitle.setText("");
            }
            if(TextUtils.isEmpty(toToPosition.biz) == false) {
                customViewHolder.selectItemBtn.setText(toToPosition.biz);
                customViewHolder.selectItemBtn.setTextColor(textColor);
            }
            else {
                customViewHolder.selectItemBtn.setText("");
            }
            if(TextUtils.isEmpty(toToPosition.addressData) == false) {
                customViewHolder.marketAddress.setTextColor(textColor);
                customViewHolder.marketAddress.setText(toToPosition.addressData);
            }
            else {
                customViewHolder.marketAddress.setText("");
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
            protected TextView msiCode;
            protected TextView marketTitle;
            protected TextView marketCategory;
            protected TextView marketAddress;
            protected Button selectItemBtn;

            public CustomViewHolder(View view) {
                super(view);
                this.msiCode = (TextView) view.findViewById(R.id.market_msi_code);
                this.marketTitle = (TextView) view.findViewById(R.id.market_title);
                this.marketCategory = (TextView) view.findViewById(R.id.market_category);
                this.marketAddress = (TextView) view.findViewById(R.id.market_address);
                this.selectItemBtn = (Button) view.findViewById(R.id.select_item_btn);
            }
        }
    }

    public class AddLoadMarkerTask extends AsyncTask<Boolean, String, String> {
        ProgressDialog progressDialog;
        Context mContext;
        int mTitle;
        int mMessage;

        public AddLoadMarkerTask(Context context, int title, int message) {
            mContext = context;
            mTitle = title;
            mMessage = message;
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.setCancelable(false);
            progressDialog.setTitle(mTitle);
            progressDialog.setMessage(getResources().getString(mMessage));
            progressDialog.show();
        }

        public void onDestroy() {
            progressDialog.dismiss();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Boolean... params) {
            Realm realm = Realm.getDefaultInstance();
            realm.beginTransaction();
            bizCategoryColorMap.clear();
            bizCategoryColorMap.put(getResources().getString(R.string.none), BitmapDescriptorFactory.HUE_ORANGE);
            List<ToToPosition> positionList = PositionDataSingleton.getInstance().getMarkerPositions();
            markerColorIndex = 0;
            if(params[0] == false) {
                HSSFSheet mySheet = FileManager.getReadExcelSheet(PositionMangerActivity.this,"address.xls");
                if(null == mySheet) {
                    realm.commitTransaction();
                    realm.close();
                    return null;
                }
                int totalCount = mySheet.getLastRowNum();
                publishProgress("max", Integer.toString(totalCount));
                Iterator<Row> rowIter = mySheet.rowIterator();
                // 헤더 부분
                if(rowIter.hasNext()) {
                    HSSFRow myRow = (HSSFRow) rowIter.next();
                }

                RealmResults<ToToPositionRealmObj> objs = realm.where(ToToPositionRealmObj.class).equalTo("msiCode", 0).findAll();
                if(objs.isEmpty() == false){
                    objs.deleteAllFromRealm();
                }

                int timeoutError = 0;
                int rowIndex = 0;
                while(rowIter.hasNext()) {
                    ++rowIndex;
                    publishProgress("progress", Integer.toString(rowIndex), "데이터 읽기" + Integer.toString(rowIndex) + "번 작업중");
                    String[] rawData = new String[LAST_INDEX];
                    Arrays.fill(rawData, ""); //특정 값으로 초기

                    HSSFRow myRow = (HSSFRow) rowIter.next();
                    Iterator<Cell> cellIter = myRow.cellIterator();
                    int i = 0;
                    final ToToPosition toToPosition = new ToToPosition();
                    while (cellIter.hasNext()) {
                        if(i == LAST_INDEX) {
                            break;
                        }
                        HSSFCell myCell = (HSSFCell) cellIter.next();
                        if (ADDRESS1 <= i && i <= ADDRESS5) {
                            toToPosition.addressList.add(myCell.toString());
                            i++;
                        } else {
                            try {
                                rawData[i++] = myCell.toString();
                            }
                            catch (ArrayIndexOutOfBoundsException e) {
                                Log.e("PositionMangerActivity", e.getMessage().toString());
                            }
                        }
                    }

                    try {
                        toToPosition.msiCode = (int) Double.parseDouble(rawData[MSI]);
                    }
                    catch (NumberFormatException e) {
                        // 없는 정보임...
                        continue;
                    }
                    if(containsMSICode(positionList, toToPosition.msiCode)) {
//                        Log.d("READ ", "이미 존재: " + toToPosition.msiCode);
                        continue;
                    }
//                    Log.e("READ ", "                   ================= 신규: " + toToPosition.msiCode);
                    toToPosition.uniqueId = UUID.randomUUID().toString();
                    toToPosition.name = rawData[NAME];
                    toToPosition.biz = rawData[BUSINESS];
                    toToPosition.channel = rawData[CHANNEL];
                    toToPosition.bizState = rawData[BIZSTATE];
                    toToPosition.phone = rawData[PHONE];
                    toToPosition.addressData = TextUtils.join(" ", toToPosition.addressList);
                    LatLng targetLatLng = null;
                    try {
                        targetLatLng = AddressConvert.getLatLng(PositionMangerActivity.this, toToPosition.addressData);
                        if (targetLatLng == null) {
                            targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                            toToPosition.phone = ERROR_CHANNEL;
                        }
                    } catch (TimeoutException e) {
                        targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                        if(timeoutError++ < 10) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(PositionMangerActivity.this, R.string.map_address_timeout, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                    toToPosition.latLng = targetLatLng;
                    positionList.add(toToPosition);

                    // 파일에서 읽은 엑셀 정보를 디비 컬럼에 넣음
                    ToToPositionRealmObj obj = realm.createObject(ToToPositionRealmObj.class);
                    obj.setUniqueId(toToPosition.uniqueId);
                    obj.setBizState(toToPosition.bizState);
                    obj.setTargetName(toToPosition.name);
                    obj.setTargetBiz(toToPosition.biz);
                    obj.setPhone(toToPosition.phone);
                    obj.setAddressData(toToPosition.addressData);
                    obj.setLatitude(toToPosition.latLng.latitude);
                    obj.setLongtitude(toToPosition.latLng.longitude);
                    obj.setMsiCode(toToPosition.msiCode);
                }
            }
            else {
                // 디비에서 새로 읽을 일 없을 것 같음
            }

            final Map<String, Integer> bizCategoryColorIndexs = PositionDataSingleton.getInstance().getBizCategoryColorIndexs();
            for(final ToToPosition position: positionList) {
                if (bizCategoryColorMap.containsKey(position.biz) == false) {
                    bizCategoryColorIndexs.put(position.biz, markerColorIndex % arrayPinColors.length);
                    bizCategoryColorMap.put(position.biz, arrayPinColors[markerColorIndex++ % arrayPinColors.length]);
                }
            }

            realm.commitTransaction();
            realm.close();
            return null;
        }

        @Override
        protected void onProgressUpdate(String... progress) {
            if (progress[0].equals("progress")) {
                progressDialog.setProgress(Integer.parseInt(progress[1]));
                progressDialog.setMessage(progress[2]);
            }
            else if (progress[0].equals("max")) {
                progressDialog.setMax(Integer.parseInt(progress[1]));
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressDialog.dismiss();
            dataset = PositionDataSingleton.getInstance().getMarkerPositions();
            for(ToToPosition item: dataset) {
                if(item.msiCode == 0) {
                    dataset.remove(item);
                }
            }
            mAdapter = new PositionAdapter(PositionMangerActivity.this, dataset);
            mRecyclerView.setAdapter(mAdapter);
        }
    };


}
