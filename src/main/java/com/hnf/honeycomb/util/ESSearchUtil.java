package com.hnf.honeycomb.util;

import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用于es中搜索的工具类
 *
 * @author yy
 */
@Component
public class ESSearchUtil {

    /**
     * es索引库名称
     */
    public static String INDEX_NAME = "infodata";
    /**
     * wx群聊天信息的索引type类型
     */
    public static String WXCHATROOMMSG_TYPE = "wxChatroomMsg";
    /**
     * qq好友聊天信息的索引type类型
     */
    public static String QQMSG_TYPE = "qqmsg";
    /**
     * wx好友聊天信息索引的type类型
     */
    public static String WXMSG_TYPE = "wxmsg";
    /**
     * QQ群聊天信息索引的type类型
     */
    public static String QQTROOPMSG_TYPE = "qqTroopMsg";
    /**
     * 短消息信息索引的type类型
     */
    public static String MESSAGE_TYPE = "message";

    public static String RECORD_CALL_TYPE = "record";

    /**
     * 案件的索引名称
     */
    public static String CASE_INDEX_NAME = "infodata2";
    /**
     * 案件的类型
     */
    public static String CASE_TYPE = "t_case";
    /**
     * 设备的索引名称
     */
    public static String DEVICE_INDEX_NAME = "infodata2";
    /**
     * 设备的索引类型
     */
    public static String DEVICE_TYPE = "t_device";
    /**
     * 人员的索引名称
     */
    public static String PERSON_INDEX_NAME = "infodata2";
    /**
     * 人员的索引类型
     */
    public static String PERSON_TYPE = "t_person";
    /**
     * 通讯录的索引名称
     */
    public static String CONTACT_INDEX_NAME = "infodata2";
    /**
     * 通讯录的索引类型
     */
    public static String CONTACT_TYPE = "t_contact_phonenum";
    /**
     * qq用户的索引名称
     */
    public static String QQUSER_INDEX_NAME = "infodata2";
    /**
     * qq用户的索引类型
     */
    public static String QQUSER_TYPE = "t_qquser";
    /**
     * wx用户的索引名称
     */
    public static String WXUSER_INDEX_NAME = "infodata2";
    /**
     * wx用户的索引类型
     */
    public static String WXUSER_TYPE = "t_wxuser";
    /**
     * qq群的索引名称
     */
    public static String QQTROOP_INDEX_NAME = "infodata2";
    /**
     * qq群的索引类型
     */
    public static String QQTROOP_TYPE = "t_qq_troop";
    /**
     * wx群的索引名称
     */
    public static String WXTROOP_INDEX_NAME = "infodata2";
    /**
     * wx群的索引类型
     */
    public static String WXTROOP_TYPE = "t_wxchatroom";

    /**
     * 通过高亮的字段名，获取对应的高亮搜索条件
     *
     * @param fields 高亮字段名的list集合
     * @param highlightOnOtherFields 在给定的字段名之外也进行高亮
     * @return 返回用于es搜索的高亮条件
     */
    public static HighlightBuilder getHighLightBuilder(
        List<String> fields,
        final boolean highlightOnOtherFields
    ) {
        HighlightBuilder hb = new HighlightBuilder();
        fields.forEach(hb::field);
        hb.requireFieldMatch(!highlightOnOtherFields);
        hb.preTags("<span style=\"color:red;font-size:14px\" class=\"hight-light\">");
        hb.postTags("</span>");
        return hb;
    }

    /**
     * 通过高亮对象获取其的字符串值
     *
     * @param hf 高亮对象
     * @return 返回高亮后的字符串
     */
    public static String getHightLightStr(HighlightField hf) {
        Text[] fragments = hf.fragments();
        StringBuilder name = new StringBuilder();
        for (Text text : fragments) {
            name.append(text);
        }
        return name.toString();
    }

    /**
     * 通过高亮对象获取其的字符串值
     *
     * @param hf 高亮对象
     * @return 返回高亮后的字符串
     */
    public static String getHightLightStr(String search ,String hf) {
        if(hf == null){
            return null;
        }
        return hf.replaceAll(search,"<span style=\"color:red;font-size:14px\" class=\"hight-light\">" + search + "</span>");
    }

