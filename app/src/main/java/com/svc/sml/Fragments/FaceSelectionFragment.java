package com.svc.sml.Fragments;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.VideoView;

import com.svc.sml.Activity.TakePicActivity;
import com.svc.sml.Database.User;
import com.svc.sml.R;
import com.svc.sml.Utility.ConstantsUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FaceSelectionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FaceSelectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FaceSelectionFragment extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final  int REQ_CODE_PICK_IMAGE = 1888;
    private static final  int REQUEST_IMAGE_CAPTURE =1889;
    public static final int RESULT_GALLERY = 0;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LinearLayout btnTakeSelfie,btnTakeExisting;
    private Bitmap selectedImageBitmap;
    private String imagePath;
    private VideoView videoView;
    private RelativeLayout containerImageSelection;
    private ImageView ivVideoThumbnail;
    private  String videoPath;

    private OnFragmentInteractionListener mListener;

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String imagePath);
        public void onFragmentInteraction(Uri uri);
    }

    public FaceSelectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FaceSelectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static FaceSelectionFragment newInstance(String param1, String param2) {
        FaceSelectionFragment fragment = new FaceSelectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_face_image_selection, container, false);
        videoView = (VideoView) v.findViewById(R.id.vv_face_selection);
        containerImageSelection = (RelativeLayout)v.findViewById(R.id.con_face_image_selection);
        //containerImageSelection.setVisibility(View.INVISIBLE);
        ivVideoThumbnail = (ImageView)v.findViewById(R.id.iv_thumbnail_face_selection);
        btnTakeExisting = (LinearLayout)v.findViewById(R.id.con_btn_use_existing);
        btnTakeSelfie = (LinearLayout)v.findViewById(R.id.con_btn_take_selfie);
        btnTakeExisting.setOnClickListener(this);
        btnTakeSelfie.setOnClickListener(this);
        if(User.getInstance().getmGender().equals("m")){
            ivVideoThumbnail.setImageResource(R.drawable.thumbnail_image_selection_video_male);
            videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.v_face_selection_light_instruction_loop_male;
        }
        else{
            ivVideoThumbnail.setImageResource(R.drawable.thumbnail_image_selection_video_female);
            videoPath = "android.resource://" + getActivity().getPackageName() + "/" + R.raw.v_face_selection_light_instruction_loop_female;
        }
        ivVideoThumbnail.setOnClickListener(this);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;

//        Display display = getActivity().getWindowManager().getDefaultDisplay();
//        int width = display.getWidth();
//        int height = display.getHeight();
//        //videoview.setLayoutParams(new FrameLayout.LayoutParams(550,550));
       // videoView.setLayoutParams(new FrameLayout.LayoutParams(w, h));

        return  v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onResume(){
        super.onResume();
        playInstructionVideo();
    }

    public void playInstructionVideo() {
        videoView.setVisibility(View.VISIBLE);
        ivVideoThumbnail.setVisibility(View.INVISIBLE);
        videoView.setVideoPath(videoPath);
        videoView.start();
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer mp) {

            }
        });
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
                ivVideoThumbnail.setVisibility(View.INVISIBLE);
            }
        });
        videoView.start();
        videoView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                /*
                if(videoView.isPlaying())
                   videoView.pause();
                else
                videoView.resume();
                */
                return true;
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.con_btn_use_existing:
                takePictureFromGallery();
                break;
            case R.id.con_btn_take_selfie:
                takePictureFromCamera();
                break;
            case R.id.iv_thumbnail_face_selection:
            {
                playInstructionVideo();
            }
                break;
            default:
                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event

    @TargetApi(23)
    //@Override
    public void onAttach(Context context) {
        super.onAttach((Activity) context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            onAttachToContext(activity);
        }
    }

    protected void onAttachToContext(Activity context) {
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()+ " must implement OnFragmentInteractionListener");

        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void takePictureFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    private void takePictureFromGallery2(){
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(
            "content://media/internal/images/media"));
    startActivity(intent);
}
    private void takePictureFromGallery() {
        Intent galleryIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, RESULT_GALLERY);
    }

    private void takePictureFromGallery1()
    {
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT >= 19) {
            // For Android versions of KitKat or later, we use a
            // different intent to ensure
            // we can get the file path from the returned intent URI
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        } else {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        }

        intent.setType("image/*");
        startActivityForResult(intent,REQ_CODE_PICK_IMAGE);
    }

    private String saveCameraImage(Bitmap finalBitmap,boolean isJpg) {
        String filePath = null;
        String root = ConstantsUtil.FILE_PATH_APP_ROOT;
        File myDir = new File(root + ConstantsUtil.FILE_PATH_VISAGE_SELFI_IMAGE);
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10;
        n = generator.nextInt(n);
        String fName = "Camera-image-" + n + ".png";
        File file = new File(myDir, fName);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            if(isJpg){
                finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }else {
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            filePath = file.getAbsolutePath();
            out.flush();
            out.close();
            return filePath;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filePath;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imagePath = saveCameraImage(imageBitmap, true);
            if (mListener != null) {
                mListener.onFragmentInteraction(imagePath);
            }
        }

        if (requestCode == REQ_CODE_PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                imagePath = getPath(uri);
                if (mListener != null) {
                    mListener.onFragmentInteraction(imagePath);
                }
            } catch (URISyntaxException e) {
                Toast.makeText(getActivity(),
                        "Unable to get the file from the given URI.  See error log for details",
                        Toast.LENGTH_LONG).show();
                Log.e("DEBUG", "Unable to upload file from the given uri", e);
            }
        }
    }

