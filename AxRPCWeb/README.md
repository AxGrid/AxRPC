AxRPCWeb
========

Install
-------

```xml
<dependency>
    <groupId>com.axgrid.rpc</groupId>
    <artifactId>AxRPCWeb</artifactId>
    <version>1.1</version>
</dependency>
```

Usage
-----

Enable AxRPCWeb

```java

@Configuration
@EnableAxRPCWeb
public class AxRPCWebServiceConfiguration{ }

```

Create a WebService

```java

@Component
@RequestMapping("/ax-rpc/")
public class MyAxRPCWebHandler extends AxRPCWebHandler<Request, Response, MyContext> { 
    
}
```


Create ContextService

```java

@Component
@RequestMapping("/ax-rpc/")
public class MyAxRPCContextService implements AxRPCContextService<Request, MyContext> { 
    public MyContext getContext(Request request, HttpServletRequest httpRequest) {
        return new MyContext(); // Get From Repository
    }       
}
```


Enjoy !
