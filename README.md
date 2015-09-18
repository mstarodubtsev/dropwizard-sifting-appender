# dropwizard-sifting-appender

[Dropwizard](http://dropwizard.io/) logging addon for using logback SiftingAppender for dynamically splitting log messages into separate log files depending of MDC context. This is needed because Dropwizard overwrites the default mechanism for loading logback configuration (logback.xml) in favor of its application.yml files.

## Installation
Maven:
```xml
<dependency>
  <groupId>com.github.mstarodubtsev.dropwizard</groupId>
  <artifactId>dropwizard-sifting-appender</artifactId>
  <version>0.0.7</version>
</dependency>
```

## Usage
You must configure dropwizard to use this appender in your application.yml file:
Example config:
```yaml
logging:
  appenders:
    - type: sift
      discriminatorKey: logfileName
      discriminatorDefaultValue: default
      messagePattern: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36}: %msg%n"
      threshold: ALL
      timeZone: YEKT
```

Required configuration keys for the appender:
* `type` - must be 'sift'
* `discriminatorKey` - Discriminator key for sift events
* `discriminatorDefaultValue` - Discriminator default value
* `messagePattern` - The Logback pattern with which events will be formatted.
* `threshold` - The lowest level of events to processing by appender.
* `timeZone` - The time zone to which event timestamps will be converted.


Then, loggers can be used the same way as if they were configured using logback.xml, 
example with slf4j:
```java
class Test {
	public static void main(String[] args) {
		org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Test.class);
		MDC.put("logfileName", Thread.currentThread().getName());
		log.info("test");
	}
}
```

or just using Lombok annotation:
```java
@Slf4j
class Test {
	public static void main(String[] args) {
		MDC.put("logfileName", Thread.currentThread().getName());
		log.info("test");
	}
}
```

## License

Distributed under the Apache License, Version 2.0.