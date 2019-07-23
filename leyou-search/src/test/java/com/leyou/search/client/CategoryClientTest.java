package com.leyou.search.client;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Category;
import com.leyou.item.pojo.Spu;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/4 17:17
 * @Description:
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {

    @Autowired
    CategoryClient categoryClient;
    @Autowired
    GoodsClient goodsClient;

    @Test
    public void queryCategoryByIds() {
        List<Category> list = categoryClient.queryCategoryByIds(Arrays.asList(1L,2L,3L));
        Assert.assertEquals(3,list.size());
        System.out.println("size = " + list.size());
        for (Category c :list) {
            System.out.println("category : " + c);
        }
    }

//    @Test
//    public void testQueryGoods(){
//        int page = 1;       //  页码
//        int rows = 100;     //  条数
//        int size = 0;       //标记
//        //获取spuList
//        System.out.println("--------------------开始查询---------------------------");
//        PageResult<Spu> result = goodsClient.querySpuByPage(page, rows,true, null);
//        List<Spu> items = result.getItems();
//        System.out.println("item.size = " + items.size());
//        for (Spu item : items) {
//            System.out.println("item = "+item);
//        }
//    }
}