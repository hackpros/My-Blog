package com.my.blog.website.crawler;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.my.blog.website.crawler.base.MoviePlate;
import com.my.blog.website.crawler.exception.CrawlerException;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.dao.FilmMapper;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.modal.Vo.Film;
import com.my.blog.website.modal.Vo.FilmQueryHelper;
import com.my.blog.website.modal.constants.FilmHelper;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Description: 对于英文资源的影视.通过douban抓取中文对照
 * 对于不能获取的数据，就使用google，youdao　翻译
 * </p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: jumore</p>
 *
 * @author fan(renntrabbit @ foxmail.com) created by  2018/3/20 16:26
 */
@Component
public class DouBanMovieSource1 implements MoviePlate {

    static String doubanLoginUrl = "https://www.douban.com/accounts/login";

    static String doubanLoginApi = "https://accounts.douban.com/login";

    static String httpsURLAsString = "https://movie.douban.com/subject_suggest";
    static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Safari/537.36";
    protected final Logger log = Logger.getLogger(this.getClass());
    @Resource
    private FilmMapper filmMapper;
    @Resource
    private ContentVoMapper contentVoMapper;

    public static void main(String[] args) throws IOException, InterruptedException {
        Film e = new Film();
        e.setTitle("Pacific Rim: Uprising (2018)");
        e.setYear("2018");
        //ContentVo vo = new DouBanMovieSource().getResponse(e);
    }

