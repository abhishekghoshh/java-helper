

## Medium blogs

- [Everything You Need To Know About Apache Maven : Part 1](https://medium.com/@saquibdev/everything-you-need-to-know-about-apache-maven-part-1-f16ce2ed7d24)
- [Maven Build Lifecycles, Maven Plugins and Maven Profiles](https://medium.com/javarevisited/maven-build-lifecycles-maven-plugins-and-maven-profiles-10bc01640662)


## Youtube video

- [Maven Tutorial - Crash Course](https://www.youtube.com/watch?v=Xatr8AZLOsE)
    - [marcobehlerjetbrains/maven-tutorial](https://github.com/marcobehlerjetbrains/maven-tutorial)

## Official documentation

- [Introduction to the POM](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html)
- [Introduction to the Dependency Mechanism](https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html#Dependency_Scope)
- [Maven: The Complete Reference](https://books.sonatype.com/mvnref-book/reference/index.html)

## Useful links

- [mvn clean install - a short guide to Maven](https://www.marcobehler.com/guides/mvn-clean-install-a-short-guide-to-maven)

## Useful commands

Below are some useful Maven commands with descriptions:

**Display Maven version**  
This command displays the installed Maven version.
```sh
mvn --version
```

**Clean the project**  
This command removes the target directory with all the build data.
```sh
mvn clean
```

**Install the project**  
This command installs the project's artifacts into the local repository.
```sh
mvn install
```

**Validate the project**  
This command validates the project is correct and all necessary information is available.
```sh
mvn validate
```

**Compile the project**  
This command compiles the source code of the project.
```sh
mvn compile
```

**Run tests**  
This command runs the tests using a suitable unit testing framework.
```sh
mvn test
```

**Clean and install the project**  
This command cleans the project and then installs the project's artifacts into the local repository.
```sh
mvn clean install
```

**Clean and package the project, skipping tests**  
This command cleans the project and packages the code, skipping the test phase.
```sh
mvn clean package -DskipTests
```

**Compile, test, and package the project**  
This command compiles the source code, runs the tests, and packages the code.
```sh
mvn compile test package
```

**Clean and compile the project natively, skipping tests**  
This command cleans the project and compiles it natively, skipping the test phase.
```sh
mvn clean -Pnative native:compile -DskipTests
```

**Create Maven wrapper**  
This command creates the Maven wrapper scripts.
```sh
mvn wrapper:wrapper
```

**Display Maven wrapper version**  
This command displays the version of the Maven wrapper.
```sh
mvnw --version
```


```sh
mvn clean deploy
```

```sh
mvn nexus-staging:release
```


```sh
mvn nexus-staging:drop
```


These commands are essential for managing and building Maven projects. They help in various stages of the build lifecycle, from cleaning and compiling to packaging and testing.



## Multimodule pom projects


## Publish your repositories in central maven repo

**Watch this:**

- [How to publish your own library to Maven Central Repository | Publishing dependency to OSSRH | HINDI](https://www.youtube.com/watch?v=xEMOF443WI8)
    - [librbary/librbary-retry](https://github.com/librbary/librbary-retry)
- [Publish your artifact to the Maven Central Repository using GitHub Actions](https://medium.com/@jtbsorensen/publish-your-artifact-to-the-maven-central-repository-using-github-actions-15d3b5d9ce88)
- [Deploying to OSSRH with Apache Maven](https://central.sonatype.org/publish/publish-maven/)


**My Packages**

- [maven central repository of io/github/abhishekghoshh](https://repo1.maven.org/maven2/io/github/abhishekghoshh/)
- [My Core project Maven publish](https://central.sonatype.com/artifact/io.github.abhishekghoshh/feather)
- [Group: GitHub Abhishekghoshh](https://mvnrepository.com/artifact/io.github.abhishekghoshh)



**Step 1:**<br>
Create an account on maven central and create namespace and verify that. Try to sign in with github. That will help for the namespace verification. Also generate a token and save it somewhere.

- [Maven Central](https://central.sonatype.com/)
- [Register to Publish Via the Central Portal](https://central.sonatype.org/register/central-portal/)


**Step 2:**<br>
Add the plugins and remaining things one by one from [Deploying to OSSRH with Apache Maven](https://central.sonatype.org/publish/publish-maven/)


**Step 3:**
Install GnuPG and create a key pair from [GPG](https://central.sonatype.org/publish/requirements/gpg/) and also push it<br>
use this command to generate a key pair `gpg --gen-key` <br>
use this command to find out key `gpg --list-signatures --keyid-format 0xshort` <br>
use this command to publish your public key `gpg --keyserver keys.openpgp.org --send-keys $PUBLIC_KEY` <br>
use this command to get the private key `gpg --armor --export-secret-keys $PUBLIC_KEY` <br>




**Official maven plugin for jar signing is not working in Github Action, so I searched some repos and found a way around**

> I have followed this [pom.xml](https://github.com/elasticsoftwarefoundation/elasticactors/blob/master/pom.xml) and [maven-publish.yml](https://github.com/elasticsoftwarefoundation/elasticactors/blob/master/.github/workflows/maven-publish-v6.yml)


```xml
<!-- This plugin is used for signing the jars in GitHub action-->
<plugin>
    <groupId>org.simplify4u.plugins</groupId>
    <artifactId>sign-maven-plugin</artifactId>
    <version>1.1.0</version>
</plugin>
<!-- This plugin is not working in GitHub action, Uncomment this if you are building from local-->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-gpg-plugin</artifactId>
    <version>${maven-gpg-plugin.version}</version>
    <executions>
        <execution>
            <id>sign-artifacts</id>
            <phase>verify</phase>
            <goals>
                <goal>sign</goal>
            </goals>
            <configuration>
                <keyname>0xF362E38F</keyname>
                <gpgArguments>
                    <arg>--pinentry-mode</arg>
                    <arg>loopback</arg>
                </gpgArguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

Also added these, check these later
```xml
<profiles>
    <profile>
        <id>maven-release</id>
    </profile>
    <profile>
        <id>ci</id>
        <build>
            <plugins>
                <plugin>
                    <artifactId>maven-javadoc-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>attach-javadocs</id>
                            <goals>
                                <goal>jar</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
    <profile>
        <id>release</id>
        <build>
            <plugins>
                <plugin>
                    <groupId>org.sonatype.plugins</groupId>
                    <artifactId>nexus-staging-maven-plugin</artifactId>
                    <extensions>true</extensions>
                    <configuration>
                        <serverId>ossrh</serverId>
                        <nexusUrl>https://s01.oss.sonatype.org/</nexusUrl>
                        <autoReleaseAfterClose>true</autoReleaseAfterClose>
                    </configuration>
                </plugin>
                <plugin>
                    <groupId>org.simplify4u.plugins</groupId>
                    <artifactId>sign-maven-plugin</artifactId>
                    <configuration>
                        <skipNoKey>false</skipNoKey>
                    </configuration>
                    <executions>
                        <execution>
                            <goals>
                                <goal>sign</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <artifactId>maven-source-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>attach-source</id>
                            <phase>package</phase>
                            <goals>
                                <goal>jar-no-fork</goal>
                            </goals>
                            <configuration>
                                <attach>true</attach>
                            </configuration>
                        </execution>
                    </executions>
                </plugin>
                <plugin>
                    <artifactId>maven-javadoc-plugin</artifactId>
                    <executions>
                        <execution>
                            <id>attach-javadocs</id>
                            <goals>
                                <goal>jar</goal>
                            </goals>
                        </execution>
                    </executions>
                </plugin>
            </plugins>
        </build>
    </profile>
</profiles>
```

Check the [Easy way to create PGP/GPG Signatures with Maven on CI/CD](https://www.simplify4u.org/how-to/maven-openpgp-signatures.html)

[Sign artifacts with GnuPG](https://maven.apache.org/plugins/maven-gpg-plugin/usage.html)


> See the feather project if there is any problem