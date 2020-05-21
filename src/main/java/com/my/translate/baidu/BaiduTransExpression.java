package com.my.translate.baidu;

import com.my.blog.website.crawler.base.MoviePlate;
import com.my.translate.Expression;
import com.my.translate.TranslateException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *调用百度的翻译api,基本和有道的是一模一样的,不知谁抄的谁，需要注册账号,开通应用，应该每月有字数限制
 */
@Slf4j
public class BaiduTransExpression implements Expression {

	private static final String TRANS_API_HOST = "http://api.fanyi.baidu.com/api/trans/vip/translate";
	private static String APPID ="20180831000201016";
	private static String SECURITYKEY="AViQZ0V256QPUKq5nXI9";

	@Override
	public String interpret(String srcLanguage, String context, String tarLanguage) throws IOException {
		String query = "Height of 600 meters";
		Map<String, String> params = new HashMap<String, String>();
		params.put("q", query);
		params.put("from", "en");
		params.put("to", "zh");
		params.put("appid", APPID);
		// 随机数
		String salt = String.valueOf(System.currentTimeMillis());
		params.put("salt", salt);
		// 签名
		String sign = DigestUtils.md5Hex(APPID + query + salt + SECURITYKEY);
		String sign1 = MD5.md5(APPID + query + salt + SECURITYKEY);

		log.info("apache md5:"+sign );
		log.info("baidu md5:"+sign1 );
		params.put("sign", sign);
		Connection connection = Jsoup.connect(TRANS_API_HOST)
				.data(params)
				.ignoreContentType(true).maxBodySize(0).timeout(25000).followRedirects(true);
		connection.execute();
		BaiduResult result=null;
		log.info(connection.response().body());
		if (connection.response().statusCode() == HttpStatus.OK.value()) {
				result = MoviePlate.objectMapper.readValue(connection.response().body(), BaiduResult.class);
		}
		if (result.getError_code()!=0){
			throw new TranslateException(result.toString());
		}
		return result.getTrans_result().get(0).getDst();
	}


	public static void main(String[] args) throws IOException {
		System.out.println(new BaiduTransExpression().interpret("","",""));
	}
}
	@Data
	class BaiduResult{
		private Integer error_code=0;
		private String error_msg;
		private String from;
		private String to;
		private String src;
		private String dst;
		private List<BaiduResult> trans_result;
	}
