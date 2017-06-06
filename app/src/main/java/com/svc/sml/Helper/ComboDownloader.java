package com.svc.sml.Helper;

import android.content.ContentValues;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.svc.sml.Database.ComboData;
import com.svc.sml.Database.DatabaseHandler;
import com.svc.sml.Database.InkarneDataSource;
import com.svc.sml.Database.User;
import com.svc.sml.InkarneAppContext;
import com.svc.sml.Model.BaseAccessoryItem;
import com.svc.sml.Utility.AWSUtil;
import com.svc.sml.Utility.ConstantsUtil;
import com.svc.sml.Utility.Unzip;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by himanshu on 5/26/16.
 */
public class ComboDownloader {

    private final static String LOGTAG = ComboDownloader.class.toString();
    private static final int INDEX_DOWNLOAD_LEG_OBJ = 11;
    private static final int INDEX_DOWNLOAD_LEG_TEX = 111;
    private static final int INDEX_DOWNLOAD_A1_OBJ = 0;
    private static final int INDEX_DOWNLOAD_A1_TEX = 1;
    private static final int INDEX_DOWNLOAD_A6_OBJ = 6;
    private static final int INDEX_DOWNLOAD_A6_TEX = 61;
    private static final int INDEX_DOWNLOAD_A7_OBJ = 7;
    private static final int INDEX_DOWNLOAD_A7_TEX = 71;
    private static final int INDEX_DOWNLOAD_A8_OBJ = 8;
    private static final int INDEX_DOWNLOAD_A8_TEX = 81;
    private static final int INDEX_DOWNLOAD_A9_OBJ = 9;
    private static final int INDEX_DOWNLOAD_A9_TEX = 91;
    private static final int INDEX_DOWNLOAD_A10_OBJ = 10;
    private static final int INDEX_DOWNLOAD_A10_TEX = 101;

    //private Context context;
    private String textureKeyPath;
    private String objKeyPath;
    private WeakReference<OnComboDownloadListener> onComboDownloadWeakListener;
    private OnComboDownloadListener onComboDownloadListener;
    private boolean downloadedSuccessful = false;
    private TransferUtility transferUtility;
    private InkarneDataSource dataSource;
    protected ArrayList<TransferObserver> observers = new ArrayList<TransferObserver>();
    private int totalCountTobeDownloaded;
    private Unzip unzip = new Unzip();

    public interface OnComboDownloadListener {
        void onDownload(ComboData comboData);
        void onDownloadFailed(String comboId);
        void onDownloadProgress(String comboId,int percentage);
        void onComboInfoFailed(String comboId,int error_code);
        void onComboInfoResponse(String comboId);
    }

    public ComboDownloader(Context ctx) {
        //this.context = ctx;
        this.transferUtility = AWSUtil.getTransferUtility(ctx);
//        int SDK_INT = android.os.Build.VERSION.SDK_INT;
//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }
    }

    public ComboDownloader(Context ctx,final ComboData comboData, OnComboDownloadListener onComboDownloadListener) {
        this.transferUtility = AWSUtil.getTransferUtility(ctx);
        this.dataSource = InkarneAppContext.getDataSource();

        //this.context = ctx;
        //this.onComboDownloadWeakListener =  new WeakReference<OnComboDownloadListener>(onComboDownloadListener);
        this.onComboDownloadListener = onComboDownloadListener;
       new Thread(new Runnable() {
           @Override
           public void run() {
               startSkusDownloadService(comboData);
           }
       }).start();
    }

    protected void removeTransferListener() {
        Log.w(LOGTAG, "removeTransferListener");
        for (TransferObserver ob : observers) {
            if (transferUtility != null)
                transferUtility.cancel(ob.getId());
            ob.cleanTransferListener();
        }
        observers.clear();
    }

