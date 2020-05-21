package com.my.blog.website.init;

import com.my.blog.website.component.BloomFilterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @ClassName: BloomFilterInit
 * @Description: TODO
 * @version: V1.0
 * @date: 4/22/2020 2:13 PM
 * @author: fan
 */

@Component
public class Init implements CommandLineRunner {
    private Logger log = LoggerFactory.getLogger(Init.class);
    @Resource
    BloomFilterService bloomFilterService;

    public void run(String... args) throws Exception {

        /*log.info("----------------------------------------------------------");
        log.info("\tstart shadowsocks server.. ");
        SocksServer.getInstance().start();
        log.info("\tshadowsocks strt success ..");
        log.info("----------------------------------------------------------");*/

        log.info("----------------------------------------------------------");
        log.info("\t初始化bloomFilter.. ");
        bloomFilterService.init();
        log.info("\tbloomFilter init finish..");
        log.info("----------------------------------------------------------");


    }


}
