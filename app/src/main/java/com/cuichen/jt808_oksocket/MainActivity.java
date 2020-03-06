package com.cuichen.jt808_oksocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import com.cuichen.jt808_oksocket.ui.Jt808OkSocketActivity;
import com.cuichen.jt808_oksocket.ui.OkSocketActivity;
import com.cuichen.jt808_oksocket.ui.SocketActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        requestPermission();
    }

    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.READ_PHONE_STATE},
                    10);//自定义的code
        }

    }


    public void openSocketAct(View view) {
        startActivity(new Intent(this, SocketActivity.class));
    }

    public void OkSocket(View view) {
        startActivity(new Intent(this, OkSocketActivity.class));
    }
    public void OkSocket_Release(View view) {
        startActivity(new Intent(this, Jt808OkSocketActivity.class));
    }
}
