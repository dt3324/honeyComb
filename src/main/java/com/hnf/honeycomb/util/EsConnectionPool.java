package com.hnf.honeycomb.util;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * es 连接池类
 *
 * @author lsj
 * @version 1.0.0
 * @since old pool class
 */
@Component(value = "esCollectionPool")
@Lazy(value = false)
public class EsConnectionPool {

    private static final int MIN = 2;

    private static final int MAX = 100;

    private Logger logger;

    private String ip;

    private String name;

    private Integer port;

    private Integer maxConnection;

    private Integer incrementsNumb;

    private ConcurrentLinkedQueue<Client> clients;

    /**
     * 初始化连接池
     */
    public EsConnectionPool(@Value("${cluster.name}") String name,
                            @Value("${cluster.ip}") String ip,
                            @Value("${cluster.port}") Integer port,
                            @Value("${maxConnection}") Integer maxConnection
    ) {
        this.name = name;
        this.ip = ip;
        this.port = port;
        this.maxConnection = maxConnection;
        incrementsNumb = maxConnection;
        clients = createClients(maxConnection, name, ip, port);
        logger = LoggerFactory.getLogger(EsConnectionPool.class);
        logger.warn("init Es Pool-----{}" + "Size:" + clients.size());
    }

    /**
     * 创建连接
     *
     * @param num         创建连接数
     * @param clusterName 集群名称
     * @param ip          ip地址
     * @param port        端口号
     * @return 连接Client集合
     */
    private ConcurrentLinkedQueue<Client> createClients(int num, String clusterName, String ip, Integer port) {
        ConcurrentLinkedQueue<Client> esClients = new ConcurrentLinkedQueue<Client>();
        //避免netty 版本冲突
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Settings settings = Settings.builder().put("cluster.name", name).build();
        for (int i = 0; i < num; i++) {
            try {
                Client client = new PreBuiltTransportClient(settings)
                        .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(ip), port));
                esClients.add(client);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return esClients;
    }

    /**
     * 获取连接
     * ConcurrentLinkedQueue 支持线程安全，因此无需加锁
     *
     * @return 返回一个Es连接对象
     */
    public synchronized Client getClient() {
        /**
         * 剩余连接数量小于最小连接数，则扩容连接
         */
        if (getSizeOfClient() < MIN) {
            //扩容1.5 倍
            incrementsNumb = incrementsNumb + (incrementsNumb >> 1);
            /* 实际容量可能超出新的容量，因为还有一部分连接待归还 */
            clients = createClients(incrementsNumb, name, ip, port);
            logger.warn("increment Clients {}" + "Clients Size: " + clients.size());
        }
        //当剩余连接数量大于初始化开辟空间1.5倍 并且当前总连接数小于初始空间的三倍可以缩容到原始大小
        //进行缩容 连接池最大数量不会超过110个
        if ((getSizeOfClient() > ((maxConnection + maxConnection) >> 1)) && (incrementsNumb >> 1 < ((maxConnection + maxConnection) << 1))) {
            clients = createClients(maxConnection, name, ip, port);
            logger.warn(" reduce Clients capacity {}" + "clients Size: " + clients.size());
        }
        Client client = clients.poll();
        logger.info("【用户获取了一个连接】-1" + "{} 剩余连接数量" + getSizeOfClient());
        return client;
    }

    /**
     * 获取当前剩余连接数量
     *
     * @return 返回剩余连接数
     */
    private synchronized int getSizeOfClient() {
        if (null != clients) {
            return clients.size();
        }
        return 0;
    }


    /**
     * 归还连接到连接池
     *
     * @param client Es连接对象
     */
    public synchronized void release(Client client) {
        clients.add(client);
        logger.info("【连接已归还】{} +1" + " 剩余连接数量{}" + getSizeOfClient());
    }

}
