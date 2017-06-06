package com.svc.sml.Activity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.svc.sml.Database.User;
import com.svc.sml.InkarneAppContext;
import com.svc.sml.R;
import com.svc.sml.Utility.ConstantsUtil;

import java.io.File;

//import org.opencv.core.Mat;
//import org.opencv.imgcodecs.Imgcodecs;

public class FiducialActivity1 extends FaceUploadActivity {

    public static final String LOGTAG = "FiducialsActivity";
    protected ProgressDialog progressDialogFed;
    //Location of demopic & userpic
//    String foldernamed = "inkarne/pics/demopic";
//    File newFolderd = new File(Environment.getExternalStorageDirectory(), foldernamed);
//    String foldernameu = "inkarne/pics/userpic";
//    File newFolderu = new File(Environment.getExternalStorageDirectory(), foldernameu);

    //Load open CV lib
//    static {
//        System.loadLibrary("opencv_java3");
//    }

    //Mat umatrix;
    //Mat dmatrix;
    //Declare button, bitmaps and imageviews
    //public Button btnNextGetFaceItem;
    public boolean SuccessStatus = false;
    public BitmapFactory.Options options;
    public Bitmap Imagebmp;
    public Bitmap Imagebmpdemo;
    public ImageView ptable;
    public ImageView dtable;
    //public String User_ID; //Should be read from previous activity
    //public String Gender = "m"; //Should be read from previous activity

    //Processing variables
    public String[] dcolumns; // string values of feducials from webservice
    public String[] drows;
    public int uWIDTH; // physical user pic width in pixels
    public int uHEIGHT;
    public int dWIDTH; // physical demo pic width in pixels
    public int dHEIGHT;
    public float[] uwidth;
    public float[] uheight;
    public int[] uwidthn; // final int values of feducials for webservice
    public int[] uheightn;
    public float[] dwidth;
    public float[] dheight;

    public int urowm;
    public int ucolumnm;
    public float uratiow;
    public float uratioh;
    public int drowm;
    public int dcolumnm;
    public float dratiow;
    public float dratioh;
    int uwidth_physical_pixels;
    int uheight_physical_pixels;
    int uwidth_mat;
    int uheight_mat;
    int dwidth_physical_pixels;
    int dheight_physical_pixels;
    int dwidth_mat;
    int dheight_mat;
    public int c = 0;
    public int j;
    public int f;

    //Other variables
    public int FedNo;
    public int ClickState;
    //    public String uFilePathPic;
//    public String dFilePathPic;
    //public String resultString;
    //public String Fed1Path;
    public int selectedlayer;
    public String dFilePathPic;
    private String uFilePathPic;
    //btnFaceObjUploadHandler

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fiducial);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        btnNextGetFaceItem = (Button) findViewById(R.id.btn_shared_next);
        btnNextGetFaceItem.setText("Click to confirm face detection markers \n and move next \n");

        options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        //Delete bundle if not required
        //Note to HM - user id & gender will be required for calling webservices
        Bundle bundle = getIntent().getExtras();


        //resultString = "GetReferenceFidicualsResult";
        selectedlayer = -1;
        ClickState = 0;
        FedNo = 11;

        ptable = (ImageView) findViewById(R.id.userpic);
        dtable = (ImageView) findViewById(R.id.demopic);
        drows = new String[11];
        dcolumns = new String[11];

        uwidth = new float[12];
        uheight = new float[12];
        uwidthn = new int[12];
        uheightn = new int[12];
        dwidth = new float[12];
        dheight = new float[12];

        beginDownload();

        //uFilePathPic = newFolderu + "/" + "upic.jpg";
        //dFilePathPic = newFolderu + "/" + "upic.jpg";

        //initData();
        //dFilePathPic = newFolderd+"/"+"dpic.jpg";
        //Note to HM - pls download demopic in prior activity and save in sd card as inkarne/pics/demopic/dpic.jpg and uncomment below code, delete above


