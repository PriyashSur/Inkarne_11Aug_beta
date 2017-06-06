package com.svc.sml;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.Tracker;
import com.svc.sml.Activity.BaseActivity;
import com.svc.sml.Activity.OnAssetDownloadListener;
import com.svc.sml.Activity.RedoAvatarActivity;
import com.svc.sml.Activity.Video360Activity;
import com.svc.sml.Adapter.ZeroPaddingArrayAdapter;
import com.svc.sml.Database.ComboData;
import com.svc.sml.Database.ComboDataLooksItem;
import com.svc.sml.Database.ComboDataReconcile;
import com.svc.sml.Database.DatabaseHandler;
import com.svc.sml.Database.User;
import com.svc.sml.Fragments.CartFragment;
import com.svc.sml.Fragments.CollectionFragment;
import com.svc.sml.Fragments.LookLikeFragment;
import com.svc.sml.Fragments.ShopMixMatchFragment;
import com.svc.sml.Graphics.IRenderer;
import com.svc.sml.Graphics.PLYLoadMismatch;
import com.svc.sml.Graphics.Screenshot;
import com.svc.sml.Helper.AssetDownloader;
import com.svc.sml.Helper.ComboDownloader;
import com.svc.sml.Helper.DataManager;
import com.svc.sml.Helper.DownloadIntentService;
import com.svc.sml.Model.BaseAccessoryItem;
import com.svc.sml.Model.FaceItem;
import com.svc.sml.Utility.ConstantsFunctional;
import com.svc.sml.Utility.ConstantsUtil;
import com.svc.sml.View.LoadingView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class ShopActivity extends BaseActivity implements LookLikeFragment.OnFragmentInteractionListener,
        CartFragment.OnFragmentInteractionListener,
        View.OnClickListener,
        ShopMixMatchFragment.OnMixMatchFragmentInteractionListener, CollectionFragment.OnFragmentInteractionListener {

    //private static final String LOGTAG = ShopActivity.class.getName();
    private static final String LOGTAG = "MainActivity";
    private static final String SIDE_MIDDLE = "middle";
    private static final String SIDE_RIGHT = "right";
    private static final String SIDE_LEFT = "left";
    private static int WIDTH_GL = 1080;

    private static final int TAB_INDEX_COLLECTION = 4;
    private static final int TAB_INDEX_REDO_AVATAR = 2;
    private static final int TAB_INDEX_SHOP = 3;
    private static final int TAB_INDEX_LOOKALIKE = 1;
    private static final int TAB_INDEX_MIXMATCH = 5;
    private static final int TAB_INDEX_CART = 6;
    private static final int RESULT_CODE_UPDATE_AVATAR_ACTIVITY = 3;
    private static final int RESULT_CODE_VIDEO_360_ACTIVITY = 103;
    private boolean isScreenDisabled;

    private static final String OPTION0 = "LD";
    private static final String OPTION1 = "SD";
    private static final String OPTION2 = "HD";
    private static final String OPTION3 = "UHD";
    private static final String OPTION4 = "4K";
    private static final String OPTIONMAX = "MAX";
    private static final int RES4K = 4096;
    private static final int RESUHD = 2560;
    private static final int RESHD = 1920;
    private static final int RESSD = 1280;
    private static final int RESLD = 960;
    private static final String SIZE_TITLE = "SELECT A RESOLUTION :";

    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 29;
    private String StoragePath = ConstantsUtil.FILE_PATH_APP_ROOT;
    private FaceItem faceItem;

    private GLSurfaceView mGLView;
    private FrameLayout conSurfaceView;
    private glViewRenderer myRenderer;
    private Toolbar mToolbar;
    private LookLikeFragment lookLikeFragment;
    private CollectionFragment collectionFragment;
    private CartFragment cartFragment;
    private ShopMixMatchFragment mixMatchFrag;
    private LinearLayout conBtnLike;

    private LinearLayout conCartTopbar;
    private TextView tvCartTopbar;
    private ImageView btnLookaLike;
    private ImageView btnRedoAvatar;
    private ImageView btnShop;
    private ImageView btnLooks;
    private ImageView btnMixMatch;
    private ImageButton btnShopZoom;

    private ImageView ivLike;
    private TextView tvCountLike;
    private TextView tvStyleRating;
    //private ImageView ivBtnHomeDrawer;
    private LinearLayout ivBtnShare;
    private TextView tvTitleToolbar;
    private Spinner spinnerToolbar;
    private ComboData currentComboData;
    private ComboData prevComboData;
    private int lookAtIndex = 0;
    private boolean isMixMatchShowing = false;

    private int selectedTabIndex = TAB_INDEX_SHOP;
    private LoadingView pbGLView;
    private ProgressBar pbCircular;
    private ProgressDialog pbDialogComboLoad;
    public boolean isTakeScreenShot = false;
    public boolean isTakingScreenshot = false;
    private String shareType = "NA";
    private String shareMedium = "NA";
    private String glWindowStartTime;
    private PopupWindow popupWindowShare;
    private View popupView;

    //private RatingBarDialog ratingBarDialog;
    private VideoDialog alertVideoDialog;
    private int countDownloadMixmatch = 0;
    private int countDownloadAvatar = 0;
    private int countDownloadProgress = 0;
    private int countDownloadCombo = 0;
    private boolean isContainerChecked = false;

    private int shareWidth, shareHeight;
    private boolean isShare = true, isFirstLoadDone = false;

    private boolean videoCreationInProgress = false;
    private Screenshot videoScreen;


    private int comboIndex = 0;
    private ArrayList<ComboData> comboList = new ArrayList<>();

    private ImageButton btnForward;
    private ImageButton btnBackward;
    public ArrayList<ComboDataLooksItem> listOfComboList = new ArrayList<>();
    public ArrayList<ComboData> listComboHistory;
    public ArrayList<ComboData> listComboLikes;
    public int positionSelectedCat = 0;
    private Tracker mTracker;
    private FrameLayout conInstruction;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.d(LOGTAG, "Shop screen dispatch touch");
//        if (SystemClock.elapsedRealtime() - firstClickTime < 3000) {
//            //return false;//TODO
//        }
//        if(isScreenDisabled)
//            return false;

        return super.dispatchTouchEvent(ev);
    }

    private void showInstruction() {

        if (InkarneAppContext.getSPValue(ConstantsUtil.SETTING_KEY_COUNT_INSTRUCTION_SHOP_ACTIVITY, 0) > 2) {
            return;
        }
        //final FrameLayout conInstruction = (FrameLayout) findViewById(R.id.con_inst);
        FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final ImageView iv1 = getImageView();
        iv1.setImageResource(R.drawable.inst_shop_lookalike);
        conInstruction.addView(iv1);
        final ImageView iv2 = getImageView();
        iv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conInstruction.removeView(iv1);
                iv2.setImageResource(R.drawable.inst_shop_redoavatar);
                conInstruction.addView(iv2);
            }
        });

        iv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                conInstruction.removeView(iv2);
                conInstruction.setVisibility(View.GONE);
                int count = InkarneAppContext.getSPValue(ConstantsUtil.SETTING_KEY_COUNT_INSTRUCTION_SHOP_ACTIVITY, 0);
                InkarneAppContext.saveSPValue(ConstantsUtil.SETTING_KEY_COUNT_INSTRUCTION_SHOP_ACTIVITY, count+1);
            }
        });
//        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
//                .findViewById(android.R.id.content)).getChildAt(0);
//        viewGroup.addView(l);
    }

    private ImageView getImageView() {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ImageView iv1 = new ImageView(ShopActivity.this);
        iv1.setClickable(true);
        iv1.setLayoutParams(p);
        iv1.setScaleType(ImageView.ScaleType.FIT_CENTER);
        return iv1;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop);
        videoScreen = new Screenshot();
        pbDialogComboLoad = getProgressDialogTransparent();
        pbCircular = (ProgressBar) findViewById(R.id.pb_circular);
        pbCircular.setVisibility(View.INVISIBLE);
        btnLookaLike = (ImageView) findViewById(R.id.toolbar_ibtn_lookalike);
        btnLookaLike.setOnClickListener(this);
        btnRedoAvatar = (ImageView) findViewById(R.id.toolbar_ibtn_redoAvatar);
        btnRedoAvatar.setOnClickListener(this);
        btnShop = (ImageView) findViewById(R.id.toolbar_ibtn_shop);
        btnShop.setOnClickListener(this);
        btnLooks = (ImageView) findViewById(R.id.toolbar_ibtn_looks);
        btnLooks.setOnClickListener(this);
        btnMixMatch = (ImageView) findViewById(R.id.toolbar_ibtn_mixmatch);
        btnMixMatch.setOnClickListener(this);
        pbGLView = (LoadingView) findViewById(R.id.loading_view);
        btnShopZoom = (ImageButton) findViewById(R.id.btn_shop_zoom);

        btnForward = (ImageButton) findViewById(R.id.ib_avatar_forward);
        btnForward.setOnClickListener(this);
        btnForward.setVisibility(View.INVISIBLE);
        btnBackward = (ImageButton) findViewById(R.id.ib_avatar_backword);
        btnBackward.setOnClickListener(this);
        btnBackward.setVisibility(View.INVISIBLE);
        conInstruction = (FrameLayout) findViewById(R.id.con_inst);
        //btnShop.setSelected(true);
        setupToolbar();
        faceItem = User.getInstance().getDefaultFaceItem();
        //mGLView = (GLSurfaceView) findViewById(R.id.surfaceviewclass);
        conSurfaceView = (FrameLayout) findViewById(R.id.con_surfaceview);
        mGLView = new GLSurfaceView(ShopActivity.this);
        mGLView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        conSurfaceView.addView(mGLView);
        myRenderer = new glViewRenderer(this, mGLView, ConstantsUtil.GL_INDEX_TOTAL, User.getInstance().getmGender());
        setGLViewSize();
        //mGLView.setZOrderOnTop(true);
        mGLView.setEGLContextClientVersion(3);
        mGLView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        mGLView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
        mGLView.setRenderer(myRenderer);
        mGLView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        initView();
        GATrackActivity(LOGTAG);
        Log.e(LOGTAG, "onCreate");
    }


    public ProgressDialog getProgressDialogTransparent() {
        //ProgressDialog p = new ProgressDialog(this, R.style.AppCompatAlertTranslucentDialogStyle);
        ProgressDialog p = new ProgressDialog(this, R.style.AppCompatAlertTransparentDialogStyle);
        p.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        p.setTitle("Loading...");
        p.setCanceledOnTouchOutside(false);
        p.setCancelable(false);
        return p;
    }

    public void showingComboLoading() {
        showDownloadProgressBarWithoutIncrement();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (pbDialogComboLoad != null && !isFinishing())
                    pbDialogComboLoad.show();
                if (pbGLView != null) {
                    pbGLView.setLoadingText(getString(R.string.message_rendering_looks));
                    pbGLView.setVisibility(View.VISIBLE);
                }
                //btnBackward.setVisibility(View.INVISIBLE);
                //btnForward.set
            }
        });

