package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.Date;

import static gitlet.Repository.GITLET_DIR;
import static gitlet.Repository.OBJECT_DIR;
import static gitlet.Utils.*;
import static gitlet.Commit.*;
import static gitlet.myUtils.*;

public class Blob implements Serializable {
    private File fileInCWD;
    private byte[] filecontent;
    private String filePath;
    private String blobID;

    public Blob(File f) {
        this.fileInCWD = f;
        this.filecontent = readContents(fileInCWD);
        this.filePath = fileInCWD.getPath();
        this.blobID = sha1(filecontent);
    }

    public byte[] getContent() {return filecontent; }
    public String getID() {
        return blobID;
    }

    public String getPath() {
        return filePath;
    }

    public void saveBlob(File f) {
        File outFile = getObjectFilebyID(blobID);
        writeObject(outFile, this);
    }
}