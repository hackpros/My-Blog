package com.my.blog.website.crawler;

import com.my.blog.website.constant.ArticleCateEnum;
import com.my.blog.website.crawler.ocr.OCR;
import com.my.blog.website.dao.FilmMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.modal.Vo.Film;
import com.my.blog.website.modal.Vo.FilmQueryHelper;
import com.my.blog.website.modal.constants.FilmHelper;
import com.my.blog.website.service.IContentService;
import com.rarchives.ripme.utils.Http;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.server.ExportException;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Description: 影视资源</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: jumore</p>
 *
 * @author fan(renntrabbit @ foxmail.com) created by  2018/3/20 16:26
 */
@Component
public class RarbgMovieSource implements MoviePlate {

    //static String httpsURLAsString = "https://rarbg.is/torrents.php?category=movies";
    /**前100名*/
    static String httpsURLAsString ="https://rarbg.is/top100.php?category[]=14&category[]=15&category[]=16&category[]=17&category[]=21&category[]=22&category[]=42&category[]=44&category[]=45&category[]=46&category[]=47&category[]=48";
    static String httpsURLAsStringSecondStep = "https://rarbg.is/threat_defence.php?defence=2&sk={0}&cid={3}&i={4}&ref_cookie={1}&r={2}";
    static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400";
    protected final Logger log = Logger.getLogger(this.getClass());
    @Resource
    private IContentService contentService;
    @Resource
    private FilmMapper filmMapper;

    /**
     * 获取指定HTML标签的指定属性的值
     *
     * @param source  要匹配的源文本
     * @param element 标签名称
     * @param attr    标签的属性名称
     * @return 属性值列表
     */
    public static List<String> match(String source, String element, String attr) {
        List<String> result = new ArrayList<String>();
        String reg = "<" + element + "[^<>]*?\\s" + attr + "=['\"]?(.*?)['\"]?(\\s.*?)?>";
        Matcher m = Pattern.compile(reg).matcher(source);
        while (m.find()) {
            String r = m.group(1);
            /**去掉斜杠单引号*/
            r = r.replaceAll("\\\\|'", "");
            result.add(r);
        }
        return result;
    }

    public static void main(String[] args) {
        String source = "return overlib('<img src=\\'//dyncdn.me/mimages/40536/over_opt.jpg\\' border=0>'";
        List<String> list = match(source, "img", "src");
        System.out.println(list);
    }

    //@Scheduled(cron = "0 0/1 * * * ?")
    @Scheduled(cron = " 0 0 0/1 * * ?")
    public void start() throws IOException, InterruptedException {
        log.info("start task....");
        Connection connection = null;
        /**
         * 递归获取资源
         */
        connection = this.getResponse(connection);
        Document doc = Jsoup.parse(connection.response().body());
        Elements elements = doc.select("table .lista2t tr");
        List<ContentVo> contentVos = new ArrayList<ContentVo>();
        log.info("get ["+elements.size()+"] movie resource..");
        /**移除第一个无素，第一个是表头*/
        if (elements.size()>0){
            elements.remove(0);
        }
        for (Element e : elements) {
            ContentVo vo = new ContentVo();

            String title = e.select("td:eq(1)").select("a").attr("title");
            String href = "https://rarbg.is" + e.select("td:eq(1)").select("a").attr("href");
            //String addedTime = e.select("td:eq(2)").text();
            //String size = e.select("td:eq(3)").text();
            String thumbnail = e.select("td:eq(1)").select("a").attr("onmouseover");
            thumbnail = "https:" + match(thumbnail, "img", "src").get(0);

            this.getDescription(href, connection.request().cookies(), vo);

            /**如果数据已抓取，就不处理了*/
            FilmQueryHelper exp=new  FilmQueryHelper();
            exp.createCriteria().andSrcEqualTo(vo.getFilm().getSrc());
            if (filmMapper.countByExample(exp)>0){
                break;
            }

            vo.setTitle(title);
            vo.setAuthorId(1);
            vo.setType(Types.ARTICLE.getType());
            vo.setCategories(ArticleCateEnum.FILMS.name().toLowerCase());
            vo.setAllowComment(false);
            vo.setStatus("publish");
            vo.setThumbnail(thumbnail);
            contentVos.add(vo);
        }
        contentService.batchAppend(contentVos);

    }

