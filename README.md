# Swagger Maven Plugin [![Build Status](https://travis-ci.org/kongchen/swagger-maven-plugin.png)](https://travis-ci.org/kongchen/swagger-maven-plugin)
This plugin can let your Swagger annotated project generate **Swagger JSON** and your **customized API documents** in build phase.

[**Swagger JSON**](http://petstore.swagger.wordnik.com/v2/swagger.json) is a json file which represents your APIs fully follows [Swagger Spec 2.0](https://github.com/swagger-api/swagger-spec/blob/master/versions/2.0.md).

**Customized API document** looks like this:

<img src="https://cloud.githubusercontent.com/assets/1485800/4130419/c121d19c-3336-11e4-921f-ca8207ed9053.png" width="35%"/>
<img src="https://cloud.githubusercontent.com/assets/1485800/4130438/28359ea4-3337-11e4-8c1a-5d9b06854e3f.png" width="35%"/>


# Example
There's a [sample here](https://github.com/kongchen/swagger-maven-example), just fork it and have a try.

## Configurations for Swagger JSON

```xml
<project>
...
<build>
<plugins>
<plugin>
    <groupId>com.github.kongchen</groupId>
    <artifactId>swagger-maven-plugin</artifactId>
    <version>3.0-SNAPSHOT</version>
    <configuration>
        <apiSources>
            <apiSource>
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

If you cannot wait to try out the plugin, here's a [sample project](https://github.com/kongchen/swagger-maven-example), go to see how it happens.

You can specify several ```apiSources```. Generally, one is enough.

One ```apiSource``` can be considered as a set of APIs. Here's the required parameters for an ```apiSource```:

## Required parameters

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `locations` | Java classes containing Swagger's annotation ```@Api```, or Java packages containing those classes can be configured here, using ```;``` as the delimiter. |
| `info` | The basic information of the api, the field `version` and `title` are required. |
| `schemes` | The supported schemes of the api source. |
| `host` | The hostname and port of the api service. |
| `basePath` | The base path of this api source. |

## Optional parameters

| **name** | **description** |
|------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `templatePath` | The path of a handlebars template file, see more details below.|
| `outputPath` | The path of the output document, not existed parent directories will be created. If you don't want to generate html api just don't set it. |
| `swaggerDirectory` | The directory of generated Swagger JSON files. If null, no Swagger JSON will be generated. The final output swagger json would be `${swaggerDirectory}/swagger.json` |

# About the template file

You need to specify a [handlebars](https://github.com/jknack/handlebars.java) template file in ```templatePath```.
The value for ```templatePath``` supports 2 kinds of path:
1. Resource in classpath. You should specify a resource path with a **classpath:** prefix.
    e.g: "classpath:/markdown.hbs", "classpath:/templates/hello.html"
1. Local file path.
    e.g: "${basedir}/src/main/resources/markdown.hbs", "${basedir}/src/main/resources/template/hello.html"

To display swagger.json correctly, there're 2 new helpers added in. For more details see the template files.


There's a [standalone project](https://github.com/kongchen/api-doc-template) for the template files, see more details there and welcome to send pull request.


[Versions](https://github.com/kongchen/swagger-maven-plugin/blob/master/CHANGES.md)
==

This plugin has 3 serials of versions:

- 3.x.x : For [Swagger spec 2.0](https://github.com/wordnik/swagger-spec/blob/master/versions/2.0.md)
> **Latest version `3.0.0-SNAPSHOT`**

- 2.x.x : For [swagger-spec 1.2](https://github.com/wordnik/swagger-spec/blob/master/versions/1.2.md)
> **Latest version `2.3.1` is available in central repository.**
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
