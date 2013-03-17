# Why this plugin

First of all, Swagger is awesome!

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

## Write Swagger-style document
Follow <https://github.com/wordnik/swagger-core/wiki/java-jax-rs>

## Add repository of this plugin in your pom.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    …
    <repositories>
        <repository>
            <id>swagger-maven-plugin-mvn-repo</id>
            <url>https://raw.github.com/kongchen/swagger-maven-plugin/mvn-repo/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>
    …
</project>
```

## configure plugin in pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    …
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
</project>
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

1. [wiki.mustache](https://github.com/kongchen/swagger-maven-plugin/blob/master/src/main/resources/wiki.mustache) for wiki markup output.
2. [html.mustache](https://github.com/kongchen/swagger-maven-plugin/blob/master/src/main/resources/html.mustache) for html output.
3. [markdown.mustache](https://github.com/kongchen/swagger-maven-plugin/blob/master/src/main/resources/markdown.mustache) for markdown output.

If you dissatisfied with the included mustache template, you can write your own, just follow the following json-schema of the hash your mustache template file will consume. It looks big but actually simple:
```json
{
    "type": "object",
    "properties": {
        "basePath": {
            "type": "string"
        },
        "apiVersion": {
            "type": "string"
        },
        "apiDocuments": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "index": {
                        "type": "integer"
                    },
                    "resourcePath": {
                        "type": "string"
                    },
                    "description": {
                        "type": "string"
                    },
                    "apis": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "apiIndex": {
                                    "type": "integer"
                                },
                                "path": {
                                    "type": "string"
                                },
                                "url": {
                                    "type": "string"
                                },
                                "operations": {
                                    "type": "array",
                                    "items": {
                                        "type": "object",
                                        "properties": {
                                            "opIndex": {
                                                "type": "integer"
                                            },
                                            "httpMethod": {
                                                "type": "string"
                                            },
                                            "summary": {
                                                "type": "string"
                                            },
                                            "notes": {
                                                "type": "string"
                                            },
                                            "responseClass": {
                                                "type": "string"
                                            },
                                            "nickname": {
                                                "type": "string"
                                            },
                                            "className": {
                                                "type": "string"
                                            },
                                            "parameters": {
                                                "type": "array",
                                                "items": {
                                                    "type": "object",
                                                    "properties": {
                                                        "paramType": {
                                                            "type": "string"
                                                        },
                                                        "paras": {
                                                            "type": "array",
                                                            "items": {
                                                                "type": "object",
                                                                "properties": {
                                                                    "name": {
                                                                        "type": "string"
                                                                    },
                                                                    "required": {
                                                                        "type": "boolean",
                                                                        "required": true
                                                                    },
                                                                    "description": {
                                                                        "type": "string"
                                                                    },
                                                                    "type": {
                                                                        "type": "string"
                                                                    },
                                                                    "linkType": {
                                                                        "type": "string"
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            },
                                            "errorResponses": {
                                                "type": "array",
                                                "items": {
                                                    "type": "object",
                                                    "properties": {
                                                        "code": {
                                                            "type": "integer"
                                                        },
                                                        "reason": {
                                                            "type": "string"
                                                        }
                                                    }
                                                }
                                            },
                                            "responseClassLinkType": {
                                                "type": "string"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        "dataTypes": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "name": {
                        "type": "string"
                    },
                    "items": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "name": {
                                    "type": "string"
                                },
                                "type": {
                                    "type": "string"
                                },
                                "linkType": {
                                    "type": "string"
                                },
                                "required": {
                                    "type": "boolean",
                                    "required": true
                                },
                                "access": {
                                    "type": "string"
                                },
                                "description": {
                                    "type": "string"
                                },
                                "notes": {
                                    "type": "string"
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```