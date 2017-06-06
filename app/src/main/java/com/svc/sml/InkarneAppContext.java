package com.svc.sml;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.svc.sml.Database.ComboData;
import com.svc.sml.Database.InkarneDataSource;
import com.svc.sml.Database.User;
import com.svc.sml.Utility.AWSUtil;
import com.svc.sml.Utility.Connectivity;
import com.svc.sml.Utility.ConstantsUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by himanshu on 1/12/16.
 */
public class InkarneAppContext extends Application {
    private final static String LOGTAG = InkarneAppContext.class.toString();
    public final static String SHARED_PREF_FIEST_TIME_COMBO_RENDERED = "is_first_time_combo_Rendered";
    private static Context context;
    private static InkarneDataSource dataSource;
    private static TransferUtility transferUtility;
    private static int cartNumber = -1;

    public static ComboData combData;

    public static String adjustPicActivityPicPath;
    public static String fiducialPicPath;

    //private static boolean isWifiOnlyDownload;

    public static boolean isAddFaceForRedoAvatar = false;
    public static boolean shouldRearrangeLooks = true;
    private Timer mActivityTransitionTimer;
    private TimerTask mActivityTransitionTimerTask;
    public boolean wasInBackground;
    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;

    public void startActivityTransitionTimer() {
        this.mActivityTransitionTimer = new Timer();
        this.mActivityTransitionTimerTask = new TimerTask() {
            public void run() {
                InkarneAppContext.this.wasInBackground = true;
            }
        };

        this.mActivityTransitionTimer.schedule(mActivityTransitionTimerTask,
                MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer() {
        if (this.mActivityTransitionTimerTask != null) {
            this.mActivityTransitionTimerTask.cancel();
        }

        if (this.mActivityTransitionTimer != null) {
            this.mActivityTransitionTimer.cancel();
        }

        this.wasInBackground = false;
    }

    public static boolean isDefaultFaceChanged() {
        return isDefaultFaceChanged;
    }

    public static void setIsDefaultFaceChanged(boolean isDefaultFaceChanged) {
        InkarneAppContext.isDefaultFaceChanged = isDefaultFaceChanged;
    }

    public static void setCartNumber(int cartNumbers){
        SharedPreferences settings = context.getSharedPreferences("inkarne", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(ConstantsUtil.SETTING_KEY_CART_NUMBER, cartNumbers);
        editor.commit();
        cartNumber = cartNumbers;
    }

    public static int getCartNumber(){
        if(cartNumber == -1) {
            SharedPreferences settings = context.getSharedPreferences("inkarne", 0);
            cartNumber = settings.getInt(ConstantsUtil.SETTING_KEY_CART_NUMBER, 0);
            return cartNumber;
        }
        else return cartNumber;
    }

    public static boolean isFirstTimeComboRender(){
        SharedPreferences settings = context.getSharedPreferences("inkarne", 0);
        return settings.getBoolean(SHARED_PREF_FIEST_TIME_COMBO_RENDERED, true);
    }
    public static void setFirstTimeComboRender(boolean isFirstTimeComboRender){
        SharedPreferences settings = context.getSharedPreferences("inkarne", 0);
        SharedPreferences.Editor e = settings.edit();
        e.putBoolean(SHARED_PREF_FIEST_TIME_COMBO_RENDERED, isFirstTimeComboRender);
        e.commit();
    }

    public static void incrementCartNumber(int increment){
        int cartNumber = getCartNumber();
        cartNumber += increment;
        setCartNumber(cartNumber);
    }

    private static Typeface inkarneTypeFaceHeader = null;
    private static Typeface inkarneTypeFaceML = null;
    private static Typeface inkarneTypeOpensans = null;
    private static Typeface inkarneTypeFaceMolengo = null;
    private static Typeface inkarneTypeFaceFutura = null;

    //private static final String TRACKING_ID = "UA-81490524-1";
    private static final String TRACKING_ID = "UA-81490524-2";
    private static final String TEST_TRACKING_ID="UA-81467859-1";
    private Tracker mTracker;
    private static final boolean isTest = false;

    private static  boolean isDefaultFaceChanged;
    @Override public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        //LogSecretKey();//TODO
       // SharedPreferences settings = InkarneAppContext.getAppContext().getSharedPreferences("inkarne", 0);
        SharedPreferences settings = context.getSharedPreferences("inkarne", 0);
        isDefaultFaceChanged = settings.getBoolean(User.SHARED_PREF_IS_DEFAULT_FACE_CHANGED, true);
    }



    public static Context getAppContext() {
        return InkarneAppContext.context;
    }
    public static InkarneDataSource getDataSource(){
        if(dataSource == null)
            dataSource = InkarneDataSource.getInstance(context);
        if(dataSource.getDatabase() == null || !dataSource.getDatabase().isOpen())
            dataSource.open();
        return dataSource;
    }

    synchronized public Tracker getTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(context);
// To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            if(isTest)
                mTracker = analytics.newTracker(TEST_TRACKING_ID);
            else
                mTracker = analytics.newTracker(TRACKING_ID);
        }
        return mTracker;
    }

//    public static boolean isOnline() {
//        //ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
//        ConnectivityManager cm = (ConnectivityManager) getAppContext().getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo netInfo = cm.getActiveNetworkInfo();
//        if (netInfo != null && netInfo.isConnectedOrConnecting()) {
//            Log.e(LOGTAG,"*** network connected *****");
//            return true;
//        } else {
//            Log.e(LOGTAG,"**** network not connected *****");
//            return false;
//        }
//    }

