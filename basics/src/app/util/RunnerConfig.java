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


    public RunnerConfig<T> print() {
        this.toPrint = true;
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

    public RunnerConfig<T> error() {
        this.showError = true;
        return this;
    }

    public RunnerConfig<T> throwing() {
        this.throwError = true;
        return this;
    }


    public T run() throws Exception {
        if (null != this.identifier) System.out.println(identifier);
        long currentTime = System.currentTimeMillis();
        T obj = null;
        try {
            if (null != this.runnable) runnable.run();
            else {
                obj = this.callable.call();
                System.out.println(obj);
            }
        } catch (Exception exception) {
            if (this.showError) System.out.println(exception.getMessage());
            if (this.showStacktrace) exception.printStackTrace();
            if (throwError) throw exception;
        }
        if (this.checkTime)
            System.out.println((this.timeIdentifier != null ? this.timeIdentifier : "it is")
                    + " taking " + (System.currentTimeMillis() - currentTime) + " milliseconds");
        return obj;
    }

    public static <T> RunnerConfig<T> code(CallableV2<T> callable) {
        return new RunnerConfig<>(callable);
    }


    public static <T> RunnerConfig<T> code(RunnableV2 runnable) {
        return new RunnerConfig<>(runnable);
    }

}
