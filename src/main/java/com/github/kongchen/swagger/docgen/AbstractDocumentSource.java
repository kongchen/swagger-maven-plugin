package com.github.kongchen.swagger.docgen;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.helper.StringHelpers;
import com.github.jknack.handlebars.io.TemplateLoader;
import com.wordnik.swagger.models.*;
import com.wordnik.swagger.util.Json;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong 05/13/2013
 */
public abstract class AbstractDocumentSource {
	protected final LogAdapter LOG;

	private final String outputPath;

	private final String templatePath;

	private final String swaggerPath;

	private final String overridingModels;

	protected Swagger swagger;

	private ObjectMapper mapper = new ObjectMapper();
	private boolean isSorted = false;

	public AbstractDocumentSource(LogAdapter logAdapter, String outputPath,
								  String outputTpl, String swaggerOutput, String overridingModels) {
		LOG = logAdapter;
		this.outputPath = outputPath;
		this.templatePath = outputTpl;
		this.swaggerPath = swaggerOutput;
		this.overridingModels = overridingModels;
	}

	public abstract void loadDocuments() throws Exception, GenerateException, GenerateException;

	public void toSwaggerDocuments(String swaggerUIDocBasePath)
			throws GenerateException {
		mapper.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
		if (swaggerPath == null) {
			return;
		}
		if (!isSorted) {
			Utils.sortSwagger(swagger);
			isSorted = true;
		}
		File dir = new File(swaggerPath);
		if (dir.isFile()) {
			throw new GenerateException(String.format(
					"Swagger-outputDirectory[%s] must be a directory!",
					swaggerPath));
		}

		if (!dir.exists()) {
			try {
				FileUtils.forceMkdir(dir);
			} catch (IOException e) {
				throw new GenerateException(String.format(
						"Create Swagger-outputDirectory[%s] failed.",
						swaggerPath));
			}
		}
		cleanupOlds(dir);

		File swaggerFile = new File(dir, "swagger.json");
		try {
			ObjectWriter jsonWriter = mapper.writer(new DefaultPrettyPrinter());
			jsonWriter.writeValue(swaggerFile, swagger);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
	}

	public void loadOverridingModels() throws GenerateException {
		if (overridingModels != null) {
			try {
				JsonNode readTree = mapper.readTree(this.getClass()
						.getResourceAsStream(overridingModels));
				for (JsonNode jsonNode : readTree) {
					JsonNode classNameNode = jsonNode.get("className");
					String className = classNameNode.asText();
					JsonNode jsonStringNode = jsonNode.get("jsonString");
					String jsonString = jsonStringNode.asText();


					// 1.5.0 does not support override models by now
				}
			} catch (JsonProcessingException e) {
				throw new GenerateException(
						String.format(
								"Swagger-overridingModels[%s] must be a valid JSON file!",
								overridingModels), e);
			} catch (IOException e) {
				throw new GenerateException(String.format(
						"Swagger-overridingModels[%s] not found!",
						overridingModels), e);
			}
		}
	}

	private void cleanupOlds(File dir) {
		if (dir.listFiles() != null) {
			for (File f : dir.listFiles()) {
				if (f.getName().endsWith("json")) {
					f.delete();
				}
			}
		}
	}

	private void writeInDirectory(File dir, Swagger swaggerDoc,
			String basePath) throws GenerateException {

//		try {
//			File serviceFile = createFile(dir, filename);
//			String json = JsonSerializer.asJson(swaggerDoc);
//			JsonNode tree = mapper.readTree(json);
//			if (basePath != null) {
//				((ObjectNode) tree).put("basePath", basePath);
//			}
//
//			JsonUtil.mapper().writerWithDefaultPrettyPrinter()
//					.writeValue(serviceFile, tree);
//		} catch (IOException e) {
//			throw new GenerateException(e);
//		}
	}

	protected File createFile(File dir, String outputResourcePath)
			throws IOException {
		File serviceFile;
		int i = outputResourcePath.lastIndexOf("/");
		if (i != -1) {
			String fileName = outputResourcePath.substring(i + 1);
			String subDir = outputResourcePath.substring(0, i);
			File finalDirectory = new File(dir, subDir);
			finalDirectory.mkdirs();
			serviceFile = new File(finalDirectory, fileName);
		} else {
			serviceFile = new File(dir, outputResourcePath);
		}
		while (!serviceFile.createNewFile()) {
			serviceFile.delete();
		}
		LOG.info("Creating file " + serviceFile.getAbsolutePath());
		return serviceFile;
	}

	public void toDocuments() throws GenerateException {

		if (!isSorted) {
			Utils.sortSwagger(swagger);
			isSorted = true;
		}
		LOG.info("Writing doc to " + outputPath + "...");

		FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(outputPath);
		} catch (FileNotFoundException e) {
			throw new GenerateException(e);
		}
		OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream,
				Charset.forName("UTF-8"));

		try {
			TemplatePath tp = Utils.parseTemplateUrl(templatePath);

			Handlebars handlebars = new Handlebars(tp.loader);
			initHandlebars(handlebars);

			Template template = handlebars.compile(tp.name);

			template.apply(swagger, writer);
			writer.close();
			LOG.info("Done!");
		} catch (MalformedURLException e) {
			throw new GenerateException(e);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
	}

	private void initHandlebars(Handlebars handlebars) {
		handlebars.registerHelper("ifeq", new Helper<String>() {
			@Override
			public CharSequence apply(String value, Options options) throws IOException {
				if (value == null || options.param(0) == null) return options.inverse();
				if (value.equals(options.param(0))){
					return options.fn();
				}
				return options.inverse();
			}
		});

		handlebars.registerHelper("basename", new Helper<String>() {
			@Override
			public CharSequence apply(String value, Options options) throws IOException {
				if (value == null) return null;
				int lastSlash = value.lastIndexOf("/");
				if (lastSlash == -1) {
					return value;
				} else {
					return value.substring(lastSlash + 1);
				}
			}
		});

		handlebars.registerHelper(StringHelpers.join.name(), StringHelpers.join);
		handlebars.registerHelper(StringHelpers.lower.name(), StringHelpers.lower);
	}

	private String getUrlParent(URL url) {

		if (url == null) return null;

		String strurl = url.toString();
		int idx = strurl.lastIndexOf('/');
		if (idx == -1) {
			return strurl;
		}
		return strurl.substring(0,idx);
	}


}


class TemplatePath {
	String prefix;
	String name;
	String suffix;
	public TemplateLoader loader;
}