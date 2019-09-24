package com.anuchandy.learnings.dynamicinvoke;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.Page;
import com.azure.core.http.rest.ResponseBase;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.StreamResponse;
import com.azure.core.http.rest.VoidResponse;
import com.azure.core.implementation.http.PagedResponseBase;
import reactor.core.publisher.Flux;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestData {
    // Model type for Http content
    private static class Foo {
        private String name;

        public Foo setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return this.name;
        }
    }

    // Model type for custom Http headers
    private static class FooHeader {
    }

    // 1. final VoidResponse               (Ctr_args: 3)
    //    VoidResponse(HttpRequest request, int statusCode, HttpHeaders headers)

    // 2. SimpleResponse<Foo> Type         (Ctr_args: 4)
    private static class FooSimpleResponse extends SimpleResponse<Foo> {
        public FooSimpleResponse(HttpRequest request,
                                 int statusCode,
                                 HttpHeaders headers,
                                 Foo value) {
            super(request, statusCode, headers, value);
        }
    }

    // 3. final StreamResponse             (Ctr_args: 4)
    //    StreamResponse(HttpRequest request, int statusCode, HttpHeaders headers, Flux<ByteBuffer> value)

    // 4. ResponseBase<FooHeader, Foo>     (Ctr_args: 5)
    private static class FooResponseBase extends ResponseBase<FooHeader, Foo> {
        public FooResponseBase(HttpRequest request,
                               int statusCode,
                               HttpHeaders headers,
                               Foo value,
                               FooHeader deserHeaders) {
            super(request, statusCode, headers, value, deserHeaders);
        }
    }

    // 5. PagedResponseBase<FooHeader, Foo> (Ctr_args: 5)
    private static class FooPagedResponseBase extends PagedResponseBase<FooHeader, Foo> {
        public FooPagedResponseBase(HttpRequest request,
                                    int statusCode,
                                    HttpHeaders headers,
                                    Page<Foo> page,
                                    FooHeader deserHeaders) {
            super(request, statusCode, headers, page, deserHeaders);
        }
    }

    public interface FooService {
        VoidResponse getVoidResponse();
        FooSimpleResponse getFooSimpleResponse();
        StreamResponse getStreamResponse();
        FooResponseBase getResponseBaseFoo();
        FooPagedResponseBase getPagedResponseBaseFoo();
    }

    private final HttpRequest HTTP_REQUEST = new HttpRequest(HttpMethod.GET, createUrl());
    private final HttpHeaders RESPONSE_HEADERS = new HttpHeaders().put("hello", "world");
    private final int RESPONSE_STATUS_CODE = 200;
    private final FooHeader FOO_HEADER = new FooHeader();
    private final Foo FOO = new Foo().setName("foo1");
    private final Flux BB_FLUX = Flux.just(ByteBuffer.wrap(new byte[1]));
    private final Page<Foo> PAGE_FOO = new Page<Foo>() {
        @Override
        public List<Foo> getItems() {
            List<Foo> items = new ArrayList<>();
            items.add(FOO);
            return items;
        }

        @Override
        public String getNextLink() {
            return null;
        }
    };
    //
    public final TestData.Input[] inputs;

    TestData() {
        this.inputs = new TestData.Input[5];
        //
        // Void Body
        // No Custom Header
        this.inputs[0] = new TestData.Input(findMethod(FooService.class, "getVoidResponse")
                .getGenericReturnType(), HTTP_REQUEST, RESPONSE_HEADERS, RESPONSE_STATUS_CODE);
        //
        // No Custom Header
        this.inputs[1] = new TestData.Input(findMethod(FooService.class, "getFooSimpleResponse")
                .getGenericReturnType(), HTTP_REQUEST, RESPONSE_HEADERS, RESPONSE_STATUS_CODE, FOO);
        //
        // No Custom Header
        this.inputs[2] = new TestData.Input(findMethod(FooService.class, "getStreamResponse")
                .getGenericReturnType(), HTTP_REQUEST, RESPONSE_HEADERS, RESPONSE_STATUS_CODE, BB_FLUX);
        //
        this.inputs[3] = new TestData.Input(findMethod(FooService.class, "getResponseBaseFoo")
                .getGenericReturnType(), HTTP_REQUEST, RESPONSE_HEADERS, RESPONSE_STATUS_CODE, FOO, FOO_HEADER);
        //
        this.inputs[4] = new  TestData.Input(findMethod(FooService.class, "getPagedResponseBaseFoo")
                .getGenericReturnType(),HTTP_REQUEST, RESPONSE_HEADERS, RESPONSE_STATUS_CODE, PAGE_FOO, FOO_HEADER);
    }

    private Method findMethod(Class<?> cls, String methodName) {
        Optional<Method> optMethod = Arrays.stream(cls.getDeclaredMethods())
                .filter(m -> m.getName().equalsIgnoreCase(methodName))
                .findFirst();
        if (optMethod.isPresent()) {
            return optMethod.get();
        } else {
            throw new RuntimeException("Method with name '"+ methodName + "' not found.");
        }
    }

    private URL createUrl() {
        try {
            return new URL("http://localhost");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    class Input {
        public final Type returnType;
        public final HttpRequest httpRequest;
        public final HttpHeaders responseHeaders;
        public final int responseStatusCode;
        public final Object bodyAsObject;
        public final FooHeader deserHeaders;

        Input(Type returnType,
              HttpRequest httpRequest,
              HttpHeaders responseHeaders,
              int responseStatusCode) {
            this.returnType = returnType;
            this.httpRequest = httpRequest;
            this.responseHeaders = responseHeaders;
            this.responseStatusCode = responseStatusCode;
            this.bodyAsObject = null;
            this.deserHeaders = null;
        }

        Input(Type returnType,
              HttpRequest httpRequest,
              HttpHeaders responseHeaders,
              int responseStatusCode,
              Object body) {
            this.returnType = returnType;
            this.httpRequest = httpRequest;
            this.responseHeaders = responseHeaders;
            this.responseStatusCode = responseStatusCode;
            this.bodyAsObject = body;
            this.deserHeaders = null;
        }

        Input(Type returnType,
              HttpRequest httpRequest,
              HttpHeaders responseHeaders,
              int responseStatusCode,
              Object body,
              FooHeader deserHeaders) {
            this.returnType = returnType;
            this.httpRequest = httpRequest;
            this.responseHeaders = responseHeaders;
            this.responseStatusCode = responseStatusCode;
            this.bodyAsObject = body;
            this.deserHeaders = deserHeaders;
        }

        Input(Type returnType,
              HttpRequest httpRequest,
              HttpHeaders responseHeaders,
              int responseStatusCode,
              Page<Foo> body,
              FooHeader deserHeaders) {
            this.returnType = returnType;
            this.httpRequest = httpRequest;
            this.responseHeaders = responseHeaders;
            this.responseStatusCode = responseStatusCode;
            this.bodyAsObject = body;
            this.deserHeaders = deserHeaders;
        }
    }
}
