package com.my.blog.website.crawler;

import com.github.pagehelper.PageInfo;
import com.my.blog.website.constant.ArticleCateEnum;
import com.my.blog.website.constant.ArticleStatusEnum;
import com.my.blog.website.dao.FilmMapper;
import com.my.blog.website.dto.Types;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.service.IContentService;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.runner.RunWith;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 测试数据库事务
 * Created by BlueT on 2017/3/8.
 */
@MapperScan("com.my.blog.website.dao")
@RunWith(SpringRunner.class)
@SpringBootTest
//@Transactional(rollbackFor = TipException.class)
public class T66ySourceTest {


    @Resource
    private T66ySource t66ySource;
    @Resource
    private IContentService contentService;

    @org.junit.Test
    public void test() throws IOException {
        String srcUrl="http://t66y.com/htm_data/7/1811/3327315.html";
        ContentVo vo = new ContentVo();
        vo.setTitle("消磨些许无聊");
        t66ySource.getContents(srcUrl, vo);
        vo.setAuthorId(1);
        vo.setType(Types.ARTICLE.getType());
        vo.setCategories(ArticleCateEnum.MESSY.name().toLowerCase());
        vo.setAllowComment(false);
        vo.setStatus(ArticleStatusEnum.draft.name());
        contentService.publish(vo);
    }

    @org.junit.Test
    public void downImage() throws IOException {
        String srcUrl="http://t66y.com/htm_data/7/1811/3327315.html";
        ContentVo vo = new ContentVo();
        vo.setTitle("消磨些许无聊");
        t66ySource.getContents(srcUrl, vo);


        Elements elements =  Jsoup.parse(vo.getContent()).select("img");
        for(Element e:elements){
            System.out.println(e.attr("data-src"));
            String[] filePath=e.attr("data-src").split("/");
            String fileFullName=filePath[filePath.length-1];
            System.out.println(e.attr("fileFullName:")+fileFullName);
            File pic = new File("c:/temp/" +fileFullName);
            if (pic.exists()){
                continue;
            }

            URL url = new URL(e.attr("data-src"));
            HttpURLConnection connection = (HttpURLConnection ) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(15000);

            String expFileName= fileFullName.substring(fileFullName.length()-3);
            System.out.println(e.attr("expFileName:"+expFileName));
            try{
                BufferedImage imageBuffer = ImageIO.read(connection.getInputStream());
                ImageIO.write(imageBuffer,expFileName, pic);
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }

        /**存储图片*/
        //BufferedImage imageBuffer = ImageIO.read(connection.getInputStream());
        //File captchaFileAsLocal = new File("c:/captcha/" + captcha_id_value + ".png");
        //ImageIO.write(imageBuffer, "png", captchaFileAsLocal);



    }

}