package me.dags.installer.process;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author dags <dags@dags.me>
 */
public interface IOTask extends Closeable {

    void process() throws IOException;
}
