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
        this.filecontent = getContent();
        this.filePath = getpath();
        this.blobID = getID();
    }

    private byte[] getContent() {
        return readContents(fileInCWD);
    }
    private String getID() {
        return sha1(filecontent);
    }

    private String getpath() {
        return fileInCWD.getPath();
    }

    public void saveBlobtoObjects(File f) {
        File outFile = getObjectFilebyID(blobID);
        writeObject(outFile, this);
    }
}