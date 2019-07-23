package le.com.search.web;

import com.leyou.common.vo.PageResult;
import le.com.search.pojo.Goods;
import le.com.search.pojo.SearchRequest;
import le.com.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @Author: cuzz
 * @Date: 2018/11/12 15:05
 * @Description:
 */
@RestController
public class SearchController {

    @Autowired
    private SearchService searchService;

    /**
     * 搜索功能
     * @param request
     * @return
     */
    @PostMapping("page")
    public ResponseEntity<PageResult<Goods>> search(@RequestBody SearchRequest request) {
        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("list/{openId}")
    public ResponseEntity<Void> test(@PathVariable("openId") String openId){
        System.out.println(openId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
