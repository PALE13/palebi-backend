package com.pale.springbootinit.bizmq;

import com.pale.springbootinit.constant.BiMqConstant;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class BiMqInit {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            // 设置 rabbitmq 对应的信息
            factory.setHost(BiMqConstant.BI_MQ_HOST);
//            factory.setUsername(BiMqConstant.BI_MQ_USERNAME);
//            factory.setPassword(BiMqConstant.BI_MQ_PASSWORD);

            Connection connection = factory.newConnection();

            Channel channel = connection.createChannel();

            String biExchange = BiMqConstant.BI_EXCHANGE_NAME;

            channel.exchangeDeclare(biExchange, BiMqConstant.BI_DIRECT_EXCHANGE);

            // 创建队列，分配一个队列名称
            String queueName = BiMqConstant.BI_QUEUE;
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, biExchange, BiMqConstant.BI_ROUTING_KEY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}