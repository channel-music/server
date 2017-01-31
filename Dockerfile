FROM java:8-alpine

MAINTAINER Antonis Kalou <kalouantonis@gmail.com>

ADD target/uberjar/sound-app.jar /usr/bin/sound-app.jar

EXPOSE 3000

CMD ["java", "-jar", "/usr/bin/sound-app.jar"]
