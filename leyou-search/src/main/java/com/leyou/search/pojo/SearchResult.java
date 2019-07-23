package com.leyou.search.pojo;

import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/8 13:29
 * @Description:
 */
@Data
public class SearchResult extends PageResult<Goods> {

    private List<Category> categories;      //分类待选项
    private List<Brand> brands;             //品牌待选项
    private List<Map<String,Object>> specs; // 规格参数过滤条件

    public SearchResult() {
    }

    public SearchResult(Long total, Long totalPage, List<Goods> items, List<Category> categories, List<Brand> brands, List<Map<String, Object>> specs) {
        super(total, totalPage, items);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
