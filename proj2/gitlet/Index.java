package gitlet;
import static gitlet.Repository.*;
import static gitlet.Utils.*;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;


/** Represents a gitlet index / staging area,
 * it's an object built up to easily edit the index file
 * but not like Blob / Commit serving as key components in the DAG graph
 *  @author Grebeth.P
 */
public class Index implements Serializable {
    // two hashmaps are utilized to save to_add files and to_remove files for easier retrieving
    private HashMap<String, String> stagedToAddFiles = new HashMap<>();
    private HashMap<String, String> stagedToRemoveFiles = new HashMap<>();

    public void stageToAdd(Blob b) {
        stagedToAddFiles.put(b.getFilePath(), b.getID());
    }

    public void removeFromStagedToAdd(String filePath) {
        stagedToAddFiles.remove(filePath);
    }

    public void stagedToRemove(Blob b) {
        restrictedDelete(b.getFileInCWD());
        stagedToRemoveFiles.put(b.getFilePath(), b.getID());
    }

    public void removeFromStagedToRemove(String filePath) {
        stagedToRemoveFiles.remove(filePath);
    }

    public HashMap<String, String> getStagedToAdd() {
        return new HashMap<>(stagedToAddFiles);
    }

    public HashMap<String, String> getStagedToRemove() {
        return new HashMap<>(stagedToRemoveFiles);
    }

    // save the index by serialization to index file in .gitlet
    public void saveIndex() {
        File outFile = Utils.join(GITLET_DIR, "index");
        writeObject(outFile, this);
    }

    public void clearStagingArea() {
        stagedToAddFiles.clear();
        stagedToRemoveFiles.clear();
    }

}