    private Connection getResponse(Connection connection) throws IOException, InterruptedException {
        Document doc = null;
        if (connection == null) {
            log.info("1.0 打开目录网站首页");
            connection = Jsoup.connect(httpsURLAsString)
                    .userAgent(userAgent)
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .timeout(60000).validateTLSCertificates(false)
                    .method(Connection.Method.GET);
            connection.execute();
            if (connection.response().statusCode() != HttpStatus.OK.value()) {
                log.error(connection.response().statusMessage());
            }
            return this.getResponse(connection);
        }
        if (connection.response().url().toString().startsWith("https://rarbg.is/torrents.php?category=movies")) {
            return connection;
        } else if (connection.response().url().toString().startsWith("https://rarbg.is/threat_defence.php?defence=1")) {
            log.info("2.0 open step 2 write cookies..");
            doc = Jsoup.parse(connection.response().body());
            Element script = doc.select("script").get(1); // Get the script part
            /**
             * 参数一
             */
            String value_sk = "r8l5i0vmpc";
            Pattern p = Pattern.compile("(?is)value_sk = '(.+?)'"); // Regex for the value of the key
            Matcher m = p.matcher(script.html()); // you have to use html here and NOT text! Text will drop the 'key' part
            while (m.find()) {
                log.info(m.group()); // the whole key ('key = value')
                log.info(m.group(1)); // value only
                value_sk = m.group(1);
            }
            /**
             * 参数二
             */
            String value_c = "";
            p = Pattern.compile("(?is)value_c = '(.+?)'"); // Regex for the value of the key
            m = p.matcher(script.html()); // you have to use html here and NOT text! Text will drop the 'key' part
            while (m.find()) {
                log.info(m.group()); // the whole key ('key = value')
                log.info(m.group(1)); // value only
                value_c = m.group(1);
            }
            /**
             * 参数三
             */
            String value_i = "";
            p = Pattern.compile("(?is)value_i = '(.+?)'"); // Regex for the value of the key
            m = p.matcher(script.html()); // you have to use html here and NOT text! Text will drop the 'key' part
            while (m.find()) {
                log.info(m.group()); // the whole key ('key = value')
                log.info(m.group(1)); // value only
                value_i = m.group(1);
            }

            int days = 7;
            Date date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            Map<String, String> cookies = connection.response().cookies();
            cookies.put("sk", value_sk);
            cookies.put("expires", date.toGMTString());
            cookies.put("path", "/");
            cookies.put("aby", "2");
            log.info("2.1 send ajax request..");
            Connection.Response res = Jsoup.connect("https://rarbg.is/threat_defence_ajax.php?sk=" + value_sk + "&cid=" + value_c + "&i=" + value_i + "&r=" + RandomStringUtils.randomNumeric(8))
                    .timeout(60000).validateTLSCertificates(false)
                    .execute();
            if (res.statusCode() != HttpStatus.OK.value()) {
                log.error(connection.response().statusMessage());
            }
            Thread.sleep(1000L);
            log.info("2.2 open captcha page..");
            connection = Jsoup.connect(MessageFormat.format(httpsURLAsStringSecondStep, value_sk, "rarbg.is", RandomStringUtils.randomNumeric(8), value_c, value_i))
                    .userAgent(userAgent)
                    .header("Host", "rarbg.is")
                    .timeout(60000).validateTLSCertificates(false)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .followRedirects(false);
            connection.execute();
            if (connection.response().statusCode() != HttpStatus.OK.value()) {
                log.error(connection.response().statusMessage());
                throw new IOException("get captcha page fail");
            }
            return this.getResponse(connection);

        } else if (connection.response().url().toString().startsWith("https://rarbg.is/threat_defence.php?defence=nojc")) {
            connection = Jsoup.connect("https://rarbg.is" + "/threat_defence.php?defence=1")
                    .userAgent(userAgent)
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .cookies(connection.response().cookies())
                    .timeout(60000).validateTLSCertificates(false)
                    .method(Connection.Method.GET);
            connection.execute();
            log.info(connection.response().body());
            return this.getResponse(connection);
        } else if (connection.response().url().toString().startsWith("https://rarbg.is/threat_defence.php?defence=2")) {
            log.info("2.3  use OCR see captcha..");
            doc = Jsoup.parse(connection.response().body());
            String fromActionUrl = "https://rarbg.is" + doc.select("form").attr("action");
            String captcha_id = doc.select("form input[name=captcha_id]").attr("value");
            String defence = doc.select("form input[name=defence]").attr("value");
            String sk = doc.select("form input[name=sk]").attr("value");
            String cid = doc.select("form input[name=cid]").attr("value");
            String i = doc.select("form input[name=i]").attr("value");
            String ref_cookie = doc.select("form input[name=ref_cookie]").attr("value");
            String r = doc.select("form input[name=r]").attr("value");
            String submitted_bot_captcha = doc.select("form input[name=submitted_bot_captcha]").attr("value");

            if (StringUtils.isEmpty(captcha_id)) {
                log.error("get param from form error");
                throw new ExportException("get param from form error");
            }
            String solve_string = getCaptchaValue(captcha_id);
            Map<String, String> map = new HashMap<String, String>();
            map.put("defence", defence);
            map.put("sk", sk);
            map.put("cid", cid);
            map.put("i", i);
            map.put("ref_cookie", ref_cookie);
            map.put("r", r);
            map.put("submitted_bot_captcha", submitted_bot_captcha);
            map.put("captcha_id", captcha_id);
            map.put("solve_string", solve_string);
            connection = Jsoup.connect(fromActionUrl)
                    .data(map)
                    .method(Connection.Method.GET)
                    .userAgent(userAgent)
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .cookies(connection.response().cookies())
                    .timeout(60000).validateTLSCertificates(false);
            connection.execute();
            if (connection.response().statusCode() != HttpStatus.OK.value()) {
                log.error(connection.response().statusMessage());
                throw new IOException("use captcha request netx page error");
            }
            return this.getResponse(connection);
        } else if (connection.response().url().toString().startsWith("https://rarbg.is/torrents.php?r=")) {
            log.info("3.0 get movie resource");
            connection = Jsoup.connect(httpsURLAsString)
                    .userAgent(userAgent)
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .cookies(connection.response().cookies())
                    .timeout(60000).validateTLSCertificates(false)
                    .followRedirects(true)
                    .method(Connection.Method.GET);
            connection.execute();
            if (connection.response().statusCode() != HttpStatus.OK.value()) {
                log.error(connection.response().statusMessage());
                throw new IOException("get movie resource error");
            }
            return this.getResponse(connection);
        }
        return connection;
    }


