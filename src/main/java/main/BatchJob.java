package main;

import java.io.File;
import java.nio.file.Path;

public class BatchJob {
    private Path data;
    private Path control;
    private int fraglen;

    public BatchJob(String path_data, String path_control, String fraglen) {
        this.data = new File(path_data).toPath();
        this.control = new File(path_control).toPath();
        this.fraglen = Integer.parseInt(fraglen);
    }

    public Path getData() {
        return data;
    }

    public Path getControl() {
        return control;
    }

    public int getFraglen() {
        return fraglen;
    }
}
