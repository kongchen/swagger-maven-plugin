# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin helps you **generate API documents** in build phase according to [customized output templates](https://github.com/kongchen/api-doc-template).

# Usage

```xml
<project>
  <build>
    <plugins>
      <plugin>
        <groupId>com.github.kongchen</groupId>
        <artifactId>swagger-maven-plugin</artifactId>
        <version>2.3-SNAPSHOT</version>
        <configuration>
          <apiSources>
            <apiSource>
              <locations>sample.api</locations>
              <apiVersion>1.0</apiVersion>
              <basePath>http://example.com</basePath>
              <apiInfo>
                <title>Swagger Maven Plugin Sample</title>
                <description>
                  This is a sample of [swagger-maven-plugin](http://www.github.com/kongchen/swagger-maven-plugin).
                  The plugin helps you generate **Swagger JSON** and **customized API document** in build phase.
                </description>
                <termsOfServiceUrl>http://www.github.com/kongchen/swagger-maven-plugin</termsOfServiceUrl>
                <contact>kongchen#gmail$com</contact>
                <license>Apache 2.0</license>
                <licenseUrl>http://www.apache.org/licenses/LICENSE-2.0.html</licenseUrl>
              </apiInfo>
              <outputTemplate>
                https://raw.github.com/kongchen/api-doc-template/master/v2.0/strapdown.html.mustache
              </outputTemplate>
              <outputPath>${basedir}/generated/document.html</outputPath>
              <swaggerDirectory>generated/swagger-ui</swaggerDirectory>
              <swaggerUIDocBasePath>http://www.example.com/restapi/doc</swaggerUIDocBasePath>
              <useOutputFlatStructure>false</useOutputFlatStructure>
              <mustacheFileRoot>${basedir}/src/main/resources/</mustacheFileRoot>
              <overridingModels>/swagger-overriding-models.json</overridingModels>
              <swaggerInternalFilter>com.wordnik.swagger.config.DefaultSpecFilter</swaggerInternalFilter>
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
</project>
```

- One ```apiSource``` can be considered as a set of APIs for one ```apiVersion``` in API's ```basePath```, and you can define your api's information in ```<apiInfo>```.
- Java classes containing Swagger's annotation ```@Api```, or Java packages containing those classes can be configured in ```locations```, using ```;``` as the delimiter.
- ```outputTemplate``` is the path of a mustache template file, see more details in next section. If you don't want to generate html api just don't set it.
- ```mustacheFileRoot``` is the root path of your mustach template file, see more details in next section.
- ```outputPath``` is the path of your output file, not existed parent directories will be created. If you don't want to generate html api just don't set it.
- If ```swaggerDirectory``` is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.
  - ```useOutputFlatStructure``` indicates whether swagger output will be created in subdirs by path defined in @com.wordnik.swagger.annotations.Api#value (false), or the filename will be the path with replaced slashes to underscores (true). Default: true
  - Generally, the `baseUrl` in `service.json` is always as same as `<basePath>` you specified. However, you can use ```swaggerUIDocBasePath``` to overwrite it.
- ```overridingModels``` is the name of *overridingModels* file, see more details in next section.
- ```swaggerInternalFilter``` should be full name of class implementing `com.wordnik.swagger.core.filter.SpecFilter`. This allows 
you to filter both methods and parameters from generated api. 

You can specify several ```apiSources``` with different api versions and base paths.

# About the template file

```outputTemplate``` is the path of a mustache template file.

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

- 2.x.x : For [Swagger core version >= 1.3.0] (https://github.com/wordnik/swagger-core/wiki/Changelog#v130-aug-12-2013) swagger-spec 1.2
> **Latest version `2.2` is available in central repository.**
`2.3-SNAPSHOT` is the latest SNAPSHOT version.

- 1.x.x : For [Swagger core version 1.2.x](https://github.com/wordnik/swagger-core/wiki/Changelog#v125-jun-19-2013) swagger-spec 1.1
> **Latest version `1.1.3-SNAPSHOT` is available in sonatype repository.**

> To use SNAPSHOT version, you should add plugin repository in your pom.xml first:

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

# A Sample
Check out this [sample project](https://github.com/kongchen/swagger-maven-example) to see how this happens.

# Dependency conflict
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
