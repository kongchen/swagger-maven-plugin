package com.github.kongchen.swagger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kongchen.swagger.docgen.GenerateException;
import com.github.kongchen.swagger.docgen.LogAdapter;
import com.github.kongchen.swagger.docgen.mavenplugin.ApiSourceInfo;
import com.github.kongchen.swagger.docgen.mustache.OutputTemplate;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.wordnik.swagger.converter.OverrideConverter;
import com.wordnik.swagger.core.util.JsonSerializer;
import com.wordnik.swagger.core.util.JsonUtil;
import com.wordnik.swagger.model.ApiListing;
import com.wordnik.swagger.model.ApiListingReference;
import com.wordnik.swagger.model.ResourceListing;

import com.wordnik.swagger.models.Info;
import com.wordnik.swagger.models.Path;
import com.wordnik.swagger.models.Swagger;
import org.apache.commons.io.FileUtils;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 *
 * @author: chekong 05/13/2013
 */
public abstract class AbstractDocumentSource {

	protected final LogAdapter LOG;

	private final String outputPath;

	private final String templatePath;

	private final String mustacheFileRoot;

	private final String swaggerPath;

	protected Swagger swagger;

	List<Map<String, Path>> validDocuments = new ArrayList<Map<String, Path>>();

	private String basePath = "";

	private String apiVersion;
    
    private Info info;

	private ObjectMapper mapper = new ObjectMapper();

	private OutputTemplate outputTemplate;

	private boolean useOutputFlatStructure;

	private String overridingModels;

	public AbstractDocumentSource(LogAdapter logAdapter, String outputPath,
			String outputTpl, String swaggerOutput, String mustacheFileRoot,
			boolean useOutputFlatStructure1, String overridingModels) {
		LOG = logAdapter;
		this.outputPath = outputPath;
		this.templatePath = outputTpl;
		this.mustacheFileRoot = mustacheFileRoot;
		this.useOutputFlatStructure = useOutputFlatStructure1;
		this.swaggerPath = swaggerOutput;
		this.overridingModels = overridingModels;
	}

	public abstract void loadDocuments() throws Exception, GenerateException;

	public String getBasePath() {
		return basePath;
	}

	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public OutputTemplate getOutputTemplate() {
		return outputTemplate;
	}

    public Info getApiInfo() {
        return info;
    }

    public void setApiInfo(Info info) {
        this.info = info;
    }

    protected void acceptDocument(Swagger swagger) {
		validDocuments.add(swagger.getPaths());
	}

	public List<Map<String, Path>> getValidDocuments() {
		return validDocuments;
	}

	public void toSwaggerDocuments(String swaggerUIDocBasePath)
			throws GenerateException {
		if (swaggerPath == null) {
			return;
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

		prepareServiceDocument();
		// rewrite basePath in swagger-ui output file using the value in
		// configuration file.
		writeInDirectory(dir, swagger, swaggerUIDocBasePath);
		for (ApiListing doc : validDocuments) {
			writeInDirectory(dir, doc, basePath);
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

	private void prepareServiceDocument() {
		List<ApiListingReference> apiListingReferences = new ArrayList<ApiListingReference>();
		for (Iterator<ApiListingReference> iterator = swagger.apis()
				.iterator(); iterator.hasNext();) {
			ApiListingReference apiListingReference = iterator.next();
			String newPath = apiListingReference.path();
			if (useOutputFlatStructure) {
				newPath = newPath.replaceAll("/", "_");
				if (newPath.startsWith("_")) {
					newPath = "/" + newPath.substring(1);
				}
			}
			newPath += ".{format}";
			apiListingReferences.add(new ApiListingReference(newPath,
					apiListingReference.description(), apiListingReference
							.position()));
		}
		// there's no setter of path for ApiListingReference, we need to create
		// a new ResourceListing for new path
		swagger = new ResourceListing(swagger.apiVersion(),
				swagger.swaggerVersion(),
				scala.collection.immutable.List.fromIterator(JavaConversions
						.asScalaIterator(apiListingReferences.iterator())),
				swagger.authorizations(), swagger.info());
	}

	protected String resourcePathToFilename(String resourcePath) {
		if (resourcePath == null) {
			return "service.json";
		}
		String name = resourcePath;
		if (name.startsWith("/")) {
			name = name.substring(1);
		}
		if (name.endsWith("/")) {
			name = name.substring(0, name.length() - 1);
		}

		if (useOutputFlatStructure) {
			name = name.replaceAll("/", "_");
		}

		return name + ".json";
	}

	private void writeInDirectory(File dir, ApiListing apiListing,
			String basePath) throws GenerateException {
		String filename = resourcePathToFilename(apiListing.resourcePath());
		try {
			File serviceFile = createFile(dir, filename);
			String json = JsonSerializer.asJson(apiListing);
			JsonNode tree = mapper.readTree(json);
			if (basePath != null) {
				((ObjectNode) tree).put("basePath", basePath);
			}
			JsonUtil.mapper().writerWithDefaultPrettyPrinter()
					.writeValue(serviceFile, tree);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
	}

	private void writeInDirectory(File dir, ResourceListing resourceListing,
			String basePath) throws GenerateException {
		String filename = resourcePathToFilename(null);
		try {
			File serviceFile = createFile(dir, filename);
			String json = JsonSerializer.asJson(resourceListing);
			JsonNode tree = mapper.readTree(json);
			if (basePath != null) {
				((ObjectNode) tree).put("basePath", basePath);
			}

			JsonUtil.mapper().writerWithDefaultPrettyPrinter()
					.writeValue(serviceFile, tree);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
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

	public OutputTemplate prepareMustacheTemplate() throws GenerateException {
		this.outputTemplate = new OutputTemplate(swagger);
		return outputTemplate;
	}

	public void toDocuments() throws GenerateException {
		if (outputTemplate == null) {
			prepareMustacheTemplate();
		}
		if (outputTemplate.getApiDocuments().isEmpty()) {
			LOG.warn("nothing to write.");
			return;
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
			URL url = getTemplateUri().toURL();
			InputStreamReader reader = new InputStreamReader(url.openStream(),
					Charset.forName("UTF-8"));
			Mustache mustache = getMustacheFactory().compile(reader,
					templatePath);

			mustache.execute(writer, outputTemplate).flush();
			writer.close();
			LOG.info("Done!");
		} catch (MalformedURLException e) {
			throw new GenerateException(e);
		} catch (IOException e) {
			throw new GenerateException(e);
		}
	}

	private URI getTemplateUri() throws GenerateException {
		URI uri;
		try {
			uri = new URI(templatePath);
		} catch (URISyntaxException e) {
			File file = new File(templatePath);
			if (!file.exists()) {
				throw new GenerateException(
						"Template "
								+ file.getAbsoluteFile()
								+ " not found. You can go to https://github.com/kongchen/api-doc-template to get templates.");
			}
			uri = file.toURI();
		}
		if (!uri.isAbsolute()) {
			File file = new File(templatePath);
			if (!file.exists()) {
				throw new GenerateException(
						"Template "
								+ file.getAbsoluteFile()
								+ " not found. You can go to https://github.com/kongchen/api-doc-template to get templates.");
			} else {
				uri = new File(templatePath).toURI();
			}
		}
		return uri;
	}

	private DefaultMustacheFactory getMustacheFactory() {
		if (mustacheFileRoot == null) {
			return new DefaultMustacheFactory();
		} else {
			return new DefaultMustacheFactory(new File(mustacheFileRoot));
		}
	}
}
