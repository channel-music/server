FROM java:8-alpine

MAINTAINER Antonis Kalou <kalouantonis@gmail.com>

ADD target/uberjar/channel.jar /usr/bin/channel.jar

EXPOSE 3000

CMD ["java", "-jar", "/usr/bin/channel.jar"]
