package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;

import static gitlet.Utils.*;

class myUtils {
    public static File getObjectFilebyID(String ID) {
        File objectFolder = join(Repository.OBJECT_DIR, ID.substring(0,3));
        objectFolder.mkdir();
        return new File(objectFolder, ID.substring(3));
    }

    public static <T extends Serializable> T getObjectbyID(String ID, Class<T> expectedClass) {
        File f = getObjectFilebyID(ID);
        return readObject(f, expectedClass);
    }

}