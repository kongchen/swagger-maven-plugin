# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin can let your Swagger annotated project generate **Swagger JSON** and your **customized API documents** in build phase.

You must already known what is Swagger JSON, check the results of this plugin generates [here](https://github.com/kongchen/swagger-maven-example/tree/master/generated/swagger-ui) to see if it is what you want.

What is *customized API document*? Here're two screenshots:
<img src="https://cloud.githubusercontent.com/assets/1485800/4130419/c121d19c-3336-11e4-921f-ca8207ed9053.png" width="50%"/>
<img src="https://cloud.githubusercontent.com/assets/1485800/4130438/28359ea4-3337-11e4-8c1a-5d9b06854e3f.png" width="50%"/>


# Usage
There's a [sample here](https://github.com/kongchen/swagger-maven-example), fork it and have a try.

## Minimal Configuartion for Swagger JSON

```xml
<project>
...
<build>
<plugins>
<plugin>
  <groupId>com.github.kongchen</groupId>
  <artifactId>swagger-maven-plugin</artifactId>
  <version>2.3</version>
  <configuration>
    <apiSources>
      <apiSource>
        <locations>sample.api</locations>
        <apiVersion>1.0</apiVersion>
        <basePath>http://example.com</basePath>
        <swaggerDirectory>generated/swagger-ui</swaggerDirectory>
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

### Minimal Configuration for customized API Document

```xml
<project>
...
<build>
<plugins>
<plugin>
  <groupId>com.github.kongchen</groupId>
  <artifactId>swagger-maven-plugin</artifactId>
  <version>2.3</version>
  <configuration>
    <apiSources>
      <apiSource>
        <locations>sample.api</locations>
        <apiVersion>1.0</apiVersion>
        <basePath>http://example.com</basePath>
        <outputTemplate>/markdown.mustache</outputTemplate>
        <mustacheFileRoot>${basedir}/src/main/resources/</mustacheFileRoot>
        <outputPath>${basedir}/generated/document.html</outputPath>
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

## Minimal Configuration for Swagger JSON & customized API Document

Just merge the above 2 configuartions together.


## Compelete Configuration

The compelete configuration for the plugin lists below, you'll see some advanced configurations there.

If you cannot wait to try out the plugin, here's a [sample project](https://github.com/kongchen/swagger-maven-example), go to see how it happens.

```xml
<project>
...
<build>
<plugins>
<plugin>
  <groupId>com.github.kongchen</groupId>
  <artifactId>swagger-maven-plugin</artifactId>
  <version>2.3</version>
  <configuration>
    <apiSources>
      <apiSource>
<!--Required parameters BEGIN-->
        <locations>sample.api</locations>
        <apiVersion>1.0</apiVersion>
        <basePath>http://example.com</basePath>
<!--Required parameters END-->

<!--Optional parameters BEGIN-->
        <!---General parameters BEGIN-->
        <apiInfo>
          <title>Swagger Maven Plugin Sample</title>
          <description>Hellow world!</description>
          <termsOfServiceUrl>http://www.github.com/kongchen/swagger-maven-plugin</termsOfServiceUrl>
          <contact>kongchen#gmail$com</contact>
          <license>Apache 2.0</license>
          <licenseUrl>http://www.apache.org/licenses/LICENSE-2.0.html</licenseUrl>
        </apiInfo>
        <apiSortComparator>com.foo.bar.YourApiComarator</apiSortComparator>
        <overridingModels>/swagger-overriding-models.json</overridingModels>
        <swaggerInternalFilter>com.wordnik.swagger.config.DefaultSpecFilter</swaggerInternalFilter>
        <swaggerApiReader>com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader</swaggerApiReader>
        <!---General parameters END-->

        <!---Document generation parameters BEGIN-->
        <outputTemplate>
          https://raw.github.com/kongchen/api-doc-template/master/v2.0/strapdown.html.mustache
        </outputTemplate>
        <mustacheFileRoot>${basedir}/src/main/resources/</mustacheFileRoot>
        <outputPath>${basedir}/generated/document.html</outputPath>
        <!---Document generation parameters END-->

        <!---Swagger JSON parameters BEGIN-->
        <swaggerDirectory>generated/swagger-ui</swaggerDirectory>
        <swaggerUIDocBasePath>http://www.example.com/restapi/doc</swaggerUIDocBasePath>
        <useOutputFlatStructure>false</useOutputFlatStructure>
        <!---Swagger JSON parameters END-->
<!--Optional parameters END-->
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

One ```apiSource``` can be considered as a set of APIs for one ```apiVersion``` in API's ```basePath```, here's the parameter list of `apiSource`:

## Required parameters
| name | description |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `locations` | Java classes containing Swagger's annotation ```@Api```, or Java packages containing those classes can be configured here, using ```;``` as the delimiter. |
| `apiVersion` | The version of the api source. |
| `basePath` | The base path of this api source. |

## Optional parameters
### General parameters

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apiInfo` | Some information of the API document. |
| `overridingModels` | The name of *overridingModels* file, see more details in sections below. |
| `swaggerInternalFilter` | If not null, the value should be full name of class implementing `com.wordnik.swagger.core.filter.SpecFilter`. This allows you to filter both methods and parameters from generated api. |
| `swaggerApiReader` | If not null, the value should be full name of class implementing `com.wordnik.swagger.reader.ClassReader`. This allows you flexibly implement/override the reader's implementation. Default is `com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader` |


The parameters of `apiInfo`:

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `title` | The title of your API document. |
| `description` | The brief introduction of your API document. |
| `termsOfServiceUrl` | The URL of your API's terms of service. |
| `contact` | should be an email here. |
| `license` | Your API's license. |
| `licenseUrl` | The license's URL. |



### Document generation parameters

| **name**| **description** |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `apiSortComparator` | You can specify an comparator to sort your apis in output document. The class should implements `Comparator<MustacheApi>`. Default is null which means there the order of your apis will be as same as the `swaggerApiReader`.|
| `outputTemplate` | The path of a mustache template file, see more details in sections below.|
| `mustacheFileRoot` | The root path of your mustache template file. |
| `outputPath` | The path of generate-by-template document, not existed parent directories will be created. If you don't want to generate html api just don't set it. |


### Swager JSON parameters


| **name**| **description** |
|-----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `swaggerDirectory` | The directory of generated Swagger JSON files. If null, no Swagger JSON will be generated. |
| `useOutputFlatStructure` | Indicates whether Swagger JSON will be created in subdirs by path defined in @com.wordnik.swagger.annotations.Api#value (false), or the filename will be the path with replaced slashes to underscores (true). Default: `true` |
| `swaggerUIDocBasePath` | Generally, the `baseUrl` in Swagger JSON's `service.json` is always as same as `basePath` you specified above. However, you can use this parameter to overwrite it. |


You can specify several ```apiSources``` with different api versions and base paths.

# About the template file

You need to specify a mustache template file in ```outputTemplate```.

It supports a remote path such as https://raw.github.com/kongchen/api-doc-template/master/v2.0/markdown.mustache but local file is highly recommanded because:

1. You can modify the template to match your requirement easily.
1. Mustache can use `>localfile` for mustache partials, but you should put the partials in `mustacheFileRoot` if any.

>E.g: 
The template https://raw.github.com/kongchen/api-doc-template/master/v2.0/strapdown.html.mustache uses
     [`markdown.mustache`](https://raw.github.com/kongchen/api-doc-template/master/v2.0/markdown.mustache) as a partial by this way,
     to use `strapdown.html.mustache` you should put `markdown.mustache` in your local path and tell the path to plugin via `mustacheFileRoot`.

There's a [standalone project](https://github.com/kongchen/api-doc-template) for the template files, see more details there and welcome to send pull request.

# About the overridingModels file

```overridingModels``` is the name of *overridingModels* file.

It will be loaded with [Class#getResourceAsStream](http://docs.oracle.com/javase/7/docs/api/java/lang/Class.html#getResourceAsStream(java.lang.String)).

Example file below. Note that you can name every mapping the way you want - it's not used for anything. *className* and *jsonString* are used as described [here](https://github.com/wordnik/swagger-core/wiki/overriding-models#in-java-with-swagger-core-version-13x) to create *OverrideConverter* and add it to the *ModelConverters*. 
```json
{
	"DateTimeMapping" :
	{
		"className" : "org.joda.time.DateTime",
		"jsonString" : "{\"id\": \"DateTime\",\"properties\": {\"value\": {\"required\": true, \"description\": \"Date in ISO-8601 format\", \"notes\": \"Add any notes you like here\", \"type\": \"string\", \"format\": \"date-time\"}}}"
	},
	"DateMidnightMapping" :
	{
		"className" : "org.joda.time.DateMidnight",
		"jsonString" : "{\"id\": \"DateTime\",\"properties\": {\"value\": {\"required\": true, \"description\": \"Date in ISO-8601 format\", \"notes\": \"Add any notes you like here\", \"type\": \"string\", \"format\": \"date-time\"}}}"
	}
}
```

[Versions](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md)
==

This plugin has 2 serials of versions:

- 2.x.x : For [Swagger core version >= 1.3.0] (https://github.com/wordnik/swagger-core/wiki/Changelog#v130-aug-12-2013) [swagger-spec 1.2](https://github.com/wordnik/swagger-spec/blob/master/versions/1.2.md)
> **Latest version `2.3` is available in central repository.**
`2.3.1-SNAPSHOT` is the latest SNAPSHOT version.

- 1.x.x : For [Swagger core version 1.2.x](https://github.com/wordnik/swagger-core/wiki/Changelog#v125-jun-19-2013) swagger-spec 1.1
> **Latest version `1.1.3-SNAPSHOT` is available in sonatype repository.**

> To use SNAPSHOT version, you need to add plugin repository in your pom.xml first:

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

# FAQ
## Dependency conflict
If you have package depedency conflict issues, such as jackson, joda-time, or [jsr311-api](https://github.com/kongchen/swagger-maven-plugin/issues/81). Run `mvn dependency:tree` to check which package introduces the one conflicts with yours and exclude it using `<exclusion/>` in pom.xml.
> e.g. exclude `javax.ws.rs:jsr311-api:jar:1.1.1:compile` from `swagger-jaxrs_2.10`:
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
