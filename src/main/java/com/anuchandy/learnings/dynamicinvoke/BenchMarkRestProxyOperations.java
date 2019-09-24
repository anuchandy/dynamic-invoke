package com.anuchandy.learnings.dynamicinvoke;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.util.TypeUtil;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Constructor;
import java.util.Optional;


public class BenchMarkRestProxyOperations {
    private TestData testData = new TestData();
    private static final int ITERATIONS = 5000 * 500;

    public static void main(String[] args) {
        BenchMarkRestProxyOperations op = new BenchMarkRestProxyOperations();
//        op.reflectionInvokeNonCached();
//        op.reflectionInvokeCached();
//        op.methodHandleInvokeCached();
//        op.lambdaMetaInvokeCached();
    }

    private void lambdaMetaInvokeCached() {
        ResponseCreatorLambdaMetaCached responseCreator = new ResponseCreatorLambdaMetaCached();
        for (int i = 0; i< ITERATIONS; i++) {
            for (int j = 0; j < testData.inputs.length; j++) {
                locateResponseAndInvoke(responseCreator, testData.inputs[j]);
            }
        }
    }

    private void methodHandleInvokeCached() {
        ResponseCreatorMethodHandleCached responseCreator = new ResponseCreatorMethodHandleCached();
        for (int i = 0; i< ITERATIONS; i++) {
            for (int j = 0; j < testData.inputs.length; j++) {
                locateResponseAndInvoke(responseCreator, testData.inputs[j]);
            }
        }
    }

    private void reflectionInvokeCached() {
        ResponseCreatorReflection responseCreator = new ResponseCreatorReflectionCached();
        for (int i = 0; i< ITERATIONS; i++) {
            for (int j = 0; j < testData.inputs.length; j++) {
                locateResponseAndInvoke(responseCreator, testData.inputs[j]);
            }
        }
    }

    private void reflectionInvokeNonCached() {
        ResponseCreatorReflection responseCreator = new ResponseCreatorReflectionNonCached();
        for (int i = 0; i< ITERATIONS; i++) {
            for (int j = 0; j < testData.inputs.length; j++) {
                locateResponseAndInvoke(responseCreator, testData.inputs[j]);
            }
        }
    }

    private void locateResponseAndInvoke(ResponseCreatorLambdaMetaCached responseCreator, TestData.Input input) {
        //
        Class<? extends Response<?>> responseClass =
                (Class<? extends Response<?>>) TypeUtil.getRawClass(input.returnType);
        //
        // Step1: Locate Constructor using LambdaMetaFactory.
        Optional<ResponseCreatorLambdaMetaCached.Entry> optionalConstructor = responseCreator.locateResponseLambdaMetaCtr(responseClass);
        if (!optionalConstructor.isPresent()) {
            throw new IllegalStateException("Response constructor with expected parameters not found.");
        }
        // Step2: Invoke Constructor using LambdaMetaFactory functional interface.
        Response<?> response = responseCreator.invokeCreateResponseInstance(optionalConstructor.get(),
                input);
        //
        System.out.println(response.getValue());
    }

    private void locateResponseAndInvoke(ResponseCreatorMethodHandleCached responseCreator, TestData.Input input) {
        //
        Class<? extends Response<?>> responseClass =
                (Class<? extends Response<?>>) TypeUtil.getRawClass(input.returnType);
        //
        // Step1: Locate Constructor MethodHandle.
        Optional<MethodHandle> optionalMethodHandle = responseCreator.locateResponseMethodHandleCtr(responseClass);
        if (!optionalMethodHandle.isPresent()) {
            throw new IllegalStateException("Response constructor with expected parameters not found.");
        }
        // Step2: Invoke Constructor using MethodHandle.
        Response<?> response = responseCreator.invokeCreateResponseInstance(optionalMethodHandle.get(),
                input);
        //
        System.out.println(response.getValue());
    }


    private void locateResponseAndInvoke(ResponseCreatorReflection responseCreator, TestData.Input input) {
        //
        Class<? extends Response<?>> responseClass =
                (Class<? extends Response<?>>) TypeUtil.getRawClass(input.returnType);
        //
        // Step1: Locate Constructor reflect Type.
        Optional<Constructor<? extends Response<?>>> optionalConstructor = responseCreator.locateResponseReflectionCtr(responseClass);
        if (!optionalConstructor.isPresent()) {
            throw new IllegalStateException("Response constructor with expected parameters not found.");
        }
        // Step2: Invoke Constructor using reflect Type.
        Response<?> response = responseCreator.invokeCreateResponseInstance(optionalConstructor.get(),
                input);
        //
        System.out.println(response.getValue());
    }
}