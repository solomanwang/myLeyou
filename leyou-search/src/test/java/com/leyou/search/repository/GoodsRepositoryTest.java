package com.leyou.search.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.SpecParam;
import com.leyou.item.pojo.Spu;
import com.leyou.item.pojo.SpuDetail;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.service.SearchServcie;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/5 09:58
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class GoodsRepositoryTest {
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    ElasticsearchTemplate template;
    @Autowired
    SearchServcie searchServcie;
    @Autowired
    GoodsClient goodsClient;
    @Autowired
    SpecificationClient specificationClient;

    @Test
    public void testcrateIndex(){
        template.createIndex(Goods.class);
        template.putMapping(Goods.class);
    }

    //导入数据到elasticsearch
    @Test
    public void loadData() {
        int page = 1;       //  页码
        int rows = 100;     //  条数
        int size = 0;       //标记
        do {
            //获取spuList
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows,true, null);
            List<Spu> spus = result.getItems();
            if (CollectionUtils.isEmpty(spus)){ //表示已经没有结果不用再查询
                break;
            }
            //生成goods
            List<Goods> goodsList = new ArrayList<>();
            // 遍历spu
            for (Spu spu : spus) {
                try {
                    Goods goods = searchServcie.buildGoods(spu);
                    goodsList.add(goods);
                } catch (Exception e) {
                    log.error("出现异常{}",e);
                    break;
                }
            }

            goodsRepository.saveAll(goodsList);     //存入索引库
            page++;                             //翻页
            size = spus.size();                 //size设置为查询当前页的条数
        }while (size == 100);                   //满足条件说明后面还有结果继续查询
    }

    @Test
    public void testQueryGoods(){
        int page = 1;       //  页码
        int rows = 100;     //  条数
        int size = 0;       //标记
            //获取spuList
        System.out.println("--------------------开始查询---------------------------");
            PageResult<Spu> result = goodsClient.querySpuByPage(page, rows,true, null);
        List<Spu> items = result.getItems();
        System.out.println("item.size = " + items.size());
        for (Spu item : items) {
            System.out.println("item = "+item);
        }
    }

    @Test
    public void testQuerySkuBySpuId(){
        List<Sku> skus = goodsClient.querySkuBySpuId(215L);
        for (Sku sku : skus) {
            System.out.println("sku = " + sku);
        }
    }

    @Test
    public void testqueryDetailById(){
        List<SpecParam> params = specificationClient.queryParamByList(null, 76L, true);
        params.forEach(System.out::println);
        SpuDetail spuDetail = goodsClient.queryDetailById(194L);
        Map<Long, String> genericSpecs = JsonUtils.parseMap(spuDetail.getGenericSpec(),Long.class,String.class);
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});

        genericSpecs.forEach((k,v)->System.out.println(k + " = " + v));
        System.out.println("=====================================================================");
        specialSpec.forEach((k,v)->System.out.println(k + " = " + v));

        //获取可搜素的规格参数
        Map<String,Object> specs = new HashMap<>();
        //过滤规格参数
        params.forEach(p->{
            Object value = "";
                if (p.getGeneric()) {           //如果是通用属性
                    value = genericSpecs.get(p.getId());       //取值
//                    if(p.getNumeric()){         //如果值的类型为数字还要处理成段，在查询时就可以精确匹配
//                        value = chooseSegment(value, p);
//                    }
                    specs.put(p.getName(), value);
                } else {
                    specs.put(p.getName(), specialSpec.get(p.getId()));
                }
        });
        specs.forEach((k,v)->System.out.println(k + " = " + v));
    }
}