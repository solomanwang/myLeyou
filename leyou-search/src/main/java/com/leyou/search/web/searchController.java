package com.leyou.search.web;

import com.leyou.common.vo.PageResult;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.service.SearchServcie;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/6 20:50
 * @Description:
 */
@Slf4j
@RestController
public class searchController {

    @Autowired
    SearchServcie searchServcie;

    /**
     *
     * 功能描述: 搜索功能
     *
     * @param: 
     * @return: 
     * @auther: 王忠强
     * @date: 2019/3/6 20:52
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request){
        log.info("request:{}",request);
        return ResponseEntity.ok(searchServcie.search(request));
    }
}
