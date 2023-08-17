package com.didichuxing.datachannel.arius.admin.persistence.component;

import java.net.InetSocketAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.didichuxing.datachannel.arius.admin.common.bean.po.BaseESPO;
import com.didichuxing.datachannel.arius.admin.common.bean.po.cluster.ClusterPhyPO;
import com.didichuxing.datachannel.arius.admin.persistence.mysql.resource.PhyClusterDAO;
import com.didiglobal.knowframework.elasticsearch.client.ESClient;
import com.didiglobal.knowframework.elasticsearch.client.gateway.document.ESIndexRequest;
import com.didiglobal.knowframework.elasticsearch.client.gateway.document.ESIndexResponse;
import com.didiglobal.knowframework.elasticsearch.client.model.type.ESVersion;
import com.didiglobal.knowframework.elasticsearch.client.request.batch.BatchNode;
import com.didiglobal.knowframework.elasticsearch.client.request.batch.BatchType;
import com.didiglobal.knowframework.elasticsearch.client.request.batch.ESBatchRequest;
import com.didiglobal.knowframework.elasticsearch.client.request.index.refreshindex.ESIndicesRefreshIndexRequest;
import com.didiglobal.knowframework.elasticsearch.client.response.batch.ESBatchResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.batch.IndexResultItemNode;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.deletebyquery.ESIndicesDeleteByQueryResponse;
import com.didiglobal.knowframework.elasticsearch.client.response.indices.refreshindex.ESIndicesRefreshIndexResponse;
import com.didiglobal.knowframework.log.ILog;
import com.didiglobal.knowframework.log.LogFactory;
import com.google.common.collect.Lists;

import lombok.NoArgsConstructor;

/**
 * @Author: D10865
 * @Description:
 * @Date: Create on 2018/9/14 下午1:22
 * @Modified By 2023-08-17 11:14 slhu
 *
 * 更新es数据的客户端，数据只会写入arius-meta集群
 */
@Component
@NoArgsConstructor
public class ESUpdateClient {

    private static final ILog             LOGGER            = LogFactory.getLog(ESUpdateClient.class);

    /**
     * 集群名称
     */
    @Value("${es.update.cluster.name}")
    private String                        metadataClusterName;

    @Value("${es.client.io.thread.count:0}")
    private Integer                       ioThreadCount;

    /**
     * 客户端个数
     */
    private final int                           clientCount       = 3;
    /**
     *  更新es数据的客户端连接队列
     */
    private List<ESClient> updateClientPool  = Lists.newCopyOnWriteArrayList();

    /**
     * 轮询获取esClient 时使用的下标
     */
    private final AtomicInteger  esClientCounter   = new AtomicInteger(0);
    /**
     * 之前的Cluster对象连接地址
     */
    private ClusterPhyPO beforeCluster = null;

    @Autowired
    private PhyClusterDAO                 clusterDAO;

    public static final int               MAX_RETRY_CNT     = 5;

    private static final String           COMMA             = ",";
    private static final String           ES_VERSION_PREFIX = "es-version";

    @PostConstruct
    @Scheduled(cron = "0 3/5 * * * ?")
    public void refreshConnect() {
        LOGGER.info("class=ESUpdateClient||method=init||ESUpdateClient refreshConnect start.");
        ClusterPhyPO query = new ClusterPhyPO();
        query.setCluster(metadataClusterName);
        List<ClusterPhyPO> clusterList = clusterDAO.listByCondition(query);
        refreshUpdateClientPool(clusterList);
        LOGGER.info("class=ESUpdateClient||method=init||ESUpdateClient refreshConnect finished.");
    }

    // delete by query
    public ESIndicesDeleteByQueryResponse deleteByQuery(String index, String type, String query) {
        ESClient esClient = getUpdateEsClientFromPool();
        if (esClient == null) {
            LOGGER.warn("class=ESUpdateClient||method=deleteByQuery||errMsg=esClient is null");
            return null;
        }

        ESIndicesDeleteByQueryResponse resp = esClient.admin().indices().prepareDeleteByQuery().setIndex(index)
            .setType(type).setQuery(query).execute().actionGet(5, TimeUnit.MINUTES);
        if (resp == null) {
            LOGGER.warn("class=UpdateClient||method=deleteByQuery||errMsg=resp is null");
            return null;
        }

        return resp;
    }

