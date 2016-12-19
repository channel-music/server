FROM java:8-alpine
MAINTAINER Your Name <you@example.com>

ADD target/uberjar/sound-app.jar /sound-app/app.jar

EXPOSE 3000

CMD ["java", "-jar", "/sound-app/app.jar"]
