package br.com.caelum.brutauth.interceptors;

import java.lang.reflect.Method;

import javax.inject.Inject;

import br.com.caelum.brutauth.auth.annotations.AccessLevel;
import br.com.caelum.brutauth.auth.annotations.SimpleBrutauthRules;
import br.com.caelum.brutauth.auth.handlers.HandlerSearcher;
import br.com.caelum.brutauth.auth.handlers.RuleHandler;
import br.com.caelum.brutauth.auth.rules.SimpleBrutauthRule;
import br.com.caelum.vraptor4.InterceptionException;
import br.com.caelum.vraptor4.Intercepts;
import br.com.caelum.vraptor4.core.InterceptorStack;
import br.com.caelum.vraptor4.interceptor.Interceptor;
import br.com.caelum.vraptor4.ioc.Container;
import br.com.caelum.vraptor4.restfulie.controller.ControllerMethod;

@Intercepts
public class SimpleBrutauthRuleInterceptor implements Interceptor {

	@Inject private Container container;
	@Inject private HandlerSearcher handlers;
	@Inject private ControllerMethod method;

	@Override
	public void intercept(InterceptorStack stack, ControllerMethod method,
			Object controllerInstance) throws InterceptionException {
		Method controllerMethod = method.getMethod();
		SimpleBrutauthRules permissionAnnotation = controllerMethod.getAnnotation(SimpleBrutauthRules.class);
		Class<? extends SimpleBrutauthRule>[] permissions = permissionAnnotation.value();
		long permissionData = 0l;
		if (method.containsAnnotation(AccessLevel.class)) {
			permissionData = controllerMethod.getAnnotation(AccessLevel.class).value();
		}
		for (Class<? extends SimpleBrutauthRule> permission : permissions) {
			SimpleBrutauthRule rule = container.instanceFor(permission);
			RuleHandler handler = handlers.getHandler(rule);
			if(!rule.isAllowed(permissionData)){
				handler.handle();
				return;
			}
		}
		stack.next(method, controllerInstance);
		
	}

	@Override
	public boolean accepts(ControllerMethod method) {
		return method.containsAnnotation(SimpleBrutauthRules.class);
	}
	
	

}