package com.hnf.honeycomb.daoimpl;

import com.hnf.honeycomb.dao.ElasticSearchDao;
import com.hnf.honeycomb.util.EsConnectionPool;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.PrefixQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.Map;


/**
 * @author admin
 */
@Repository("elasticSearchDao")
public class ElasticSearchDaoImpl implements ElasticSearchDao {
    @Resource
    private EsConnectionPool esCollectionPool;

    @Override
    public void esUpdate(String indexName, String typeName,String id, Map<String,Object> map){
        Client client=null;
        try {
            client=esCollectionPool.getClient();
            client.prepareUpdate(indexName, typeName, id).setDoc(map).get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(client!=null){
                esCollectionPool.release(client);
            }
        }
    }

    private SearchHits searchES(String indexName, String typeName,
                                QueryBuilder mmqb, RangeQueryBuilder rqb,
                                HighlightBuilder hb, Integer from, Integer size) {
        Client client = null;
        try {
            client = esCollectionPool.getClient();
            SearchResponse response;
            //判断是否有时间范围
            if (rqb != null) {
                response = client.prepareSearch(indexName)
                        .setTypes(typeName)
                        .setQuery(mmqb)
                        //					.setPostFilter(rqb1)
                        .setPostFilter(rqb)
                        .highlighter(hb)
                        .setFrom(from)
                        .setSize(size)
                        .execute()
                        .actionGet();
                if (null != response) {
                    return response.getHits();
                }
            }
            response = client.prepareSearch(indexName)
                    .setTypes(typeName)
                    .setQuery(mmqb)
                    .highlighter(hb)
                    .setFrom(from)
                    .setSize(size)
                    .execute()
                    .actionGet();
            //释放数据局连接
            if (null != response) {
                return response.getHits();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                esCollectionPool.release(client);
            }
        }
        return null;
    }


