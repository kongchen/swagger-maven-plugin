package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.jaxrs.model.*;
import com.github.kongchen.jaxrs.model.v2.Car;
import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.TestSwaggerApiReader;
import com.github.kongchen.swagger.docgen.TypeUtils;
import com.github.kongchen.swagger.docgen.filter.TestSwaggerSpecFilter;
import com.github.kongchen.swagger.docgen.mustache.*;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static junit.framework.Assert.*;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class MavenDocumentSourceTest {


    private ApiSource prepare() {
		ApiSource apiSource = new ApiSource();
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("com.github.kongchen.jaxrs.api.car;com.github.kongchen.jaxrs.api.garage");
        apiSource.setOutputPath("sample.html");
        apiSource.setOutputTemplate("https://github.com/kongchen/api-doc-template/blob/master/v1.1/html.mustache");
        apiSource.setSwaggerDirectory(null);
        apiSource.setOverridingModels("swagger-overriding-models.json");
		return apiSource;
    }

    @Test
    public void testIssue17() throws Exception, GenerateException {
		ApiSource apiSource = prepare();
        String locations = apiSource.getLocations();
        apiSource.setLocations("issue17");
        AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, new SystemStreamLog());
        documentSource.loadDocuments();
        OutputTemplate outputTemplate = new OutputTemplate(documentSource);
        Assert.assertEquals(outputTemplate.getDataTypes().size(), 1);
        Assert.assertEquals(outputTemplate.getDataTypes().iterator().next().getName(), "Child");
        // set back
        apiSource.setLocations(locations);
    }

    @Test
    public void test() throws Exception, GenerateException {
		ApiSource apiSource = prepare();
        AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, new SystemStreamLog());
        documentSource.loadDocuments();
        OutputTemplate outputTemplate = new OutputTemplate(documentSource);
        assertEquals(apiSource.getApiVersion(), outputTemplate.getApiVersion());
        assertEquals(3, outputTemplate.getApiDocuments().size());
        for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
            if (doc.getIndex() == 1) {
                Assert.assertEquals(doc.getResourcePath(), "/car");
                for (MustacheApi api : doc.getApis()) {
                    assertTrue(api.getUrl().startsWith(apiSource.getBasePath()));
                    assertFalse(api.getPath().contains("{format}"));
                    for (MustacheOperation op : api.getOperations()) {
                        if (op.getOpIndex() == 2) {

                            Assert.assertEquals(op.getParameters().size(), 4);

                            Assert.assertEquals("ETag", op.getResponseHeader().getParas().get(0).getName());

                            Assert.assertEquals("carId",
                                    op.getRequestPath().getParas().get(0).getName());
                            Assert.assertEquals("1.0 to 10.0",
                                    op.getRequestPath().getParas().get(0).getAllowableValue());

                            Assert.assertEquals("e",
                                    op.getRequestQuery().getParas().get(0).getName());

                            Assert.assertEquals("Accept",
                                    op.getRequestHeader().getParas().get(0).getName());
                            Assert.assertEquals("MediaType",
                                    op.getRequestHeader().getParas().get(0).getType());
                            Assert.assertEquals("application/json, application/*",
                                    op.getRequestHeader().getParas().get(0).getAllowableValue());
                            Assert.assertEquals(op.getResponseMessages().size(), 2);
                            Assert.assertEquals(op.getResponseMessages().get(0).getMessage(), "Invalid ID supplied");
                            Assert.assertEquals(op.getResponseMessages().get(0).getCode(), 400);
                            Assert.assertEquals(op.getResponseMessages().get(1).getCode(), 404);
                            // Testing deprecated method. Should remove tests when deprecated method is gone
                            Assert.assertEquals(op.getErrorResponses().size(), 2);
                            Assert.assertEquals(op.getErrorResponses().get(0).getMessage(), "Invalid ID supplied");
                            Assert.assertEquals(op.getErrorResponses().get(0).getCode(), 400);
                            Assert.assertEquals(op.getErrorResponses().get(1).getCode(), 404);
                            Assert.assertEquals(op.getAuthorizations().get(0).getType(), "oauth2");
                            Assert.assertEquals(op.getAuthorizations().get(0).getAuthorizationScopes().get(0).description(), "car1 des get");
                        }
                        if (op.getOpIndex() == 1) {
                            Assert.assertEquals(op.getSummary(), "search cars");
                        }
                    }
                }
            }
            if (doc.getIndex() == 2) {
                Assert.assertEquals(doc.getResourcePath(), "/v2/car");
            }
            if (doc.getIndex() == 3) {
                Assert.assertEquals(doc.getResourcePath(), "/garage");
            }

        }


        assertEquals(10, outputTemplate.getDataTypes().size());
        List<MustacheDataType> typeList = new LinkedList<MustacheDataType>();
        for (MustacheDataType type : outputTemplate.getDataTypes()) {
            typeList.add(type);
        }
        Collections.sort(typeList, new Comparator<MustacheDataType>() {

            @Override
            public int compare(MustacheDataType o1, MustacheDataType o2) {

                return o1.getName().compareTo(o2.getName());
            }
        });
        assertDataTypeInList(typeList, 0, Address.class);
        assertDataTypeInList(typeList, 1, BadIdResponse.class);
        assertDataTypeInList(typeList, 2, com.github.kongchen.jaxrs.model.Car.class);
        assertDataTypeInList(typeList, 3, Customer.class);
        assertDataTypeInList(typeList, 4, Email.class);
        assertDataTypeInList(typeList, 5, ForGeneric.class);
        assertDataTypeInList(typeList, 6, G1.class);
        assertDataTypeInList(typeList, 7, G2.class);
        assertDataTypeInList(typeList, 8, MediaType.class);
        assertDataTypeInList(typeList, 9, com.github.kongchen.jaxrs.model.v2.Car.class);
    }

	@Test
	public void testSwaggerFilter() throws Exception, GenerateException {
		ApiSource apiSource = prepare();
		apiSource.setSwaggerInternalFilter(TestSwaggerSpecFilter.class.getName());
		AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, new SystemStreamLog());
		documentSource.loadDocuments();
		OutputTemplate outputTemplate = new OutputTemplate(documentSource);
		assertEquals(apiSource.getApiVersion(), outputTemplate.getApiVersion());
		assertEquals(3, outputTemplate.getApiDocuments().size());
		for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
			if (doc.getIndex() == 1) {
				Assert.assertEquals(doc.getResourcePath(), "/car");
				for (MustacheApi api : doc.getApis()) {
					assertTrue(api.getUrl().startsWith(apiSource.getBasePath()));
					assertFalse(api.getPath().contains("{format}"));
					for (MustacheOperation op : api.getOperations()) {
						if (op.getOpIndex() == 2) {

							Assert.assertEquals(0, op.getParameters().size());

							Assert.assertEquals(op.getResponseMessages().size(), 2);
							Assert.assertEquals(op.getResponseMessages().get(0).getMessage(), "Invalid ID supplied");
							Assert.assertEquals(op.getResponseMessages().get(0).getCode(), 400);
							Assert.assertEquals(op.getResponseMessages().get(1).getCode(), 404);
							// Testing deprecated method. Should remove tests when deprecated method is gone
							Assert.assertEquals(op.getErrorResponses().size(), 2);
							Assert.assertEquals(op.getErrorResponses().get(0).getMessage(), "Invalid ID supplied");
							Assert.assertEquals(op.getErrorResponses().get(0).getCode(), 400);
							Assert.assertEquals(op.getErrorResponses().get(1).getCode(), 404);
							Assert.assertEquals(op.getAuthorizations().get(0).getType(), "oauth2");
							Assert.assertEquals(op.getAuthorizations().get(0).getAuthorizationScopes().get(0).description(), "car1 des get");
						}
						if (op.getOpIndex() == 1) {
							Assert.assertEquals(op.getSummary(), "search cars");
						}
					}
				}
			}
			if (doc.getIndex() == 2) {
				Assert.assertEquals(doc.getResourcePath(), "/v2/car");
			}
			if (doc.getIndex() == 3) {
				Assert.assertEquals(doc.getResourcePath(), "/garage");
			}

		}


		assertEquals(9, outputTemplate.getDataTypes().size());
		List<MustacheDataType> typeList = new LinkedList<MustacheDataType>();
		for (MustacheDataType type : outputTemplate.getDataTypes()) {
			typeList.add(type);
		}
		Collections.sort(typeList, new Comparator<MustacheDataType>() {

			@Override
			public int compare(MustacheDataType o1, MustacheDataType o2) {

				return o1.getName().compareTo(o2.getName());
			}
		});
        assertDataTypeInList(typeList, 0, Address.class);
        assertDataTypeInList(typeList, 1, BadIdResponse.class);
        assertDataTypeInList(typeList, 2, com.github.kongchen.jaxrs.model.Car.class);
        assertDataTypeInList(typeList, 3, Customer.class);
        assertDataTypeInList(typeList, 4, Email.class);
        assertDataTypeInList(typeList, 5, ForGeneric.class);
        assertDataTypeInList(typeList, 6, G1.class);
        assertDataTypeInList(typeList, 7, G2.class);
        assertDataTypeInList(typeList, 8, com.github.kongchen.jaxrs.model.v2.Car.class);
	}

	@Test
	public void testSwaggerApiReader() throws Exception {
		ApiSource apiSource = prepare();
		apiSource.setSwaggerApiReader(TestSwaggerApiReader.class.getName());
		AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, new SystemStreamLog());
		documentSource.loadDocuments();
		OutputTemplate outputTemplate = new OutputTemplate(documentSource);
		assertEquals(apiSource.getApiVersion(), outputTemplate.getApiVersion());
		assertEquals(3, outputTemplate.getApiDocuments().size());
		for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
			if (doc.getIndex() == 1) {
				Assert.assertEquals(doc.getResourcePath(), "/car");
				for (MustacheApi api : doc.getApis()) {
					assertTrue(api.getUrl().startsWith(apiSource.getBasePath()));
					assertFalse(api.getPath().contains("{format}"));
					for (MustacheOperation op : api.getOperations()) {
						Assert.assertEquals(op.getSummary(), "summary by the test swagger test filter");
					}
				}
			}

		}
	}

	@Test
	public void testSwaggerApiReaderDefaultConfig() throws Exception {
		ApiSource apiSource = prepare();
		AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, new SystemStreamLog());
		documentSource.loadDocuments();
		OutputTemplate outputTemplate = new OutputTemplate(documentSource);
		assertEquals(apiSource.getApiVersion(), outputTemplate.getApiVersion());
		assertEquals(3, outputTemplate.getApiDocuments().size());
		for (MustacheDocument doc : outputTemplate.getApiDocuments()) {
			if (doc.getIndex() == 1) {
				Assert.assertEquals(doc.getResourcePath(), "/car");
				for (MustacheApi api : doc.getApis()) {
					assertTrue(api.getUrl().startsWith(apiSource.getBasePath()));
					assertFalse(api.getPath().contains("{format}"));
					for (MustacheOperation op : api.getOperations()) {
						if (op.getOpIndex() == 1) {
							Assert.assertEquals(op.getSummary(), "search cars");
						}
					}
				}
			}

		}
	}

    private void assertDataTypeInList(List<MustacheDataType> typeList, int indexInList,
                                      Class<?> aClass) throws NoSuchMethodException, NoSuchFieldException {
        MustacheDataType dataType = typeList.get(indexInList);
        XmlRootElement root = aClass.getAnnotation(XmlRootElement.class);
        if (root == null) {
            assertEquals(dataType.getName(), aClass.getSimpleName());
        } else {
            assertEquals(dataType.getName(), root.name());
        }

        for (MustacheItem item : dataType.getItems()) {

            String name = item.getName();
            ApiModelProperty a = null;

            Field f = null;
            try {
                f = aClass.getDeclaredField(name);
                a = f.getAnnotation(ApiModelProperty.class);
                if (a == null) {
                    a = getApiProperty(aClass, name);
                }
            } catch (NoSuchFieldException e) {
                a = getApiProperty(aClass, name);
            }

            if (a == null) {
                return;
            }
            String type = a.dataType();
            if (type.equals("")) {
                // need to get true data type
                type = getActualDataType(aClass, name);
            }

            assertEquals(aClass.toString() + " type", type, item.getType());
            assertEquals(aClass.toString() + " required", a.required(), item.isRequired());
            assertEquals(aClass.toString() + " value", a.value(), nullToEmpty(item.getDescription()));
            assertEquals(aClass.toString() + " allowableValues", stringToList(a.allowableValues(), ","), stringToList(item.getAllowableValue(), ","));
        }
    }

    private String getActualDataType(Class<?> aClass, String name) throws NoSuchFieldException {
        String t = null;
        Class<?> type = null;
        Field f = null;
        boolean isArray = false;
        ParameterizedType parameterizedType = null;
        for (Method _m : aClass.getMethods()) {
            XmlElement ele = _m.getAnnotation(XmlElement.class);
            if (ele == null) {
                continue;
            }
            if (ele.name().equals(name)) {
                t = ele.type().getSimpleName();
                if (!t.equals("DEFAULT")) {
                    break;
                }
                type = _m.getReturnType();
                Type gType = _m.getGenericReturnType();
                if (gType instanceof ParameterizedType) {
                    parameterizedType = (ParameterizedType) gType;
                }
                break;
            }
        }
        if (type == null && t == null) {
            for (Field _f : aClass.getDeclaredFields()) {
                XmlElement ele = _f.getAnnotation(XmlElement.class);
                if (ele == null) {
                    continue;
                }
                if (ele.name().equals(name)) {
                    type = _f.getType();
                    break;
                }
            }
        }
        if (type == null) {
            f = aClass.getDeclaredField(name);
            type = f.getType();
            if (Collection.class.isAssignableFrom(type)) {
                parameterizedType = (ParameterizedType) f.getGenericType();
            }
        }
        if (parameterizedType != null) {
            Class<?> genericType = (Class<?>) parameterizedType.getActualTypeArguments()[0];
            t = genericType.getSimpleName();
            isArray = true;
        } else {
            t = type.getSimpleName();
        }

        t = toPrimitive(t);
        return isArray ? TypeUtils.AsArrayType(t) : t;
    }

    private String toPrimitive(String type) {
        if (type.equals("Byte")) {
            return "byte";
        }
        if (type.equals("Short")) {
            return "short";
        }
        if (type.equals("Integer")) {
            return "int";
        }
        if (type.equals("Long")) {
            return "long";
        }
        if (type.equals("Float")) {
            return "float";
        }
        if (type.equals("Double")) {
            return "double";
        }
        if (type.equals("Boolean")) {
            return "boolean";
        }
        if (type.equals("Character")) {
            return "char";
        }
        if (type.equals("String")) {
            return "string";
        }
        return type;
    }

    private ApiModelProperty getApiProperty(Class<?> aClass, String name) {
        ApiModelProperty a = null;
        for (Field _f : aClass.getDeclaredFields()) {
            XmlElement ele = _f.getAnnotation(XmlElement.class);
            if (ele == null) {
                continue;
            }
            if (ele.name().equals(name)) {
                a = _f.getAnnotation(ApiModelProperty.class);
                break;
            }
        }
        for (Method _m : aClass.getMethods()) {
            XmlElement ele = _m.getAnnotation(XmlElement.class);
            if (ele == null) {
                continue;
            }
            if (ele.name().equals(name)) {
                a = _m.getAnnotation(ApiModelProperty.class);
                break;
            }
        }
        return a;
    }

    private String nullToEmpty(String item) {
        return item == null ? "" : item;
    }

    // helper function so that we ignore any spaces we trim off or add when we build a string
    private List<String> stringToList(String srcStr, String token) {
        if (srcStr == null) {
            return null;
        }

        List<String> lst = new ArrayList<String>();
        String[] array = srcStr.split(token);

        for (String str : array) {
            lst.add(str.trim());
        }
        return lst;

    }
}
