package us.syh.jkcp;

import com.blankj.utilcode.util.BusUtils;

import org.beykery.jkcp.Kcp;
import org.beykery.jkcp.KcpClient;
import org.beykery.jkcp.KcpOnUdp;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.util.ResourceLeakDetector;

public class KCPClient extends KcpClient {

    @Override
    public void handleReceive(ByteBuf bb, KcpOnUdp kcp) {
        String content = bb.toString(StandardCharsets.UTF_8);
        System.out.println("conv:" + kcp.getKcp().getConv() + " recv:" + content + " kcp-->" + kcp);
        BusUtils.post(MainActivity.TAG_ADD_CLIENT_INFO, "conv:" + kcp.getKcp().getConv() + " recv:" + content + " kcp-->" + kcp);
        ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer(2048);
        buf.writeBytes(content.getBytes(StandardCharsets.UTF_8));
        //kcp.send(buf);
        bb.release();
    }

    /**
     * kcp异常，之后此kcp就会被关闭
     *
     * @param ex
     * @param kcp
     */
    @Override
    public void handleException(Throwable ex, KcpOnUdp kcp) {
        System.out.println(ex);
        BusUtils.post(MainActivity.TAG_ADD_CLIENT_INFO, ex);
    }

    @Override
    public void handleClose(KcpOnUdp kcp) {
        super.handleClose(kcp);
        System.out.println("服务器离开:" + kcp);
        System.out.println("waitSnd:" + kcp.getKcp().waitSnd());
        BusUtils.post(MainActivity.TAG_ADD_CLIENT_INFO, "服务器离开:" + kcp);
        BusUtils.post(MainActivity.TAG_ADD_CLIENT_INFO, "waitSnd:" + kcp.getKcp().waitSnd());
    }

    @Override
    public void out(ByteBuf msg, Kcp kcp, Object user) {
        super.out(msg, kcp, user);
    }

    /**
     * tcpdump udp port 2225 -x -vv -s0 -w 1112.pcap
     *
     * @param args
     * @throws java.lang.InterruptedException
     */
    public static void main(String[] args) throws InterruptedException {
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.DISABLED);
        KCPClient tc = new KCPClient();
        tc.noDelay(1, 20, 2, 1);
        tc.setMinRto(10);
        tc.wndSize(32, 32);
        tc.setTimeout(10 * 1000);
        tc.setMtu(512);
        // tc.setConv(121106);//默认conv随机

        tc.connect(new InetSocketAddress("localhost", 2222));
        tc.start();
        String content = "sdfkasd你好。。。。。。。";
        //ByteBuf bb = PooledByteBufAllocator.DEFAULT.buffer(1500);
        ByteBuf bb = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
        tc.send(bb);
    }
}