package rkernel.utils.file;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;
import rkernel.exception.FileManagerException;
import rkernel.signal.SignalRegistry;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileManager {

    protected File targetFolder;

    protected static FileManager instance;

    private FileManager(File folder) throws FileManagerException {
        if (folder.exists() && !folder.isDirectory()) {
            throw new FileManagerException(700, "The parameter should be a folder");
        }
        this.targetFolder = folder;
    }
    public static FileManager getInstance(File folder) throws FileManagerException {
        if (instance == null){
            instance = new FileManager(folder);
        }
        return instance;
    }

    public File[] getFiles(){
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }
        return targetFolder.listFiles((dir, name) -> name.endsWith(".jar"));
    }

    public Object getFileContent(Path filePath) throws IOException {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(SignalRegistry.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            return unmarshaller.unmarshal(filePath.toFile());
        } catch (JAXBException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean pathExist (Path path){
        return Files.exists(path);
    }

    public void writeFileContent(SignalRegistry registry, Path filePath) throws IOException {
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(SignalRegistry.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(registry, filePath.toFile());
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
