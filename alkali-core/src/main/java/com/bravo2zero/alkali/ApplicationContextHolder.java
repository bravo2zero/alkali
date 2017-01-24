package com.bravo2zero.alkali;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author brawo2zero
 */
public class ApplicationContextHolder implements ApplicationContextAware {

	private static ApplicationContext context;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		context = applicationContext;
	}

	static public <T> T getBeanByName(String beanName, Class<T> beanType) {
		return ((T) context.getBean(beanName));
	}
}
