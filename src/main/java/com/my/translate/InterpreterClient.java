package com.my.translate;

import com.my.translate.baidu.BaiduTransExpression;
import com.my.translate.baidu.GoogleTransExpression;

import java.io.IOException;

public class InterpreterClient {

	public InterpreterContext ic;
	
	public InterpreterClient(InterpreterContext i){
		this.ic=i;
	}
	
	public String interpret(String str) throws IOException, InterruptedException {
		Expression exp = null;
		//create rules for expressions
		if(str.contains("Hexadecimal")){
			exp=new BaiduTransExpression();
		}else if(str.contains("Binary")){
			exp=new GoogleTransExpression();
		}else return str;
		
		return exp.interpret("","","");
	}
	
	public static void main(String args[]){
		String str1 = "28 in Binary";
		String str2 = "28 in Hexadecimal";
		
		InterpreterClient ec = new InterpreterClient(new InterpreterContext());
		//System.out.println(str1+"= "+ec.interpret(str1));
		//System.out.println(str2+"= "+ec.interpret(str2));

	}
}