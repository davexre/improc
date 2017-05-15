package com.slavi.example.springBoot.example2.itest;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.BeforeClass;
import org.springframework.context.expression.MapAccessor;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Test1 extends RestAssured {

	@BeforeClass
	public static void beforeClass() throws Exception {
		Properties prop = new Properties();
		prop.load(new InputStreamReader(Test1.class.getResourceAsStream("/test.properties")));

		RestAssured.port = 8080;
		RestAssured.basePath = "/";
		RestAssured.baseURI = "http://localhost";
	}

	void doIt() throws Exception {
		SessionFilter sessionFilter = new SessionFilter();
		RequestSpecification rs = given().filter(sessionFilter)
				.auth().form("admin", "admin", new FormAuthConfig("/login", "username", "password").withCsrfFieldName("_csrf"));
		Response r = rs.get("/users");
		r.then().statusCode(200);
		System.out.println(r.getBody().asString());

	}

	public static void main(String[] args) throws Exception {
		Properties prop = new Properties();
		prop.load(new InputStreamReader(Test1.class.getResourceAsStream("/test.properties")));
		Map map = new HashMap(prop);

		SpelExpressionParser parser = new SpelExpressionParser();
		TemplateParserContext templateContext = new TemplateParserContext();

		StandardEvaluationContext context = new StandardEvaluationContext();
		context.addPropertyAccessor(new MapAccessor());
		context.setRootObject(map);
		String spel = "#{ ['test.nested'] }";
//		String spel = "['my.setting.test']";
		Object o = parser.parseExpression(spel, templateContext).getValue(context);
		System.out.println(o);

		StrSubstitutor subst = new StrSubstitutor(map);
		System.out.println(subst.replace("asd ${test.substitute} qwe"));

		System.out.println("Done.");
	}
}
