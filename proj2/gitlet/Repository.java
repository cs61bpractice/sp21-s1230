package gitlet;

import java.io.File;
import java.nio.file.Files;

import static gitlet.Utils.*;
import static gitlet.myUtils.*;

// TODO: any imports you need here

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

    /* TODO: fill in the rest of this class. */

    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
    }

    public static void initiateGitlet() {
        if (GITLET_DIR.exists()) {
            throw new RuntimeException(
                    "A Gitlet version-control system already exists in the current directory"
            );
        }
        setupPersistence();
        newCommit("initial commit");
    }

    public static void addToStage(String fileName) {

    }

    public static void newCommit(String commitMsg) {

        // abort if the staging area is clear
        if (index.exists() || index.length() == 0) {
            throw new RuntimeException(
                    "No changes added to the commit."
            );
        } else if (commitMsg.isEmpty()) {
            // abort if the commit msg is blank
            throw new RuntimeException(
                    "Please enter a commit message."
            );
        }

        Commit c = new Commit(commitMsg);
        c.addParent(Repository.HEAD);
        c.generateCommitIDAndWriteIn();
        c.saveHEAD();
        c.saveBranchHead("master");
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
