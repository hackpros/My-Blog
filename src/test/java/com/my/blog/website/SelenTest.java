package com.my.blog.website;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.File;

/**
 * <p>Description: 实现dao中的方法</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: jumore</p>
 *
 * @author fan
 * @date 2018/9/4
 */
public class SelenTest {
    public static void main(String[] args) throws InterruptedException {

        String rootPath =System.getProperty("user.dir");

        System.out.println(SelenTest.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        System.out.println(System.getProperty("user.dir"));
        /**
         * chromedriver 下载地址
         */
        //https://chromedriver.storage.googleapis.com/index.html?path=2.41/

        //让浏览器后台运行
        ChromeOptions option = new ChromeOptions();
        option.addArguments("headless");
        // 取消"Chrome正在受到自动软件的控制"提示
         option.addArguments("disable-infobars");
        // 导入Chrome驱动
        // WebDriver driver = new ChromeDriver(option);

        String exePath = "D://Java/chromedriver.exe";
        System.setProperty("webdriver.chrome.driver",rootPath+"\\src\\main\\resources\\lib\\chromedriver.exe");
        System.setProperty("webdriver.chrome.logfile", "d:\\Java\\chromedriver.log");
        WebDriver driver  = new ChromeDriver(option);

        driver.get("https://translate.google.cn/");
        WebElement srcEelement = driver.findElement(By.id("source"));
        srcEelement.sendKeys("this is green");

        Thread.sleep(4000L);
        WebElement tarElement = driver.findElement(By.id("gt-res-dir-ctr"));
        System.out.println("xxxxxxxxxxxxxxxx"+tarElement.getText());
        //driver.close();

    }
}
