package com.anuchandy.learnings.dynamicinvoke;

import com.azure.core.http.rest.Response;

import java.lang.reflect.Constructor;
import java.util.Optional;

public interface ResponseCreatorReflection {
    Optional<Constructor<? extends Response<?>>> locateResponseReflectionCtr(Class<? extends Response<?>> responseClass);
    Response<?> invokeCreateResponseInstance(Constructor<? extends Response<?>> constructor,
                                             TestData.Input input);
}
