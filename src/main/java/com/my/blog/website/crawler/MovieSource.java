package com.my.blog.website.crawler;

import com.my.blog.website.crawler.ocr.OCR;
import com.my.blog.website.dto.Types;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.service.IContentService;
import com.rarchives.ripme.utils.Http;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Description: 影视资源</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: jumore</p>
 *
 * @author fan(renntrabbit@foxmail.com) created by  2018/3/20 16:26
 */
@Component
public class MovieSource {
    static String httpsURLAsString = "https://rarbg.is/torrents.php?category=movies";
    static String httpsURLAsStringSecondStep = "https://rarbg.is/threat_defence.php?defence=2&sk={0}&ref_cookie={1}&r={2}";

    private final static String DATA_PATH = "D:\\Tesseract-OCR\\tessdata\\";

    @Resource
    private IContentService contentService;

    @Scheduled(cron = "0 0/5 * * * ? ")
    public void start() throws IOException {


        Connection connection = null;
        /**
         * 递归获取资源
         */
        connection = this.getResponse(connection);
        Document doc = Jsoup.parse(connection.response().body());
        Elements elements = doc.select("table .lista2t tr");
        List<ContentVo> contentVos = new ArrayList<ContentVo>();
        /**移除第一个无素，第一个是表头*/
        elements.remove(0);
        for (Element e : elements) {
            ContentVo vo = new ContentVo();

            String title = e.select("td:eq(1)").select("a").attr("title");
            String href = "https://rarbg.is" + e.select("td:eq(1)").select("a").attr("href");
            String addedTime = e.select("td:eq(2)").text();
            String size = e.select("td:eq(3)").text();
            String description = this.getDescription(href, connection.request().cookies());
            vo.setTitle(title);
            vo.setContent(description);
            vo.setAuthorId(1);
            vo.setType(Types.ARTICLE.getType());
            vo.setCategories("默认分类");
            vo.setAllowComment(false);
            vo.setStatus("publish");
            contentVos.add(vo);
        }
        contentService.batchAppend(contentVos);

    }

    private Connection getResponse(Connection connection) throws IOException {
        Document doc = null;
        if (connection == null) {
            connection = Jsoup.connect(httpsURLAsString)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400")
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .timeout(60000)
                    .method(Connection.Method.GET);
            connection.execute();
            System.out.println(connection.response().body());
            return this.getResponse(connection);
        }
        if (connection.response().url().toString().startsWith("https://rarbg.is/torrents.php?category=movies")) {
            return connection;
        } else if (connection.response().url().toString().startsWith("https://rarbg.is/threat_defence.php?defence=1")) {
            doc = Jsoup.parse(connection.response().body());
            Element script = doc.select("script").first(); // Get the script part
            String value_sk = "r8l5i0vmpc";
            Pattern p = Pattern.compile("(?is)value_sk = '(.+?)'"); // Regex for the value of the key
            Matcher m = p.matcher(script.html()); // you have to use html here and NOT text! Text will drop the 'key' part
            while (m.find()) {
                System.out.println(m.group()); // the whole key ('key = value')
                System.out.println(m.group(1)); // value only
                value_sk = m.group(1);
            }
            int days = 7;
            Date date = new Date();
            date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
            Map<String, String> cookies = connection.response().cookies();
            cookies.put("sk", value_sk);
            cookies.put("expires", date.toGMTString());
            cookies.put("path", "/");
            cookies.put("aby", "2");

            connection = Jsoup.connect(MessageFormat.format(httpsURLAsStringSecondStep, value_sk, "rarbg.is", RandomStringUtils.randomNumeric(8)))
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400")
                    .header("Host", "rarbg.is")
                    .timeout(60000)
                    .method(Connection.Method.GET)
                    .cookies(cookies)
                    .followRedirects(false);
            connection.execute();
            System.out.println(connection.response().body());
            return this.getResponse(connection);

        } else if (connection.response().url().toString().startsWith("https://rarbg.is/threat_defence.php?defence=nojc")) {
            connection = Jsoup.connect("https://rarbg.is" + "/threat_defence.php?defence=1")
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400")
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .cookies(connection.response().cookies())
                    .timeout(60000)
                    .method(Connection.Method.GET);
            connection.execute();
            System.out.println(connection.response().body());
            return this.getResponse(connection);
        } else if (connection.response().url().toString().startsWith("https://rarbg.is/threat_defence.php?defence=2")) {
            doc = Jsoup.parse(connection.response().body());
            String fromActionUrl = "https://rarbg.is" + doc.select("form").attr("action");
            String captcha_id = doc.select("form input[name=captcha_id]").attr("value");
            String defence = doc.select("form input[name=defence]").attr("value");
            String sk = doc.select("form input[name=sk]").attr("value");
            String ref_cookie = doc.select("form input[name=ref_cookie]").attr("value");
            String r = doc.select("form input[name=r]").attr("value");
            String submitted_bot_captcha = doc.select("form input[name=submitted_bot_captcha]").attr("value");

            String solve_string = getCaptchaValue(captcha_id);

            Map<String, String> map = new HashMap<String, String>();
            map.put("defence", defence);
            map.put("sk", sk);
            map.put("ref_cookie", ref_cookie);
            map.put("r", r);
            map.put("submitted_bot_captcha", submitted_bot_captcha);
            map.put("captcha_id", captcha_id);
            map.put("solve_string", solve_string);
            connection = Jsoup.connect(fromActionUrl)
                    .data(map)
                    .method(Connection.Method.GET)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400")
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .cookies(connection.response().cookies())
                    .timeout(60000);
            connection.execute();
            System.out.println(connection.response().body());
            return this.getResponse(connection);
        } else if (connection.response().url().toString().startsWith("https://rarbg.is/torrents.php?r=")) {


            connection = Jsoup.connect(httpsURLAsString)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400")
                    .header("Host", "rarbg.is")
                    .header("Referer", "https://rarbg.is/torrents.php")
                    .cookies(connection.response().cookies())
                    .timeout(60000)
                    .followRedirects(true)
                    .method(Connection.Method.GET);
            connection.execute();
            System.out.println(connection.response().body());
            return this.getResponse(connection);
        }
        return connection;

    }

    private String getCaptchaValue(String captcha_id_value) {
        String captchaFileRemoteAsString = "https://rarbg.is/captcha2/" + captcha_id_value + ".png";
        System.out.println("the captcha file path: " + captchaFileRemoteAsString);
        try {
            URL url = new URL(captchaFileRemoteAsString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400");
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            connection.connect(); //访问图片
            BufferedImage imageBuffer = ImageIO.read(connection.getInputStream());
            File captchaFileAsLocal = new File("d:/captcha/" + captcha_id_value + ".png");
            ImageIO.write(imageBuffer, "png", captchaFileAsLocal);

            String code = StringUtils.trim(new OCR().recognizeText(captchaFileAsLocal, "png"));
            System.out.println("the captcha code: " + code);
            return code;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "12345";

    }

    private String getDescription(String descURLAsString, Map<String, String> cookies) throws IOException {

        Connection.Response resp = Http.url(descURLAsString)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.26 Safari/537.36 Core/1.63.5005.400 QQBrowser/10.0.923.400")
                .header("Host", "rarbg.is")
                .header("Referer", "https://rarbg.is/torrents.php")
                .cookies(cookies)
                .timeout(60000)
                .method(Connection.Method.GET)
                .response();
        System.out.println(resp.body());
        Document doc = Jsoup.parse(resp.body());
        Element e = doc.select("#description").first();
        return e.text() == null ? "" : e.html();
    }

}

