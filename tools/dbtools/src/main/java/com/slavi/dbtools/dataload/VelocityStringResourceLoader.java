package com.slavi.dbtools.dataload;

import java.io.Reader;
import java.io.StringReader;

import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.velocity.util.ExtProperties;

public class VelocityStringResourceLoader extends ResourceLoader {
	@Override
	public void init(ExtProperties configuration) {
	}

	@Override
	public Reader getResourceReader(String source, String encoding) throws ResourceNotFoundException {
		return new StringReader(source == null ? "" : source);
	}

	@Override
	public boolean isSourceModified(Resource resource) {
		return false;
	}

	@Override
	public long getLastModified(Resource resource) {
		return 0;
	}
}
