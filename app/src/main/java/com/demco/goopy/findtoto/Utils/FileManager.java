package com.demco.goopy.findtoto.Utils;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.R;
import com.google.android.gms.maps.model.LatLng;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static com.demco.goopy.findtoto.MapsActivity.defaultLatitude;
import static com.demco.goopy.findtoto.MapsActivity.defaultLongitude;

/**
 * Created by goopy on 2017-03-23.
 */

public class FileManager {

    public static int NAME = 0;
    public static int BUSINESS = 1;
    public static int CHANNEL = 2;
    public static int ADDRESS1 = 3;
    public static int ADDRESS2 = 4;
    public static int ADDRESS3 = 5;
    public static int ADDRESS4 = 6;
    public static int ADDRESS5 = 7;
    public static int BIZSTATE = 8;
    public static int PHONE = 9;
    public static int LAST_INDEX = 10;


    public static final String RECEIVEFILE_DIR = "/dmko";
    public static final String RECEIVEFILE_FOLDER_FULLPATH = Environment.getDataDirectory().getAbsolutePath() + RECEIVEFILE_DIR;
    public static final String RECEIVEFILE_FOLDER_FULLPATH2 = Environment.getExternalStorageDirectory().getAbsolutePath() + RECEIVEFILE_DIR;



    public static File createDirIfNotExistsDir(String path) {

        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
//                Toast.makeText(t"Problem creating Image folder");
            }
        }
        return file;
    }

    public static boolean saveExcelFile(Context context, String fileName) {

        createDirIfNotExistsDir(context.getExternalFilesDir(null).getAbsolutePath());
        // check if available and not read only
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Log.w("FileUtils", "Storage not available or read only");
            return false;
        }

        boolean success = false;
        List<ToToPosition> toToPositionList = PositionDataSingleton.getInstance().getMarkerPositions();
        //New Workbook
        Workbook wb = new HSSFWorkbook();

        Cell c = null;

        //Cell style for header row
        CellStyle cs = wb.createCellStyle();
        cs.setFillForegroundColor(HSSFColor.LIME.index);
        cs.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);


        //New Sheet
        Sheet sheet1 = null;
        sheet1 = wb.createSheet("totoList");
        sheet1.setColumnWidth(0, (15 * 500));
        sheet1.setColumnWidth(1, (15 * 500));
        sheet1.setColumnWidth(2, (15 * 500));

        Row row = sheet1.createRow(0);
        c = row.createCell(0);
        c.setCellValue(context.getResources().getString(R.string.name_label));
        c.setCellStyle(cs);
        c = row.createCell(1);
        c.setCellValue(context.getResources().getString(R.string.biz_label));
        c.setCellStyle(cs);
        c = row.createCell(2);
        c.setCellValue(context.getResources().getString(R.string.chanel_label));
        c.setCellStyle(cs);
        c = row.createCell(3);
        c.setCellValue(context.getResources().getString(R.string.address_label1));
        c.setCellStyle(cs);
        c = row.createCell(4);
        c.setCellValue(context.getResources().getString(R.string.address_label2));
        c.setCellStyle(cs);
        c = row.createCell(5);
        c.setCellValue(context.getResources().getString(R.string.address_label3));
        c.setCellStyle(cs);
        c = row.createCell(6);
        c.setCellValue(context.getResources().getString(R.string.address_label4));
        c.setCellStyle(cs);
        c = row.createCell(7);
        c.setCellValue(context.getResources().getString(R.string.address_label5));
        c.setCellStyle(cs);
        c = row.createCell(8);
        c.setCellValue(context.getResources().getString(R.string.state_label));
        c.setCellStyle(cs);
        c = row.createCell(9);
        c.setCellValue(context.getResources().getString(R.string.phone_label));
        c.setCellStyle(cs);

        int rowIndex = 1;
        for(ToToPosition position : toToPositionList) {
            // Generate column headings
            row = sheet1.createRow(rowIndex++);

            c = row.createCell(0);
            c.setCellValue(position.name);

            c = row.createCell(1);
            c.setCellValue(position.biz);

            c = row.createCell(2);
            c.setCellValue(position.channel);

            // 주소 정보를 토큰해서 저장
            String[] splitAddress = TextUtils.split(position.addressData, " ");
            position.addressList.clear();
            for(int i = 0; i < splitAddress.length; ++i) {
                position.addressList.add(splitAddress[i]);
            }
            for(int i = 0; i < 5; ++i) {
                c = row.createCell(i + 3);
                if(i < position.addressList.size()) {
                    c.setCellValue(position.addressList.get(i));
                }
                else {
                    c.setCellValue("");
                }
            }

            c = row.createCell(8);
            c.setCellValue(position.bizState);

            c = row.createCell(9);
            c.setCellValue(position.phone);
        }

        // Create a path where we will place our List of objects on external storage
        File file = new File(context.getExternalFilesDir(null), fileName);
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(file);
            wb.write(os);
            Log.w("FileUtils", "Writing file" + file);
            success = true;
        } catch (IOException e) {
            Log.w("FileUtils", "Error writing " + file, e);
        } catch (Exception e) {
            Log.w("FileUtils", "Failed to save file", e);
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception ex) {
            }
        }

        return success;
    }

    public static boolean readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.w("FileUtils", "Storage not available or read only");
