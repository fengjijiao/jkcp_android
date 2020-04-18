package us.syh.jkcp;

import com.blankj.utilcode.util.BusUtils;

import org.beykery.jkcp.KcpOnUdp;
import org.beykery.jkcp.KcpServer;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class KCPServer extends KcpServer {
    private List<KcpOnUdp> clientList;
    /**
     * server
     *
     * @param port
     * @param workerSize
     */
    public KCPServer(int port, int workerSize) {
        super(port, workerSize);
        clientList = new ArrayList<>();
        BusUtils.post(MainActivity.TAG_ADD_SERVER_INFO, "已开启");
    }

    @Override
    public void handleReceive(ByteBuf bb, KcpOnUdp kcp) {
        if(clientList.indexOf(kcp) == -1) {
            clientList.add(kcp);
        }
        if (c == 0) {
            start = System.currentTimeMillis();
        }
        c++;
        if(bb.getByte(0) == 0x00) return;
        String content = bb.toString();
        System.out.println("msg:" + content + " kcp--> " + kcp);
        BusUtils.post(MainActivity.TAG_ADD_SERVER_INFO, "msg:" + content + " kcp--> " + kcp);
        if (c < 10000) {
            //kcp.send(bb);//echo
        } else {
            System.out.println("cost:" + (System.currentTimeMillis() - start));
            BusUtils.post(MainActivity.TAG_ADD_SERVER_INFO, "cost:" + (System.currentTimeMillis() - start));
            this.close();
        }
    }

    @Override
    public void handleException(Throwable ex, KcpOnUdp kcp) {
        System.out.println(ex);
        BusUtils.post(MainActivity.TAG_ADD_SERVER_INFO, ex);
    }

    @Override
    public void handleClose(KcpOnUdp kcp) {
        clientList.remove(kcp);
        System.out.println("客户端离开:" + kcp);
        System.out.println("waitSnd:" + kcp.getKcp().waitSnd());
        BusUtils.post(MainActivity.TAG_ADD_SERVER_INFO, "客户端离开:" + kcp);
        BusUtils.post(MainActivity.TAG_ADD_SERVER_INFO, "waitSnd:" + kcp.getKcp().waitSnd());
    }

    public void send(ByteBuf bb) {
        for (KcpOnUdp kcp : clientList) {
            send(bb, kcp);
        }
    }
    private static long start;
    private static int c = 0;

    /**
     * 测试
     *
     * @param args
     */
    public static void main(String[] args) {
        KCPServer s = new KCPServer(2222, 1);
        s.noDelay(1, 10, 2, 1);
        s.setMinRto(10);
        s.wndSize(64, 64);
        s.setTimeout(10 * 1000);
        s.setMtu(512);
        s.start();
    }
}
