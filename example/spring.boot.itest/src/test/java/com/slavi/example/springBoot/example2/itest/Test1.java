package com.slavi.example.springBoot.example2.itest;

import java.io.InputStreamReader;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.junit.BeforeClass;
import org.junit.Test;

import io.restassured.authentication.FormAuthConfig;
import io.restassured.filter.session.SessionFilter;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.RestAssured;


public class Test1 {

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

	SessionFilter authorizedSessionFilter;
	public RequestSpecification authorized() throws Exception {
		RequestSpecification rs = given();
		if (authorizedSessionFilter == null) {
			authorizedSessionFilter = new SessionFilter();
			rs.auth()
				.form(
					subst.replace("${login.admin.user}"),
					subst.replace("${login.admin.pass}"),
					new FormAuthConfig("/login", "username", "password") //.withCsrfFieldName("_csrf")
				);
		}
		return rs.filter(authorizedSessionFilter);
	}

	@Test
	public void doIt() throws Exception {
		Response r = authorized().get("/users");
		System.out.println(r.getBody().asString());
		r.then().statusCode(200).body(
			"page", equalTo(1),
			"items.size", lessThanOrEqualTo(10),
			"items.findAll{ item -> item.someInt >= 2 }.username", hasItem(equalTo("User 1"))
		);
		JsonPath jsonPath = r.jsonPath();
		Map user = jsonPath.get("items[1]");

		r = authorized().get("/users/{username}", user.get("username"));
		user = r.getBody().as(Map.class);
		System.out.println(user);
		r.then().statusCode(200);
		user.put("displayName", "Edit " + user.get("displayName"));
		((Map) user.get("department")).put("id", "3");

		r = authorized().body(user).contentType(ContentType.JSON).post("/users").thenReturn();
		System.out.println(r.getBody().asString());
		r.then().statusCode(200);

/*		user.put("displayName", "Edit " + user.get("displayName"));
		r = authorized.body(user).post("/users/User 0").thenReturn();
		System.out.println(r.asString());
		r.then().statusCode(200);*/
	}
}
