package com.arona.control_res_app;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.arona.control_res_app.databinding.ActivityHomeBinding;
import com.arona.control_res_app.functions.MySingleton;
import com.arona.control_res_app.functions.Tools;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationView;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {
    ActivityHomeBinding mBinding;
    private AlertDialog mAlertDialog;
    Date dateAndTime = Calendar.getInstance().getTime();
    SimpleDateFormat dateF = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);
    SimpleDateFormat dateFormat = new SimpleDateFormat("kk:mm:ss", Locale.ENGLISH);
    String time = dateFormat.format(dateAndTime);
    String date = dateF.format(dateAndTime);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        mBinding.ahBtnScaner.setOnClickListener(v -> scanQR());
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        toolbar.setNavigationOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
        navigationView.setNavigationItemSelectedListener(item -> {

            int id = item.getItemId();
            drawerLayout.closeDrawer(GravityCompat.START);
            switch (id) {

                case R.id.nav_home:
                    Toast.makeText(HomeActivity.this, "Home is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_message:
                    Toast.makeText(HomeActivity.this, "Message is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.synch:
                    Toast.makeText(HomeActivity.this, "Synch is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.trash:
                    Toast.makeText(HomeActivity.this, "Trash is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.settings:
                    Toast.makeText(HomeActivity.this, "Settings is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_login:
                    Toast.makeText(HomeActivity.this, "Login is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_share:
                    Toast.makeText(HomeActivity.this, "Share is clicked", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.nav_rate:
                    Toast.makeText(HomeActivity.this, "Rate us is Clicked", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    return true;

            }
            return true;
        });
        if (compareTime("06:00:00", "09:00:00", time)) {
            mBinding.ahTextViewRepas.setText("Petit dejeuné");
            mBinding.ahTextViewDate.setText("Date: " + date);
        } else if (compareTime("11:00:00", "14:00:00", time)) {
            mBinding.ahTextViewRepas.setText("Repas test");
            mBinding.ahTextViewDate.setText("Date: " + date);
        } else if (compareTime("18:00:00", "22:00:00", "19:00:00")) {
            mBinding.ahTextViewRepas.setText("Diner");
            mBinding.ahTextViewDate.setText("Date: " + date);
        } else {
            mBinding.ahTextViewRepas.setText("--");
            mBinding.ahTextViewDate.setText("Date: -- -- --");
            mBinding.ahTextViewNbrTickets.setText("----");
        }
        getNbrTicket();
    }

    private void scanQR() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Augmenter volume flash");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        options.setCaptureActivity(CaptureAct.class);
        mLauncher.launch(options);
    }

    ActivityResultLauncher<ScanOptions> mLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            verifyIsExist(result.getContents());
        }
    });

    public void verifyIsExist(String s) {
        String url = "http://" + LoginActivity.getIpAdd() + "/memoir/server/scanner.php";
        Map<String, String> hashMap = new HashMap<>();
        if (compareTime("06:00:00", "09:00:00", time)) {
            hashMap.put("periode", "matin");
        } else if (compareTime("11:00:00", "14:00:00", time) || compareTime("18:00:00", "22:00:00", "21:00:00")) {
            hashMap.put("periode", "repas");
        } else {
            new Tools(this).displayAlert("erreur", "impossible d'effectuer cette opération à cette moment");
        }
        hashMap.put("numberCart", s);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                String res = jsonObject.getString("response");
                if (res.equals("numCard_not_exist")) {
                    new Tools(this).displayAlert("erreur", "Etudiant in trouvable");
                } else if (res.equals("not_ok")) {
                    new Tools(this).displayAlert("erreur", "Pas assez de tickets");
                } else {
                    Bundle bundle = getIntent().getExtras();
                    int id_controler = bundle.getInt("id_controler");
                    String resto = bundle.getString("nomRest");
                    Map<String, String> map = new HashMap<>();
                    if (compareTime("06:00:00", "09:00:00", time)) {
                        map.put("type", "peti_dej");
                    } else if (compareTime("11:00:00", "14:00:00", time)) {
                        map.put("type", "repas");
                    } else if (compareTime("18:00:00", "22:00:00", "21:00:00")) {
                        map.put("type", "diner");
                    }
                    map.put("id_controler", "" + id_controler);
                    map.put("resto", resto);
                    map.put("etudiant", s);
                    transaction(map);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                return hashMap;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public void transaction(Map<String, String> hashMap) {
        String url = "http://" + LoginActivity.getIpAdd() + "/memoir/server/scanner.php";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                String responsePhp = jsonObject.getString("response");
                if (responsePhp.equals("ok")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("l' opération a reussi");
                    View view = getLayoutInflater().inflate(R.layout.layout_dialog, null);
                    Button buttonPositive = view.findViewById(R.id.btnPositive);
                    Button buttonNegative = view.findViewById(R.id.btnNegative);
                    buttonPositive.setOnClickListener(v -> {
                        scanQR();
                    });
                    builder.setView(view);
                    buttonNegative.setOnClickListener(v -> {
                        mAlertDialog.dismiss();
                        getNbrTicket();
                    });
                    mAlertDialog = builder.create();
                    mAlertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @Nullable
            @Override
            protected Map<String, String> getParams() {
                return hashMap;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }

    public Boolean compareTime(String time1, String time2, String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("kk:mm:ss", Locale.ENGLISH);
        try {
            Date timeOne = simpleDateFormat.parse(time1);
            Date timeTwo = simpleDateFormat.parse(time2);
            Date CompTime = simpleDateFormat.parse(time);
            assert CompTime != null;
            return CompTime.after(timeOne) && CompTime.before(timeTwo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Boolean compareDateTime(String time1, String time2, String time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);
        try {
            Date timeOne = simpleDateFormat.parse(time1);
            Date timeTwo = simpleDateFormat.parse(time2);
            Date CompTime = simpleDateFormat.parse(time);
            assert CompTime != null;
            return CompTime.after(timeOne) && CompTime.before(timeTwo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void getNbrTicket() {
        String url = "http://" + LoginActivity.getIpAdd() + "/memoir/server/scanner.php";
        Map<String, String> map = new HashMap<>();
        Bundle bundle = getIntent().getExtras();
        String resto = bundle.getString("nomRest");
        map.put("restoNbrTicket", resto);
        if (compareTime("06:00:00", "09:00:00", time)) {
            map.put("typeNbrTicket", "peti_dej");
        } else if (compareTime("11:00:00", "14:00:00", time)) {
            map.put("typeNbrTicket", "repas");
        } else if (compareTime("18:00:00", "21:00:00", "19:00:00")) {
            map.put("typeNbrTicket", "diner");
        }
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
            JSONObject jsonObject;
            try {
                jsonObject = new JSONObject(response);
                String responsePhp = jsonObject.getString("response");
                Log.d("tttttttttt", "getNbrTicket: " + responsePhp);
                mBinding.ahTextViewNbrTickets.setText(responsePhp);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(this, error.getMessage(), Toast.LENGTH_SHORT).show()) {
            @NonNull
            @Override
            protected Map<String, String> getParams() {
                return map;
            }
        };
        MySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }
}