//           new Thread(new Runnable() {
//               @Override
//               public void run() {
//                   runOnUiThread(new Runnable() {
//                       @Override
//                       public void run() {
//                           if(pbDialogComboLoad != null)
//                               pbDialogComboLoad.show();
//                       }
//                   });
//               }
//           });
    }

    public void dismissComboLoading() {
        if (pbCircular != null)
            pbCircular.setVisibility(View.INVISIBLE);
        if (pbDialogComboLoad != null)
            pbDialogComboLoad.dismiss();
        if (pbGLView != null)
            pbGLView.setVisibility(View.INVISIBLE);
//           new Thread(new Runnable() {
//               @Override
//               public void run() {
//                   runOnUiThread(new Runnable() {
//                       @Override
//                       public void run() {
//                           if(pbDialogComboLoad != null)
//                               pbDialogComboLoad.show();
//                       }
//                   });
//               }
//           });
    }

    private void setGLViewSize() {
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int width = display.getWidth();  // deprecated
        int height = display.getHeight();  // deprecated
        int heightMargin = (int) getResources().getDimension(R.dimen.height_footer);
        heightMargin += (int) getResources().getDimension(R.dimen.height_header);
        //int hMarginInPx = ConstantsUtil.getPxFromDp(heightMargin);
        int heightGL = height * WIDTH_GL / width - heightMargin;
        Log.e(LOGTAG, "w :" + WIDTH_GL + "  h:" + heightGL);
        if (width > (int) (WIDTH_GL * 1.2))
            mGLView.getHolder().setFixedSize(WIDTH_GL, heightGL);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.e(LOGTAG, "new intent");
        GATrackActivity(LOGTAG);
        //getIntent() should always return the most recent
        setIntent(intent);
        initView();
        videoCreationInProgress = false;
        boolean isBodyORFaceChanged = getIntent().getBooleanExtra("isBodyORFaceChanged", false);
        if (isBodyORFaceChanged) {
            myRenderer.loadAvatar();
            if (currentComboData != null)
                myRenderer.changeObjects(currentComboData);
        }
        btnShop.setSelected(true);
        updateShopBtnImage();
    }

    private void initView() {
        //setupToolbar(currentComboData);
        glWindowStartTime = getDate();
        faceItem = User.getInstance().getDefaultFaceItem();
        ComboData comboData = (ComboData) getIntent().getSerializableExtra("comboData");
        if (comboData != null) {
            currentComboData = comboData;
            Log.e(LOGTAG, "Loading passed comboId :" + comboData.getCombo_ID());
        } else if (prevComboData != null && currentComboData == null) {
            currentComboData = prevComboData;
        }

        List<ComboData> comboDataList = null;
        if (currentComboData == null) {
            Log.e(LOGTAG, "initView : currentComboData = null ");
            comboDataList = dataSource.getComboDataLaunchSeen(1);
            if (comboDataList.size() == 0) {
                Toast.makeText(ShopActivity.this, "No Looks found. " + ConstantsUtil.MESSAGE_TOAST_NETWORK_RESPONSE_FAILED, Toast.LENGTH_LONG).show();
            } else {
                currentComboData = comboDataList.get(0);
                loadCombo(currentComboData);
            }
        }
        new LoadLooksListTask().execute();
    }

    @Override
    public void onPause() {
        super.onPause();
        //LocalBroadcastManager.getInstance(ShopActivity.this).unregisterReceiver(mMessageReceiver);
        if (popupWindowShare != null)
            popupWindowShare.dismiss();
    }

    @Override
    protected void onStop() {
        updateTime(currentComboData);
        super.onStop();
        if (popupWindowShare != null)
            popupWindowShare.dismiss();
        dismissComboLoading();
    }

    @Override
    public void onResume() {
        faceItem = User.getInstance().getDefaultFaceItem();
        //LocalBroadcastManager.getInstance(ShopActivity.this).registerReceiver(mMessageReceiver, new IntentFilter(DownloadIntentService.INTENT_NAME_VIDEO_DOWNLOAD_SERVICE_LAUNCH));
        super.onResume();
        //glWindowStartTime = getDate();
        Log.w(LOGTAG, "onresume");
        mGLView.onResume();
        myRenderer.onResume();
        myRenderer.resetCount();
        hideForceDownloadProgressBar();

        mGLView.requestRender();
        //updateHistoryAndLikes();
        if (((InkarneAppContext) this.getApplication()).wasInBackground && !isContainerChecked) {
            Log.w(LOGTAG, " resume Come from background");
            if (mTracker == null)
                createTracker();
            if (currentComboData == null) {
                Log.w(LOGTAG, "resume comboData was null");
                currentComboData = InkarneAppContext.combData;
            }
            if (listOfComboList == null || listOfComboList.size() == 0) {
                initData();
            }
//            if(myRenderer.isChangeObjFinish) {
//                dismissFaceLoading();
//            }else {
//                showFaceLoading();
//            }
            myRenderer.checkObjContainer();
            //Do specific came-here-from-background code
        }

        if (myRenderer.isChangeObjFinish) {
            dismissComboLoading();
        } else {
            showingComboLoading();
        }

        isContainerChecked = false;
        tvCartTopbar.setText(String.valueOf(InkarneAppContext.getCartNumber()));
        videoCreationInProgress = false;
        if (pbGLView.getLoadingText().equals(getString(R.string.message_loading_text_video_share_creation))) {
            pbGLView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        new LoadLooksListTask().execute();
        videoCreationInProgress = false;
        glWindowStartTime = getDate();
        faceItem = User.getInstance().getDefaultFaceItem();
        myRenderer.resetCount();
        hideForceDownloadProgressBar();
        Log.w(LOGTAG, "restart Come from background");
        if (currentComboData == null) {
            Log.w(LOGTAG, "restart comboData was null");
            currentComboData = InkarneAppContext.combData;
        }
        isContainerChecked = true;
        myRenderer.checkObjContainer();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGLView.requestRender();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("Home", "Permission Granted");
                    pbGLView.setVisibility(View.VISIBLE);
                    isTakeScreenShot = true;
                } else {
                    Log.d("Home", "Permission Failed");
                    Toast.makeText(this, "You must allow permission to share Images.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (selectedTabIndex == TAB_INDEX_SHOP || selectedTabIndex == TAB_INDEX_MIXMATCH) {
                myRenderer.buttonPressed(0);
                mGLView.requestRender();

            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (selectedTabIndex == TAB_INDEX_SHOP || selectedTabIndex == TAB_INDEX_MIXMATCH) {
            if (!isScreenDisabled) {
                myRenderer.onTouchEvent(e);
                mGLView.requestRender();
            }
        }
        return super.onTouchEvent(e);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed  here it is 2
        if (requestCode == RESULT_CODE_UPDATE_AVATAR_ACTIVITY) {
            hideAllFrag(TAB_INDEX_SHOP);
        }

        if (requestCode == RESULT_CODE_VIDEO_360_ACTIVITY) {
            if (selectedTabIndex != TAB_INDEX_SHOP)
                hideAllFrag(TAB_INDEX_SHOP);
            String comboId = data.getStringExtra(Video360Activity.INTENT_KEY_COMBO_ID);
            String videoKey = data.getStringExtra(Video360Activity.INTENT_KEY_VIDEO_PATH);
            Uri videoUri = data.getParcelableExtra(Video360Activity.INTENT_KEY_VIDEO_URI);
            if (alertVideoDialog == null)
                alertVideoDialog = new VideoDialog(videoKey, comboId, videoUri);
            alertVideoDialog.videoKey = videoKey;
            alertVideoDialog.comboId = comboId;
            alertVideoDialog.showVideoDialog();
        }
    }

    protected void loadCombo(final ComboData combodata) {
        Log.e(LOGTAG, "loadCombo new : " + combodata.getCombo_ID());
        showingComboLoading();

        new ComboDownloader(ShopActivity.this, combodata, new ComboDownloader.OnComboDownloadListener() {
            @Override
            public void onDownload(ComboData comboData) {
                Log.e(LOGTAG, "loadCombo  onDownload : " + comboData.getCombo_ID());
                if (currentComboData != null)
                    prevComboData = currentComboData;
                currentComboData = comboData;
                //hideAllFrag(TAB_INDEX_SHOP);
                myRenderer.changeObjects(currentComboData);
            }

            @Override
            public void onDownloadFailed(String comboId) {
                Toast.makeText(ShopActivity.this, "Looks download failed.", Toast.LENGTH_SHORT).show();
                dismissComboLoading();
            }

            @Override
            public void onDownloadProgress(String comboId, int percentage) {

            }

            @Override
            public void onComboInfoFailed(String comboId, int error_code) {
                if (error_code == DataManager.CODE_DATA_MANAGER_NETWORK_ERROR) {
                    Toast.makeText(ShopActivity.this, getString(R.string.message_network_failure), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ShopActivity.this, "Looks info not available", Toast.LENGTH_SHORT).show();
                }
                dismissComboLoading();
            }

            @Override
            public void onComboInfoResponse(String comboId) {

            }
        });
    }

    private String getDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strDate = sdf.format(new Date());
        return strDate;
    }

    private void updateToolbar(final ComboData comboData) {
        Log.d(LOGTAG, "UpdateToolbar 0 :");
        if (comboData == null || comboData.getVogue_Flag() == null) {
            Log.e(LOGTAG, "UpdateToolbar 1 :" + comboData.getCombo_ID());
            return;
        }
        tvCartTopbar.setText(String.valueOf(InkarneAppContext.getCartNumber()));

        Log.d(LOGTAG, "UpdateToolbar 2 :" + comboData.getCombo_ID());
        tvCountLike.setText(" " + comboData.getLikes_Count() + " ");
        String style = String.valueOf(comboData.getStyle_Rating());
        String sub = style.substring(style.length() - 1).trim();
        if (sub != null && sub.equals("0")) {
            style = style.substring(0, style.length() - 2);
        }
        tvStyleRating.setText(style + "/5");
        String title = comboData.getLooksCategoryTitle();
        if (title == null || title.length() == 0) {
            if (comboData.getVogue_Flag().equals("True")) {
                title = "Trending";
            } else {
                title = comboData.getCombo_Style_Category();
            }
        }
        String titleCap = title.substring(0, 1).toUpperCase() + title.substring(1).toLowerCase();
        tvTitleToolbar.setText(titleCap);
        int index = ConstantsUtil.arrayListLooksLabelName.indexOf(title);
        Log.w(LOGTAG, "title: " + title + "   spinner selected: " + index);

//        if (listOfComboList != null && listOfComboList.size() > index && index >= 0) {
//            ComboDataLooksItem cl = listOfComboList.get(index);
//            comboList = cl.getComboList();
//            if (comboList != null && comboList.size() != 0) {
//                spinnerToolbar.setSelection(index);
//            }
//        }

        if (comboData.isLiked() > 0) {
            ivLike.setImageResource(R.drawable.liked);
        } else {
            ivLike.setImageResource(R.drawable.like);
        }
        updatedComboDataSeen(comboData);
        updateLikeCounts(comboData);
        dataSource.updateComboTimeStamp(comboData);
    }

    private void setupToolbar() {
        spinnerToolbar = (Spinner) findViewById(R.id.s_shop_toolbar);
        ZeroPaddingArrayAdapter<String> aspin = new ZeroPaddingArrayAdapter<String>(ShopActivity.this, R.layout.shop_spinner_looktype_dropdown_item, ConstantsUtil.arrayListLooksLabelName.toArray(new String[ConstantsUtil.arrayListLooksLabelName.size()])); //the adapter for the Spinner
        //aspin.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        aspin.setDropDownViewResource(R.layout.shop_spinner_looktype_dropdown_item);
        spinnerToolbar.getBackground().setColorFilter(ContextCompat.getColor(ShopActivity.this, R.color.tcolor_green), PorterDuff.Mode.SRC_ATOP);
        //spinnerToolbar.getBackground().setColorFilter(ContextCompat.getColor(ShopActivity.this, R.color.transparent), PorterDuff.Mode.SRC_ATOP);
        //spinnerToolbar.setBackgroundColor(ContextCompat.getColor(ShopActivity.this,R.color.transparent));
        spinnerToolbar.setAdapter(aspin);
        SpinnerInteractionListener listener = new SpinnerInteractionListener();
        spinnerToolbar.setOnTouchListener(listener);
        spinnerToolbar.setOnItemSelectedListener(listener);

//        spinnerToolbar.post(new Runnable() {
//            @Override
//            public void run() {
//                spinnerToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//                    @Override
//                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                        // Only called when the user changes the selection
//                        Log.w(LOGTAG, "onItemSelected position: " + position);
//                        ComboDataLooksItem cl = listOfComboList.get(position);
//                        comboList = cl.getComboList();
//                        if (comboList != null && comboList.size() > 0) {
//                            comboIndex = 0;
//                            currentComboData = comboList.get(comboIndex);
//                            loadCombo(currentComboData);
//                        } else {
//
//                        }
//                    }
//
//                    @Override
//                    public void onNothingSelected(AdapterView<?> parent) {
//                    }
//                });
//            }
//        });


//        spinnerToolbar.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                Log.w(LOGTAG,"onItemSelected position: "+ position);
//                ComboDataLooksItem cl = listOfComboList.get(position);
//                comboList = cl.getComboList();
//                if(comboList != null && comboList.size()>0) {
//                    comboIndex =0;
//                    currentComboData = comboList.get(comboIndex);
//                    loadCombo(currentComboData);
//                }else{
//
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        tvCartTopbar = (TextView) findViewById(R.id.tv_topbar_cart);
        conCartTopbar = (LinearLayout) findViewById(R.id.con_cart);

//        ivBtnHomeDrawer = (ImageView) findViewById(R.id.ivBtn_home_drawer);
        conCartTopbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(LOGTAG, "cart clicked 1");
                if (myRenderer.isZoomCompleted() && isFirstLoadDone && !myRenderer.isVideo) {
                    Log.e(LOGTAG, "cart clicked  2");
                    onHomeDrawerCartClick(v);
                }
            }
        });
        ivBtnShare = (LinearLayout) findViewById(R.id.ivBtnShare);
        ivBtnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (myRenderer.isZoomCompleted() && isFirstLoadDone && !myRenderer.isVideo) {//&& (selectedTabIndex== TAB_INDEX_SHOP || selectedTabIndex == TAB_INDEX_MIXMATCH)

                    if (selectedTabIndex == TAB_INDEX_LOOKALIKE) {
                        hideAllFrag(TAB_INDEX_SHOP);

                    }
                    trackEvent("Share", currentComboData.getCombo_ID(), "");
                    showPopupWindow();
//                isTakeScreenShot = true;
//                pbGLView.setVisibility(View.VISIBLE);
//                pbGLView.setLoadingText("Capturing render image ...");
                    mGLView.requestRender();
                }
            }
        });

        tvTitleToolbar = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        tvTitleToolbar.setTypeface(InkarneAppContext.getInkarneTypeFaceHeaderJennaSue());
        tvStyleRating = (TextView) findViewById(R.id.sku_style_rating);
        tvCountLike = (TextView) findViewById(R.id.tv_likes_count);
        ivLike = (ImageView) findViewById(R.id.iv_like);
        conBtnLike = (LinearLayout) findViewById(R.id.ll_like_container);
        conBtnLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO
                /*
                if(currentComboData.getIsLiked() !=0) {
                    dataSource.deleteComboDetailLikeById(currentComboData.getCombo_ID());
                    //dataSource.updateComboLikeAndIsLiked(currentComboData.getCombo_ID(),currentComboData);
                }
                else{
                    saveLikesAccessory(currentComboData);
                }
                */
                trackEvent("Like", currentComboData.getCombo_ID(), "");
                if (myRenderer.isZoomCompleted() && isFirstLoadDone && !myRenderer.isVideo) {
                    addToLikes(currentComboData);
                    updateIsLikeToServer(currentComboData);
                }
            }
        });
    }

    public class SpinnerInteractionListener implements AdapterView.OnItemSelectedListener, View.OnTouchListener {

        boolean userSelect = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            userSelect = true;
            return false;
        }

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Log.w(LOGTAG, "onItemSelected position 1: " + position);
            if (userSelect) {
                // Your selection handling code here
                userSelect = false;
                Log.w(LOGTAG, "onItemSelected position 2: " + position);

                ComboDataLooksItem cl = listOfComboList.get(position);
                ArrayList<ComboData> comboListTemp = cl.getComboList();
                trackEvent("Spinner", cl.getLooksLabelName(), "");
                if (comboListTemp != null && comboListTemp.size() > 0) {
                    if (selectedTabIndex == TAB_INDEX_LOOKALIKE) {
                        hideAllFrag(TAB_INDEX_SHOP);

                    }
                    comboIndex = 0;
                    comboList = comboListTemp;
                    positionSelectedCat = position;
                    currentComboData = comboList.get(comboIndex);
                    loadCombo(currentComboData);
                } else {
                    Toast.makeText(getApplicationContext(), "There are no looks in " + ConstantsUtil.arrayListLooksLabelName.get(position), Toast.LENGTH_SHORT).show();
                    spinnerToolbar.setSelection(positionSelectedCat);
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }

    }

    private void updatedComboDataSeen(ComboData comboData) {
        comboData.setViewCount(comboData.getViewCount() + 1);
        // Log.d(LOGTAG, "updatedComboDataSeen " + new Date());
        //comboData.setComboUpdatedDateFormated(new Date());
        dataSource.updateComboSeenDate(comboData);
    }

    private void setLikeImage(int liked) {
        if (liked == 0) {//TODO
            ivLike.setImageResource(R.drawable.like);
        } else {
            ivLike.setImageResource(R.drawable.liked);
        }
    }

    private void updateLikeCounts(final ComboData combodata) {
        final String uri = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_UPDATE_LIKE_COUNT + combodata.getCombo_ID();
        DataManager.getInstance().updateMethodToServer(uri, ConstantsUtil.EUpdateType.eUpdateTypeLikeCount.toString(), new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {
                int likeCount = (int) obj;
                combodata.setLikes_Count(likeCount);
                tvCountLike.setText(String.valueOf(likeCount));
                dataSource.updateComboLikeCount(combodata.getCombo_ID(), likeCount);
            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });
    }

    private void updateLikeView(final ComboData combodata) {
        int likeCount = combodata.getLikes_Count();
        if (combodata.getIsLiked() == 0) {//TODO
            combodata.setIsLiked(1);
            likeCount++;
        } else {
            combodata.setIsLiked(0);
            likeCount--;
            if (likeCount < 0)
                likeCount = 0;
        }
        combodata.setLikes_Count(likeCount);
        tvCountLike.setText(String.valueOf(likeCount));
        dataSource.updateComboLikeAndIsLiked(combodata.getCombo_ID(), likeCount, combodata.isLiked());
    }

    private void updateIsLikeToServer(final ComboData combodata) {
        //http://inkarneweb-prod.elasticbeanstalk.com/Service1.svc/UpdateLikes/4/FC01/0
        int isLiked = combodata.isLiked() == 0 ? 1 : 0;
        setLikeImage(isLiked);
        final String uri = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_UPDATE_LIKE + User.getInstance().getmUserId() + "/" + combodata.getCombo_ID() + "/" + isLiked;
        DataManager.getInstance().updateMethodToServer(uri, ConstantsUtil.EUpdateType.eUpdateTypeLike.toString(), new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {
//                int likeCount = combodata.getLikes_Count();
//                if (combodata.getIsLiked() == 0) {//TODO
//                    combodata.setIsLiked(1);
//                    likeCount++;
//                } else {
//                    combodata.setIsLiked(0);
//                    likeCount--;
//                    if (likeCount < 0)
//                        likeCount = 0;
//                }
//                combodata.setLikes_Count(likeCount);
//                tvCountLike.setText(String.valueOf(likeCount));
//                dataSource.updateComboLikeAndIsLiked(combodata.getCombo_ID(), likeCount,combodata.isLiked());
            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });

        updateLikeView(combodata);
    }

    private void updateTime(ComboData comboData) {
        String url = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_UPDATE_ENGAGEMENT_TIME + User.getInstance().getmUserId() + "/" + comboData.getCombo_ID() + "/"
                + "?Start_Timestamp=" + Uri.encode(glWindowStartTime)
                + "&End_Timestamp=" + Uri.encode(getDate());

        DataManager.getInstance().updateMethodToServer(url, ConstantsUtil.EUpdateType.eUpdateTypeEngagement.toString(), new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {

            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });
    }


    public void onHomeDrawerClick1(View v) {
        Log.d(LOGTAG, "Drawer responding to home drawer click...");
        //hideAllFrag(TAB_INDEX_CART);
        if (lookLikeFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            lookLikeFragment = LookLikeFragment.newInstance(currentComboData);
            //ft.addToBackStack(null);
            ft.replace(R.id.dLayout, lookLikeFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else {
            if (lookLikeFragment.isDetached()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

                ft.attach(lookLikeFragment);
                ft.show(lookLikeFragment);
                lookLikeFragment.setComboData(currentComboData);
//                    fm.beginTransaction()
//                            .setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out)
//                            .show(lookLikeFragment)
//                            .commit();
                try {
                    ft.commit();
                } catch (IllegalStateException ignore) {

                }
            } else if (lookLikeFragment.isHidden()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.show(lookLikeFragment);
                lookLikeFragment.setComboData(currentComboData);
                try {
                    ft.commit();
                } catch (IllegalStateException ignore) {

                }
            } else {
                hideLookaLikeFragWithDetach();
            }
        }
    }

    private void hideLookaLikeFragWithDetach1() {
        // if (lookLikeFragment != null && (!lookLikeFragment.isDetached() || !lookLikeFragment.isHidden())) {
        if (lookLikeFragment != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(lookLikeFragment);
            ft.detach(lookLikeFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
    }

    public void onHomeDrawerCartClick(View v) {
        hideAllFrag(TAB_INDEX_CART);
        //btnLookaLike.setSelected(true);
        if (cartFragment == null) {
            cartFragment = CartFragment.newInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            //ft.addToBackStack(null);
            ft.replace(R.id.ll_shop_base, cartFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }

        } else if (cartFragment.isDetached()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.attach(cartFragment);
            //ft.addToBackStack();
            ft.show(cartFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else if (cartFragment.isHidden()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.show(cartFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
        //else{
//            hideCartFrag();
//        }
    }

//    private void showCartFragment() {
//        btnLookaLike.setSelected(true);
//        if (cartFragment == null) {
//            cartFragment = CartFragment.newInstance();
//            //ft.addToBackStack(null);
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.replace(R.id.ll_shop_base, cartFragment);
//            ft.commit();
//
//        } else if (cartFragment.isDetached()) {
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.attach(cartFragment);
//            ft.show(cartFragment);
//            ft.commit();
//        } else if (cartFragment.isHidden()) {
//            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//            ft.show(cartFragment);
//            ft.commit();
//        }else{
//            hideCartFrag();
//        }
//    }

    private void hideCartFrag() {
        //btnLookaLike.setSelected(false);
        btnShop.setSelected(true);
        updateCartNumber();
        //if (cartFragment != null && (!cartFragment.isDetached() || !cartFragment.isHidden())) {
        if (cartFragment != null && !cartFragment.isDetached()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(cartFragment);
            ft.detach(cartFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
    }

    private void showLookalikeFragment() {
        Log.d(LOGTAG, "Drawer responding to home drawer click...");
        hideAllFrag(TAB_INDEX_LOOKALIKE);
        btnLookaLike.setSelected(true);
        if (lookLikeFragment == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            lookLikeFragment = LookLikeFragment.newInstance(currentComboData);
            //ft.addToBackStack(null);
            ft.replace(R.id.dLayout, lookLikeFragment);
            //ft.replace(R.id.ll_shop_base, lookLikeFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else {
            if (lookLikeFragment.isDetached()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.attach(lookLikeFragment);
                ft.show(lookLikeFragment);
                lookLikeFragment.setComboData(currentComboData);
                try {
                    ft.commit();
                } catch (IllegalStateException ignore) {

                }
            } else if (lookLikeFragment.isHidden()) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.show(lookLikeFragment);
                lookLikeFragment.setComboData(currentComboData);
                try {
                    ft.commit();
                } catch (IllegalStateException ignore) {

                }
            } else {
                //hideLookaLikeFragWithDetach();
            }
        }
    }

    private void hideLookaLikeFragWithDetach() {
        btnLookaLike.setSelected(false);
        updateCartNumber();
        if (lookLikeFragment != null && !lookLikeFragment.isDetached()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(lookLikeFragment);
            ft.detach(lookLikeFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
    }

    private void showMixMatchFragment() {
        hideAllFrag(TAB_INDEX_MIXMATCH);
        btnMixMatch.setSelected(true);
        btnForward.setVisibility(View.INVISIBLE);
        if (mixMatchFrag == null) {
            mixMatchFrag = ShopMixMatchFragment.newInstance(faceItem);
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.con_mixmatch_fragment, mixMatchFrag);
            ft.addToBackStack(null);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else if (mixMatchFrag.isDetached()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.attach(mixMatchFrag);
            ft.show(mixMatchFrag);
            mixMatchFrag.setFaceItem(faceItem);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else if (mixMatchFrag.isHidden()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.show(mixMatchFrag);
            mixMatchFrag.setFaceItem(faceItem);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else {
            hideMixMatchFragWithDetach();
        }
    }

    private void hideMixMatchFragWithDetach() {
        btnMixMatch.setSelected(false);
        updateChangeButton();
        if (mixMatchFrag != null && !mixMatchFrag.isDetached()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.detach(mixMatchFrag);
            ft.hide(mixMatchFrag);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
    }

    private void setButtonsClick(boolean b) {
        Log.w(LOGTAG, "setButtonsClick " + b);
        btnShop.setEnabled(b);
        btnRedoAvatar.setEnabled(b);
        btnLookaLike.setEnabled(b);
        btnMixMatch.setEnabled(b);
        btnLooks.setEnabled(b);
        spinnerToolbar.setEnabled(b);
        isScreenDisabled = !b;
        btnBackward.setEnabled(b);
        btnForward.setEnabled(b);

    }

    private void showCollectionFrag() {
        hideAllFrag(TAB_INDEX_COLLECTION);
        btnLooks.setSelected(true);
        updateCollectionList();
        if (collectionFragment == null) {
            collectionFragment = CollectionFragment.newInstance();

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.ll_shop_base, collectionFragment);
            //ft.addToBackStack(null);
            ft.show(collectionFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else if (collectionFragment.isDetached()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.attach(collectionFragment);
            ft.show(collectionFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        } else if (collectionFragment.isHidden()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.show(collectionFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
    }

    private void hideCollectionFragWithDetach() {
        btnLooks.setSelected(false);
        updateHistoryAndLikes();
        if (collectionFragment != null && !collectionFragment.isDetached()) {
            //if (collectionFragment != null && !collectionFragment.isHidden()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.hide(collectionFragment);
            ft.detach(collectionFragment);
            try {
                ft.commit();
            } catch (IllegalStateException ignore) {

            }
        }
    }


    private boolean shouldClick() {
        //if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
//        if (SystemClock.elapsedRealtime() - mLastClickTime < 100) {
//                return false;
//            }
//        mLastClickTime = SystemClock.elapsedRealtime();
        return true;
    }

    private void updateLookAtIndex(int index, int angle) {
        if (index != lookAtIndex) {
            lookAtIndex = index;
            updateShopBtnImage();
            myRenderer.viewLookAt(lookAtIndex, angle);
        }
    }

    private void updateShopBtnImage() {
        //if(index== TAB_INDEX_SHOP || index == TAB_INDEX_MIXMATCH )
        switch (lookAtIndex) {
            case IRenderer.LOOKAT_INDEX_BODY: {
                if (btnShop.isSelected()) {
                    btnShop.setImageResource(R.drawable.btn_shop_body_selected);
                } else {
                    btnShop.setImageResource(R.drawable.btn_shop_body);
                }
            }
            break;
            case IRenderer.LOOKAT_INDEX_SHOULDER: {
                if (btnShop.isSelected()) {
                    btnShop.setImageResource(R.drawable.btn_shop_halfbody_selected);
                } else {
                    btnShop.setImageResource(R.drawable.btn_shop_halfbody);
                }
            }
            break;
            case IRenderer.LOOKAT_INDEX_FACE: {
                if (btnShop.isSelected()) {
                    btnShop.setImageResource(R.drawable.btn_shop_face_selected);
                } else {
                    btnShop.setImageResource(R.drawable.btn_shop_face);
                }
            }
            break;
            default:
                break;
        }
        // mGLView.requestRender();
    }

    public void updateCartNumber() {
        Log.e(LOGTAG, "cart number 2 :" + InkarneAppContext.getCartNumber());
        tvCartTopbar.setText(String.valueOf(InkarneAppContext.getCartNumber()));
    }

    private int hideAllFrag(int index) {
        btnRedoAvatar.setSelected(false);
        switch (index) {
            case TAB_INDEX_COLLECTION: {
                //myRenderer.setAcceptTouch(false);
                //myRenderer.stopZoom();
                hideMixMatchFragWithDetach();
                hideLookaLikeFragWithDetach();
                hideCartFrag();
                btnShop.setSelected(false);
                updateShopBtnImage();
            }
            break;
            case TAB_INDEX_REDO_AVATAR: {
                // myRenderer.setAcceptTouch(false);
                //myRenderer.stopZoom();
                hideCollectionFragWithDetach();
                hideLookaLikeFragWithDetach();
                hideMixMatchFragWithDetach();
                hideCartFrag();
            }
            break;
            case TAB_INDEX_SHOP: {
                hideCollectionFragWithDetach();
                hideLookaLikeFragWithDetach();
                hideCartFrag();
                updateCartNumber();
                btnShop.setSelected(true);
                updateShopBtnImage();
            }
            break;
            case TAB_INDEX_LOOKALIKE: {
                //myRenderer.stopZoom();
                //myRenderer.setAcceptTouch(false);
                hideCollectionFragWithDetach();
                hideMixMatchFragWithDetach();
                hideCartFrag();
                btnShop.setSelected(false);
                updateShopBtnImage();
            }
            break;
            case TAB_INDEX_MIXMATCH: {
                //myRenderer.stopZoom();
                //myRenderer.setAcceptTouch(false);
                hideCollectionFragWithDetach();
                hideLookaLikeFragWithDetach();
                hideCartFrag();
            }
            break;
            case TAB_INDEX_CART: {
                //myRenderer.stopZoom();
                //myRenderer.setAcceptTouch(false);
                hideCollectionFragWithDetach();
                hideLookaLikeFragWithDetach();
                btnLookaLike.setSelected(false);
                btnShop.setSelected(true);
            }
            break;
        }
        selectedTabIndex = index;
        return selectedTabIndex;
    }

    private void updateChangeButton() {
        Log.w(LOGTAG, "updateChangeButton index: " + comboIndex + "  size: " + comboList.size());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (comboIndex >= comboList.size() - 1) {
                    btnForward.setVisibility(View.INVISIBLE);
                } else {
                    btnForward.setVisibility(View.VISIBLE);
                }
                if (comboIndex <= 0) {
                    btnBackward.setVisibility(View.INVISIBLE);
                } else {
                    btnBackward.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (!shouldClick() || !myRenderer.isZoomCompleted() || !isFirstLoadDone || myRenderer.isVideo) {
            return;
        }
        switch (v.getId()) {
            case R.id.ib_avatar_backword: {
                myRenderer.changeState(-1);
            }
            break;
            case R.id.ib_avatar_forward: {
                myRenderer.changeState(1);
            }
            break;
            case R.id.toolbar_ibtn_looks: {
                showCollectionFrag();
            }
            break;

            case R.id.toolbar_ibtn_redoAvatar: {
                updateTime(currentComboData);
                btnLookaLike.setSelected(false);
                btnLooks.setSelected(false);
                btnRedoAvatar.setSelected(true);
                Intent updateAvatar = new Intent(ShopActivity.this, RedoAvatarActivity.class);
                updateAvatar.putExtra("comboData", currentComboData);
                startActivityForResult(updateAvatar, RESULT_CODE_UPDATE_AVATAR_ACTIVITY);

                /*
                Intent intent = new Intent(ShopActivity.this, RedoAvatarActivity.class);
                intent.putExtra("comboData", currentComboData);
                startActivity(updateAvatar);
                finish();
                */
            }
            break;

            case R.id.toolbar_ibtn_shop: {
                if (myRenderer.isZoomCompleted()) {
                    int prevIndex = selectedTabIndex;
                    hideAllFrag(TAB_INDEX_SHOP);
                    btnShop.setSelected(true);
                    if (prevIndex == TAB_INDEX_SHOP || prevIndex == TAB_INDEX_MIXMATCH) {
                        lookAtIndex++;
                        if (lookAtIndex == 3) {
                            lookAtIndex = 0;//LOOKAT_INDEX_BODY
                        }
                    }
                    updateShopBtnImage();
                    myRenderer.viewLookAt(lookAtIndex);
                }
            }
            break;

            case R.id.toolbar_ibtn_lookalike: {
                // showCartFragment();
                showLookalikeFragment();
            }
            break;
            case R.id.toolbar_ibtn_mixmatch: {
                if (isMixMatchShowing) {
                    isMixMatchShowing = false;
                } else {
                    isMixMatchShowing = true;
                }
                showMixMatchFragment();
            }
            break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public void onCollectionFragmentInteraction(ComboData comboData) {
//        Log.w("receiver", "onCollectionFragmentInteraction combo received : " + comboData.getCombo_ID());
//        if (collectionFragment != null && !collectionFragment.isHidden()) {//TODO
//            hideCollectionFragWithDetach();
//        }
//        hideLookaLikeFragWithDetach();
//        btnShop.setSelected(true);
//        updateShopBtnImage();
//        prevComboData = currentComboData;
//        currentComboData = comboData;
//
//        lookAtIndex = 0;
//        myRenderer.viewLookAt(lookAtIndex);
//        updateShopBtnImage();
//
//        Log.d(LOGTAG, "Looks currentComboData  :" + currentComboData.getCombo_ID());
//        myRenderer.changeObjects(currentComboData);
//
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 60);
    }

    @Override
    public void onCollectionFragmentInteraction(ComboData comboData, ArrayList<ComboData> comboList, int index) {
        Log.w("receiver", "onCollectionFragmentInteraction combo received : " + comboData.getCombo_ID());
        if (collectionFragment != null && !collectionFragment.isHidden()) {//TODO
            hideCollectionFragWithDetach();
        }
        showingComboLoading();
        hideLookaLikeFragWithDetach();
        btnShop.setSelected(true);
        updateShopBtnImage();

        String title = comboData.getLooksCategoryTitle();
        int indexTitle = ConstantsUtil.arrayListLooksLabelName.indexOf(title);
        spinnerToolbar.setSelection(indexTitle);

        prevComboData = currentComboData;
        currentComboData = comboData;
        this.comboList = comboList;
        comboIndex = index;
        updateChangeButton();
        lookAtIndex = 0;
        myRenderer.viewLookAt(lookAtIndex);
        updateShopBtnImage();

        myRenderer.changeObjects(currentComboData);
        Log.d(LOGTAG, "Looks currentComboData  :" + currentComboData.getCombo_ID());
    }


    @Override
    public void onMixMatchFragmentInteraction(BaseAccessoryItem item) {
        if (!myRenderer.isZoomCompleted())
            return;
        if (countDownloadMixmatch != 0) {
            countDownloadProgress--;
        }
        countDownloadMixmatch = 0;
        hideDownloadProgressBar();

        String accessoryType = item.getAccessoryType();
        updateViewAccessoryToServer(item);
        if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeEarrings.toString())) {
            item.setCategorySku(""); //TODO
            currentComboData.setA61(item);
            currentComboData.setIsA61Removed(false);
            myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_A7_EARRINGS, item, false);
            // updateLookAtIndex(IRenderer.LOOKAT_INDEX_FACE, 180);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeShoes.toString())) {
            currentComboData.setA71(item);
            if (item.dependentItem != null)
                myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_LEGS, item.dependentItem, false, false);
            else {
                myRenderer.resetObj(ConstantsUtil.GL_INDEX_LEGS);
            }
            myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_A6_SHOES, item, false);
            //updateLookAtIndex(IRenderer.LOOKAT_INDEX_BODY, 180);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeSunglasses.toString())) {
            currentComboData.setA91(item);
            currentComboData.setIsA91Removed(false);
            myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_SPECS_A9, item, true);
            //updateLookAtIndex(IRenderer.LOOKAT_INDEX_SHOULDER, 180);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeBags.toString())
                || accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeClutches.toString())) {
            currentComboData.setA101(item);
            currentComboData.setIsA101Removed(false);
            myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_A10_BAGS_CLUTCHES, item, false);
            //updateLookAtIndex(IRenderer.LOOKAT_INDEX_BODY, 180);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeHair.toString())) {
            currentComboData.setA81(item);
            myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_HAIR_A8, item, true);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString())) {
            currentComboData.setA91(item);
            currentComboData.setIsA91Removed(false);
            myRenderer.renderMixMatchObj(ConstantsUtil.GL_INDEX_SPECS_A9, item, true);
            //updateLookAtIndex(IRenderer.LOOKAT_INDEX_SHOULDER, 180);
        }
    }

    @Override
    public void onMixMatchFragmentInteractionSetDefault(BaseAccessoryItem item) {


    }

    @Override
    public void onMixMatchFragmentInteractionRemoveAccessory(String accType) {
        ////listRenderedAccessory.remove(accType);

        String accessoryType = accType;
        if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeEarrings.toString())) {
            myRenderer.resetRenderMixMatchObj(ConstantsUtil.GL_INDEX_A7_EARRINGS);
            currentComboData.setA61(null);
            currentComboData.setIsA61Removed(true);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeShoes.toString())) {
            myRenderer.resetRenderMixMatchObj(ConstantsUtil.GL_INDEX_A6_SHOES);
            //currentComboData.setA71(null);
            //currentComboData.setIsA71Removed(true);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeSunglasses.toString())) {

            myRenderer.resetRenderMixMatchObj(ConstantsUtil.GL_INDEX_SPECS_A9);
            currentComboData.setA91(null);
            currentComboData.setIsA91Removed(true);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeBags.toString())
                || accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeClutches.toString())) {
            myRenderer.resetRenderMixMatchObj(ConstantsUtil.GL_INDEX_A10_BAGS_CLUTCHES);
            currentComboData.setA101(null);
            currentComboData.setIsA101Removed(true);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeHair.toString())) {
            myRenderer.resetRenderMixMatchObj(ConstantsUtil.GL_INDEX_HAIR_A8);
            //currentComboData.setA81(null);
            //currentComboData.setIsA81Removed(true);
        } else if (accessoryType.equals(ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString())) {
            if (MixMatchSharedResource.getInstance().renderedSpecs.equals(faceItem.getSpecsId())) {
                faceItem.setSpecsId("");
                faceItem.setSpecsObjkey("");
                faceItem.setSpecsPngkey("");
                dataSource.create(faceItem);
                User.getInstance().setDefaultFaceItem(faceItem);
                removeDefaultSpecs();
            }

            currentComboData.setA91(null);
            currentComboData.setIsA91Removed(true);
            myRenderer.resetRenderMixMatchObj(ConstantsUtil.GL_INDEX_SPECS_A9);
        }
        MixMatchSharedResource.getInstance().removeAccessory(accType);
    }

    private void removeDefaultSpecs() {
        //TODO
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        });
        ArrayList<FaceItem> listFaceItem = (ArrayList<FaceItem>) InkarneAppContext.getDataSource().getAvatars();
        for (FaceItem face : listFaceItem) {
            if (face != null && face.getPbId() != null && faceItem != null && !face.getPbId().equals(faceItem.getPbId())) {
                face.setSpecsId("");
                face.setSpecsObjkey("");
                face.setSpecsPngkey("");
                dataSource.create(face);
            }
        }
    }

    @Override
    public void onFragmentLookLikeInteraction(Uri uri) {

    }

    @Override
    public void onFragmentLookLikeInteractionCartClicked() {
        if (myRenderer.isZoomCompleted()) {
            hideAllFrag(TAB_INDEX_CART);
            onHomeDrawerCartClick(conCartTopbar);

        }
    }

    @Override
    public void onFragmentCartBackBtnInteraction() {
        hideCartFrag();
        selectedTabIndex = TAB_INDEX_SHOP;
        mGLView.requestRender();
    }

    @Override
    public void onFragmentCartInteraction(ComboData receivedCombo) {
        if (cartFragment != null) {//TODO
            hideCartFrag();
        }

        String looksCat = receivedCombo.getCombo_Style_Category();
        int index = ConstantsUtil.arrayListLooksCategory.indexOf(looksCat);
        ComboDataLooksItem looksItem = listOfComboList.get(index);
        comboList = looksItem.getComboList();
        comboIndex = getComboIndex(comboList, receivedCombo);
        spinnerToolbar.setSelection(index);

        hideLookaLikeFragWithDetach();
        btnShop.setSelected(true);
        updateShopBtnImage();
        updateCartNumber();
        prevComboData = currentComboData;
        currentComboData = receivedCombo;

        lookAtIndex = 0;
        myRenderer.viewLookAt(lookAtIndex);
        myRenderer.changeObjects(currentComboData);
        Log.d(LOGTAG, "Looks currentComboData  :" + currentComboData.getCombo_ID());
//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {
//
//            }
//        }, 60);
    }

    public void resetMixMatch(ComboData comboData) {
        comboData.resetMixMatch();
    }

    public void onBtnZoomHandler(View v) {
        if (myRenderer.isVideo) {
            //myRenderer.stopVideo();
            //finishVideo();
        } else {
            if (myRenderer.isZoomCompleted()) {
                myRenderer.zoomAnimation();
            } else {
                myRenderer.stopZoom();
                setButtonsClick(true);

            }
        }
    }

    public void showDownloadProgressBar() {
        countDownloadProgress++;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //pbGLView.setLoadingText(getString(R.string.message_rendering_looks));
                //pbGLView.setVisibility(View.VISIBLE);
                pbCircular.setVisibility(View.VISIBLE);

            }
        });
    }

    public void showDownloadProgressBarWithoutIncrement() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //pbGLView.setLoadingText(getString(R.string.message_rendering_looks));
                //pbGLView.setVisibility(View.VISIBLE);
                pbCircular.setVisibility(View.VISIBLE);

            }
        });
    }

    public void hideDownloadProgressBar() {
        countDownloadProgress--;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (countDownloadProgress <= 0) {
                    //pbGLView.setLoadingText(getString(R.string.message_rendering_looks));
                    //pbGLView.setVisibility(View.INVISIBLE);
                    pbCircular.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public void hideForceDownloadProgressBar() {
        countDownloadProgress = 0;
        hideDownloadProgressBar();
    }

    public void showAlertForCreateAvatarInfo() {
        if (InkarneAppContext.isFirstTimeComboRender()) {
            final AlertDialog.Builder b = new AlertDialog.Builder(ShopActivity.this);
            //b.setTitle("  ");
            b.setMessage(Html.fromHtml("<font color='#3c2273'>You can personalise your avatar using one selfie! <br><br>" +
                    "Click on the avatar tab below.</font>"));
            //b.setMessage("You can personalise your avatar using one selfie! \nClick on the avatar tab below. ");
            final AlertDialog a = b.create();
            b.setCancelable(false);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    a.show();
                    InkarneAppContext.setFirstTimeComboRender(false);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            a.dismiss();
                        }
                    }, 4000);
                }
            }, 4000);
        }
    }


    public class glViewRenderer extends IRenderer {
        private final Context context;
        public BaseAccessoryItem currentMixMatchItem;
        public boolean isChangeObjFinish = false;
        public boolean isCriticalObjRenderFailed = false;
        public int lookAtObjn = 0;


        public void checkObjContainer() {
            for (int i = 0; i < ConstantsUtil.GL_INDEX_TOTAL; i++) {
                boolean isObjNull = isNull(i);
                if (!isObjNull)
                    continue;
                switch (i) {
                    case ConstantsUtil.GL_INDEX_BODY: {
                        renderCriticalObj(i, faceItem.getBodyPngkey(), faceItem.getBodyObjkey(), false);
                    }
                    break;
                    case ConstantsUtil.GL_INDEX_FACE: {
                        renderCriticalObj(i, faceItem.getFacePngkey(), faceItem.getFaceObjkey(), false);
                    }
                    break;
                    case ConstantsUtil.GL_INDEX_HAIR_A8: {
                        renderCriticalObj(i, faceItem.getHairPngKey(), faceItem.getHairObjkey(), false);
                    }
                    break;
                    case ConstantsUtil.GL_INDEX_A1_BOTTOM: {
                        renderCriticalObj(i, currentComboData.getmA1_Png_Key_Name(), currentComboData.getmA1_Obj_Key_Name(), false);
                    }
                    break;
                    case ConstantsUtil.GL_INDEX_A6_SHOES: {
                        if (currentComboData.getLegId() == null || currentComboData.getLegId().equals("NA"))
                            renderCriticalObj(i, currentComboData.getmA7_Png_Key_Name(), currentComboData.getmA7_Obj_Key_Name(), false);
                    }
                    break;

                    case ConstantsUtil.GL_INDEX_LEGS: {
                        renderLeg(currentComboData);
                    }
                    break;
                }
            }
        }

        //@Override
        public void glInit() {
            Log.d(LOGTAG, "glInit");
            setAcceptTouch(false);
            showDownloadProgressBarWithoutIncrement();
            loadAvatar();

            if (currentComboData != null) {
                Log.d(LOGTAG, "Avatar Loaded");
                resetMixMatch(currentComboData);
                //Load 7 SKUs
                changeObjects(currentComboData);
            } else {
                InkarneAppContext.showAlert("Looks not available. Please choose other look from collection");
            }
            isFirstLoadDone = true;
        }

        public void zoomAnimation() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setButtonsClick(false);
                    btnShopZoom.setImageResource(R.drawable.btn_rot_stop);
                    btnLookaLike.setSelected(false);
                    btnLooks.setSelected(false);
                    btnMixMatch.setSelected(false);
                    btnRedoAvatar.setSelected(false);
                    btnShop.setSelected(false);
                    lookAtIndex = 0;
                    hideMixMatchFragWithDetach();
                    myRenderer.viewLookAt(lookAtIndex);
                    updateShopBtnImage();
                    mGLView.requestRender();
                }
            });
            Log.d("Zoom", "I ah here");
            runAutoZoom();
        }

        private void resetCount() {
            countDownloadProgress = 0;
            countDownloadMixmatch = 0;
            countDownloadAvatar = 0;
            countDownloadCombo = 0;
        }

        protected void loadAvatar() {
            if (faceItem == null)
                return;
            //showDownloadProgressBarWithoutIncrement();
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    showFaceLoading();
//                }
//            });

            showingComboLoading();
            isChangeObjFinish = false;
            isCriticalObjRenderFailed = false;
            countDownloadAvatar = 0;
            setAcceptTouch(false);
            Log.e(LOGTAG, "LOAD AVATAR");

            try {
                if (User.getInstance().getmGender().equals("m")) {
                    Log.d(LOGTAG, "BG texture path:  bg_shop_screen_male");

                    myRenderer.changeObj(ConstantsUtil.GL_INDEX_BG, "ply_bg_shop_screen_male.ply", (R.drawable.texture_stage_male), false);

                } else {
                    Log.d(LOGTAG, "BG texture path:  bg_shop_screen_female");
                    myRenderer.changeObj(ConstantsUtil.GL_INDEX_BG, "ply_bg_shop_screen_female.ply", (R.drawable.texture_stage_female), false);
                }
            } catch (PLYLoadMismatch plyLoadMismatch) {
                plyLoadMismatch.printStackTrace();
            }

            Log.e(LOGTAG, "AVATAR: GL_INDEX_FACE");
            if (faceItem.getFaceId().equals("1")) {
                try {
                    if (User.getInstance().getmGender().equals("m")) {
                        Log.d(LOGTAG, "face texture path:  bg_shop_screen_male");
                        myRenderer.changeObj(ConstantsUtil.GL_INDEX_FACE, "ply_face_male.ply", (R.drawable.texture_face_male), false);
                    } else {
                        Log.d(LOGTAG, "BG texture path:  bg_shop_screen_female");
                        myRenderer.changeObj(ConstantsUtil.GL_INDEX_FACE, "ply_face_female.ply", (R.drawable.texture_face_female), false);
                    }
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    plyLoadMismatch.printStackTrace();
                }
            } else {
                renderCriticalObj(ConstantsUtil.GL_INDEX_FACE, faceItem.getFacePngkey(), faceItem.getFaceObjkey(), false);
            }

            Log.e(LOGTAG, "AVATAR: GL_INDEX_BODY");
            renderCriticalObj(ConstantsUtil.GL_INDEX_BODY, faceItem.getBodyPngkey(), faceItem.getBodyObjkey(), false);

            Log.e(LOGTAG, "GL_INDEX_SPECS_A8  Index: " + ConstantsUtil.GL_INDEX_HAIR_A8 + "  Texture :" + faceItem.getHairPngKey() + "  Obj :" + faceItem.getHairObjkey());
            if (faceItem.getFaceId().equals("1") && (faceItem.getHairstyleId() == null || faceItem.getHairstyleId().isEmpty())) {
                try {
                    if (User.getInstance().getmGender().equals("m")) {
                        Log.d(LOGTAG, "torso texture path:  bg_shop_screen_male");
                        myRenderer.changeObj(ConstantsUtil.GL_INDEX_HAIR_A8, "ply_hair_male_mhs002.ply", (R.drawable.texture_hair_male_mhs002), false);

                    } else {
                        Log.d(LOGTAG, "torso texture path:  bg_shop_screen_female");
                        myRenderer.changeObj(ConstantsUtil.GL_INDEX_HAIR_A8, "ply_hair_female_fhs005.ply", (R.drawable.texture_hair_female_fhs005), false);
                    }
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    plyLoadMismatch.printStackTrace();
                }

            } else {
                if (renderCriticalObj(ConstantsUtil.GL_INDEX_HAIR_A8, faceItem.getHairPngKey(), faceItem.getHairObjkey(), false)) {
                    MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeHair.toString(), faceItem.getHairstyleId());
                }
            }

            Log.e(LOGTAG, "GL_INDEX_SPECS_A9  Index: " + ConstantsUtil.GL_INDEX_SPECS_A9 + "  Texture :" + faceItem.getSpecsPngkey() + "  Obj :" + faceItem.getSpecsObjkey());
            if (renderObj(ConstantsUtil.GL_INDEX_SPECS_A9, faceItem.getSpecsObjkey(), faceItem.getSpecsPngkey(), false)) {
                MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString(), faceItem.getSpecsId());
            }
        }

        @Override
        public void zoom(float v) {
            Log.w(LOGTAG, "zom value : " + v);
            if (Math.abs(v) < 50) {
                return;
            }
            super.zoom(v);
//            scale += v/10000f;
//            Log.d("zoomEffect", "val: " + v);
//            fixZoom();

            if (myRenderer.isZoomCompleted() && isAcceptTouch()) {
//                int prevIndex = selectedTabIndex;
//                //hideAllFrag(TAB_INDEX_SHOP);
//                btnShop.setSelected(true);
                if (selectedTabIndex == TAB_INDEX_SHOP || selectedTabIndex == TAB_INDEX_MIXMATCH) {
                    Log.w(LOGTAG, "zom index : " + lookAtIndex);
                    if (v > 0)
                        lookAtIndex++;
                    else {
                        lookAtIndex--;
                    }
                    if (lookAtIndex > 2) {
                        lookAtIndex = 2;//LOOKAT_INDEX_BODY
                    }
                    if (lookAtIndex < 0) {
                        lookAtIndex = 0;//LOOKAT_INDEX_BODY
                    }
                }
                updateShopBtnImage();
                Log.w(LOGTAG, "zom index 2: " + lookAtIndex);
                myRenderer.viewLookAt(lookAtIndex);
            }

        }

        @Override
        public void onDrawFrame(GL10 gl) {
            super.onDrawFrame(gl);
            if (isTakeScreenShot && !isTakingScreenshot) {
                mGLView.requestRender();
                isTakeScreenShot = false;
                isTakingScreenshot = true;
                final String filename = ConstantsUtil.FILE_NAME_SHARE;
                new Screenshot().takeScreenshot(0, 0, width, height, gl, filename);
                Log.e(LOGTAG, filename);
                isTakingScreenshot = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        pbGLView.setVisibility(View.INVISIBLE);
                        sharePics(filename + ".png");
                        Log.e(LOGTAG, "onDrawFrame share");
                    }
                });
            }
            if (isVideo) {
//                if (videoScreen != null) {
//                    videoScreen.saveVideoStart(width, height);
//                }
                Bitmap image = videoScreen.takeVideoScreen(0, 0, width, height);
                videoScreen.saveVideoAddImage(image);
                Log.e(LOGTAG, "360 image added");
            }
        }


        public void renderMixMatchObj(int index, BaseAccessoryItem item, boolean shouldShine) {
            renderMixMatchObj(index, item, shouldShine, true);
        }

        public void renderMixMatchObj(int index, BaseAccessoryItem item, boolean shouldShine, boolean shouldRender) {
            currentMixMatchItem = item;
            if (shouldRender)
                setAcceptTouch(false);
            String pngFilePath = StoragePath + "/" + item.getTextureAwsKey();
            String objFilePath = StoragePath + "/" + item.getObjAwsKey();
            Log.d(LOGTAG, "renderMixMatchObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
            if (ConstantsUtil.checkFileKeysExist(item.getTextureAwsKey(), item.getObjAwsKey())) {
                try {
                    myRenderer.changeObj(index, objFilePath, (pngFilePath), shouldShine);
                    MixMatchSharedResource.getInstance().addAccessory(item);
                    if (mixMatchFrag != null) {
                        mixMatchFrag.accessoryLoadSuccessful(item);
                    }
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    Log.e(LOGTAG, "plyLoadMismatch exception");
                    plyLoadMismatch.printStackTrace();
                    shouldRender = true;
                    //TODO
                    if (mixMatchFrag != null) {
                        mixMatchFrag.accessoryLoadFailed(item);
                    }
                    //downloadAssetMixMatch(currentComboData.getCombo_ID(), index, item, true);
                }
            } else {
                shouldRender = false;
                downloadAssetMixMatch(currentComboData.getCombo_ID(), index, item, true);
            }

            if (shouldRender) {
                mGLView.requestRender();
                //changeObjFinish();
                setAcceptTouch(true);
            }
        }

        public void reRenderMixMatchObj(int index, BaseAccessoryItem item, boolean shouldShine, boolean shouldRender) {
            if (shouldRender)
                setAcceptTouch(false);
            Log.e(LOGTAG, "reRenderMixMatchObj : index: " + index + " ");
            String pngFilePath = StoragePath + "/" + item.getTextureAwsKey();
            String objFilePath = StoragePath + "/" + item.getObjAwsKey();
            Log.d(LOGTAG, "renderMixMatchObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
            if (ConstantsUtil.checkFileKeysExist(item.getTextureAwsKey(), item.getObjAwsKey())) {
                try {
                    myRenderer.changeObj(index, objFilePath, (pngFilePath), shouldShine);
                    MixMatchSharedResource.getInstance().addAccessory(item);
                    if (mixMatchFrag != null) {
                        mixMatchFrag.accessoryLoadSuccessful(item);
                    }
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    Log.e(LOGTAG, "plyLoadMismatch exception");
                    Toast.makeText(getApplicationContext(), "Opps,could not load the Accessory.", Toast.LENGTH_SHORT).show();
                    plyLoadMismatch.printStackTrace();
                    shouldRender = false;
                    if (mixMatchFrag != null) {
                        mixMatchFrag.accessoryLoadFailed(item);
                    }
                }
            } else {
                shouldRender = false;
            }

            if (shouldRender) {
                mGLView.requestRender();
                //changeObjFinish();
                setAcceptTouch(true);
            }
        }

        public void downloadAssetMixMatch(final String comboId, final int index, final BaseAccessoryItem item, boolean shouldRender) {
            showDownloadProgressBar();
            countDownloadMixmatch++;
            item.setTextureDStatus(ConstantsUtil.EDownloadStatusType.eDownloadTobeStarted.intStatus());
            item.setObjDStatus(ConstantsUtil.EDownloadStatusType.eDownloadTobeStarted.intStatus());
            new AssetDownloader(ShopActivity.this).downloadAsset(item, new OnAssetDownloadListener() {
                @Override
                public void onDownload(BaseAccessoryItem item) {
                    hideDownloadProgressBar();
                    countDownloadMixmatch--;

                    InkarneAppContext.getDataSource().create(item);
                    if (currentMixMatchItem != null && item.getObjId().equals(currentMixMatchItem.getObjId()) && currentComboData.getCombo_ID().equals(comboId))
                        reRenderMixMatchObj(index, item, false, true);

                    else {
                        // if(!currentComboData.getCombo_ID().equals(comboId))//TODO
                        hideForceDownloadProgressBar();
                    }
                }

                @Override
                public void onDownloadFailed(String comboId) {
                    hideDownloadProgressBar();
                    if (mixMatchFrag != null) {
                        mixMatchFrag.accessoryLoadFailed(item);
                    }
                }

                @Override
                public void onDownloadProgress(String comboId, int percentage) {
                    hideDownloadProgressBar();
                    if (mixMatchFrag != null) {
                        mixMatchFrag.accessoryLoadFailed(item);
                    }
                }

            });
        }


        public void resetRenderMixMatchObj(int index) {
            mGLView.requestRender();
            setAcceptTouch(false);
            resetObj(index);
            changeObjFinish();
            setAcceptTouch(true);
        }

//        public boolean renderObj(final int index, final String pngKey, final String objKey, final boolean shouldShine) {
//            return renderObj(index, pngKey, objKey, shouldShine);
//        }

        public synchronized boolean renderObj(final int index, final String pngKey, final String objKey, final boolean shouldShine) {
            final boolean[] iAssetReady = {true};

            String pngFilePath = StoragePath + pngKey;
            String objFilePath = StoragePath + objKey;
            Log.d(LOGTAG, "renderObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
            if (ConstantsUtil.checkFileKeysExist(pngKey, objKey)) {
                Log.e(LOGTAG, "renderObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
                try {
                    myRenderer.changeObj(index, objFilePath, (pngFilePath), shouldShine);
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    plyLoadMismatch.printStackTrace();
                    Log.d(LOGTAG, "Index: " + index + "  Exception occurred");
                    iAssetReady[0] = false;
                }
            } else {
                iAssetReady[0] = false;
            }
            return iAssetReady[0];
        }

        public synchronized boolean renderCriticalObj(final int index, final String pngKey, final String objKey, final boolean shouldShine) {
            final boolean[] iAssetReady = {true};

            String pngFilePath = StoragePath + pngKey;
            String objFilePath = StoragePath + objKey;
            Log.e(LOGTAG, "RenderObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
            if (ConstantsUtil.checkFileKeysExist(pngKey, objKey)) {
                Log.e(LOGTAG, "RenderObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
                try {
                    myRenderer.changeObj(index, objFilePath, (pngFilePath), shouldShine);
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    plyLoadMismatch.printStackTrace();
                    Log.e(LOGTAG, "RenderObj Index: " + index + "  Exception occurred");
                    iAssetReady[0] = false;
                    ConstantsUtil.deleteDirectory(new File(pngFilePath));
                    ConstantsUtil.deleteDirectory(new File(objFilePath));
                    downloadAsset(currentComboData.getCombo_ID(), index, pngKey, objKey, true);
                }
            } else {
                Log.e(LOGTAG, "RenderObj Index: " + index + "  checkFileKeysExist failed");
                iAssetReady[0] = false;
                downloadAsset(currentComboData.getCombo_ID(), index, pngKey, objKey, true);
            }
            return iAssetReady[0];
        }

        public synchronized boolean reRenderCriticalObj(final int index, final String pngKey, final String objKey, final boolean shouldShine) {
            final boolean[] iAssetReady = {true};
            String pngFilePath = StoragePath + pngKey;
            String objFilePath = StoragePath + objKey;
            Log.d(LOGTAG, "Re-RenderObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
            if (ConstantsUtil.checkFileKeysExist(pngKey, objKey)) {
                Log.e(LOGTAG, "Re-RenderObj Index: " + index + " Texture :" + pngFilePath + "  Obj :" + objFilePath);
                try {
                    myRenderer.changeObj(index, objFilePath, (pngFilePath), shouldShine);
                } catch (PLYLoadMismatch plyLoadMismatch) {
                    plyLoadMismatch.printStackTrace();
                    Log.d(LOGTAG, "Re-RenderObj Index: " + index + "  Exception occurred");
                    iAssetReady[0] = false;
                    isCriticalObjRenderFailed = true;
                    //checkShouldAcceptTouch();
                }
            } else {
                Log.e(LOGTAG, "Re-RenderObj Index: " + index + "  checkFileKeysExist failed");
                iAssetReady[0] = false;
                isCriticalObjRenderFailed = true;
                //checkShouldAcceptTouch();
            }
            checkShouldAcceptTouch();
            return iAssetReady[0];
        }

        public void downloadAsset(final String comboId, final int index, final String keyPng, final String keyObj, boolean shouldRender) {
            showDownloadProgressBar();
            if (index == ConstantsUtil.GL_INDEX_HAIR_A8 || index == ConstantsUtil.GL_INDEX_FACE || index == ConstantsUtil.GL_INDEX_BODY) {
                countDownloadAvatar++;
            } else if (index == ConstantsUtil.GL_INDEX_LEGS || index == ConstantsUtil.GL_INDEX_A6_SHOES || index == ConstantsUtil.GL_INDEX_A1_BOTTOM) {
                countDownloadCombo++;
            }
            new AssetDownloader(ShopActivity.this).downloadAsset(keyObj, keyPng, new OnAssetDownloadListener() {
                @Override
                public void onDownload(BaseAccessoryItem item) {
                    hideDownloadProgressBar();
                    if (index == ConstantsUtil.GL_INDEX_HAIR_A8 || index == ConstantsUtil.GL_INDEX_FACE || index == ConstantsUtil.GL_INDEX_BODY) {
                        countDownloadAvatar--;
                    } else if (index == ConstantsUtil.GL_INDEX_LEGS || index == ConstantsUtil.GL_INDEX_A6_SHOES || index == ConstantsUtil.GL_INDEX_A1_BOTTOM) {
                        countDownloadCombo--;
                    }
                    if (currentComboData.getCombo_ID().equals(comboId)) {
                        //reRenderCriticalObj(index, keyPng, keyObj, true);
                        reRenderCriticalObj(index, keyPng, item.getObjAwsKey(), true);
                        //checkShouldAcceptTouch();
                    } else {
                        //isCriticalObjRenderFailed = false;
                        countDownloadCombo = 0;
                        countDownloadAvatar = 0;
                        hideForceDownloadProgressBar();
                    }
                }

                @Override
                public void onDownloadFailed(String comboId) {
                    hideDownloadProgressBar();
                }

                @Override
                public void onDownloadProgress(String comboId, int percentage) {
                    hideDownloadProgressBar();
                }
            });
        }

        private void checkShouldAcceptTouch() {
            Log.e(LOGTAG, "checkShouldAcceptTouch 0");
            if (isCriticalObjRenderFailed) {
                if (prevComboData != null) {//todo should we remove it
                    currentComboData = prevComboData;
                    prevComboData = null;
                    Log.e(LOGTAG, "checkShouldAcceptTouch 1 Previous combo loaded : Asset not ready");
                    changeObjects(currentComboData);
                } else {
                    countDownloadAvatar = 0;
                    countDownloadCombo = 0;
                    countDownloadProgress = 0;
                    if (comboIndex < comboList.size() - 2) {
                        comboIndex += 1;
                        currentComboData = comboList.get(comboIndex);
                        //updateChangeButton();
                        Log.e(LOGTAG, "checkShouldAcceptTouch 1 loadCombo");
                        loadCombo(currentComboData);
                    } else {
                        comboIndex -= 1;
                        currentComboData = comboList.get(comboIndex);
                        //updateChangeButton();
                        Log.e(LOGTAG, "checkShouldAcceptTouch 1 loadCombo");
                        loadCombo(currentComboData);
                    }
                    if (currentComboData != null)
                        loadCombo(currentComboData);
                    else {// not required
                        ArrayList<ComboData> comboListSeen = (ArrayList<ComboData>) dataSource.getComboDataLaunchSeen(2);
                        if (comboListSeen != null && comboListSeen.size() > 0) {
                            currentComboData = comboListSeen.get(0);
                            loadCombo(currentComboData);
                        }
                    }


                    Log.e(LOGTAG, "Could not load complete looks : Asset not ready");
                }
            } else {
                Log.e(LOGTAG, "checkShouldAcceptTouch 2  : countDownloadAvatar: " + countDownloadAvatar + "   countDownloadCombo: " + countDownloadCombo + "   isChangeObjFinish:" + isChangeObjFinish);
                if (countDownloadAvatar <= 0 && countDownloadCombo <= 0 && isChangeObjFinish) {
                    Log.e(LOGTAG, "checkShouldAcceptTouch 3 setAcceptTouch");
                    if (!InkarneAppContext.getSettingIsAutoRotateLookDisabled()) {
                        zoomAnimation();
                    } else {
                        zoomCompleted();
                    }
                    GATrackActivity("MainActivity/" + currentComboData.getCombo_ID());
                    mGLView.requestRender();
                    setAcceptTouch(true);
                    resetCount();
                    hideForceDownloadProgressBar();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    pbGLView.setVisibility(View.INVISIBLE);

                }
            });
        }

        public boolean renderLeg(ComboData comboData) {
            final boolean[] iAssetReady = {true};
            Log.d(LOGTAG, "GL_INDEX_LEGS");
            if (comboData.getLegId() == null || comboData.getLegId().equals("NA")) {
                resetObj(ConstantsUtil.GL_INDEX_LEGS);
                return iAssetReady[0];
            }
            if (comboData.getLegId() != null && comboData.getLegId().length() != 0) {
                if (comboData.getLegItem() == null) {
                    Log.e(LOGTAG, "******** Leg Obj not found ***********");
                    BaseAccessoryItem item = dataSource.getAccessory(ConstantsUtil.EAccessoryType.eAccTypeLegs.toString(), comboData.getLegId());
                    if (item != null) {
                        comboData.setLegItem(item);
                    } else {
                        Log.e(LOGTAG, "******** Leg Obj not found 2 ***********");
                    }
                }
                if (comboData.getLegItem() != null) {
                    if (!renderCriticalObj(ConstantsUtil.GL_INDEX_LEGS, comboData.getLegItem().getTextureAwsKey(), comboData.getLegItem().getObjAwsKey(), false)) {
                        iAssetReady[0] = false;
                    }
                } else {
                    iAssetReady[0] = false;
                }
            } else {
                iAssetReady[0] = false;
            }
            return iAssetReady[0];
        }

        //@Override
        public void changeState(int change) {
            Log.d("AAA", "CS: " + comboList.size());
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    updateChangeButton();
//                }
//            });
            int state = comboIndex + change;
            String side = null;

            if (state < 0) {
                Log.d(LOGTAG, "Left Limit Achieved");
                return;
            }
            if (state >= comboList.size()) {
                Log.d(LOGTAG, "Right Limit Achieved");
                return;
            }
            if (change == -1) {
                side = "left";
                btnForward.setVisibility(View.VISIBLE);
                Log.d(LOGTAG, "shift left");
            } else {
                side = "right";
                btnBackward.setVisibility(View.VISIBLE);
                Log.d(LOGTAG, "shift right");
            }
            //shiftObj(side);
            if (side.equalsIgnoreCase("left")) {
                comboIndex = comboIndex - 1;
            } else {
                comboIndex = comboIndex + 1;

                //changeObjects(currentComboData);
            }
            currentComboData = comboList.get(comboIndex);
            loadCombo(currentComboData);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateChangeButton();
                }
            });
        }


        public void changeObjects(final ComboData comboData) {
//            new Handler().post(new Runnable() {
//                @Override
//                public void run() {
            if (comboData == null) {
                Log.e(LOGTAG, "changeObjects failed");
                return;
            }
            setAcceptTouch(false);
          //  new Thread(new Runnable() {
          //      @Override
          //      public void run() {
                    int t = 0;

                    Log.w("time", "start " + SystemClock.currentThreadTimeMillis());
                    isChangeObjFinish = false;
                    isCriticalObjRenderFailed = false;
                    countDownloadCombo = 0;
                    MixMatchSharedResource.getInstance().reset();

                    boolean isAssetReady = true;
                    selectedTabIndex = TAB_INDEX_SHOP;
                    Log.e(LOGTAG, "CHANGE OBJECTS ComboId: " + comboData.getCombo_ID());

                    showDownloadProgressBarWithoutIncrement();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateToolbar(comboData);
                            if (prevComboData != null) {
                                updateTime(prevComboData);
                            }
                            glWindowStartTime = getDate();
                            pbGLView.setLoadingText(getString(R.string.message_rendering_looks));
                            pbGLView.setVisibility(View.VISIBLE);
                            btnShop.setSelected(true);
                            updateShopBtnImage();
                            btnShop.setEnabled(false);
                            updateChangeButton();
                        }
                    });

                    Log.d(LOGTAG, "GL_INDEX_A1_BOTTOM");
                    boolean isReadyA1 = renderCriticalObj(ConstantsUtil.GL_INDEX_A1_BOTTOM, comboData.getmA1_Png_Key_Name(), comboData.getmA1_Obj_Key_Name(), false);
                    if (!isReadyA1) {
                        isAssetReady = false;
                    }
                    Log.e(LOGTAG, "GL_INDEX_HAIR_A8");
                    if (currentComboData.getLooksCategoryTitle().equals(ConstantsUtil.arrayListLooksLabelName.get(7))) {
                        if (!renderObj(ConstantsUtil.GL_INDEX_HAIR_A8, comboData.getmA8_Png_Key_Name(), comboData.getmA8_Obj_Key_Name(), true)) {
                            if (!renderObj(ConstantsUtil.GL_INDEX_HAIR_A8, faceItem.getHairPngKey(), faceItem.getHairObjkey(), true)) {
                                isAssetReady = false;
                            } else {
                                MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeHair.toString(), faceItem.getHairstyleId());
                            }
                        } else {
                            MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeHair.toString(), comboData.getmSKU_ID8());
                        }
                    } else {

                        if (faceItem.getFaceId().equals("1") && (faceItem.getHairstyleId() == null || faceItem.getHairstyleId().isEmpty())) {
                            try {
                                if (User.getInstance().getmGender().equals("m")) {
                                    Log.d(LOGTAG, "torso texture path:  bg_shop_screen_male");
                                    myRenderer.changeObj(ConstantsUtil.GL_INDEX_HAIR_A8, "ply_hair_male_mhs002.ply", (R.drawable.texture_hair_male_mhs002), false);

                                } else {
                                    Log.d(LOGTAG, "torso texture path:  bg_shop_screen_female");
                                    myRenderer.changeObj(ConstantsUtil.GL_INDEX_HAIR_A8, "ply_hair_female_fhs005.ply", (R.drawable.texture_hair_female_fhs005), false);
                                }
                            } catch (PLYLoadMismatch plyLoadMismatch) {
                                plyLoadMismatch.printStackTrace();
                            }

                        } else if (!renderCriticalObj(ConstantsUtil.GL_INDEX_HAIR_A8, faceItem.getHairPngKey(), faceItem.getHairObjkey(), true)) {
                            if (!renderObj(ConstantsUtil.GL_INDEX_HAIR_A8, comboData.getmA8_Png_Key_Name(), comboData.getmA8_Obj_Key_Name(), true)) {
                                isAssetReady = false;
                            } else {
                                MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeHair.toString(), comboData.getmSKU_ID8());
                            }
                        } else {
                            MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeHair.toString(), faceItem.getHairstyleId());
                        }
                    }

                    boolean isReady = renderLeg(comboData);
                    if (!isReady) {
                        isAssetReady = false;
                    }

                    Log.d(LOGTAG, "GL_INDEX_SPECS_A9");
                    if (!renderObj(ConstantsUtil.GL_INDEX_SPECS_A9, comboData.getmA9_Png_Key_Name(), comboData.getmA9_Obj_Key_Name(), true)) {
                        if (!renderObj(ConstantsUtil.GL_INDEX_SPECS_A9, faceItem.getSpecsPngkey(), faceItem.getSpecsObjkey(), true)) {
                            resetObj(ConstantsUtil.GL_INDEX_SPECS_A9);
                        } else {
                            MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString(), faceItem.getSpecsId());
                        }
                    } else {
                        if (comboData.getmA9_Category() != null && comboData.getmA9_Category().equals("Specs"))
                            MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeSpecs.toString(), comboData.getmSKU_ID9());
                        else
                            MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeSunglasses.toString());
                    }

                    Log.d(LOGTAG, "GL_INDEX_A7_EARRINGS");
                    if (!renderObj(ConstantsUtil.GL_INDEX_A7_EARRINGS, comboData.getmA6_Png_Key_Name(), comboData.getmA6_Obj_Key_Name(), false)) {
                        resetObj(ConstantsUtil.GL_INDEX_A7_EARRINGS);
                    } else {
                        MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeEarrings.toString());
                    }

                    Log.d(LOGTAG, "GL_INDEX_A6_SHOES");
                    if (!renderCriticalObj(ConstantsUtil.GL_INDEX_A6_SHOES, comboData.getmA7_Png_Key_Name(), comboData.getmA7_Obj_Key_Name(), false)) {
                        if (comboData.getLegId() != null && !comboData.getLegId().equals("NA")) {
                            resetObj(ConstantsUtil.GL_INDEX_A6_SHOES);
                        }
                        isAssetReady = false;
                    } else {
                        MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeShoes.toString());
                    }

                    Log.d(LOGTAG, "GL_INDEX_A10_BAGS_CLUTCHES");
                    if (!renderObj(ConstantsUtil.GL_INDEX_A10_BAGS_CLUTCHES, comboData.getmA10_Png_Key_Name(), comboData.getmA10_Obj_Key_Name(), false)) {
                        resetObj(ConstantsUtil.GL_INDEX_A10_BAGS_CLUTCHES);
                    } else {
                        MixMatchSharedResource.getInstance().addAccessory(ConstantsUtil.EAccessoryType.eAccTypeBags.toString());
                    }
                    isChangeObjFinish = true;

                    if (isAssetReady) {
                        Log.e(LOGTAG, "setAcceptTouch-isAssetReady " + currentComboData.getCombo_ID());
                        // setAcceptTouch(true);
                        if (!InkarneAppContext.getSettingIsAutoRotateLookDisabled()) {
                            zoomAnimation();
                        } else {
                            zoomCompleted();
                        }
                        GATrackActivity("MainActivity/" + currentComboData.getCombo_ID());
                        mGLView.requestRender();
                        setAcceptTouch(true);
                        resetCount();
                        hideForceDownloadProgressBar();
                        InkarneAppContext.combData = currentComboData;
                        Log.w("time", "end " + SystemClock.currentThreadTimeMillis());
                        addToHistory(currentComboData);
                    } else {
                        //TODO
                        hideForceDownloadProgressBar();
                        Log.e(LOGTAG, "Could not load complete looks : Asset not ready ComboId: " + currentComboData.getCombo_ID());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //Toast.makeText(getApplicationContext(), "Could not reach our server", Toast.LENGTH_SHORT).show();

                            }
                        });
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            pbGLView.setVisibility(View.INVISIBLE);
                            dismissComboLoading();
                        }
                    });

          //      }
          //  }).start();
        }


        public void zoomCompleted() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setButtonsClick(true);
                    btnShopZoom.setImageResource(R.drawable.btn_shop_zoom_animation);
                    showAlertForCreateAvatarInfo();
                    btnShop.setEnabled(true);
                    btnShop.setSelected(true);
                    lookAtIndex = 0;
                    updateShopBtnImage();
                    myRenderer.viewLookAt(lookAtIndex);
                    myRenderer.setAcceptTouch(true);