    public static TransferUtility getTransferUtility() {
        if(transferUtility == null) {
            transferUtility = AWSUtil.getTransferUtility(context);
        }

        return transferUtility;
    }

    public static void setTransferUtility(TransferUtility transferUtility) {
        InkarneAppContext.transferUtility = transferUtility;
    }

    public static void cleanUpDownload(ArrayList<TransferObserver>observers){
        for (TransferObserver ob : observers) {
            if (transferUtility != null)
                transferUtility.cancel(ob.getId());
            ob.cleanTransferListener();
        }
        observers.clear();
    }

    public static Typeface getInkarneTypeFaceMolengo(){
        if(inkarneTypeFaceMolengo ==null) {
            String fontPath = "fonts/Molengo-Regular.ttf";
            inkarneTypeFaceMolengo = Typeface.createFromAsset(context.getAssets(), fontPath);
        }
        return inkarneTypeFaceMolengo;
    }


    public static Typeface getInkarneTypeFaceHeaderJennaSue(){
        if(inkarneTypeFaceHeader ==null) {
            //String fontPath = "fonts/font-lobster-two-regular.otf";
            String fontPath = "fonts/Jenna_sue.ttf";
            inkarneTypeFaceHeader = Typeface.createFromAsset(context.getAssets(), fontPath);
        }
        return inkarneTypeFaceHeader;
    }

    public static Typeface getInkarneTypeFaceML(){
        if(inkarneTypeFaceML ==null) {
            //String fontPath = "fonts/font-lobster-two-regular.otf";
            String fontPath = "fonts/font-moon-light.otf";
            inkarneTypeFaceML = Typeface.createFromAsset(context.getAssets(), fontPath);
        }
        return inkarneTypeFaceML;
    }

    public static Typeface getInkarneTypeOpensans(){
        if(inkarneTypeOpensans ==null) {
            //String fontPath = "fonts/font-moon-light.otf";
            //String fontPath = "fonts/montserrat_regular.ttf";
            String fontPath = "fonts/opensans-regular.ttf";
            inkarneTypeOpensans = Typeface.createFromAsset(context.getAssets(), fontPath);
        }
        return inkarneTypeOpensans;
    }


    public static SQLiteDatabase getOpenedDB() {
        Log.i("debug", "Database opened");
        if(getDataSource().getDatabase() == null || !getDataSource().getDatabase().isOpen())
            getDataSource().open();
        return getDataSource().getDatabase();
    }
    public static void closeDB() {
        Log.i("debug", "Database opened");
        getDataSource().close();
    }


