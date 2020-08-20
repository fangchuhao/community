package com.example.demo;

import com.example.demo.dao.DiscussPostMapper;
import com.example.demo.dao.elasticsearch.DiscussPostRepository;
import com.example.demo.entity.DiscussPost;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = DemoApplication.class)
public class ElasticsearchTest {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Test
    public void test1() {
        discussPostRepository.save(discussPostMapper.selectDisByDisId(241));
        discussPostRepository.save(discussPostMapper.selectDisByDisId(242));
        discussPostRepository.save(discussPostMapper.selectDisByDisId(243));
    }

    @Test
    public void test2() {
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(101,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(102,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(103,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(111,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(112,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(131,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(132,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(133,0,100,0));
        discussPostRepository.saveAll(discussPostMapper.selectDiscussPosts(134,0,100,0));
    }

    @Test
    public void test3() {
        DiscussPost discussPost = discussPostMapper.selectDisByDisId(231);
        discussPost.setContent("新人灌水失败");
        discussPostRepository.save(discussPost);
    }

    @Test
    public void test4() {
        //discussPostRepository.deleteById(231);
        discussPostRepository.deleteAll();
    }

    @Test
    public void test5() {
        // 关键词 排序 分页 高亮
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> search = discussPostRepository.search(searchQuery);
        System.out.println("一共"+search.getTotalElements()+"条数据");
        System.out.println("一共"+search.getTotalPages()+"页");
        System.out.println("当前是第"+search.getNumber()+"页");
        System.out.println("每一页有"+search.getSize()+"条数据");
        for(DiscussPost discussPost:search) {
            System.out.println(discussPost);
        }
    }

    @Test
    public void test6() {
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬","title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0,10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        Page<DiscussPost> search=elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();
                long totalHits = hits.getTotalHits();
                if(totalHits<=0) {
                    return null;
                }
                List<DiscussPost> list=new ArrayList<>();
                for(SearchHit hit:hits) {
                    DiscussPost discussPost=new DiscussPost();
                    Map<String, Object> map = hit.getSourceAsMap();
                    discussPost.setId((Integer) map.get("id"));
                    discussPost.setUserId((Integer) map.get("userId"));
                    discussPost.setTitle((String) map.get("title"));
                    discussPost.setContent((String) map.get("content"));

                    String createTime = map.get("createTime").toString();
                    discussPost.setCreateTime(new Date(Long.valueOf(createTime)));

                    discussPost.setCommentCount((Integer) map.get("commentCount"));

                    String score = map.get("score").toString();
                    discussPost.setScore(Double.valueOf(score));

                    discussPost.setStatus((Integer) map.get("status"));
                    discussPost.setType((Integer) map.get("type"));

                    HighlightField title = hit.getHighlightFields().get("title");
                    if(title!=null) {
                        discussPost.setTitle(title.getFragments()[0].toString());
                    }

                    HighlightField content = hit.getHighlightFields().get("content");
                    if(content!=null) {
                        discussPost.setContent(content.getFragments()[0].toString());
                    }

                    list.add(discussPost);
                }
                return new AggregatedPageImpl(list,pageable,hits.getTotalHits(),searchResponse.getAggregations(),searchResponse.getScrollId(),hits.getMaxScore());
            }
        });
        System.out.println("一共"+search.getTotalElements()+"条数据");
        System.out.println("一共"+search.getTotalPages()+"页");
        System.out.println("当前是第"+search.getNumber()+"页");
        System.out.println("每一页有"+search.getSize()+"条数据");
        for(DiscussPost discussPost:search) {
            System.out.println(discussPost);
        }
    }
}
