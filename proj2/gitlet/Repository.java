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
    public static final File REMOTE = new File(GITLET_DIR, "remote");
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
        initRemote();
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

    private static void initRemote() {
        writeObject(REMOTE, new Remote());
    }

    private static String getCurrBranch() {
        return readContentsAsString(HEAD);
    }

    private static String getCurrCommitID() {
        return readContentsAsString(join(HEADS_DIR, getCurrBranch()));
    }
    private static Commit getCurrCommit() {
        return getObjectbyID(getCurrCommitID(), Commit.class, OBJECT_DIR);
    }

    public static void addToStage(String fileName) {
        File f = join(CWD, fileName);
        checkFileExist(f, "add");

        Blob b = new Blob(f);
        b.saveBlob();
        Index stagedArea = getStagedArea(INDEX);
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
            System.out.println(getStagedArea(INDEX).getStagedToAdd());
            System.out.println(getStagedArea(INDEX).getStagedToRemove());
        } else if (toPrint.equals("currCommit")) {
            System.out.println(getCurrCommit().getBlobs());
            String toPrint1 = readContentsAsString(join(HEADS_DIR, getCurrBranch()));
            System.out.println(toPrint1);
        }
    }

    private static Index getStagedArea(File idx) {
        return readObject(idx, Index.class);
    }

    public static void newCommit(String commitMsg, String mCommitID) {

        // abort if the staging area is clear
        Index stagedArea = getStagedArea(INDEX);
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
        clearStagedArea(INDEX);
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
        Index stagedArea = getStagedArea(INDEX);
        for (String i: stagedArea.getStagedToAdd().keySet()) {
            // update + add if any changes in staged
            blobs.put(i, stagedArea.getStagedToAdd().get(i));
        }
        for (String j: stagedArea.getStagedToRemove().keySet()) {
            blobs.remove(j); // remove files which are staged
        }
        return blobs;
    }

    private static void clearStagedArea(File idx) {
        Index stagedArea = getStagedArea(idx);
        stagedArea.clearStagingArea();
        stagedArea.saveIndex();
    }

    public static void removeFile(String fileName) {
        File f = join(CWD, fileName);
        Index stagedArea = getStagedArea(INDEX);
        currCommit = getCurrCommit();

        if (stagedArea.getStagedToAdd().containsKey(f.getPath())) {
            stagedArea.removeFromStagedToAdd(f.getPath());
        } else if (currCommit.getBlobs().containsKey(f.getPath())) {
            String blobID = currCommit.getBlobs().get(f.getPath());
            Blob b = readObject(getObjectFilebyID(blobID, Repository.OBJECT_DIR), Blob.class);
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
                commitToDisplay = getObjectbyID(tempCommitID, Commit.class, OBJECT_DIR);
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
        displayStagedFiles(getStagedArea(INDEX).getStagedToAdd());
        System.out.println();

        System.out.println("=== Removed Files ===");
        displayStagedFiles(getStagedArea(INDEX).getStagedToRemove());
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
            if (!getStagedArea(INDEX).getStagedToAdd().containsKey(f.getPath())
                    && !getStagedArea(INDEX).getStagedToRemove().containsKey(f.getPath())
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
                System.out.printf("*%s%n", currBranch.replace("_", "/"));
            } else {
                System.out.println(branch.replace("_", "/"));
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
        List<String> res = unstagedFilesfromMap(getStagedArea(INDEX).getStagedToAdd());
        for (String entry: unstagedFilesfromMap(getCurrCommit().getBlobs())) {
            String[] tempParts = entry.split(Pattern.quote(" "));
            String filePath = tempParts[0];
            if (!getStagedArea(INDEX).getStagedToRemove().containsKey(filePath)
                    && !getStagedArea(INDEX).getStagedToAdd().containsKey(filePath)) {
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
        if (!getObjectFilebyID(id, Repository.OBJECT_DIR).exists()) {
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
        Commit c = getObjectbyID(id, Commit.class, OBJECT_DIR);
        checkFileExistInCommit(f, c);

        rewriteContentforCheckoutToFile(c, f);
    }

    private static void rewriteContentforCheckoutToFile(Commit c, File f) {
        Blob oldBlob = getObjectbyID(getBlobIDbyFile(c, f), Blob.class, OBJECT_DIR);
        writeContents(f, oldBlob.getContent());
    }

    public static void checkoutToBranch(String branchName) {
        String safeBranchName = branchName.replace("/", "_");
        File branch = join(HEADS_DIR, safeBranchName);
        checkFileExist(branch, "checkout-branch");
        checkBranchiscurrBranch(safeBranchName, "checkout");

        String id = readContentsAsString(branch);
        Commit c = getObjectbyID(id, Commit.class, OBJECT_DIR);
        checkPossibleRewritesToUntrackedFile(c, GITLET_DIR);

        changeToCommit(c);
        changeBranchTo(safeBranchName);
        clearStagedArea(INDEX);
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
        writeContents(HEAD, branchName.replace("/", "_"));
    }

    private static void checkPossibleRewritesToUntrackedFile(Commit c, File gitletDir) {
        int tempLength = gitletDir.getAbsolutePath().length();
        String parentPath = gitletDir.getAbsolutePath().substring(0, tempLength-8);
        File tempCwd = new File(parentPath);

        for (File f: tempCwd.listFiles()) {
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
        String safeBranchName = branchName.replace("/", "_");
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
        String safeBranchName = branchName.replace("/", "_");
        File branch = join(HEADS_DIR, safeBranchName);
        checkFileExist(branch, "rm-branch");
        checkBranchiscurrBranch(safeBranchName, "rm-branch");
        branch.delete();
    }

    public static void resetToCommit(String commitID, File gitletDir) {
        File tempObjectDir = join(gitletDir, "objects");
        File tempHead = join(gitletDir, "HEAD");

        Commit c = getObjectbyID(commitID, Commit.class, tempObjectDir);
        // checkPossibleRewritesToUntrackedFile(c, gitletDir);

        changeToCommit(c);
        changeBranchHeadToGivenCommit(readContentsAsString(tempHead), commitID,
                join(gitletDir, "refs", "heads"));
        clearStagedArea(join(gitletDir, "index"));
    }

    private static void changeBranchHeadToGivenCommit(String branch,
                                                      String commitID, File headsDir) {
        writeContents(join(headsDir, branch), commitID);
    }

    public static void mergeToBranch(String branchName) {
        String safeBranchName = branchName.replace("/", "_");
        checkUncommitedChanges();
        File tbranch = join(HEADS_DIR, safeBranchName);
        checkFileExist(tbranch, "merge");
        checkBranchiscurrBranch(safeBranchName, "merge");
        String tbranchId = readContentsAsString(tbranch);
        Commit mCommit = getObjectbyID(tbranchId, Commit.class, OBJECT_DIR);
        checkPossibleRewritesToUntrackedFile(mCommit, GITLET_DIR);
        Commit splitPoint = findSplitPoint(mCommit);
        fastMerge(splitPoint, mCommit, safeBranchName);

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
                String cBlobId = cCommit.getBlobs().get(mKey);
                Blob currBranchVersion = getObjectbyID(cBlobId, Blob.class, OBJECT_DIR);
                String currFileContent = convertBytesToString(currBranchVersion.getContent());
                Blob mBranchVersion = getObjectbyID(entry.getValue(), Blob.class, OBJECT_DIR);
                String mFileContent = convertBytesToString(mBranchVersion.getContent());
                mergeConflictFilesContent(currFileContent, mFileContent, mKey);
            } else if (!cCommit.getBlobs().containsKey(mKey)
                        && splitPoint.getBlobs().containsKey(mKey)
                        && !entry.getValue().equals(splitPoint.getBlobs().get(mKey))) {
                // only given branch has this file but this was originally in splitpoint
                Blob mBranchVersion = getObjectbyID(entry.getValue(), Blob.class, OBJECT_DIR);
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
                        Blob currBranchVersion = getObjectbyID(id, Blob.class, OBJECT_DIR);
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

    // if the split point is the same commit as the given branch / current branch
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
        HashMap<String, Integer> ancestors = commitsWithDepth(getCurrCommit(), OBJECT_DIR);
        ArrayList<String> level = new ArrayList<>();
        level.add(mCommit.getCommitID());
        while (!level.isEmpty()) {
            ArrayList<String> newLevel = new ArrayList<>();
            HashMap<Integer, String> tempRes = new HashMap<>();
            for (String id: level) {
                if (ancestors.containsKey(id)) {
                    tempRes.put(ancestors.get(id), id);
                } else {
                    Commit tempC = getObjectbyID(id, Commit.class, OBJECT_DIR);
                    newLevel.addAll(tempC.getParents());
                }
            }
            if (!tempRes.isEmpty()) {
                Integer minKey = Collections.min(tempRes.keySet());
                String id = tempRes.get(minKey);
                return getObjectbyID(id, Commit.class, OBJECT_DIR);
            }
            level = newLevel;
        }
        return new Commit(); // will not be incur; to fulfill Java compiling requirements
    }

    private static HashMap<String, Integer> commitsWithDepth(Commit c, File objectDir) {
        HashMap<String, Integer> res = new HashMap<>();
        Queue<Object[]> q = new LinkedList<>();
        Object[] initPair = new Object[2];
        initPair[0] = c;
        initPair[1] = 0;
        q.add(initPair);
        while (!q.isEmpty()) {
            Object[] currPair = q.remove();
            Commit currC = (Commit) currPair[0];
            Integer lvl = (Integer) currPair[1];
            res.put(currC.getCommitID(), lvl);
            for (String id: currC.getParents()) {
                Commit p = getObjectbyID(id, Commit.class, objectDir);
                Object[] newPair = new Object[2];
                newPair[0] = p;
                newPair[1] = lvl + 1;
                q.add(newPair);
            }
        }
        return res;
    }

    private static void checkUncommitedChanges() {
        if (!getStagedArea(INDEX).getStagedToRemove().isEmpty()
                || !getStagedArea(INDEX).getStagedToAdd().isEmpty()) {
            System.out.println("You have uncommitted changes.");
            System.exit(0);
        }
    }

    public static void addRemote(String remoteName, String remoteDir) {
        File remoteFile = join(GITLET_DIR, "remote");
        Remote remote = readObject(remoteFile, Remote.class);

        if (remote.remoteMap.containsKey(remoteName)) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }

        remote.addRemote(remoteName, remoteDir);
        remote.saveRemote();
    }

    public static void rmRemote(String remoteName) {
        File remoteFile = join(GITLET_DIR, "remote");
        Remote remote = readObject(remoteFile, Remote.class);

        if (!remote.remoteMap.containsKey(remoteName)) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }

        remote.rmRemote(remoteName);
        remote.saveRemote();
    }

    public static void push(String remoteName, String remoteBranch) {
        // use safe branch name due to settings of UNIX system
        String safeRemoteBranch = remoteBranch.replace("/", "_");
        // read remote object and check if we have this remote .gitlet dir
        File remoteFile = join(GITLET_DIR, "remote");
        Remote remote = readObject(remoteFile, Remote.class);
        File remoteDir = remote.remoteMap.get(remoteName);
        if (!remoteDir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        // get targetId i.e. head at the remote specific branch
        File remoteObjects = join(remoteDir, "objects");
        File targetFile = join(remoteDir, "refs", "heads", safeRemoteBranch);
        String targetId = readContentsAsString(targetFile);
        if (!findHistoricCommit(getCurrCommitID(), targetId)) {
            System.out.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }

        // append future commits
        Commit targetCommit = getObjectbyID(targetId, Commit.class, remoteObjects);
        Set<String> commitIdSet = commitsWithDepth(targetCommit, remoteObjects).keySet();
        Queue<String> queue = new LinkedList<>();
        queue.add(getCurrCommitID());
        HashSet<String> blobIdSet = new HashSet<>();
        while (!queue.isEmpty()) {
            String tempCommitId = queue.remove();
            if (!commitIdSet.contains(tempCommitId)) {
                File outFile = getObjectFilebyID(tempCommitId, remoteObjects);
                Commit commitToWrite = getObjectbyID(tempCommitId, Commit.class, OBJECT_DIR);
                writeObject(outFile, commitToWrite);
                blobIdSet.addAll(commitToWrite.getBlobs().values());
                queue.addAll(commitToWrite.getParents());
            }
        }

        for (String blobId: blobIdSet) {
            if (!objectExistence(blobId, OBJECT_DIR)) {
                File outFile = getObjectFilebyID(blobId, remoteObjects);
                Blob outBlob = getObjectbyID(blobId, Blob.class, OBJECT_DIR);
                writeObject(outFile, outBlob);
            }
        }

        resetToCommit(getCurrCommitID(), remoteDir);
    }

    public static boolean findHistoricCommit(String startCommitID, String targetId) {
        Queue<String> queue = new LinkedList<>();
        queue.add(startCommitID);
        while (!queue.isEmpty()) {
            String tempCommitID = queue.remove();
            if (tempCommitID.equals(targetId)) {
                return true;
            }
            Commit tempCommit = getObjectbyID(tempCommitID, Commit.class, OBJECT_DIR);
            queue.addAll(tempCommit.getParents());
        }
        return false;
    }

    public static void pull(String remoteName, String remoteBranch) {
        String safeRemoteBranch = remoteBranch.replace("/", "_");
        fetch(remoteName, safeRemoteBranch);
        mergeToBranch(remoteName + "/" + safeRemoteBranch);
    }

    public static void fetch(String remoteName, String remoteBranch) {
        String safeRemoteBranch = remoteBranch.replace("/", "_");
        File remoteFile = join(GITLET_DIR, "remote");
        Remote remote = readObject(remoteFile, Remote.class);
        File toCopy = remote.remoteMap.get(remoteName);
        if (!toCopy.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }

        // get the branch file under heads_dir in remote dir
        File remoteObjects = join(toCopy, "objects");
        File startFile = join(toCopy, "refs", "heads", safeRemoteBranch);
        if (!startFile.exists()) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        String startCommitID = readContentsAsString(startFile);

        // create the new branch and change the head to the new branch
        File newBranch = join(HEADS_DIR, remoteName + "_" + safeRemoteBranch);
        newBranch.delete();
        createNewBranch(remoteName + "_" + safeRemoteBranch);
        writeContents(newBranch, startCommitID);

        // copy all the commits / blobs from startCommit to the current .gitlet dir
        Queue<String> queue = new LinkedList<>();
        queue.add(startCommitID);
        HashSet<String> blobIdSet = new HashSet<>();
        while (!queue.isEmpty()) {
            String tempCommitID = queue.remove();
            Commit tempCommit = getObjectbyID(tempCommitID, Commit.class, remoteObjects);

            // write the remote object not in curr repo to the current .gitlet folder
            if (!objectExistence(tempCommitID, OBJECT_DIR)) {
                File outFile = getObjectFilebyID(tempCommitID, OBJECT_DIR);
                updateBlobsFilePath(tempCommit, remoteObjects);
                writeObject(outFile, tempCommit);
            }

            // add the commits on this branch to the commitsQueue
            queue.addAll(tempCommit.getParents());

            // add all the blobs id of this commit into a set
            // to finalize it after the while loop
            blobIdSet.addAll(tempCommit.getBlobs().values());
        }

        for (String blobId: blobIdSet) {
            if (!objectExistence(blobId, OBJECT_DIR)) {
                File outFile = getObjectFilebyID(blobId, OBJECT_DIR);
                Blob outBlob = getObjectbyID(blobId, Blob.class, remoteObjects);
                writeObject(outFile, outBlob);
            }
        }
    }

    // update the blobs file path when removing commit from remote to current
    public static void updateBlobsFilePath(Commit c, File objectsDir) {
        HashMap<String, String> updatedBlobs = new HashMap<>();
        for (Map.Entry<String, String> entry: c.getBlobs().entrySet()) {
            String tempPath = entry.getKey();
            String tempBlobId = entry.getValue();
            String tempFileName = getFileNameFromPath(tempPath);
            File updatedFile = join(CWD, tempFileName);

            // update for blob object
            Blob tempBlob = getObjectbyID(tempBlobId, Blob.class, objectsDir);
            tempBlob.updateBlob(updatedFile);

            // update for commit blobs map
            updatedBlobs.put(updatedFile.getPath(), tempBlobId);
        }
        c.updateBlobs(updatedBlobs);
    }

}