/*old */

     /*
    private void setPic() {
        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        mImageView.setImageBitmap(bitmap);
    }
    */


    //private String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        String mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }


    private void takePictureFromCamera3() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }


    private void takePictureFromGalleryOld()
    {
        /*
        startActivityForResult(
                Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .setType("image/*"), "Choose an image"),
                REQ_CODE_PICK_IMAGE);
                */

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                "Select Picture"), REQ_CODE_PICK_IMAGE);
    }


    private void takePictureFromCamera1()
    {

        /*
        startActivityForResult(
                Intent.createChooser(
                        new Intent(Intent.ACTION_GET_CONTENT)
                                .setType("image/*"), "Choose an image"),
                REQ_CODE_PICK_IMAGE);
                */

        Intent intent = new Intent(getActivity(), TakePicActivity.class);
        // Intent intent = new Intent(getActivity(), CameraActivity.class);

        startActivity(intent);
    }

    public void onActivityResultOld(int requestCode, int resultCode,
                                    Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

        switch(requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if(resultCode == Activity.RESULT_OK){
                    try {
                        handleGalleryResult(imageReturnedIntent);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
//                    Uri selectedImage = imageReturnedIntent.getData();
//                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
//
//                    Cursor cursor = getActivity().getContentResolver().query(
//                            selectedImage, filePathColumn, null, null, null);
//                    cursor.moveToFirst();
//                    if(cursor.moveToFirst()) {
//                        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
//                        String filePath = cursor.getString(columnIndex);
//                        cursor.close();
//
//                        if(filePath != null) {
//                            selectedImageBitmap = BitmapFactory.decodeFile(filePath);
//                        }
//                    }
                }
        }
    }

    private void handleGalleryResult(Intent data) throws URISyntaxException {
        Uri selectedImage = data.getData();
        String filePathColumn;
        filePathColumn = getPath(selectedImage);
        if(filePathColumn!=null) {
            selectedImageBitmap = BitmapFactory.decodeFile(filePathColumn);
            //ImageUtils.setPictureOnScreen(mTmpGalleryPicturePath, mImageView);
        }
        else
        {
            try {
                InputStream is = getActivity().getContentResolver().openInputStream(selectedImage);
                filePathColumn = selectedImage.getPath();
                selectedImageBitmap =(BitmapFactory.decodeStream(is));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @SuppressLint("NewApi")
    private String getPath_old(Uri uri) {
        if( uri == null ) {
            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };

        Cursor cursor;
        if(Build.VERSION.SDK_INT >23)
        {
            // Will return "image:x*"
            String wholeID = DocumentsContract.getDocumentId(uri);
            // Split at colon, use second item in the array
            //String id = wholeID.split(":")[1];
            // where id is equal to
            String sel = MediaStore.Images.Media._ID + "=?";
            //cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, sel, new String[]{ id }, null);
            cursor = getActivity().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, sel, null, null);
        }
        else
        {
            cursor = getActivity().getContentResolver().query(uri, projection, null, null, null);
        }
        String path = null;
        try
        {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            int column_index1 = cursor.getColumnIndexOrThrow(projection[0]);
            cursor.moveToFirst();

            path = cursor.getString(column_index);
            path = cursor.getString(column_index).toString();
            cursor.close();
        }
        catch(NullPointerException e) {

        }
        return path;
    }

//    public String getPath2(Uri uri) {
//        // just some safety built in
//        if( uri == null ) {
//            // TODO perform some logging or show user feedback
//            return null;
//        }
//        // try to retrieve the image from the media store first
//        // this will only work for images selected from gallery
//        String[] projection = { MediaStore.Images.Media.DATA };
//        Cursor cursor = managedQuery(uri, projection, null, null, null);
//        if( cursor != null ){
//            int column_index = cursor
//                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//            cursor.moveToFirst();
//            return cursor.getString(column_index);
//        }
//        // this is our fallback here
//        return uri.getPath();
//    }


    /*
     * Gets the file path of the given Uri.
     */
    @SuppressLint("NewApi")
    private String getPath(Uri uri) throws URISyntaxException {
        final boolean needToCheckUri = Build.VERSION.SDK_INT >= 19;
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        // deal with different Uris.
        if (needToCheckUri && DocumentsContract.isDocumentUri(getActivity(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return ConstantsUtil.FILE_PATH_APP_ROOT + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[] {
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = getActivity().getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

}
