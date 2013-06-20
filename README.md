# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin helps you **generate API documents** in build phase according to [customized output templates](https://github.com/kongchen/swagger-maven-plugin/wiki/Document-Templates).

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
                    <apiSources>
                        <apiSource>
                            <locations>com.foo.bar.apis;com.foo.bar.apis.internal.Resource</locations>
                            <apiVersion>v1</apiVersion>
                            <basePath>http://www.example.com</basePath>
                            <outputTemplate>strapdown.html.mustache</outputTemplate>
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


- One ```apiSource``` can be considered as a set of APIs for one ```apiVersion``` in API's ```basePath```.
- Java classes containing Swagger's annotation ```@Api```, or Java packages containing those classes can be configured in ```locations```, using ```;``` as the delimiter.
- ```outputTemplate``` is the path of the mustache template file.
- ```outputPath``` is the path of your output file.
- If ```swaggerDirectory``` is configured, the plugin will also generate a Swagger resource listing suitable for feeding to swagger-ui.
- ```withFormatSuffix``` indicates if you need Swagger's _.{format}_ suffix in API's path. Default: false

You can specify several ```apiSources``` with different api versions and base paths.

# About the template file
See [this page](https://github.com/kongchen/swagger-maven-plugin/wiki/Document-Templates) for more details.

# A Sample
Check out this [sample project](https://github.com/kongchen/swagger-maven-example) to see how this happens.

[![githalytics.com alpha](https://cruel-carlota.pagodabox.com/8e57158a366298512499affc8b585976 "githalytics.com")](http://githalytics.com/kongchen/swagger-maven-plugin)

