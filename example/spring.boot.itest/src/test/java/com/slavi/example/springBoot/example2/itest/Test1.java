package com.slavi.example.springBoot.example2.itest;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.RestAssured;
import io.restassured.authentication.FormAuthConfig;
import io.restassured.filter.session.SessionFilter;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class Test1 extends RestAssured {

	protected static StrSubstitutor subst;
	
	@BeforeClass
	public static void beforeClass() throws Exception {
		Properties prop = new Properties();
		prop.load(new InputStreamReader(Test1.class.getResourceAsStream("/test.properties")));
		prop.putAll(prop);
		prop.putAll(System.getenv());
		prop.putAll(System.getProperties());
		subst = new StrSubstitutor((Map) prop);
		subst.setEnableSubstitutionInVariables(true);
		
		RestAssured.port = Integer.parseInt(subst.replace("${server.port}"));
		RestAssured.basePath = subst.replace("${server.basePath}");
		RestAssured.baseURI = subst.replace("${server.baseURI}");
	}

	RequestSpecification authorized;
	@Before
	public void prepare() throws Exception {
		SessionFilter sessionFilter = new SessionFilter();
		authorized = given().filter(sessionFilter).auth()
			.form(
				subst.replace("{login.admin.user}"), 
				subst.replace("{login.admin.pass}"), 
				new FormAuthConfig("/login", "username", "password").withCsrfFieldName("_csrf")
			);
	}

	@Test
	public void doIt() throws Exception {
		Response r = authorized.get("/users");
		r.then().statusCode(200);
		System.out.println(r.getBody().asString());

	}

	public static void main(String[] args) throws Exception {
		beforeClass();
/*		Properties prop = new Properties();
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

		StrSubstitutor subst = new StrSubstitutor(prop);
		System.out.println(subst.replace("asd ${test.substitute} qwe"));
*/
		new Test1().doIt();
		System.out.println("Done.");
	}
}
