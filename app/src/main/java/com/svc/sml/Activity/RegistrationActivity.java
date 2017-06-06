package com.svc.sml.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.msg91.sendotp.library.PhoneNumberUtils;
import com.svc.sml.Database.InkarneDataSource;
import com.svc.sml.Database.User;
import com.svc.sml.Helper.DataManager;
import com.svc.sml.InkarneAppContext;
import com.svc.sml.R;
import com.svc.sml.Utility.Connectivity;
import com.svc.sml.Utility.ConstantsUtil;
import com.svc.sml.Utility.Phone.Country;
import com.svc.sml.Utility.Phone.CountryAdapter;
import com.svc.sml.Utility.Phone.PhoneUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


//import com.svc.inkarne.Fragments.DialerSelectionFragment;

public class RegistrationActivity extends RegistrationSocialMediaSignInActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {
    //private RegistrationFragment regFragment;
    private static final String LOGTAG = RegistrationActivity.class.toString();
    public static final int DIALOG_FRAGMENT = 1;
    public static final int DIAILER_FRAGMENT = 3;
    //public static FragmentCommunicator fComm;
    private Button btnDay, btnMonth, btnYear;
    private Button btnRegistration;
    private ImageView ivMale, ivFemale;
    private TextView tvMale, tvFemale;
    private LinearLayout llMaleContainer, llFemaleContainer;
    private CheckBox cbTermsCondition;
    private EditText etPhoneNumber, etFirstName, etLastName, etEmail;
    private boolean isPhoneNumberWarningShown = false;
    //private EditText etPhoneCode;
    private TextInputLayout tilPhoneCode, tilPhoneNumber, tilFirstName, tilLastName, tilEmail;

    //
    private String gender;
    private String firstname, lastname, DOB, mobilenumber, email;
    private String mobilenumbrwithoutCode;
    private String mobileCode = "";
    int day, month, year;
    private String locationCity = "india";
    InputMethodManager imm;
    InkarneDataSource dataSource;
    private ProgressBar pbRegistration;
    private int countRetry = 0;
    private LocationManager locationManager;
    private String provider;
    private LocationListener locationListener;
    private TermsDialogFragment dialogFragTermsCondition;

    private static final int INITIAL_REQUEST = 1;
    //    private static final int CAMERA_REQUEST=INITIAL_REQUEST+1;
//    private static final int CONTACTS_REQUEST=INITIAL_REQUEST+2;
    private static final int REQUEST_CODE_LOCATION = INITIAL_REQUEST + 3;

