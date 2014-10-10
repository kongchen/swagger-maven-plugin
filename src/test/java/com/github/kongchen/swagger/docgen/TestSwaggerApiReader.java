package com.github.kongchen.swagger.docgen;

import java.lang.reflect.Method;

import scala.collection.immutable.List;
import scala.collection.mutable.ListBuffer;

import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.model.Operation;
import com.wordnik.swagger.model.Parameter;
import com.wordnik.swagger.model.ResponseMessage;

public class TestSwaggerApiReader extends DefaultJaxrsApiReader {

	@Override
	public Operation parseOperation(Method method, ApiOperation apiOperation,
			List<ResponseMessage> apiResponses, String isDeprecated,
			List<Parameter> parentParams, ListBuffer<Method> parentMethods) {
		Operation operation = super.parseOperation(method, apiOperation, apiResponses, isDeprecated,
						parentParams, parentMethods);
		return new Operation(operation.method(),
				// for a testing purposes
				"summary by the test swagger test filter",
				operation.notes(),
				operation.responseClass(),
				operation.nickname(),
				operation.position(),
				operation.produces(),
				operation.consumes(),
				operation.protocols(),
				operation.authorizations(),
				operation.parameters(),
				operation.responseMessages(),
				operation.deprecated());
	}
}
