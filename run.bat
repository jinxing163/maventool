set "PATH=%PATH%;%JAVA_HOME%/bin"
java -version
copy config.properties target /Y
copy conf.json target /Y
java -jar target/maven_tool.jar
pause