    private static final String[] PERMISSION_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    /* PHONE */
    public static final String INTENT_PHONENUMBER = "phonenumber";
    public static final String INTENT_COUNTRY_CODE = "code";
    private String mCountryIso;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_registration);

        pbRegistration = (ProgressBar) findViewById(R.id.pb_registration);
        pbRegistration.setVisibility(View.INVISIBLE);
        btnDay = (Button) findViewById(R.id.btn_day);
        //btnDay.setTypeface(InkarneAppContext.getInkarneTypeFaceML());
        btnDay.setOnClickListener(this);
        btnMonth = (Button) findViewById(R.id.btn_month);
        //btnMonth.setTypeface(InkarneAppContext.getInkarneTypeFaceML());
        btnMonth.setOnClickListener(this);
        btnYear = (Button) findViewById(R.id.btn_year);
        //btnYear.setTypeface(InkarneAppContext.getInkarneTypeFaceML());

        btnYear.setOnClickListener(this);

        btnRegistration = (Button) findViewById(R.id.btn_shared_next);
        btnRegistration.setText("REGISTER");
        btnRegistration.setOnClickListener(this);
        btnRegistration.setEnabled(true);

        llFemaleContainer = (LinearLayout) findViewById(R.id.ll_female);
        llFemaleContainer.setOnClickListener(this);
        llMaleContainer = (LinearLayout) findViewById(R.id.ll_male);
        llMaleContainer.setOnClickListener(this);

        ivFemale = (ImageView) findViewById(R.id.iv_female);
        ivMale = (ImageView) findViewById(R.id.iv_male);
        tvFemale = (TextView) findViewById(R.id.tv_female);
        tvMale = (TextView) findViewById(R.id.tv_male);

        tilFirstName = (TextInputLayout) findViewById(R.id.il_et_firstname);
        tilLastName = (TextInputLayout) findViewById(R.id.il_et_lastname);
        tilEmail = (TextInputLayout) findViewById(R.id.il_et_email);
        tilPhoneNumber = (TextInputLayout) findViewById(R.id.il_et_telephone_number);

        tilFirstName = (TextInputLayout) findViewById(R.id.il_et_firstname);
        etFirstName = (EditText) findViewById(R.id.et_firstname);
        //etFirstName.setTypeface(InkarneAppContext.getInkarneTypeFaceML());
        //etFirstName.addTextChangedListener(generalTextWatcher);
        //etFirstName.setOnEditorActionListener(editorActionListner);

        etLastName = (EditText) findViewById(R.id.et_lastname);
        //etLastName.setTypeface(InkarneAppContext.getInkarneTypeFaceML());
        etEmail = (EditText) findViewById(R.id.et_email);
        //etEmail.setTypeface(InkarneAppContext.getInkarneTypeFaceML());
        etPhoneNumber = (EditText) findViewById(R.id.et_telephone_number);
        //etPhoneNumber.setTypeface(InkarneAppContext.getInkarneTypeFaceML());
        /* phone */
        initCodes(RegistrationActivity.this);
        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (dstart > 0 && !Character.isDigit(c)) {
                        return "";
                    }
                }
                return null;
            }
        };
        etPhoneNumber.setFilters(new InputFilter[]{filter});
        mSpinner = (Spinner) findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
        mAdapter = new CountryAdapter(RegistrationActivity.this);
        mSpinner.setAdapter(mAdapter);

        mCountryIso = PhoneNumberUtils.getDefaultCountryIso(RegistrationActivity.this);
        cbTermsCondition = (CheckBox) findViewById(R.id.cb_reg_terms_condition);
        imm = (InputMethodManager) getSystemService(RegistrationActivity.this.INPUT_METHOD_SERVICE);
        // imm.showSoftInput(etFirstName, InputMethodManager.SHOW_IMPLICIT);
        //imm.hideSoftInputFromWindow(etFirstName.getWindowToken(), 0);
        dataSource = InkarneDataSource.getInstance(InkarneAppContext.getAppContext());
        customizeSignBtn();

    }

    protected void customizeSignBtn() {
        signIn_btn = (SignInButton) findViewById(R.id.sign_in_button);
        signIn_btn.setSize(SignInButton.SIZE_STANDARD);
        signIn_btn.setScopes(new Scope[]{Plus.SCOPE_PLUS_LOGIN});
        signIn_btn.setOnClickListener(RegistrationActivity.this);
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permissionFine = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);
        boolean isPermissionFine = true;
        if (permissionFine != PackageManager.PERMISSION_GRANTED) {
            isPermissionFine = false;
            //We don't have permission so prompt the user
//            ActivityCompat.requestPermissions(
//                    activity,
//                    PERMISSION_LOCATION,
//                    REQUEST_CODE_LOCATION
//            );
        }

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (permission != PackageManager.PERMISSION_GRANTED || !isPermissionFine) {
            //We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSION_LOCATION,
                    REQUEST_CODE_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_LOCATION && grantResults != null && grantResults.length != 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //  gps functionality
        }
    }


    @Override
    public void onBackPressed() {
        if (dialogFragTermsCondition != null && dialogFragTermsCondition.isVisible()) {
            dialogFragTermsCondition.goBackIfCan();
        }
        super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (dialogFragTermsCondition != null && dialogFragTermsCondition.isVisible()) {
            dialogFragTermsCondition.goBackIfCan();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    public void onStart() {
        /// mGoogleApiClient.connect();
        super.onStart();
        verifyStoragePermissions(RegistrationActivity.this);
        initLocation();
    }

    public void onStop() {
        //// mGoogleApiClient.disconnect();
        super.onStop();
    }


    @Override
    public void onResume() {
        super.onResume();
        countRetry = 0;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager != null && locationListener != null)
                locationManager.removeUpdates(locationListener);
        }
    }

    private void resetDBAndData() {
        if (dataSource != null)
            dataSource.close();
        getApplicationContext().deleteDatabase("inkarne.db");
        File inkarneDir = new File(ConstantsUtil.FILE_PATH_APP_ROOT + "inkarne");
        getSharedPreferences("inkarne", 0).edit().clear().commit();
        //inkarneDir.delete();
        //deleteDirectory(inkarneDir);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                Toast.makeText(this, "start sign process", Toast.LENGTH_SHORT).show();
                gPlusSignIn();
                break;
            case R.id.btn_day: {
                //hideSoftKeyboard();
                clickHandlerDOB(v);
            }
            break;
            case R.id.btn_month: {
                //hideSoftKeyboard();
                clickHandlerDOB(v);
            }
            break;
            case R.id.btn_year: {
                //hideSoftKeyboard();
                clickHandlerDOB(v);
            }
            break;
            case R.id.btn_shared_next: {
                if (verifyInput(true)) {//TODO
                    countRetry = 0;
                    //showPhoneNumberWarning(errorMessage, "");
                    showFragmentDialog(DIALOG_FRAGMENT);
                }
            }
            break;
            case R.id.ll_female: {
                ivFemale.setImageResource(R.drawable.btn_female_selected);
                ivMale.setImageResource(R.drawable.btn_male);
                tvFemale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.textColorPrimary));
                // tvMale.setTextColor(getColor(R.color.hint_text_color));
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    tvMale.setTextColor(getResources().getColor(R.color.hint_text_color, getTheme()));
//                }else {
//                    tvMale.setTextColor(getResources().getColor(R.color.hint_text_color));
//                }
                tvMale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.hint_text_color));
                gender = getString(R.string.female);
            }
            break;
            case R.id.ll_male: {
                ivMale.setImageResource(R.drawable.btn_male_selected);
                ivFemale.setImageResource(R.drawable.btn_female);
                tvMale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.textColorPrimary));
                tvFemale.setTextColor(ContextCompat.getColor(RegistrationActivity.this, R.color.hint_text_color));
                gender = getString(R.string.male);
            }
            break;

            default:

                break;
        }
    }


    private void clickHandlerDOB(View v) {
        String title = null;
        final ArrayList<String> entries = new ArrayList<String>();
        final int viewId = v.getId();
        switch (viewId) {
            case R.id.btn_day: {
                entries.addAll(Arrays.asList(ConstantsUtil.getArrayDays()));
                title = "Select Day";
            }
            break;
            case R.id.btn_month: {
                entries.addAll(Arrays.asList(ConstantsUtil.getArrayMonths()));
                title = "Select Month";
            }
            break;
            case R.id.btn_year: {
                ArrayList<String> listYear = new ArrayList<String>();
                listYear.addAll(Arrays.asList(ConstantsUtil.getArrayYear()));
                Collections.reverse(listYear);
                Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int latestYearInList = Integer.parseInt(listYear.get(0));
                int diff = year - latestYearInList - 18; //2016 2013

                ArrayList<String> added = new ArrayList<String>();
                for (int i = 0; i < diff; i++) {
                    int year1 = latestYearInList + i + 1;
                    listYear.add(0, String.valueOf(year1));
                }

                entries.addAll(listYear);
                title = "Select Year";
            }
            break;
            default:
                break;
        }
        int selected = 0;
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(RegistrationActivity.this);
        builderSingle.setIcon(R.mipmap.ic_launcher);
        builderSingle.setTitle(title);

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                RegistrationActivity.this,
                android.R.layout.simple_list_item_1, entries);
        arrayAdapter.notifyDataSetChanged();

        builderSingle.setNegativeButton(
                "cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builderSingle.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String strName = arrayAdapter.getItem(which);

                        switch (viewId) {
                            case R.id.btn_day: {
                                btnDay.setText(strName);

                            }
                            break;
                            case R.id.btn_month: {
                                btnMonth.setText(strName);
                            }
                            break;
                            case R.id.btn_year: {
                                btnYear.setText(strName);
                            }
                            break;
                            default:
                                break;
                        }
                    }
                });
        builderSingle.show();
    }

    public boolean verifyInput(boolean showToast) {
        String errorMessage = "";
        boolean isVerified = true;
        firstname = etFirstName.getText().toString().trim();
        if (firstname.isEmpty()) {
            etFirstName.setError("First name can not be empty");
            isVerified = false;
        }
        lastname = etLastName.getText().toString().trim();
        if (lastname.isEmpty()) {
            //etLastName.setError("Last name can not be empty");
        }

        email = etEmail.getText().toString().trim();
        if (email.isEmpty()) {
            etEmail.setError("Email can not be empty");
            isVerified = false;
        } else if (!isEmailValid(email)) {
            etEmail.setError("Please enter valid email address");
            isVerified = false;
        }

        mobileCode = "" + ((Country) mSpinner.getSelectedItem()).getCountryCode();
        mobilenumber = etPhoneNumber.getText().toString().trim();
        if (mobilenumber.isEmpty()) {
            etPhoneNumber.setError("Phone number can not be empty");
            isVerified = false;
        } else if (mobileCode.equals("91") && mobilenumber.length() != 10) {
            etPhoneNumber.setError("Please enter valid phone number");
            isVerified = false;
        } else {
            String phone = validatePhoneNumber(mobileCode + mobilenumber);
            if (phone == null) {
                etPhoneNumber.setError("Please enter valid phone number");
                isVerified = false;
            }
        }

        String yearStr = btnYear.getText().toString();
        if (!yearStr.isEmpty())
            year = Integer.parseInt(yearStr);
        else {
            errorMessage = "Please select year of your birthday";
            isVerified = false;
        }

        String monthStr = btnMonth.getText().toString();
        if (!monthStr.isEmpty()) {
            month = Arrays.asList(ConstantsUtil.getArrayMonths()).indexOf(monthStr) + 1;
        } else {
            errorMessage = "Please select month of your birthday";
            isVerified = false;
        }

        String dayStr = btnDay.getText().toString();
        if (!dayStr.isEmpty())
            day = Integer.parseInt(dayStr);
        else {
            errorMessage = "Please select day of your birthday";
            isVerified = false;
        }

        if (year != 0 && month != 0 && day != 0) {
            boolean isValidDate = validateDate(year, month, day);
            if (!isValidDate) {
                errorMessage = "DOB is not a valid date";
                isVerified = false;
            } else {
                int age = getAge(year, month, day);
                if (age < 18) {
                    errorMessage = "You should be above 18 to be able to use this app ";
                    isVerified = false;
                }
            }
        }

        if (gender == null || gender.isEmpty()) {
            errorMessage = "Please select gender";
            isVerified = false;
        }

        DOB = day + "-" + month + "-" + year;
        mobilenumbrwithoutCode = mobilenumber;
        mobilenumber = mobileCode + mobilenumber;

        int length = String.valueOf(etPhoneNumber.getText().toString()).length();
        if (errorMessage.length() != 0)
            showAlertError(errorMessage, "");
        else {
            if (isVerified && !isPhoneNumberWarningShown && !mobileCode.equals("91")) {
                showPhoneNumberWarning("", getString(R.string.message_non_indian_phone_warning));

                isVerified = false;
            }
        }

        //TODO for testing purpose
//        if(isVerified== true){
//            return false;
//        }
        return isVerified;
    }

    protected void showAlertError(String title, String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                }).create().show();
    }

    protected void showPhoneNumberWarning(String title, String msg) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(RegistrationActivity.this);
        builder.setTitle(title)
                .setMessage(msg)
                .setCancelable(false)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        isPhoneNumberWarningShown = true;
                        countRetry = 0;
                        showFragmentDialog(DIALOG_FRAGMENT);

                    }
                }).create().show();
    }


    void showFragmentDialog(int type) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
