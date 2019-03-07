package com.my.translate;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.io.IOException;

/**
 *
 */
public interface Expression {
	String CHROME_DRIVER_ROOT_PATH = System.getProperty("user.dir");
	/**
	 *
	 * @param srcLanguage
	 * @param context
	 * @param tarLanguage
	 * @return
	 */
	String interpret(String srcLanguage, String context, String tarLanguage) throws IOException, InterruptedException;

    @Slf4j
	abstract class AbsExpression implements  Expression {
        protected WebDriver  createWebDriver(){
            /**让浏览器后台运行*/
            ChromeOptions option = new ChromeOptions();
           // option.addArguments("headless");
            /**取消"Chrome正在受到自动软件的控制"提示*/
           // option.addArguments("disable-infobars");

            /**设置参数及日志文件路径*/
            String chromeDriverPath = CHROME_DRIVER_ROOT_PATH + "\\src\\main\\resources\\lib\\chromedriver.exe";
            System.setProperty("webdriver.chrome.driver", chromeDriverPath);
            String chromeDriverLogPath = CHROME_DRIVER_ROOT_PATH + "\\src\\main\\resources\\lib\\chromedriver.log";
            System.setProperty("webdriver.chrome.logfile", chromeDriverLogPath);

            log.debug("chrome.driver path:" + chromeDriverPath);
            log.debug("chrome.driver log path:" + chromeDriverLogPath);
            WebDriver driver = new ChromeDriver(option);
            return driver;
        }
    }
}
