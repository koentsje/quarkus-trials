# Generate Panache Entities Maven Plugin

This [Maven](http://maven.apache.org/) plugin allows you to generate Panache entities from an existing databaase.

## Usage

An example `pom.xml` file is found below.

```
<project 
    xmlns="http://maven.apache.org/POM/4.0.0" 
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>
  <groupId>org.foo</groupId>
  <artifactId>bar</artifactId>
  <version>0.0.1-SNAPSHOT</version>

  <build>
    <plugins>   
      <plugin>
        <groupId>io.quarkus</groupId>
        <artifactId>panache-maven-plugin</artifactId>
        <version>999-SNAPSHOT</version>
         <executions>
          <execution>
            <id>Generate Entities</id>
            <phase>generate-sources</phase>
            <goals>
              <goal>generate-panache-entities</goal>
            </goals>
           </execution>
        </executions>
      </plugin>
    </plugins>    
  </build>

</project>
```

Issuing `mvn generate-sources` in the root of the project produces the following output:
```
foo@foo bar % mvn  generate-sources 
[INFO] Scanning for projects...
[INFO] 
[INFO] ----------------------------< org.foo:bar >-----------------------------
[INFO] Building bar 0.0.1-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[INFO] 
[INFO] --- panache-maven-plugin:999-SNAPSHOT:generate-panache-entities (Generate Entities) @ bar ---
*** Generating Panache Entities ***
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  0.312 s
[INFO] Finished at: 2020-03-13T15:05:57+01:00
[INFO] ------------------------------------------------------------------------
foo@foo bar %
```
