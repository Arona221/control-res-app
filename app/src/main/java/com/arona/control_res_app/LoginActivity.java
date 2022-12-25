package com.arona.control_res_app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.arona.control_res_app.databinding.ActivityLoginBinding;
import com.arona.control_res_app.functions.MySingleton;
import com.arona.control_res_app.functions.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
public class LoginActivity extends AppCompatActivity {
    private static String ipAdd;

    public static String getIpAdd() {
        return ipAdd;
    }

    public static void setIpAdd(String ipAdd) {
        LoginActivity.ipAdd = ipAdd;
    }
    ActivityLoginBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setIpAdd("192.168.56.1");
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        setContentView(mBinding.getRoot());
        mBinding.alBtnConnect.setOnClickListener(v -> {
            {
                String url = "http://"+LoginActivity.getIpAdd()+"/memoir/server/logControler.php";
                String string_NumCard_or_email = (String) Objects.requireNonNull(mBinding.alId.getText()).toString();
                String string_Password = (String) Objects.requireNonNull(mBinding.alPassword.getText()).toString();
                if (string_Password.isEmpty()) {
                    mBinding.alId.setError("vide");
                    mBinding.alId.setFocusable(true);
                    new Tools(LoginActivity.this).displayAlert("erreur", "Remplissez les champs !");
                }
                else {
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, url, response -> {
                        JSONObject jsonObject;
                        JSONObject jsonObject1 = null;
                        try {
                            jsonObject = new JSONObject(response);
                            String message = jsonObject.getString("message");
                            Log.d("eeeeeeeeeeeeeee", "onCreate: "+jsonObject.toString());
                            if (message.equals("message_error")) {
                                new Tools(LoginActivity.this).displayAlert("erreur", "Verifiez vos informations");
                            } else {
                                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                Bundle bundle = new Bundle();
                                jsonObject1 = jsonObject.getJSONObject("content");
                                startActivity(intent);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }, error -> Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show()) {
                        @NonNull
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> hashMap = new HashMap<>();
                            hashMap.put("idenifiant", string_NumCard_or_email);
                            hashMap.put("mot_passe", string_Password);
                            return hashMap;
                        }
                    };
                    MySingleton.getInstance(this).addToRequestQueue(stringRequest);
                }
            }
        });
    }
}