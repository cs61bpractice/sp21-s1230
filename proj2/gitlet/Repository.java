package gitlet;
import static gitlet.Utils.*;
import static gitlet.Utils.readContents;
import static gitlet.MyUtils.*;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;


/** Represents a gitlet repository.
 *
 * The repository includes main algorithms and calculations compared to other classes,
 * which are objects built up with some simple private helper functions and
 * public functions for Repository to access the instances' fields
 *
 *  @author Grebeth.P
 */
public class Repository {

    /** The current working directory structure.
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
    public static final File GITLET_DIR = join(CWD, ".gitlet"); // .gitlet directory
    // objects folder to store commits and blobs
    public static final File OBJECT_DIR = join(GITLET_DIR, "objects");
    // refs folder -> heads folder -> all branches,
    // each named by the branch name, content is SHA-1 ID for the head commit of each branch
    public static final File REFS_DIR = join(GITLET_DIR, "refs");
    public static final File HEADS_DIR = join(REFS_DIR, "heads");
    // HEAD file storing current commit ID
    public static final File HEAD = new File(GITLET_DIR, "HEAD");
    // index file storing staging area
    public static final File INDEX = new File(GITLET_DIR, "index");
    private static Commit currCommit;

    public static void setupPersistence() {
        GITLET_DIR.mkdir();
        OBJECT_DIR.mkdir();
        REFS_DIR.mkdir();
        HEADS_DIR.mkdir();
    }

    public static void checkFolderExistence() {
        if (!GITLET_DIR.exists()) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
    }

    public static void initiateGitlet() {
        if (GITLET_DIR.exists()) {
            String msg = "A Gitlet version-control system already exists in the current directory";
            System.out.println(msg);
            System.exit(0);
        }
        setupPersistence();
        Commit c = initCommit();
        c.saveCommit();
        initHEAD();
        initOrUpdateHeads(c);
        initIndex();
    }

    private static Commit initCommit() {
        currCommit = new Commit();
        return currCommit;
    }

    private static void initHEAD() {
        writeContents(HEAD, "master");
    }

    private static void initOrUpdateHeads(Commit c) {
        File branchHead = new File(HEADS_DIR, getCurrBranch());
        writeContents(branchHead, c.getCommitID());
    }

    private static void initIndex() {
        writeObject(INDEX, new Index());
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
        if (commitFileMap.containsKey(b.getFilePath())
                && commitFileMap.get(b.getFilePath()).equals(b.getID())) {
            stagedArea.removeFromStagedToAdd(b.getFilePath());
            stagedArea.removeFromStagedToRemove(b.getFilePath());
        } else {
            if (stagedArea.getStagedToRemove().containsKey(b.getFilePath())) {
                stagedArea.removeFromStagedToRemove(b.getFilePath());
            } else {
                stagedArea.stageToAdd(b);
            }
        }

        stagedArea.saveIndex();
    }

    // test function for debugging purpose
    public static void test(String toPrint) {
        if (toPrint.equals("index")) {
            System.out.println(getStagedArea().getStagedToAdd());
            System.out.println(getStagedArea().getStagedToRemove());
        } else if (toPrint.equals("currCommit")) {
            System.out.println(getCurrCommit().getBlobs());
            String toPrint1 = readContentsAsString(join(HEADS_DIR, getCurrBranch()));
            System.out.println(toPrint1);
        }
    }

    private static Index getStagedArea() {
        return readObject(INDEX, Index.class);
    }

    public static void newCommit(String commitMsg, String mCommitID) {

        // abort if the staging area is clear
        Index stagedArea = getStagedArea();
        if (stagedArea.getStagedToAdd().isEmpty()
                && stagedArea.getStagedToRemove().isEmpty()) {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        } else if (commitMsg.isEmpty()) {
            // abort if the commit msg is blank
            System.out.println("Please enter a commit message.");
            System.exit(0);
        }

        Commit c = new Commit(commitMsg, calculateParents(mCommitID), calculateBlobs());
        c.saveCommit();
        initOrUpdateHeads(c);
        clearStagedArea();
    }

    private static List<String> calculateParents(String mCommitID) {
        List<String> parents = new ArrayList<>();
        currCommit = getCurrCommit();
        parents.add(currCommit.getCommitID());
        if (isTrusy(mCommitID)) {
            parents.add(mCommitID);
        }
        return parents;
    }

    private static HashMap<String, String> calculateBlobs() {
        HashMap<String, String> blobs = getCurrCommit().getBlobs();
        Index stagedArea = getStagedArea();
        for (String i: stagedArea.getStagedToAdd().keySet()) {
            // update + add if any changes in staged
            blobs.put(i, stagedArea.getStagedToAdd().get(i));
        }
        for (String j: stagedArea.getStagedToRemove().keySet()) {
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

        if (stagedArea.getStagedToAdd().containsKey(f.getPath())) {
            stagedArea.removeFromStagedToAdd(f.getPath());
        } else if (currCommit.getBlobs().containsKey(f.getPath())) {
            String blobID = currCommit.getBlobs().get(f.getPath());
            Blob b = readObject(getObjectFilebyID(blobID), Blob.class);
            stagedArea.stagedToRemove(b);
        } else {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
        stagedArea.saveIndex();
    }

    public static void displayLog() {
        currCommit = getCurrCommit();
        Commit commitToDisplay = currCommit;
        while (commitToDisplay != null) {
            printLog(commitToDisplay);
            if (commitToDisplay.getParents().isEmpty()) {
                commitToDisplay = null;
            } else {
                String tempCommitID = commitToDisplay.getParents().get(0);
                commitToDisplay = getObjectbyID(tempCommitID, Commit.class);
            }
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
            for (String id: plainFilenamesIn(folder)) {
                File f = join(folder, id);
                if (commitOrBlob(f).equals("Commit")) {
                    Commit c = readObject(f, Commit.class);
                    printLog(c);
                }
            }
        }
    }

    public static void findCommitsWithMsg(String commitMsg) {
        List<String> commitIdList = new ArrayList<>();
        for (File folder: OBJECT_DIR.listFiles()) {
            for (String id: plainFilenamesIn(folder)) {
                File f = join(folder, id);
                if (commitOrBlob(f).equals("Commit")) {
                    Commit c = readObject(f, Commit.class);
                    if (c.getCommitMsg().equals(commitMsg)) {
                        commitIdList.add(c.getCommitID());
                    }
                }
            }
        }
        printCommitIDList(commitIdList);
    }

    private static void printCommitIDList(List<String> commitIdList) {
        if (commitIdList.isEmpty()) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        } else {
            for (String id: commitIdList) {
                System.out.println(id);
            }
        }
    }

    public static void displayStatus() {
        System.out.println("=== Branches ===");
        displayBranches();
        System.out.println();

        System.out.println("=== Staged Files ===");
        displayStagedFiles(getStagedArea().getStagedToAdd());
        System.out.println();

        System.out.println("=== Removed Files ===");
        displayStagedFiles(getStagedArea().getStagedToRemove());
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
            if (!getStagedArea().getStagedToAdd().containsKey(f.getPath())
                    && !getStagedArea().getStagedToRemove().containsKey(f.getPath())
                    && !getCurrCommit().getBlobs().containsKey(f.getPath())
                    && !f.isDirectory()) {
                res.add(f.getName());
            }
        }
        Collections.sort(res);
        for (String fileName: res) {
            System.out.println(fileName);
        }
    }

    private static void displayBranches() {
        String currBranch = getCurrBranch();
        for (String branch: plainFilenamesIn(HEADS_DIR)) {
            if (currBranch.equals(branch)) {
                System.out.printf("*%s%n", currBranch);
            } else {
                System.out.println(branch);
            }
        }
    }

    private static void displayStagedFiles(HashMap<String, String> files) {
        printFileNamesfromPaths(asSortedList(files.keySet()));
    }

    private static void printFileNamesfromPaths(List<String> l) {
        for (String filePath: l) {
            System.out.println(getFileNameFromPath(filePath));
        }
    }

    private static void printUnstagedFiles() {
        printFileNamesfromPaths(getUnstagedFiles());
    }

    private static List<String> getUnstagedFiles() {
        List<String> res = unstagedFilesfromMap(getStagedArea().getStagedToAdd());
        for (String entry: unstagedFilesfromMap(getCurrCommit().getBlobs())) {
            String[] tempParts = entry.split(Pattern.quote(" "));
            String filePath = tempParts[0];
            if (!getStagedArea().getStagedToRemove().containsKey(filePath)
                    && !getStagedArea().getStagedToAdd().containsKey(filePath)) {
                res.add(filePath + " " + tempParts[1]);
            }
        }
        Collections.sort(res);
        return res;
    }

    // a helper function for above to shorten codes
    private static List<String> unstagedFilesfromMap(HashMap<String, String> map) {
        List<String> res = new ArrayList<>();
        for (Map.Entry<String, String> entry: map.entrySet()) {
            String filePath = entry.getKey();
            String fileContent = entry.getValue();
            File fileInCWD = new File(filePath);

            // if the file does not exist in CWD or the version is diff with CWD version
            if (!fileInCWD.exists()) {
                res.add(filePath + " (deleted)");
            } else {
                String fileContentInCWD = sha1(filePath, readContents(fileInCWD));
                if (!fileContent.equals(fileContentInCWD)) {
                    res.add(filePath + " (modified)");
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

    private static void checkCommitExistwithID(String id) {
        if (!getObjectFilebyID(id).exists()) {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
    }

    private static String getBlobIDbyFile(Commit c, File f) {
        return c.getBlobs().get(f.getPath());
    }

    public static void checkoutToCommitsFile(String id, String fileName) {
        File f = join(CWD, fileName);
        checkCommitExistwithID(id);
        Commit c = getObjectbyID(id, Commit.class);
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

        String id = readContentsAsString(branch);
        Commit c = getObjectbyID(id, Commit.class);
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
                String m1 = "There is an untracked file in the way; ";
                String m2 = "delete it, or add and commit it first.";
                System.out.println(m1 + m2);
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
        Commit mCommit = getObjectbyID(readContentsAsString(tbranch), Commit.class);
        checkPossibleRewritesToUntrackedFile(mCommit);

        Commit splitPoint = findSplitPoint(mCommit);
        // if the split point is the same commit as the given branch / current branch
        fastMerge(splitPoint, mCommit, branchName);
        // else, continue with the conflict merge

        // loop through splitpoint files
        Commit cCommit = getCurrCommit();
        for (Map.Entry<String, String> entry : mCommit.getBlobs().entrySet()) {
            String mKey = entry.getKey();
            // 1-1 situation to stage new files from given branch
            if (
                    (!cCommit.getBlobs().containsKey(mKey)
                            && !splitPoint.getBlobs().containsKey(mKey))
                    ||
                    (cCommit.getBlobs().containsKey(mKey)
                            && splitPoint.getBlobs().containsKey(mKey)
                            && !splitPoint.getBlobs().get(mKey).equals(entry.getValue())
                            && splitPoint.getBlobs().get(mKey).equals(cCommit.getBlobs().get(mKey)))
            ) {
                checkoutToCommitsFile(mCommit.getCommitID(),
                        getFileNameFromPath(mKey));
                addToStage(getFileNameFromPath(mKey));
            } else if (
                    (!splitPoint.getBlobs().containsKey(mKey)
                            && cCommit.getBlobs().containsKey(mKey)
                            && !cCommit.getBlobs().get(mKey).equals(entry.getValue()))
                    ||
                    (splitPoint.getBlobs().containsKey(mKey)
                            && cCommit.getBlobs().containsKey(mKey)
                            && new HashSet<>(Arrays.asList(cCommit.getBlobs().get(mKey),
                                                entry.getValue(),
                                                splitPoint.getBlobs().get(mKey))).size() == 3)
            ) {
                // 1-2 situation when both have same file and file contents have conflicts
                Blob currBranchVersion = getObjectbyID(cCommit.getBlobs().get(mKey), Blob.class);
                String currFileContent = convertBytesToString(currBranchVersion.getContent());
                Blob mBranchVersion = getObjectbyID(entry.getValue(), Blob.class);
                String mFileContent = convertBytesToString(mBranchVersion.getContent());
                mergeConflictFilesContent(currFileContent, mFileContent, mKey);
            } else if (!cCommit.getBlobs().containsKey(mKey)
                        && splitPoint.getBlobs().containsKey(mKey)
                        && !entry.getValue().equals(splitPoint.getBlobs().get(mKey))) {
                // only given branch has this file but this was originally in splitpoint
                Blob mBranchVersion = getObjectbyID(entry.getValue(), Blob.class);
                String mFileContent = convertBytesToString(mBranchVersion.getContent());
                mergeConflictFilesContent("", mFileContent, mKey);
            }
        }

        for (Map.Entry<String, String> entry : splitPoint.getBlobs().entrySet()) {
            String key = entry.getKey();
            if (!mCommit.getBlobs().containsKey(key)) {
                if (cCommit.getBlobs().containsKey(key)) {
                    if (cCommit.getBlobs().get(key).equals(entry.getValue())) {
                        // 2-1 situation: when we need to remove as given branch did this
                        removeFile(getFileNameFromPath(key));
                    } else {
                        // 2-2 situation: conflict also, but only exist in currBranch
                        String id = cCommit.getBlobs().get(key);
                        Blob currBranchVersion = getObjectbyID(id, Blob.class);
                        byte[] c = currBranchVersion.getContent();
                        String currFileContent = convertBytesToString(c);
                        mergeConflictFilesContent(currFileContent, "", key);
                    }
                }
            }
        }

        newCommit("Merged " + branchName + " into "
                + getCurrBranch() + ".", mCommit.getCommitID());

    }

    private static void mergeConflictFilesContent(String content1,
                                                  String content2, String filePath) {
        String newfile = "<<<<<<< HEAD\n";
        newfile += content1;
        newfile += "=======\n";
        newfile += content2;
        newfile += ">>>>>>>\n";
        writeContents(getFileFromPath(filePath), newfile);
        addToStage(getFileNameFromPath(filePath));
        System.out.println("Encountered a merge conflict.");
    }
    private static void fastMerge(Commit splitPoint, Commit mCommit, String branchName) {
        if (splitPoint.getCommitID().equals(mCommit.getCommitID())) {
            System.out.println("Given branch is an ancestor of the current branch.");
            System.exit(0);
        } else if (splitPoint.getCommitID().equals(getCurrCommitID())) {
            checkoutToBranch(branchName);
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
    }

    private static Commit findSplitPoint(Commit mCommit) {
        HashMap<String, Integer> ancestorsWithDepth = listOfCommitsWithDepth(getCurrCommit());
        ArrayList<String> level = new ArrayList<>();
        level.add(mCommit.getCommitID());
        while (!level.isEmpty()) {
            ArrayList<String> newLevel = new ArrayList<>();
            HashMap<Integer, String> tempRes = new HashMap<>();
            for (String id: level) {
                if (ancestorsWithDepth.containsKey(id)) {
                    tempRes.put(ancestorsWithDepth.get(id), id);
                } else {
                    newLevel.addAll(getObjectbyID(id, Commit.class).getParents());
                }
            }
            if (!tempRes.isEmpty()) {
                Integer minKey = Collections.min(tempRes.keySet());
                String id = tempRes.get(minKey);
                return getObjectbyID(id, Commit.class);
            }
            level = newLevel;
        }
        return new Commit(); // will not be incur; to fulfill Java compiling requirements
    }

    private static HashMap<String, Integer> listOfCommitsWithDepth(Commit c) {
        HashMap<String, Integer> res = new HashMap<>();
        Queue<ArrayList<Object>> q = new LinkedList<>();
        ArrayList<Object> initPair = new ArrayList<>();
        initPair.add(c);
        initPair.add(0);
        q.add(initPair);
        while (!q.isEmpty()) {
            ArrayList<Object> currPair = q.remove();
            Commit currC = (Commit) currPair.get(0);
            Integer lvl = (Integer) currPair.get(1);
            res.put(currC.getCommitID(), lvl);
            for (String id: currC.getParents()) {
                Commit p = getObjectbyID(id, Commit.class);
                ArrayList<Object> newPair = new ArrayList<>();
                newPair.add(p);
                newPair.add(lvl + 1);
                q.add(newPair);
            }
        }
        return res;
    }

    private static void checkUncommitedChanges() {
        if (!getStagedArea().getStagedToRemove().isEmpty()
                || !getStagedArea().getStagedToAdd().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

}
