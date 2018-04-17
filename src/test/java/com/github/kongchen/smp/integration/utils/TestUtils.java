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

    public static String YamlToJson(final String yamlString) {
        final Yaml yaml = new Yaml();
        final Map<String, Object> map = (Map<String, Object>) yaml.load(yamlString);
        return new JSONObject(map).toString();
    }

    public static String createTempDirPath() throws Exception {
        final File tempFile = File.createTempFile("swagmvn", "test");
        final String path = tempFile.getAbsolutePath();
        tempFile.delete();
        return path;
    }

    public static void setCustomReader(final ApiDocumentMojo mojo, final String location) {
        for (final ApiSource apiSource : mojo.getApiSources()) {
            apiSource.setSwaggerApiReader(location);
        }
    }

    public static void changeDescription(final JsonNode root, final String text) {
        final JsonNode node = root.path("info");
        ((ObjectNode) node).put("description", text);
    }
}
