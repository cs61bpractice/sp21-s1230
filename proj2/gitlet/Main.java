package gitlet;

import javax.net.ssl.SSLServerSocket;
import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author TODO
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // TODO: what if args is empty?

        // check if args are empty
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // TODO: handle the `init` command
                validateNumArgs("init", args, 1);
                Repository.initiateGitlet();
                //System.out.println(currCommit.getCommitMsg()); // own test
                // System.out.println(currCommit.getBlobs());
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                validateNumArgs("add", args, 2);
                Repository.addToStage(args[1]);
                break;
            // TODO: FILL THE REST IN
            case "commit":
                // TODO
                validateNumArgs("commit", args, 2);
                Repository.newCommit(args[1], "");
                break;
            case "rm":
                // TODO
                validateNumArgs("rm", args, 2);
                Repository.removeFile(args[1]);
                break;
            case "log":
                // TODO
                validateNumArgs("log", args, 1);
                Repository.displayLog();
                break;
            case "global-log":
                // TODO
                validateNumArgs("global-log", args, 1);
                Repository.displayGlobalLog();
                break;
            case "find":
                // TODO
                validateNumArgs("find", args, 2);
                Repository.findCommitsWithMsg(args[1]);
                break;
            case "status":
                // TODO
                validateNumArgs("status", args, 1);
                Repository.displayStatus();
                break;
            case "checkout":
                // TODO
                if ((args.length == 3 && !args[1].equals("--"))
                        || (args.length == 4 && !args[2].equals("--"))){
                    invalidOperand();
                }
                if (args.length == 3) {
                    Repository.checkoutToFile(args[2]);
                } else if (args.length == 4) {
                    Repository.checkoutToCommitsFile(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutToBranch(args[1]);
                } else {
                    validateNumArgs("checkout", args, 3);
                }
                break;
            case "branch":
                // TODO
                validateNumArgs("branch", args, 2);
                Repository.createNewBranch(args[1]);
                break;
            case "rm-branch":
                // TODO
                validateNumArgs("rm-branch", args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                // TODO
                validateNumArgs("reset", args, 2);
                Repository.resetToCommit(args[1]);
                break;
            case "merge":
                // TODO
                validateNumArgs("merge", args, 2);
                Repository.mergeToBranch(args[1]);
                break;
            case "test":
                validateNumArgs("test", args, 2);
                Repository.test(args[1]);
                break;
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.printf("Invalid number of arguments for: %s.", cmd);
            System.exit(0);
        }
    }

    public static void invalidOperand() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }
}
