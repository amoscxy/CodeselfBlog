package com.hcxy.aidlclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.hcxy.aidlserver.IDemandListener;
import com.hcxy.aidlserver.IDemandManager;
import com.hcxy.aidlserver.MessageBean;

import org.w3c.dom.ProcessingInstruction;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView tv_proxy;
    private TextView tv_stub;
    private boolean isBind;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_proxy = (TextView) findViewById(R.id.tv_proxy);
        tv_stub = (TextView) findViewById(R.id.tv_stub);
//        Intent intent = new Intent();
//        intent.setAction("com.hcxy.aidlserver"); // service的action
//        intent.setPackage("com.hcxy.aidlserver"); // aidl文件夹里面aidl文件的包名
//        bindService(intent,connection,BIND_AUTO_CREATE);
    }

    private IDemandManager demandManager;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBind = true;
            demandManager = IDemandManager.Stub.asInterface(service); // 得到该对象之后，我们可以用来进行进程间的方法调用和传输啦
            tv_proxy.append("\n服务绑定");
            try {
                tv_stub.append("\ngetDemand方法返回：\n"+demandManager.getDemand().toString());
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBind = false;
            tv_proxy.append("\n解绑服务");
        }
    };

    IDemandListener.Stub listener = new IDemandListener.Stub() {
        @Override
        public void onDemandReceiver(MessageBean msg) throws RemoteException {
            // 该方法运行在Binder线程池中，是非ui线程
            tv_stub.append("\n监听到需求："+msg.toString());
        }
    };

    public void btnIn(View view) {
        if (isBind) {
            MessageBean messageBean = new MessageBean("初次见面，失礼失礼", 1);
            tv_proxy.append("\n\n程序员发送:\n" + messageBean.toString());
            try {
                demandManager.setDemandIn(messageBean);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void btnOut(View view) {
        if (isBind) {
            MessageBean messageBean = new MessageBean("你个扑街", 100);
            tv_proxy.append("\n\n程序员发送:\n" + messageBean.toString());
            try {
                demandManager.setDemandOut(messageBean);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            tv_proxy.append("\n程序员上面的发送对象:\n" + messageBean.toString());
        }
    }

    public void btnBind(View view) {
        Intent intent = new Intent();
        intent.setAction("com.hcxy.aidlserver"); // service的action
        intent.setPackage("com.hcxy.aidlserver"); // aidl文件夹里面aidl文件的包名
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    public void btnUnBind(View view) {
        if (isBind) {
            isBind = false;
            unbindService(connection);
        }
    }

    public void btnListener(View view){
        try {
            demandManager.registerListener(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public void btnUnListener(View view){
        try {
            demandManager.unregisterListener(listener);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