    private SearchHits searchES1(String indexName, String typeName,
                                 QueryBuilder mmqb, RangeQueryBuilder rqb,
                                 HighlightBuilder hb, Integer from, Integer size) {
        Client client = null;
        try {
            client = esCollectionPool.getClient();
            SearchResponse response;
            //判断是否有时间范围
            if (rqb != null) {
                response = client.prepareSearch(indexName)
                        .setTypes(typeName)
                        .setQuery(mmqb)
                        //					.setPostFilter(rqb1)
                        .setPostFilter(rqb)
                        .highlighter(hb)
                        .setFrom(from)
                        //设置查询方式
                        .setSearchType(SearchType.QUERY_THEN_FETCH)
                        .setSize(size)
                        .execute()
                        .actionGet();

                return response.getHits();
            }
            response = client.prepareSearch(indexName)
                    .setTypes(typeName)
                    .setQuery(mmqb)
                    .highlighter(hb)
                    .setFrom(from)
                    .setSearchType(SearchType.QUERY_THEN_FETCH)
                    .setSize(size)
                    .execute()
                    .actionGet();
            //释放数据局连接
            return response.getHits();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                esCollectionPool.release(client);
            }
        }
        return null;
    }

    @Override
    public SearchHits searchWxChatRoomMsg(String indexName, String typeName, QueryBuilder mmqb,
                                          RangeQueryBuilder rqb, HighlightBuilder hb, Integer from, Integer size) {
        return searchES(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public SearchHits searchRecord(String indexName, String typeName, QueryBuilder mmqb,
                                   RangeQueryBuilder rqb, HighlightBuilder hb, Integer from, Integer size) {
        return searchES(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public SearchHits searchQqTroopMsg(String indexName, String typeName,
                                       QueryBuilder mmqb, RangeQueryBuilder rqb,
                                       HighlightBuilder hb, Integer from, Integer size) {
        return searchES(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public SearchHits searchQqMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                  HighlightBuilder hb, Integer from, Integer size) {
        return searchES(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public SearchHits searchWxMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                  HighlightBuilder hb, Integer from, Integer size) {
        return searchES(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public SearchHits searchMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                HighlightBuilder hb, Integer from, Integer size) {
        return searchES(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    private Long countES(
            String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        Client client = null;
        try {
            client = esCollectionPool.getClient();
            SearchResponse response;
            SearchHits hits;
            //判断是否有时间范围
            if (rqb != null) {
                response = client.prepareSearch(indexName)
                        .setTypes(typeName)
                        .setQuery(mmqb)
                        .setPostFilter(rqb)
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        //					.setFrom(0)
                        .setSize(0)
                        .execute()
                        .actionGet();
                hits = response.getHits();
                if (hits.totalHits() > 0) {
                    esCollectionPool.release(client);
                    return hits.getTotalHits();
                }

                return 0L;
            }
            response = client.prepareSearch(indexName)
                    .setTypes(typeName)
                    .setQuery(mmqb)
                    .setFrom(0)
                    .setSearchType(SearchType.QUERY_AND_FETCH)
                    .setSize(0)
                    .execute()
                    .actionGet();
            hits = response.getHits();
            //释放数据局连接
            return hits.getTotalHits();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                esCollectionPool.release(client);
            }
        }
        return 0L;
    }


    private Long countES1(
            String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        Client client = null;
///        //在统计之前先查询redis
//        StringBuilder str = new StringBuilder();
//        String uniqueKey = "";
//        if (null != mmqb) {
//            uniqueKey = str.append(indexName).append(typeName).append(mmqb).toString();
//            System.out.println("带查询返回的uniqueKey: "+ uniqueKey);
//        }else {
//            uniqueKey = str.append(indexName).append(typeName).toString();
//        }
//        System.out.println("不带时间的UniqueKey :" +uniqueKey);
//        Long redisResult = (Long) redisTemplate.opsForValue().get(uniqueKey);
//        if (null != redisResult) {
//            //有该缓存直接返回
//            System.out.println("从缓存中返回数据");
//            return redisResult;
//        }
        try {
            client = esCollectionPool.getClient();
            SearchResponse response;
            SearchHits hits;
            //判断是否有时间范围
            if (rqb != null) {
                response = client.prepareSearch(indexName)
                        .setTypes(typeName)
                        .setQuery(mmqb)
                        .setPostFilter(rqb)
                        .setSearchType(SearchType.QUERY_AND_FETCH)
                        .setSize(0)
                        .execute()
                        .actionGet();
                if (response != null) {
                    hits = response.getHits();
                    if (hits.totalHits() > 0) {
                        esCollectionPool.release(client);
                        return hits.getTotalHits();
                    }
                    return 0L;
                }
            }
            response = client.prepareSearch(indexName)
                    .setTypes(typeName)
                    .setQuery(mmqb)
                    .setFrom(0)
                    .setSearchType(SearchType.QUERY_AND_FETCH)
                    .setSize(0)
                    .execute()
                    .actionGet();
            hits = response.getHits();
            //释放数据局连接
            //先把查询结果存入redis
            ///      redisTemplate.opsForValue().set(uniqueKey, totalHits, redisDataSaveTimeConfig.getTime(), TimeUnit.MINUTES);
            return hits.getTotalHits();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                esCollectionPool.release(client);
            }
        }
        return 0L;
    }

    @Override
    public Long countMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES(indexName, typeName, mmqb, rqb);
    }

    @Override
    public Long countWxMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES(indexName, typeName, mmqb, rqb);
    }

    @Override
    public Long countQqMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES(indexName, typeName, mmqb, rqb);
    }

    @Override
    public Long countWxTroopMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES(indexName, typeName, mmqb, rqb);
    }

    @Override
    public Long countQqTroopMsg(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES(indexName, typeName, mmqb, rqb);
    }

    @Override
    public Long countRecordCall(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES(indexName, typeName, mmqb, rqb);
    }

    @Override
    public Long countPerson(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchPerson(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                   HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countCase(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchCase(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                 HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countDevice(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchDevice(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                   HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countQQUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchQQUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                   HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countQQTroop(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchQQTroop(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                    HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countWXUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchWXUser(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                   HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countWXChatroom(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchWxChatroom(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                       HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }

    @Override
    public Long countContactPhoneNum(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb) {
        return countES1(indexName, typeName, mmqb, rqb);
    }

    @Override
    public SearchHits searchContactPhoneNum(String indexName, String typeName, QueryBuilder mmqb, RangeQueryBuilder rqb,
                                            HighlightBuilder hb, Integer from, Integer size) {
        return searchES1(indexName, typeName, mmqb, rqb, hb, from, size);
    }


    @Override
    public SearchHits searchPre(String indexName, String typeName, PrefixQueryBuilder query) {
        Client client = null;
        try {
            client = esCollectionPool.getClient();
            Long time1 = System.currentTimeMillis();
            SearchResponse response = client.prepareSearch(indexName)
                    .setTypes(typeName)
                    .setQuery(query)
                    .setFrom(0)
                    .setSize(10)
                    .execute()
                    .actionGet();
            Long time2 = System.currentTimeMillis();
            System.out.println("耗时>>>>>>>>>>>:" + (time2 - time1));
            return response.getHits();
            //判断是否有时间范围
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (client != null) {
                esCollectionPool.release(client);
            }
        }
        return null;
    }
}
