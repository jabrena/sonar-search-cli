# Sonar Search CLI

## Motivation

`Sonar` from `SonarSource` is a static code analysis platform that helps developers find and fix bugs, security vulnerabilities, and code quality issues before they reach production.

Using this tool you could retrieve in JSON format information about issues & hotspots to be fixed in your project.

## How to use

```bash
#JBang
sdk install jbang

jbang trust list
jbang trust add https://github.com/jabrena/
jbang cache clear
jbang catalog list jabrena

jbang sonar-search@jabrena --help
jbang sonar-search@jabrena --project jabrena_churrera-cli --issues BUG
jbang sonar-search@jabrena --project jabrena_churrera-cli --issues BUG --quiet >> bugs.json
jabrena_churrera-cli --issues CODE_SMELL --quiet >> code_smells.json
jabrena_churrera-cli --issues VULNERABILITY --quiet >> vulnerabilities.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --hotspots --quiet >> hotspots.json

# In local
./mvnw clean package

java -jar target/sonar-search-0.1.0.jar --help
java -jar target/sonar-search-0.1.0.jar --project jabrena_churrera-cli --issues BUG
java -jar target/sonar-search-0.1.0.jar --project jabrena_churrera-cli --issues BUG --quiet >> bugs.json
java -jar target/sonar-search-0.1.0.jar --project jabrena_churrera-cli --issues CODE_SMELL --quiet >> code-smells.json
java -jar target/sonar-search-0.1.0.jar --project jabrena_churrera-cli --issues VULNERABILITY --quiet  >> vulnerabilities.json
java -jar target/sonar-search-0.1.0.jar --project jabrena_churrera-cli --hotspots --quiet >> hotspots.json
```

## Reference

- https://sonarcloud.io/web_api
- https://www.sonarsource.com/

Powered by [Cursor](https://www.cursor.com/) with ❤️ from [Madrid](https://www.google.com/maps/place/Community+of+Madrid,+Madrid/@40.4983324,-6.3162283,8z/data=!3m1!4b1!4m6!3m5!1s0xd41817a40e033b9:0x10340f3be4bc880!8m2!3d40.4167088!4d-3.5812692!16zL20vMGo0eGc?entry=ttu&g_ep=EgoyMDI1MDgxOC4wIKXMDSoASAFQAw%3D%3D)
