# build-graph-maven-plugin description
This plugin designed to draw maven reactor build graph with relations and statuses.

Plugin is designed to run with -T and -fae options.

It could be placed in any lifecycle phase, plugin always tries launch in the end of maven build session.
   
Look at examples in tests/src/test/resources folder.

Example launch:

![sample build graph](https://github.com/misterreg/build-graph-maven-plugin/blob/master/documentation/sample.png "sample build graph")

```
mvn -T 4 -fae clean package
```
Write this in your aggregator pom.xml
```xml
<plugin>
  <groupId>com.github.misterreg.mavenplugins</groupId>
  <artifactId>build-graph-maven-plugin</artifactId>
  <version>0.0.1</version>
  <executions>
    <execution>
      <id>build-graph</id>
      <phase>package</phase>
      <goals>
        <goal>build-graph</goal>
      </goals>
    </execution>
  </executions>
  <configuration>
    <pngScale>2</pngScale>
    <pngFileName>testGraph</pngFileName>
    <excludeProjects>
      <param>project1</param>
      <param>project1</param>
    </excludeProjects>
    <projectMask>project $groupId:$artifactId:$version</projectMask>
    <graphOrientation>horizontal</graphOrientation>
  </configuration>
</plugin>
```
