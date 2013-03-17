# Why this plugin

First of all, Swagger is awesome, thanks to the team!

However, a limitation in using Swagger is that you cannot get your API document unless you:

1. write documents with Swagger style
* configure servlet
* build your project
* deploy your project on server
* start the server
* call to the servlet to let Swagger listing your APIs.

Another thing is you can get beautiful document by Swagger-ui, but you cannot get the format you need easily. e.g. A simple single HTML page.

This plugin will resolve these problems.
It supports customized output template (mustache) and will let you generate the document in build phase, in another word, you'll get the API document by following steps:

1. write documents with Swagger style
* configure this plugin
* build (mvn package)

You'll always get your up to date document after you launch _mvn package_. You can easily put the generated document anywhere. (put in service war package using maven-assembly-plugin.)

# Tutorials
## write Swagger-style document

## configure pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>…</groupId>
    <artifactId>…</artifactId>
    <version>…</version>

    <dependencies>
        …
      <dependency>
        <groupId>com.wordnik</groupId>
        <artifactId>swagger-jaxrs_2.9.1</artifactId>
        <version>1.2.0</version>
      </dependency>
        …
    </dependencies>

    <build>
        <plugins>
            …
          <plugin>
            <groupId>com.github.kongchen</groupId>
            <artifactId>swagger-maven-plugin</artifactId>
            <version>1.0-SNAPSHOT</version>
            <configuration>
                <apiSources>
                    <!--can speficy multi sources here to get multi format outputs-->
                    <apiSource>
                        <apiClasses>
                            <apiClass>com.foo.bar.ApiResource1Version1</apiClass>
                            <apiClass>com.foo.bar.ApiResource2Version1</apiClass>
                        </apiClasses>
                        <apiPackage>com.foo.bar.api</apiPackage>
                        <apiVersion>v1</apiVersion>
                        <basePath>http://host:port/foo/bar</basePath>
                        <outputTemplate>markdown.mustache</outputTemplate>
                        <outputPath>doc.md</outputPath>
                    </apiSource>
                    <apiSource>
                        <apiPackage>com.foo.bar.api.v2</apiPackage>
                        <apiVersion>v2</apiVersion>
                        <basePath>http://host:port/bar/foo</basePath>
                        <outputTemplate>html.mustache</outputTemplate>
                        <outputPath>index.html</outputPath>
                    </apiSource>
                </apiSources>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>generate</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
            …
        </plugins>
    </build>
</xml>
```


> - A Java Class which contains Swagger's annotation @Api will be considered as a apiClass.
- You can specify several apiClass or a apiPackage which contains several apiClass to an apiSource.
- One apiSource will be considered as a set of APIs for one apiVersion in basePath.
- You can specify several apiSource to the plugin. For example:
 - You can generate documents for your mutil-version supported service.
 - Or you can generate several formats of output for one version of your API.
- outputTemplate is the path of the mustache template file.
- outputPath is the path for the output file.

Don't worry about the template file, the plugin has embed 3 templates in:

1. wiki.mustache for wiki markup output.
2. html.mustache for html output.
3. markdown.mustache for markdown output.

If you dissatisfied with the included mustache template, you can write your own, just follow the following json-schema of the hash your mustache template file will consume. It looks big but actually simple:
```json

```