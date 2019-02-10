package com.test.java;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class BeanUtilsExample {

	public void doIt(String[] args) throws Exception {
		Map root = new HashMap();

		Map a = new HashMap();
		int arr[] = new int[] { 3, 5, 7, 8 };
		a.put("b", arr);

		root.put("A", a);
		root.put("Z", arr);

		Velocity.init();
		VelocityContext velocityContext = new VelocityContext(root);
		StringWriter content = new StringWriter();
		Velocity.evaluate(velocityContext, content, "", "$A.b[1]");
		content.append('\n');
		Velocity.evaluate(velocityContext, content, "", "${A.b[1]}");
		System.out.println(content);

		System.out.println(BeanUtils.getProperty(root, "A.b.[1]"));
		System.out.println(BeanUtils.getProperty(root, "A.(b)[2]"));
		System.out.println(BeanUtils.getProperty(root, "(A)(b).[1]"));
		System.out.println(BeanUtils.getProperty(root, "(A)(b)[1]"));

		ExpressionParser parser = new SpelExpressionParser();
		System.out.println(parser.parseExpression("[A][b][1]").getValue(root));
		EvaluationContext context = new StandardEvaluationContext();
		context.setVariable("A", a);
		System.out.println(parser.parseExpression("#A[b][1]").getValue(context));
		System.out.println(parser.parseExpression("#A.get('b')[1]").getValue(context));
	}

	public static void main(String[] args) throws Exception {
		new BeanUtilsExample().doIt(args);
		System.out.println("Done.");
	}
}
