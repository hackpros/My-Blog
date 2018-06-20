package com.my.blog.base.service;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.client.RestTemplate;



public class RestBaseService {
	protected final Logger log = Logger.getLogger(this.getClass());


	public static final String RETURN_INTERFACE_KEY = "ICResponse";


	@Qualifier("restTemplate")
	@Autowired
	protected RestTemplate restTemplate;



	
}
