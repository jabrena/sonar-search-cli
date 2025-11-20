# Essential Maven Goals:

```bash
# Analyze dependencies
./mvnw dependency:tree
./mvnw dependency:analyze
./mvnw dependency:resolve

./mvnw clean validate -U
./mvnw buildplan:list-plugin
./mvnw buildplan:list-phase
./mvnw help:all-profiles
./mvnw help:active-profiles
./mvnw license:third-party-report

# Clean the project
./mvnw clean

# Clean and package in one command
./mvnw clean package

# Run unit tests & integration tests
./mvnw clean test verify

# Generate project reports
./mvnw clean site
jwebserver -p 8000 -d "$(pwd)/target/site/"

# Check for dependency updates
./mvnw versions:display-property-updates
./mvnw versions:display-dependency-updates
./mvnw versions:display-plugin-updates

./mvnw versions:set -DnewVersion=0.1.0
./mvnw versions:commit

# Create jar
./mvnw clean package -DskipTests

# Run Sonar Search
java -jar churrera-cli/target/sonar-search-0.1.0.jar
```
