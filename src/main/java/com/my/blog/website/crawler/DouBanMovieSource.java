package com.my.blog.website.crawler;

import com.my.blog.website.crawler.base.MoviePlate;
import com.my.blog.website.crawler.base.SeleniumMaster;
import com.my.blog.website.dao.ContentVoMapper;
import com.my.blog.website.dao.FilmMapper;
import com.my.blog.website.modal.Vo.ContentVo;
import com.my.blog.website.modal.Vo.Film;
import com.my.blog.website.modal.Vo.FilmQueryHelper;
import com.my.blog.website.modal.constants.FilmHelper;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

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
public class DouBanMovieSource extends SeleniumMaster.AbsSeleniumMaster implements MoviePlate {
    static String httpsURLAsString = "https://movie.douban.com/";
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

    @Scheduled(cron = "0 0/1 * * * ?")
    @Override
    public void start() {
        /**
         * 跟据已抓取的英文资源进行抓取
         */
        FilmQueryHelper e = new FilmQueryHelper();
        e.createCriteria().andStatusEqualTo(FilmHelper.EStatus.UN_TRANS.getStatus());
        List<Film> films = filmMapper.selectByExample(e);
        if (films.isEmpty()) {
            return;
        }
        WebDriver driver = null;
        try {
            driver = super.createWebDriver(Boolean.FALSE, Boolean.TRUE);
            WebDriver finalDriver = driver;
            films.forEach(el -> {
                finalDriver.get(httpsURLAsString);
                log.info(finalDriver.getCurrentUrl());
                /***搜索框*/
                WebDriverWait wait = new WebDriverWait(finalDriver, Duration.ofSeconds(10));
                By by = By.xpath("//*[@id=\"inp-query\"]");
                wait.until(ExpectedConditions.elementToBeClickable(by));
                finalDriver.findElement(by).sendKeys(el.getTitle());
                log.info("搜索影片：" + el.getTitle());
                /***执行搜索 */
                by = By.xpath("//*[@id=\"db-nav-movie\"]/div[1]/div/div[2]/form/fieldset/div[2]/input");
                wait.until(ExpectedConditions.elementToBeClickable(by));
                finalDriver.findElement(by).click();

                /**搜索结果页*/
                // = By.xpath("//*[@id=\"root\"]/div/div[2]/div[1]/div[1]/div/div/div/div[1]/a"
                by = By.xpath("//*[@id=\"root\"]/div/div[2]/div[1]/div[1]/div/div/div/div[1]/a");
                wait = new WebDriverWait(finalDriver, Duration.ofSeconds(15));
                wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(by));
                List<WebElement> webElements = finalDriver.findElement(by).findElements(by);
                Optional<WebElement> webElement = (webElements.stream().filter((s)->s.getText().contains(el.getTitle()))).findFirst();

                ContentVo contentVo = null;
                if (!webElement.isPresent()) {
                    el.setStatus(FilmHelper.EStatus.NO_TRANS.getStatus());
                } else {
                    String firstUrl = webElement.get().getAttribute("href");
                    finalDriver.get(firstUrl);
                    String title = finalDriver.findElement(By.xpath("//*[@id=\"content\"]/h1/span[1]")).getText();
                    String content = finalDriver.findElement(By.xpath("//*[@id=\"link-report\"]/span")).getText();
                    contentVo = new ContentVo();
                    contentVo.setCid(el.getCid().intValue());
                    contentVo.setTitle(title);
                    contentVo.setContent(content);
                    el.setStatus(FilmHelper.EStatus.HAS_TRANS.getStatus());
                }
                filmMapper.updateByPrimaryKey(el);
                if (contentVo != null) {
                    String beforeContent = contentVoMapper.selectByPrimaryKey(contentVo.getCid()).getContent();
                    contentVo.setContent(contentVo.getContent() + "\n" + beforeContent);
                    contentVoMapper.updateByPrimaryKeySelective(contentVo);
                }

            });
        } catch (Exception ex) {
            ex.printStackTrace();
            if (driver != null) {
                log.error("出错的url=" + driver.getCurrentUrl());
            }
        } finally {
            if (driver != null) {
                driver.close();
                driver.quit();
                /***批量保存，并且数据要添加的bloom中*/
            }
        }

    }

    @Override
    public void interpret(String context, int maxPage) {
    }

}

