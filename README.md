# NTR Parser

A Java library for parsing NTR files, a hierarchical key-value file format.

## Features

- Parse NTR files from files, streams, or strings
- Access values using dot notation paths
- Build NTR data structures programmatically
- Write NTR data to files, streams, or strings
- Comprehensive error handling
- Well-documented API
- Fully tested

## Installation

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.Fedox-die-Ente</groupId>
        <artifactId>ntr-java</artifactId>
        <version>v1.0.0</version>
    </dependency>
</dependencies>
```

### Gradle

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Fedox-die-Ente:ntr-java:v1.0.0'
}
```

## Usage

### Parsing NTR Files

```java
import ovh.fedox.ntr.NTRParser;
import ovh.fedox.ntr.exception.NTRParseException;

// Parse from a file
NTRParser parser = new NTRParser();
try {
    parser.parseFile("config.ntr");
    
    // Get a value
    String title = parser.getValue("welcome.title").orElse("Default Title");
    System.out.println("Title: " + title);
} catch (NTRParseException | IOException e) {
    e.printStackTrace();
}

// Parse from a string
String content = "welcome\n  title>Hello\n  message>Welcome!";
try {
    parser.parseString(content);
} catch (NTRParseException e) {
    e.printStackTrace();
}
```

### Building NTR Data

```java
import ovh.fedox.ntr.NTRBuilder;
import ovh.fedox.ntr.NTRWriter;
import ovh.fedox.ntr.exception.NTRWriteException;

// Build NTR data programmatically
NTRBuilder builder = new NTRBuilder();
builder.addRoot("config")
       .addChild("database")
       .addChild("url", "jdbc:mysql://localhost:3306/mydb")
       .addChild("username", "user")
       .addChild("password", "pass")
       .navigateTo("database")
       .addChild("pool")
       .addChild("min", "5")
       .addChild("max", "20");

// Write to a file
try {
    NTRWriter writer = builder.createWriter();
    writer.setComment("Configuration File");
    writer.writeToFile("config.ntr");
} catch (NTRWriteException | IOException e) {
    e.printStackTrace();
}
```

## NTR File Format

NTR is a hierarchical key-value file format with the following features:

- Lines starting with @ are comments
- Hierarchical structure is represented by indentation
- Key-value pairs are separated by >

Example:

```ntr
@Configuration File

config
  database
    url>jdbc:mysql://localhost:3306/mydb
    username>user
    password>pass
    pool
      min>5
      max>20
  app
    name>My App
    version>1.0.0
```

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
```
```