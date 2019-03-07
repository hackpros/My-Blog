package com.my.translate.baidu;

import com.my.blog.website.crawler.MoviePlate;
import com.my.translate.Expression;
import com.my.translate.TranslateException;
import lombok.Data;
import lombok.extern.log4j.Log4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * you dao　翻译
 */
@Log4j
public class YouDaoTransExpression implements Expression {
	//protected static final Logger log = Logger.getLogger(YouDaoTransExpression.class);
	static  String appKey ="50087e868a254161";
	static  String appKeys ="KCeM2JHe3WxhGRgRN6SQCOtNMy0jQYAr";
	static  String appUrl=  "http://openapi.youdao.com/api";

	public static void main(String[] args) throws Exception {
		String query = "你好";
		String salt = String.valueOf(System.currentTimeMillis());
		String from = "zh-CHS";
		String to = "EN";
		String sign = DigestUtils.md5Hex(appKey + query + salt + appKey).toUpperCase();

		System.out.println(sign);
		Map params = new HashMap();
		params.put("q", query);
		params.put("from", from);
		params.put("to", to);
		params.put("sign", sign);
		params.put("salt", salt);
		params.put("appKey", appKey);
		System.out.println(requestForHttp(appUrl, params));
	}


	public static String requestForHttp(String appUrl,Map requestParams) throws Exception{
	 String result = null;
		Connection connection = Jsoup.connect(appUrl )
				.data(requestParams)
				.ignoreContentType(true).maxBodySize(0).timeout(25000).followRedirects(true);
		connection.execute();
		log.info(connection.response().body());
		if (connection.response().statusCode() == HttpStatus.OK.value()) {
			result=connection.response().body();
		}
		return result;

	}


	@Override
	public String interpret(String srcLanguage, String context, String tarLanguage) throws IOException {
		String salt = String.valueOf(System.currentTimeMillis());
		String from = "EN";
		String to = "zh-CHS";
		String sign = DigestUtils.md5Hex(appKey + context + salt + appKeys).toUpperCase();

		System.out.println(sign);
		Map params = new HashMap();
		params.put("q", context);
		params.put("from", from);
		params.put("to", to);
		params.put("sign", sign);
		params.put("salt", salt);
		params.put("appKey", appKey);

		Connection connection = Jsoup.connect(appUrl )
				.data(params)
				.ignoreContentType(true).maxBodySize(0).timeout(25000).followRedirects(true);
		connection.execute();
		log.info(connection.response().body());
		YouDaoResult result=null;
		if (connection.response().statusCode() == HttpStatus.OK.value()) {
			result = MoviePlate.objectMapper.readValue(connection.response().body(), YouDaoResult.class);
		}
		if (!result.getErrorCode().equalsIgnoreCase("0")){
			throw new TranslateException(result.getErrorCode());
		}
		return result.getTranslation();
	}
}
	@Data
	class YouDaoResult {
		private String errorCode;
		private String translation;
		private String query;
	}
