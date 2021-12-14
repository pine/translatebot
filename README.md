# translatebot
:abcd: Automatic translation bot for learning English

## Requirements
- Java 11 or later
- Redis

## Libraries
- Spring Boot 2.x


### Deployment

```sh
$ heroku apps:create your-app
$ heroku config:set SPRING_PROFILES_ACTIVE=prod
$ heroku config:set TZ=Asia/Tokyo
$ heroku config:set 'JAVA_OPTS=-Xmx200m -XX:+UseCompressedOops -XX:+UseStringDeduplication -XX:-OmitStackTraceInFastThrow -Dlog4j2.formatMsgNoLookups=true'

# Setup Redis
$ heroku addons:create heroku-redis:hobby-dev

# Deploy JAR file
$ ./gradlew build
$ heroku plugins:install java
$ heroku deploy:jar --jar app/build/libs/app.jar --jdk 11
```

## License
MIT &copy; Pine Mizune