//        umatrix = Imgcodecs.imread(uFilePathPic);
//        uwidth_mat = umatrix.cols();
//        uheight_mat = umatrix.rows();
//
//        dmatrix = Imgcodecs.imread(dFilePathPic);
//        dwidth_mat = dmatrix.cols();
//        dheight_mat = dmatrix.rows();
//
//        //Below needs to be modified to accomodate other devices
//        options.inSampleSize = 2;
//        uwidth_physical_pixels = uwidth_mat;
//        uheight_physical_pixels = uheight_mat;
//        dwidth_physical_pixels = dwidth_mat / 3;
//        dheight_physical_pixels = dheight_mat / 3;
//
//        uBitmap = BitmapFactory.decodeFile(uFilePathPic, options);
//        dBitmap = BitmapFactory.decodeFile(dFilePathPic, options);
//        RelativeLayout.LayoutParams layoutParamsu = new RelativeLayout.LayoutParams(uwidth_physical_pixels, uheight_physical_pixels);
//        layoutParamsu.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
//        RelativeLayout.LayoutParams layoutParamsd = new RelativeLayout.LayoutParams(dwidth_physical_pixels, dheight_physical_pixels);
//        layoutParamsd.addRule(RelativeLayout.ALIGN_RIGHT, utable.getId());
//        utable.setLayoutParams(layoutParamsu);
//        dtable.setLayoutParams(layoutParamsd);
//
//        String text1 = "Set Image HEIGHT:" + String.valueOf(uheight_physical_pixels) + " WIDTH:" + String.valueOf(uwidth_physical_pixels);
//        Log.d("Check point ImageView", text1);
//
//        uHEIGHT = uheight_physical_pixels;
//        uWIDTH = uwidth_physical_pixels;
//        ucolumnm = umatrix.cols();
//        urowm = umatrix.rows();
//        uratioh = (float) uHEIGHT / (float) urowm;
//        uratiow = (float) uWIDTH / (float) ucolumnm;
//
//        dHEIGHT = dheight_physical_pixels;
//        dWIDTH = dwidth_physical_pixels;
//        dcolumnm = dmatrix.cols();
//        drowm = dmatrix.rows();
//        dratioh = (float) dHEIGHT / (float) drowm;
//        dratiow = (float) dWIDTH / (float) dcolumnm;


        //Read fiducials from webservice
        //getfeds();
        requestDataFedPoints();

