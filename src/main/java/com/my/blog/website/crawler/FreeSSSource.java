package com.my.blog.website.crawler;

import com.my.blog.website.crawler.base.MoviePlate;
import com.my.blog.website.crawler.base.SeleniumMaster;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p>Description: 小草资原</p>
 * <p>Copyright: Copyright (c) 2018</p>
 * <p>Company: jumore</p>
 *
 * @author fan(renntrabbit @ foxmail.com) created by  2018/3/20 16:26
 */
@Component
public class FreeSSSource extends SeleniumMaster.AbsSeleniumMaster implements MoviePlate {
    protected final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());

    private static String TRANS_API_HOST = "https://free-ss.site/";

    @Override
    public void start() {
        WebDriver driver = null;
        try {
            driver = super.createWebDriver();
            driver.get(TRANS_API_HOST);
            /**给js一点的执行时间*/
            Thread.sleep(4000);
            List<WebElement> elementList = driver.findElements(By.xpath("//*[@id=\"tbss\"]/tbody/tr"));
            elementList.stream().forEach(e -> {
                log.info("V/T/U/M:" + e.findElements(By.tagName("td")).get(0).getText());
                log.info("Address:" + e.findElements(By.tagName("td")).get(1).getText());
                log.info("Port:" + e.findElements(By.tagName("td")).get(2).getText());
                log.info("Password:{}" + e.findElements(By.tagName("td")).get(3).getText());
                log.info("Method:" + e.findElements(By.tagName("td")).get(4).getText());
                log.info("publish date：" + e.findElements(By.tagName("td")).get(5).getText());
                log.info("area:" + e.findElements(By.tagName("td")).get(6).getText());
                log.info("QR code" + e.findElements(By.tagName("td")).get(7).getText());
            });
            Thread.sleep(2000);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (driver != null) {
                driver.close();
                driver.quit();
            }
        }

    }


    @Override
    public void interpret(String context, int maxPage) {

    }

    public static void main(String[] args) {
        FreeSSSource trans = new FreeSSSource();
        trans.start();
    }

}

