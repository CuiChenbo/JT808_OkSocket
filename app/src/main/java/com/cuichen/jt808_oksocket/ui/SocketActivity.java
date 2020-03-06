package com.cuichen.jt808_oksocket.ui;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.cuichen.jt808_oksocket.R;
import com.cuichen.jt808_oksocket.service.SocketService;


public class SocketActivity extends AppCompatActivity {

    private EditText etIp ,etPort,etContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket);
        etIp = findViewById(R.id.etIp);
        etPort = findViewById(R.id.etPort);
        etContent = findViewById(R.id.etContent);
    }

    public void lianjie(View view) {
        bindSocketService();
    }

    public void lianjie2(View view) {
       if (socketService != null)socketService.onlyConnect();
    }

    public void sendDatas(View view) {
        if (socketService == null)return;
        socketService.sendOrder(etContent.getText().toString().trim());
    }


    private ServiceConnection sc;
    public SocketService socketService;
    private void bindSocketService() {

        /*通过binder拿到service*/
        sc = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                SocketService.SocketBinder binder = (SocketService.SocketBinder) iBinder;
                socketService = binder.getService();

            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {

            }
        };


        Intent intent = new Intent(this, SocketService.class);
        intent.putExtra("ip",etIp.getText().toString().trim());
        intent.putExtra("port",etPort.getText().toString().trim());
        startService(intent);
        bindService(intent, sc, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SocketService.class));
    }
}
