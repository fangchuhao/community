package com.example.demo.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussPostRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    public void savePost(DiscussPost discussPost) {
        discussPostRepository.save(discussPost);
    }

    public void deletePost(int id) {
        discussPostRepository.deleteById(id);
    }

    public Page<DiscussPost> searchPost(String keyword,int currentPage,int limit) {
        SearchQuery searchQuery=new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword,"title","content"))
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC))
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(currentPage,limit))
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
        return search;
    }
}

