package com.fcbox.fms.elasticSearch;

import com.fcbox.fms.disconf.AppConfig;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
public class ElasticsConfig {

    @Autowired
    private AppConfig appConfig;
    /**
     * 初始化
     */
    @Bean
    public RestHighLevelClient restHighLevelClient() {
        return getEsClientDecorator().getRestHighLevelClient();
    }

    @Bean
    @Scope("singleton")
    public ESClientDecorator getEsClientDecorator() {
        //可以配置集群 通过逗号隔开 10.204.58.170:9200,10.204.58.171:9200,10.204.58.172:9200
        String clusterNodes =appConfig.getElasticsearchClusterNodes();
        String[] node =clusterNodes.split(",");
        int length = node.length;
        HttpHost[] httpHost=new HttpHost[length];
        for(int i=0;i<length;i++){
            String[] nodes = node[i].split(":");
            httpHost[i]=new HttpHost(nodes[0],Integer.valueOf(nodes[1]));
        }
        return new ESClientDecorator(httpHost);
    }

}
