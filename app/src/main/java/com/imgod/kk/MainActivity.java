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
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.imgod.kk.app.Constants;
import com.imgod.kk.request_model.GetTaskModel;
import com.imgod.kk.request_model.ReportModel;
import com.imgod.kk.response_model.BaseResponse;
import com.imgod.kk.response_model.GetTaskResponse;
import com.imgod.kk.utils.BitmapUtils;
import com.imgod.kk.utils.DateUtils;
import com.imgod.kk.utils.GsonUtil;
import com.imgod.kk.utils.LogUtils;
import com.imgod.kk.utils.MediaPlayUtils;
import com.imgod.kk.utils.ModelUtils;
import com.imgod.kk.utils.OperatorUtils;
import com.imgod.kk.utils.SPUtils;
import com.imgod.kk.utils.ScreenUtils;
import com.imgod.kk.utils.ToastUtils;
import com.imgod.kk.utils.VibratorUtils;
import com.imgod.kk.views.RowView;
import com.zhy.adapter.recyclerview.CommonAdapter;
import com.zhy.adapter.recyclerview.MultiItemTypeAdapter;
import com.zhy.adapter.recyclerview.base.ViewHolder;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.Arrays;

import okhttp3.Call;
import okhttp3.MediaType;

public class MainActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    public static final int RUSH_MODEL_NOT_RUSH = 0;//不抢购
    public static final int RUSH_MODEL_RUSH = 1;//抢购
    private int rush_model = RUSH_MODEL_NOT_RUSH;

    public static final int TYPE_NORMAL = 0x00;//正常登陆
    public static final int TYPE_RELOGIN = 0x01;//进来跳转到登陆页
    private int come_type;

    public static void actionStart(Context context) {
        actionStart(context, TYPE_NORMAL);
    }

    public static void actionStart(Context context, int come_type) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("come_type", come_type);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        initEvent();
    }

    Toolbar toolbar;
    private RowView rview_operator;
    private RowView rview_province;
    private RowView rview_amount;

    private View item_order;
    private TextView tv_phone_number;
    private TextView tv_province;
    private TextView tv_amount;
    private TextView tv_id;
    private TextView tv_date;
    private TextView tv_action_1;
    private TextView tv_action_2;


    private ProgressBar progress_bar;

    private TextView tv_get_mobile_number;

    private MainHandler mMainHandler;

    private void initViews() {
        mMainHandler = new MainHandler(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        rview_operator = findViewById(R.id.rview_operator);
        rview_province = findViewById(R.id.rview_province);
        rview_amount = findViewById(R.id.rview_amount);

        item_order = findViewById(R.id.item_order);
        tv_phone_number = findViewById(R.id.tv_phone_number);
        tv_province = findViewById(R.id.tv_province);
        tv_amount = findViewById(R.id.tv_amount);
        tv_id = findViewById(R.id.tv_id);
        tv_date = findViewById(R.id.tv_date);
        tv_action_1 = findViewById(R.id.tv_action_1);
        tv_action_2 = findViewById(R.id.tv_action_2);

        progress_bar = findViewById(R.id.progress_bar);
        tv_get_mobile_number = findViewById(R.id.tv_get_mobile_number);
        rview_operator.setTitle("运营商");
        rview_province.setTitle("省份");
        rview_amount.setTitle("面额");
        setRowViewContent();
    }

    private void initEvent() {
        rview_operator.setOnClickListener(this);
        rview_province.setOnClickListener(this);
        rview_amount.setOnClickListener(this);
        tv_get_mobile_number.setOnClickListener(this);
    }


    private long loopTimes = 0;
    //获取平台上现在的订单量
    public static final String ORDER_LIST_URL = "http://www.mf178.cn/customer/order/mytasks";

    private RequestCall requestPlatformOrderSizeCall;


    private void requestPlatformOrderSize() {
        if (rush_model == RUSH_MODEL_RUSH) {
            loopTimes++;

            requestPlatformOrderSizeCall = OkHttpUtils.get().url(ORDER_LIST_URL).build();
            requestPlatformOrderSizeCall.execute(new StringCallback() {
                @Override
                public void onError(Call call, Exception e, int id) {
                    if (!call.isCanceled()) {
                        requestPlatformOrderSize();
                    }
                }

                @Override
                public void onResponse(String response, int id) {//
                    if (response.contains("平台暂未订单，请稍后再试")) {
                        ToastUtils.showToastShort(MainActivity.this, "平台暂未订单，请稍后再试");
                        requestPlatformOrderSize();
                    } else {
                        parseOrderSizeResponse(response);
                    }
                }
            });
        }
    }


    /**
     * 解析网络请求得到的数据
     */
    private void parseOrderSizeResponse(String content) {
        Document document = null;
        try {
            document = Jsoup.parse(content);
            Elements elements = document.getElementsByClass("table table-striped table-bordered table-advance table-hover text-center");
            Element element = elements.select("tr").first();
            Elements tdElements = element.select("td");
            for (int i = 0; i < tdElements.size(); i++) {
                Element tempElement = tdElements.get(i);
                String techphoneChargeName = getTelephoneChargeName(tempElement.text());
                int orderNum = getTelephoneChargeOrderNum(tempElement.getElementsByClass("text-success").get(0).text());
                if (techphoneChargeName.equals(selectTechphoneChargeName)) {
                    LogUtils.e(TAG, techphoneChargeName);
                    LogUtils.e(TAG, "数量:" + orderNum);

                    if (orderNum > 0) {
                        //如果该选项还有剩余订单的话,那这个时候应该先发起抢订单的操作
                        LogUtils.e(TAG, techphoneChargeName + "话费单有库存,请及时去抢单");
                    } else {
                        //如果没有数量 那就应该执行刷新操作了
                        requestPlatformOrderSize();
                    }
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void setRowViewContent() {
        rview_operator.setContent(OperatorUtils.getOperatorNameByType(mRequestOperator));
        if (TextUtils.isEmpty(mRequestProvince)) {
            rview_province.setContent("不限");
        } else {
            rview_province.setContent(mRequestProvince);
        }
        rview_amount.setContent("" + mRequestAmount);
    }


    private int mRequestOperator = Constants.OPERATOR_TYPE.DEFAULT;
    private String mRequestProvince = "";
    private int mRequestAmount = 50;
    private RequestCall requestGetTaskCall;

    //获取任务
    private void requestGetTask(int operator, String prov, int amount) {
        if (rush_model != RUSH_MODEL_RUSH) {
            return;
        }
        GetTaskModel getTaskModel = new GetTaskModel();
        getTaskModel.setAction(API.ACTION_GET);
        getTaskModel.setOperator(operator);
        getTaskModel.setProv(prov);
        getTaskModel.setAmount(amount);

        ModelUtils.initModelSign(getTaskModel);

        requestGetTaskCall = OkHttpUtils.postString().url(API.OPEN_API)
                .content(GsonUtil.GsonString(getTaskModel))
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build();
        requestGetTaskCall.execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                ToastUtils.showToastShort(MainActivity.this, e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                LogUtils.e(TAG, "requestGetTask onResponse: " + response);
                parseGetTaskResponse(response);
            }
        });
    }

    private GetTaskResponse.DataBean orderDataBean;

    //解析获取任务的结果
    private void parseGetTaskResponse(String response) {
        BaseResponse baseResponse = GsonUtil.GsonToBean(response, BaseResponse.class);
        if (baseResponse.getRet() == Constants.REQUEST_STATUS.SUCCESS) {
            GetTaskResponse getTaskResponse = GsonUtil.GsonToBean(response, GetTaskResponse.class);
            orderDataBean = getTaskResponse.getData();
            tv_get_mobile_number.setText("获取号码");
            item_order.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
            tv_phone_number.setText(orderDataBean.getMobile());
            tv_province.setText(orderDataBean.getProv());
            tv_amount.setText(orderDataBean.getAmount());
            tv_id.setText("订单号:" + orderDataBean.getId());
            if (!TextUtils.isEmpty(orderDataBean.getTimeout())) {
                tv_date.setText(DateUtils.getFormatDateTimeFromMillSecons(Long.parseLong(orderDataBean.getTimeout()) * 1000));
            }
            tv_action_1.setText("我已充值");
            tv_action_2.setText("我没充值");
            tv_action_1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ToastUtils.showToastShort(mContext, "选择凭证");
                    choosePhotoWithPermissionCheck();
                }
            });

            tv_action_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showAlertDialog();
                }
            });
            showGetOrderSuccessDialog();
        } else {
            String msg = baseResponse.getMsg();
            if (!TextUtils.isEmpty(msg)) {
                if (msg.contains("平台暂无订单") || msg.contains("稍后再试")) {
                    item_order.setVisibility(View.GONE);
                    progress_bar.setVisibility(View.VISIBLE);
                    tv_get_mobile_number.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            requestGetTask(mRequestOperator, mRequestProvince, mRequestAmount);
                        }
                    }, 5000);
                } else {
                    item_order.setVisibility(View.GONE);
                    progress_bar.setVisibility(View.GONE);
                    rush_model = RUSH_MODEL_NOT_RUSH;
                    tv_get_mobile_number.setText("获取号码");
                    ToastUtils.showToastShort(mContext, baseResponse.getMsg());
                }
            } else {
                ToastUtils.showToastShort(mContext, "蜜蜂服务器异常");
            }
        }
    }


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
                            requestReportTask(orderDataBean.getId(), orderDataBean.getMobile(), Constants.RECHARGE_TYPE.FAILED, "");
                        }
                    })
                    .create();
        }

        if (!mAlertDialog.isShowing()) {
            mAlertDialog.show();
        }
    }


    private AlertDialog getOrderSuccessDialog;

    private void showGetOrderSuccessDialog() {
        if (null == getOrderSuccessDialog) {
            getOrderSuccessDialog = new AlertDialog.Builder(mContext)
                    .setTitle("提示")
                    .setMessage("成功获取到一条订单,请及时充值")
                    .setCancelable(false)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            getOrderSuccessDialog.dismiss();
                        }
                    })
                    .create();

            getOrderSuccessDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    MediaPlayUtils.stopPlay();
                    VibratorUtils.stopVibrator();
                }
            });
        }

        if (!getOrderSuccessDialog.isShowing()) {
            getOrderSuccessDialog.show();
            if (SPUtils.getInstance().getBoolean(SettingActivity.SP_MUSIC, true)) {
                MediaPlayUtils.playSound(mContext, "memeda.wav");
            }

            if (SPUtils.getInstance().getBoolean(SettingActivity.SP_VIBRATOR, true)) {
                VibratorUtils.startVibrator(MainActivity.this);
            }
        }
    }


    private static RequestCall requestReportCall;

    //上报充值结果
    private void requestReportTask(String id, String mobile, int result, String voucher) {
        requestReportTask(id, mobile, result, voucher, new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                hideProgressDialog();
                ToastUtils.showToastShort(mContext, e.getMessage());
            }

            @Override
            public void onResponse(String response, int id) {
                hideProgressDialog();
                LogUtils.e(TAG, "requestGetTask onResponse: " + response);
                parseReportResponse(response);
            }
        });
    }

    //上报充值结果
    public static void requestReportTask(String id, String mobile, int result, String voucher, StringCallback stringCallback) {
        ReportModel reportModel = new ReportModel();
        reportModel.setAction(API.ACTION_REPORT);
        reportModel.setId(id);
        reportModel.setMobile(mobile);
        reportModel.setResult(result);
        reportModel.setVoucher(voucher);
        ModelUtils.initModelSign(reportModel);

        String requestContent = GsonUtil.GsonString(reportModel);
        requestReportCall = OkHttpUtils.postString().url(API.OPEN_API)
                .content(requestContent)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build();
        requestReportCall.execute(stringCallback);
    }


    //解析上报充值结果
    private void parseReportResponse(String response) {
        BaseResponse baseResponse = GsonUtil.GsonToBean(response, BaseResponse.class);
        if (baseResponse.getRet() == Constants.REQUEST_STATUS.SUCCESS) {
            ToastUtils.showToastShort(mContext, baseResponse.getMsg());
            item_order.setVisibility(View.GONE);
        } else {
            ToastUtils.showToastShort(mContext, baseResponse.getMsg());
        }
    }

    private String getTelephoneChargeName(String text) {
        if (!TextUtils.isEmpty(text)) {
            int startPosition = 0;
            int endPosition = text.indexOf("元") + 1;
            return text.substring(startPosition, endPosition);
        }

        return null;
    }

    private int getTelephoneChargeOrderNum(String text) {
        if (!TextUtils.isEmpty(text)) {
            return Integer.parseInt(text.replace("单", ""));
        }
        return 0;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mMainHandler) {
            mMainHandler.removeCallbacksAndMessages(null);
        }
        MediaPlayUtils.stopPlay();
        VibratorUtils.stopVibrator();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private String selectTechphoneChargeName;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_histroy:
                RechargingActivity.actionStart(mContext);
                break;
            case R.id.action_about:
                AboutActivity.actionStart(MainActivity.this);
                break;
            case R.id.action_setting:
                SettingActivity.actionStart(mContext);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private BottomSheetDialog operatorDialog;
    private View operatorDialogView;
    private TextView tv_default;
    private TextView tv_mobile;
    private TextView tv_unicom;
    private TextView tv_telecom;

    //选择运营商的对话框
    private void showSelectOperatorDialog() {
        if (null == operatorDialog) {
            operatorDialog = new BottomSheetDialog(mContext);
            operatorDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_operator, null, false);
            tv_default = operatorDialogView.findViewById(R.id.tv_default);
            tv_mobile = operatorDialogView.findViewById(R.id.tv_mobile);
            tv_unicom = operatorDialogView.findViewById(R.id.tv_unicom);
            tv_telecom = operatorDialogView.findViewById(R.id.tv_telecom);

            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String) v.getTag();
                    if (!TextUtils.isEmpty(tag)) {
                        mRequestOperator = Integer.parseInt(tag);
                        operatorDialog.dismiss();
                        setRowViewContent();
                    }
                }
            };

            tv_default.setOnClickListener(onClickListener);
            tv_mobile.setOnClickListener(onClickListener);
            tv_unicom.setOnClickListener(onClickListener);
            tv_telecom.setOnClickListener(onClickListener);

            operatorDialog.setContentView(operatorDialogView);
        }
        if (null != operatorDialog && !operatorDialog.isShowing()) {
            operatorDialog.show();
        }
    }


    private BottomSheetDialog amountDialog;
    private View amountDialogView;
    private TextView tv_30;
    private TextView tv_50;
    private TextView tv_100;
    private TextView tv_200;
    private TextView tv_300;
    private TextView tv_500;

    //选择话费面额的对话框
    private void showSelectAmountDialog() {
        if (null == amountDialog) {
            amountDialog = new BottomSheetDialog(mContext);
            amountDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_amount, null, false);
            tv_30 = amountDialogView.findViewById(R.id.tv_30);
            tv_50 = amountDialogView.findViewById(R.id.tv_50);
            tv_100 = amountDialogView.findViewById(R.id.tv_100);
            tv_200 = amountDialogView.findViewById(R.id.tv_200);
            tv_300 = amountDialogView.findViewById(R.id.tv_300);
            tv_500 = amountDialogView.findViewById(R.id.tv_500);


            View.OnClickListener onClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = (String) v.getTag();
                    if (!TextUtils.isEmpty(tag)) {
                        mRequestAmount = Integer.parseInt(tag);
                        amountDialog.dismiss();
                        setRowViewContent();
                    }
                }
            };

            tv_30.setOnClickListener(onClickListener);
            tv_50.setOnClickListener(onClickListener);
            tv_100.setOnClickListener(onClickListener);
            tv_200.setOnClickListener(onClickListener);
            tv_300.setOnClickListener(onClickListener);
            tv_500.setOnClickListener(onClickListener);

            amountDialog.setContentView(amountDialogView);
        }
        if (null != amountDialog && !amountDialog.isShowing()) {
            amountDialog.show();
        }
    }


    private BottomSheetDialog provinceDialog;
    private View provinceDialogView;
    private RecyclerView dialog_recylerview;

    //选择省份的对话框
    private void showSelectProvinceDialog() {
        if (null == provinceDialog) {
            provinceDialog = new BottomSheetDialog(mContext);
            provinceDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_list, null, false);
            dialog_recylerview = provinceDialogView.findViewById(R.id.dialog_recylerview);
            dialog_recylerview.setLayoutManager(new LinearLayoutManager(mContext));
            CommonAdapter<String> commonAdapter = new CommonAdapter<String>(mContext, R.layout.item_province, Arrays.asList(Constants.PROVINCE_ARRAY)) {
                @Override
                protected void convert(ViewHolder holder, String s, int position) {
                    holder.setText(R.id.tv_title, s);
                }
            };
            commonAdapter.setOnItemClickListener(new MultiItemTypeAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, RecyclerView.ViewHolder holder, int position) {
                    String result = Constants.PROVINCE_ARRAY[position];
                    if (result.equals("不限")) {
                        mRequestProvince = "";
                    } else {
                        mRequestProvince = result;
                    }
                    provinceDialog.dismiss();
                    setRowViewContent();
                }

                @Override
                public boolean onItemLongClick(View view, RecyclerView.ViewHolder holder, int position) {
                    return false;
                }
            });
            dialog_recylerview.setAdapter(commonAdapter);

            provinceDialog.setContentView(provinceDialogView);

            //设置对话框的高度
            View parent = (View) provinceDialogView.getParent();
            CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
            params.height = ScreenUtils.getScreenHeight(mContext) / 2;
            parent.setLayoutParams(params);

        }
        if (null != provinceDialog && !provinceDialog.isShowing()) {
            provinceDialog.show();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rview_operator:
                showSelectOperatorDialog();
                break;
            case R.id.rview_province:
                showSelectProvinceDialog();
                break;
            case R.id.rview_amount:
                showSelectAmountDialog();
                break;
            case R.id.tv_get_mobile_number:
                if (rush_model == RUSH_MODEL_NOT_RUSH) {
                    rush_model = RUSH_MODEL_RUSH;
                    tv_get_mobile_number.setText("正在获取号码...");
                    progress_bar.setVisibility(View.VISIBLE);
                    requestGetTask(mRequestOperator, mRequestProvince, mRequestAmount);
                } else {
                    rush_model = RUSH_MODEL_NOT_RUSH;
                    tv_get_mobile_number.setText("获取号码");
                    progress_bar.setVisibility(View.GONE);
                    requestGetTaskCall.cancel();
                }
                break;
        }
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


    private Uri photoUri;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                photoUri = data.getData();
                LogUtils.e(TAG, "onActivityResult: " + photoUri.getEncodedPath());
                //展示加载中的进度
                showProgressDialog();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap bit = null;
                        try {
                            bit = BitmapFactory.decodeStream(getContentResolver().openInputStream(photoUri));
                            String voucher = BitmapUtils.bitmapToBase64WithTitle(bit, 40);
                            LogUtils.e(TAG, "onActivityResult: " + voucher);

                            //发送给Handler
                            Message message = Message.obtain();
                            message.what = WHAT_DECODE_SUCCESS;
                            message.obj = voucher;
                            mMainHandler.sendMessage(message);

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            //发送给Handler
                            mMainHandler.sendEmptyMessage(WHAT_DECODE_FAILED);
                        }
                    }
                }).start();