//        Fragment prev = getActivity().getFragmentManager().findFragmentByTag("dialog");
//        if (prev != null) {
//            ft.remove(prev);
//        }
//        ft.addToBackStack(null);
        switch (type) {
            case DIALOG_FRAGMENT:
                //dialogFragTermsCondition = TermsDialogFragment.newInstance(0);
                dialogFragTermsCondition = new TermsDialogFragment();
                dialogFragTermsCondition.setCancelable(true);
                dialogFragTermsCondition.setTargetFragment(dialogFragTermsCondition, DIALOG_FRAGMENT);
                dialogFragTermsCondition.show(getSupportFragmentManager().beginTransaction(), "term_condition_dialog");

                break;
            default:
                break;
        }
    }

    public void loginServiceCall() {
        if (countRetry < ConstantsUtil.COUNT_RETRY_SERVICE_CRITICAL)
            pbRegistration.setVisibility(View.VISIBLE);
        resetDBAndData(); //TODO 19may
        if (!Connectivity.isConnected(RegistrationActivity.this)) {
            Toast.makeText(getApplicationContext(), "Please check your network connection", Toast.LENGTH_SHORT).show();
        }

        /* TODO Temporary for fast testing */
        if (gender == null) {
            gender = "f";
        }
        if (firstname == null || firstname.length() == 0) {
            Random r = new Random();
            int i1 = r.nextInt(900) + 101;
            firstname = "TestFirstName_" + i1;
        }
        if (lastname == null || lastname.length() == 0) {
            lastname = "lastname";
        }
        if (email == null || email.isEmpty()) {
            email = "test@email.com";
        }
        if (mobilenumber == null || mobilenumber.isEmpty()) {
            mobilenumber = "12345678";
        }
        if (day == 0) {
            day = 1;
        }
        if (month == 0) {
            month = 6;
        }
        if (year == 0) {
            year = 1990;
        }

        //Mobile_OS_Version=Android_Lollipop_2.0&Device_Manufacturer=Samsung&Device_Model=s7&Device_Memory=4GB&Device_Processor=SnapDragon&Network_Type=Wifi
        //String osVersion = System.getProperty("os.version");
        String notFound = "NA";
        String UTF8 = "utf-8";
        int osAPI = Build.VERSION.SDK_INT;
        String osVersion = Build.VERSION.RELEASE;
        String brand_Manufacturer = Build.BRAND;
        String deviceModel = Build.MODEL;
        String deviceRAM = getTotalRAM();
        String internalMemory = getTotalInternalMemorySize();
        String extMemory = getTotalExternalMemorySize();
        String arch = System.getProperty("os.arch");
        String networkType = getNetworkType(RegistrationActivity.this);
        try {
            osVersion = Uri.encode(osVersion);
            firstname = Uri.encode(firstname);
            lastname = Uri.encode(lastname);

            if (brand_Manufacturer != null)
                brand_Manufacturer = Uri.encode(brand_Manufacturer);
            else
                brand_Manufacturer = notFound;

            if (deviceModel != null) {
                String dm = deviceModel;
                deviceModel = Uri.encode(deviceModel, UTF8);

                Log.d(LOGTAG, "Uri.encode(deviceModel, UTF8) : " + deviceModel);
                String dmNotUTF8 = Uri.encode(dm);
                Log.d(LOGTAG, "Uri.encode(dm) :" + dmNotUTF8);
                String dmURLEncode = URLEncoder.encode(dm, UTF8);
                Log.d(LOGTAG, "URLEncoder.encode(dm, UTF8) :" + dmURLEncode);
            } else
                deviceModel = notFound;

//            if (deviceRAM != null)
//                deviceRAM = URLEncoder.encode(deviceRAM, UTF8);
//            else
//                deviceRAM = notFound;

            if (arch != null)
                arch = Uri.encode(arch, UTF8);
            else
                arch = notFound;

            if (locationCity != null) {
                Log.d(LOGTAG, locationCity);
                String loc = locationCity;
                locationCity = Uri.encode(locationCity, UTF8);
            } else
                locationCity = notFound;
            networkType = getNetworkType(RegistrationActivity.this);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = ConstantsUtil.URL_BASEPATH + ConstantsUtil.URL_METHOD_CREATEUSER + gender + "/" + firstname + "/" + lastname + "/" + day + "/" + month + "/" + year + "/" + email + "/" + mobilenumber
                + "/" + locationCity
                + "?Mobile_OS_Version=" + osVersion
                + "&Device_Manufacturer=" + brand_Manufacturer
                + "&Device_Model=" + deviceModel
                + "&Device_Memory=" + deviceRAM
                + "&Device_Internal_Storage=" + internalMemory
                + "&Device_External_Storage=" + extMemory
                + "&Device_Processor=" + arch
                + "&Network_Type=" + networkType;
        DataManager.getInstance().requestCreateUser(url, new DataManager.OnResponseHandlerInterface() {
            @Override
            public void onResponse(Object obj) {
                String userId = (String) obj;
                Log.d(LOGTAG, "Registration Successful userId :" + userId);
                User user = new User();
                user.setmUserId(userId);
                user.setmFirstName(firstname);
                user.setmLastName(lastname);
                user.setmMobileNumber(mobilenumber);
                user.setmGender(gender);
                user.setDob_dd_mmm_yyyy(day + "-" + month + "-" + year);
                user.setDob_day(day);
                user.setDob_month(month);
                user.setDob_year(year);
                user.setEmailId(email);
                user.setmPIN(" ");
                user = InkarneAppContext.getDataSource().create(user);
                User.setInstance(user);
                User.getInstance().saveUserId(user.getmUserId());

                //TODO SENDOTP
                if (mobileCode.equals("91")) {
                    launchOTPVerificationActivity(mobilenumbrwithoutCode, mobileCode);
                } else {
                    launchBMActivity();
                }
                //launchBMActivity();
                pbRegistration.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onResponseError(String errorMessage, int errorCode) {
                pbRegistration.setVisibility(View.INVISIBLE);
                countRetry++;
                if (countRetry == 1) {
                    Toast.makeText(RegistrationActivity.this, "Please check your network connection", Toast.LENGTH_SHORT).show();
                }
                if (countRetry < ConstantsUtil.COUNT_RETRY_SERVICE_CRITICAL) {
                    loginServiceCall();
                }
            }
        });
    }

    private void launchOTPVerificationActivity(String phoneNumber, String mobileCode) {
        Intent verification = new Intent(RegistrationActivity.this, VerificationActivity.class);
        verification.putExtra(INTENT_PHONENUMBER, phoneNumber);
        verification.putExtra(INTENT_COUNTRY_CODE, mobileCode);
        //verification.putExtra(INTENT_COUNTRY_CODE, Iso2Phone.getPhone(mCountryIso));
        startActivity(verification);
    }

    private void launchBMActivity() {
        //Intent myIntent = new Intent(getActivity(), FaceSelectionActivity.class);
        Intent myIntent = new Intent(RegistrationActivity.this, BodyMeasurementActivity.class);
        //myIntent.putExtra("key", ""); //Optional parameters
        startActivity(myIntent);
    }


    /******
     * Verify Methods
     *********/
    public int getAge(int _year, int _month, int _day) {
        GregorianCalendar cal = new GregorianCalendar();
        int y, m, d, a;

        y = cal.get(Calendar.YEAR);
        m = cal.get(Calendar.MONTH);
        d = cal.get(Calendar.DAY_OF_MONTH);
        cal.set(_year, _month, _day);
        a = y - cal.get(Calendar.YEAR);
        if ((m < cal.get(Calendar.MONTH))
                || ((m == cal.get(Calendar.MONTH)) && (d < cal
                .get(Calendar.DAY_OF_MONTH)))) {
            --a;
        }
        if (a < 0)
            throw new IllegalArgumentException("Age < 0");
        return a;
    }

    public boolean validateDate(int year, int month, int day) {
        if (day == 31 &&
                (month == 4 || month == 6 || month == 9 ||
                        month == 11)) {
            return false; // only 1,3,5,7,8,10,12 has 31 days
        } else if (month == 2) {
            //leap year
            if (year % 4 == 0) {
                if (day == 30 || day == 31) {
                    return false;
                } else {
                    return true;
                }
            } else {
                if (day == 29 || day == 30 || day == 31) {
                    return false;
                } else {
                    return true;
                }
            }
        } else {
            return true;
        }
    }

    //    public class DateValidator {
//
//        public boolean isThisDateValid(String dateToValidate, String dateFromat){
//
//            if(dateToValidate == null){
//                return false;
//            }
//
//            SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
//            sdf.setLenient(false);
//
//            try {
//
//                //if not valid, it will throw ParseException
//                Date date = sdf.parse(dateToValidate);
//                System.out.println(date);
//
//            } catch (ParseException e) {
//
//                e.printStackTrace();
//                return false;
//            }
//
//            return true;
//        }
//
//    }
    private static boolean isEmailValid(String email) {
        boolean isValid = false;

        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = email;

        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (matcher.matches()) {
            isValid = true;
        }
        return isValid;
    }

    /**********************
     * Get Analytics Data
     **********************/

    private void initLocation() {
        if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) RegistrationActivity.this.getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        if (provider == null)
            provider = LocationManager.NETWORK_PROVIDER;
        Location location = locationManager.getLastKnownLocation(provider);
        // Define a listener that responds to location updates
        if (location == null) {
            locationListener = new LocationListener() {
                public void onLocationChanged(Location location) {
                    // Called when a new location is found by the network location provider.
                    locationCity = getCityName(location);
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }

                public void onProviderEnabled(String provider) {
                    // Toast.makeText(getActivity(), "Enabled new provider " + provider,Toast.LENGTH_SHORT).show();
                    Log.e(LOGTAG, "Enabled new provider " + provider);
                }

                public void onProviderDisabled(String provider) {
                    //Toast.makeText(getActivity(), "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
                }
            };
            // Register the listener with the Location Manager to receive location updates
            if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 100, locationListener);
            locationManager.requestLocationUpdates(provider, 0, 0, locationListener);
        } else {
            locationCity = getCityName(location);
        }
    }

    public String getCityName(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        Geocoder geoCoder = new Geocoder(RegistrationActivity.this, Locale.getDefault());
        try {
            List<Address> address = geoCoder.getFromLocation(lat, lng, 1);
            Log.e(LOGTAG, "address: " + address);
            if (address != null && address.size() != 0) {

                StringBuilder builder = new StringBuilder();
                int maxLines = address.get(0).getMaxAddressLineIndex();
                for (int i = 0; i < maxLines; i++) {
                    String addressStr = address.get(0).getAddressLine(i);
                    builder.append(addressStr);
                    builder.append(" ");
                }
                String finalAddress = builder.toString(); //This is the complete address.

                String locationcity = address.get(0).getAddressLine(maxLines - 1);
                String[] split = locationcity.split(",");
                locationCity = split[0];
                Log.e(LOGTAG, "address getAddressLine :" + locationCity);

                if (locationCity == null || locationCity.isEmpty())
                    locationCity = address.get(0).getLocality();
                Log.e(LOGTAG, "address getLocality  " + address.get(0).getLocality());

                if (locationCity != null && locationCity.length() != 0) {
                    if (ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            || ActivityCompat.checkSelfPermission(RegistrationActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        if (locationManager != null && locationListener != null)
                            locationManager.removeUpdates(locationListener);
                    }
                }
                return locationCity;
            }
        } catch (IOException e) {

        } catch (NullPointerException e) {
            //Handle NullPointerException
        }
        return null;
    }

    public String getTotalRAM() {

        /*
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) getActivity().getSystemService(getActivity().ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        */

        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);
//            double mb = totRam / 1024.0;
            double gb = totRam / 1048576.0;
//            double tb = totRam / 1073741824.0;
            lastValue = twoDecimalForm.format(gb).concat("GB");
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }
        return lastValue;
    }


    public static String getNetworkType(Context context) {
        if (Connectivity.isConnectedWifi(context))
            return "Wifi";
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();
        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_EDGE:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "2G";
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
                /**
                 From this link https://goo.gl/R2HOjR ..NETWORK_TYPE_EVDO_0 & NETWORK_TYPE_EVDO_A
                 EV-DO is an evolution of the CDMA2000 (IS-2000) standard that supports high data rates.

                 Where CDMA2000 https://goo.gl/1y10WI .CDMA2000 is a family of 3G[1] mobile technology standards for sending voice,
                 data, and signaling data between mobile phones and cell sites.
                 */
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                //Log.d("Type", "3g");
                //For 3g HSDPA , HSPAP(HSPA+) are main  networktype which are under 3g Network
                //But from other constants also it will 3g like HSPA,HSDPA etc which are in 3g case.
                //Some cases are added after  testing(real) in device with 3g enable data
                //and speed also matters to decide 3g network type
                //http://goo.gl/bhtVT
                return "3G";
            case TelephonyManager.NETWORK_TYPE_LTE:
                //No specification for the 4g but from wiki
                //I found(LTE (Long-Term Evolution, commonly marketed as 4G LTE))
                //https://goo.gl/9t7yrR
                return "4G";
            default:
                return "NA";
        }
    }

    public String getExternalStorageInMB() {
        StatFs stat = new StatFs(ConstantsUtil.FILE_PATH_APP_ROOT);
        long bytesAvailable = (long) stat.getBlockSize() * (long) stat.getAvailableBlocks();
        long megAvailable = bytesAvailable / (1024 * 1024);
        //long GBAvailable =  (bytesAvailable / (1024 * 1048576));
        Log.e("", "Available MB : " + megAvailable);
        return String.valueOf(megAvailable);
    }

    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    public static String getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = stat.getBlockSize();
            long totalBlocks = stat.getBlockCount();
            long GBAvailable = (totalBlocks * blockSize / (1024 * 1048576));
            return String.valueOf(GBAvailable) + "GB";
        } else {
            return "NA";
        }
    }

    public static String getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        long GBAvailable = (totalBlocks * blockSize / (1024 * 1048576));
        return String.valueOf(GBAvailable) + "GB";
    }


