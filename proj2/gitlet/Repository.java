package gitlet;

import java.io.File;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

// TODO: any imports you need here

import static gitlet.Utils.*;
import static gitlet.Utils.readContents;
import static gitlet.myUtils.*;
import static gitlet.myUtils.asSortedList;

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
            System.out.println("A Gitlet version-control system already exists in the current directory");
            System.exit(0);
        }
        setupPersistence();
        Commit currCommit = initCommit();
        currCommit.saveCommit();
        initHEAD();
        initOrUpdateHeads(currCommit);
        initIndex();
    }

    private static Commit initCommit() {
        currCommit = new Commit();
        return currCommit;
    }

    private static void initHEAD() {
        writeContents(HEAD, "master");
    }

    private static void initOrUpdateHeads(Commit currCommit) {
        File branchHead = new File(HEADS_DIR, getCurrBranch());
        writeContents(branchHead, currCommit.getCommitID());
    }

    private static void initIndex() {
        writeObject(index, new Index());
    }

    private static String getCurrBranch() {
        return readContentsAsString(HEAD);
    }

    private static String getCurrCommitID() {
        return readContentsAsString(join(HEADS_DIR, getCurrBranch()));
    }
    private static Commit getCurrCommit() {
        return getObjectbyID(getCurrCommitID(), Commit.class);
    }

    public static void addToStage(String fileName) {
        File f = join(CWD, fileName);
        checkFileExist(f, "add");

        Blob b = new Blob(f);
        b.saveBlob();
        Index stagedArea = getStagedArea();
        HashMap<String, String> commitFileMap = getCurrCommit().getBlobs();
        if (commitFileMap.containsKey(b.getFilePath()) && commitFileMap.get(b.getFilePath()).equals(b.getID())) {
            removeEntry(stagedArea.stagedToAddFiles, b.getFilePath());
            removeEntry(stagedArea.stagedToRemoveFiles, b.getFilePath());
        } else if (!removeEntry(stagedArea.stagedToRemoveFiles, b.getFilePath())) {
            stagedArea.addFile(b);
        }
        stagedArea.saveIndex();
    }

    private static Boolean removeEntry(HashMap<String, String> map, String key) {
        if (map.containsKey(key)){
            map.remove(key);
            return true;
        }
        return false;
    }

    public static void test(String to_print) {
        if (to_print.equals("index")) {
            System.out.println(getStagedArea().stagedToAddFiles); // own test
            System.out.println(getStagedArea().stagedToRemoveFiles); // own test
        } else if (to_print.equals("currCommit")) {
            System.out.println(getCurrCommit().getBlobs()); // own test
            System.out.println(readContentsAsString(join(HEADS_DIR, getCurrBranch()))); // own test
        }
    }

    private static Index getStagedArea() {return readObject(index, Index.class); }

    public static void newCommit(String commitMsg) {

        // abort if the staging area is clear
        Index stagedArea = getStagedArea();
        if (stagedArea.stagedToAddFiles.isEmpty() && stagedArea.stagedToRemoveFiles.isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (commitMsg.isEmpty()) {
            // abort if the commit msg is blank
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Commit c = new Commit(commitMsg, CalculateParents(), calculateBlobs());
        c.saveCommit();
        initOrUpdateHeads(c);
        clearStagedArea();
    }

    private static List<String> CalculateParents() {
        List<String> parents = new ArrayList<>();
        currCommit = getCurrCommit();
        parents.add(currCommit.getCommitID());
        return parents;
    }

    private static HashMap<String, String> calculateBlobs() {
        HashMap<String, String> blobs = getCurrCommit().getBlobs();
        Index stagedArea = getStagedArea();
        for (String i: stagedArea.stagedToAddFiles.keySet()) {
            blobs.put(i, stagedArea.stagedToAddFiles.get(i)); // update + add if any changes in staged
        }
        for (String j: stagedArea.stagedToRemoveFiles.keySet()) {
            blobs.remove(j); // remove files which are staged
        }
        return blobs;
    }

    private static void clearStagedArea() {
        Index stagedArea = getStagedArea();
        stagedArea.clearStagingArea();
        stagedArea.saveIndex();
    }

    public static void removeFile(String fileName) {
        File f = join(CWD, fileName);
        Index stagedArea = getStagedArea();
        currCommit = getCurrCommit();

        if (stagedArea.stagedToAddFiles.containsKey(f.getPath())) {
            stagedArea.stagedToAddFiles.remove(f.getPath());
        } else if (currCommit.getBlobs().containsKey(f.getPath())) {
            Blob b = readObject(myUtils.getObjectFilebyID(currCommit.getBlobs().get(f.getPath())), Blob.class);
            stagedArea.removeFile(b);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        stagedArea.saveIndex();
    }

    public static void displayLog() {
        currCommit = getCurrCommit();
        List<String> firstParents = currCommit.getParents();
        Commit commitToDisplay = currCommit;
        while (commitToDisplay != null) {
            printLog(commitToDisplay);
            if (commitToDisplay.getParents().isEmpty()) {commitToDisplay = null; }
            else {commitToDisplay = getObjectbyID(commitToDisplay.getParents().get(0), Commit.class);}
        }
    }

    private static void printLog(Commit commitToDisplay) {
        System.out.println("===");
        System.out.printf("commit %s%n", commitToDisplay.getCommitID());
        System.out.printf("Date: %s%n", commitToDisplay.getCommitTime());
        System.out.println(commitToDisplay.getCommitMsg());
        System.out.println();
    }

    public static void displayGlobalLog() {
        for (File folder: OBJECT_DIR.listFiles()) {
            for (String ID: plainFilenamesIn(folder)) {
                File f = join(folder, ID);
                try {
                    Commit c = readObject(f, Commit.class);
                    printLog(c);
                } catch (Exception ignore) {}
            }
        }
    }

    public static void findCommitsWithMsg(String commitMsg) {
        List<String> commitIdList = new ArrayList<>();
        for (File folder: OBJECT_DIR.listFiles()) {
            for (String ID: plainFilenamesIn(folder)) {
                File f = join(folder, ID);
                try {
                    Commit c = readObject(f, Commit.class);
                    if (c.getCommitMsg().equals(commitMsg)) {commitIdList.add(c.getCommitID()); }
                } catch (Exception ignore) {}
            }
        }
        printCommitIDList(commitIdList);
    }

    private static void printCommitIDList(List<String> commitIdList) {
        if (commitIdList.isEmpty()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        } else {
            for (String ID: commitIdList) {System.out.println(ID);}
        }
    }

    public static void displayStatus() {
        System.out.println("=== Branches ===");
        displayBranches();
        System.out.println();

        System.out.println("=== Staged Files ===");
        displayStagedFiles(getStagedArea().stagedToAddFiles);
        System.out.println();

        System.out.println("=== Removed Files ===");
        displayStagedFiles(getStagedArea().stagedToRemoveFiles);
        System.out.println();

        System.out.println("=== Modifications Not Staged For Commit ===");
        printUnstagedFiles();
        System.out.println();

        System.out.println("=== Untracked Files ===");
        displayUntrackedFiles();
        System.out.println();
    }

    private static void displayUntrackedFiles() {
        List<String> res = new ArrayList<>();
        for (File f: CWD.listFiles()) {
            if (!getStagedArea().stagedToAddFiles.containsKey(f.getPath())
                    && !getStagedArea().stagedToRemoveFiles.containsKey(f.getPath())
                    && !getCurrCommit().getBlobs().containsKey(f.getPath())
                    && !f.isDirectory()) {
                res.add(f.getName());
                // System.out.printf("test: %s%n", CWD); // test statement
            }
        }
        Collections.sort(res);
        for (String fileName: res) {System.out.println(fileName);}
    }

    private static void displayBranches() {
        String currBranch = getCurrBranch();
        for (String branch: plainFilenamesIn(HEADS_DIR)) {
            if (currBranch.equals(branch)) {
                System.out.printf("*%s%n", currBranch);
            } else {System.out.println(branch); }
        }
    }

    private static void displayStagedFiles(HashMap<String, String> files) {
        printFileNamesfromPaths(asSortedList(files.keySet()));
    }

    private static void printFileNamesfromPaths(List<String> l) {
        for (String filePath: l) {System.out.println(getFileNameFromPath(filePath)); }
    }

    private static void printUnstagedFiles() {
        printFileNamesfromPaths(getUnstagedFiles());
    }

    private static List<String> getUnstagedFiles() {
        List<String> res = getUnstagedFilesfromHashmap(getStagedArea().stagedToAddFiles);
        for (String entry: getUnstagedFilesfromHashmap(getCurrCommit().getBlobs())) {
            String[] tempParts = entry.split(Pattern.quote(" "));
            String filePath = tempParts[0];
            if (!getStagedArea().stagedToRemoveFiles.containsKey(filePath)
                    && !getStagedArea().stagedToAddFiles.containsKey(filePath)) {
                res.add(filePath+" "+tempParts[1]);
            }
        }
        Collections.sort(res);
        return res;
    }

    // a helper function for above to shorten codes
    private static List<String> getUnstagedFilesfromHashmap(HashMap<String, String> Map) {
        List<String> res = new ArrayList<>();
        for (Map.Entry<String, String> entry: Map.entrySet()) {
            String filePath = entry.getKey();
            String fileContent = entry.getValue();
            File fileInCWD = new File(filePath);
            // System.out.printf("test: %s%n", fileInCWD); // test line

            // if the file does not exist in CWD or the version is diff with CWD version
            if (!fileInCWD.exists()) {
                res.add(filePath+" (deleted)");
            } else {
                String fileContentInCWD = sha1(filePath, readContents(fileInCWD));
                if (!fileContent.equals(fileContentInCWD)) {
                    res.add(filePath+" (modified)");
                }
            }
        }
        return res;
    }

    public static void checkoutToFile(String fileName) {
        File f = join(CWD, fileName);
        checkFileExistInCommit(f, getCurrCommit());

        rewriteContentforCheckoutToFile(getCurrCommit(), f);
    }

    private static void checkFileExistInCommit(File f, Commit c) {
        if (!c.getBlobs().containsKey(f.getPath())) {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    private static void checkCommitExistwithID(String ID) {
        if (!getObjectFilebyID(ID).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    private static String getBlobIDbyFile(Commit c, File f) {
        return c.getBlobs().get(f.getPath());
    }

    public static void checkoutToCommitsFile(String ID, String fileName) {
        File f = join(CWD, fileName);
        checkCommitExistwithID(ID);
        Commit c = getObjectbyID(ID, Commit.class);
        checkFileExistInCommit(f, c);

        rewriteContentforCheckoutToFile(c, f);
    }

    private static void rewriteContentforCheckoutToFile(Commit c, File f) {
        Blob oldBlob = getObjectbyID(getBlobIDbyFile(c, f), Blob.class);
        writeContents(f, oldBlob.getContent());
    }

    public static void checkoutToBranch(String branchName) {
        File branch = join(HEADS_DIR, branchName);
        checkFileExist(branch, "checkout-branch");
        checkBranchiscurrBranch(branchName, "checkout");

        String ID = readContentsAsString(branch);
        //System.out.println(ID);//own test
        Commit c = getObjectbyID(ID, Commit.class);
        //System.out.println(c.getBlobs()); // own test
        checkPossibleRewritesToUntrackedFile(c);

        changeToCommit(c);
        changeBranchTo(branchName);
        clearStagedArea();
    }

    private static void changeToCommit(Commit c) {
        for (String filePath: c.getBlobs().keySet()) {
            rewriteContentforCheckoutToFile(c, new File(filePath));
        } // rewrite + add files

        for (String filePath: getCurrCommit().getBlobs().keySet()) {
            if (!c.getBlobs().containsKey(filePath)) {
                restrictedDelete(new File(filePath));
            }
        }
    }

    private static void changeBranchTo(String branchName) {
        writeContents(HEAD, branchName);
    }

    private static void checkPossibleRewritesToUntrackedFile(Commit c) {
        for (File f: CWD.listFiles()) {
            if (c.getBlobs().containsKey(f.getPath())
                    && !getCurrCommit().getBlobs().containsKey(f.getPath())) {
                System.out.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
    }

    private static void checkFileExist(File f, String operation) {
        if (!f.exists()) {
            if (operation.equals("checkout-branch")) {
                System.out.println("No such branch exists.");
            } else if (operation.equals("add")) {
                System.out.println("File does not exist.");
            } else {
                System.out.println("A branch with that name does not exist.");
            }
            System.exit(0);
        }
    }

    private static void checkBranchiscurrBranch(String branchName, String operation) {
        if (getCurrBranch().equals(branchName)) {
            if (operation.equals("checkout")) {
                System.out.println("No need to checkout the current branch.");
            } else if (operation.equals("merge")) {
                System.out.println("Cannot merge a branch with itself.");
            } else {
                System.out.println("Cannot remove the current branch.");
            }
            System.exit(0);
        }
    }

    public static void createNewBranch(String branchName) {
        File newBranch = join(HEADS_DIR, branchName);
        branchAlrExist(newBranch);
        writeContents(newBranch, getCurrCommitID());
    }

    private static void branchAlrExist(File branch) {
        if (branch.exists()) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
    }

    public static void removeBranch(String branchName) {
        File branch = join(HEADS_DIR, branchName);
        checkFileExist(branch, "rm-branch");
        checkBranchiscurrBranch(branchName, "rm-branch");
        branch.delete();
    }

    public static void resetToCommit(String commitID) {
        Commit c = getObjectbyID(commitID, Commit.class);
        checkPossibleRewritesToUntrackedFile(c);

        changeToCommit(c);
        changeBranchHeadToGivenCommit(readContentsAsString(HEAD), commitID);
        clearStagedArea();
    }

    private static void changeBranchHeadToGivenCommit(String branch, String commitID) {
        writeContents(join(HEADS_DIR, branch), commitID);
    }

    public static void mergeToBranch(String branchName) {
        checkUncommitedChanges();
        File tbranch = join(HEADS_DIR, branchName);
        checkFileExist(tbranch, "merge");
        checkBranchiscurrBranch(branchName, "merge");
        Commit mCommit = readObject(tbranch, Commit.class);
        checkPossibleRewritesToUntrackedFile(mCommit);
    }

    private static Commit findSplitPoint(File branch) {
        Commit c1 = getCurrCommit();
        Commit c2 = getObjectbyID(readContentsAsString(branch), Commit.class);

        HashSet<String> set = listOfCommitsWithDepth(c1);
        ArrayList<Commit> level = new ArrayList<>();
        level.add(c2);
        while (!level.isEmpty()) {
            ArrayList<Commit> new_level = new ArrayList<>();
            for (Commit c: level) {
                if (set.contains(c)) {
                    return c;
                } else {
                    for (String id: c.getParents()) {
                        Commit p = getObjectbyID(id, Commit.class);
                        new_level.add(p);
                    }
                }
            }
            level = new_level;
        }
        return new Commit(); // will not be incur
    }

    private static HashSet<String> listOfCommitsWithDepth(Commit c) {
        HashSet<String> set = new HashSet<>();
        Queue<Commit> q = new LinkedList<Commit>();
        q.add(c);
        while (!q.isEmpty()) {
            Commit currC = q.remove();
            set.add(currC.getCommitID());
            for (String id: currC.getParents()) {
                Commit p = getObjectbyID(id, Commit.class);
                q.add(p);
            }
        }
        return set;
    }

    private static void checkUncommitedChanges() {
        if (!getStagedArea().stagedToRemoveFiles.isEmpty()
                || !getStagedArea().stagedToAddFiles.isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }
}
