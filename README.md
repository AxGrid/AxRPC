AxRPC
=====

Install
-------

```xml
<dependency>
    <groupId>com.axgrid.rpc</groupId>
    <artifactId>AxRPCService</artifactId>
    <version>1.2</version>
</dependency>
```


Usage
-----

Create proto:

```proto

message Request {
    int64 correlationId = 1; // CID Field
    OpPing op_ping = 10;     // Your Request Message
}

message Response {
    int64 correlationId = 1; // CID Field
    bool success = 2;        // Success RPC Field
    string errorText = 3;    // Error Text Field
    int32 errorCode = 4;     // Code Field

    RpPing rp_ping = 10;     // Your Response Message
}

message OpPing {             // Your Request Message
    uint64 time = 1;
}

message RpPing {              // Your Response Message
    uint64 time = 1;
}

```

Create Spring Boot Service

```java

@Service
public class AxTestRPC extends AxRPCService<Request, Response, AxContext> {

    @AxRPC
    public RpPing.Builder ping(OpPing ping) {
        return RpPing.newBuilder().setTime(ping.getTime());
    }
}

```


Execute Spring Boot Service
```java

@Component
public class TestAxRPC {

    @Autowired
    AxTestRPC rpc;

    public void execute(Request r) {
        Response response = rpc.request(r, null);
    }
}

```

Additional:

* [AxRPCWeb](AxRPCWeb/) Web Handlers for AxRPC
* [AxRPCNet](AxRPCNet/) TCP Handlers for AxRPC


For me:

`mvn versions:set -DnewVersion=1.4-SNAPSHOT`
