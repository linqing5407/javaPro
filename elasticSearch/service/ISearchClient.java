package com.fcbox.fms.elasticSearch.service;

import com.alibaba.fastjson.JSONObject;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;

import java.util.List;

public interface ISearchClient {

    /**
     * 搜索结果
     */
    List<JSONObject> search(SearchRequest request);
    /**
     * 搜索结果
     */
    List<String> searchString(SearchRequest request);

    /**
     * 查询总数
     * @param request
     * @return
     */
    Long searchCount(SearchRequest request);
    /**
     * 查询list数据
     */
    <T> List<T> search(SearchRequest request, Class<T> tClass);

    /**
     * 查询list数据 scroll 可拉取全量数据
     */
    <T>List<T> searchScroll(SearchRequest request, Class<T> tClass);

    /**
     * 插入数据
     * @param index 数据库名称
     * @param type 数据库表名
     * @param id 数据库主键ID
     * @param t 实体class
     * @param <T>
     * @return
     */
	<T> IndexResponse saveEntity(String index, String type, String id, T t);

}
