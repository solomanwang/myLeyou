package com.leyou.search.mq;

import com.leyou.service.SearchServcie;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/13 20:10
 * @Description:
 */
@Component
public class ItemListener {

    @Autowired
    private SearchServcie searchServcie;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.insert.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert","item.update"}
    ))//    新增或者修改
    public  void listenerInsertOrUpdate(Long spuId){
        if (spuId == null){
            return;
        }
        //  处理消息，对索引库进行新增或修改
        searchServcie.createOrUpdateIndex(spuId);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "search.item.delete.queue",durable = "true"),
            exchange = @Exchange(name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.delete"}
    ))//    删除
    public  void listenerDelete(Long spuId){
        if (spuId == null){
            return;
        }
        //  处理消息，对索引库进行新增或修改
        searchServcie.deleteIndex(spuId);
    }
}