//            Toast.makeText(context, R.string.permission_not_exist, Toast.LENGTH_LONG).show();
            return false;
        }
        String folderPath = context.getExternalFilesDir(null).getAbsolutePath();
        List<ToToPosition> positionList = PositionDataSingleton.getInstance().getMarkerPositions();
        File destDir = createDirIfNotExistsDir(folderPath);

        try{
            // Creating Input Stream
            File file = new File(destDir, filename);
            if(file.exists() == false) {
//                Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_LONG).show();
                return false;
            }
            FileInputStream myInput = new FileInputStream(file);

            // Create a POIFSFileSystem object
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);

            // Create a workbook using the File System
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            // Get the first sheet from workbook
            HSSFSheet mySheet = myWorkBook.getSheetAt(0);

            /** We now need something to iterate through the cells.**/
            Iterator<Row> rowIter = mySheet.rowIterator();
            // 헤더 부분
            if(rowIter.hasNext()) {
                HSSFRow myRow = (HSSFRow) rowIter.next();
            }

            positionList.clear();
            boolean timeoutError = false;
            while(rowIter.hasNext()){
                String[] rawData = new String[LAST_INDEX];
                for(int i = 0; i < LAST_INDEX; ++i) {
                    rawData[i] = "";
                }
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator<Cell> cellIter = myRow.cellIterator();
                int i = 0;
                ToToPosition toToPosition = new ToToPosition();
                toToPosition.uniqueId = UUID.randomUUID().toString();
                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    if(ADDRESS1 <= i && i <= ADDRESS5) {
                        toToPosition.addressList.add(myCell.toString());
                        i++;
                    }
                    else {
                        rawData[i++] = myCell.toString();
                    }
                    Log.d("FileUtils", "Cell Value: " +  myCell.toString());
                }
                toToPosition.name = rawData[NAME];
                toToPosition.biz = rawData[BUSINESS];
                toToPosition.channel = rawData[CHANNEL];
                toToPosition.bizState = rawData[BIZSTATE];
                toToPosition.phone = rawData[PHONE];
                toToPosition.addressData = TextUtils.join(" ", toToPosition.addressList);
                LatLng targetLatLng = null;
                try {
                    if(timeoutError) {
                        targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                    }
                    else {
                        targetLatLng = AddressConvert.getLatLng(context, toToPosition.addressData);
                        if(targetLatLng == null) {
                            targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                        }
                    }
                }
                catch(TimeoutException e) {
                    timeoutError = true;
                    targetLatLng = new LatLng(defaultLatitude, defaultLongitude);
                }
                toToPosition.latLng = targetLatLng;
                positionList.add(toToPosition);
            }
        }catch (Exception e){
//            Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
}
