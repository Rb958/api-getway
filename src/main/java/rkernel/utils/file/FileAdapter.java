package rkernel.utils.file;

public abstract class FileAdapter implements FileListener{
    @Override
    public void onCreateFile(FileEvent event) {}

    @Override
    public void onDeleteFile(FileEvent event) {}

    @Override
    public void onModifyFile(FileEvent event) {}
}
