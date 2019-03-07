package com.my.translate.baidu;

import com.my.translate.Expression;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

/**
 * google翻译
 * 1.第一种方法也是注册google账号，开通翻译的api应用，对于普通人来说比较昂贵
 * 2.使用jsoup爬取网而翻译的api tkk参数是个问题，需要使用js来计算，虽然网上已有大神破解，但是这种方法封ip的机率真很大
 * 3.使用selenium的方式，什么后果不知，以前听说过，本次就当学习
 */
@Slf4j
public class GoogleTransExpression extends Expression.AbsExpression {

    private static String TRANS_API_HOST = "https://translate.google.cn";

    static {
        //TRANS_API_HOST="https://translate.google.cn/translate_a/single?client=t&sl=en&tl=zh-CN&hl=zh-CN&dt=at&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=t&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&kc=2&tk=87518.513860&q=this%20is%20a%20green";
    }

    public static void main(String[] args) throws InterruptedException {
        GoogleTransExpression a = new GoogleTransExpression();
        System.out.println(a.interpret("", "No, because your interface does not have exactly one unimplemented method (that a lambda could provide the implementation for).", ""));
    }

    @Override
    public String interpret(String srcLanguage, String context, String tarLanguage) throws  InterruptedException {
        WebDriver driver =super.createWebDriver();

        driver.get(TRANS_API_HOST);
        /**设置需要翻译的参数*/
        log.debug("translate context :" + context);
        WebElement srcElement = driver.findElement(By.id("source"));
        srcElement.sendKeys(context);
        /**设置语言*/ //todo
        /**给js一点的执行时间*/
        Thread.sleep(2000L);

        /**获取翻译的果*/
        WebElement tarElement = driver.findElement(By.id("gt-res-dir-ctr"));
        log.debug("after translate result:" + tarElement.getText());
        String result= tarElement.getText();
        driver.close();
        return result;


    }
}