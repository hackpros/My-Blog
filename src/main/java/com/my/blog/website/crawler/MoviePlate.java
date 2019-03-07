package com.my.blog.website.crawler;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * <p>Description: 实现dao中的方法</p>
 * <p>Copyright: Copyright (c) 2017</p>
 * <p>Company: jumore</p>
 *
 * @author rongzheng
 * @date 2018/6/11
 */
public interface MoviePlate {
    ObjectMapper objectMapper = new ObjectMapper(){
          {
              this.setSerializationInclusion(JsonInclude.Include.NON_NULL);
              this.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
          }
    };


}
