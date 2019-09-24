package com.anuchandy.learnings.dynamicinvoke;

import com.azure.core.http.rest.Response;
import reactor.core.Exceptions;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseCreatorReflectionCached implements ResponseCreatorReflection {
    private final Map<Class<?>, Optional<Constructor<? extends Response<?>>>> cache = new ConcurrentHashMap<>();
    //
    @Override
    public Optional<Constructor<? extends Response<?>>> locateResponseReflectionCtr(Class<? extends Response<?>> responseClass) {
        if (!cache.containsKey(responseClass)) {
            cache.put(responseClass, Arrays.stream(responseClass.getDeclaredConstructors())
                    .filter(constructor -> {
                        int paramCount = constructor.getParameterCount();
                        return paramCount >= 3 && paramCount <= 5;
                    })
                    .sorted(Comparator.comparingInt(Constructor::getParameterCount))
                    .findFirst()
                    .map(constructor -> (Constructor<? extends Response<?>>) constructor));
        }
        return cache.get(responseClass);
    }

    @Override
    public Response<?> invokeCreateResponseInstance(Constructor<? extends Response<?>> constructor,
                                                    TestData.Input input) {
        final int paramCount = constructor.getParameterCount();
        switch (paramCount) {
            case 3:
                try {
                    return constructor.newInstance(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw Exceptions.propagate(e);
                }
            case 4:
                try {
                    return constructor.newInstance(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders,
                            input.bodyAsObject);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw Exceptions.propagate(e);
                }
            case 5:
                try {
                    return constructor.newInstance(input.httpRequest,
                            input.responseStatusCode,
                            input.responseHeaders,
                            input.bodyAsObject,
                            input.deserHeaders);
                } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
                    throw Exceptions.propagate(e);
                }
            default:
                throw new IllegalStateException("Response constructor with expected parameters not found.");
        }
    }
}
