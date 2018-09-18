# Swagger Maven Plugin
[![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.kongchen/swagger-maven-plugin/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.kongchen/swagger-maven-plugin)

This plugin enables your Swagger-annotated project to generate **Swagger specs** and **customizable, templated static documents** during the maven build phase. Unlike swagger-core, swagger-maven-plugin does not actively serve the spec with the rest of the application; it generates the spec as a build artifact to be used in downstream Swagger tooling.

# Features

* Supports [Swagger Spec 2.0](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md)
* Supports [SpringMvc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html) & [JAX-RS](https://jax-rs-spec.java.net/)
* Quickly generates *[swagger.json](https://github.com/kongchen/swagger-maven-example/blob/master/generated/swagger-ui/swagger.json)* and [static document](http://htmlpreview.github.io/?https://raw.github.com/kongchen/swagger-maven-example/master/generated/document.html) by `mvn compile`
* Use [Handlebars](http://handlebarsjs.com/) as template to customize the static document.

# Versions
- [3.1.0](https://github.com/kongchen/swagger-maven-plugin/) supports Swagger Spec [2.0](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md), support JAX-RS & SpingMVC. (**ACTIVE!**)
- [3.0.1](https://github.com/kongchen/swagger-maven-plugin/tree/swagger-core_com.wordnik_namespaces/) supports Swagger Spec [2.0](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md), support JAX-RS & SpingMVC. (**ACTIVE!**)
- [2.3.4](https://github.com/kongchen/swagger-maven-plugin/tree/spec1.2) supports Swagger Spec [1.2](https://github.com/swagger-api/swagger-spec/blob/master/versions/1.2.md), support JAX-RS & SpringMVC. (**Lazily maintained**)
- [1.1.1](https://github.com/kongchen/swagger-maven-plugin/tree/1.1.1) supports Swagger Spec 1.1. (**No longer maintained**)

## Upgrading from 3.0.1 to 3.1.0+
Version 3.1.0+ of this plugin depends on the re-packaged/re-branded io.swagger.swagger-core dependency, which is formerly known as com.wordnik.swagger-core. If you use 3.1.0+, you must use the swagger-core dependency in the io.swagger namespace instead of the com.wordnik namespace, which is deprecated. You may see an example of migrating a project from 3.0.1 to 3.1.0 in the [swagger-maven-plugin example project](https://github.com/swagger-maven-plugin/swagger-maven-example/commit/3d6bfa06d638d0855edc04816d4e35bff4a5e771#diff-600376dffeb79835ede4a0b285078036).


# Usage
Import the plugin in your project by adding following configuration in your `plugins` block:

```xml
<build>
	<plugins>
		<plugin>
			<groupId>com.github.kongchen</groupId>
			<artifactId>swagger-maven-plugin</artifactId>
			<version>${swagger-maven-plugin-version}</version>
			<configuration>
				<apiSources>
					<apiSource>
						...
					</apiSource>
				</apiSources>
			</configuration>
			<executions>
				<execution>
					<phase>compile</phase>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
		</plugin>
	</plugins>
</build>
```

The `executions` block is used to specify the phase of the build lifecycle you want the plugin to be executed in.

# Configuration for `configuration`

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `skipSwaggerGeneration` | If `true`, swagger generation will be skipped. Default is `false`. User property is `swagger.skip`. |
| `apiSources` | List of `apiSource` elements. One `apiSource` can be considered as a version of APIs of your service. You can specify several `apiSource` elements, though generally one is enough. |

# Configuration for `apiSource`

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `springmvc` | Tell the plugin your project is a JAX-RS(`false`) or a SpringMvc(`true`) project |
| `locations` **required**| Classes containing Swagger's annotation ```@Api```, or packages containing those classes can be configured here. Each item must be located inside a <location> tag. Example: `<locations><location>com.github.kongchen.swagger.sample.wordnik.resource</location><location>com.github.kongchen.swagger.sample.wordnik.resource2</location></locations>` |
| `schemes` | The transfer protocol of the API. Values MUST be from the list: `"http"`, `"https"`, `"ws"`, `"wss"`. Each value must be located inside its own `<scheme>` tag. Example: `<schemes><scheme>http</scheme><scheme>https</scheme></schemes>` |
| `host` | The host (name or IP) serving the API. This MUST be the host only and does not include the scheme nor sub-paths. It MAY include a port.  The host does not support [path templating](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#pathTemplating).|
| `basePath` | The base path on which the API is served, which is relative to the host. The value MUST start with a leading slash (/). The basePath does not support [path templating](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#pathTemplating). |
| `descriptionFile` | A Path to file with description to be set to Swagger Spec 2.0's [info Object](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#infoObject) |
| `info` **required**| The basic information of the api, using same definition as Swagger Spec 2.0's [info Object](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#infoObject) |
| `securityDefinitions` | You can put your [security definitions](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#security-definitions-object) here, see more details [below](#securityDefinitions)|
| `templatePath` | The path of a [handlebars](http://handlebarsjs.com/) template file, see more details [below](#templatefile).|
| `outputPath` | The path of the generated static document, not existed parent directories will be created. If you don't want to generate a static document, just don't set it. |
| `outputFormats` | The format types of the generated swagger spec. Valid values are `json`, `yaml` or both `json,yaml`. The `json` format is default.|
| `swaggerDirectory` | The directory of generated `swagger.json` file. If null, no `swagger.json` will be generated. |
| `swaggerFileName` | The filename of generated `filename.json` file. If null, `swagger.json` will be generated. |
| `swaggerApiReader` | If not null, the value should be a full name of the class implementing `com.github.kongchen.swagger.docgen.reader.ClassSwaggerReader`. This allows you to flexibly implement/override the reader's implementation. `com.github.kongchen.swagger.docgen.reader.SwaggerReader` can be used to strictly use the official Swagger reader in order to generate the exact same output as Swagger''s runtime generation (with all its bugs). Default is `com.github.kongchen.swagger.docgen.reader.JaxrsReader`.  |
| `attachSwaggerArtifact` | If enabled, the generated `swagger.json` file will be attached as a maven artifact. The `swaggerDirectory`'s name will be used as an artifact classifier. Default is `false`. |
| `modelSubstitute` | The model substitute file's path, see more details [below](#model-substitution)|
| `typesToSkip` | Nodes of class names to explicitly skip during parameter processing. More details [below](#typesToSkip)|
| `apiModelPropertyAccessExclusions` | Allows the exclusion of specified `@ApiModelProperty` fields. This can be used to hide certain model properties from the swagger spec. More details [below](#apiModelPropertyAccessExclusions)|
| `jsonExampleValues` | If `true`, all example values in `@ApiModelProperty` will be handled as json raw values. This is useful for creating valid examples in the generated json for all property types, including non-string ones. |
| `modelConverters` | List of custom implementations of `io.swagger.converter.ModelConverter` that should be used when generating the swagger files. |
| `swaggerExtensions` | List of custom implementations of `io.swagger.jaxrs.ext.SwaggerExtension` that should be used when generating the swagger files. |
| `enabledObjectMapperFeatures`    | List of ConfigFeature enums that are supported by ObjectMapper.configure - the feature is set to true. https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#features) here, see more details [below](#features)|
| `disabledObjectMapperFeatures`    | List of ConfigFeature enums that are supported by ObjectMapper.configure - the feature is set to false. https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#features) here, see more details [below](#features)|
| `operationIdFormat` | Format of `operationId` used in Swagger spec. For historical reasons default is Java method name. Since 3.1.8, for new APIs suggested format is: `{{className}}_{{methodName}}_{{httpMethod}}`. `{{packageName}}` token is also supported. |
# <a id="templatefile">Template File</a>

If you'd like to generate a template-driven static document, such as markdown or HTML documentation, you'll need to specify a [handlebars](https://github.com/jknack/handlebars.java) template file in ```templatePath```.
The value for ```templatePath``` supports 2 kinds of path:

1. Resource in classpath. You should specify a resource path with a **classpath:** prefix.
    e.g:

    1. **`classpath:/markdown.hbs`**
    1. **`classpath:/templates/hello.html`**

1. Local file's absolute path.
    e.g:

    1. **`${basedir}/src/main/resources/markdown.hbs`**
    1. **`${basedir}/src/main/resources/template/hello.html`**


There's a [standalone project](https://github.com/kongchen/api-doc-template) for the template files, fetch them and customize it for your own project.

# <a id="securityDefinitions">Security Definitions</a>

There are 3 types of security definitions according to Swagger Spec: `basic`, `apiKey` and `oauth2`.

You can define multi definitions here, but you should fully follow [the spec](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#securitySchemeObject).

You can define a `basic` definition like this:

```xml
<securityDefinition>
    <name>MybasicAuth</name>
    <type>basic</type>
</securityDefinition>
```

or define several definitions in a json file and specify the json path like this:

```xml
<securityDefinition>
    <json>/securityDefinition.json</json>
</securityDefinition>
```

The file will be read by `getClass().getResourceAsStream`, so please note the path you configured.

Alternatively, specify the __absolute__ file path to the json definition file:

```xml
<securityDefinition>
    <jsonPath>${basedir}/securityDefinition.json</jsonPath>
</securityDefinition>
```

The `securityDefinition.json` file should also follow the spec, one sample file like this:

```js
{
  "api_key": {
    "type": "apiKey",
    "name": "api_key",
    "in": "header"
  },
  "petstore_auth": {
    "type": "oauth2",
    "authorizationUrl": "http://swagger.io/api/oauth/dialog",
    "flow": "implicit",
    "scopes": {
      "write:pets": "modify pets in your account",
      "read:pets": "read your pets"
    }
  }
}
```
# <a id="modelSubstitute">Model Substitution</a>
Throughout the course of working with Swagger, you may find that you need to substitute non-primitive objects for primitive objects. This is called model substitution, and it is supported by swagger-maven-plugin. In order to configure model substitution, you'll need to create a model substitute file. This file is a simple text file containing `n` lines, where each line tells swagger-maven-plugin to substitutes a model class with the supplied substitute. These two classes should be separated by a colon (`:`).

## Sample model substitution

```
com.foo.bar.PetName : java.lang.String
```

The above model substitution configuration would tell the plugin to substitute `com.foo.bar.PetName` with `java.lang.String`.  As a result, the generated `swagger.json` would look like this ...

```
 "definitions" : {
    "Pet" : {
      "properties" : {
        ...
        "petName" : {
          "type" : "string"
        }
        ...
      }
    }
```
... instead of like this:

```
 "definitions" : {
    "Pet" : {
      "properties" : {
        ...
        "petName" : {
          "$ref" : "#/definitions/PetName"
        }
        ...
      }
    }
```

The model substitution file will be read by `getClass().getResourceAsStream`, so please note the path you configured.

# <a id="typesToSkip">Skipping Types During Processing with `typesToSkip`</a>

You can instruct `swagger-maven-plugin` to skip processing the parameters of certain types by adding the following to your pom.xml:

```
<typesToSkip>
  <typeToSkip>com.foobar.skipper.SkipThisClassPlease</typeToSkip>
  <typeToSkip>com.foobar.skipper.AlsoSkipThisClassPlease</typeToSkip>
</typesToSkip>
```

This requires at least `swagger-maven-plugin` version 3.1.1-SNAPSHOT.

# <a id="apiModelPropertyAccessExclusions">Excluding certain `@ApiModelProperty` items</a>

If you'd like to exclude certain `@ApiModelProperty`s based on their `access` values, you may do so by adding the following as a child node of `apiSource` in your pom.xml:

```
<apiModelPropertyAccessExclusions>
    <apiModelPropertyAccessExclusion>secret-property</apiModelPropertyAccessExclusion>
</apiModelPropertyAccessExclusions>
```

The above setting would prevent `internalThing` from appearing in the swagger spec output, given this annotated model:

```
...
    @ApiModelProperty(name = "internalThing", access = "secret-property")
    public String getInternalThing() {
        return internalThing;
    }
...
```

Note: In order to use `apiModelPropertyAccessExclusions`, you must specify both the `name` and `access` fields of the property you wish to exclude. Additionally, `apiModelPropertyAccessExclusions` requires at least `swagger-maven-plugin` version 3.1.1-SNAPSHOT.

# Defining common Swagger parameters with JAX-RS annotations

When defining the Swagger API with JAX-RS, it is possible to specify common parameters by defining an `@Api` class that only contains fields annotated with JAX-RS parameter annotations. Note that this class should not contain any `@Path` or HTTP method annotation in order to consider it as a declaration of common parameters. When a common parameter is specified in another JAX-RS resource class, the parameter will have a reference to the common parameter.

The result of having common parameters and parameter references can be seen in the test file [swagger-common-parameters.json](src/test/resources/swagger-common-parameters.json). The `@Api` class used to generate this example can be seen in the test file [JaxrsReaderTest.java](src/test/java/com/github/kongchen/swagger/docgen/reader/JaxrsReaderTest.java).

# Install/Deploy `swagger.json`

You can instruct `swagger-maven-plugin` to deploy the generated `swagger.json` by adding the following to your pom.xml:

```
<swaggerDirectory>${project.build.directory}/swagger-ui</swaggerDirectory>
<attachSwaggerArtifact>true</attachSwaggerArtifact>

```

or `custom.json` by adding the following to your pom.xml:

```
<swaggerDirectory>${project.build.directory}/swagger-ui</swaggerDirectory>
<swaggerFileName>custom</swaggerFileName>
<attachSwaggerArtifact>true</attachSwaggerArtifact>

```

The above setting attaches the generated file to Maven for install/deploy purpose with `swagger-ui`as classifier and `json` as type

# <a id="features">Object Mapper Configuration Features</a>

Enables or disables the static Json.mapper config, by setting the feature of a known enum to true or false respectively.

N.B. Inner class fully qualified domain names are using $

```
<configuration>
...
   <enabledObjectMapperFeatures>
       <feature>com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING</feature>
       <feature>com.fasterxml.jackson.core.JsonParser$Feature.ALLOW_NUMERIC_LEADING_ZEROS</feature>
   </enabledObjectMapperFeatures>
...
    <disabledObjectMapperFeatures>
        <feature>com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS</feature>
    </disabledObjectMapperFeatures>
</configuration>
```


# Example
There's a [sample here](https://github.com/swagger-maven-plugin/swagger-maven-example), just fork it and have a try.

## A Sample Configuration

```xml
<project>
...
<build>
<plugins>
<plugin>
    <groupId>com.github.kongchen</groupId>
    <artifactId>swagger-maven-plugin</artifactId>
    <version>3.0.0</version>
    <configuration>
        <apiSources>
            <apiSource>
	            <springmvc>true</springmvc>
                <locations>
                    <location>com.wordnik.swagger.sample</location>
                </locations>
                <schemes>
                    <scheme>http</scheme>
                    <scheme>https</scheme>
                </schemes>
                <host>www.example.com:8080</host>
                <basePath>/api</basePath>
                <info>
                    <title>Swagger Maven Plugin Sample</title>
                    <version>v1</version>
                    <!-- use markdown here because I'm using markdown for output,
                    if you need to use html or other markup language, you need to use your target language,
                     and note escape your description for xml -->
                    <description>
                        This is a sample.
                    </description>
                    <termsOfService>
                        http://www.github.com/kongchen/swagger-maven-plugin
                    </termsOfService>
                    <contact>
                        <email>kongchen@gmail.com</email>
                        <name>Kong Chen</name>
                        <url>http://kongch.com</url>
                    </contact>
                    <license>
                        <url>http://www.apache.org/licenses/LICENSE-2.0.html</url>
                        <name>Apache 2.0</name>
                    </license>
                </info>
                <securityDefinitions>
                    <securityDefinition>
                        <name>basicAuth</name>
                        <type>basic</type>
                    </securityDefinition>
                    <securityDefinition>
                        <json>/securityDefinition.json</json>
                    </securityDefinition>
                </securityDefinitions>
                <!-- Support classpath or file absolute path here.
                1) classpath e.g: "classpath:/markdown.hbs", "classpath:/templates/hello.html"
                2) file e.g: "${basedir}/src/main/resources/markdown.hbs",
                    "${basedir}/src/main/resources/template/hello.html" -->
                <templatePath>${basedir}/src/test/resources/strapdown.html.hbs</templatePath>
                <outputPath>${basedir}/generated/document.html</outputPath>
                <swaggerDirectory>${basedir}/generated/swagger-ui</swaggerDirectory>
                <swaggerApiReader>com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader</swaggerApiReader>
                <attachSwaggerArtifact>true</attachSwaggerArtifact>
                <modelConverters>io.swagger.validator.BeanValidator</modelConverters>
                <swaggerExtensions>
                    <swaggerExtension>com.example.VendorExtension</swaggerExtension>
                </swaggerExtensions>
                <enabledObjectMapperFeatures>
                    <feature>com.fasterxml.jackson.databind.SerializationFeature.WRITE_ENUMS_USING_TO_STRING</feature>
                    <feature>com.fasterxml.jackson.core.JsonParser$Feature.ALLOW_NUMERIC_LEADING_ZEROS</feature>
                </enabledObjectMapperFeatures>
                <disabledObjectMapperFeatures>
                    <feature>com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS</feature>
                </disabledObjectMapperFeatures>
                <operationIdFormat>{{className}}_{{methodName}}_{{httpMethod}}</operationIdFormat>
            </apiSource>
        </apiSources>
    </configuration>
    <executions>
        <execution>
            <phase>compile</phase>
            <goals>
                <goal>generate</goal>
            </goals>
        </execution>
    </executions>
    <dependencies>
        <!-- Adding dependency to swagger-hibernate-validations to enable the BeanValidator as a custom
             model converter -->
        <dependency>
            <groupId>io.swagger</groupId>
            <artifactId>swagger-hibernate-validations</artifactId>
            <version>1.5.6</version>
        </dependency>
    </dependencies>
</plugin>
...
</plugins>
</build>
</project>
```

# FAQ

## 1. SNAPSHOT Version
SNAPSHOT versions are available for verifying issues and new features. If you would like to try to verify the fixed issues or the new added features, you may need to add a `pluginRepository` node in your `pom.xml`:

```
<pluginRepositories>
  <pluginRepository>
    <id>sonatype-snapshot</id>
    <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </pluginRepository>
</pluginRepositories>
```


## 2. Dependency conflicts
If you have package dependency conflict issues, such as jackson, joda-time, or [jsr311-api](https://github.com/kongchen/swagger-maven-plugin/issues/81).
Run

```
mvn dependency:tree
```

to check which package introduces the one conflicts with yours, and then you can use `<exclusion>` configuration in pom.xml to exclude it.

**Here's an example:**

To exclude `javax.ws.rs:jsr311-api:jar:1.1.1:compile` from `swagger-jaxrs_2.10`:

```xml
<dependency>
    <groupId>com.wordnik</groupId>
    <artifactId>swagger-jaxrs_2.10</artifactId>
    <version>1.3.2</version>
    <exclusions>
        <exclusion>
            <groupId>javax.ws.rs</groupId>
            <artifactId>jsr311-api</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```


## 3. Building from source
To build from source and run tests, you should:
```
mvn install
```
