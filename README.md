# Sonar Search CLI

## Motivation

`Sonar` from `SonarSource` is a static code analysis platform that helps developers find and fix bugs, security vulnerabilities, and code quality issues before they reach production.

Using this tool you could retrieve in JSON format information about issues, hotspots, and duplications to be fixed in your project.

The `--severity` option allows you to filter issues by severity level: `BLOCKER`, `CRITICAL`, `MAJOR`, `MINOR`, or `INFO`. The option accepts lowercase values (e.g., `blocker`, `critical`).

The `--status` option allows you to filter issues by status: `OPEN`, `CLOSED`, `CONFIRMED`, `REOPENED`, or `RESOLVED`. The option accepts lowercase values (e.g., `open`, `closed`).

The `--size` option allows you to control the page size (number of results per page). Valid range: 1-500. Default: 100. This maps to the `ps` parameter in the SonarCloud API.

The `--detail` option allows you to retrieve detailed information about a specific issue or hotspot by its key-id. This option requires `--query` to specify whether you want issue details (`--query issues`) or hotspot details (`--query hotspots`). This option cannot be used together with `--types`, `--severity`, or `--status` options.

The `--query duplications` option allows you to retrieve information about code duplications in your project. This option requires `--project` and cannot be used together with `--types`, `--severity`, or `--status` options.

## How to use

```bash
#JBang
sdk install jbang

jbang trust list
jbang trust add https://github.com/jabrena/
jbang cache clear
jbang catalog list jabrena

jbang sonar-search@jabrena --help
jbang sonar-search.0.2.0-SNAPSHOT@jabrena --project jabrena_churrera-cli --query issues --types all  --severity blocker --status open --size 5
jbang sonar-search.0.2.0-SNAPSHOT@jabrena --project jabrena_churrera-cli --query issues --types all  --severity critical --status open --size 5 --quiet
jbang sonar-search.0.2.0-SNAPSHOT@jabrena --project jabrena_churrera-cli --query issues --detail AZqYjkIv6smiiI-pnAwI


jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types bug
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types bug --quiet >> bugs.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types bug --severity blocker --quiet >> blocker_bugs.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types bug --status open --quiet >> open_bugs.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types bug --severity blocker --status open --quiet >> open_blocker_bugs.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types code_smell --quiet >> code_smells.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types code_smell --severity critical --quiet >> critical_code_smells.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types vulnerability --quiet >> vulnerabilities.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types vulnerability --severity major --quiet >> major_vulnerabilities.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query issues --types bug --size 200 --quiet >> bugs_page200.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query hotspots --quiet >> hotspots.json
jbang sonar-search@jabrena --project jabrena_churrera-cli --query duplications --quiet >> duplications.json
jbang sonar-search@jabrena --query issues --detail AZqZJmQWWyUHIeVsO2He --quiet >> issue_detail.json
jbang sonar-search@jabrena --query hotspots --detail AXqZJmQWWyUHIeVsO2Hf --quiet >> hotspot_detail.json

# In local
./mvnw clean package

java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --help
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types bug
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types bug --quiet >> bugs.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types bug --severity blocker --quiet >> blocker_bugs.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types bug --status open --quiet >> open_bugs.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types bug --severity blocker --status open --quiet >> open_blocker_bugs.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types code_smell --quiet >> code-smells.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types code_smell --severity critical --quiet >> critical_code_smells.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types vulnerability --quiet >> vulnerabilities.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types vulnerability --severity major --quiet >> major_vulnerabilities.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query issues --types bug --size 200 --quiet >> bugs_page200.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query hotspots --quiet >> hotspots.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --project jabrena_churrera-cli --query duplications --quiet >> duplications.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --query issues --detail AZqZJmQWWyUHIeVsO2He --quiet >> issue_detail.json
java -jar target/sonar-search-0.2.0-SNAPSHOT.jar --query hotspots --detail AXqZJmQWWyUHIeVsO2Hf --quiet >> hotspot_detail.json
```

## Reference

- https://sonarcloud.io/web_api
- https://www.sonarsource.com/

Powered by [Cursor](https://www.cursor.com/) with ❤️ from [Madrid](https://www.google.com/maps/place/Community+of+Madrid,+Madrid/@40.4983324,-6.3162283,8z/data=!3m1!4b1!4m6!3m5!1s0xd41817a40e033b9:0x10340f3be4bc880!8m2!3d40.4167088!4d-3.5812692!16zL20vMGo0eGc?entry=ttu&g_ep=EgoyMDI1MDgxOC4wIKXMDSoASAFQAw%3D%3D)
