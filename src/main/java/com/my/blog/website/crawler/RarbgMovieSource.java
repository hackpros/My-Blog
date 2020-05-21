package com.my.blog.website.crawler;

import com.my.blog.website.component.BloomFilterService;
import com.my.blog.website.constant.ArticleCateEnum;
import com.my.blog.website.crawler.base.MoviePlate;
import com.my.blog.website.crawler.base.SeleniumMaster;
import com.my.blog.website.crawler.ocr.OCR;
import com.my.blog.website.dto.Types;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.modal.Vo.Film;
import com.my.blog.website.modal.constants.FilmHelper;
import com.my.blog.website.service.IContentService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
public class RarbgMovieSource extends SeleniumMaster.AbsSeleniumMaster implements MoviePlate {

    /**
     * 前100名
     */
    static String TOP_100 = "https://rarbg.is/top100.php?category[]=14&category[]=15&category[]=16&category[]=17&category[]=21&category[]=22&category[]=42&category[]=44&category[]=45&category[]=46&category[]=47&category[]=48";
    protected final Logger log = Logger.getLogger(this.getClass());
    @Resource
    private IContentService contentService;
    @Resource
    BloomFilterService bloomFilterService;

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

    public static void main(String[] args) throws IOException, InterruptedException {
        new RarbgMovieSource().start();
    }

