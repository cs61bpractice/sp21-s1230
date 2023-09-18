package gitlet;

import java.io.File;
import static gitlet.Utils.*;

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

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");

    /* TODO: fill in the rest of this class. */

    public static void initiateGitlet() {

    }

    public static void addToStage(String fileName) {

    }

    public static void commitFiles(String commitMsg) {

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
