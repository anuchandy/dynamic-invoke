package com.anuchandy.learnings.dynamicinvoke;


import com.azure.core.http.rest.Response;
import reactor.core.Exceptions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseCreatorMethodHandleCached {
    private final Map<Class<?>, Optional<MethodHandle>> cache = new ConcurrentHashMap<>();

    public Optional<MethodHandle> locateResponseMethodHandleCtr(Class<? extends Response<?>> responseClass) {
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
                            return Optional.of(lookup.unreflectConstructor(constructor));
                        } catch (IllegalAccessException ieae) {
                            return Optional.empty();
                        }
                    }));
        }
        return cache.get(responseClass);
    }

    public Response<?> invokeCreateResponseInstance(MethodHandle ctrMethodHandle,
                                                    TestData.Input input) {
        final int paramCount = ctrMethodHandle.type().parameterCount();
        switch (paramCount) {
            case 3:
                try {
                    return (Response<?>) ctrMethodHandle.invoke(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders);
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            case 4:
                try {
                    return (Response<?>) ctrMethodHandle.invoke(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders,
                            input.bodyAsObject);
                } catch (Throwable t) {
                    throw Exceptions.propagate(t);
                }
            case 5:
                try {
                    return (Response<?>) ctrMethodHandle.invoke(input.httpRequest,
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
}
