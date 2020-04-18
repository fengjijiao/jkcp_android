package us.syh.jkcp;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.BusUtils;
import com.blankj.utilcode.util.KeyboardUtils;

import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;

public class MainActivity extends AppCompatActivity {

    private KCPServer kcpServer;
    private KCPClient kcpClient;
    private TextView textView_info;
    private Button button_start_kcp_server, button_start_kcp_client, button_stop_kcp_server, button_stop_kcp_client, button_send_kcp_server, button_send_kcp_client, button_show_all_ip;
    private EditText editText_server_listen_port, editText_server_ip, editText_server_port;
    public static final String TAG_ADD_SERVER_INFO = "ADD_SERVER_INFO";
    public static final String TAG_ADD_CLIENT_INFO = "ADD_CLIENT_INFO";

    @BusUtils.Bus(tag = TAG_ADD_SERVER_INFO, threadMode = BusUtils.ThreadMode.MAIN)
    public void onHandlerADDServerInfo(String text) {
        addInfo("服务端：" + text);
    }
    @BusUtils.Bus(tag = TAG_ADD_CLIENT_INFO, threadMode = BusUtils.ThreadMode.MAIN)
    public void onHandlerADDClientInfo(String text) {
        addInfo("客户端：" + text);
    }

    @Override
    public void onStart() {
        super.onStart();
        BusUtils.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        BusUtils.unregister(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        KeyboardUtils.fixAndroidBug5497(this);
        initView();
    }

    private void initView() {
        textView_info = findViewById(R.id.textView_info);
        button_start_kcp_server = findViewById(R.id.button_start_kcp_server);
        button_start_kcp_client = findViewById(R.id.button_start_kcp_client);
        button_stop_kcp_server = findViewById(R.id.button_stop_kcp_server);
        button_stop_kcp_client = findViewById(R.id.button_stop_kcp_client);
        button_send_kcp_server = findViewById(R.id.button_send_kcp_server);
        button_send_kcp_client = findViewById(R.id.button_send_kcp_client);
        button_show_all_ip = findViewById(R.id.button_show_all_ip);
        editText_server_listen_port = findViewById(R.id.editText_server_listen_port);
        editText_server_ip = findViewById(R.id.editText_server_ip);
        editText_server_port = findViewById(R.id.editText_server_port);
        button_start_kcp_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                kcpServer = new KCPServer(Integer.parseInt(editText_server_listen_port.getText().toString()), 1);
                kcpServer.noDelay(1, 10, 2, 1);
                kcpServer.setMinRto(10);
                kcpServer.wndSize(64, 64);
                kcpServer.setTimeout(10 * 1000);
                kcpServer.setMtu(512);
                kcpServer.start();
            }
        });
        button_start_kcp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
                kcpClient = new KCPClient();
                kcpClient.noDelay(1, 20, 2, 1);
                kcpClient.setMinRto(10);
                kcpClient.wndSize(32, 32);
                kcpClient.setTimeout(10 * 1000);
                kcpClient.setMtu(512);
                // tc.setConv(121106);//默认conv随机

                kcpClient.connect(new InetSocketAddress(editText_server_ip.getText().toString(), Integer.parseInt(editText_server_port.getText().toString())));
                kcpClient.start();
                ByteBuf bb = Unpooled.copiedBuffer("c", StandardCharsets.UTF_8);
                kcpClient.send(bb);
                BusUtils.post(MainActivity.TAG_ADD_CLIENT_INFO, "已连接");
            }
        });
        button_stop_kcp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(kcpClient != null) {
                    kcpClient.close();
                    onHandlerADDClientInfo("关闭连接");
                }
            }
        });
        button_stop_kcp_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(kcpServer != null) {
                    kcpServer.close();
                    onHandlerADDServerInfo("关闭服务");
                }
            }
        });
        button_send_kcp_client.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = new Date().toString();
                ByteBuf bb = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
                kcpClient.send(bb);
            }
        });
        button_send_kcp_server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = new Date().toString();
                ByteBuf bb = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
                kcpServer.send(bb);
            }
        });
        button_show_all_ip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] allIt = Utils.getAllNetInterface();
                for (String it: allIt) {
                    try {
                        addInfo("接口：" + it + ",IP地址：" + Arrays.toString(Utils.getIpAddress(it, "ipv6")));
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
    private void addInfo(String text) {
        textView_info.setText(textView_info.getText() + "\n" + text);
    }
}
