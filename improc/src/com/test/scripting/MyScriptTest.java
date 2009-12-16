package com.test.scripting;

import java.io.InputStreamReader;
import java.io.Reader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public class MyScriptTest {
	public static void main(String[] args) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("js");
		Reader source = new InputStreamReader(MyScriptTest.class.getResourceAsStream("MyScriptTest.js"));

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("a", "2");
        bindings.put("b", "3");
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        
		if (engine instanceof Compilable) {
			CompiledScript bin = ((Compilable) engine).compile(source);
			long start = System.currentTimeMillis();
			bin.eval();
			Object res = engine.get("res");
			System.out.println("Script execution time: " + (System.currentTimeMillis() - start) + " ms");
			System.out.println("Result is:" + res);
			System.out.println("Result type is:" + res.getClass());
		}

	}
}
