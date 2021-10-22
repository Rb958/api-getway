package rkernel.utils.file;

import java.io.File;
import java.util.EventObject;

public class FileEvent extends EventObject {
    public FileEvent(File source) {
        super(source);
    }

    public File getFile(){
        return (File) getSource();
    }
}
