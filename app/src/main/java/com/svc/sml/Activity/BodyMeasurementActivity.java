package com.svc.sml.Activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.svc.sml.Database.User;
import com.svc.sml.Fragments.BMFragment;
import com.svc.sml.Helper.AssetDownloader;
import com.svc.sml.InkarneAppContext;
import com.svc.sml.Model.BaseAccessoryItem;
import com.svc.sml.Model.FaceItem;
import com.svc.sml.R;
import com.svc.sml.Utility.ConstantsUtil;

import java.io.File;


public class BodyMeasurementActivity extends BaseActivity implements BMFragment.OnFragmentInteractionListener {
    public static final String LOGTAG = "BodyMeasurementActivity";
    private Button btnNext;
    private BMFragment bmFragment;
    private FaceItem faceItem;
    private boolean nextBtnClicked;

    private ProgressDialog progressDialog;
    private String picSource = "0";
    private String awsKeyPathPicUpload;
    private String feducialPath;
    private int countRetry;
    private boolean serverCallInProgress;
    private boolean isServiceFailed;
    private boolean isDownloadFailed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_body_measurement);
        btnNext = (Button) findViewById(R.id.btn_shared_next);
        //btnNext.setText("Let's fast forward to future fashion!");
        btnNext.setText("Next");
        verifyStoragePermissions(this);
        //deleteOldFolder();
        //btnNext.setTypeface(InkarneAppContext.getInkarneTypeFaceFutura());
        bmFragment = (BMFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentBM);
        faceItem = getDefaultFaceItem();
        getIntentVar();
        GATrackActivity(LOGTAG);
        Log.e(LOGTAG, "onCreate");
    }

    @Override
    protected void storagePermissionVerified() {
        super.storagePermissionVerified();
        deleteOldFolder();
    }

    private void deleteOldFolder() {
        SharedPreferences sh = getSharedPreferences("inkarne", MODE_PRIVATE);
        boolean isOldFileDeletedOnReinstall = sh.getBoolean("isOldFileDeletedOnReinstall", false);
        if (!isOldFileDeletedOnReinstall) {
            File inkarneDir = new File(ConstantsUtil.FILE_PATH_APP_ROOT + "inkarne");
            Log.e(LOGTAG, "deleteOldFolderOnInstall : " + inkarneDir.toString());
            ConstantsUtil.deleteDirectory(inkarneDir);
            sh.edit().putBoolean("isOldFileDeletedOnReinstall", true).commit();
        }

    }

    private FaceItem getDefaultFaceItem() {
        User user = User.getInstance();
        if (user != null) {
            faceItem = user.getDefaultFaceItem();
            if (faceItem == null) {
                faceItem = dataSource.getAvatar(user.getDefaultFaceId());
                user.setDefaultFaceItem(faceItem);
            }
            if (faceItem == null) {
                Log.e(LOGTAG, "faceItem null");
                FaceItem face = new FaceItem();
                face.setFaceId("1");
                faceItem = face;
                user.setDefaultFaceItem(faceItem);
                user.setDefaultFaceId(faceItem.getFaceId());
                dataSource.create(faceItem);
                //Toast.makeText(DataActivity.this, "Please restart the app ,some internal error has occurred", Toast.LENGTH_SHORT).show();
                //finish();
            }
        } else {
            Log.e(LOGTAG, "User null");
            //Toast.makeText(BodyMeasurementActivity.this, "Please restart the app ,some internal error has occurred", Toast.LENGTH_SHORT).show();
            finish();
        }
        return faceItem;
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        bmFragment.onWindowFocusChanged(hasFocus);
//        if(bmFragment instanceof BMFragment.IOnFocusListenable) {
//            ((BMFragment.IOnFocusListenable) bmFragment).onWindowFocusChanged(hasFocus);
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        nextBtnClicked = false;
        //getFaceItem();
    }

    private void getIntentVar() {
        awsKeyPathPicUpload = getIntent().getStringExtra(AdjustPicActivity.EXTRA_PARAM_PIC_AWS_KEY_PATH);
        picSource = getIntent().getStringExtra(FaceSelectionActivity.EXTRA_PARAM_PIC_SOURCE_TYPE);
        feducialPath = getIntent().getStringExtra(FiducialActivity2.EXTRA_PARAM_FEDUCIAL_PATH);
    }

    public void nextBtnClickHandler1(View v) {
        nextBtnClicked = true;
        if (faceItem == null || User.getInstance().getDefaultFaceItem() == null) {
            if (progressDialog == null)
                progressDialog = getProgressDialogTranslucent();
            progressDialog.setTitle(getString(R.string.message_loading_face_response));
            progressDialog.setMessage(getString(R.string.message_wait_face_download));
            if (!isFinishing())
                progressDialog.show();
            if (isServiceFailed) {
                getFaceItem();
            } else if (isDownloadFailed) {
                startFaceItemDownload();
            }
        } else {
            updateUserBM(bmFragment.bmModel);
            launchDataActivity();
        }
    }

    public void nextBtnClickHandler(View v) {
        nextBtnClicked = true;
        updateUserBM(bmFragment.bmModel);
        launchDataActivity();
    }

    public void launchDataActivity() {
        Log.e(LOGTAG, " ******** Launch DataActivity in BodyMeasurementActivity  *******");
        Intent intent = new Intent(BodyMeasurementActivity.this, DataActivity.class);
        startActivity(intent);
    }

    public void updateUserBM(BMFragment.BMModel bmModel) {
        User user = User.getInstance();
        user.setBust(bmModel.bustSize);
        user.setHip(bmModel.hipsSize);
        user.setWaist(bmModel.waistSize);
        user.setHeight(bmModel.heightBM);
        user.setWeight(bmModel.weightBM);
        dataSource.create(user);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    /*************/
    public void getFaceItem() {
//        if (faceItem == null)
//            requestFaceObj();
//        else {
//            startFaceItemDownload();
//        }
    }

    protected void showAlertError(String title, String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        finish();
                    }
                }).create();
        if (!isFinishing())
            builder.show();
    }

    private void startFaceItemDownload() {
        isDownloadFailed = false;
        BaseAccessoryItem face = new BaseAccessoryItem(faceItem.getFaceId(), faceItem.getFaceObjkey(), faceItem.getObjFaceDStatus(), faceItem.getFacePngkey(), faceItem.getTextureFaceDStatus());
        if (faceItem.getHairObjkey() != null && !faceItem.getHairObjkey().isEmpty() && faceItem.getHairPngKey() != null && !faceItem.getHairPngKey().isEmpty()) {
            face.dependentItem = new BaseAccessoryItem(faceItem.getHairstyleId(), faceItem.getHairObjkey(), faceItem.getObjHairDStatus(), faceItem.getHairPngKey(), faceItem.getTextureHairDStatus());
        }
        new AssetDownloader(this).downloadAsset(face, new OnAssetDownloadListener() {
            @Override
            public void onDownload(BaseAccessoryItem item) {
                faceItem.setObjFaceDStatus(ConstantsUtil.EDownloadStatusType.eDownloaded.intStatus());
                faceItem.setTextureFaceDStatus(ConstantsUtil.EDownloadStatusType.eDownloaded.intStatus());
                if (faceItem.getHairObjkey() != null && !faceItem.getHairObjkey().isEmpty() && faceItem.getHairPngKey() != null && !faceItem.getHairPngKey().isEmpty()) {
                    faceItem.setObjHairDStatus(ConstantsUtil.EDownloadStatusType.eDownloaded.intStatus());
                    faceItem.setTextureHairDStatus(ConstantsUtil.EDownloadStatusType.eDownloaded.intStatus());
                }
                updateDefaultFace();
                progressDialog.dismiss();
                if (nextBtnClicked) {
                    updateUserBM(bmFragment.bmModel);
                    launchDataActivity();
                }
            }

            @Override
            public void onDownloadFailed(String comboId) {
                isDownloadFailed = true;
            }

            @Override
            public void onDownloadProgress(String comboId, int percentage) {

            }
        });
    }

    private void updateDefaultFace() {
        User.getInstance().setDefaultFaceId(faceItem.getFaceId());
        User.getInstance().setDefaultFaceItem(faceItem);
        InkarneAppContext.getDataSource().create(User.getInstance());
        InkarneAppContext.getDataSource().create(faceItem);
    }
}
