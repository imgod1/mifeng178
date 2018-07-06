package com.imgod.kk;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * BaseActivity.java是液总汇的类。
 *
 * @author imgod1
 * @version 2.0.0 2018/5/26 15:40
 * @update imgod1 2018/5/26 15:40
 * @updateDes
 * @include {@link }
 * @used {@link }
 */
public class BaseActivity extends AppCompatActivity {
    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        if (null == mProgressDialog) {
            mProgressDialog = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
        }
        if (!mProgressDialog.isShowing()) {
            mProgressDialog.show();
        }
    }

    public void hideProgressDialog() {
        if (null != mProgressDialog && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
