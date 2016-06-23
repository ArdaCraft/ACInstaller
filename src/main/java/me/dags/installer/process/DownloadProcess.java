package me.dags.installer.process;

import me.dags.installer.Installer;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author dags <dags@dags.me>
 */
public class DownloadProcess extends IOProgress {

    private final Path outputPath;
    private final URL url;

    DownloadProcess(Builder builder) {
        super(builder);
        this.outputPath = builder.outputPath;
        this.url = builder.url;
    }

    @Override
    protected void onComplete() {
        super.onCompletion(outputPath);
    }

    @Override
    protected IOProcess process() {
        return new DownloadTask();
    }

    public static Builder builder() {
        return new Builder();
    }

    private class DownloadTask implements IOProcess {

        private Path temp;
        private HttpURLConnection connection;
        private InputStream inputStream;
        private ReadableByteChannel inputChannel;
        private FileChannel outputChannel;

        @Override
        public void process() throws IOException {
            if (Files.exists(outputPath)) {
                DownloadProcess.super.setComplete(true);
                Installer.logMessage("File exists, skipping download...");
                return;
            }

            Path parent = outputPath.getParent();
            Files.createDirectories(parent);

            temp = parent.resolve(System.currentTimeMillis() + ".tmp");
            Files.createFile(temp);

            connection = (HttpURLConnection) url.openConnection();
            inputStream = connection.getInputStream();
            inputChannel = Channels.newChannel(inputStream);
            outputChannel = FileChannel.open(temp, StandardOpenOption.WRITE);

            int length = Integer.valueOf(connection.getHeaderFields().get("Content-Length").get(0));
            int segment = length / 100;

            DownloadProcess.super.setLength(length);

            for (int i = 0; i <= 100; i++) {
                outputChannel.transferFrom(inputChannel, i * segment, segment);
                DownloadProcess.super.updateValue(i * segment);
            }

            if (Files.exists(outputPath)) {
                Files.delete(outputPath);
            }

            Files.copy(temp, outputPath);
            DownloadProcess.super.setComplete(true);
        }

        @Override
        public void close() throws IOException {
            if (connection != null) {
                connection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (inputChannel != null) {
                inputChannel.close();
            }
            if (outputChannel != null) {
                outputChannel.close();
            }
            if (temp != null && Files.exists(temp)) {
                Files.delete(temp);
            }
        }
    }

    public static class Builder extends IOProgress.Builder<DownloadProcess> {

        private URL url = null;
        private Path outputPath = null;

        public Builder url(String url) {
            try {
                this.url = new URL(url);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return this;
        }

        public Builder path(Path path) {
            this.outputPath = path;
            return this;
        }

        @Override
        public DownloadProcess build() {
            return new DownloadProcess(this);
        }
    }
}
