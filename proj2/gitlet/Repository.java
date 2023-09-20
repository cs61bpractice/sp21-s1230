package gitlet;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

// TODO: any imports you need here

import static gitlet.Utils.*;
import static gitlet.myUtils.*;
import gitlet.Blob.*;
import gitlet.Commit.*;
import gitlet.Index.*;

/** Represents a gitlet repository.
 *  TODO: It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * TODO: add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory.
     *  .gitlet
     *    |--objects
     *      |--commits and blobs
     *    |--refs
     *      |--heads
     *        |--branch names
     *    |--HEAD
     *    |--index
     * */

    public static final File CWD = new File(System.getProperty("user.dir"));
    // The .gitlet directory
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    // objects folder to store commits and blobs
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    // refs folder -> heads folder -> all branches,
    // each named by the branch name, content is SHA-1 ID for the head commit of each branch
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    // HEAD file storing current commit ID
    public static final File HEAD = new File(GITLET_DIR, "HEAD");
    // index file storing staging area
    public static final File index = new File(GITLET_DIR, "index");
    public static Commit currCommit;

    /* TODO: fill in the rest of this class. */

    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
    }

    public static void initiateGitlet() {
        if (GITLET_DIR.exists()) {
            throw new GitletException(
                    "A Gitlet version-control system already exists in the current directory"
            );
        }
        setupPersistence();
        initCommit();
        initHEAD();
        initOrUpdateHeads();
    }

    private static void initCommit() {
        currCommit = new Commit();
    }

    private static void initHEAD() {
        writeContents(HEAD, "master");
    }

    private static void initOrUpdateHeads() {
        File branchHead = new File(HEADS_DIR, getCurrBranch());
        writeContents(branchHead, currCommit.getCommitID());
    }

    private static String getCurrBranch() {
        return readContentsAsString(HEAD);
    }

    public static void addToStage(String fileName) {
        File f_to_add = join(CWD, fileName);
        if (!f_to_add.exists()) {
            throw new GitletException("File does not exist.");
        }

        Blob b = new Blob(f_to_add);
        Index stagedArea = readObject(index, Index.class);
        HashMap<String, String> commitFileMap = currCommit.getBlobs();
        if (commitFileMap.containsKey(b.getPath()) && commitFileMap.get(b.getPath()).equals(b.getID())) {
            if (stagedArea.stagedToAddFiles.containsKey(b.getPath())) {
                stagedArea.stagedToAddFiles.remove(b.getPath());
            }
        } else {
            if (stagedArea.stagedToRemoveFiles.containsKey(b.getPath())) {
                stagedArea.stagedToRemoveFiles.remove(b.getPath());
            }
            stagedArea.addFile(b);
        }
    }

    public static void newCommit(String commitMsg) {

        // abort if the staging area is clear
        if (index.exists() || index.length() == 0) {
            throw new GitletException("No changes added to the commit.");
        } else if (commitMsg.isEmpty()) {
            // abort if the commit msg is blank
            throw new GitletException("Please enter a commit message.");
        }

        Commit c = new Commit(commitMsg, getParents(), getblobs());
        c.saveCommit();
        initOrUpdateHeads();
        clearStagedArea();
    }

    private static List<String> getParents() {
        List<String> parents = new ArrayList<>();
        parents.add(currCommit.getCommitID());
        return parents;
    }

    private static HashMap<String, String> getblobs() {
        HashMap<String, String> blobs = currCommit.getBlobs();
        Index stagedArea = readObject(Repository.index, Index.class); // get index file
        for (String i: stagedArea.stagedToAddFiles.keySet()) {
            blobs.put(i, stagedArea.stagedToAddFiles.get(i)); // update + add if any changes in staged
        }
        for (String j: stagedArea.stagedToRemoveFiles.keySet()) {
            blobs.remove(j); // remove files which are staged
        }
        return blobs;
    }

    private static void clearStagedArea() {
        Index stagedArea = readObject(Repository.index, Index.class);
        stagedArea.clearStagingArea();
        stagedArea.saveIndex();
    }

    public static void removeFile(String fileName) {

    }

    public static void displayLog() {

    }

    public static void displayGlobalLog() {

    }

    public static void findCommitsWithMsg(String commitMsg) {

    }

    public static void displayStatus() {

    }

    public static void checkoutToFile(String fileName) {

    }

    public static void checkoutToCommitsFile(String commitID, String fileName) {

    }

    public static void checkoutToBranch(String branchName) {

    }

    public static void createNewBranch(String branchName) {

    }

    public static void removeBranch(String branchName) {

    }

    public static void resetToCommit(String commitID) {

    }

    public static void mergeToBranch(String branchName) {

    }
}
