package com.my.blog.website.crawler.base;

import org.apache.commons.lang3.SystemUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.concurrent.TimeUnit;


/**
 *
 */
public interface SeleniumMaster {
    String CHROME_DRIVER_ROOT_PATH = System.getProperty("user.dir");

    /**
     * @param context
     * @param maxPage
     * @return
     */
    void interpret(String context, int maxPage);


    abstract class AbsSeleniumMaster implements SeleniumMaster {
        /**
         * def 使用代码
         *
         * @return
         */
        protected WebDriver createWebDriver() {
            return createWebDriver(Boolean.TRUE, Boolean.TRUE);
        }

        /**
         * @param isProxy true 使用代理，false 直连
         * @param isHide  是否静默
         * @return
         */
        protected WebDriver createWebDriver(Boolean isProxy, Boolean isHide) {
            String chromeDriverPath = "";
            /**设置参数及日志文件路径*/
            if (SystemUtils.IS_OS_LINUX) {
                chromeDriverPath = CHROME_DRIVER_ROOT_PATH + System.getProperty("file.separator") + "lib" + System.getProperty("file.separator") + "chromedriver";
            } else if (SystemUtils.IS_OS_WINDOWS) {
                chromeDriverPath = CHROME_DRIVER_ROOT_PATH + System.getProperty("file.separator") + "lib" + System.getProperty("file.separator") + "chromedriver.exe";
            }
            DesiredCapabilities capabilities = new DesiredCapabilities("chrome", "", Platform.ANY);
            ChromeDriverService service = new ChromeDriverService.Builder()
                    .usingDriverExecutable(new File(chromeDriverPath))
                    .build();

            ChromeOptions options = new ChromeOptions();
            /***静默方式*/
            if (isHide) {
                options.addArguments("--headless");
            }
            options.addArguments("--no-sandbox"); // Bypass OS security model, MUST BE THE VERY FIRST OPTION
            options.setExperimentalOption("useAutomationExtension", false);
            options.addArguments("start-maximized"); // open Browser in maximized mode
            options.addArguments("disable-infobars"); // disabling infobars
            options.addArguments("--disable-extensions"); // disabling extensions
            options.addArguments("--disable-gpu"); // applicable to windows os only
            options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
            options.merge(capabilities);
            options.addArguments("disable-infobars");

            /***使用代理*/
            if (isProxy) {
                options.addArguments("--proxy-server=socks5://127.0.0.1:4080");
            }
            /***反爬穿透*/
            ChromeDriver driver = new ChromeDriver(service, options);
            java.util.Map<String, Object> params = new java.util.HashMap<String, Object>();
            params.put("source", "Object.defineProperty(navigator, 'webdriver', { get: () => undefined })");
            driver.executeCdpCommand("Page.addScriptToEvaluateOnNewDocument", params);
            return driver;
        }


    }

}
