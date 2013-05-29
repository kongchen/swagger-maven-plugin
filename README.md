# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin helps you generate API documents in build phase according to customized output templates.

Check out this [demo](https://github.com/kongchen/swagger-maven-plugin/wiki/Document-Output-Samples) to see how this happens.

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
                <version>1.1-SNAPSHOT</version>
                <configuration>
                    <apiSources>com.foo.bar.api;org.foo.bar</apiSources>
                    <apiVersion>v1</apiVersion>
                    <basePath>http://www.example.com/foo/bar/</basePath>
                    <outputTemplate>strapdown.html.mustache</outputTemplate>
                    <outputPath>strapdown.html</outputPath>
                    <swaggerDirectory>${project.build.outputDirectory}/apidocs/</swaggerDirectory>
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
    <pluginRepositories>
        <pluginRepository>
            <id>com.github.kongchen</id>
            <url>https://oss.sonatype.org/content/repositories/snapshots/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </pluginRepository>
    </pluginRepositories>
</project>
```


> - A Java Class which contains Swagger's annotation @Api will be considered as a generatable resource.
- A package containing *resource*s OR a *resource* Class is called as a **apiSource**
- You can specify several apiSources in **apiSources**, using ';' as delimiter
- If the **apiSources** in configuration is omitted, the plugin will search all *resource*s in your classpath.
- **outputTemplate** is the path of the mustache template file.
- **outputPath** is the path of your output file.
- If **swaggerDirectory** is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.

# About the template file
Don't worry about the template file, see [this page](https://github.com/kongchen/swagger-maven-plugin/wiki/Document-Templates) for more details.
[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8e57158a366298512499affc8b585976 "githalytics.com")](http://githalytics.com/kongchen/swagger-maven-plugin)
