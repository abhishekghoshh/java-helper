package app.util;

import java.util.Objects;

public class RunnerConfig<T> {

    public interface RunnableV2 {
        void run() throws Exception;
    }

    public interface CallableV2<V> {
        V call() throws Exception;
    }

    CallableV2<T> callable = null;
    RunnableV2 runnable = null;
    boolean toPrint = false;
    boolean checkTime = false;
    String timeIdentifier = null;
    boolean showStacktrace = false;
    boolean showError = false;
    boolean throwError = false;
    String identifier = null;

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
            if (this.showError) System.err.println(exception.getMessage());
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