    //@Scheduled(cron = "0 0/1 * * * ?")
    //@Scheduled(cron = " 0 0 0/4 * * ?")
    @Override
    public void start() throws IOException {
        /**
         * 跟据已抓取的英文资源进行抓取
         */
        FilmQueryHelper e = new FilmQueryHelper();
        e.createCriteria().andStatusEqualTo(FilmHelper.EStatus.UN_TRANS.getStatus());
        List<Film> films = filmMapper.selectByExample(e);
        if (films.isEmpty()) {
            return;
        }
        Connection[] connection = {null};
        connection[0] = loginDouban();
        films.stream().forEach(item -> {
            try {
                /**
                 * 递归获取jsoup资源,填充数据
                 */
                ContentVo vo = this.getResponse(connection[0], item);
                if (vo != null) {
                    String beforeContent = contentVoMapper.selectByPrimaryKey(vo.getCid()).getContent();
                    vo.setContent(vo.getContent() + "\n" + beforeContent);
                    contentVoMapper.updateByPrimaryKeySelective(vo);
                    Film film = new Film();
                    film.setId(item.getId());
                    film.setStatus(FilmHelper.EStatus.HAS_TRANS.getStatus());
                    filmMapper.updateByPrimaryKeySelective(film);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        });
    }

    private ContentVo fillContent(Connection connection, Film item) {
        Document doc = Jsoup.parse(connection.response().body());
        String title = doc.select("#content h1").text();
        String content = doc.select("#link-report span:eq(0)").text();
        ContentVo contentVo = new ContentVo();
        contentVo.setCid(item.getCid().intValue());
        contentVo.setTitle(title);
        contentVo.setContent(content);
        return contentVo;
    }

    public ContentVo getResponse(Connection connection, Film film) throws IOException, InterruptedException {

        Map<String, String> cookies = connection.response().cookies();
        cookies.put("__utma", "4372.1528777828.1528777828.1528777828.1");
        cookies.put("__utma", "223695111.1847921201.1528686548.1528686548.1528686548.1");
        cookies.put("__utmc", "30149280");
        cookies.put("__utmc", "223695111");
        cookies.put("__utmz", "30149280.1528685618.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        cookies.put("__utmz", "223695111.1528686548.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none)");
        cookies.put("__yadk_uid", "VesRdyOEJOJLbZ2grf8Y4JHKVYZjQdZH");
        cookies.put("_pk_id.100001.4cf6", "bea0b59a0b21b191.1528685618.1.1528686548.1528685618.");
        cookies.put("_vwo_uuid_v2", "D44AE6B72A349DE59A38E2ED2A5A42AE1|0a98bb397b263e953d91ec72e8bbab37");
        cookies.put("ap", "1");
        cookies.put("bid", "JnYYupH52Qs");
        cookies.put("ll", "\"118172\"");
        connection = Jsoup.connect(httpsURLAsString)
                .userAgent(userAgent)
                .data("q", film.getTitle())
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Cache-Control", "max-age=0")
                .header("Connection", "keep-alive")
                .header("Host", "movie.douban.com")
                .header("Upgrade-Insecure-Requests", "1")
                .cookies(cookies)
                .ignoreContentType(true).maxBodySize(0).timeout(25000).followRedirects(true);
        Thread.sleep(3000L);
        connection.execute();
        log.info(connection.response().body());

        if (connection.response().statusCode() == HttpStatus.OK.value()) {
            List<DouBanMoviesList> douBanMoviesList = objectMapper.readValue(connection.response().body(), new TypeReference<List<DouBanMoviesList>>() {
            });//把json转换为Java list
            if (douBanMoviesList == null || douBanMoviesList.isEmpty()) {
                return null;
            }
            for (DouBanMoviesList e : douBanMoviesList) {
                if (e.getYear().equals(film.getYear()) && film.getTitle().contains(e.getSubTitle())) {
                    return getDetailResponse(cookies, e, film);
                }
            }
        }
        return null;
    }

    private Connection loginDouban() throws IOException {
        log.info("login douban , open logn page");
        Connection connection = Jsoup.connect(doubanLoginUrl)
                .userAgent(userAgent)
                .header("Host", "movie.douban.com")
                .timeout(60000)
                .method(Connection.Method.GET);
        connection.execute();
        if (connection.response().statusCode() != HttpStatus.OK.value()) {
            log.error("open login error:");
            throw new CrawlerException(connection.response().statusMessage());
        }

        log.info("get login page element");
        Document doc = Jsoup.parse(connection.response().body());
        String source = doc.select("#lzform input[name=source]").val();
        String redir = doc.select("#lzform input[name=redir]").val();
        String form_email = "fans_2046@126.com";
        String form_password = "1q2wazsx";

        Map<String, String> formData = new HashMap<String, String>();
        formData.put("source", source);
        formData.put("redir", redir);
        formData.put("form_email", form_email);
        formData.put("form_password", form_password);

        connection = Jsoup.connect(doubanLoginApi)
                .userAgent(userAgent)
                .header("Host", "movie.douban.com")
                .timeout(60000)
                .data(formData)
                .method(Connection.Method.POST);
        connection.execute();
        if (connection.response().statusCode() != HttpStatus.OK.value()) {
            log.info("login doubon api errpr:");
            throw new CrawlerException(connection.response().statusMessage());
        }
        return connection;
    }

    private ContentVo getDetailResponse(Map<String, String> cookies, DouBanMoviesList e, Film film) throws IOException, InterruptedException {
        Connection connection = Jsoup.connect(e.getUrl())
                .userAgent(userAgent)
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8")
                .header("Accept-Encoding", "gzip, deflate, br")
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .header("Cache-Control", "max-age=0")
                .header("Connection", "keep-alive")
                .header("Host", "movie.douban.com")
                .header("Upgrade-Insecure-Requests", "1")
                .timeout(60000)
                .cookies(cookies)
                .method(Connection.Method.GET);
        Thread.sleep(3000L);
        connection.execute();
        if (connection.response().statusCode() != HttpStatus.OK.value()) {
            return null;
        }
        return fillContent(connection, film);
    }

    public static class DouBanMoviesList implements Serializable {
        private String episode;
        private String img;
        private String title;
        private String url;
        private String type;
        private String year;
        @JsonProperty(value = "sub_title")
        private String subTitle;
        private Long id;

        public String getEpisode() {
            return episode;
        }

        public void setEpisode(String episode) {
            this.episode = episode;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getYear() {
            return year;
        }

        public void setYear(String year) {
            this.year = year;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }
    }


}

