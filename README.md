# emp-script-utils

Easy Web Application Utilities for Java

## Official web
https://www.gdxsoft.com 

## Github
https://github.com/gdx1231/emp-script-utils

## Maven
```xml
<dependency>
  <groupId>com.gdxsoft.easyweb</groupId>
  <artifactId>emp-script-utils</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Key Features (v2.0.0)

- **JDK 17+** minimum requirement
- **Tomcat 10+** compatibility with Jakarta EE 9+
- **UNet class** uses JDK built-in HttpURLConnection (no Apache HttpClient dependency)
- **Jakarta namespace** migration (`javax.*` → `jakarta.*`)
- All dependencies updated to latest stable versions

## Migration from v1.x.x

### Breaking Changes:
- JDK 8 → JDK 17
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.mail.*` → `jakarta.mail.*`
- UNet now uses JDK built-in HTTP client instead of Apache HttpClient

### UNet Usage Example:
```java
// Basic HTTPS request
UNet unet = new UNet();
String response = unet.doGet("https://gdxsoft.com");
System.out.println("Status: " + unet.getLastStatusCode());
```

## Dependencies Removed in v2.0.0
- org.apache.httpcomponents:httpclient
- org.apache.httpcomponents:httpmime
- commons-codec (runtime, kept for tests)

## Testing
Comprehensive unit tests included, including UNet HTTPS connectivity tests to https://gdxsoft.com

