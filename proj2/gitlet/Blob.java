package gitlet;
import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

import java.io.File;
import java.io.Serializable;


/** Represents a gitlet blob object,
 * which is basically a file object,
 * but the same file with different contents are regarded as different blobs
 * so that it's able to save and recover different versions of the same file across commits
 *  @author Grebeth.P
 */

public class Blob implements Serializable {
    private File fileInCWD;
    private byte[] filecontent;
    private String filePath;
    private String blobID;

    public Blob(File f) {
        this.fileInCWD = f;
        this.filecontent = readContents(fileInCWD);
        this.filePath = fileInCWD.getPath();
        this.blobID = sha1(this.filePath, filecontent);
    }

    public byte[] getContent() {
        return filecontent;
    }

    public String getID() {
        return blobID;
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFileInCWD() {
        return fileInCWD;
    }

    public void saveBlob() {
        File outFile = getObjectFilebyID(blobID);
        writeObject(outFile, this);
    }

}