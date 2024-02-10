package com.pale.springbootinit.bizmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 创建测试测序用到的交换机和队列 (仅执行一次)
 */
public class RabbitMqInitDemo {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
        	// 设置 rabbitmq 对应的信息
       	 	factory.setHost("localhost");
//        	factory.setUsername("xxxx.xxxx.xxx");
//        	factory.setPassword("xxx.xxx.xxx");

            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            
            String demoExchange = "demo_exchange";
            
            channel.exchangeDeclare(demoExchange, "direct");

            // 创建队列，分配一个队列名称
            String queueName = "demo_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            channel.queueBind(queueName, demoExchange, "demo_routingKey");
            
        }catch (Exception e){
            
        }
    }

}