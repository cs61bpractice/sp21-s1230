package gitlet;

// TODO: any imports you need here

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;import static gitlet.myUtils.*;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import gitlet.Utils.*;

import static gitlet.Repository.HEADS_DIR;
import static gitlet.Utils.*;
import static gitlet.myUtils.*;


/** Represents a gitlet commit object.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    // The message of this Commit.
    private String commitMsg;
    // commit time
    private Date commitTime;
    // first parent's SHA1 ID - reference to the blob
    private List<String> parents;
    // a map from filepath to its file/blob ID
    private HashMap<String, String> blobs;
    // own commit SHA1 ID later to generated based on the 4 parts above
    private String commitID;
    private File commitF; // pointer pointing to the file of this commit object

    /* TODO: fill in the rest of this class. */

    /**
     * Creates a dog object with the specified parameters.
     * @param commitMsg commit message
     * // commitTime: time of the new commit
     * // files: the files' ID that this commit is tracking
     */
    public Commit(String commitMsg) {
        this.commitMsg = commitMsg;
        this.commitTime = getCommitTime();
        this.parents = new ArrayList<>();
        this.blobs = getPreviousBlobsMap();
    }

    private Date getCommitTime() {
        return new Date();
    }

    public void addParent(File f) {
        // return ID reference for parent commit
        parents.add(readContentsAsString(f));
    }

    public List<String> getParents() {
        return parents;
    }

    public String getFilePath() {
        return commitF.getPath();
    }

    private HashMap<String, String> getPreviousBlobsMap() {
        if (Repository.HEAD.exists()) {
            String currentCommitID = readContentsAsString(Repository.HEAD); // == parents.get(0)
            File currentCommitFile = getObjectFilebyID(currentCommitID);
            Commit currentCommit = readObject(currentCommitFile, Commit.class);
            return currentCommit.getBlobs();
        } else {return new HashMap<>(); }
    }

    // update files for only one parent
    public void updateBlobsReferences() {
        Index stagingArea = readObject(Repository.index, Index.class); // get index file
        for (String i: stagingArea.stagedFiles.keySet()) {
            blobs.put(i, stagingArea.stagedFiles.get(i)); // update + add if any changes in staged
        }
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public void generateCommitIDAndWriteIn() {
        commitID = sha1(this);
    }

    public String getCommitID() {
        return commitID;
    }

    public void saveHEAD() {
        // save most current commit to head
        writeContents(Repository.HEAD, commitID);
    }

    public void saveBranchHead(String branchName) {
        File branchHead = new File(HEADS_DIR, branchName);
        writeContents(branchHead, commitID);
    }

    private void clearStagingArea () {
        blobs.clear();
    }



}
