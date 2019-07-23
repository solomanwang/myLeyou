package com.leyou.common.mapper;

import tk.mybatis.mapper.additional.idlist.IdListMapper;
//该mapper可以自己设置主键
import tk.mybatis.mapper.additional.insert.InsertListMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @Auther: 王忠强
 * @Date: 2019/2/28 21:28
 * @Description:
 */
@RegisterMapper //必须加该注解 通用mapper才会生效
public interface BaseMapper<T> extends Mapper<T>,IdListMapper<T,Long>,InsertListMapper<T> {
}
