package com.anuchandy.learnings.dynamicinvoke;


import com.azure.core.http.rest.Response;
import reactor.core.Exceptions;

import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseCreatorLambdaMetaCached {
    private final Map<Class<?>, Optional<Entry>> cache = new ConcurrentHashMap<>();

    public Optional<Entry> locateResponseLambdaMetaCtr(Class<? extends Response<?>> responseClass) {
        if (!cache.containsKey(responseClass)) {
            cache.put(responseClass, Arrays.stream(responseClass.getDeclaredConstructors())
                    .filter(constructor -> {
                        int paramCount = constructor.getParameterCount();
                        return paramCount >= 3 && paramCount <= 5;
                    })
                    .sorted(Comparator.comparingInt(Constructor::getParameterCount))
                    .findFirst()
                    .flatMap(constructor -> {
                        try {
                            MethodHandles.Lookup lookup = MethodHandles.lookup();
                            MethodHandle ctrMethodHandle = lookup.unreflectConstructor(constructor);
                            switch (constructor.getParameterCount()) {
                                case 3:
                                    Object f3 = LambdaMetafactory.metafactory(lookup,
                                            "apply",
                                            MethodType.methodType(Function3.class),
                                            ctrMethodHandle.type().generic(),
                                            ctrMethodHandle,
                                            ctrMethodHandle.type()).getTarget().invoke();
                                    return Optional.of(new Entry(3, f3));
                                case 4:
                                    Object f4 = LambdaMetafactory.metafactory(lookup,
                                            "apply",
                                            MethodType.methodType(Function4.class),
                                            ctrMethodHandle.type().generic(),
                                            ctrMethodHandle,
                                            ctrMethodHandle.type()).getTarget().invoke();
                                    return Optional.of(new Entry(4, f4));
                                case 5:
                                    Object f5 = LambdaMetafactory.metafactory(lookup,
                                            "apply",
                                            MethodType.methodType(Function5.class),
                                            ctrMethodHandle.type().generic(),
                                            ctrMethodHandle,
                                            ctrMethodHandle.type()).getTarget().invoke();
                                    return Optional.of(new Entry(5, f5));
                                default:
                                    return Optional.empty();
                            }

                        } catch (Throwable ieae) {
                            return Optional.empty();
                        }
                    }));
        }
        return cache.get(responseClass);
    }

    public Response<?> invokeCreateResponseInstance(Entry entry,
                                                    TestData.Input input) {
        switch (entry.paramCount) {
            case 3:
                try {
                    return (Response<?>) ((Function3) entry.function).apply(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders);
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            case 4:
                try {
                    return (Response<?>) ((Function4) entry.function).apply(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders,
                            input.bodyAsObject);
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            case 5:
                try {
                    return (Response<?>) ((Function5) entry.function).apply(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders,
                            input.bodyAsObject,
                            input.deserHeaders);
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            default:
                throw new IllegalStateException("Response constructor with expected parameters not found.");
        }
    }

    public static class Entry {
        public final int paramCount;
        public final Object function;

        public Entry(int paramCount, Object function) {
            this.paramCount = paramCount;
            this.function = function;
        }
    }

    @FunctionalInterface
    private interface Function3<T1, T2, T3, R> {
        R apply(T1 request, T2 statusCode, T3 responseHeaders);
    }

    @FunctionalInterface
    private interface Function4<T1, T2, T3, T4, R> {
        R apply(T1 request, T2 statusCode, T3 responseHeaders, T4 bodyAsObject);
    }

    @FunctionalInterface
    private interface Function5<T1, T2, T3, T4, T5, R> {
        R apply(T1 request, T2 statusCode, T3 responseHeaders, T4 bodyAsObject, T5 dserHeaders);
    }
}
