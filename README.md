# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)

This plugin can let your Swagger annotated project generate **Swagger JSON** and your **Customized Static Documents** in maven build phase.

# Features

* Support [Swagger Spec 2.0](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md)
* Support [SpringMvc](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html) & [JAX-RS](https://jax-rs-spec.java.net/)
* Quickly generates *[swagger.json](https://github.com/kongchen/swagger-maven-example/blob/master/generated/swagger-ui/swagger.json)* and [static document](http://htmlpreview.github.io/?https://raw.github.com/kongchen/swagger-maven-example/master/generated/document.html) by `mvn compile`
* Use [Handlebars](http://handlebarsjs.com/) as template to customize the static document.

# Versions
- [3.1.0](https://github.com/kongchen/swagger-maven-plugin/) supports Swagger Spec [2.0](https://github
.com/swagger-api/swagger-spec/blob/master/versions/2.0.md), support JAX-RS & SpingMVC. (**ACTIVE!**) *with new `swagger.io` namespace jars depedency*
- [3.0.1](https://github.com/kongchen/swagger-maven-plugin/tree/swagger-core_com.wordnik_namespaces/) supports Swagger Spec [2.0](https://github
.com/swagger-api/swagger-spec/blob/master/versions/2.0.md), support JAX-RS & SpingMVC. (**ACTIVE!**) *with old `com.wordnik` namespace jars depedency*
- [2.3.4](https://github.com/kongchen/swagger-maven-plugin/tree/spec1.2) supports Swagger Spec [1.2](https://github.com/swagger-api/swagger-spec/blob/master/versions/1.2.md), support JAX-RS & SpringMVC. (**Lazy maintained**)
- [1.1.1](https://github.com/kongchen/swagger-maven-plugin/tree/1.1.1) supports Swagger Spec 1.1. (**No longer maintained**)


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
		</plugin>
	</plugins>
</build>
```
One `apiSource` can be considered as a version of APIs of your service.

You can specify several `apiSource`s. Generally, one is enough.


# Configuration for `apiSource`

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `springmvc` | Tell the plugin your project is a JAX-RS(`false`) or a SpringMvc(`true`) project | 
| `locations` **required**| Classes containing Swagger's annotation ```@Api```, or packages containing those classes can be configured here, using ```;``` as the delimiter. |
| `schemes` | The transfer protocol of the API. Values MUST be from the list: `"http"`, `"https"`, `"ws"`, `"wss"`, using ```,``` as the delimiter.|
| `host` | The host (name or ip) serving the API. This MUST be the host only and does not include the scheme nor sub-paths. It MAY include a port.  The host does not support [path templating](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#pathTemplating).|
| `basePath` | The base path on which the API is served, which is relative to the host. The value MUST start with a leading slash (/). The basePath does not support [path templating](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#pathTemplating). |
| `info` **required**| The basic information of the api, using same definition as Swagger Spec 2.0's [info Object](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#infoObject) |
| `securityDefinitions` | You can put your [security definitions](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md#security-definitions-object) here, see more details [below](#securityDefinitions)|
| `templatePath` | The path of a [handlebars](http://handlebarsjs.com/) template file, see more details [below](#templatefile).|
| `outputPath` | The path of the generated static document, not existed parent directories will be created. If you don't want to generate a static document, just don't set it. |
| `swaggerDirectory` | The directory of generated `swagger.json` file. If null, no `swagger.json` will be generated. |
| `modelSubstitute` | The model substitute file's path, see more details [below](#modelsubstitute)|
# <a id="templatefile">Template File</a>

You need to specify a [handlebars](https://github.com/jknack/handlebars.java) template file in ```templatePath```.
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

There're 3 types of security definitions according to Swagger Spec: `basic`, `apiKey` and `oauth2`.

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
# <a id="modelSubstitute">Model Substitute</a>
Model substitute file is a simple text file with multiple lines, each line tells the plugin to substitutes a model class with the supplied substitute, and the 2 classes should be seperated by a `:`. 

e.g The line:

```
com.foo.bar.PetName : java.lang.String
```

would tell the plugin substitute `com.foo.bar.PetName` with `java.lang.String`, 
and the generated `swagger.json` will become:

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
instead of :

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

Model substitute file will be read by `getClass().getResourceAsStream`, so please note the path you configured. 

# Example
There's a [sample here](https://github.com/kongchen/swagger-maven-example), just fork it and have a try.

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
                <locations>com.wordnik.swagger.sample</locations>
                <schemes>http,https</schemes>
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
...
</plugins>
</build>
</project>
```

# FAQ

## 1. SNAPSHOT Version
SNAPSHOT versions are available for verify issues, new features. If you would like to try to verify the fixed issues or the new added features, you may need to add `pluginRepository` in your `pom.xml`:

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


## 2. Dependency conflict
If you have package depedency conflict issues, such as jackson, joda-time, or [jsr311-api](https://github.com/kongchen/swagger-maven-plugin/issues/81). 
Run

```
mvn dependency:tree
```

to check which package introduces the one conflicts with yours, and then you can use `<exclusion>` conficuration in pom.xml to exlcude it.

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

Developed with ![IntelliJ IDEA](https://www.jetbrains.com/idea/docs/logo_intellij_idea.png)
