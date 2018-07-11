package com.imgod.kk;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.imgod.kk.app.Constants;
import com.imgod.kk.request_model.BaseRequestModel;
import com.imgod.kk.response_model.BaseResponse;
import com.imgod.kk.response_model.RechargingResponse;
import com.imgod.kk.utils.BitmapUtils;
import com.imgod.kk.utils.DateUtils;
import com.imgod.kk.utils.GsonUtil;
import com.imgod.kk.utils.LogUtils;
import com.imgod.kk.utils.ModelUtils;
import com.imgod.kk.utils.ToastUtils;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.adapter.recyclerview.wrapper.EmptyWrapper;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

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

    private List<RechargingResponse.DataBean> orderList = new ArrayList<>();
    Toolbar toolbar;

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("未到账订单");
        setSupportActionBar(toolbar);

        srlayout = findViewById(R.id.srlayout);
        recylerview = findViewById(R.id.recylerview);
        recylerview.setLayoutManager(new LinearLayoutManager(mContext));
        CommonAdapter<RechargingResponse.DataBean> commonAdapter = new CommonAdapter<RechargingResponse.DataBean>(mContext, R.layout.item_recharge_order, orderList) {
            @Override
            protected void convert(ViewHolder holder, RechargingResponse.DataBean dataBean, final int position) {
                holder.setText(R.id.tv_id, "订单号:" + dataBean.getId());
                holder.setText(R.id.tv_phone_number, dataBean.getMobile());
                holder.setText(R.id.tv_province, dataBean.getProv());
                holder.setText(R.id.tv_amount, "" + dataBean.getAmount());
                holder.setText(R.id.tv_date, DateUtils.getFormatDateTimeFromMillSecons(dataBean.getCreate_time() * 1000));

                TextView tv_change_photo = holder.getView(R.id.tv_action_1);
                TextView tv_change_2_failed = holder.getView(R.id.tv_action_2);
                tv_change_photo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionPosition = position;
                        //去选择图片
                    }
                });

                tv_change_2_failed.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        actionPosition = position;
                        showAlertDialog();
                    }
                });

            }
        };


        EmptyWrapper emptyWrapper = new EmptyWrapper(commonAdapter);
        emptyWrapper.setEmptyView(R.layout.layout_list_empty);


        recylerview.setAdapter(emptyWrapper);
    }

    private void initEvents() {
        srlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestOrderHistroy();
            }
        });

        srlayout.setRefreshing(true);
        requestOrderHistroy();
    }


    private void choosePhotoWithPermissionCheck() {
        //第二个参数是需要申请的权限
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //权限还没有授予，需要在这里写申请权限的代码
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_PERMISSION_CODE_WRITE_STORAGE);
        } else {
            //权限已经被授予，在这里直接写要执行的相应方法即可
            choosePhoto();
        }
    }

    private static final int REQUEST_PERMISSION_CODE_WRITE_STORAGE = 0x01;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                choosePhoto();
            } else {
                // Permission Denied
                ToastUtils.showToastShort(mContext, "选择照片的权限被拒绝.无法选择");
            }
        }
    }

    private void choosePhoto() {
        /**
         * 打开选择图片的界面
         */
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");//相片类型
        startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
    }

    private static final int REQUEST_CODE_PICK_IMAGE = 0x00;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                LogUtils.e(TAG, "onActivityResult: " + uri.getEncodedPath());
                try {
                    Bitmap bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
                    String voucher = BitmapUtils.bitmapToBase64WithTitle(bit, 40);
                    LogUtils.e(TAG, "onActivityResult: " + voucher);
                    requestReportTask(Constants.RECHARGE_TYPE.SUCCESS, voucher);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    ToastUtils.showToastShort(mContext, "图片不存在");
                }
            } else {
                ToastUtils.showToastShort(mContext, "取消选择图片");
            }
        }
    }


    private int actionPosition;

    AlertDialog mAlertDialog;

    private void showAlertDialog() {
        if (null == mAlertDialog) {
            mAlertDialog = new AlertDialog.Builder(mContext)
                    .setTitle("警告")
                    .setMessage("请确认此次充值的确失败,因用户以及第三方充值平台造成的损失,蜜蜂平台以及软件作者概不负责")
                    .setCancelable(false)
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                        }
                    })
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                            requestReportTask(Constants.RECHARGE_TYPE.FAILED, "");
                        }
                    })
                    .create();
        }

        if (!mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
        }
    }


//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu_recharge, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_pick_photo:
                choosePhotoWithPermissionCheck();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //上报充值结果
    private void requestReportTask(int result, String voucher) {
        RechargingResponse.DataBean dataBean = orderList.get(actionPosition);
        MainActivity.requestReportTask(dataBean.getId(), dataBean.getMobile(), result, voucher, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showToastShort(mContext, e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.e(TAG, "requestGetTask onResponse: " + response);
                parseReportResponse(response);
            }
        });
    }

    //解析上报充值结果
    private void parseReportResponse(String response) {
        BaseResponse baseResponse = GsonUtil.GsonToBean(response, BaseResponse.class);
        if (baseResponse.getRet() == Constants.REQUEST_STATUS.SUCCESS) {
            ToastUtils.showToastShort(mContext, baseResponse.getMsg());
            srlayout.setRefreshing(true);
            requestOrderHistroy();
        } else {
            ToastUtils.showToastShort(mContext, baseResponse.getMsg());
        }
    }


    private static final String TAG = "RechargingActivity";
    private RequestCall requestOrderHistroyCall;

    //获取历史任务
    private void requestOrderHistroy() {
        BaseRequestModel baseRequestModel = new BaseRequestModel();
        baseRequestModel.setAction(API.ACTION_ORDER);

        ModelUtils.initModelSign(baseRequestModel);

        requestOrderHistroyCall = OkHttpUtils.postString().url(API.OPEN_API)
                .content(GsonUtil.GsonString(baseRequestModel))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build();
        requestOrderHistroyCall.execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showToastShort(mContext, e.getMessage());
                srlayout.setRefreshing(false);
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.e(TAG, "requestGetTask onResponse: " + response);
                srlayout.setRefreshing(false);
                parseOrderHirstroyResponse(response);
            }
        });
    }

    private void parseOrderHirstroyResponse(String response) {
        BaseResponse baseResponse = GsonUtil.GsonToBean(response, BaseResponse.class);
        orderList.clear();
        if (baseResponse.getRet() == Constants.REQUEST_STATUS.SUCCESS) {
            RechargingResponse rechargingResponse = GsonUtil.GsonToBean(response, RechargingResponse.class);
            orderList.addAll(rechargingResponse.getData());
        } else {
            ToastUtils.showToastShort(mContext, baseResponse.getMsg());
        }
        recylerview.getAdapter().notifyDataSetChanged();
    }
}
