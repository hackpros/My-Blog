package com.my.translate;

import com.my.translate.baidu.BaiduTransExpression;

import java.io.IOException;

/**
 * Interpreter Design Pattern
 */
public class InterpreterContext {

	public String translate(int i,String srcLanguage, String context,String tarLanguage) throws IOException, InterruptedException {

		Expression expression=new BaiduTransExpression();
		return expression.interpret(srcLanguage,  context, tarLanguage);

	}
	

}