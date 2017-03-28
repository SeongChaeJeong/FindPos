package com.demco.goopy.findtoto;

import android.app.Application;
import android.support.multidex.MultiDexApplication;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by goopy on 2017-03-27.
 */
@ReportsCrashes(
        mailTo = "goopy80@daum.net",
        mode = ReportingInteractionMode.SILENT,
        resToastText = R.string.error_crash
)
public class MyApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
//        ACRA.init(this);
    }
}
