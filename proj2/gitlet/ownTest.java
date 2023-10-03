package gitlet;

public class ownTest {
    public static void main(String[] args) {
        String filePath = "C:\\Users\\UserName\\Documents\\file.txt";
        String fileName = gitlet.myUtils.getFileNameFromPath(filePath);
        System.out.println(fileName);
    }
}