package com.my.blog.website.crawler;

import com.my.blog.website.constant.ArticleCateEnum;
import com.my.blog.website.constant.ArticleStatusEnum;
import com.my.blog.website.dao.FilmMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.service.IContentService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>Description: 小草资原</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: jumore</p>
 *
 * @author fan(renntrabbit @ foxmail.com) created by  2018/3/20 16:26
 */
@Component
public class T66ySource implements MoviePlate {

    /**技术讨论区*/
    /**
     * fid :表示区
     * search->"":所有主題
     * digest:本版精華區
     * 1:1天內的主題
     * 2:2天內的主題
     * 7:1星期內的主題
     * 30:1個月內的主題
     * 60:2個月內的主題
     * 90:3個月內的主題
     * 180:6個月內的主題
     * 365:1年內的主題
     */

    static String httpsURLAsString = "https://www.t66y.com/thread0806.php?fid=7&search=1&page=1";
    static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400";
    protected final Logger log = Logger.getLogger(this.getClass());

    @Resource
    private IContentService contentService;

    public static void main(String[] args) throws Exception {
        T66ySource t = new T66ySource();
        t.start();
    }


    @Scheduled(cron = "0 5 23 * * ?", zone = "Asia/Shanghai")
    //@Scheduled(cron = "0 */1 * * * ?", zone = "Asia/Shanghai")
    public void start() throws IOException, InterruptedException {
        log.info("start task....");
        List<ContentVo> contentVos = new ArrayList<ContentVo>();
        log.info("open 技术讨论区 一天内的主题列表");
        Document doc = this.getDoc(httpsURLAsString);
        Elements elements = doc.select("#ajaxtable tbody:eq(1) tr.tr2 td.tac").parents().nextAll();
        int i = 1;
        for (Element e : elements) {
            String title = e.select("td:eq(1)").select("a").text();
            /**
             * 先简单的过滤一下 h内容 ,后面想用es分词技术同时存到es中，便于搜索，学习es技术
             */
            if (title.contains("AV") || title.contains("女优") || title.contains("男优") || title.contains("番号")) {
                continue;
            }
            String srcUrl = "http://www.t66y.com/" + e.select("td:eq(0) a").attr("href");
            //srcUrl="http://www.t66y.com/htm_data/7/1807/3207163.html";
            //srcUrl="http://www.t66y.com/htm_data/7/1807/3207075.html";
            ContentVo vo = new ContentVo();
            vo.setTitle(title);
            this.getContents(srcUrl, vo);
            vo.setTitle(title);
            vo.setAuthorId(1);
            vo.setType(Types.ARTICLE.getType());
            vo.setCategories(ArticleCateEnum.MESSY.name().toLowerCase());
            vo.setAllowComment(false);
            vo.setStatus(ArticleStatusEnum.draft.name());
            contentVos.add(vo);
            contentService.publish(vo);
            log.info("add  [" + (i++) + "/" + elements.size() + "] theme  success..");
        }
    }

    /**
     * 获取详情页
     *
     * @param srcUrl
     * @param vo
     */
    public void getContents(String srcUrl, ContentVo vo) throws IOException {
        log.info("open detail ");
        log.debug(vo.getTitle());
        Document doc = this.getDoc(srcUrl);
        Elements elements = doc.select("table table tbody tr");
        if (elements.size() > 0) {
            Element e = elements.get(1);
            e.select("div.tiptop").remove();
            log.debug(e.html());
            vo.setContent(e.html());
        }


    }

    /**
     * 获取列表
     *
     * @return
     * @throws IOException
     */
    private Document getDoc(String url) throws IOException {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new Socket5Test.MyConnectionSocketFactory())
                .register("https", new Socket5Test.MySSLConnectionSocketFactory(SSLContexts.createSystemDefault())).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg, new Socket5Test.FakeDnsResolver());
        CloseableHttpClient httpclient = HttpClients.custom().setConnectionManager(cm).build();
        CloseableHttpResponse response = null;
        try {
            /**使用socket5代理　shadowsocks-netty　自己维护代理池*/
            //InetSocketAddress socksAddr = new InetSocketAddress("127.0.0.1", 4108);
            HttpClientContext context = HttpClientContext.create();
            //context.setAttribute("socks.address", socksAddr);
            HttpGet request = new HttpGet(url);
            request.setHeader("User-Agent", userAgent);
            response = httpclient.execute(request, context);
            /**检查是是请求成功*/
            if (response.getStatusLine().getStatusCode() != HttpStatus.OK.value()) {
                //todo mq消息　不成功通知代理池理
            }
            /***乱码处理*/
            String result = EntityUtils.toString(response.getEntity(), "GBK");
            Document docAsList = Jsoup.parse(result);
            return docAsList;
        } catch (Exception e) {
            return new Document("");
        } finally {
            httpclient.close();
            if (response != null) {
                response.close();
            }
        }
    }

}