//        //Lets read demo image fiducial points and create a canvas layer
//        Bitmap db = Bitmap.createBitmap(dWIDTH, dHEIGHT, Bitmap.Config.ARGB_8888);
//        Canvas dca = new Canvas(db);
//        for (j = 1; j < (FedNo + 1); j++) {
//            dheight[j] = Float.parseFloat(drows[j - 1]) * dratioh;
//            dwidth[j] = Float.parseFloat(dcolumns[j - 1]) * dratiow;
//            Paint dpa = new Paint();
//            dpa.setStyle(Paint.Style.STROKE);
//            dpa.setAntiAlias(true);
//            dpa.setStrokeWidth((float) 2);
//            dpa.setARGB(251, 57, 255, 10);
//            float cx = dwidth[j];
//            float cy = dheight[j];
//            dca.drawCircle(cx, cy, 3, dpa);
//            dca.drawCircle(cx, cy, 10, dpa);
//        }
//        BitmapDrawable dbmpl = new BitmapDrawable(getResources(), db);
//
//        //Create demo image with demo feducial layer
//        Drawable[] layersdemo = new Drawable[2];
//        BitmapDrawable d_bmp = new BitmapDrawable(getResources(), dBitmap);
//        layersdemo[0] = (Drawable) d_bmp;
//        layersdemo[1] = (Drawable) dbmpl;
//        LayerDrawable d_ldr = new LayerDrawable(layersdemo);
//        dtable.setImageDrawable(d_ldr);
//
//        //Lets create fiducials starting point for user picture
//        Bitmap ub = Bitmap.createBitmap(uWIDTH, uHEIGHT, Bitmap.Config.ARGB_8888);
//        Canvas uca = new Canvas(ub);
//        for (j = 1; j < (FedNo + 1); j++) {
//            uheight[j] = Float.parseFloat(drows[j - 1]) * uratioh; //TBD: normalisation code for points to be done
//            uwidth[j] = Float.parseFloat(dcolumns[j - 1]) * uratiow;
//            Paint upa = new Paint();
//            upa.setStyle(Paint.Style.STROKE);
//            upa.setAntiAlias(true);
//            upa.setStrokeWidth((float) 7);
//            upa.setARGB(251, 57, 255, 20);
//            float cx = uwidth[j];
//            float cy = uheight[j];
//            uca.drawCircle(cx, cy, 7, upa);
//            uca.drawCircle(cx, cy, 25, upa);
//        }
//        BitmapDrawable ubmpl = new BitmapDrawable(getResources(), ub);
//
//        //Create user image with adjusted feducial layer
//        Drawable[] layers = new Drawable[2];
//        BitmapDrawable ubmp = new BitmapDrawable(getResources(), uBitmap);
//        layers[0] = (Drawable) ubmp;
//        layers[1] = (Drawable) ubmpl;
//        LayerDrawable uldr = new LayerDrawable(layers);
//        utable.setImageDrawable(uldr);

        ptable.setOnTouchListener(new View.OnTouchListener() {
                                      @Override
                                      public boolean onTouch(View v, MotionEvent event) {

                                          //lets do some maths, objective is to calculate (x,y) which is coord at which zoomed image starts and (w,h) which is size of zoomed image
                                          int l = -1; // l means the point selected
                                          int k = 0;
                                          float ex = event.getX(); //touch coordinate on screen
                                          float ey = event.getY();

                                          int uh = Imagebmp.getHeight(); //physical dimens of user pic
                                          int uw = Imagebmp.getWidth();
                                          float urh = (float) uh / (float) uHEIGHT;
                                          float urw = (float) uw / (float) uWIDTH;
                                          float r = (float) uh / (float) uw;

                                          float cx1 = ex * urw;
                                          float cy1 = ey * urh;
                                          float hf = uh / 6;
                                          int h = (int) hf; // size of zoomed image determined with (w,h)
                                          float wf = h / r;
                                          int w = (int) wf;
                                          float y11 = h / 2;
                                          float x11 = w / 2;
                                          float zh = (float) uh / (float) h;
                                          float zw = (float) uw / (float) w;

                                          int x111 = Math.max(((int) cx1 - (int) x11), 0); //(x,y) tells where zoomed image has to be cropped from in the base image
                                          int x;
                                          if ((uw - cx1) < x11) {
                                              x = uw - (int) w - 3;
                                          } else {
                                              x = x111;
                                          }

                                          int y;
                                          int y111 = Math.max(((int) cy1 - (int) y11), 0);
                                          if ((uh - cy1) < y11) {
                                              y = uh - (int) h - 3;
                                          } else {
                                              y = y111;
                                          }
                                          float xadj = (float) x / urw;
                                          float yadj = (float) y / urh;

                                          //Determine the point to be moved
                                          if (event.getAction() == MotionEvent.ACTION_DOWN) {
                                              for (k = 0; k < FedNo; k++) {
                                                  float num1 = uheight[k + 1] - ey;
                                                  float num2 = uwidth[k + 1] - ex;
                                                  float abs_num1 = (num1 < 0) ? -num1 : num1;
                                                  float abs_num2 = (num2 < 0) ? -num2 : num2;
                                                  if (((abs_num1) < 60) && ((abs_num2) < 60)) {
                                                      l = k + 1;
                                                      selectedlayer = l; // this code lets you pick up the point touched
                                                  }
                                              }
                                              l = selectedlayer;
                                              if (!(l == -1)) {
                                                  //Lets redraw demo image with only selected point
                                                  Bitmap db = Bitmap.createBitmap(dWIDTH, dHEIGHT, Bitmap.Config.ARGB_8888);
                                                  Canvas dca = new Canvas(db);
                                                  for (j = 1; j < (FedNo + 1); j++) {
                                                      if ((j) == l) {
                                                          Paint dpa = new Paint();
                                                          dpa.setStyle(Paint.Style.STROKE);
                                                          dpa.setAntiAlias(true);
                                                          dpa.setStrokeWidth((float) 2);
                                                          dpa.setARGB(251, 57, 255, 10);
                                                          float cx = dwidth[j];
                                                          float cy = dheight[j];
                                                          dca.drawCircle(cx, cy, 3, dpa);
                                                          dca.drawCircle(cx, cy, 10, dpa);
                                                      } else {
                                                      }
                                                  }
                                                  BitmapDrawable dbmpl = new BitmapDrawable(getResources(), db);

                                                  Drawable[] layersdemo = new Drawable[2];
                                                  BitmapDrawable d_bmp = new BitmapDrawable(getResources(), Imagebmpdemo);
                                                  layersdemo[0] = (Drawable) d_bmp;
                                                  layersdemo[1] = (Drawable) dbmpl;
                                                  LayerDrawable d_ldr = new LayerDrawable(layersdemo);
                                                  dtable.setImageDrawable(d_ldr);
                                              }
                                          }

                                          l = selectedlayer;
                                          if (!(l == -1)) {
                                              //Lets redraw user image in zoom with only the selected point and allow him to move the point
                                              Bitmap ubc = Bitmap.createBitmap(uWIDTH, uHEIGHT, Bitmap.Config.ARGB_8888);
                                              Canvas uca = new Canvas(ubc);
                                              Paint upa = new Paint();
                                              upa.setStyle(Paint.Style.STROKE);
                                              upa.setAntiAlias(true);
                                              upa.setStrokeWidth((float) 20);
                                              upa.setARGB(251, 60, 34, 115);
                                              uca.drawCircle(ex, ey, 50, upa);
                                              uca.drawCircle(ex, ey, 100, upa);
                                              uca.drawCircle(ex, ey, 160, upa);
                                              uwidth[l] = ex;
                                              uheight[l] = ey;
                                              BitmapDrawable ubmpl = new BitmapDrawable(getResources(), ubc);

                                              Bitmap ub = Bitmap.createBitmap(Imagebmp, x, y, w, h);
                                              BitmapDrawable ubmp = new BitmapDrawable(getResources(), ub);
                                              Drawable[] layers = new Drawable[2];
                                              layers[0] = (Drawable) ubmp;
                                              layers[1] = (Drawable) ubmpl;
                                              LayerDrawable uldr = new LayerDrawable(layers);
                                              ptable.setImageDrawable(uldr);
                                          }

                                          if (event.getAction() == MotionEvent.ACTION_UP) {
                                              if (!(l == -1)) {
                                                  uwidth[l] = xadj + ex / zw;
                                                  uheight[l] = yadj + ey / zh;
                                              }
                                              selectedlayer = -1;

                                              //Lets redraw user image with new points
                                              Bitmap ub = Bitmap.createBitmap(uWIDTH, uHEIGHT, Bitmap.Config.ARGB_8888);
                                              Canvas uca = new Canvas(ub);
                                              Paint upa = new Paint();
                                              upa.setStyle(Paint.Style.STROKE);
                                              upa.setAntiAlias(true);
                                              upa.setStrokeWidth((float) 7);
                                              for (k = 0; k < FedNo; k++) {
                                                  upa.setARGB(251, 57, 255, 20);
                                                  uca.drawCircle(uwidth[k + 1], uheight[k + 1], 7, upa);
                                                  uca.drawCircle(uwidth[k + 1], uheight[k + 1], 25, upa);
                                              }
                                              BitmapDrawable u_bmp1 = new BitmapDrawable(getResources(), ub);

                                              Drawable[] layers = new Drawable[2];
                                              BitmapDrawable u_bmp = new BitmapDrawable(getResources(), Imagebmp);
                                              layers[0] = (Drawable) u_bmp;
                                              layers[1] = (Drawable) u_bmp1;
                                              LayerDrawable u_ldr = new LayerDrawable(layers);
                                              ptable.setImageDrawable(u_ldr);

                                              //Lets redraw demo image with all points
                                              Bitmap db = Bitmap.createBitmap(dWIDTH, dHEIGHT, Bitmap.Config.ARGB_8888);
                                              Canvas dca = new Canvas(db);
                                              for (j = 1; j < (FedNo + 1); j++) {

                                                  Paint dpa = new Paint();
                                                  dpa.setStyle(Paint.Style.STROKE);
                                                  dpa.setAntiAlias(true);
                                                  dpa.setStrokeWidth((float) 2);
                                                  dpa.setARGB(251, 57, 255, 10);
                                                  float cx = dwidth[j];
                                                  float cy = dheight[j];
                                                  dca.drawCircle(cx, cy, 3, dpa);
                                                  dca.drawCircle(cx, cy, 10, dpa);
                                              }
                                              BitmapDrawable dbmpl = new BitmapDrawable(getResources(), db);

                                              Drawable[] layersdemo = new Drawable[2];
                                              BitmapDrawable d_bmp = new BitmapDrawable(getResources(), Imagebmpdemo);
                                              layersdemo[0] = (Drawable) d_bmp;
                                              layersdemo[1] = (Drawable) dbmpl;
                                              LayerDrawable d_ldr = new LayerDrawable(layersdemo);
                                              dtable.setImageDrawable(d_ldr);

                                              return true;
                                          }
                                          return true;
                                      }
                                  }
        );


        //Button cick listener for posting feducials and calling next activity
        btnNextGetFaceItem.setOnClickListener(new View.OnClickListener() {
                                         @Override
                                         public void onClick(View v) {
                                             //createFiducialPoints();
                                             //getFaceItem(uFilePathPic);
                                         }
                                     }

        );
    }

    private void drawFeducialPoints() {
        //Lets read demo image fiducial points and create a canvas layer
        Bitmap db = Bitmap.createBitmap(dWIDTH, dHEIGHT, Bitmap.Config.ARGB_8888);
        Canvas dca = new Canvas(db);
        for (j = 1; j < (FedNo + 1); j++) {
            dheight[j] = Float.parseFloat(drows[j - 1]) * dratioh;
            dwidth[j]  = Float.parseFloat(dcolumns[j - 1]) * dratiow;
            Paint dpa = new Paint();
            dpa.setStyle(Paint.Style.STROKE);
            dpa.setAntiAlias(true);
            dpa.setStrokeWidth((float) 2);
            dpa.setARGB(251, 57, 255, 10);
            float cx = dwidth[j];
            float cy = dheight[j];
            dca.drawCircle(cx, cy, 3, dpa);
            dca.drawCircle(cx, cy, 10, dpa);
        }
        BitmapDrawable dbmpl = new BitmapDrawable(getResources(), db);

        //Create demo image with demo feducial layer
        Drawable[] layersdemo = new Drawable[2];
        BitmapDrawable d_bmp = new BitmapDrawable(getResources(), Imagebmpdemo);
        layersdemo[0] = (Drawable) d_bmp;
        layersdemo[1] = (Drawable) dbmpl;
        LayerDrawable d_ldr = new LayerDrawable(layersdemo);
        dtable.setImageDrawable(d_ldr);

        //Lets create fiducials starting point for user picture
        Bitmap ub = Bitmap.createBitmap(uWIDTH, uHEIGHT, Bitmap.Config.ARGB_8888);
        Canvas uca = new Canvas(ub);
        for (j = 1; j < (FedNo + 1); j++) {
            uheight[j] = Float.parseFloat(drows[j - 1]) * uratioh; //TBD: normalisation code for points to be done
            uwidth[j] = Float.parseFloat(dcolumns[j - 1]) * uratiow;
            Paint upa = new Paint();
            upa.setStyle(Paint.Style.STROKE);
            upa.setAntiAlias(true);
            upa.setStrokeWidth((float) 7);
            upa.setARGB(251, 57, 255, 20);
            float cx = uwidth[j];
            float cy = uheight[j];
            uca.drawCircle(cx, cy, 7, upa);
            uca.drawCircle(cx, cy, 25, upa);
        }
        BitmapDrawable ubmpl = new BitmapDrawable(getResources(), ub);

        //Create user image with adjusted feducial layer
        Drawable[] layers = new Drawable[2];
        BitmapDrawable ubmp = new BitmapDrawable(getResources(), Imagebmp);
        layers[0] = (Drawable) ubmp;
        layers[1] = (Drawable) ubmpl;
        LayerDrawable uldr = new LayerDrawable(layers);
        ptable.setImageDrawable(uldr);
        btnNextGetFaceItem.setEnabled(true);
    }

    private void createFiducialPoints() {
        if (SuccessStatus == true) {
            return; // Prevents multi clicks
        } else {
            //Send confirmed fidicuals to server for user pic
            //changes
            for (f = 1; f < 12; f++) {
                uwidth[f] = uwidth[f] / uratiow;
                uheight[f] = uheight[f] / uratioh;
                uwidthn[f] = (int) uwidth[f];
                uheightn[f] = (int) uheight[f];
            }

            Fed1Path = "";
            for (int i = 1; i < 12; i++) {
                Fed1Path += "/" + uwidthn[i] + "/";
                Fed1Path += uheightn[i];
            }
            //Fed1Path = "/" + uwidthn[1] + "/" + uheightn[1] + "/" + uwidthn[2] + "/" + uheightn[2] + "/" + uwidthn[3] + "/" + uheightn[3] + "/" + uwidthn[4] + "/" + uheightn[4] + "/" + uwidthn[5] + "/" + uheightn[5] + "/" + uwidthn[6] + "/" + uheightn[6] + "/" + uwidthn[7] + "/" + uheightn[7] +
            //      "/" + uwidthn[8] + "/" + uheightn[8] + "/" + uwidthn[9] + "/" + uheightn[9] + "/" + uwidthn[10] + "/" + uheightn[10] + "/" + uwidthn[11] + "/" + uheightn[11];

                                       /*Note to HM - now fiducials are finalised - pls call below service to create face with above fiducials; also ensure you have stored the user pic on server
                                       STEP 4. Create Face
                                       http://inkarnepub-prod.elasticbeanstalk.com/Service1.svc/CreateFace/5/m/1/852/796/285/771/660/907/497/905/693/1013/446/1006/712/732/444/724/829/1041/299/1017/567/1239/?Pic_Path=inkarne/users/1/pictures/1001.JPG
                                       Format: CreateFace/{User_ID}/{Gender}/{Pic_Source}/{w1}/{h1}/{w2}/{h2}/{w3}/{h3}/{w4}/{h4}/{w5}/{h5}/{w6}/{h6}/{w7}/{h7}/{w8}/{h8}/{w9}/{h9}/{w10}/{h10}/{w11}/{h11}/?Pic_Path={Pic_Path}
                                       Note: This will be called at the end once you have the user selected fed points and have uploaded the picture to S3.
                                        */
            SuccessStatus = true;
            //Note to HM - call the next activity here
        }

    }


    private void initFeducials() {
        /*
        umatrix = Imgcodecs.imread(uFilePathPic);
        uwidth_mat = umatrix.cols();
        uheight_mat = umatrix.rows();

        dmatrix = Imgcodecs.imread(dFilePathPic);
        //dmatrix = Imgcodecs.imread(uFilePathPic);
        dwidth_mat = dmatrix.cols();
        dheight_mat = dmatrix.rows();

        //Below needs to be modified to accomodate other devices
        //options.inSampleSize = 2;
        uwidth_physical_pixels = uwidth_mat;
        uheight_physical_pixels = uheight_mat;
//        dwidth_physical_pixels = dwidth_mat / 3;   //commented
//        dheight_physical_pixels = dheight_mat / 3;   //commented
        dwidth_physical_pixels = dwidth_mat; //added
        dheight_physical_pixels = dheight_mat; //added
//
//        dwidth_physical_pixels = dwidth_mat*2; //added
//        dheight_physical_pixels = dheight_mat*2; //added

        Imagebmp = BitmapFactory.decodeFile(uFilePathPic, options);
        Imagebmpdemo = BitmapFactory.decodeFile(dFilePathPic);
        RelativeLayout.LayoutParams layoutParamsu = new RelativeLayout.LayoutParams(uwidth_physical_pixels, uheight_physical_pixels);
        layoutParamsu.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        RelativeLayout.LayoutParams layoutParamsd = new RelativeLayout.LayoutParams(dwidth_physical_pixels, dheight_physical_pixels);
        layoutParamsd.addRule(RelativeLayout.ALIGN_RIGHT, ptable.getId());
        ptable.setLayoutParams(layoutParamsu);
        dtable.setLayoutParams(layoutParamsd);

        String text1 = "Set Image HEIGHT:" + String.valueOf(uheight_physical_pixels) + " WIDTH:" + String.valueOf(uwidth_physical_pixels);
        Log.d("Check point ImageView", text1);

        uHEIGHT = uheight_physical_pixels;
        uWIDTH = uwidth_physical_pixels;
        ucolumnm = umatrix.cols();
        urowm = umatrix.rows();
        uratioh = (float) uHEIGHT / (float) urowm;  ///// deleted
        uratiow = (float) uWIDTH / (float) ucolumnm;  /////deleted

        dHEIGHT = dheight_physical_pixels;
        dWIDTH = dwidth_physical_pixels;
        dcolumnm = dmatrix.cols();
        drowm = dmatrix.rows();
        dratioh = (float) dHEIGHT / (float) drowm;
        dratiow = (float) dWIDTH / (float) dcolumnm;

        //change him
        uratioh = (float) uHEIGHT / (float) drowm;  /////added
        uratiow = (float) uWIDTH / (float) dcolumnm;  /////added
        uratioh = (float) (uratiow*242.0/204);
        */
    }


    private void initFeducials1() {
        /*
        umatrix = Imgcodecs.imread(uFilePathPic);
        uwidth_mat = umatrix.cols();
        uheight_mat = umatrix.rows();

        dmatrix = Imgcodecs.imread(dFilePathPic);
        //dmatrix = Imgcodecs.imread(uFilePathPic);
        dwidth_mat = dmatrix.cols();
        dheight_mat = dmatrix.rows();

        //Below needs to be modified to accomodate other devices
        options.inSampleSize = 2;
        uwidth_physical_pixels = uwidth_mat;
        uheight_physical_pixels = uheight_mat;
//        dwidth_physical_pixels = dwidth_mat / 3;   //commented
//        dheight_physical_pixels = dheight_mat / 3;   //commented
        dwidth_physical_pixels = dwidth_mat; //added
        dheight_physical_pixels = dheight_mat; //added
//
//        dwidth_physical_pixels = dwidth_mat*2; //added
//        dheight_physical_pixels = dheight_mat*2; //added

        Imagebmp = BitmapFactory.decodeFile(uFilePathPic, options);
        Imagebmpdemo = BitmapFactory.decodeFile(uFilePathPic);
        RelativeLayout.LayoutParams layoutParamsu = new RelativeLayout.LayoutParams(uwidth_physical_pixels, uheight_physical_pixels);
        layoutParamsu.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
        RelativeLayout.LayoutParams layoutParamsd = new RelativeLayout.LayoutParams(dwidth_physical_pixels, dheight_physical_pixels);
        layoutParamsd.addRule(RelativeLayout.ALIGN_RIGHT, ptable.getId());
        ptable.setLayoutParams(layoutParamsu);
        dtable.setLayoutParams(layoutParamsd);

        String text1 = "Set Image HEIGHT:" + String.valueOf(uheight_physical_pixels) + " WIDTH:" + String.valueOf(uwidth_physical_pixels);
        Log.d("Check point ImageView", text1);

        uHEIGHT = uheight_physical_pixels;
        uWIDTH = uwidth_physical_pixels;
        ucolumnm = umatrix.cols();
        urowm = umatrix.rows();
        uratioh = (float) uHEIGHT / (float) urowm;  ///// deleted
        uratiow = (float) uWIDTH / (float) ucolumnm;  /////deleted

        dHEIGHT = dheight_physical_pixels;
        dWIDTH = dwidth_physical_pixels;
        dcolumnm = dmatrix.cols();
        drowm = dmatrix.rows();
        dratioh = (float) dHEIGHT / (float) drowm;
        dratiow = (float) dWIDTH / (float) dcolumnm;

        //change him
        uratioh = (float) uHEIGHT / (float) drowm;  /////added
        uratiow = (float) uWIDTH / (float) dcolumnm;  /////added
        uratioh = uratiow;  /////added
        */

    }


    private void requestDataFedPoints() {
        /*
        String url = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_FEDUCIAL_POINTS + User.getInstance().getmGender();
        DataManager.getInstance().requestFiducialPoints(url, new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {
                JSONException error = null;
                JSONArray fDetails = (JSONArray) obj;
                for (int i = 0; i < fDetails.length(); i++) {
                    JSONObject obj2 = null;
                    try {
                        obj2 = fDetails.getJSONObject(i);
                        //dcolumns[i] = obj2.getString("column");
                        //drows[i] = obj2.getString("row");

                        String c = obj2.getString("column");
                        int c1 = Integer.parseInt(c);
                        dcolumns[i] = String.valueOf(c1);

                        String r = obj2.getString("row");
                        int r1 = Integer.parseInt(r);
                        drows[i] = String.valueOf(r1);

                    } catch (JSONException e) {
                        error = e;
                        e.printStackTrace();
                    }
                }
                if (error == null) {
                    initFeducials();
                    drawFeducialPoints();
                }
                progressDialogFed.dismiss();
            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {

            }
        });
        */
    }

    private void beginDownload() {
        progressDialogFed = ProgressDialog.show(this, getString(R.string.message_fiducials_get_points_2), getString(R.string.message_wait_please));
        String demoPicAwsKey;
        if (User.getInstance().getmGender().toLowerCase().equals("m")) {
            demoPicAwsKey = ConstantsUtil.AWS_KEY_FEDUCIALS_DEMOPIC_MALE;
        } else {
            demoPicAwsKey = ConstantsUtil.AWS_KEY_FEDUCIALS_DEMOPIC_FEMALE;
        }
        File file = new File(Environment.getExternalStorageDirectory().toString() + "/" + demoPicAwsKey);
        Log.d(LOGTAG, "To be downloaded. file:  " + file);
        if (ConstantsUtil.checkFileKeyExist(demoPicAwsKey)){//TODO both file need to be checked
            dFilePathPic = file.getAbsolutePath();
            //initFeducials();
            requestDataFedPoints();
            return;
        }
        TransferObserver observer = InkarneAppContext.getTransferUtility().download(ConstantsUtil.AWSBucketName, demoPicAwsKey, file);
        observer.setTransferListener(new DownloadListener(demoPicAwsKey));
    }


    private class DownloadListener implements TransferListener {
        public String awsKey;

        public DownloadListener(String key) {
            this.awsKey = key;
        }

        @Override
        public void onError(int id, Exception ex) {

        }

        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {

        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            if (state == TransferState.COMPLETED) {
                File file = new File(ConstantsUtil.FILE_PATH_APP_ROOT  + this.awsKey);
                dFilePathPic = file.getAbsolutePath();
                //initFeducials();
                requestDataFedPoints();
            } else if (state == TransferState.FAILED) {

            }
        }
    }

    //Lets get fiducials for demo image
//    private void getfeds() {
//        requestFedData(ConstantsUtil.URL_BASEPATH + "GetReferenceFidicuals/" + Gender);
//    }


//    public void requestFedData(String uri) {
//        String content = HttpManager.getData(uri);
//        JSONObject parentObject = null;
//        try {
//            parentObject = new JSONObject(content);
//            JSONArray fDetails = parentObject.getJSONArray(resultString);
//            for (int i = 0; i < fDetails.length(); i++) {
//                JSONObject obj2 = fDetails.getJSONObject(i);
//                dcolumns[i] = obj2.getString("column");
//                drows[i] = obj2.getString("row");
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    @Override
    public void onBackPressed() {
        // your code.
        if(progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            // your code
            if(progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();
        }
        return super.onKeyDown(keyCode, event);
    }
}