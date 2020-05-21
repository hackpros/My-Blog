package com.my.blog.website.crawler;

import com.my.blog.website.component.ApplicationContextProvider;
import com.my.blog.website.crawler.base.MoviePlate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * <p>Description: 数据爬取模块</p>
 * <p>Copyright: Copyright (c) 2018</p>
 *
 * @author fan(renntrabbit @ foxmail.com) created by  2018/3/20 16:24
 */
@Component
public class CrawkerService {

    public enum TarSite {
        T66Y(T66ySource.class),
        DIU_BAN(Socket5Test.class),
        RAR(RarbgMovieSource.class),
        SS(Socket5Test.class);
        private final Class cls;

        TarSite(Class sourceClass) {
            this.cls = sourceClass;
        }
    }

    @Resource
    ApplicationContextProvider applicationContextProvider;

    /**
     * 执爬程虫
     */
    @Async
    public void doExcute(TarSite s) throws IOException, InterruptedException {
        MoviePlate moviePlate = (MoviePlate) applicationContextProvider.getContext().getBean(s.cls);
        moviePlate.start();
    }

}
