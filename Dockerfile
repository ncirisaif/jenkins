FROM bitnami/java:17 as builder
RUN apt-get update -y && apt-get install maven -y
COPY . .
RUN mvn clean package -Dmaven.test.skip=true


FROM bitnami/java:17
ENV APPLICATION_DIR="/target/"
ENV MODULE="demo-0.0.1.jar"
ENV JAVA_OPTIONS=${java_options}
ENV APPLICATION_OPTIONS=${application_options}
COPY --from=builder $APPLICATION_DIR/$MODULE $APPLICATION_DIR/$MODULE
CMD java $JAVA_OPTIONS -jar $APPLICATION_DIR/$MODULE $APPLICATION_OPTIONS
