package com.imgod.kk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.imgod.kk.app.Constants;
import com.imgod.kk.request_model.GetTaskModel;
import com.imgod.kk.response_model.BaseResponse;
import com.imgod.kk.response_model.GetTaskResponse;
import com.imgod.kk.utils.GsonUtil;
import com.imgod.kk.utils.LogUtils;
import com.imgod.kk.utils.MediaPlayUtils;
import com.imgod.kk.utils.ModelUtils;
import com.imgod.kk.utils.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;
import com.zhy.http.okhttp.request.RequestCall;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import okhttp3.Call;
import okhttp3.MediaType;

public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    public static final int RUSH_MODEL_NOT_RUSH = 0;//不抢购
    public static final int RUSH_MODEL_RUSH = 1;//抢购
    private int rush_model = RUSH_MODEL_NOT_RUSH;

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

    }

    private TextView tv_hint;
    private TextView tv_result;
    Toolbar toolbar;
    Button btn_action;

    private void initViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_hint = findViewById(R.id.tv_hint);
        tv_result = findViewById(R.id.tv_result);

        btn_action = findViewById(R.id.btn_action);
        btn_action.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String title = btn_action.getText().toString();
//
//                if (title.equals("开始")) {
//                    btn_action.setText("停止");
//                    rush_model = RUSH_MODEL_RUSH;
////                    requestPlatformOrderSize();
//                    requestGetTask(mRequestOperator, mRequestProvince, mRequestAmount);
//                } else {
//                    btn_action.setText("开始");
//                    rush_model = RUSH_MODEL_NOT_RUSH;
//                    requestPlatformOrderSizeCall.cancel();
//                }
                requestGetTask(mRequestOperator, mRequestProvince, mRequestAmount);

            }
        });
        selectId = R.id.action_30;
        selectTechphoneChargeName = getString(R.string.action_30);
        setToolBarTitle();
    }

    private void setToolBarTitle() {
        toolbar.setTitle("蜜蜂抢单: " + selectTechphoneChargeName);
    }


    private long loopTimes = 0;
    //获取平台上现在的订单量
    public static final String ORDER_LIST_URL = "http://www.mf178.cn/customer/order/mytasks";

    private RequestCall requestPlatformOrderSizeCall;


    private void requestPlatformOrderSize() {
        if (rush_model == RUSH_MODEL_RUSH) {
            loopTimes++;
            tv_hint.setText("正在为你进行第" + loopTimes + "次尝试");
            tv_result.setVisibility(View.GONE);

            btn_action.postDelayed(new Runnable() {
                @Override
                public void run() {
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
            }, 500);
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
                        requestGetTaskWarn(techphoneChargeName.replace("元", ""), "1");
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


    private RequestCall requestGetTaskWarnCall;
    //请求获取任务之前的确认弹窗
    private static final String GET_TASK_WARN_URL = "http://www.mf178.cn/customer/order/ajax";

    private void requestGetTaskWarn(final String amount, final String count) {
        requestGetTaskWarnCall = OkHttpUtils.get().url(GET_TASK_WARN_URL)
                .addParams("action", "get_tasks")
                .addParams("amount", amount)
                .build();
        requestGetTaskWarnCall.execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                if (!call.isCanceled()) {
                    requestGetTaskWarn(amount, count);
                }
            }

            @Override
            public void onResponse(String response, int id) {
            }
        });
    }


    private int mRequestOperator = Constants.OPERATOR_TYPE.DEFAULT;
    private String mRequestProvince = "";
    private int mRequestAmount = 50;
    private RequestCall requestGetTaskCall;

    private void requestGetTask(int operator, String prov, int amount) {
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


    private void parseGetTaskResponse(String response) {
        BaseResponse baseResponse = GsonUtil.GsonToBean(response, BaseResponse.class);
        if (baseResponse.getRet() == Constants.REQUEST_STATUS.SUCCESS) {
            GetTaskResponse getTaskResponse = GsonUtil.GsonToBean(response, GetTaskResponse.class);
            ToastUtils.showToastShort(mContext, "获取成功:" + getTaskResponse.getData().getMobile());
        } else {
            ToastUtils.showToastShort(mContext, "获取失败:" + baseResponse.getMsg());
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
        MediaPlayUtils.stopPlay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private int selectId;
    private String selectTechphoneChargeName;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == selectId) {
            return super.onOptionsItemSelected(item);
        }
        selectId = id;
        if (null != requestPlatformOrderSizeCall) {
            requestPlatformOrderSizeCall.cancel();
        }
        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_30:
                selectTechphoneChargeName = getString(R.string.action_30);
                break;
            case R.id.action_50:
                selectTechphoneChargeName = getString(R.string.action_50);
                break;
            case R.id.action_100:
                selectTechphoneChargeName = getString(R.string.action_100);
                break;
            case R.id.action_200:
                selectTechphoneChargeName = getString(R.string.action_200);
                break;
            case R.id.action_300:
                selectTechphoneChargeName = getString(R.string.action_300);
                break;
            case R.id.action_500:
                selectTechphoneChargeName = getString(R.string.action_500);
                break;
        }
        setToolBarTitle();
        loopTimes = 0;
        return super.onOptionsItemSelected(item);
    }
}
