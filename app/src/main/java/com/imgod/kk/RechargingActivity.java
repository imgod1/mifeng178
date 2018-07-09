package com.imgod.kk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class RechargingActivity extends BaseActivity {

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, RechargingActivity.class);
        context.startActivity(intent);
    }

    private SwipeRefreshLayout srlayout;
    private RecyclerView recylerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recharging);
        initViews();
        initEvents();
    }

    private void initViews() {
        srlayout = findViewById(R.id.srlayout);
        recylerview = findViewById(R.id.recylerview);

        recylerview.setLayoutManager(new LinearLayoutManager(mContext));

    }

    private void initEvents() {
        srlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

            }
        });
    }
}
