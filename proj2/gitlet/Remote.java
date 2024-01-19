package gitlet;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;

import static gitlet.Repository.*;
import static gitlet.Utils.*;
import static gitlet.MyUtils.*;

public class Remote implements Serializable {
    HashMap<String, File> remoteMap;

    public Remote() {
        this.remoteMap = new HashMap<>();
    }
    public Remote(HashMap<String, File> remoteMap) {
        this.remoteMap = remoteMap;
    }

    public void addRemote(String remoteName, String remoteDir) {
        String[] paths = remoteDir.split("/");
        File outFolder = new File(connectStringPaths(paths));
        remoteMap.put(remoteName, outFolder);
    }

    public void saveRemote() {
        File outFile = Utils.join(GITLET_DIR, "remote");
        writeObject(outFile, this);
    }

    public void rmRemote(String remoteName) {
        remoteMap.remove(remoteName);
    }

}
