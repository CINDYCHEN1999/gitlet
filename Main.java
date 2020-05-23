package gitlet;
import java.io.File;
import static gitlet.Utils.messageq;
import static gitlet.Utils.writeObject;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Cindy Chen
 * collaborators:
 * @author collab:
 * Xavilla Zang: the DFS idea for finding all history
 * in splitpoint under repo class comes from him.
 */

public class Main {
    /**
     * command type.
     */
    private static String _headCommand;

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            messageq("Please enter a command.");
        } else if (args[0].equals("init")) {
            if (args.length != 1) {
                messageq("Incorrect operands.");
            }
            File gitlet = new File("./.gitlet");
            if (gitlet.exists()) {
                messageq("A Gitlet version-control system already"
                        + " exists in the current directory.");
            }
            File repo = new File("./.gitlet/repo");
            gitlet.mkdir();
            Repo raw = new Repo();
            writeObject(repo, raw);
        } else {
            if (!new File("./.gitlet").exists()) {
                messageq("Not in an initialized Gitlet directory.");
            }
            _headCommand = args[0];
            File repoFile = new File("./.gitlet/repo");
            Repo curr = Utils.readObject(repoFile, Repo.class);

            if (_headCommand.equals("add")) {
                if (args.length != 2) {
                    messageq("Incorrect operands.");
                }
                curr.add(args[1]);
                writeObject(repoFile, curr);
            } else if (_headCommand.equals("commit")) {
                if (args.length != 2) {
                    messageq("Incorrect operands.");
                } else if (args.length < 2 || args[1].equals("")) {
                    messageq("Please enter a commit message.");
                }
                curr.commit(args[1]);
                writeObject(repoFile, curr);
            } else if (_headCommand.equals("rm")) {
                if (args.length != 2) {
                    messageq("Incorrect operands.");
                }
                curr.rm(args[1]);
                writeObject(repoFile, curr);
            } else {
                main2(args);
            }
        }
    }

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main2(String... args) {
        if (!new File("./.gitlet").exists()) {
            messageq("Not in an initialized Gitlet directory.");
        }
        _headCommand = args[0];
        File repoFile = new File("./.gitlet/repo");
        Repo curr = Utils.readObject(repoFile, Repo.class);

        if (_headCommand.equals("log")) {
            if (args.length > 1) {
                messageq("Incorrect operands.");
            }
            curr.log();
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("global-log")) {
            if (args.length > 1) {
                messageq("Incorrect operands.");
            }
            curr.globalLog();
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("find")) {
            if (args.length != 2) {
                messageq("Incorrect operands.");
            }
            curr.find(args[1]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("status")) {
            if (args.length > 1) {
                messageq("Incorrect operands.");
            }
            curr.status();
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("checkout")) {
            if (args.length == 3 && args[1].equals("--")) {
                curr.checkoutid(args[2]);
            } else if (args.length == 4 && args[2].equals("--")) {
                curr.checkoutcommit(args[1], args[3]);
            } else if (args.length == 2) {
                curr.checkoutBranch(args[1]);
            } else {
                messageq("Incorrect operands.");
            }
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("branch")) {
            if (args.length != 2) {
                messageq("Incorrect operands.");
            }
            curr.branch(args[1]);
            writeObject(repoFile, curr);
        } else {
            main3(args);
        }
    }

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main3(String... args) {
        if (!new File("./.gitlet").exists()) {
            messageq("Not in an initialized Gitlet directory.");
        }
        _headCommand = args[0];
        File repoFile = new File("./.gitlet/repo");
        Repo curr = Utils.readObject(repoFile, Repo.class);

        if (_headCommand.equals("rm-branch")) {
            if (args.length != 2) {
                messageq("Incorrect operands.");
            }
            curr.rmBranch(args[1]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("reset")) {
            if (args.length != 2) {
                messageq("Incorrect operands.");
            }
            curr.reset(args[1]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("merge")) {
            if (args.length != 2) {
                messageq("Incorrect operands.");
            }
            curr.merge(args[1]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("add-remote")) {
            if (args.length != 3) {
                messageq("Incorrect operands.");
            }
            curr.addRemote(args[1], args[2]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("rm-remote")) {
            if (args.length != 2) {
                messageq("Incorrect operands.");
            }
            curr.rmRemote(args[1]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("push")) {
            if (args.length != 3) {
                messageq("Incorrect operands.");
            }
            curr.push(args[1], args[2]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("fetch")) {
            if (args.length != 3) {
                messageq("Incorrect operands.");
            }
            curr.fetch(args[1], args[2]);
            writeObject(repoFile, curr);
        } else if (_headCommand.equals("pull")) {
            if (args.length != 3) {
                messageq("Incorrect operands.");
            }
            curr.pull(args[1], args[2]);
            writeObject(repoFile, curr);
        } else {
            messageq("No command with that name exists.");
        }
    }

}
