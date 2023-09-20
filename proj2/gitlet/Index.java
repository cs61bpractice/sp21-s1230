package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.myUtils.*;
import gitlet.Blob.*;
import gitlet.Commit.*;

/** Represents a gitlet index / staging area.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Index implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Index class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used.
     */

    // a map from filepath to its file/blob ID
    public HashMap<String, String> stagedToAddFiles;
    public HashMap<String, String> stagedToRemoveFiles;

    /* TODO: fill in the rest of this class. */

    public void addFile(Blob b) {
        stagedToAddFiles.put(b.getFilePath(), b.getID());
    }

    public void removeFile(Blob b) {
        restrictedDelete(b.getFileInCWD());
        stagedToRemoveFiles.put(b.getFilePath(), b.getID());
    }

    // save the index by serialization to index file in .gitlet
    public void saveIndex() {
        File outFile = Utils.join(GITLET_DIR, "index");
        writeObject(outFile, this);
    }

    public void clearStagingArea () {
        stagedToAddFiles.clear();
        stagedToRemoveFiles.clear();
    }







}