//    @Override
//    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
//        // TODO Auto-generated method stub
//        if (arg2.getAction() == KeyEvent.ACTION_UP){
//            // Key up event
//        }
//        else{
//            // Key down event
//        }
//        return false;
//    }

    @SuppressLint("ValidFragment")
    public class TermsDialogFragment extends DialogFragment {
        public ProgressBar pbWebView;
        public WebView webView;
        private CheckBox checkBox;
        private TextView tvTitle;
        private ImageButton btnBack;
        private String url = "http://www.stylemylooks.com/termsofuse.htm";

        public TermsDialogFragment() {

        }

        public TermsDialogFragment newInstance(int num) {
            TermsDialogFragment dialogFragment = new TermsDialogFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("num", num);
            dialogFragment.setArguments(bundle);
            return dialogFragment;
        }

        public void goBackIfCan() {
            if (webView.canGoBack()) {
                webView.goBack();
            }
        }

        public void onResume() {
            super.onResume();
            getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (webView.canGoBack()) {
                            webView.goBack();
                            return false;
                        }
                        return true; // pretend we've processed it
                    } else
                        return false; // pass on to be processed as normal
                }
            });
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View view = getActivity().getLayoutInflater().inflate(R.layout.dailog_reg_terms_condition, new LinearLayout(getActivity()), false);
            pbWebView = (ProgressBar) view.findViewById(R.id.pb_web);
            tvTitle = (TextView) view.findViewById(R.id.tv_header_title_terms_condition);

            //textview.setPaintFlags(textview.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            btnBack = (ImageButton) view.findViewById(R.id.btn_back_terms_condition);
            webView = (WebView) view.findViewById(R.id.wb_terms_condition);
            checkBox = (CheckBox) view.findViewById(R.id.cb_reg_terms_condition);
            checkBox.setOnCheckedChangeListener(
                    new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            dismiss();
                            loginServiceCall();
                        }
                    }
            );

            //webView.loadUrl("file:///android_asset/terms_condition.html");
            Dialog builder = new Dialog(getActivity());
            builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
            builder.setContentView(view);


            webView.setWebViewClient(new WebViewClient() {
                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    pbWebView.setVisibility(View.VISIBLE);
                    view.loadUrl(url);
                    return true;
                }

                @Override
                public void onPageFinished(WebView view, final String url) {
                    pbWebView.setVisibility(View.INVISIBLE);
                }
            });
            webView.loadUrl(url);


            return builder;
        }
    }


    /******************
     * PHONE VERIFICATION
     ***************************/
