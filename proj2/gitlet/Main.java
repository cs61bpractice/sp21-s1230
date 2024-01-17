package gitlet;
import static gitlet.Repository.*;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *
 * Serves as an entry point for each of the commands,
 * and some basic validation to filter out the obvious invalid commands
 *
 *  @author Grebeth.P
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // check if args are empty
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }

        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                validateNumArgs("init", args, 1);
                Repository.initiateGitlet();
                break;
            case "add":
                validateNumArgs("add", args, 2);
                Repository.addToStage(args[1]);
                break;
            case "commit":
                validateNumArgs("commit", args, 2);
                Repository.newCommit(args[1], "");
                break;
            case "rm":
                validateNumArgs("rm", args, 2);
                Repository.removeFile(args[1]);
                break;
            case "log":
                validateNumArgs("log", args, 1);
                Repository.displayLog();
                break;
            case "global-log":
                validateNumArgs("global-log", args, 1);
                Repository.displayGlobalLog();
                break;
            case "find":
                validateNumArgs("find", args, 2);
                Repository.findCommitsWithMsg(args[1]);
                break;
            case "status":
                validateNumArgs("status", args, 1);
                Repository.displayStatus();
                break;
            case "checkout":
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
                validateNumArgs("branch", args, 2);
                Repository.createNewBranch(args[1]);
                break;
            case "rm-branch":
                validateNumArgs("rm-branch", args, 2);
                Repository.removeBranch(args[1]);
                break;
            case "reset":
                validateNumArgs("reset", args, 2);
                Repository.resetToCommit(args[1]);
                break;
            case "merge":
                validateNumArgs("merge", args, 2);
                Repository.mergeToBranch(args[1]);
                break;
            case "test":
                validateNumArgs("test", args, 2);
                Repository.test(args[1]);
                break;
            case "add-remote":
                validateNumArgs("add-remote", args, 2);
                int endIndex = args[1].length()-8;
                Repository.addRemote(args[0], args[1].substring(0, endIndex));
            case "rm-remote":
                validateNumArgs("rm-remote", args, 1);
                Repository.rmRemote(args[0]);
            case "push":
                validateNumArgs("push", args, 2);
                Repository.push(args[0], args[1]);
            case "pull":
                validateNumArgs("pull", args, 2);
                Repository.pull(args[0], args[1]);
            case "fetch":
                validateNumArgs("fetch", args, 2);
                Repository.fetch(args[0], args[1]);
            default:
                System.out.println("No command with that name exists");
                break;
        }
    }

    public static void validateNumArgs(String cmd, String[] args, int n) {
        if (args.length != n) {
            System.out.printf("Invalid number of arguments for: %s.", cmd);
            System.exit(0);
        } else if (!cmd.equals("init")) {
            checkFolderExistence();
        }
        if (cmd.equals("add-remote")) {
            if (!args[1].endsWith("/.gitlet")) {
                System.out.println("Invalid server repository name.");
                System.exit(0);
            }
        }
    }

    public static void invalidOperand() {
        System.out.println("Incorrect operands.");
        System.exit(0);
    }
}
