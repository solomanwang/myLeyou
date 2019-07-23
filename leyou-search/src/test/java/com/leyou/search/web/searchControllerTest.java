package com.leyou.search.web;

import com.leyou.common.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.service.SearchServcie;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.Assert.*;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/6 22:21
 * @Description:
 */
@Slf4j
@RestController
@RunWith(SpringRunner.class)
@SpringBootTest
public class searchControllerTest {

    @Autowired
    SearchServcie searchServcie;

    @Test
    public void search() {
        SearchRequest request = new SearchRequest();
        request.setKey("手机");
        request.setPage(2);
        PageResult<Goods> result = searchServcie.search(request);
        log.info(result.toString());
    }
}