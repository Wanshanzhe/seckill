package com.wsz.seckill.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author : wanshanzhe
 * @date : 2022/4/10 5:09 下午
 * @desc : RabbitMQ配置类
 */

@Configuration
public class RabbitMQTopicConfig {

    //秒杀队列
    private static final String QUEUE = "seckillQueue";
    //秒杀交换机
    private static final String EXCHANGE = "seckillExchange";

    @Bean
    public Queue queue(){
        return new Queue(QUEUE);
    }

    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(EXCHANGE);
    }

    /**
     * 绑定队列和交换机操作
     * @return
     */
    @Bean
    public Binding binding(){
        return BindingBuilder.bind(queue()).to(topicExchange()).with(
              "seckill.#"
        );
    }

}
