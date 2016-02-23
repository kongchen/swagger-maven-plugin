package com.github.kongchen.smp.integration.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiDocumentMojo;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSource;
import org.codehaus.jettison.json.JSONObject;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.util.Map;

/**
 * @author Igor Gursky
 *         15.12.2015.
 */
public class TestUtils {

    public static String YamlToJson(String yamlString) {
        Yaml yaml = new Yaml();
        Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
        return new JSONObject(map).toString();
    }

    public static String createTempDirPath() throws Exception {
        File tempFile = File.createTempFile("swagmvn", "test");
        String path = tempFile.getAbsolutePath();
        tempFile.delete();
        return path;
    }

    public static void setCustomReader(ApiDocumentMojo mojo, String location) {
        for (ApiSource apiSource : mojo.getApiSources()) {
            apiSource.setSwaggerApiReader(location);
        }
    }

    public static void changeDescription(JsonNode root, String text) {
        JsonNode node = root.path("info");
        ((ObjectNode) node).put("description", text);
    }
}
