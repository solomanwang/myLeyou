package com.leyou.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Auther: 王忠强
 * @Date: 2019/3/5 11:02
 * @Description:
 */
@Slf4j
@Service
public class SearchServcie {

    @Autowired
    private BrandClient brandClient;
    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private GoodsClient goodsClient;
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    GoodsRepository goodsRepository;
    @Autowired
    ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu){
        //查询分类
        List<Category> categoryList = categoryClient.queryCategoryByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        if (CollectionUtils.isEmpty(categoryList)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FIND);
        }
        List<String> names = categoryList.stream().map(Category::getName).collect(Collectors.toList());
        //查询品牌
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //组成搜索字段   spu标题            分类名称                            品牌名称
        String all = spu.getTitle() + StringUtils.join(names," ") + brand.getName();
        //查询sku
        List<Sku> skuList = goodsClient.querySkuBySpuId(spu.getId());
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_SKU_NOT_FIND);
        }
        //对sku进行处理，因为展示只需要id title price image
        List<Map<String,Object>> skus = new ArrayList<>();
        Set<Long> priceSet = new HashSet<>();
        for (Sku sku : skuList) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("id",sku.getId());
            map.put("price",sku.getPrice());
            map.put("title",sku.getTitle());
            map.put("image",StringUtils.substringBefore(sku.getImages(),","));
            skus.add(map);
            priceSet.add(sku.getPrice());
        }
        //查询规格参数
        List<SpecParam> params = specificationClient.queryParamByList(null, spu.getCid3(), true);
        if (CollectionUtils.isEmpty(params)) {
            throw new LyException(ExceptionEnum.SPEC_PARAM_NOT_FOND);
        }
        //查询商品详情
        SpuDetail spuDetail = goodsClient.queryDetailById(spu.getId());
        //获取通用规格参数
        Map<Long, String> genericSpecs = JsonUtils.parseMap(spuDetail.getGenericSpec(),Long.class,String.class);
        //获取特有规格参数
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {});
        //获取可搜素的规格参数
        Map<String,Object> specs = new HashMap<>();
        //过滤规格参数
        params.forEach(p->{
            Object value = "";
            if (p.getGeneric()) {           //如果是通用属性
                value = genericSpecs.get(p.getId());       //取值
                    if(p.getNumeric()){         //如果值的类型为数字还要处理成段，在查询时就可以精确匹配
                        value = chooseSegment(value.toString(), p);
                    }
                specs.put(p.getName(), value);
            } else {
                specs.put(p.getName(), specialSpec.get(p.getId()));
            }
        });

        //构建goods对象
        Goods goods = new Goods();
        goods.setId(spu.getId());
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setSubTitle(spu.getSubTitle());
        goods.setPrice(priceSet);   //  所有sku价格的集合
        goods.setAll(all);          //  所有搜索字段 包括标题，分类，品牌，规格等
        goods.setSkus(JsonUtils.serialize(skuList));    //  所有sku的集合的json格式
        goods.setSpecs(specs);      //  所有的可用于搜索的规格参数
        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if(segs.length == 2){
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if(val >= begin && val < end){
                if(segs.length == 1){
                    result = segs[0] + p.getUnit() + "以上";
                }else if(begin == 0){
                    result = segs[1] + p.getUnit() + "以下";
                }else{
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }
    //  根据页码，关键词查询
    public PageResult<Goods> search(SearchRequest request) {
        //  不允许查所有
        if (StringUtils.isBlank(request.getKey())){
           return null;
        }
        Integer page = request.getPage() - 1;   //  因为elasticsearch 默认查询从0开始
        Integer size = request.getSize();
        //构建查询
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //过滤结果
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id","subTitle","skus"},null));
        //分页
        queryBuilder.withPageable(PageRequest.of(page,size));
        //查询关键字
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        //做聚合处理 聚合分类和品牌
        String categoryAggName = "category_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));  //查询字段
        String brandAggName = "brand_agg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));
        //查询
//        Page<Goods> result = goodsRepository.search(queryBuilder.build());
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        
        //解析结果
        long total = result.getTotalElements();
        long totalPage = result.getTotalPages();
        List<Goods> goodsList = result.getContent();
        //解析聚合的结果
        Aggregations aggs = result.getAggregations();

        LongTerms categoryAggs = aggs.get(categoryAggName);
        LongTerms brandAggs = aggs.get(brandAggName);
        List<Category> categoryList = parseCategoryAgg(categoryAggs);
        List<Brand> brandList = parseBrandAgg(brandAggs);

        //完成规格参数聚合
        List<Map<String,Object>> specs = null;
        if (categoryList != null && categoryList.size() == 1){
            //商品分类存在且等于1是开始聚合规格参数
            specs = buildSpecificationAgg(categoryList.get(0).getId(),basicQuery);
        }

        return new SearchResult(total, totalPage, goodsList,categoryList,brandList,specs);
    }
    //构建查询过滤条件filter
    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //创建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all",request.getKey()));
        //过滤条件
        Map<String,String> map = request.getFilter();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            //处理key
            if (!"cid3".equals(key) && !"brand".equals(key)){
                key = "specs." + key + ".keyword";
            }
            queryBuilder.filter(QueryBuilders.termQuery(key,entry.getValue()));
        }
        return queryBuilder;
    }
    //构建规格参数list
    private List<Map<String,Object>> buildSpecificationAgg(Long cid, QueryBuilder basicQuery) {
        List<Map<String,Object>> specs = new ArrayList<>();
        //  查询需要聚合的规格参数
        List<SpecParam> params = specificationClient.queryParamByList(null, cid, true);
        //聚合
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //带上查询条件
        queryBuilder.withQuery(basicQuery);
        //聚合
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name)
                    .field("specs." + name + ".keyword"));
        }
        //获取结果
        AggregatedPage<Goods> result = template.queryForPage(queryBuilder.build(), Goods.class);
        //解析结果
        Aggregations aggs = result.getAggregations();
        for (SpecParam param : params) {
            //规格参数名
            String name = param.getName();
            StringTerms terms = aggs.get(name);
        //准备map
        Map<String,Object> map = new HashMap<>();
        map.put("k",name);
        map.put("options",terms.getBuckets().stream().map(b -> b.getKeyAsString())
                .collect(Collectors.toList()));
        specs.add(map);
        }
        return specs;
    }

    //解析聚合的品牌
    private List<Brand> parseBrandAgg(LongTerms aggregation) {

        try {
            //取出所有的ids
            List<Long> ids = aggregation.getBuckets().stream()
                    .map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            //根据ids查询所有的品牌对象
            List<Brand> brands = brandClient.queryBrandByIds(ids);
            return brands;
        } catch (Exception e) {
            log.error("[搜索服务]查询品牌异常",e);
            return null;
        }
    }
    //解析聚合的分类
    private List<Category> parseCategoryAgg(LongTerms aggregation) {
        try {
            //取出所有的ids
            List<Long> ids = aggregation.getBuckets().stream()
                    .map(b -> b.getKeyAsNumber().longValue())
                    .collect(Collectors.toList());
            //根据ids查询所有的分类
            List<Category> categories = categoryClient.queryCategoryByIds(ids);
            return categories;
        } catch (Exception e) {
            log.error("[搜索服务]查询分类异常",e);
            return null;
        }
    }

    //  处理消息，对索引库进行新增或修改
    public void createOrUpdateIndex(Long spuId){
        //查询spu
        Spu spu = goodsClient.querySpuById(spuId);
        //构建goods对象
        Goods goods = buildGoods(spu);
        //存入索引库
        goodsRepository.save(goods);
    }
    //  处理消息，对索引库进行删除
    public void deleteIndex(Long spuId) {
        goodsRepository.deleteById(spuId);
    }
}
