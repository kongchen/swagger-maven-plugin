package com.github.kongchen.swagger.docgen.filter;

import com.wordnik.swagger.core.filter.SwaggerSpecFilter;
import com.wordnik.swagger.model.ApiDescription;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;

import java.util.List;
import java.util.Map;

/**
 * Sample test swagger filter that filters out everything.
 *
 * @author marek
 */
public class TestSwaggerSpecFilter implements SwaggerSpecFilter
{
	@Override
	public boolean isOperationAllowed(Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers)
	{
		return true;
	}

	@Override
	public boolean isParamAllowed(Parameter parameter, Operation operation, ApiDescription api, Map<String, List<String>> params, Map<String, String> cookies, Map<String, List<String>> headers)
	{
		return false;
	}
}
