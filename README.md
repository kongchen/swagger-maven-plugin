# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin helps you **generate API documents** in build phase according to [customized output templates](https://github.com/kongchen/api-doc-template).

[Versions](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md)
==
This plugin has 2 serials of versions:

- 2.x.x : For [Swagger core version >= 1.3.0] (https://github.com/wordnik/swagger-core/wiki/Changelog#v130-aug-12-2013) swagger-spec 1.2
> **Latest version `2.2` is available in central repository.**
`2.3-SNAPSHOT` is the latest SNAPSHOT version.

- 1.x.x : For [Swagger core version 1.2.x](https://github.com/wordnik/swagger-core/wiki/Changelog#v125-jun-19-2013) swagger-spec 1.1
> **Latest version `1.1.3-SNAPSHOT` is available in sonatype repository.**


See [change log](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md) for more details.

> To use SNAPSHOT version, you should add plugin repository first:

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

# Usage

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    …
    <dependencies>
        ...
        <dependency>
            <groupId>com.wordnik</groupId>
            <artifactId>swagger-jaxrs_2.10</artifactId>
            <version>1.3.2</version>
        </dependency>
        ...
    </dependencies>
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.github.kongchen</groupId>
                <artifactId>swagger-maven-plugin</artifactId>
                <version>2.0</version>
                <configuration>
                    <apiSources>
                        <apiSource>
                            <locations>com.foo.bar.apis;com.foo.bar.apis.internal.Resource</locations>
                            <apiVersion>v1</apiVersion>
                            <basePath>http://www.example.com</basePath>
                            <outputTemplate>
                                     https://raw.github.com/kongchen/api-doc-template/master/v2.0/markdown.mustache
                            </outputTemplate>
                            <outputPath>generated/strapdown.html</outputPath>
                            <!--swaggerDirectory>generated/apidocs</swaggerDirectory-->
                            <!--swaggerUIDocBasePath>http://www.example.com/restapi/doc</swaggerUIDocBasePath-->
                            <!--useOutputFlatStructure>false</useOutputFlatStructure-->
                            <!--mustacheFileRoot>${basedir}/src/main/resources/</mustacheFileRoot-->
                            <!--overridingModels>/swagger-overriding-models.json</overridingModels-->
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
    ...


</project>
```

- One ```apiSource``` can be considered as a set of APIs for one ```apiVersion``` in API's ```basePath```.
- Java classes containing Swagger's annotation ```@Api```, or Java packages containing those classes can be configured in ```locations```, using ```;``` as the delimiter.
- ```outputTemplate``` is the path of a mustache template file, see more details in next section. If you don't want to generate html api just don't set it.
- ```mustacheFileRoot``` is the root path of your mustach template file, see more details in next section.
- ```outputPath``` is the path of your output file, not existed parent directories will be created. If you don't want to generate html api just don't set it.
- If ```swaggerDirectory``` is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.
  - ```useOutputFlatStructure``` indicates whether swagger output will be created in subdirs by path defined in @com.wordnik.swagger.annotations.Api#value (false), or the filename will be the path with replaced slashes to underscores (true). Default: true
  - Generally, the `baseUrl` in `service.json` is always as same as `<basePath>` you specified. However, you can use ```swaggerUIDocBasePath``` to overwrite it.
- ```overridingModels``` is the name of *overridingModels* file, see more details in next section.

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

# A Sample
Check out this [sample project](https://github.com/kongchen/swagger-maven-example) to see how this happens.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8e57158a366298512499affc8b585976 "githalytics.com")](http://githalytics.com/kongchen/swagger-maven-plugin)

