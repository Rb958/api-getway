package rkernel.utils.file;

import java.util.EventListener;

public interface FileListener extends EventListener {

    void onCreateFile(FileEvent event);

    void onDeleteFile(FileEvent event);

    void onModifyFile(FileEvent event);
}
