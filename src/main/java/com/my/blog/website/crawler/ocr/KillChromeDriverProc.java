package com.my.blog.website.crawler.ocr;

import com.my.blog.website.crawler.FreeSSSource;

/**
 * @ClassName: KillChromeDriverProc
 * @Description: TODO
 * @version: V1.0
 * @date: 4/24/2020 10:04 AM
 * @author: fan
 */

public class KillChromeDriverProc {


    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();
        try {
            System.out.println("kill chromedriver.exe");
            runtime.exec("taskkill /f /im chromedriver.exe");
        } catch (Exception e) {
            System.out.println("Error!");
        }


    }
}