//                    “You can personalise your avatar using one selfie!
//                            <put an empty line here>
//                    Click on the avatar tab below. “
                    if (InkarneAppContext.isDefaultFaceChanged()) {
                        // ratingBarDialog = new RatingBarDialog();
                        //  ratingBarDialog.showDialog();
                    }
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            showInstruction();
                        }
                    }, 1500);
                }
            });
        }

        glViewRenderer(Context context, GLSurfaceView view, int objNo, String gender) {
            super(context, view, objNo, gender);
            this.context = context;
        }

        @Override
        public void onDoubleTap() {
            Log.d("Double Tap", "I am Double Tapped.");
        }
//        @Override
//        public void zoom(float v) {
//            scale += v / 10000f;
//            Log.d("zoomEffect", "val: " + v);
//            fixZoom();
//        }
    }


    /* Share Functionality */
    public String getShareText() {
        String shareText = "";
        InputStream is = null;
        String base64 = "";
        try {
            is = getAssets().open("share.html");
            int size = is.available();

            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            Bitmap sharableBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.logo_full_105);
            base64 = ConstantsUtil.getEncodeTobase64(sharableBitmap);
            shareText = new String(buffer);

        } catch (IOException e) {
            e.printStackTrace();
        }
        shareText = shareText.replace("imageBase64", base64);
        return shareText;
    }


    private void saveLikesAccessory(ComboData comboData) {
        if (comboData.isLiked() == 1) {
            dataSource.delete(comboData);
            return;
        }
        ComboData comboDataLike = new ComboData();
        boolean isMixmatch = false;
        if (!comboData.isA61Removed()) {
            if (comboData.getA61() != null && comboData.getA61().getObjId() != null && !comboData.getA61().getObjId().isEmpty()) {
                comboDataLike.setmA6_Category(comboData.getA61().getAccessoryType());
                comboDataLike.setmSKU_ID6(comboData.getA61().getObjId());
                comboDataLike.setmA6_Obj_Key_Name(comboData.getA61().getObjAwsKey());
                comboDataLike.setmA6_Png_Key_Name(comboData.getA61().getTextureAwsKey());
                comboDataLike.setmA6_PIC_Png_Key_Name(comboData.getA61().getThumbnailAwsKey());
                isMixmatch = true;
            }
        } else {
            comboDataLike.setmSKU_ID6("NA");
            isMixmatch = true;
        }

        if (comboData.getA71() != null && comboData.getA71().getObjId() != null && !comboData.getA71().getObjId().isEmpty()) {
            comboDataLike.setmA7_Category(comboData.getA71().getAccessoryType());
            comboDataLike.setmSKU_ID7(comboData.getA71().getObjId());
            comboDataLike.setmA7_Obj_Key_Name(comboData.getA71().getObjAwsKey());
            comboDataLike.setmA7_Png_Key_Name(comboData.getA71().getTextureAwsKey());
            comboDataLike.setmA7_PIC_Png_Key_Name(comboData.getA71().getThumbnailAwsKey());
            //comboDataLike.setLegId(comboData.getA71().getDependentItem().getObjId());
            if (comboData.getA71().dependentItem != null) {
                Log.d(LOGTAG, "leg found");
                comboDataLike.setLegId(comboData.getA71().getDependentItem().getObjId());
                comboDataLike.setLegItem(comboData.getA71().dependentItem);
                comboDataLike.setLegObjAwsKey(comboData.getA71().getDependentItem().getObjAwsKey());
                comboDataLike.setLegTextureAwsKey(comboData.getA71().getDependentItem().getTextureAwsKey());

            } else {
                comboDataLike.setLegId("NA");
            }
            isMixmatch = true;
        }
        if (comboData.getA81() != null && comboData.getA81().getObjId() != null && !comboData.getA81().getObjId().isEmpty()) {
            comboDataLike.setmA8_Category(comboData.getA81().getAccessoryType());
            comboDataLike.setmSKU_ID8(comboData.getA81().getObjId());
            comboDataLike.setmA8_Obj_Key_Name(comboData.getA81().getObjAwsKey());
            comboDataLike.setmA8_Png_Key_Name(comboData.getA81().getTextureAwsKey());
            comboDataLike.setmA8_PIC_Png_Key_Name(comboData.getA81().getThumbnailAwsKey());
            isMixmatch = true;
        }

        if (!comboData.isA91Removed()) {
            if (comboData.getA91() != null && comboData.getA91().getObjId() != null && !comboData.getA91().getObjId().isEmpty()) {
                comboDataLike.setmA9_Category(comboData.getA91().getAccessoryType());
                comboDataLike.setmSKU_ID9(comboData.getA91().getObjId());
                comboDataLike.setmA9_Obj_Key_Name(comboData.getA91().getObjAwsKey());
                comboDataLike.setmA9_Png_Key_Name(comboData.getA91().getTextureAwsKey());
                comboDataLike.setmA9_PIC_Png_Key_Name(comboData.getA91().getThumbnailAwsKey());
                isMixmatch = true;
            }
        } else {
            comboDataLike.setmSKU_ID9("NA");
            isMixmatch = true;
        }

        if (!comboData.isA101Removed()) {
            if (comboData.getA101() != null && comboData.getA101().getObjId() != null && !comboData.getA101().getObjId().isEmpty()) {
                comboDataLike.setmA10_Category(comboData.getA101().getAccessoryType());
                comboDataLike.setmSKU_ID10(comboData.getA101().getObjId());
                comboDataLike.setmA10_Obj_Key_Name(comboData.getA101().getObjAwsKey());
                comboDataLike.setmA10_Png_Key_Name(comboData.getA101().getTextureAwsKey());
                comboDataLike.setmA10_PIC_Png_Key_Name(comboData.getA101().getThumbnailAwsKey());
                isMixmatch = true;
            }
        } else {
            comboDataLike.setmSKU_ID10("NA");
            isMixmatch = true;
        }
        if (isMixmatch) {
            comboDataLike.setCombo_ID(comboData.getCombo_ID());
            comboDataLike.setPbId(comboData.getPbId());
            comboDataLike.setFaceId(comboData.getFaceId());
//            if(comboDataLike.getLegId()== null)
//                comboDataLike.setLegId(comboData.getLegId());
            dataSource.createComboLike(comboDataLike);
        }
    }

    private SparseArray<String> getArrayMixMatchSku(ComboData comboData) {
        String a6Id = "NA", a7Id = "NA", a8Id = "NA", a9Id = "NA", a10Id = "NA";
        if (!comboData.isA61Removed()) {
            if (comboData.getA61() != null && comboData.getA61().getObjId() != null && !comboData.getA61().getObjId().isEmpty()) {
                a6Id = comboData.getA61().getObjId();
            } else if (comboData.getmSKU_ID6() != null && !comboData.getmSKU_ID6().isEmpty()) {
                a6Id = comboData.getmSKU_ID6();
            }
        }

        if (comboData.getA71() != null && comboData.getA71().getObjId() != null && !comboData.getA71().getObjId().isEmpty()) {
            a7Id = comboData.getA71().getObjId();
        } else if (comboData.getmSKU_ID7() != null && !comboData.getmSKU_ID7().isEmpty()) {
            a7Id = comboData.getmSKU_ID7();
        }

        if (comboData.getA81() != null && comboData.getA81().getObjId() != null && !comboData.getA81().getObjId().isEmpty()) {
            a8Id = comboData.getA81().getObjId();
        } else {
            a8Id = faceItem.getHairstyleId();
        }

        if (a8Id == null && faceItem.getFaceId().equals("1")) { //Case default faceId ==1
            if (User.getInstance().getmGender().equals("m"))
                a8Id = ConstantsFunctional.HAIRSTYLE_DEFAULT_MALE;
            else
                a8Id = ConstantsFunctional.HAIRSTYLE_DEFAULT_FEMALE;

        }//TODO opened from liked
        if (a8Id == null && comboData.getmSKU_ID8() != null && !comboData.getmSKU_ID8().isEmpty()) {
            a8Id = comboData.getmSKU_ID8();
        }
//        }else if (comboData.getmSKU_ID8() != null && !comboData.getmSKU_ID8().isEmpty()) {
//            a8Id = comboData.getmSKU_ID8();
//        }

        if (!comboData.isA91Removed()) {
            if (comboData.getA91() != null && comboData.getA91().getObjId() != null && !comboData.getA91().getObjId().isEmpty()) {
                a9Id = comboData.getA91().getObjId();
            } else if (comboData.getmSKU_ID9() != null && !comboData.getmSKU_ID9().isEmpty()) {
                a9Id = comboData.getmSKU_ID9();
            } else if (faceItem.getSpecsId() != null && !faceItem.getSpecsId().isEmpty()) {
                a9Id = faceItem.getSpecsId();
            }
        }
        if (!comboData.isA101Removed()) {
            if (comboData.getA101() != null && comboData.getA101().getObjId() != null && !comboData.getA101().getObjId().isEmpty()) {
                a10Id = comboData.getA101().getObjId();
            } else if (comboData.getmSKU_ID10() != null && !comboData.getmSKU_ID10().isEmpty()) {
                a10Id = comboData.getmSKU_ID10();
            }
        }
        SparseArray<String> sparseArray = new SparseArray<>();
        sparseArray.put(6, a6Id);
        sparseArray.put(7, a7Id);
        sparseArray.put(8, a8Id);
        sparseArray.put(9, a9Id);
        sparseArray.put(10, a10Id);
        return sparseArray;
    }

    protected void updateViewAccessoryToServer(BaseAccessoryItem item) {
        final String uri = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_UPDATE_MIXMATCH_SLECTION + User.getInstance().getmUserId() + "/" + item.getObjId();
        DataManager.getInstance().updateMethodToServer(uri, ConstantsUtil.EUpdateType.eUpdateTypeViewAccessory.toString(), new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {

            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });
    }


    private void updateShareToServer(ComboData comboData, String shareType) {
        SparseArray<String> sparseArray = getArrayMixMatchSku(comboData);

        final String uri = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_UPDATE_SHARE + User.getInstance().getmUserId() + "/" + currentComboData.getCombo_ID()
                + "/" + sparseArray.get(6) + "/" + sparseArray.get(7) + "/" + sparseArray.get(8) + "/" + sparseArray.get(9) + "/" + sparseArray.get(10) + "/"
                + shareMedium + "/" + shareType;

        //ConstantsUtil.EShareType.eShareTypePicture.toString()
        DataManager.getInstance().updateMethodToServer(uri, ConstantsUtil.EUpdateType.eUpdateTypeShare.toString(), new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {

            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });
    }

    private String getVideoUrl(ComboData comboData) {
        SparseArray<String> sparseArray = getArrayMixMatchSku(comboData);
        User user = User.getInstance();
        String uri = ConstantsUtil.URL_BASEPATH_VIDEO + ConstantsUtil.URL_METHOD_CREATE_VIDEO + user.getmUserId()
                + "/" + user.getDefaultFaceId()
                + "/" + user.getPBId()
                + "/" + currentComboData.getCombo_ID()
                + "/" + sparseArray.get(6) + "/" + sparseArray.get(7) + "/" + sparseArray.get(8) + "/" + sparseArray.get(9) + "/" + sparseArray.get(10);

        //uri = "http://style360-prod.ap-southeast-1.elasticbeanstalk.com/Service1.svc/CreateVideo/789/2/FB019/FC1/ER020/FSH014/FHS001/FSG002/FBG021";
        return uri;
    }

    private void showPopupWindow1() {
        mGLView.requestRender();
        isTakeScreenShot = true;
        pbGLView.setVisibility(View.VISIBLE);
        pbGLView.setLoadingText(getString(R.string.message_loading_text_pics_share));
    }


    private void showPopupWindow() {
        if (popupWindowShare != null && popupWindowShare.isShowing()) {
            popupWindowShare.dismiss();
            return;
        }

        if (popupWindowShare == null) {
            LayoutInflater layoutInflater
                    = (LayoutInflater) getBaseContext()
                    .getSystemService(LAYOUT_INFLATER_SERVICE);
            popupView = layoutInflater.inflate(R.layout.view_popup_share, null);
            popupWindowShare = new PopupWindow(
                    popupView,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        ImageButton btnSharePicsPopup = (ImageButton) popupView.findViewById(R.id.btn_share_pics_popup);
        btnSharePicsPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePopupShare(500);
                mGLView.requestRender();
                isTakeScreenShot = true;
                pbGLView.setVisibility(View.VISIBLE);
                pbGLView.setLoadingText(getString(R.string.message_loading_text_pics_share));
                trackEvent("Share Pic", currentComboData.getCombo_ID(), "");
            }
        });
        ImageButton btnShareVideoPopup = (ImageButton) popupView.findViewById(R.id.btn_share_videos_popup);
        btnShareVideoPopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!videoCreationInProgress) {
                    videoCreationInProgress = true;
                    trackEvent("Share 360", currentComboData.getCombo_ID(), "");
                    // String videoUrl = getVideoUrl(currentComboData);
                    //   startVideoDownloadService(currentComboData, videoUrl);
                    Log.d("V360", "I am starting");
                    DisplayMetrics metrics1 = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(metrics1);

                    int hPx = metrics1.heightPixels;
                    int wPx = metrics1.widthPixels;
                    videoScreen.saveVideoStart(1080, 1620);
                    // btnShopZoom.setImageResource(R.drawable.btn_rot_stop);
                    myRenderer.createVideo();
                    removePopupShare(500);
                    setButtonsClick(false);
                    pbGLView.setVisibility(View.VISIBLE);
                    pbGLView.setLoadingText(getString(R.string.message_loading_text_video_share_creation));
                } else {
                    Toast.makeText(getApplicationContext(), "Video processing is already in progress. Please wait.", Toast.LENGTH_SHORT).show();
                }
            }
        });
        //popupWindowShare.showAsDropDown(ivBtnShare, -100, -30);
        popupWindowShare.showAsDropDown(ivBtnShare, 0, -20);
        removePopupShare(ConstantsFunctional.TIME_WAIT_SHARE_POPUP_DISAPPEAR_IN_MILLI);
    }

    private void startVideoDownloadService(ComboData comboData, String videoUrl) {
        Intent intent = new Intent(ShopActivity.this, DownloadIntentService.class);
        intent.setAction(DownloadIntentService.ACTION_DOWNLOAD_VIDEO);
        intent.putExtra(DownloadIntentService.EXTRA_PARAM_VIDEO_URL, videoUrl);
        intent.putExtra(DownloadIntentService.EXTRA_PARAM_COMBO_ID, comboData.getCombo_ID());
        //intent.putExtra(DownloadIntentService.EXTRA_PARAM_VIDEO_URL,videoUrl);
        //startActivity(intent);
        startService(intent);
    }

    public void removePopupShare(int milliSecond) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (popupWindowShare != null && popupWindowShare.isShowing())
                    popupWindowShare.dismiss();
            }
        }, milliSecond);
    }

    /*
    public void resizePic(String filePath, int width, int height) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, width, height, false);
        File file = new File(filePath);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            scaled.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    */

    public void sharePics(String fileName) {
        updateShareToServer(currentComboData, ConstantsUtil.EShareType.eShareTypePicture.toString());
        final String filePath = ConstantsUtil.FILE_PATH_APP_ROOT + ConstantsUtil.FILE_PATH_SHARE + fileName;
        Log.w(LOGTAG, "sharePics filepath: " + filePath);
        shareResizedPic(filePath);
    }


    public void shareResizedPic(String filePath) {
        String shareString = "http://stylemylooks.com";
        Uri imageUri = Uri.fromFile(new File(filePath));
        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("image/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //sendIntent.setType("text/plain");

        Intent instagram = new Intent(Intent.ACTION_SEND);
        instagram.setPackage("com.whatsapp");
        //instagram.setPackage("c");
        instagram.setType("image/*");
        instagram.putExtra(Intent.EXTRA_STREAM, imageUri);
        instagram.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        instagram.putExtra(Intent.EXTRA_TEXT, shareString);
        //instagram.putExtra(Intent.EXTRA_TITLE, "YOUR TEXT HERE");

        Intent openInChooser = Intent.createChooser(instagram, "Share using");
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;


            Log.d(LOGTAG, "********************" + packageName);
            if (packageName.contains("com.instagram.android") || (packageName.contains("twitter") && ri.activityInfo.name.equals("com.twitter.android.composer.ComposerActivity")) || packageName.contains("facebook")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_STREAM, imageUri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.putExtra(Intent.EXTRA_TEXT, shareString);

                if (packageName.contains("twitter") && ri.activityInfo.name.equals("com.twitter.android")) {
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else if (packageName.contains("facebook")) {
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else if (packageName.contains("com.instagram.android")) {

                } else if (packageName.contains("com.whatsapp")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                }

                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        //convert intentList to array
        if (intentList.size() > 0) {
            if (openInChooser == null) {
                openInChooser = Intent.createChooser(intentList.get(0), "Share using");
                //intentList.remove(0);
            }
            LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        }
        if (openInChooser != null)
            startActivity(openInChooser);
    }

/*
    public void onShare() {
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        Uri screenshotUri = Uri.parse("android.resource://comexample.sairamkrishna.myapplication/*");

        try {
            InputStream stream = getContentResolver().openInputStream(screenshotUri);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        sharingIntent.setType("image/jpeg");
        sharingIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        startActivity(Intent.createChooser(sharingIntent, "Share image using"));
    }
    */


    public void shareVideo(Uri uri, String comboId) {
        updateShareToServer(currentComboData, ConstantsUtil.EShareType.eShareTypeVideo.toString());
        String shareString = "http://stylemylooks.com";
//        String filePath = ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO + fileName;
//        Uri uri = Uri.fromFile(new File(filePath));
        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("video/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        //sendIntent.putExtra(Intent.EXTRA_TEXT, shareString);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://sdcard/dcim/Camera/filename.3gp"));

        Intent whatsApp = new Intent(Intent.ACTION_SEND);
        whatsApp.setAction(Intent.ACTION_SEND);
        //whatsApp.setPackage("com.whatsapp");
        whatsApp.setPackage("com.facebook.katana");
        whatsApp.setType("video/*");
        whatsApp.putExtra(Intent.EXTRA_STREAM, uri);
        //whatsApp.putExtra(Intent.EXTRA_TEXT, shareString);
        whatsApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //whatsApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Intent openInChooser = Intent.createChooser(whatsApp, "Share using");
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            Log.d(LOGTAG, "********************" + packageName);
            //if (packageName.contains("com.whatsapp") || packageName.contains("com.facebook.orca") || packageName.contains("com.facebook.katana") || packageName.contains("com.instagram.android")) {
            if (packageName.contains("com.facebook.orca") || packageName.contains("com.instagram.android")) {
                //if (packageName.contains("com.instagram.android")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                //intent.setType("video/3gp");
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //intent.putExtra(Intent.EXTRA_TEXT, shareString);

                if (packageName.contains("twitter") && ri.activityInfo.name.equals("com.twitter.android")) {
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else if (packageName.contains("facebook")) {
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else if (packageName.contains("com.instagram.android")) {

                } else if (packageName.contains("com.whatsapp")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        if (intentList.size() > 0) {
            if (openInChooser == null) {
                openInChooser = Intent.createChooser(intentList.get(0), "Share using");
            }
            LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        }
        if (openInChooser != null)
            startActivity(openInChooser);
    }

    public void shareVideo(String fileName, String comboId) {
        updateShareToServer(currentComboData, ConstantsUtil.EShareType.eShareTypeVideo.toString());
        String shareString = "http://stylemylooks.com";
        String filePath = ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO + fileName;
        Uri uri = Uri.fromFile(new File(filePath));
        PackageManager pm = getPackageManager();
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.setType("video/*");
        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
        //sendIntent.putExtra(Intent.EXTRA_TEXT, shareString);
        sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //sendIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //sendIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://sdcard/dcim/Camera/filename.3gp"));

        Intent whatsApp = new Intent(Intent.ACTION_SEND);
        whatsApp.setAction(Intent.ACTION_SEND);
        whatsApp.setPackage("com.whatsapp");
        //whatsApp.setPackage("com.facebook.katana");
        whatsApp.setType("video/*");
        whatsApp.putExtra(Intent.EXTRA_STREAM, uri);
        //whatsApp.putExtra(Intent.EXTRA_TEXT, shareString);
        whatsApp.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //whatsApp.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        Intent openInChooser = Intent.createChooser(whatsApp, "Share using");
        List<ResolveInfo> resInfo = pm.queryIntentActivities(sendIntent, 0);
        List<LabeledIntent> intentList = new ArrayList<LabeledIntent>();

        for (int i = 0; i < resInfo.size(); i++) {
            ResolveInfo ri = resInfo.get(i);
            String packageName = ri.activityInfo.packageName;
            Log.d(LOGTAG, "********************" + packageName);
            if (packageName.contains("com.whatsapp") || packageName.contains("com.facebook.orca") || packageName.contains("com.facebook.katana") || packageName.contains("com.instagram.android")) {
                //if (packageName.contains("com.instagram.android")) {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName(packageName, ri.activityInfo.name));
                intent.setAction(Intent.ACTION_SEND);
                //intent.setType("video/3gp");
                intent.setType("video/*");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                //intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                //intent.putExtra(Intent.EXTRA_TEXT, shareString);

                if (packageName.contains("twitter") && ri.activityInfo.name.equals("com.twitter.android")) {
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else if (packageName.contains("facebook")) {
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                } else if (packageName.contains("com.instagram.android")) {

                } else if (packageName.contains("com.whatsapp")) { // If Gmail shows up twice, try removing this else-if clause and the reference to "android.gm" above
                    //intent.putExtra(Intent.EXTRA_TEXT, shareString);
                }
                intentList.add(new LabeledIntent(intent, packageName, ri.loadLabel(pm), ri.icon));
            }
        }

        if (intentList.size() > 0) {
            if (openInChooser == null) {
                openInChooser = Intent.createChooser(intentList.get(0), "Share using");
            }
            LabeledIntent[] extraIntents = intentList.toArray(new LabeledIntent[intentList.size()]);
            openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
        }
        if (openInChooser != null)
            startActivity(openInChooser);
    }

//    protected void showCreateFaceAlert(String title, String msg) {
//        final android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
//        builder.setTitle(title)
//                .setMessage(getString(R.string.message_alert_create_another_face))
//                .setCancelable(false)
//                .setIcon(R.drawable.logo_full_105)
//                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                        finish();
//                    }
//                })
//                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Intent i1 = new Intent(ShopActivity.this, FaceSelectionActivity.class);
//                        startActivity(i1);
//                        finish();
//                    }
//                }).create();
//        if (!isFinishing())
//            builder.show();
//    }

    private class RatingBarDialog {
        public AlertDialog alertDialog;
        private RatingBar ratingBar;
        private TextView tvInfo;
        private Button btnOk;
        private LinearLayout conBtnOK;

        public RatingBarDialog() {
            createRatingBarDialog();
        }

        public void dismissDialog() {
            InkarneAppContext.saveSettingIsDefaultFaceChanged(false);
            InkarneAppContext.setIsDefaultFaceChanged(false);
            alertDialog.dismiss();
        }

//        public void showDialog() {
//            if (alertDialog != null) {
//                alertDialog.setCancelable(false);
//                alertDialog.show();
//            }
//        }

        private void createRatingBarDialog() {
            //AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ShopActivity.this, R.style.AppCompatAlertTranslucentDialogStyle);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ShopActivity.this, R.style.AppCompatAlertDialogStyle);
            LayoutInflater inflater = ShopActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_rate_avatar, null);
            dialogBuilder.setView(dialogView);
            alertDialog = dialogBuilder.create();

            conBtnOK = (LinearLayout) dialogView.findViewById(R.id.con_dailog_avatar_rating_info);
            conBtnOK.setVisibility(View.GONE);
            tvInfo = (TextView) dialogView.findViewById(R.id.tv_dialog_shop_avatar_info);
            tvInfo.setVisibility(View.GONE);
            btnOk = (Button) dialogView.findViewById(R.id.btn_dialog_shop_avatar_ok);
            btnOk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissDialog();
                }
            });
            ratingBar = (RatingBar) dialogView.findViewById(R.id.ratingBar_shop_avatar);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
                public void onRatingChanged(RatingBar ratingBar, float rating,
                                            boolean fromUser) {
                    tvInfo.setVisibility(View.VISIBLE);
                    if (rating > 3) {
                        tvInfo.setText("Thanks");
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dismissDialog();
                            }
                        }, 800);
                    } else {
                        conBtnOK.setVisibility(View.VISIBLE);
                        tvInfo.setText("You can always create new AVATAR using redoAvatar functionality.\nThanks");
                    }
                }
            });
        }
    }

    public void finishVideo() {
        Log.d("V360", "I am here");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                videoScreen.saveVideoFinish();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        showVideoAlert();
                        setButtonsClick(true);
                    }
                }, 2000);
            }
        });

    }

    public void showVideoAlert() {
        Log.d(LOGTAG, " 360 showVideoAlert");
        Uri uri = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            //File f = new File("file://"+ Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
            File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO + ConstantsUtil.VIDEO_SHARE_FILENAME);
            Log.w(LOGTAG, "greater than kitkat" + file.getAbsolutePath());
            uri = Uri.fromFile(file);
            Log.w(LOGTAG, "360 Uri: " + uri);
            mediaScanIntent.setData(uri);
            sendBroadcast(mediaScanIntent);
        } else {
            File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO + ConstantsUtil.VIDEO_SHARE_FILENAME);
            Log.w(LOGTAG, file.getAbsolutePath());
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        }
        final Uri contentUri = uri;
        videoCreationInProgress = false;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (pbGLView.getLoadingText().equals(getString(R.string.message_loading_text_video_share_creation))) {
                    pbGLView.setVisibility(View.INVISIBLE);
                }
                alertVideoDialog = new VideoDialog(ConstantsUtil.VIDEO_SHARE_FILENAME, currentComboData.getCombo_ID(), contentUri);
                alertVideoDialog.showVideoDialog();
                Log.d(LOGTAG, "360 I am done");
                btnShopZoom.setImageResource(R.drawable.btn_shop_zoom_animation);
            }
        });
    }

    public void cancelVideo() {
        setButtonsClick(true);
//        videoScreen.stopVideo();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//
//                videoCreationInProgress = false;
//                if (pbGLView.getLoadingText().equals(getString(R.string.message_loading_text_video_share_creation))) {
//                    pbGLView.setVisibility(View.INVISIBLE);
//                }
//            }
//        });
//        btnShopZoom.setImageResource(R.drawable.btn_shop_zoom_animation);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            videoCreationInProgress = false;
            int errorCode = intent.getIntExtra(DataManager.ERROR_CODE_KEY, 300);
            String comboId = intent.getStringExtra(Video360Activity.INTENT_KEY_COMBO_ID);
            String videoKey = intent.getStringExtra(Video360Activity.INTENT_KEY_VIDEO_PATH);
            Uri videoUri = intent.getParcelableExtra(Video360Activity.INTENT_KEY_VIDEO_URI);

            if (pbGLView.getLoadingText().equals(getString(R.string.message_loading_text_video_share_creation))) {
                pbGLView.setVisibility(View.INVISIBLE);
            }
            if (errorCode == 0) {
                alertVideoDialog = new VideoDialog(videoKey, comboId, videoUri);
                alertVideoDialog.showVideoDialog();
            } else {
                pbGLView.setVisibility(View.INVISIBLE);
                Toast.makeText(ShopActivity.this, "360 look could not be created. " + ConstantsUtil.MESSAGE_TOAST_NETWORK_RESPONSE_FAILED, Toast.LENGTH_LONG).show();
            }
        }
    };

    private class VideoDialog {
        public AlertDialog alertVideoDialog;
        private TextView tvInfo;
        private ImageButton btnPlay;
        private ImageButton btnDelete;
        private ImageButton btnShare;
        private Button btnCancel;
        private String videoKey;
        private Uri videoUri;
        private String comboId;
        private LinearLayout conBtnOK;

        public VideoDialog() {
            createVideoDialog();
        }

        public VideoDialog(String videoKey, String comboId, Uri videoUri) {
            this.videoKey = videoKey;
            this.comboId = comboId;
            this.videoUri = videoUri;
            createVideoDialog();
        }

        public void dismissVideoDialog() {

            alertVideoDialog.dismiss();
        }

        public void showVideoDialog() {
            if (alertVideoDialog == null) {
                createVideoDialog();
            }
            alertVideoDialog.setCancelable(false);
            if(!isFinishing())
            alertVideoDialog.show();
        }

        private void createVideoDialog() {
            // AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ShopActivity.this, R.style.AppCompatAlertTranslucentDialogStyle);
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(ShopActivity.this, R.style.AppCompatAlertDialogStyle);
            LayoutInflater inflater = ShopActivity.this.getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_360_look_created, null);
            dialogBuilder.setView(dialogView);
            alertVideoDialog = dialogBuilder.create();
//            conBtnOK = (LinearLayout) dialogView.findViewById(R.id.con_dailog_avatar_rating_info);
//            conBtnOK.setVisibility(View.GONE);
            btnPlay = (ImageButton) dialogView.findViewById(R.id.btnVideoPlay);
            btnPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissVideoDialog();
                    Intent intent = new Intent(ShopActivity.this, Video360Activity.class);
                    intent.putExtra("comboId", comboId);
                    intent.putExtra("videoKey", videoKey);
                    startActivityForResult(intent, RESULT_CODE_VIDEO_360_ACTIVITY);
                }
            });
            btnShare = (ImageButton) dialogView.findViewById(R.id.btnVideoShare);
            btnShare.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissVideoDialog();
                    // shareVideo(videoKey, comboId);
                    shareVideo(videoUri, comboId);
                }
            });
            btnDelete = (ImageButton) dialogView.findViewById(R.id.btnVideoDelete);
            btnDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissVideoDialog();
                    //File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO+videoKey);
                    deleteFile(ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO + videoKey);
                    //Uri contentUri = Uri.fromFile(file);
                    //getContentResolver().delete(videoUri, null, null);
                    //file.delete();
                }
            });
            btnCancel = (Button) dialogView.findViewById(R.id.btnVideoCancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismissVideoDialog();
                    //File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO + videoKey);
                    //file.delete();
                }
            });
        }
    }

    public void updateToServerDeleteVideo(String videoId) {
        //URL_METHOD_DELETE_VIDEO
        String url = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_DELETE_VIDEO + User.getInstance().getmUserId()
                + "/" + videoId;
        DataManager.getInstance().updateMethodToServer(url, "deleteVideo", new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {

            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });
    }

    public boolean deleteFile(String file_dj_path) {
//        String file_dj_path = Environment.getExternalStorageDirectory() + "/ECP_Screenshots/abc.jpg";
        File fdelete = new File(file_dj_path);
//        File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT_VIDEO+videoKey);
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                Log.e("-->", "file Deleted :" + file_dj_path);
                callBroadCast();
                return true;
            } else {
                Log.e("-->", "file not Deleted :" + file_dj_path);
            }
        }
        return false;
    }

    public void callBroadCast() {
        if (Build.VERSION.SDK_INT >= 14) {
            Log.e("-->", " >= 14");
            MediaScannerConnection.scanFile(this, new String[]{Environment.getExternalStorageDirectory().toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                /*
                 *   (non-Javadoc)
                 * @see android.media.MediaScannerConnection.OnScanCompletedListener#onScanCompleted(java.lang.String, android.net.Uri)
                 */
                public void onScanCompleted(String path, Uri uri) {
                    Log.e("ExternalStorage", "Scanned " + path + ":");
                    Log.e("ExternalStorage", "-> uri=" + uri);
                }
            });
        } else {
            Log.e("-->", " < 14");
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,
                    Uri.parse("file://" + Environment.getExternalStorageDirectory())));
        }
    }

    /* collection frgement data*/


    private class LoadLooksListTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            pbLooksInfo.setVisibility(View.VISIBLE);