    /**
     * 写入单条
     *
     * @param source
     * @return
     */
    public boolean index(String indexName, String typeName, String id, String source) {
        ESClient esClient = null;
        ESIndexResponse response = null;

        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESIndexRequest esIndexRequest = new ESIndexRequest();
            esIndexRequest.setIndex(indexName);
            esIndexRequest.type(typeName);
            esIndexRequest.source(source);
            esIndexRequest.id(id);

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.index(esIndexRequest).actionGet(10, TimeUnit.SECONDS);
                if (response == null) {
                    continue;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK
                       || response.getRestStatus().getStatus() == HttpStatus.SC_CREATED;
            }

        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=index||indexName={}||typeName={}||id={}||source={}||errMsg=index doc error. ",
                indexName, typeName, id, source, e);
            if (response != null) {
                LOGGER.warn(
                    "class=UpdateClient||method=index||indexName={}||typeName={}||id={}||source={}||errMsg=response {}",
                    indexName, typeName, id, source, JSON.toJSONString(response));
            }
        }

        return false;
    }

    /**
     * 批量写入
     *
     * @param indexName
     * @param typeName
     * @return
     */
    public boolean batchInsert(String indexName, String typeName, List<? extends BaseESPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return true;
        }

        ESClient esClient = null;
        ESBatchResponse response = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESBatchRequest batchRequest = new ESBatchRequest();
            for (BaseESPO po : pos) {
                //write with routing
                if (null != po.getRoutingValue()) {
                    BatchNode batchNode = new BatchNode(BatchType.INDEX, indexName, typeName, po.getKey(),
                        JSON.toJSONString(po));
                    batchNode.setRouting(po.getRoutingValue());
                    batchNode.setEsVersion(getUpdateEsVersion(esClient));
                    batchRequest.addNode(batchNode);
                    continue;
                }

                //write without routing
                batchRequest.addNode(BatchType.INDEX, indexName, typeName, po.getKey(), JSON.toJSONString(po));
            }

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
                if (response == null) {
                    continue;
                }

                if (handleErrorResponse(indexName, typeName, pos, response)) {
                    return false;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK && !response.getErrors();
            }
        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=batch insert error. ",
                indexName, typeName, e);
            if (response != null) {
                LOGGER.warn("class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=response {}",
                    indexName, typeName, JSON.toJSONString(response));
            }

        }

        return false;
    }

    private ESVersion getUpdateEsVersion(ESClient esClient) {
        return null != esClient ? ESVersion.valueBy(ES_VERSION_PREFIX + esClient.getEsVersion()) : null;
    }

    /**
     * 批量删除
     *
     * @param indexName
     * @param typeName
     * @param ids
     * @return
     */
    public boolean batchDelete(String indexName, String typeName, List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }

        ESClient esClient = null;
        ESBatchResponse response = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESBatchRequest batchRequest = new ESBatchRequest();
            for (String id : ids) {
                batchRequest.addNode(BatchType.DELETE, indexName, typeName, id, "");
            }

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
                if (response == null) {
                    continue;
                }

                return response.getRestStatus().getStatus() == HttpStatus.SC_OK && !response.getErrors();
            }
        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=batchDelete||indexName={}||typeName={}||errMsg=batch delete error. ",
                indexName, typeName, e);
            if (response != null) {
                LOGGER.warn("class=UpdateClient||method=batchDelete||indexName={}||typeName={}||errMsg=response {}",
                    indexName, typeName, JSON.toJSONString(response));
            }

        }

        return false;
    }

    /**
     * 批量更新
     *
     * @param indexName
     * @param typeName
     * @param pos
     * @return
     */
    public boolean batchUpdate(String indexName, String typeName, List<? extends BaseESPO> pos) {
        if (CollectionUtils.isEmpty(pos)) {
            return true;
        }

        ESClient esClient = null;
        ESBatchResponse response = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESBatchRequest batchRequest = new ESBatchRequest();
            for (BaseESPO po : pos) {
                batchRequest.addNode(BatchType.UPDATE, indexName, typeName, po.getKey(), JSON.toJSONString(po));
            }

            for (int i = 0; i < MAX_RETRY_CNT; ++i) {
                response = esClient.batch(batchRequest).actionGet(2, TimeUnit.MINUTES);
                if (response == null) {
                    continue;
                }
                //todo 存在失败的情况需要解析 缺陷
                return response.getRestStatus().getStatus() == HttpStatus.SC_OK && !response.getErrors();
            }
        } catch (Exception e) {
            LOGGER.warn(
                "class=UpdateClient||method=batchUpdate||indexName={}||typeName={}||errMsg=batch update error. ",
                indexName, typeName, e);
            if (response != null) {
                LOGGER.warn("class=UpdateClient||method=batchUpdate||indexName={}||typeName={}||errMsg=response {}",
                    indexName, typeName, JSON.toJSONString(response));
            }

        }

        return false;
    }

    /**
     * 刷新索引
     *
     * @param indexName
     * @return
     */
    public boolean refreshIndex(String indexName) {
        if (StringUtils.isBlank(indexName)) {
            return false;
        }

        ESClient esClient = null;
        try {
            esClient = getUpdateEsClientFromPool();
            if (esClient == null) {
                return false;
            }

            ESIndicesRefreshIndexRequest request = new ESIndicesRefreshIndexRequest();
            request.setIndex(indexName);

            ESIndicesRefreshIndexResponse response = null;
            response = esClient.admin().indices().refreshIndex(request).actionGet(10, TimeUnit.SECONDS);

            return response.getRestStatus().getStatus() == HttpStatus.SC_OK;
        } catch (Exception e) {
            LOGGER.warn("class=UpdateClient||method=refreshIndex||indexName={}||errMsg=refresh index error. ",
                indexName, e);
        }

        return false;
    }

    /**************************************** private method ****************************************************/
    /**
     * 从更新es http 客户端连接池找那个获取
     *
     * @return
     */
    private ESClient getUpdateEsClientFromPool() {
        ESClient esClient = null;
        try {
            //这里获取当前轮询的次数，如果次数超过一定阈值则置为 0 （暂定阈值为client数量的5倍）
            int flag = esClientCounter.getAndIncrement();
            if (flag > clientCount * 5) {
                esClientCounter.lazySet(0);
            }
            //获取真正的esClient下标
            int currentIndex = flag % clientCount;
            esClient = updateClientPool.get(currentIndex);
        } catch (Exception e) {
            LOGGER.error(
                "class=ESUpdateClient||method=getUpdateEsClientFromPool||errMsg=fail to get es client from pool：", e);
        }
        if (esClient == null) {
            LOGGER.error(
                "class=ESUpdateClient||method=getUpdateEsClientFromPool||errMsg=fail to get es client from pool");
        }

        return esClient;
    }

    private ESClient buildEsClient(String address, String password, String clusterName) {
        if (StringUtils.isBlank(address)) {
            return null;
        }

        ESClient esClient = new ESClient();
        try {
            String[] httpAddressArray = StringUtils.splitByWholeSeparatorPreserveAllTokens(address, COMMA);
            TransportAddress[] transportAddresses = new TransportAddress[httpAddressArray.length];

            for (int i = 0; i < httpAddressArray.length; ++i) {
                String[] httpAddressAndPortArray = StringUtils
                    .splitByWholeSeparatorPreserveAllTokens(httpAddressArray[i], ":");
                if (httpAddressAndPortArray != null && httpAddressAndPortArray.length == 2) {
                    transportAddresses[i] = new InetSocketTransportAddress(
                        new InetSocketAddress(httpAddressAndPortArray[0], Integer.valueOf(httpAddressAndPortArray[1])));
                }
            }
            esClient.addTransportAddresses(transportAddresses);
            if (StringUtils.isNotBlank(clusterName)) {
                esClient.setClusterName(clusterName);
            }

            if (ioThreadCount > 0) {
                esClient.setIoThreadCount(ioThreadCount);
            }
            if (StringUtils.isNotBlank(password)) {
                esClient.setPassword(password);
            }

            // 配置http超时
            esClient.setRequestConfigCallback(builder -> builder.setConnectTimeout(10000).setSocketTimeout(120000)
                .setConnectionRequestTimeout(120000));
            esClient.start();

            return esClient;
        } catch (Exception e) {
            esClient.close();

            LOGGER.error("class=ESUpdateClient||method=buildEsClient||errMsg={}||address={}", e.getMessage(), address,
                e);
            return null;
        }
    }

    /**
     * 初始化访问es集群的客户端
     *
     */
    private void refreshUpdateClientPool(List<ClusterPhyPO> dataSourceList) {
        if (CollectionUtils.isEmpty(dataSourceList)) {
            LOGGER.error("class=ESUpdateClient||method=refreshUpdateClientPool||errMsg=fail to get es clusters");
            return;
        }

        ClusterPhyPO updateClusterDataSource = getUpdateClusterDataSource(dataSourceList);

        if (updateClusterDataSource == null) {
            LOGGER.error("class=UpdateClient||method=refreshUpdateClientPool||errMsg=fail to get es cluster info {}",
                    metadataClusterName);
            return;
        }

        if (null != beforeCluster && StringUtils.equals(updateClusterDataSource.getPassword(), beforeCluster.getPassword())
                && StringUtils.equals(updateClusterDataSource.getHttpAddress(), beforeCluster.getHttpAddress())) {
            return;
        }

        String httpAddress = updateClusterDataSource.getHttpAddress();
        String pw = updateClusterDataSource.getPassword();
        List<ESClient> innerList = Lists.newCopyOnWriteArrayList();
        // 添加新的客户端
        ESClient esClient;
        for (int i = 0; i < clientCount; ++i) {
            esClient = buildEsClient(httpAddress, pw, metadataClusterName);
            if (esClient != null) {
                innerList.add(esClient);
                LOGGER.info("class=UpdateClient||method=refreshUpdateClientPool||msg=add new es client {}, {}",
                        metadataClusterName, httpAddress);
            }
        }
        if (CollectionUtils.isNotEmpty(innerList)) {
            // 移除以存在的客户端
            Iterator<ESClient> iterator = this.updateClientPool.iterator();
            this.updateClientPool = innerList;
            while (iterator.hasNext()) {
                try {
                    iterator.next().close();
                } catch (Exception e) {
                    LOGGER.error("class=UpdateClient||method=refreshUpdateClientPool||errMsg=fail to close old es client {}",
                            metadataClusterName, e);
                }
            }
        }

        beforeCluster = updateClusterDataSource;
    }

    private ClusterPhyPO getUpdateClusterDataSource(List<ClusterPhyPO> dataSourceList) {
        return dataSourceList.stream().filter(cluster -> metadataClusterName.equals(cluster.getCluster())).findAny().orElse(null);
    }

    private boolean handleErrorResponse(String indexName, String typeName, List<? extends BaseESPO> pos,
                                        ESBatchResponse response) {
        if (response.getErrors().booleanValue()) {
            int errorItemIndex = 0;

            if (CollectionUtils.isNotEmpty(response.getItems())) {
                for (IndexResultItemNode item : response.getItems()) {
                    recordErrorResponseItem(indexName, typeName, pos, errorItemIndex++, item);
                }
            }

            return true;
        }

        return false;
    }

    private void recordErrorResponseItem(String indexName, String typeName, List<? extends BaseESPO> pos,
                                         int errorItemIndex, IndexResultItemNode item) {
        if (item.getIndex() != null && item.getIndex().getShards() != null
            && CollectionUtils.isNotEmpty(item.getIndex().getShards().getFailures())) {
            LOGGER.warn(
                "class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=Failures: {}, content: {}",
                indexName, typeName, item.getIndex().getShards().getFailures().toString(),
                JSON.toJSONString(pos.get(errorItemIndex)));
        }

        if (item.getIndex() != null && item.getIndex().getError() != null) {
            LOGGER.warn(
                "class=UpdateClient||method=batchInsert||indexName={}||typeName={}||errMsg=Error: {}, content: {}",
                indexName, typeName, item.getIndex().getError().getReason(),
                JSON.toJSONString(pos.get(errorItemIndex)));
        }
    }
}