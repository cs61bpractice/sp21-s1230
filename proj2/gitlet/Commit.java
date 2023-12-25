package gitlet;
import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;


/** Represents a gitlet commit object.
 *
 * serves as the key dot in the DAG graph structured;
 * captures the current snapshot of all the files and respective contents when this new commit is created
 *
 *  @author Grebeth.P
 */
public class Commit implements Serializable {

    // The message of this Commit.
    private String commitMsg;
    // commit time
    private String commitTime;
    // first parent's SHA1 ID - reference to the blob
    private List<String> parents;
    // a map from filepath to its file/blob ID
    private HashMap<String, String> blobs;
    // own commit SHA1 ID later to generated based on the 4 parts above
    private String commitID;
    private File commitF; // pointer pointing to the file of this commit object

    /**
     * Creates a dog object with the specified parameters.
     * @param// commitMsg commit message
     * // commitTime: time of the new commit
     * // files: the files' ID that this commit is tracking
     */
    public Commit() {
        this.commitMsg = "initial commit";
        this.commitTime = generateCommitTime();
        this.parents = new ArrayList<>();
        this.blobs = new HashMap<>();
        this.commitID = generateCommitID();
    }

    public Commit(String commitMsg, List<String> parents, HashMap<String, String> blobs) {
        this.commitMsg = commitMsg;
        this.commitTime = generateCommitTime();
        this.parents = parents;
        this.blobs = blobs;
        this.commitID = generateCommitID();
    }

    private String generateCommitTime() {
        String pattern = "EEE MMM d HH:mm:ss yyyy Z";
        DateFormat df = new SimpleDateFormat(pattern, Locale.US);
        return df.format(new Date());
    }

    public String getCommitTime() {
        return commitTime;
    }

    public String getCommitMsg() {
        return commitMsg;
    }

    public List<String> getParents() {
        return parents;
    }

    public HashMap<String, String> getBlobs() {
        return blobs;
    }

    public String generateCommitID() {
        return Utils.sha1(commitMsg, commitTime, parents.toString(), blobs.toString());
    }

    public String getCommitID() {
        return commitID;
    }

    public void saveCommit() {
        File outFile = getObjectFilebyID(commitID);
        writeObject(outFile, this);
    }

}