//                    requestReportTask(orderDataBean.getId(), orderDataBean.getMobile(), Constants.RECHARGE_TYPE.SUCCESS, voucher);
            } else {
                ToastUtils.showToastShort(mContext, "取消选择图片");
            }
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int come_type = intent.getIntExtra("come_type", TYPE_NORMAL);
        if (come_type == TYPE_RELOGIN) {
            LoginActivity.actionStart(mContext);
            finish();
        }
    }

    public static final int WHAT_DECODE_SUCCESS = 0x00;
    public static final int WHAT_DECODE_FAILED = 0x01;

    public void handleMessage(Message msg) {
        switch (msg.what) {
            case WHAT_DECODE_SUCCESS:
                String voucher = (String) msg.obj;
                requestReportTask(orderDataBean.getId(), orderDataBean.getMobile(), Constants.RECHARGE_TYPE.SUCCESS, voucher);
                break;
            case WHAT_DECODE_FAILED:
                hideProgressDialog();
                ToastUtils.showToastShort(mContext, "图片不存在");
                break;
        }
    }

    @Override
    public void onBackPressed() {
        exit();
    }

    private long exitTime;

    public void exit() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            ToastUtils.showToastShort(mContext, "再按一次退出程序");
            exitTime = System.currentTimeMillis();
        } else {
            finish();
        }
    }

    public static class MainHandler extends Handler {
        private WeakReference<MainActivity> mMainActivityWeakReference;

        public MainHandler(MainActivity mainActivity) {
            mMainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity mainActivity = mMainActivityWeakReference.get();
            if (null != mainActivity) {
                mainActivity.handleMessage(msg);
            }
        }
    }
}
