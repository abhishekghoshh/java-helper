## Junit

> Watch this video [JUnit 5 Tutorial - Crash Course](https://www.youtube.com/watch?v=6uSnF6IuWIw) 
> 
> [github](https://github.com/marcobehlerjetbrains/junit5-tutorial)

> Official documentation for JUnit 5 [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
> [AssertJ - fluent assertions java library](https://assertj.github.io/doc/)
> [Jupiter / JUnit 5](https://java.testcontainers.org/test_framework_integration/junit_5/)
> [awaitility/awaitility](https://github.com/awaitility/awaitility)

For JUnit use this dependency strictly
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.junit</groupId>
            <artifactId>junit-bom</artifactId>
            <version>5.12.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
<build>
    <plugins>
        <plugin>
            <artifactId>maven-surefire-plugin</artifactId>
            <version>3.5.2</version>
        </plugin>
        <plugin>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.10.1</version>
        </plugin>
    </plugins>
</build>
```


REF4: Maven Surefire Plugin + Compiler plugin

[Maven surefire plugin](https://maven.apache.org/surefire/maven-surefire-plugin/usage.html)
```xml
<build>
   <pluginManagement>
       <plugins>
           <plugin>
               <artifactId>maven-surefire-plugin</artifactId>
               <version>3.0.0-M6</version>
           </plugin>
           <plugin>
               <artifactId>maven-compiler-plugin</artifactId>
               <version>3.10.1</version>
           </plugin>
       </plugins>
   </pluginManagement>
</build>
```

REF5: AssertJ Homepage

[AssertJ - fluent assertions java library](https://assertj.github.io/doc/)

REF6: JSON Unit - Dependencies
```xml
<dependencies>
   <dependency>
       <groupId>net.javacrumbs.json-unit</groupId>
       <artifactId>json-unit-assertj</artifactId>
       <version>2.33.0</version>
       <scope>test</scope>
   </dependency>

   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-databind</artifactId>
       <version>2.13.2.2</version>
   </dependency>
   <dependency>
       <groupId>com.fasterxml.jackson.datatype</groupId>
       <artifactId>jackson-datatype-jsr310</artifactId>
       <version>2.13.2</version>
   </dependency>
</dependencies>
```


REF7: XML Unit - Dependencies
```xml
<dependencies>
   <dependency>
       <groupId>org.xmlunit</groupId>  <!-- needs proper bytgebuddy-->
       <artifactId>xmlunit-assertj</artifactId>
       <version>2.9.0</version>
       <scope>test</scope>
   </dependency>
</dependencies>
```



REF8: XML Unit - Dependency Management
```xml
<dependencyManagement>
   <dependencies>
       <dependency>
           <groupId>net.bytebuddy</groupId>
           <artifactId>byte-buddy</artifactId>
           <version>1.12.10</version>
       </dependency>

       <dependency>
           <groupId>net.bytebuddy</groupId>
           <artifactId>byte-buddy-agent</artifactId>
           <version>1.12.10</version>
           <scope>test</scope>
       </dependency>
   </dependencies>
</dependencyManagement>
```


REF9: Resources.java

https://raw.githubusercontent.com/marcobehlerjetbrains/junit5-tutorial/main/src/test/java/com/jetbrains/util/Resources.java
