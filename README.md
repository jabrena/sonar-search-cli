# Sonar Search CLI

## How to use

```bash
./mvnw clean package

java -jar target/sonar-search-0.1.0-SNAPSHOT.jar --help
java -jar target/sonar-search-0.1.0-SNAPSHOT.jar --component jabrena_churrera-cli --type BUG --quiet >> bugs.json
java -jar target/sonar-search-0.1.0-SNAPSHOT.jar --component jabrena_churrera-cli --type CODE_SMELL --quiet >> code-smells.json
java -jar target/sonar-search-0.1.0-SNAPSHOT.jar --component jabrena_churrera-cli --type VULNERABILITY --quiet  >> vulnerability.json
```

