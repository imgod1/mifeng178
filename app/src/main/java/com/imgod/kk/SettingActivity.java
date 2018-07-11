package com.imgod.kk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.imgod.kk.app.Constants;
import com.imgod.kk.utils.SPUtils;

public class SettingActivity extends BaseActivity implements View.OnClickListener {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, SettingActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        initViews();
        initEvent();
    }

    public static final String SP_MUSIC = "music";

    private void initEvent() {
        rlayout_music.setOnClickListener(this);
        tv_change_account.setOnClickListener(this);
        cbox_music.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SPUtils.getInstance().put(SP_MUSIC, isChecked);
            }
        });
    }

    private RelativeLayout rlayout_music;
    private CheckBox cbox_music;
    private TextView tv_change_account;
    Toolbar toolbar;

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("设置");
        setSupportActionBar(toolbar);
        rlayout_music = findViewById(R.id.rlayout_music);
        cbox_music = findViewById(R.id.cbox_music);
        tv_change_account = findViewById(R.id.tv_change_account);

        cbox_music.setChecked(SPUtils.getInstance().getBoolean(SettingActivity.SP_MUSIC, true));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rlayout_music:
                cbox_music.setChecked(!cbox_music.isChecked());
                break;
            case R.id.tv_change_account:
                Constants.TOKEN = "";
                LoginActivity.clearLoginData();
                MainActivity.actionStart(mContext, MainActivity.TYPE_RELOGIN);
                finish();
                break;
        }
    }
}