//Phone verification
    protected SparseArray<ArrayList<Country>> mCountriesMap = new SparseArray<ArrayList<Country>>();
    protected Spinner mSpinner;
    protected PhoneNumberUtil mPhoneNumberUtil = PhoneNumberUtil.getInstance();
    protected CountryAdapter mAdapter;

    protected void initUI(View rootView) {
        mSpinner = (Spinner) rootView.findViewById(R.id.spinner);
        mSpinner.setOnItemSelectedListener(mOnItemSelectedListener);
        mAdapter = new CountryAdapter(RegistrationActivity.this);
        mSpinner.setAdapter(mAdapter);

        InputFilter filter = new InputFilter() {
            public CharSequence filter(CharSequence source, int start, int end,
                                       Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    char c = source.charAt(i);
                    if (dstart > 0 && !Character.isDigit(c)) {
                        return "";
                    }
                }
                return null;
            }
        };
    }

    protected AdapterView.OnItemSelectedListener mOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            Country c = (Country) mSpinner.getItemAtPosition(position);
            isPhoneNumberWarningShown = false;//TODO can be removed if we want one time alert for non indian code selection
            //showPhoneNumberWarning(getString(R.string.message_non_indian_phone_warning),"");

//            if (mLastEnteredPhone != null && mLastEnteredPhone.startsWith(c.getCountryCodeStr())) {
//                return;
//            }
//            etPhoneNumber.getText().clear();
//            etPhoneNumber.getText().insert(mPhoneEdit.getText().length() > 0 ? 1 : 0, String.valueOf(c.getCountryCode()));
//            etPhoneNumber.setSelection(mPhoneEdit.length());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    protected void initCodes(Context context) {
        new AsyncPhoneInitTask(context).execute();
    }


    protected class AsyncPhoneInitTask extends AsyncTask<Void, Void, ArrayList<Country>> {

        private int mSpinnerPosition = -1;
        private Context mContext;

        public AsyncPhoneInitTask(Context context) {
            mContext = context;
        }

        @Override
        protected ArrayList<Country> doInBackground(Void... params) {
            ArrayList<Country> data = new ArrayList<Country>(233);
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(mContext.getApplicationContext().getAssets().open("countries.dat"), "UTF-8"));

                String line;
                int i = 0;
                while ((line = reader.readLine()) != null) {
                    Country c = new Country(mContext, line, i);
                    data.add(c);
                    ArrayList<Country> list = mCountriesMap.get(c.getCountryCode());
                    if (list == null) {
                        list = new ArrayList<Country>();
                        mCountriesMap.put(c.getCountryCode(), list);
                    }
                    list.add(c);
                    i++;
                }
            } catch (IOException e) {

            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {

                    }
                }
            }
