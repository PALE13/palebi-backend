package com.pale.springbootinit.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {

    private static final String TASK_QUEUE_NAME = "multi_queue";

    public static void main(String[] argv) throws Exception {
        // 建立链接
        ConnectionFactory factory = new ConnectionFactory();
        // 设置 rabbitmq 对应的信息
        factory.setHost("localhost");
//        factory.setUsername("xxxx.xxxx.xxx");
//        factory.setPassword("xxx.xxx.xxx");
        final Connection connection = factory.newConnection();

        for (int i = 0; i < 2; i++) {
            // 创建两个消费者频道
            final Channel channel = connection.createChannel();
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            // 控制当个消费者的积压数
            channel.basicQos(1);

            int finalI = i;
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {

                String message = new String(delivery.getBody(), "UTF-8");
                try {
                    // 处理工作
                    System.out.println(" [x] Received '消费者：" + finalI + "，消费了：" + message + "'");
                    // 指定取某条消息
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);
                    // 停20秒 模拟机器处理能力有限
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.getStackTrace();
                    // 指定拒绝某条消息
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(),false,false);
                } finally {
                    System.out.println(" [x] Done");
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            // 开启消费监听，会一直监听生产者的消息
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }
    }
}