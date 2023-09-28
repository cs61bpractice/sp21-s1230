package gitlet;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import static gitlet.Utils.*;

class myUtils {
    public static File getObjectFilebyID(String ID) {
        File objectFolder = join(Repository.OBJECT_DIR, ID.substring(0,3));
        objectFolder.mkdir();
        return new File(objectFolder, ID.substring(3));
    }

    public static <T extends Serializable> T getObjectbyID(String ID, Class<T> expectedClass) {
        File f = getObjectFilebyID(ID);
        return readObject(f, expectedClass); // makesure the file exists
    }

    public static List<String> asSortedList(Set<String> s) {
        List<String> res = new ArrayList<>(s);
        java.util.Collections.sort(res);
        return res;
    }

    public static String getFileNameFromPath(String filePath) {
        String[] res = filePath.split(Pattern.quote("\\"));
        return res[res.length-1];
    }

}