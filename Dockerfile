FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/channel.jar /channel/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/channel/app.jar"]