    public void startSkusDownloadService(final ComboData combodata) {
        Log.d(LOGTAG, "LoadCombo getComboInfo " + combodata.getCombo_ID());
         ComboData comboData = combodata;
        if (combodata.getLegId() == null || combodata.getLegId().isEmpty() || combodata.getmA7_Obj_Key_Name()== null || combodata.getmA7_Obj_Key_Name().isEmpty()) {
            comboData = dataSource.getComboDataByComboID(combodata.getCombo_ID());
        }
        if (comboData != null && comboData.getLegId() != null && !comboData.getLegId().isEmpty() && combodata.getmA1_Obj_Key_Name()!= null && !combodata.getmA1_Obj_Key_Name().isEmpty()) {
            Log.d(LOGTAG, "getComboInfo 2 " + comboData.getCombo_ID());
            downloadSkus(comboData);
        } else {

            DataManager.getInstance().requestComboDataInfoById(combodata.getCombo_ID(), User.getInstance().getDefaultFaceId(),
                    new DataManager.OnResponseHandlerInterface() {
                        @Override
                        public void onResponse(Object obj) {
                            ArrayList<ComboData> arrayListCombo = (ArrayList<ComboData>) obj;
                            if (arrayListCombo != null && arrayListCombo.size() > 0) {

                                if(onComboDownloadListener != null)
                                    onComboDownloadListener.onComboInfoResponse(combodata.getCombo_ID());

                                Log.d(LOGTAG, "getComboInfo 3 " + combodata.getCombo_ID());
                                ComboData comboData = (ComboData) arrayListCombo.get(0);
                                comboData = dataSource.getComboDataByComboID(comboData.getCombo_ID());
                                downloadSkus(comboData);
                            } else {
                                //Toast.makeText(getActivity(), "Looks Info not available ", Toast.LENGTH_SHORT).show();
//                                if(onComboDownloadWeakListener != null) {
//                                    OnComboDownloadListener listener =  onComboDownloadWeakListener.get();
//                                    if(listener != null)
//                                        listener.onComboInfoFailed(combodata.getCombo_ID(),2);
//                                }
                                if(onComboDownloadListener != null)
                                    onComboDownloadListener.onComboInfoFailed(combodata.getCombo_ID(),2);
                            }
                        }

                        @Override
                        public void onResponseError(String errorMessage, int errorCode) {
                            Log.e(LOGTAG, "Error getting comboInfo :" + errorMessage);
//                            if(onComboDownloadWeakListener != null) {
//                                OnComboDownloadListener listener =  onComboDownloadWeakListener.get();
//                                if(listener != null)
//                                    listener.onComboInfoFailed(combodata.getCombo_ID(),errorCode);
//                            }
                            if(onComboDownloadListener != null)
                                onComboDownloadListener.onComboInfoFailed(combodata.getCombo_ID(),errorCode);

                        }
                    });
        }
    }

