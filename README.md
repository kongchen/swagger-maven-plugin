# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin helps you **generate API documents** in build phase according to [customized output templates](https://github.com/kongchen/api-doc-template).

[Changes](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md)
==
Latest version `1.1.1` is availabe in central repository

# Usage

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    …
    <dependencies>
        …
    </dependencies>
    <build>
        <plugins>
            ...
            <plugin>
                <groupId>com.github.kongchen</groupId>
                <artifactId>swagger-maven-plugin</artifactId>
                <version>1.1.1</version>
                <configuration>
                    <apiSources>
                        <apiSource>
                            <locations>com.foo.bar.apis;com.foo.bar.apis.internal.Resource</locations>
                            <apiVersion>v1</apiVersion>
                            <basePath>http://www.example.com</basePath>
                            <useOutputFlatStructure>false</useOutputFlatStructure>
                            <mustacheFileRoot>${basedir}/src/main/resources/</mustacheFileRoot>
                            <outputTemplate>
                                     https://raw.github.com/kongchen/api-doc-template/master/v1.1/markdown.mustache
                            </outputTemplate>
                            <outputPath>generated/strapdown.html</outputPath>
                            <withFormatSuffix>false</withFormatSuffix>
                            <swaggerDirectory>generated/apidocs</swaggerDirectory>
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

 >It supports a remote path such as https://raw.github.com/kongchen/api-doc-template/master/v1.1/markdown.mustache
 but local file is highly recommanded because:
 
    > 1. You can modify the template to match your requirement easily.
    > 2. Mustache can use `>localfile` to inculde a local file.

     >E.g: The template https://raw.github.com/kongchen/api-doc-template/master/v1.1/strapdown.html.mustache includes
     [`markdown.mustache`](https://raw.github.com/kongchen/api-doc-template/master/v1.1/markdown.mustache) by this way,
     to use `strapdown.html.mustache` you should put `markdown.mustache` in your local path first.
- ```outputPath``` is the path of your output file, not existed parent directory of the file will be created.
- If ```swaggerDirectory``` is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.
- ```withFormatSuffix``` indicates if you need Swagger's _.{format}_ suffix in API's path. Default: false
- ```useOutputFlatStructure``` indicates whether output will be created in subdirs by path defined in @com.wordnik.swagger.annotations.Api#value (false), or the filename will be the path with replaced slashes to underscores (true). Default: true
- ```mustacheFileRoot``` directory where mustache is looking for markdown.mustache

You can specify several ```apiSources``` with different api versions and base paths.

# About the template file
There's a [standalone project](https://github.com/kongchen/api-doc-template) for the template files, see more details there and welcome to pull.


# A Sample
Check out this [sample project](https://github.com/kongchen/swagger-maven-example) to see how this happens.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8e57158a366298512499affc8b585976 "githalytics.com")](http://githalytics.com/kongchen/swagger-maven-plugin)

