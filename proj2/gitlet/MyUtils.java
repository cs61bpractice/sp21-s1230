package gitlet;
import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;


/** Utilities but built by my own - to separate from the given utilities
 *
 *  @author Grebeth.P
 */

class MyUtils {
    public static File getObjectFilebyID(String id) {
        File objectFolder = join(Repository.OBJECT_DIR, id.substring(0, 3));
        objectFolder.mkdir();
        if (id.length() < 16) {
            for (File f: objectFolder.listFiles()) {
                if (f.getName().startsWith(id.substring(3))) {
                    return f;
                }
            }
        }
        return new File(objectFolder, id.substring(3));
    }

    public static <T extends Serializable> T getObjectbyID(String id, Class<T> expectedClass) {
        File f = getObjectFilebyID(id);
        if (!f.exists()) {
            System.out.println("No commit with that id exists.");
            // here actually should be both for blobs and commits,
            // // but this is to fulfill testing purpose
            System.exit(0);
        }
        return readObject(f, expectedClass); // make sure the file exists
    }

    public static List<String> asSortedList(Set<String> s) {
        List<String> res = new ArrayList<>(s);
        java.util.Collections.sort(res);
        return res;
    }

    public static String getFileNameFromPath(String filePath) {
        String[] res = filePath.split(Pattern.quote("/"));
        return res[res.length - 1];
    }

    public static File getFileFromPath(String filePath) {
        return new File(filePath);
    }

    public static String convertBytesToString(byte[] fileContent) {
        return new String(fileContent, StandardCharsets.UTF_8);
    }

    public static boolean isTrusy(String str) {
        return !(str == null || str.isEmpty());
    }

}