//            pbLooksInfo.setLoadingText(getActivity().getString(R.string.message_loading_looks));
//            if (pBInitData == null)
//                pBInitData = new ProgressDialog(getActivity());
//            pBInitData.show();
//            //TODO some data issue handling -- to be modified
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    if (pBInitData != null && pBInitData.isShowing())
//                        pBInitData.dismiss();
//
//                    pbLooksInfo.setVisibility(View.INVISIBLE);
//                }
//            }, 1000);
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (listOfComboList == null || listOfComboList.size() < ConstantsUtil.arrayListLooksLabelName.size())
                initData();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... params) {


        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
//            if (pBInitData != null)
//                pBInitData.dismiss();
//            pbLooksInfo.setVisibility(View.INVISIBLE);
//            collectionAdapter = new CollectionAdapter(getActivity(), listOfComboList, (CollectionAdapter.OnGridAdapterListener) CollectionFragment.this);
//            gridCollectionView.setAdapter(collectionAdapter);

        }
    }

    private void initData() {
        ArrayList<ComboData> comboReconcileData = (ArrayList<ComboData>) dataSource.getComboReconcileData(InkarneAppContext.shouldRearrangeLooks);
        filterCombo(comboReconcileData);
        InkarneAppContext.shouldRearrangeLooks = false;
    }

    public ArrayList<ComboDataLooksItem> getSkeletonListOfComboList() {
        ArrayList<ComboDataLooksItem> list = new ArrayList<>();
        for (int i = 0; i < ConstantsUtil.arrayListLooksLabelName.size(); i++) {
            ComboDataLooksItem looksListItem = new ComboDataLooksItem(ConstantsUtil.arrayListLooksLabelName.get(i), ConstantsUtil.arrayListLooksCategory.get(i));
            list.add(looksListItem);
        }
        return list;
    }

    public void addToLooksList(ComboData c, String looksCategory) {
        int index = ConstantsUtil.arrayListLooksCategory.indexOf(looksCategory);
        ComboDataLooksItem looksItem = listOfComboList.get(index);
        looksItem.getComboList().add(c);
    }

    public void addToTrendingLooksList() {
        int index = ConstantsUtil.arrayListLooksCategory.indexOf("Vogue_Flag");
        ComboDataLooksItem looksItem = listOfComboList.get(index);
        ArrayList<ComboData> trending = (ArrayList<ComboData>) dataSource.getComboReconcileTrendingData(InkarneAppContext.shouldRearrangeLooks);
        //looksItem.getComboList().addAll(trending);
        comboIndex = getComboIndex(trending, currentComboData);
        comboList = trending;
        Log.e(LOGTAG, "current comboIndex :" + comboIndex);
        //updateChangeButton();
        looksItem.setComboList(trending);
    }

    private int getComboIndex(ArrayList<ComboData> trending, ComboData comboData) {
        int index = 0;
        if (comboData == null)
            return 0;
        if (trending == null)
            return -1;
        Log.e(LOGTAG, "Trending.. id: " + comboData.getCombo_ID());
        for (ComboData c : trending) {
            Log.w(LOGTAG, "c id: " + c.getCombo_ID());
            if (c.getCombo_ID().equals(comboData.getCombo_ID())) {
                return index;
            }
            index++;
        }
        return index;
    }

    public void addToLooksListForYou(ComboData c, String looksCategory) {
        int index = ConstantsUtil.arrayListLooksCategory.indexOf(looksCategory);
        ComboDataLooksItem looksItem = listOfComboList.get(index);
        int count = looksItem.getComboList().size() > 20 ? 20 : looksItem.getComboList().size();
        boolean isInserted = false;
        for (int i = 0; i < count; i++) {
            ComboData comboData = looksItem.getComboList().get(i);
            if (c.getStyle_Rating() > comboData.getStyle_Rating()) {
                looksItem.getComboList().add(i, c);
                isInserted = true;
                break;
            }
        }
        if (isInserted && count >= 20) {
            looksItem.getComboList().remove(looksItem.getComboList().size() - 1);
        }
        if (!isInserted && count < 20) {
            looksItem.getComboList().add(c);
        }
    }


    public void filterCombo(ArrayList<ComboData> comboList) {
        listOfComboList = getSkeletonListOfComboList();
        for (ComboData c : comboList) {
            if (c.getCombo_ID() == null || c.getCombo_ID().isEmpty() || c.getCombo_ID().equals("null")) //TODO
                continue;
            addToLooksListForYou(c, "StyleRating");

            //Log.w(LOGTAG, "Cmbo ID: " + c.getCombo_ID());
            if (c.getVogue_Flag() == null) {
                //Log.d(LOGTAG, "Cmbo ID 1: " + c.getCombo_ID());
                continue;
            }
            Log.e(LOGTAG, "Cmbo ID 2: " + c.getCombo_ID());
            if (c.getVogue_Flag().equals("True")) {
                //addToLooksList(c, "Vogue_Flag"); //8jul
            } else if (c.getPush_Flag().equals("True")) {
                //addToLooksList(c, "Push_Flag");

            }
            if (c.getIsLiked() == 1) {
                addToLooksList(c, "isLiked");
            }

            for (String style : ConstantsUtil.arrayListCategoryStyle) {
                if (c.getCombo_Style_Category().equals(style)) {
                    addToLooksList(c, style);
                }
            }
            if (c.getViewCount() > 0) {
                addToLooksList(c, DatabaseHandler.DATE_SEEN_IN_MILLI);
            }
        }
        addToTrendingLooksList();
        reArrangeLikes();
        reArrangeHistory();

//        int index =  ConstantsUtil.arrayListLooksCategory.indexOf("isLiked");
//        ComboDataLooksItem item = listOfComboList.get(index);
//        if(item.getComboList().size()==0){
//            listOfComboList.remove(index);
//        }
    }

    private boolean isListContain(ArrayList<ComboData> list, ComboData comboData) {
        if (list == null || comboData == null)
            return false;
        for (ComboData c : list) {
            if (c.getCombo_ID().equals(comboData.getCombo_ID())) {
                return true;
            }
        }
        return false;
    }

    private void updateCollectionList() {
        int index = ConstantsUtil.arrayListLooksCategory.indexOf("isLiked");
        ComboDataLooksItem looksItem = listOfComboList.get(index);

        ArrayList<ComboData> tempList = looksItem.getComboList();
        tempList.clear();
        //looksItem.setComboList(listComboLikes);
        for (ComboData c : listComboLikes) {
            tempList.add(c);
        }

        int indexH = ConstantsUtil.arrayListLooksCategory.indexOf(DatabaseHandler.DATE_SEEN_IN_MILLI);
        ComboDataLooksItem historyItem = listOfComboList.get(indexH);

        ArrayList<ComboData> tempList2 = historyItem.getComboList();
        tempList2.clear();
        // historyItem.setComboList(listComboHistory);
        for (ComboData c : listComboHistory) {
            tempList2.add(c);
        }
    }

    private void updateHistoryAndLikes() {
        if (listOfComboList == null || listOfComboList.size() < ConstantsUtil.arrayListLooksLabelName.size())
            return;
        int index = ConstantsUtil.arrayListLooksCategory.indexOf("isLiked");
        ComboDataLooksItem looksItem = listOfComboList.get(index);
        ArrayList<ComboData> tempList = looksItem.getComboList();

        if (listComboLikes == null)
            listComboLikes = new ArrayList<>();
        listComboLikes.clear();
        for (ComboData c : tempList) {
            listComboLikes.add(c);
        }

        //listComboLikes.addAll(looksItem.getComboList());

        int indexH = ConstantsUtil.arrayListLooksCategory.indexOf(DatabaseHandler.DATE_SEEN_IN_MILLI);
        ComboDataLooksItem historyItem = listOfComboList.get(indexH);
        ArrayList<ComboData> tempList2 = historyItem.getComboList();

        if (listComboHistory == null)
            listComboHistory = new ArrayList<>();
        listComboHistory.clear();
        for (ComboData c : tempList2) {
            listComboHistory.add(c);
        }
        // listComboHistory.addAll( historyItem.getComboList());
    }


    private void addToLikes(ComboData comboData) {
        int isLiked = currentComboData.isLiked() == 0 ? 1 : 0;
        if (isLiked == 0) {
            listComboLikes.remove(comboData);
        } else {
            if (!isListContain(listComboLikes, comboData)) {
                listComboLikes.add(0, comboData);
            } else {
                listComboLikes.remove(comboData);
                listComboLikes.add(0, comboData);
            }
        }
    }

    private void addToHistory1(ComboData comboData) {
        int indexHistory = ConstantsUtil.arrayListLooksCategory.indexOf(DatabaseHandler.DATE_SEEN_IN_MILLI);
        ComboDataLooksItem historyItem = listOfComboList.get(indexHistory);
        ArrayList<ComboData> listHistory = historyItem.getComboList();
        if (!isListContain(listHistory, comboData)) {
            listHistory.add(0, comboData);
        } else {
            listHistory.remove(comboData);
            listHistory.add(0, comboData);
        }
        updateChangeButton();
        //historyItem.setComboList(listHistory);
        //reArrangeHistory();
    }

    private void addToHistory(ComboData comboData) {
        if (!isListContain(listComboHistory, comboData)) {
            listComboHistory.add(0, comboData);
        } else {
            listComboHistory.remove(comboData);
            listComboHistory.add(0, comboData);
        }
    }

    private void reArrangeLikes() {
        int index = ConstantsUtil.arrayListLooksCategory.indexOf("isLiked");
        ComboDataLooksItem looksItem = listOfComboList.get(index);
        ArrayList<ComboData> listLikes = looksItem.getComboList();
        for (ComboData co : listLikes) {
            Log.d(LOGTAG, co.getDateSeenInMilli() + "  :" + co.getCombo_ID());
        }

        Collections.sort(listLikes, Comparators.likesDESC);
        Log.w(LOGTAG, "**************** rearranged likes *********************");
        for (ComboData co : listLikes) {
            Log.d(LOGTAG, co.getDateSeenInMilli() + "  :" + co.getCombo_ID());
        }
        looksItem.setComboList(listLikes);

        if (listComboLikes == null)
            listComboLikes = new ArrayList<>();
        //listComboLikes.addAll(listLikes);
        listComboLikes.clear();
        for (ComboData c : listLikes) {
            listComboLikes.add(c);
        }
    }

    private void reArrangeHistory() {
        int indexHistory = ConstantsUtil.arrayListLooksCategory.indexOf(DatabaseHandler.DATE_SEEN_IN_MILLI);
        ComboDataLooksItem historyItem = listOfComboList.get(indexHistory);
        ArrayList<ComboData> listHistory = historyItem.getComboList();
        Log.w(LOGTAG, "**************** History *********************");
        for (ComboData co : listHistory) {
            Log.d(LOGTAG, co.getDateSeenInMilli() + "  :" + co.getCombo_ID());
        }
        Collections.sort(listHistory, Comparators.likesDESC);
        Log.w(LOGTAG, "**************** rearranged History *********************");
        for (ComboData co : listHistory) {
            Log.d(LOGTAG, co.getDateSeenInMilli() + "  :" + co.getCombo_ID());
        }
        historyItem.setComboList(listHistory);

        if (listComboHistory == null)
            listComboHistory = new ArrayList<>();
        //listComboLikes.addAll(listLikes);
        listComboHistory.clear();
        for (ComboData c : listHistory) {
            listComboHistory.add(c);
        }
    }

    public static class Comparators {
        public static Comparator<ComboDataReconcile> likesDESC = new Comparator<ComboDataReconcile>() {
            @Override
            public int compare(ComboDataReconcile o1, ComboDataReconcile o2) {
                //return o1.age - o2.age;
                return (int) o2.getDateSeenInMilli() - (int) o1.getDateSeenInMilli();
            }
        };
    }

}