//            if (!TextUtils.isEmpty(etPhoneNumber.getText())) {
//                return data;
//            }
            String countryRegion = PhoneUtils.getCountryRegionFromPhone(mContext);
            int code = mPhoneNumberUtil.getCountryCodeForRegion(countryRegion);
            ArrayList<Country> list = mCountriesMap.get(code);
            if (list != null) {
                for (Country c : list) {
                    if (c.getPriority() == 0) {
                        mSpinnerPosition = c.getNum();
                        break;
                    }
                }
            }
            return data;
        }

        @Override
        protected void onPostExecute(ArrayList<Country> data) {
            mAdapter.addAll(data);
            mSpinnerPosition = 94;//for India
            if (mSpinnerPosition >= 0) {
                mSpinner.setSelection(mSpinnerPosition);
            }
        }

    }

    protected String validatePhoneNumber(String mobilenum) {
        String region = null;
        String phone = null;
        if (mobilenum != null) {
            try {
                Phonenumber.PhoneNumber p = mPhoneNumberUtil.parse("+" + mobilenum, null);
                StringBuilder sb = new StringBuilder(16);
                sb.append("+").append(p.getCountryCode()).append(p.getNationalNumber());
                phone = sb.toString();
                region = mPhoneNumberUtil.getRegionCodeForNumber(p);
            } catch (NumberParseException ignore) {
            }
        }
        if (region != null) {
            return phone;
        } else {
            return null;
        }
    }
}
