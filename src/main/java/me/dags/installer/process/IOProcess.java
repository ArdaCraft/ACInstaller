package me.dags.installer.process;

import me.dags.installer.Installer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author dags <dags@dags.me>
 */
public abstract class IOProcess implements Runnable {

    private final Consumer<Integer> attemptConsumer;
    private final Consumer<Integer> lengthConsumer;
    private final Consumer<Integer> progressConsumer;
    private final Consumer<Path> completionConsumer;
    private final int attempts;
    private final long attemptInterval;

    private volatile boolean complete = false;

    IOProcess(Builder<?> builder) {
        this.attemptConsumer = builder.attemptConsumer;
        this.lengthConsumer = builder.lengthConsumer;
        this.progressConsumer = builder.progressConsumer;
        this.completionConsumer = builder.completionConsumer;
        this.attempts = builder.attempts;
        this.attemptInterval = builder.attemptInterval;
    }

    @Override
    public void run() {
        int attempt = 0;
        while (!complete && attempt++ < attempts) {
            try (IOTask ioTask = process()) {
                ioTask.process();
            } catch (Exception e1) {
                Installer.logMessage("Error occurred running process: \n{}", e1.getMessage());
                attemptConsumer.accept(attempt);
                try {
                    Thread.sleep(attemptInterval);
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                }
            }
        }
        if (complete) {
            Installer.logMessage(this.getClass().getSimpleName() + " completed successfully!");
            onComplete();
        }
    }
    
    protected abstract void onComplete();

    protected abstract IOTask process();

    public boolean isComplete() {
        return complete;
    }

    protected void setComplete(boolean value) {
        this.complete = value;
    }

    protected void setLength(int value) {
        this.lengthConsumer.accept(value);
    }

    protected void updateValue(int value) {
        this.progressConsumer.accept(value);
    }

    protected void onCompletion(Path path) {
        if (Files.exists(path)) {
            completionConsumer.accept(path);
        }
    }

    public static abstract class Builder<T extends IOProcess> {

        private Consumer<Integer> attemptConsumer = i -> {};
        private Consumer<Integer> lengthConsumer = i -> {};
        private Consumer<Integer> progressConsumer = i -> {};
        private Consumer<Path> completionConsumer = p -> {};
        private int attempts = 5;
        private long attemptInterval = 100L;

        public Builder<T> attempts(int attempts) {
            this.attempts = attempts;
            return this;
        }

        public Builder<T> attemptInterval(long interval) {
            this.attemptInterval = interval;
            return this;
        }

        public Builder<T> attemptConsumer(Consumer<Integer> attemptConsumer) {
            this.attemptConsumer = attemptConsumer;
            return this;
        }

        public Builder<T> lengthConsumer(Consumer<Integer> lengthConsumer) {
            this.lengthConsumer = lengthConsumer;
            return this;
        }

        public Builder<T> progressConsumer(Consumer<Integer> progressConsumer) {
            this.progressConsumer = progressConsumer;
            return this;
        }

        public Builder<T> completionConsumer(Consumer<Path> completionConsumer) {
            this.completionConsumer = completionConsumer;
            return this;
        }

        abstract public T build();
    }
}
