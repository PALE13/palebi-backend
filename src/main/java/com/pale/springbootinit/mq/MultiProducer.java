package com.pale.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;

public class MultiProducer {

    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost("localhost");
//        factory.setUsername("xxxx.xxxx.xxx");
//        factory.setPassword("xxx.xxx.xxx");
        // 建立链接
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 创建队列,durable 参数设置为 true ，服务器重启后队列不丢失
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
			
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()){
                String message = scanner.nextLine();
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }
}