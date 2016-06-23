package me.dags.installer.process;

import me.dags.installer.Installer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author dags <dags@dags.me>
 */
public class UnzipProcess extends IOProgress {

    private final Path zipPath;
    private final Path rootDir;

    UnzipProcess(Builder builder) {
        super(builder);
        this.zipPath = builder.zipPath;
        this.rootDir = builder.rootPath;
    }

    @Override
    protected void onComplete() {
        Installer.logMessage("Extraction complete!");
        super.onCompletion(zipPath);
    }

    @Override
    protected IOProcess process() {
        return new UnzipTask();
    }

    public static Builder builder() {
        return new Builder();
    }

    public class UnzipTask implements IOProcess {

        private ZipFile zipFile = null;

        @Override
        public void process() throws IOException {
            zipFile = new ZipFile(zipPath.toFile());
            UnzipProcess.super.setLength(zipFile.size());

            int value = 0;
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                UnzipProcess.super.updateValue(value++);

                ZipEntry e = entries.nextElement();
                if (e.isDirectory() || e.getName().startsWith(".") || e.getName().endsWith(".gitnore")) {
                    continue;
                }

                int index = e.getName().indexOf('/');
                String path = index > 0 ? e.getName().substring(index + 1) : e.getName();
                File targetFile = new File(rootDir.toFile(), path);
                targetFile.getParentFile().mkdirs();

                Installer.logMessage("Extracting file {}", targetFile);

                InputStream input = zipFile.getInputStream(e);
                ReadableByteChannel channel = Channels.newChannel(input);

                FileOutputStream output = new FileOutputStream(targetFile);
                FileChannel fileChannel = output.getChannel();
                fileChannel.transferFrom(channel, 0, Long.MAX_VALUE);

                fileChannel.close();
                channel.close();
                output.flush();
                output.close();
                input.close();
            }

            UnzipProcess.super.setComplete(true);
        }

        @Override
        public void close() throws IOException {
            if (zipFile != null) {
                zipFile.close();
            }
        }
    }

    public static class Builder extends IOProgress.Builder<UnzipProcess> {

        private Path zipPath;
        private Path rootPath;

        public Builder zip(Path path) {
            this.zipPath = path;
            return this;
        }

        public Builder target(Path root) {
            this.rootPath = root;
            return this;
        }

        @Override
        public UnzipProcess build() {
            this.attempts(1).attemptInterval(0L);
            return new UnzipProcess(this);
        }
    }
}
