package com.leyou.service;

import com.leyou.item.pojo.SpecParam;
import com.leyou.search.client.SpecificationClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/22 13:47
 * @Description:
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class SearchServcieTest {

    @Autowired
    private SpecificationClient client;

    @Test
    public void testQueryParams(){
        List<SpecParam> paramList = client.queryParamByList(null, 76L, true);
        System.out.println("paramList=="+paramList);
    }
}