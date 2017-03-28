package com.demco.goopy.findtoto.Utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.demco.goopy.findtoto.Data.PositionDataSingleton;
import com.demco.goopy.findtoto.Data.ToToPosition;
import com.demco.goopy.findtoto.R;

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

import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS1;
import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS2;
import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS3;
import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS4;
import static com.demco.goopy.findtoto.Data.ToToPosition.ADDRESS5;
import static com.demco.goopy.findtoto.Data.ToToPosition.BUSINESS;
import static com.demco.goopy.findtoto.Data.ToToPosition.CHANNEL;
import static com.demco.goopy.findtoto.Data.ToToPosition.NAME;
import static com.demco.goopy.findtoto.Data.ToToPosition.PHONE;
import static com.demco.goopy.findtoto.Data.ToToPosition.STATE;

/**
 * Created by goopy on 2017-03-23.
 */

public class FileManager {
    public static int UNIQUE_INDEX = 1;

    public static boolean saveExcelFile(Context context, String fileName) {

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
        sheet1 = wb.createSheet("myOrder");
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
            c.setCellValue(position.rawData[NAME]);

            c = row.createCell(1);
            c.setCellValue(position.rawData[BUSINESS]);

            c = row.createCell(2);
            c.setCellValue(position.rawData[CHANNEL]);

            c = row.createCell(3);
            c.setCellValue(position.addressList.get(0));

            c = row.createCell(4);
            c.setCellValue(position.addressList.get(1));

            c = row.createCell(5);
            c.setCellValue(position.addressList.get(2));

            c = row.createCell(6);
            c.setCellValue(position.addressList.get(3));

            c = row.createCell(7);
            c.setCellValue(position.addressList.get(4));

            c = row.createCell(8);
            c.setCellValue(position.rawData[STATE]);

            c = row.createCell(9);
            c.setCellValue(position.rawData[PHONE]);
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
            Toast.makeText(context, R.string.permission_not_exist, Toast.LENGTH_LONG).show();
            return false;
        }

        List<ToToPosition> positionList = PositionDataSingleton.getInstance().getMarkerPositions();

        try{
            // Creating Input Stream
            File file = new File(context.getExternalFilesDir(null), filename);
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
            while(rowIter.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator<Cell> cellIter = myRow.cellIterator();
                int i = 0;
                ToToPosition toToPosition = new ToToPosition();
                toToPosition.uniqueId = ++UNIQUE_INDEX;
                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    if(ADDRESS1 <= i && i <= ADDRESS5) {
                        toToPosition.addressList.add(myCell.toString());
                        i++;
                    }
                    else {
                        toToPosition.rawData[i++] = myCell.toString();
                    }
                    Log.d("FileUtils", "Cell Value: " +  myCell.toString());
                }
                positionList.add(toToPosition);
            }
        }catch (Exception e){
            Toast.makeText(context, R.string.file_not_exist, Toast.LENGTH_LONG).show();
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
