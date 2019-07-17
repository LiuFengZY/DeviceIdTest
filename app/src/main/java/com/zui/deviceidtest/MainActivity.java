package com.zui.deviceidtest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.zui.opendeviceidlibrary.OpenDeviceId;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private Button mBOAID;
    private Button mIsSupport;
    private Button mUDID;

    private Button mVAID;
    private Button mAAID;

    private OpenDeviceId odid;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mBOAID = findViewById(R.id.button_oaid);
        mBOAID.setOnClickListener(this);

        mIsSupport = findViewById(R.id.button_issupport);
        mIsSupport.setOnClickListener(this);

        mUDID = findViewById(R.id.button_udid);
        mUDID.setOnClickListener(this);

        mVAID = findViewById(R.id.button_vaid);
        mVAID.setOnClickListener(this);

        mAAID = findViewById(R.id.button_aaid);
        mAAID.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        odid = new OpenDeviceId(this.getApplicationContext());
        OpenDeviceId.testJar();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mBOAID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call getOAID:" + odid.getOAID());
            } else {
                odid = new OpenDeviceId(this.getApplicationContext());
                Log.i("liufeng", "Clinet ReStart call getOAID:" + odid.getOAID());
            }
        } else if (v.getId() == mIsSupport.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call isSupport:" + odid.isSupported());
            } else {
                Log.i("liufeng", "is null, not support");
            }
        } else if (v.getId() == mUDID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call gtUDID:" + odid.getUDID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getUDID failed.");
            }
        } else if (v.getId() == mVAID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call getVAID:" + odid.getVAID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getVAID failed.");
            }
        } else if (v.getId() == mAAID.getId()) {
            if (odid != null) {
                Log.i("liufeng", "Clinet call getAAID:" + odid.getAAID());
            } else {
                Log.i("liufeng", "Clinet ReStart call getAAID failed.");
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (odid != null) {
            odid.shutdown();
        }
    }
}
