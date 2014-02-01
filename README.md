# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin helps you **generate API documents** in build phase according to [customized output templates](https://github.com/kongchen/api-doc-template).

[Versions](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md)
==
This plugin has 2 serials of versions:
- 1.x.x : For [Swagger core version 1.2.x](https://github.com/wordnik/swagger-core/wiki/Changelog#v125-jun-19-2013)
> **Latest version `1.1.3-SNAPSHOT` is available in central repository.**

- 2.x.x : For [Swagger core version >= 1.3.0] (https://github.com/wordnik/swagger-core/wiki/Changelog#v130-aug-12-2013)
> **Latest version `2.0-SNAPSHOT` is available in central repository.**

See [change log](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md) for more details.

>To use SNAPSHOT version, you should add plugin repository first:

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
                <version>2.0-SNAPSHOT</version>
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
                            <!--useOutputFlatStructure>false</useOutputFlatStructure-->
                            <!--mustacheFileRoot>${basedir}/src/main/resources/</mustacheFileRoot-->
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
- ```outputTemplate``` is the path of the mustache template file.

 >It supports a remote path such as https://raw.github.com/kongchen/api-doc-template/master/v2.0/markdown.mustache
 but local file is highly recommanded because:
 
    > 1. You can modify the template to match your requirement easily.
    > 2. Mustache can use `>localfile` for mustache partials, but you should put the partials in `mustacheFileRoot` if any.

     >E.g: The template https://raw.github.com/kongchen/api-doc-template/master/v2.0/strapdown.html.mustache uses
     [`markdown.mustache`](https://raw.github.com/kongchen/api-doc-template/master/v2.0/markdown.mustache) as a partial by this way,
     to use `strapdown.html.mustache` you should put `markdown.mustache` in your local path and tell the path to plugin via `mustacheFileRoot`.
- ```outputPath``` is the path of your output file, not existed parent directory of the file will be created.
- If ```swaggerDirectory``` is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.
  - ```useOutputFlatStructure``` indicates whether swagger output will be created in subdirs by path defined in @com.wordnik.swagger.annotations.Api#value (false), or the filename will be the path with replaced slashes to underscores (true). Default: true

You can specify several ```apiSources``` with different api versions and base paths.

# About the template file
There's a [standalone project](https://github.com/kongchen/api-doc-template) for the template files, see more details there and welcome to pull.


# A Sample
Check out this [sample project](https://github.com/kongchen/swagger-maven-example) to see how this happens.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8e57158a366298512499affc8b585976 "githalytics.com")](http://githalytics.com/kongchen/swagger-maven-plugin)

