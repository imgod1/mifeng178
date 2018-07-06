package com.imgod.kk;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.imgod.kk.utils.LogUtils;
import com.imgod.kk.utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.BitmapCallback;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

public class LoginActivity extends BaseActivity {

    private EditText etv_phone;
    private EditText etv_pwd;
    private EditText etv_img_code;
    private ImageView iv_code;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        etv_phone = (EditText) findViewById(R.id.etv_phone);
        etv_pwd = (EditText) findViewById(R.id.etv_pwd);
        etv_img_code = findViewById(R.id.etv_img_code);
        iv_code = findViewById(R.id.iv_code);
        etv_pwd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        iv_code.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                requestLoadImageCode();
            }
        });


        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
//        requestIndexHomePage(REFRESH_TYPE_GET_COOKIE);
        requestLoadImageCode();
    }


    private void attemptLogin() {

        // Reset errors.
        etv_phone.setError(null);
        etv_pwd.setError(null);
        etv_img_code.setError(null);
        // Store values at the time of the login attempt.
        String phone = etv_phone.getText().toString();
        String password = etv_pwd.getText().toString();
        String imgCode = etv_img_code.getText().toString();
        // Check for a valid phone address.
        if (TextUtils.isEmpty(phone)) {
            etv_phone.setError(getString(R.string.error_phone_required));
            etv_phone.requestFocus();
            return;
        } else if (!isPhoneValid(phone)) {
            etv_phone.setError(getString(R.string.error_invalid_phone));
            etv_phone.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etv_pwd.setError(getString(R.string.error_invalid_password));
            etv_pwd.requestFocus();
            return;
        } else if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            etv_pwd.setError(getString(R.string.error_invalid_password));
            etv_pwd.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(imgCode)) {
            etv_img_code.setError(getString(R.string.error_invalid_img_code));
            etv_img_code.requestFocus();
            return;
        } else if (!TextUtils.isEmpty(imgCode) && !isPasswordValid(imgCode)) {
            etv_img_code.setError(getString(R.string.error_invalid_img_code));
            etv_img_code.requestFocus();
            return;
        }

        //请求登录的逻辑
        requestLogin(phone, password, imgCode);
    }

    private boolean isPhoneValid(String phone) {
        if (TextUtils.isEmpty(phone)) {
            return false;
        } else {
            if (phone.length() == 11) {
                return true;
            }
        }
        return false;
    }

    private boolean isPasswordValid(String password) {
        return !TextUtils.isEmpty(password);
    }


    public static final String IMG_CODE_URL = "http://www.mf178.cn/login/captcha";

    private void requestLoadImageCode() {
        showProgressDialog();
        OkHttpUtils
                .get()//
                .url(IMG_CODE_URL)//
                .build()//
                .execute(new BitmapCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        hideProgressDialog();
                        ToastUtils.showToastShort(LoginActivity.this, "图片验证码加载失败,请重试");
                        iv_code.setImageResource(R.drawable.ic_img_load_error);
                    }

                    @Override
                    public void onResponse(Bitmap response, int id) {
                        hideProgressDialog();
                        iv_code.setImageBitmap(response);
                    }
                });
    }

    public static final String LOGIN_URL = "http://www.mf178.cn/login/index";

    private void requestLogin(String phone, String pwd, String imgCode) {
        showProgressDialog();
        OkHttpUtils
                .post()
                .url(LOGIN_URL)
                .addParams("username", phone)
                .addParams("password", pwd)
                .addParams("vcode", imgCode)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        hideProgressDialog();
                        LogUtils.e("onError", e.getMessage());
                        ToastUtils.showToastShort(LoginActivity.this, "登录失败:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        hideProgressDialog();
                        LogUtils.e("onResponse", response);
                        requestIndexHomePage(REFRESH_TYPE_GET_LOGIN_STATUS);
                    }
                });
    }


    public static final String INDEX_URL = "http://www.mf178.cn";

    private int REFRESH_TYPE_GET_COOKIE = 0x00;
    private int REFRESH_TYPE_GET_LOGIN_STATUS = 0x01;

    private void requestIndexHomePage(final int type) {
        showProgressDialog();
        OkHttpUtils
                .get()
                .url(INDEX_URL)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                        LogUtils.e("onError", e.getMessage());
                        hideProgressDialog();
                        ToastUtils.showToastShort(LoginActivity.this, "登录失败:" + e.getMessage());
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        hideProgressDialog();
                        LogUtils.e("onResponse", response);
                        if (type == REFRESH_TYPE_GET_LOGIN_STATUS) {
                            parseLoginResponse(response);
                        } else if (type == REFRESH_TYPE_GET_COOKIE) {
                            requestLoadImageCode();
                        }
                    }
                });
    }

    private void parseLoginResponse(String response) {
        if (response.contains("登录成功")) {
            ToastUtils.showToastShort(LoginActivity.this, "登录成功");
            MainActivity.actionStart(LoginActivity.this);
            finish();
        } else if (response.contains("验证码错误")) {
            ToastUtils.showToastShort(LoginActivity.this, "验证码错误");
            requestLoadImageCode();
        } else {
            ToastUtils.showToastShort(LoginActivity.this, "密码错误");
        }
    }

}

