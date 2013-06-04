package com.github.kongchen.swagger.docgen.mavenplugin;

import com.github.kongchen.swagger.docgen.AbstractDocumentSource;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.TypeUtils;
import com.github.kongchen.swagger.docgen.mustache.MustacheDataType;
import com.github.kongchen.swagger.docgen.mustache.MustacheItem;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.wordnik.swagger.annotations.ApiProperty;
import org.apache.maven.plugin.logging.SystemStreamLog;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import sample.model.Address;
import sample.model.Customer;
import sample.model.Email;
import sample.model.Garage;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created with IntelliJ IDEA.
 * User: kongchen
 * Date: 6/4/13
 */
public class MavenDocumentSourceTest {
    ApiSource apiSource;

    @BeforeClass
    private void prepare() {
        apiSource = new ApiSource();
        apiSource.setApiVersion("1.0");
        apiSource.setBasePath("http://example.com");
        apiSource.setLocations("sample.api.car;sample.api.garage");
        apiSource.setOutputPath("sample.html");
        apiSource.setOutputTemplate("strapdown.html.mustache");
        apiSource.setSwaggerDirectory(null);
    }

    @Test
    public void test() throws Exception, GenerateException {
        AbstractDocumentSource documentSource = new MavenDocumentSource(apiSource, new SystemStreamLog());
        documentSource.loadDocuments();
        OutputTemplate outputTemplate = new OutputTemplate(documentSource);
        assertEquals(apiSource.getApiVersion(), outputTemplate.getApiVersion());
        assertEquals(apiSource.getBasePath(), outputTemplate.getBasePath());
        assertEquals(3, outputTemplate.getApiDocuments().size());

        assertEquals(6, outputTemplate.getDataTypes().size());
        List<MustacheDataType> typeList = new LinkedList<MustacheDataType>();
        for (MustacheDataType type : outputTemplate.getDataTypes()) {
            typeList.add(type);
        }
        assertDataTypeInList(typeList, 0, Address.class);
        assertDataTypeInList(typeList, 1, sample.model.Car.class);
        assertDataTypeInList(typeList, 2, Customer.class);
        assertDataTypeInList(typeList, 3, Email.class);
        assertDataTypeInList(typeList, 4, Garage.class);
        assertDataTypeInList(typeList, 5, sample.model.v2.Car.class);


    }

    private void assertDataTypeInList(List<MustacheDataType> typeList, int indexInList, Class<?> aClass) throws NoSuchMethodException {
        MustacheDataType dataType = typeList.get(indexInList);
        XmlRootElement root = aClass.getAnnotation(XmlRootElement.class);
        if (root == null) {
            assertEquals(dataType.getName(), aClass.getSimpleName());
        } else {
            assertEquals(dataType.getName(), root.name());
        }

        for (MustacheItem item : dataType.getItems()) {

            String name = item.getName();
            ApiProperty a = null;

            Field f = null;
            try {
                f = aClass.getDeclaredField(name);
                a = f.getAnnotation(ApiProperty.class);
                if (a == null) {
                    a = getApiProperty(aClass, name);
                }
            } catch (NoSuchFieldException e) {
                a = getApiProperty(aClass, name);
            }


            assertEquals(a.access(), nullToEmpty(item.getAccess()));
            assertEquals(a.notes(), nullToEmpty(item.getNotes()));
            assertEquals(a.dataType(), nullToEmpty(TypeUtils.getTrueType(item.getType())));
            assertEquals(a.required(), item.isRequired());
            assertEquals(a.value(), nullToEmpty(item.getDescription()));
        }
    }

    private ApiProperty getApiProperty(Class<?> aClass, String name) {
        ApiProperty a = null;
        for (Field _f : aClass.getDeclaredFields()) {
            XmlElement ele = _f.getAnnotation(XmlElement.class);
            if (ele == null) {
                continue;
            }
            if (ele.name().equals(name)) {
                a = _f.getAnnotation(ApiProperty.class);
                break;
            }
        }
        for (Method _m : aClass.getMethods()) {
            XmlElement ele = _m.getAnnotation(XmlElement.class);
            if (ele == null) {
                continue;
            }
            if (ele.name().equals(name)) {
                a = _m.getAnnotation(ApiProperty.class);
                break;

            }
        }
        return a;
    }

    private String nullToEmpty(String item) {
        return item == null ? "" : item;
    }
}
