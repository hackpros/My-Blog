package com.my.translate.baidu;

import com.my.translate.Expression;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

/**
 * microsoft trans
 * 微软翻译也需要注册appid,网络太扯了,也走selenium这条路算了
 */
@Slf4j
public class BingTransExpression extends Expression.AbsExpression {
	private static String TRANS_API_HOST = "http://cn.bing.com/translator/";
	@Override
	public String interpret(String srcLanguage, String context, String tarLan) throws InterruptedException {

        WebDriver driver =super.createWebDriver();

		driver.get(TRANS_API_HOST);
		/**设置需要翻译的参数*/
		log.debug("translate context :" + context);
		WebElement srcElement = driver.findElement(By.id("t_sv"));
		srcElement.sendKeys(context);
		/**设置语言*/ //todo

		Select tzhCHS = new Select(driver.findElement(By.id("t_tl")));
		tzhCHS.selectByValue("zh-CHS");

		/**给js一点的执行时间*/
		Thread.sleep(2000L);

		/**获取翻译的果*/
		WebElement tarElement = driver.findElement(By.id("t_tv"));
		String result=tarElement.getAttribute("value");
		log.debug("after translate result:" + result);
		driver.close();
		return result;
	}

    public static void main(String[] args) throws InterruptedException {
        BingTransExpression  trans =new BingTransExpression();

        System.out.println(trans.interpret("", "No, because your interface does not have exactly one unimplemented method (that a lambda could provide the implementation for).", ""));


    }
}