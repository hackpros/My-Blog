package com.my.blog.website.crawler.hyfw;

import com.my.blog.website.crawler.exception.CrawlerException;
import com.my.blog.website.crawler.ocr.OCR;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.http.HttpStatus;

import java.io.*;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * <p>Description: 中国铁路货运电子商务系统上网站数据查询</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: jumore</p>
 *
 * @author fan
 * @date 2018/8/14
 */
public class HyfwQuery {

    private static final Logger log = Logger.getLogger(HyfwQuery.class);
    /**
     * 首页
     */
    static String inexURLAsString = "http://hyfw.95306.cn/gateway/DzswNewD2D/Dzsw/page/business-chcx-hwzz";
    /**
     * 查询接口
     */
    static String orderAPIURLAsString = "http://hyfw.95306.cn/gateway/DzswNewD2D/Dzsw/action/ChcxAction_queryHwzzInfoByCarNo";
    static String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.84 Safari/537.36";


    public static void main(String[] args) throws IOException, InterruptedException {
        start();
    }


    public static void start() throws IOException, InterruptedException {
        log.info("start task....");
        Connection connection = openIndex();

        Document doc = Jsoup.parse(connection.response().body());
        HttpURLConnection httpURLConnection = procCaptcha(doc, connection.response());

        /***等待几秒针钟，让浏览器打开验证码*/
        Thread.sleep(3000L);

        log.info("ready from data");

        System.out.println("please you see captcha：");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        String QUERY_CAPTCA = br.readLine();
        log.info("you input captcha is： " + QUERY_CAPTCA);
        Map<String, String> formData = new HashMap<String, String>();
        formData.put("carNo", "4974947");
        formData.put("hph", "");
        formData.put("QUERY_CAPTCA", QUERY_CAPTCA);

        /**cookies*/

        List<String> list = httpURLConnection.getHeaderFields().get("Set-Cookie");
        String b = list.get(0).split(";")[0].split("=")[1];
        String a = list.get(1).split(";")[0].split("=")[1];

        connection = Jsoup.connect(orderAPIURLAsString)
                .userAgent(userAgent)
                .header("Host", "hyfw.95306.cn")
                .cookie("bl0gm1HBTB", a)
                .cookie("DZSW_SESSIONID", b)
                .timeout(60000)
                .data(formData)
                .method(Connection.Method.POST);
        connection.execute();
        if (connection.response().statusCode() != HttpStatus.OK.value()) {
            log.error("query api request error..");
            throw new CrawlerException(connection.response().statusMessage());
        }

        log.info(connection.response().body());

    }

    private static HttpURLConnection procCaptcha(Document doc, Connection.Response res) throws MalformedURLException {
        try {
            String captchaFileRemoteAsString = "http://hyfw.95306.cn" + doc.select("#captchaImage").attr("src");
            log.info("the captcha file path: " + captchaFileRemoteAsString);

            //获取下载地址
            URL url = new URL(captchaFileRemoteAsString);
            //链接网络地址
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", userAgent);
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);
            /**带上cookie 下面难验证码图片*/
            for (final Map.Entry<String, String> entry : res.cookies().entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
            //获取链接的输出流
            InputStream is = connection.getInputStream();
            //创建文件，fileName为编码之前的文件名
            File localCaptchaJpg = new File("d:/captcha/" + UUID.randomUUID() + ".jpg");
            //根据输入流写入文件
            FileOutputStream out = new FileOutputStream(localCaptchaJpg);
            int i = 0;
            while ((i = is.read()) != -1) {
                out.write(i);
            }
            out.close();
            is.close();

            /**将验证码浏览器中打开自己识别**/
            OpenExplorerTest4.browse(localCaptchaJpg.getPath());
            /**使用OCR识别验证码*/
            String code = StringUtils.trim(new OCR().recognizeText(localCaptchaJpg, "jpg"));
            log.info("OCR Identification captcha is:" + code);
            return connection;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    private static Connection openIndex() throws IOException {

        //bl0gm1HBTB=MDAwM2IyYWM3NDQwMDAwMDAwMDMwVm9eKAMxNTM0MjY3NTIx; DZSW_SESSIONID=6u43wSl18ePqKrr4dwxolbhhUnEcwPLhhttylSoTZb8RB5pKNq6p%21-1620711975


        log.info("1.0 打开目录网站首页");
        Connection connection = Jsoup.connect(inexURLAsString)
                .userAgent(userAgent)
                .header("Host", "hyfw.95306.cn")
                .timeout(60000)
                .method(Connection.Method.GET);

        connection.execute();
        if (connection.response().statusCode() != HttpStatus.OK.value()) {
            log.error(connection.response().statusMessage());
        }
        return connection;
    }


    public static class OpenExplorerTest4 {


        private static void browse(String url) throws Exception {
            // 获取操作系统的名字
            String osName = System.getProperty("os.name", "");
            if (osName.startsWith("Mac OS")) {
                // 苹果的打开方式
                Class fileMgr = Class.forName("com.apple.eio.FileManager");
                Method openURL = fileMgr.getDeclaredMethod("openURL",
                        new Class[]{String.class});
                openURL.invoke(null, new Object[]{url});
            } else if (osName.startsWith("Windows")) {
                // windows的打开方式。
                Runtime.getRuntime().exec(
                        "rundll32 url.dll,FileProtocolHandler " + url);
            } else {
                // Unix or Linux的打开方式
                String[] browsers = {"firefox", "opera", "konqueror", "epiphany",
                        "mozilla", "netscape"};
                String browser = null;
                for (int count = 0; count < browsers.length && browser == null; count++)
                    // 执行代码，在brower有值后跳出，
                    // 这里是如果进程创建成功了，==0是表示正常结束。
                    if (Runtime.getRuntime()
                            .exec(new String[]{"which", browsers[count]})
                            .waitFor() == 0)
                        browser = browsers[count];
                if (browser == null)
                    throw new Exception("Could not find web browser");
                else
                    // 这个值在上面已经成功的得到了一个进程。
                    Runtime.getRuntime().exec(new String[]{browser, url});
            }
        }

    }


}
