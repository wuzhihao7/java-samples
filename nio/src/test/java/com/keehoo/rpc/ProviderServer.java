package com.technologysia.rpc;

import com.alibaba.fastjson.JSONObject;
import com.technologysia.rpc.dto.RpcRequest;
import com.technologysia.rpc.dto.RpcResponse;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * @author wuzhihao
 * @version V1.0
 * @since 2019/4/2
 */

public class ProviderServer implements Runnable {
    /**
     * 服务提供端口
     */
    private int port;


    public ProviderServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                int readyChannels = selector.selectNow();
                if (readyChannels == 0){
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel1 = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = serverSocketChannel1.accept();
                        ByteBuffer buf1 = ByteBuffer.allocate(2048);
                        socketChannel.read(buf1);
                        buf1.flip();
                        String reciveStr = new String(buf1.array());
                        if (buf1.hasRemaining()) {
                            System.out.println(">>>服务端收到数据：" + reciveStr);
                            //判断接受的内容是否有结束符，如果有，说明是一个请求结束。
                            if (reciveStr.contains(RpcConstant.PROTOCOL_END)) {
                                RpcRequest req = JSONObject.parseObject(reciveStr.replace(RpcConstant.PROTOCOL_END, ""), RpcRequest.class);
                                RpcResponse res = new RpcResponse();
                                res.setRequestId(req.getRequestId());
                                System.out.println(req.toString());
                                Class<?> remoteInterface = Class.forName(req.getInterfaceName());
                                Method method = remoteInterface.getMethod(req.getMethodName(), req.getParameterTypes());
                                if (null != method) {
                                    Object obj = method.invoke(RpcRegister.buildRegister().findServer(req.getInterfaceName()), req.getParameters());
                                    res.setException(null);
                                    res.setResult(obj);
                                }
                                buf1.clear();
                                buf1.put(JSONObject.toJSON(res).toString().getBytes());
                                buf1.flip();
                                socketChannel.write(buf1);
                            }
                        }
                        socketChannel.close();
                    } else if (key.isConnectable()) {
                    } else if (key.isReadable()) {
                    } else if (key.isWritable()) {

                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        ProviderServer server = new ProviderServer(8090);
        RpcRegister.buildRegister().register(Idemo.class.getName(), new DemoImpl());
        new Thread(server).start();
    }


}