    private static void trustAllHttpsCertificates() throws Exception {
        javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
        javax.net.ssl.TrustManager tm = new Pkix.miTM();
        trustAllCerts[0] = tm;
        javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext
                .getInstance("SSL");
        sc.init(null, trustAllCerts, null);
        javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc
                .getSocketFactory());
    }

    private String getCaptchaValue(String captcha_id_value) {
        String captchaFileRemoteAsString = "https://rarbg.is/captcha2/" + captcha_id_value + ".png";
        log.info("the captcha file path: " + captchaFileRemoteAsString);
        try {


            trustAllHttpsCertificates();
            HostnameVerifier hv = new HostnameVerifier() {
                public boolean verify(String urlHostName, SSLSession session) {
                    System.out.println("Warning: URL Host: " + urlHostName + " vs. "
                            + session.getPeerHost());
                    return true;
                }
            };
            HttpsURLConnection.setDefaultHostnameVerifier(hv);

            URL url = new URL(captchaFileRemoteAsString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);


            connection.connect(); //访问图片
            BufferedImage imageBuffer = ImageIO.read(connection.getInputStream());
            File captchaFileAsLocal = new File("d:/captcha/" + captcha_id_value + ".png");
            ImageIO.write(imageBuffer, "png", captchaFileAsLocal);
            String code = StringUtils.trim(new OCR().recognizeText(captchaFileAsLocal, "png"));
            log.info("this captcha code: " + code);
            return code;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "12345";

    }

    private void getDescription(String descURLAsString, Map<String, String> cookies, ContentVo vo) throws IOException {
        log.info("5.0 get everyone resource detail page info");
        Connection.Response resp = Http.url(descURLAsString)
                .userAgent(userAgent)
                .header("Host", "rarbg.is")
                .header("Referer", "https://rarbg.is/torrents.php")
                .cookies(cookies)
                .timeout(60000)
                .method(Connection.Method.GET)
                .response();
        if (resp.statusCode() != HttpStatus.OK.value()) {
            log.error(resp.statusMessage());
            throw new IOException("get everyone resource detail page info error");
        }
        Document doc = Jsoup.parse(resp.body());
        Element e = doc.select("#description").first();
        String description = e.text() == null ? "" : e.html();
        vo.setContent(description);
        Film film = new Film();
        vo.setFilm(film);

        Elements elements=doc.select("div table .lista tr");
        elements.stream().forEach(ele->{
            if (ele.select("td:eq(0)").text().startsWith("Torrent")){
                film.setDownPath(ele.select("td:eq(1) a:eq(2)").attr("href"));
            }
            if (ele.select("td:eq(0)").text().startsWith("Poster")){
                film.setPoster(ele.select("td:eq(1) img").attr("src"));
            }
            if (ele.select("td:eq(0)").text().startsWith("Others")){
                film.setOthers(ele.select("td:eq(1)").text());
            }
            if (ele.select("td:eq(0)").text().startsWith("Size")) {
                film.setSize(ele.select("td:eq(1)").text());
            }
            if (ele.select("td:eq(0)").text().startsWith("Title")) {
                film.setTitle(ele.select("td:eq(1)").text());
            }
            if (ele.select("td:eq(0)").text().startsWith("Year")) {
                film.setYear(ele.select("td:eq(1)").text());
            }
            if (ele.select("td:eq(0)").text().startsWith("Plot")) {
                film.setPlot(ele.select("td:eq(1)").text());
            }
            film.setSrc(descURLAsString);
            film.setStatus(FilmHelper.EStatus.UN_TRANS.getStatus());
        });



    }

}