    protected void downloadSkus(ComboData combodata) {
        if(combodata.getLegItem() == null){
            combodata.countTobeDownloaded = 10;
            totalCountTobeDownloaded = 10;
        }else{
            combodata.countTobeDownloaded = 12;
            totalCountTobeDownloaded = 12;
            beginDownload(combodata.getLegItem().getObjAwsKey(), INDEX_DOWNLOAD_LEG_OBJ, combodata.getLegItem().getObjDStatus(), combodata);
            beginDownload(combodata.getLegItem().getTextureAwsKey(), INDEX_DOWNLOAD_LEG_TEX, combodata.getLegItem().getTextureDStatus(), combodata);
        }
        Log.w(LOGTAG,"downloadSkus : " + totalCountTobeDownloaded );

        beginDownload(combodata.getmA1_Obj_Key_Name(), INDEX_DOWNLOAD_A1_OBJ, combodata.getObjA1DStatus(), combodata);
        beginDownload(combodata.getmA1_Png_Key_Name(), INDEX_DOWNLOAD_A1_TEX, combodata.getTextureA1DStatus(), combodata);

        beginDownload(combodata.getmA6_Obj_Key_Name(), INDEX_DOWNLOAD_A6_OBJ, combodata.getObjA6DStatus(), combodata);
        beginDownload(combodata.getmA6_Png_Key_Name(), INDEX_DOWNLOAD_A6_TEX, combodata.getTextureA6DStatus(), combodata);

        beginDownload(combodata.getmA7_Obj_Key_Name(), INDEX_DOWNLOAD_A7_OBJ, combodata.getObjA7DStatus(), combodata);
        beginDownload(combodata.getmA7_Png_Key_Name(), INDEX_DOWNLOAD_A7_TEX, combodata.getTextureA7DStatus(), combodata);

        beginDownload(combodata.getmA9_Obj_Key_Name(), INDEX_DOWNLOAD_A9_OBJ, combodata.getObjA9DStatus(), combodata);
        beginDownload(combodata.getmA9_Png_Key_Name(), INDEX_DOWNLOAD_A9_TEX, combodata.getTextureA9DStatus(), combodata);

        beginDownload(combodata.getmA10_Obj_Key_Name(), INDEX_DOWNLOAD_A10_OBJ, combodata.getObjA10DStatus(), combodata);
        beginDownload(combodata.getmA10_Png_Key_Name(), INDEX_DOWNLOAD_A10_TEX, combodata.getTextureA10DStatus(), combodata);
    }


