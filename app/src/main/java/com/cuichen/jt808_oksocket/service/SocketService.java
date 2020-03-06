package com.cuichen.jt808_oksocket.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SocketService extends Service {
    /*socket*/
    private Socket socket;
    /*连接线程*/
    private OutputStream outputStream;

    private SocketBinder sockerBinder = new SocketBinder();
    private String ip;
    private String port;

    private ExecutorService mThreadPool;

    /*默认重连*/
    private boolean isReConnect = true;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Timer mTimer;


    @Override
    public IBinder onBind(Intent intent) {
        return sockerBinder;
    }


    public class SocketBinder extends Binder {

        /*返回SocketService 在需要的地方可以通过ServiceConnection获取到SocketService  */
        public SocketService getService() {
            return SocketService.this;
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        /*拿到传递过来的ip和端口号*/
        ip = intent.getStringExtra("ip");
        port = intent.getStringExtra("port");
        mThreadPool = Executors.newCachedThreadPool();
        initSocket();
        return super.onStartCommand(intent, flags, startId);
    }

    public void initSocket() {
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {
                if (socket == null) socket = new Socket();
                try {
                    socket.connect(new InetSocketAddress(ip, Integer.valueOf(port)), 3 * 1000);
                    if (socket.isConnected()) {
                        toastMsg("socket连接成功");
                        ReadMessage();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (e instanceof SocketTimeoutException) {
                        toastMsg("连接超时，正在重连");

                        releaseSocket();

                    } else if (e instanceof NoRouteToHostException) {
                        toastMsg("该地址不存在，请检查");
                        stopSelf();

                    } else if (e instanceof ConnectException) {
                        toastMsg("连接异常或被拒绝，请检查");
                        stopSelf();

                    } else if (e instanceof SocketException){
                       if (TextUtils.equals(e.getMessage(),"already connected"))toastMsg("当前已连接，请勿再次连接");
                    }
                }
            }
        });

    }


    /*因为Toast是要运行在主线程的   所以需要到主线程哪里去显示toast*/
    private void toastMsg(final String msg) {

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /*发送数据*/
    public void sendOrder(final String order) {
        if (socket != null && socket.isConnected()) {
            /*发送指令*/
            mThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        outputStream = socket.getOutputStream();
                        if (outputStream != null) {
                            outputStream.write((order).getBytes("gbk"));
                            outputStream.flush();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        toastMsg("发送数据异常"+e.getMessage());
                    }

                }
            });

        } else {
            toastMsg("socket连接错误,请重试");
        }
    }

    private void ReadMessage() {
        try {
//            String data;
//            InputStream inputStream = socket.getInputStream();
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
//            Log.i("ccb", "开始接收数据");
//            /*
//            通过socket测试工具在电脑上发送消息，Android真机可以收到响应BufferedReader.ready()返回true，但是readline却一直阻塞。
//            原因：readline()只有在遇到换行符的时候才会结束，因为发消息的时候加一个换行符即可。
//             */
//            while (socket.isConnected() && (data = bufferedReader.readLine()) != null) {
//                Log.i("ccb", "接收到数据：" + data);
//                toastMsg("接收到数据：" + data);
//            }

//            byte[] getData = readInputStream(inputStream);
//            inputStream.read(getData);
//            String str = new String(getData);
//            System.out.println ("打印内容："+str);


            InputStream is = socket.getInputStream();
             DataInputStream input = new DataInputStream(is);
             byte[] b = new byte[1024];

             int len = 0;
             String response = "";
             while (true) {
               len = input.read(b);
               response = new String(b, 0, len);
               Log.e("ccb", response);
             }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public  byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int len = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }
        bos.close();
        return bos.toByteArray();
    }


    /*释放资源*/
    private void releaseSocket() {
        if (outputStream != null) {
            try {
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            outputStream = null;
        }

        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
            }
            socket = null;
        }

        /*重新初始化socket*/
        if (isReConnect) {
            initSocket();
        }

    }

    public void onlyConnect(){
        if (socket != null) {
            try {
                socket.close();

            } catch (IOException e) {
            }
            socket = null;
        }
        initSocket();
    }

    /**
     * 判断socket是否连接
     * @return
     */
    public boolean isConnect(){
        byte[] bytes =new byte[1];
        try {
           int value = socket.getInputStream().read(bytes);
            if (value == -1) {return false;}
            else {return true;}
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }
}