    @Override
    public void start() throws IOException, InterruptedException {
        WebDriver driver = null;
        log.info("start task....");
        List<ContentVo> contentVos = new ArrayList<ContentVo>();
        try {
            driver = super.createWebDriver();
            driver.get(TOP_100);
            Thread.sleep(3000);
            log.info(driver.getCurrentUrl());
            /***单击-跳转到验证码页*/
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("/html/body/div/div/a")));
            driver.findElement(By.xpath("/html/body/div/div/a")).click();
            /***验证码*/
            By by = By.xpath("/html/body/form/div/div/table[1]/tbody/tr[2]/td[2]/img");
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
            String captchaValue = getCaptchaValue(driver, by);
            /***填写验证码*/
            wait = new WebDriverWait(driver, Duration.ofSeconds(15));
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//*[@id=\"solve_string\"]")));
            driver.findElement(By.xpath("//*[@id=\"solve_string\"]")).sendKeys(captchaValue);
            /***I am human */
            by = By.xpath("//*[@id=\"button_submit\"]");
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(by));
            driver.findElement(by).click();


            /***top 100 popual */
            driver.navigate().to(TOP_100);
            /***table list*/
            by = By.xpath("/html/body/table[3]/tbody/tr/td[2]/div/table/tbody/tr[2]/td/table/tbody/tr/td[2]/a[1]");
            wait = new WebDriverWait(driver, Duration.ofSeconds(5));
            wait.until(ExpectedConditions.elementToBeClickable(by));
            List<WebElement> webElements = driver.findElements(by);


            webElements.forEach(e -> {
                //布隆过滤器检查该网而有没有处理过
                if (!bloomFilterService.mightContain(e.getAttribute("href"))) {
                    String title = e.getText();
                    String thumbnail = e.getAttribute("onmouseover");
                    if (thumbnail.startsWith("https")) {
                        thumbnail = match(thumbnail, "img", "src").get(0);
                    } else {
                        thumbnail = "https:" + match(thumbnail, "img", "src").get(0);
                    }
                    ContentVo vo = new ContentVo();
                    vo.setTitle(title);
                    vo.setThumbnail(thumbnail);
                    vo.setContent(e.getAttribute("href"));
                    vo.setAuthorId(1);
                    vo.setType(Types.ARTICLE.getType());
                    vo.setCategories(ArticleCateEnum.FILMS.name().toLowerCase());
                    vo.setAllowComment(false);
                    vo.setStatus("publish");
                    contentVos.add(vo);
                }

            });
            /***detail*/
            WebDriver finalDriver = driver;
            contentVos.forEach(e -> {
                try {
                    getDescription(finalDriver, e);
                } catch (Exception x) {
                    x.printStackTrace();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.close();
                driver.quit();
                /***批量保存，并且数据要添加的bloom中*/
            }
            if (contentService == null) {
                contentService.batchAppend(contentVos);
            }
        }
    }

    private ContentVo getDescription(WebDriver driver, ContentVo vo) {
        log.info("5.0 get everyone resource detail page info");
        Film film = new Film();
        film.setSrc(vo.getContent());
        film.setStatus(FilmHelper.EStatus.UN_TRANS.getStatus());

        driver.navigate().to(vo.getContent());
        By by = By.xpath("//*[@id=\"description\"]");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(ExpectedConditions.elementToBeClickable(by));

        String desc = driver.findElement(by).getText();
        String description = desc != null ? desc : driver.findElement(by).getAttribute("innerHTML");
        vo.setContent(description);
        vo.setFilm(film);
        List<WebElement> elements = driver.findElements(By.xpath("/html/body/table[3]/tbody/tr/td[2]/div/table/tbody/tr[2]/td/div/table/tbody/tr"));
        elements.stream().forEach(e -> {
            if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Torrent")) {
                film.setDownPath(e.findElement(By.cssSelector("td.lista> a:nth-child(3)")).getAttribute("href"));
            } else if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Poster")) {
                film.setPoster(e.findElement(By.cssSelector("td.lista > img")).getAttribute("src"));
            } else if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Others")) {
                film.setOthers(e.findElement(By.cssSelector("td:nth-child(2) > table > tbody > tr:nth-child(2) > td:nth-child(5) > a")).getText());
            } else if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Size")) {
                film.setSize(e.findElement(By.cssSelector("td.lista")).getText());
            } else if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Title")) {
                film.setTitle(e.findElement(By.cssSelector("td.lista")).getText());
            } else if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Year")) {
                film.setYear(e.findElement(By.cssSelector("td.lista")).getText());
            } else if (e.findElement(By.cssSelector("td.header2")).getText().startsWith("Plot")) {
                film.setPlot(e.findElement(By.cssSelector("td.lista")).getText());
            }
        });
        return vo;
    }


    /**
     * 获取难证码,对元素截图
     *
     * @param driver
     * @param by
     * @return
     */
    private String getCaptchaValue(WebDriver driver, By by) {
        try {
            File captchaFileAsLocal = captureElementScreenshot(driver, by);
            log.info("the captcha file path: " + captchaFileAsLocal.getPath());

            String code = StringUtils.trim(new OCR().recognizeText(captchaFileAsLocal, "png"));
            log.info("this captcha code: " + code);
            return code;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "66666";

    }

    /**
     * 元素截图
     *
     * @param driver,
     * @param by
     * @return
     * @throws IOException
     */
    public File captureElementScreenshot(WebDriver driver, By by) throws IOException {
        WebElement element = driver.findElement(by);
        //Capture entire page screenshot as buffer.
        //Used TakesScreenshot, OutputType Interface of selenium and File class of java to capture screenshot of entire page.
        File screen = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        //Used selenium getSize() method to get height and width of element.
        //Retrieve width of element.
        int ImageWidth = element.getSize().getWidth();
        //Retrieve height of element.
        int ImageHeight = element.getSize().getHeight();

        //Used selenium Point class to get x y coordinates of Image element.
        //get location(x y coordinates) of the element.
        Point point = element.getLocation();
        int xcord = point.getX();
        int ycord = point.getY();

        //Reading full image screenshot.
        BufferedImage img = ImageIO.read(screen);
        //cut Image using height, width and x y coordinates parameters.
        BufferedImage dest = img.getSubimage(xcord, ycord, ImageWidth, ImageHeight);
        ImageIO.write(dest, "png", screen);

        //Used FileUtils class of apache.commons.io.
        //save Image screenshot In D: drive.
        File captchaFileAsLocal = new File("d:/captcha/" + RandomStringUtils.randomAlphanumeric(10) + ".png");
        FileUtils.copyFile(screen, captchaFileAsLocal);

        return captchaFileAsLocal;

    }

    @Override
    public void interpret(String context, int maxPage) {

    }
}

