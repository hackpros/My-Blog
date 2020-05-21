package com.my.blog.website.component;

import com.google.api.client.util.Charsets;
import com.google.common.hash.BloomFilter;
import com.google.common.hash.Funnels;
import com.my.blog.website.constant.ArticleCateEnum;
import com.my.blog.website.service.IContentService;
import com.my.blog.website.service.IFilmService;
import com.my.blog.website.service.impl.ContentServiceImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class BloomFilterService {
    /***一百万个元素,误判率 0.003 默认值*/
    private int insertions = 100 * 10000;
    private BloomFilter<String> filter = BloomFilter.create(Funnels.stringFunnel(Charsets.UTF_8), insertions);
    @Resource
    private IFilmService filmService;
    @Resource
    ApplicationContextProvider applicationContextProvider;

    @Async
    public void init() {
        List<String> urlList = filmService.selectUrlAll();
        IContentService contentService = applicationContextProvider.getContext().getBean(IContentService.class);
        urlList.addAll(contentService.selectUrlAll(ArticleCateEnum.MESSY));
        urlList.parallelStream().forEach(e -> {
            filter.put(e);
        });
    }

    /**
     * 添加一个
     *
     * @param url
     * @throws Exception
     */
    public void addOne(String url) {
        filter.put(url);
    }

    public boolean mightContain(String url) {
        return filter.mightContain(url);
    }


}