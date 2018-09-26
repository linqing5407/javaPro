package com.fcbox.fms.elasticSearch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fcbox.fms.elasticSearch.service.ISearchClient;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.*;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service("searchClient")
public class EsSearchClient implements ISearchClient {

    @Autowired
    private RestHighLevelClient client;

    @Override
    public List<JSONObject> search(SearchRequest request) {
        try {
            SearchResponse response = client.search(request);
            if (response.getHits() == null) {
                return null;
            }
            List<JSONObject> list = new ArrayList<>();
            response.getHits().forEach(item -> list.add(JSON.parseObject(item.getSourceAsString())));
            log.info("Hits",response.getHits().toString());
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<String> searchString(SearchRequest request) {
        try {

            SearchResponse response = client.search(request);

            if (response.getHits() == null) {
                return null;
            }
            List<String> list = new ArrayList<>();
            response.getHits().forEach(item -> list.add(item.getSourceAsString()));
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Long searchCount(SearchRequest request) {
        try {

            SearchResponse response = client.search(request);

            if (response.getHits() == null) {
                return 0L;
            }

            long total =response.getHits().totalHits;
            return total;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0L;
    }

    @Override
    public <T> List<T> search(SearchRequest request, Class<T> tClass) {

        List<JSONObject> searchResponse = this.search(request);
        if (searchResponse == null) {
            return null;
        }
        List<T> list = new ArrayList<>(searchResponse.size());
        searchResponse.forEach(item -> list.add(JSON.parseObject(JSON.toJSONString(item), tClass)));
        return list;
    }

    @Override
    public <T> IndexResponse saveEntity(String index, String type, String id, T t) {
    	IndexResponse indexResponse = null;
    	try {
            IndexRequest indexRequest = new IndexRequest(index, type , id);
            indexRequest.source(JSON.toJSONString(t) , XContentType.JSON);
            /*BulkRequest bulkRequest = new BulkRequest();
            bulkRequest.add(indexRequest);
            Header basicHeader = new BasicHeader("Content-Type:application" , "json");*/
            //this.client.bulk(bulkRequest , basicHeader);
            indexResponse = this.client.index(indexRequest);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return indexResponse;
    }


    @Override
    public <T> List<T> searchScroll(SearchRequest searchRequest, Class<T> tClass){

        try {

            SearchResponse searchResponse = client.search(searchRequest);
            String scrollId = searchResponse.getScrollId();
            SearchHit[] hits = searchResponse.getHits().getHits();
            System.out.println("first scroll:");
            List<T> list = new ArrayList<>();
            for (SearchHit searchHit : hits) {
                list.add(JSON.parseObject(searchHit.getSourceAsString(), tClass));
            }

            Scroll scroll = new Scroll(TimeValue.timeValueMinutes(5L));
            System.out.println("loop scroll:");
            while(hits != null && hits.length>0){
                SearchScrollRequest scrollRequest = new SearchScrollRequest(scrollId);
                scrollRequest.scroll(scroll);
                searchResponse = client.searchScroll(scrollRequest);
                scrollId = searchResponse.getScrollId();
                hits = searchResponse.getHits().getHits();
                for (SearchHit searchHit : hits) {
                    list.add(JSON.parseObject(searchHit.getSourceAsString(), tClass));
                }
            }
            ClearScrollRequest clearScrollRequest = new ClearScrollRequest();
            clearScrollRequest.addScrollId(scrollId);
            ClearScrollResponse clearScrollResponse = client.clearScroll(clearScrollRequest);
            boolean succeeded = clearScrollResponse.isSucceeded();
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




}
