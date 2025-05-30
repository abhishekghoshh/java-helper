package io.github.abhishekghoshh.runner;

import java.util.Objects;

public class RunnerConfig<T> {



    CallableV2<T> callable = null;
    RunnableV2 runnable = null;
    boolean toPrint = false;
    boolean checkTime = false;
    String timeIdentifier = null;
    boolean showStacktrace = false;
    boolean showError = false;
    boolean throwError = false;
    String identifier = null;

    private String methodName = null;
    private boolean printMethodName = false;

    public RunnerConfig(CallableV2<T> callable) {
        Objects.requireNonNull(callable);
        this.callable = callable;
    }

    public RunnerConfig(RunnableV2 runnable) {
        Objects.requireNonNull(runnable);
        this.runnable = runnable;
    }

    public RunnerConfig<T> id(String id) {
        Objects.requireNonNull(id);
        this.identifier = id;
        return this;
    }

    public RunnerConfig<T> print(boolean print) {
        this.toPrint = print;
        return this;
    }
    public RunnerConfig<T> print() {
        this.toPrint = true;
        return this;
    }

    public RunnerConfig<T> timer(boolean timer, String timeIdentifier) {
        this.checkTime = timer;
        this.timeIdentifier = timeIdentifier;
        return this;
    }
    public RunnerConfig<T> timer() {
        return timer(null);
    }

    public RunnerConfig<T> timer(String timeIdentifier) {
        if (null != timeIdentifier && !timeIdentifier.isEmpty())
            this.timeIdentifier = timeIdentifier;
        this.checkTime = true;
        return this;
    }

    public RunnerConfig<T> error(boolean showError) {
        this.showError = showError;
        return this;
    }

    public RunnerConfig<T> error() {
        this.showError = true;
        return this;
    }

    public RunnerConfig<T> showStackTrace(boolean showStacktrace) {
        this.showStacktrace = showStacktrace;
        return this;
    }

    public RunnerConfig<T> throwing(boolean throwError) {
        this.throwError = throwError;
        return this;
    }
    public RunnerConfig<T> throwing() {
        this.throwError = true;
        return this;
    }

    public T run(boolean shouldRun) throws Exception {
        if (!shouldRun) return null;
        return run();
    }

    public RunnerConfig<T> methodName(String methodName) {
        Objects.requireNonNull(methodName);
        this.methodName = methodName;
        return this;
    }

    public String methodName() {
        return this.methodName;
    }

    public RunnerConfig<T> printMethodName(boolean toPrint) {
        this.printMethodName = toPrint;
        return this;
    }

    public boolean printMethodName() {
        return printMethodName;
    }



    public T run() throws Exception {
        if (null != this.identifier && !this.identifier.isBlank())
            System.out.println(identifier);
        long currentTime = System.currentTimeMillis();
        T obj = null;
        try {
            if (null != this.runnable) runnable.run();
            else {
                obj = this.callable.call();
                if (this.toPrint) System.out.println(obj);
            }
        } catch (Exception exception) {
            if (this.showError) System.out.println("Exception is : " + exception.getCause().getMessage());
            if (this.showStacktrace) exception.printStackTrace();
            if (throwError) throw exception;
        }
        if (this.checkTime) {
            long timeTaken = System.currentTimeMillis() - currentTime;
            String timeIdentifier = this.timeIdentifier != null ? this.timeIdentifier : "it is";
            System.out.printf("%s taking %d milliseconds%n", timeIdentifier, timeTaken);
        }
        return obj;
    }
}

