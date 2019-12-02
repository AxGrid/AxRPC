AxRPCNet
========

Add netty tcp handler

Install
-------

```xml

<dependency>
    <groupId>com.axgrid.rpc</groupId>
    <artifactId>AxRPCNet</artifactId>
    <version>1.2</version>
</dependency>
```

Usage
-----

```java

@Configuration
@EnableAxRPCNet
public class AxRPCNetServiceConfiguration{ }

```

Create a NetService

```java

@Service
public class MyAxRPCTCPHandler extends AxRPCNetService<Request, Response, MyContext> { 
    public MyAxRPCTCPHandler() {
        super(9000); // Setup TCP-PORT
    }
}
```


Create ContextService

```java

@Service
public class MyAxRPCContextService implements AxRPCContextServiceImpl<Request, MyContext> { 
    public MyContext getContext(Request request, Object httpRequest) {
        return new MyContext(); // Get From Repository
    }
}
```

Enjoy !