    private void beginDownload(String key, int downloadIndex, int downloadStatus, ComboData comboData) {
        if (key == null || key.length() < 2 || key.equals("null")) {
            Log.d(LOGTAG, "combo :" + comboData.getCombo_ID() + " download key is null or blank ");
            comboData.countTobeDownloaded--;
            if (comboData.countTobeDownloaded == 0)
                checkDownload(comboData);
            return;
        }

        if (downloadStatus == ConstantsUtil.EDownloadStatusType.eDownloaded.intStatus() && ConstantsUtil.checkFileKeysExist(key)) {
            Log.e(LOGTAG, " already downloaded : " + key);
            comboData.countTobeDownloaded--;
            if (comboData.countTobeDownloaded == 0)
                checkDownload(comboData);
            return;
        }


        updateDownloadStatus(comboData,downloadIndex,ConstantsUtil.EDownloadStatusType.eDownloading.intStatus());
        if(key.contains(".ply") ){//TODO zip//&& !key.contains("legs/"
            key = key.replace(".ply",".gz");
        }
        File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT + key);
        //file.delete();//TODO
        Log.d(LOGTAG, "combo :" + comboData.getCombo_ID() + "  to be downloaded. file:  " + key);
        if (transferUtility == null) {
            Log.d(LOGTAG, " transferUtility is null. file:  " + key);
            return;
        }
        TransferObserver observer = transferUtility.download(ConstantsUtil.AWSBucketName, key, file);
        Log.d(LOGTAG, " transferUtility " + key);
        DownloadListener dListener = new DownloadListener(key, downloadIndex, comboData);
        observer.setTransferListener(dListener);
        observers.add(observer);
    }

    private class DownloadListener implements TransferListener {
        public DownloadListener() {

        }

        private ComboData comboData = null;
        private String key = "";
        private int downloadIndex = -1;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public DownloadListener(String key, int downloadIndex, ComboData comboData) {
            this.comboData = comboData;
            this.key = key;
            this.downloadIndex = downloadIndex;
        }

        @Override
        public void onError(int id, Exception e) {
            Log.e(LOGTAG, e.getStackTrace() + " Error during download  :" + key);
            removeTransferListener();
            updateDownloadStatus(comboData, downloadIndex, ConstantsUtil.EDownloadStatusType.eDownloadError.intStatus());
            onDownloadFailed(comboData);
        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            /*
            int percentage = comboData.countTobeDownloaded*100/totalCountTobeDownloaded;
            if(onComboDownloadWeakListener != null) {
                OnComboDownloadListener listener =  onComboDownloadWeakListener.get();
                if(listener != null)
                    listener.onDownloadProgress(comboData.getCombo_ID(),percentage);
            }
            */
        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            if (state == TransferState.FAILED) {
                Log.e(LOGTAG, "combo :" + comboData.getCombo_ID() + " Download failed :" + key);
                removeTransferListener();
                updateDownloadStatus(comboData, downloadIndex, ConstantsUtil.EDownloadStatusType.eDownloadError.intStatus());
                onDownloadFailed(comboData);
            } else if (state == TransferState.COMPLETED) {
                String unZipFile = "";
                if(key.contains(".gz")) {
                    Log.e(LOGTAG,"zip :"+key);
                     unZipFile = Unzip.getUnzipPlyFileName(ConstantsUtil.FILE_PATH_APP_ROOT + key);
                    Log.e(LOGTAG,"unzip File :"+ unZipFile);
                }
                if(unZipFile != null) {
                    Log.e(LOGTAG, "combo :" + comboData.getCombo_ID() + " ***** Download successful  *****  :" + key);
                    comboData.countTobeDownloaded--;
                    updateDownloadStatus(comboData, downloadIndex, ConstantsUtil.EDownloadStatusType.eDownloaded.intStatus());
                    checkDownload(comboData);
                }
            }
        }
    }

    private void updateDownloadStatus(ComboData comboData, int downloadIndex,int downloadType) {
        ContentValues values = new ContentValues();
        switch (downloadIndex) {

            case INDEX_DOWNLOAD_LEG_OBJ: {
                BaseAccessoryItem legItem = comboData.getLegItem();
                legItem.setObjDStatus(downloadType);
                legItem.setObjAwsKey(legItem.getObjAwsKey().replace(".gz", ".ply"));
                 dataSource.create(legItem);
                comboData.setLegItem(legItem);

            }
            break;
            case INDEX_DOWNLOAD_LEG_TEX: {
                comboData.getLegItem().setTextureDStatus(downloadType);
            }
            break;
            case INDEX_DOWNLOAD_A1_OBJ: {
                comboData.setObjA1DStatus(downloadType);
                values.put(DatabaseHandler.A1_OBJ_DOWNLOAD_STATUS, downloadType);
                comboData.setmA1_Obj_Key_Name(comboData.getmA1_Obj_Key_Name().replace(".gz",".ply"));
                values.put(DatabaseHandler.A1_OBJ_KEY_NAME, comboData.getmA1_Obj_Key_Name());
            }
            break;
            case INDEX_DOWNLOAD_A1_TEX: {
                comboData.setTextureA1DStatus(downloadType);
                values.put(DatabaseHandler.A1_TEXTURE_DOWNLOAD_STATUS, downloadType);
            }
            break;
            case INDEX_DOWNLOAD_A6_OBJ: {
                comboData.setObjA6DStatus(downloadType);
                values.put(DatabaseHandler.A6_OBJ_DOWNLOAD_STATUS, downloadType);
                comboData.setmA6_Obj_Key_Name(comboData.getmA6_Obj_Key_Name().replace(".gz", ".ply"));
                values.put(DatabaseHandler.A6_OBJ_KEY_NAME, comboData.getmA6_Obj_Key_Name());
            }
            break;
            case INDEX_DOWNLOAD_A6_TEX: {
                comboData.setTextureA6DStatus(downloadType);
                values.put(DatabaseHandler.A6_TEXTURE_DOWNLOAD_STATUS, downloadType);
            }
            break;
            case INDEX_DOWNLOAD_A7_OBJ: {
                comboData.setObjA7DStatus(downloadType);
                values.put(DatabaseHandler.A7_OBJ_DOWNLOAD_STATUS, downloadType);
                comboData.setmA7_Obj_Key_Name(comboData.getmA7_Obj_Key_Name().replace(".gz", ".ply"));
                values.put(DatabaseHandler.A7_OBJ_KEY_NAME, comboData.getmA7_Obj_Key_Name());
            }
            break;
            case INDEX_DOWNLOAD_A7_TEX: {
                comboData.setTextureA7DStatus(downloadType);
                values.put(DatabaseHandler.A7_TEXTURE_DOWNLOAD_STATUS, downloadType);
            }
            break;

            case INDEX_DOWNLOAD_A9_OBJ: {
                comboData.setObjA9DStatus(downloadType);
                values.put(DatabaseHandler.A9_OBJ_DOWNLOAD_STATUS, downloadType);
                comboData.setmA9_Obj_Key_Name(comboData.getmA9_Obj_Key_Name().replace(".gz", ".ply"));
                values.put(DatabaseHandler.A9_OBJ_KEY_NAME, comboData.getmA9_Obj_Key_Name());
            }
            break;
            case INDEX_DOWNLOAD_A9_TEX: {
                comboData.setTextureA9DStatus(downloadType);
                values.put(DatabaseHandler.A9_TEXTURE_DOWNLOAD_STATUS, downloadType);
            }
            break;
            case INDEX_DOWNLOAD_A10_OBJ: {
                comboData.setObjA10DStatus(downloadType);
                values.put(DatabaseHandler.A10_OBJ_DOWNLOAD_STATUS, downloadType);
                comboData.setmA10_Obj_Key_Name(comboData.getmA10_Obj_Key_Name().replace(".gz",".ply"));
                values.put(DatabaseHandler.A10_OBJ_KEY_NAME, comboData.getmA10_Obj_Key_Name());
            }
            break;
            case INDEX_DOWNLOAD_A10_TEX: {
                comboData.setTextureA10DStatus(downloadType);
                values.put(DatabaseHandler.A10_TEXTURE_DOWNLOAD_STATUS, downloadType);
            }
            break;
            default:
                break;
        }
        dataSource.updateComboItemDownloadStatus(comboData.getCombo_ID(),values);
    }

    private void onDownloadFailed(final ComboData comboData){
//        if(onComboDownloadWeakListener != null) {
//            OnComboDownloadListener listener =  onComboDownloadWeakListener.get();
//            if(listener != null)
//                listener.onDownloadFailed(comboData.getCombo_ID());
//        }


        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(onComboDownloadListener != null)
                    onComboDownloadListener.onDownloadFailed(comboData.getCombo_ID());
            }
        });
    }

    private void checkDownload(final ComboData comboData) {
        if (comboData.countTobeDownloaded == 0) {//TODO
            Log.e(LOGTAG, " -- Downloaded ComboId :" + comboData.getCombo_ID());
            removeTransferListener();
            comboData.setIsDisplayReady(1);
            InkarneAppContext.getDataSource().create(comboData);
            //InkarneAppContext.getDataSource().updateComboDisplayReady(comboData.getCombo_ID());
            //ComboData comboDataFromDB = InkarneAppContext.getDataSource().getComboDataByComboID(comboData.getCombo_ID());
            //comboDataFromDB.indexTemp = comboData.indexTemp;

//            if(onComboDownloadWeakListener != null) {
//                OnComboDownloadListener listener =  onComboDownloadWeakListener.get();
//                if(listener != null)
//                    listener.onDownload(comboData);
//            }

//            if(onComboDownloadListener != null)
//                onComboDownloadListener.onDownload(comboData);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(onComboDownloadListener != null)
                        onComboDownloadListener.onDownload(comboData);
                }
            });
        }
        else {
            final int percentage = (totalCountTobeDownloaded - comboData.countTobeDownloaded)*100/(totalCountTobeDownloaded);
//            if(onComboDownloadWeakListener != null) {
//                OnComboDownloadListener listener =  onComboDownloadWeakListener.get();
//                if(listener != null)
//                    listener.onDownloadProgress(comboData.getCombo_ID(),percentage);
//            }
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(onComboDownloadListener != null)
                        onComboDownloadListener.onDownloadProgress(comboData.getCombo_ID(),percentage);
                }
            });

        }
    }
}
