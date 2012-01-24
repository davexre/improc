package com.test.scripting;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.SimpleBindings;

public class MyScriptTest {
	
	public static class MyData {
		int intData = 5;
		String strData = "Kuku";

		public int getIntData() {
			return intData;
		}

		public void setIntData(int intData) {
			this.intData = intData;
		}

		public String getStrData() {
			return strData;
		}

		public void setStrData(String strData) {
			this.strData = strData;
		}
	}
	
	public static void main(String[] args) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		List<ScriptEngineFactory> l = manager.getEngineFactories();
		for (ScriptEngineFactory f : l) {
			System.out.println(f.getEngineName());
		}
		
		ScriptEngine engine = manager.getEngineByName("js");
		Reader source = new InputStreamReader(MyScriptTest.class.getResourceAsStream("MyScriptTest.js"));

        SimpleBindings bindings = new SimpleBindings();
        bindings.put("a", "2");
        bindings.put("b", "3");
        bindings.put("data", new MyData());
        engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        
		if (engine instanceof Compilable) {
			CompiledScript bin = ((Compilable) engine).compile(source);
			long start = System.currentTimeMillis();
			bin.eval();
			Object res = engine.get("res");
			System.out.println("Script execution time: " + (System.currentTimeMillis() - start) + " ms");
			System.out.println("Result is:" + res);
			System.out.println("Result type is:" + res.getClass());
			
			System.out.println("---------");
			Bindings binds = bindings; //engine.getBindings(ScriptContext.ENGINE_SCOPE);
			for (Map.Entry<String, Object> i : binds.entrySet()) {
				System.out.println(i.getKey() + " (" + i.getValue().getClass() + ")=" + i.getValue());
			}
		}

	}
}