    /* Checks if external storage is available for read and write */
    public boolean isExtStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExtStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public static void showNetworkAlert(){
        if(!Connectivity.isConnected(getAppContext())){
            Toast.makeText(getAppContext(), ConstantsUtil.MESSAGE_TOAST_NETWORK_RESPONSE_FAILED,Toast.LENGTH_SHORT).show();
        }
    }

    public static void showAlert(String message){
        if(Connectivity.isConnected(getAppContext())){
            Toast.makeText(getAppContext(), message,Toast.LENGTH_SHORT).show();
        }
    }

    public static void saveComboToPref(ComboData combodata) {
        combData = combodata;
    }

    public static ComboData getComboFromPref() {
        return combData;

    }

    public static boolean getSettingIsWifiOnlyDownload() {
        SharedPreferences settings = InkarneAppContext.getAppContext().getSharedPreferences("inkarne", 0);
        boolean shouldDownloadWithoutWifi = settings.getBoolean(ConstantsUtil.SETTING_KEY_IS_DOWNLOAD_WIFI_ONLY, false);
        return shouldDownloadWithoutWifi;
    }
    public static void saveSettingIsWifiOnlyDownload(boolean isChecked) {
        SharedPreferences settings = InkarneAppContext.getAppContext().getSharedPreferences("inkarne", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(ConstantsUtil.SETTING_KEY_IS_DOWNLOAD_WIFI_ONLY, isChecked);
        editor.commit();
        //setIsDefaultFaceChanged(isChecked);
    }

    public static boolean getSettingIsAutoRotateLookDisabled() {
        boolean shouldAutoRotate = getSP().getBoolean(ConstantsUtil.SETTING_KEY_IS_AUTO_ROTATE_LOOK, false);
        return shouldAutoRotate;
    }
    public static void saveSettingIsAutoRotateLookDisabled(boolean isChecked) {
        SharedPreferences.Editor editor = getSP().edit();
        editor.putBoolean(ConstantsUtil.SETTING_KEY_IS_AUTO_ROTATE_LOOK, isChecked);
        editor.commit();
    }

    public static void saveSettingIsDefaultFaceChanged(boolean isChanged) {
        SharedPreferences settings = InkarneAppContext.getAppContext().getSharedPreferences("inkarne", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(User.SHARED_PREF_IS_DEFAULT_FACE_CHANGED, isChanged);
        editor.commit();
        isDefaultFaceChanged = isChanged;
    }


    //NOTE: TO KET SECRETKEY USED IN MSG91 SEND OTP , application creation on console
    public static void LogSecretKey() {
        MessageDigest md = null;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(),
                    PackageManager.GET_SIGNATURES);

            for (Signature signature : info.signatures) {
                md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
        String key = Base64.encodeToString(md.digest(), Base64.DEFAULT);
        Log.i("SecretKey = ", key);
        Toast.makeText(getAppContext(),key,Toast.LENGTH_LONG).show();
        //writeToFile("key",key);

    }

    public static void writeToFile(String fileName, String body)
    {
        FileOutputStream fos = null;
        try {
            final File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/msg99/" );
            if (!dir.exists())
            {
                if(!dir.mkdirs()){
                    Log.e("ALERT","could not create the directories");
                }
            }
            final File myFile = new File(dir, fileName + ".txt");
            if (!myFile.exists())
            {
                myFile.createNewFile();
            }
            fos = new FileOutputStream(myFile);
            fos.write(body.getBytes());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /******************SETTINGS *******************
     *
     */
    public static SharedPreferences getSP(){
        SharedPreferences settings = InkarneAppContext.getAppContext().getSharedPreferences("inkarne", 0);
        return settings;
    }

    public static int getSPValue(String key,int defaultValue){
        return  getSP().getInt(key, defaultValue);
    }


    public static String getSPValue(String key,String defValue){
        return  getSP().getString(key, defValue);
    }

    public static boolean getSPValue(String key,boolean defaultValue){
        return  getSP().getBoolean(key, defaultValue);
    }


    public static void saveSPValue(String key,int value){
        getSP().edit().putInt(key,value).commit();
    }


    public static void saveSPValue(String key,String value){
        getSP().edit().putString(key, value).commit();
    }

    public static void saveSPValue(String key,boolean value){
        getSP().edit().putBoolean(key, value).commit();
    }
}