    /**
     * 将对应的高亮显示的字符串转换为对应的正常字符串
     *
     * @param hightLightStr 高亮显示后的字符串
     * @return 返回转换后的字符串
     */
    public static String hightLightStrToNormal(String hightLightStr) {
        return hightLightStr.replace("<span style=\"color:red;font-size:14px\" class=\"hight-light\">",
                "").replace("</span>", "");
    }

    public QueryStringQueryBuilder getQueryStringBuilder(String search, List<String> filedNames) {
        String[] strs = search.split(" ");
        StringBuilder stringBuilder = new StringBuilder();
        if (search.length() == 3) {
            //针对搜索名字手动分词--- 如黎生军  拆分黎生 军 进行搜索
            String head = search.substring(0, 2);
            String tail = search.substring(2, search.length());
            stringBuilder.append("+\"");
            stringBuilder.append(head);
            stringBuilder.append("\"");
            stringBuilder.append("+\"");
            stringBuilder.append(tail);
            stringBuilder.append("\"");
            String newStr = stringBuilder.toString();
            QueryStringQueryBuilder queryStringBuilder = QueryBuilders.queryStringQuery(newStr);
            for (String field : filedNames) {
                queryStringBuilder = queryStringBuilder.field(field);
            }
            return queryStringBuilder;
        }
        for (String str : strs) {
            if (str != null && !str.trim().isEmpty()) {
                stringBuilder.append(" +\"");
                stringBuilder.append(str);
                stringBuilder.append("\"");
            }
        }
        String newStr = stringBuilder.toString();
        QueryStringQueryBuilder queryStringBuilder = QueryBuilders.queryStringQuery(newStr);
        for (String field : filedNames) {
            queryStringBuilder = queryStringBuilder.field(field);
        }
        return queryStringBuilder;
    }

    public QueryBuilder getQueryBuilder(String search, List<String> filedNames) {
        String[] strs = search.split(" ");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String str : strs) {
            if (str != null && !str.trim().isEmpty()) {
                for (String field : filedNames) {
                    boolQueryBuilder.should(QueryBuilders.matchQuery(field,str));
                }
            }
        }
        return  QueryBuilders.boolQuery().must(boolQueryBuilder);
    }

    /**
     * 对搜索的字段进行like查询
     * @param search 搜索的值
     * @param filedNames 搜索的字段
     * @return QueryBuilder
     */
    public QueryBuilder getQueryBuilderMath(String search, List<String> filedNames) {
        String[] strs = search.trim().split(" ");
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        for (String str : strs) {
            if (str != null && !str.trim().isEmpty()) {
                for (String field : filedNames) {
                    boolQueryBuilder.should(QueryBuilders.wildcardQuery(field,"*"+str+"*"));
                }
            }
        }
        return  QueryBuilders.boolQuery().must(boolQueryBuilder);
    }

//    public  QueryStringQueryBuilder getQueryStringBuilder(String search, List<String> filedNames) {
////        String[] strs = search.split(" ");
////    	System.out.println("连接："+esConnectionPool.getClient());
//        AnalyzeResponse response = null;
//        Client client = null;
//        try{
//             client = esConnectionPool.getClient();
//            if (null != client) {
//                response = client.admin().indices()
//                        .prepareAnalyze(search)//内容
//                        .setAnalyzer("ik_smart")//指定分词器
//                        .execute().actionGet();//执行
//            }
//
//        }catch (Exception e) {
//            e.printStackTrace();
//        }finally {
//            if (null != client) {
//                esConnectionPool.release(client);
//            }
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        if (null != response) {
//        List<AnalyzeToken> tokens = response.getTokens();
//        if (!CollectionUtils.isEmpty(tokens)) {
//        	tokens.forEach(t->{
////        		System.out.println("分词结果："+t.getTerm());
////           	 stringBuilder.append(" +\"");
//              stringBuilder.append(t.getTerm());
////              stringBuilder.append("\"");
//           });
//		}
//        }
//        String newStr = stringBuilder.toString();
//        QueryStringQueryBuilder queryStringBuilder = QueryBuilders.queryStringQuery(newStr);
//        for (String field : filedNames) {
//            queryStringBuilder = queryStringBuilder.field(field);
//        }
//        return queryStringBuilder;
//    }
//

}
