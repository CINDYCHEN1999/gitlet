package gitlet;

import java.io.File;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Stack;
import java.util.LinkedList;

/**
 * Repo class for Gitlet, the tiny stupid version-control system.
 * @author Cindy Chen
 */
public class Repo implements Serializable {

    /**
     * command type.
     */
    private String currBranch;
    /**
     * command type.
     */
    private String initialId;
    /**
     * command type.
     */
    private HashMap<String, String> _staging = new HashMap<>();
    /**
     * command type.
     */
    private HashSet<String> _removed = new HashSet<>();
    /**
     * command type.
     */
    private HashMap<String, Commit> shaCommit = new HashMap<>();
    /**
     * command type.
     */
    private HashMap<String, Branch> namebranch = new HashMap<>();
    /**
     * command type.
     */
    private HashMap<String, String> _remotenamedir;
    /**
     * command type.
     */
    private Commit _head;

    /**
     * INIT.
     */
    public Repo() {
        File staged = new File("./.gitlet/staged/");
        File committed = new File(".gitlet/committed");
        File tracked = new File(".gitlet/tracked");
        staged.mkdir();
        committed.mkdir();
        tracked.mkdir();

        Commit initial = new Commit();
        Branch master = new Branch("master", initial);
        initialId = initial.id();
        File initialFile = new File(".gitlet/committed/" + initialId);
        Utils.writeObject(initialFile, initial);

        namebranch.put("master", master);
        shaCommit.put(initial.id(), initial);
        _remotenamedir = new HashMap<String, String>();

        currBranch = "master";
        _head = master.last();

    }

    /**
     * PRINT LOG.
     *
     * @param id     id.
     * @param commit commit.
     */
    private static void printlog(String id, Commit commit) {
        DateFormat formatUs =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        System.out.println("===");
        System.out.println("commit " + id);
        if (commit.parents().size() == 2) {
            System.out.println("Merge: "
                    + commit.parent().substring(0, 7)
                    + " " + commit.parent2().substring(0, 7));
        }
        System.out.println("Date: " + formatUs.format(commit.time()));
        System.out.println(commit.message());
        System.out.println();
    }

    /**
     * ADD.
     *
     * @param filename filename.
     */
    public void add(String filename) {
        File add = new File(filename);
        if (!add.exists()) {
            Utils.messageq("File does not exist.");
        } else {
            String fileContent = Utils.readContentsAsString(add);
            String fileSha1 = Utils.sha1(fileContent);
            if (_removed.contains(filename)) {
                _removed.remove(filename);
            }
            if (_head.allfiles().get(filename) != null
                    && _head.allfiles().get(filename).equals(fileSha1)) {
                _staging.remove(filename);

            } else {
                File file = new File(".gitlet/staged/" + fileSha1);
                _staging.put(filename, fileSha1);
                Utils.writeContents(file, fileContent);
            }
        }
    }

    /**
     * COMMIT.
     * @param msg msg.
     */
    public void commit(String msg) {
        if (_staging.isEmpty() && _removed.isEmpty()) {
            Utils.messageq("No changes added to the commit.");
        } else {
            for (String filename : _head.allfiles().keySet()) {
                if (!_staging.containsKey(filename)) {
                    String rest = _head.allfiles().get(filename);
                    _staging.put(filename, rest);
                }
                if (_removed.contains(filename)) {
                    _staging.remove(filename);
                }
            }

            Commit newCommit = new Commit(msg, _head.id(), _staging);

            File rem = new File(".gitlet/committed/" + newCommit.id());
            Utils.writeObject(rem, newCommit);

            shaCommit.put(newCommit.id(), newCommit);
            namebranch.get(currBranch).clast(newCommit);
            _head = newCommit;
            _staging.clear();
            _removed.clear();
        }
    }

    /**
     * Remove.
     * @param filename filename.
     */
    public void rm(String filename) {
        File rm = new File(filename);
        if (!_staging.containsKey(filename)
                && !_head.allfiles().containsKey(filename)) {
            Utils.messageq("No reason to remove the file.");
        } else {
            if (_staging.containsKey(filename)) {
                _staging.remove(filename);
            }
            if (rm.exists() && _head.allfiles().containsKey(filename)) {
                rm.delete();
            }
            if (_head.allfiles().containsKey(filename)) {
                _removed.add(filename);
            }
        }
    }

    /**
     * log.
     */
    void log() {
        Commit point = _head;
        while (point != null) {
            printlog(point.id(), point);
            point = shaCommit.get(point.parent());
        }
    }

    /**
     * glog.
     */
    void globalLog() {
        File commitF = new File(".gitlet/committed");
        File[] allcommits = commitF.listFiles();

        for (File file : allcommits) {
            Commit c = Utils.readObject(file, Commit.class);
            printlog(c.id(), c);
        }
    }

    /**
     * find.
     * @param msg msg.
     */
    public void find(String msg) {
        File commitF = new File(".gitlet/committed");
        File[] allcommits = commitF.listFiles();
        boolean found = false;

        for (File file : allcommits) {
            Commit c = Utils.readObject(file, Commit.class);
            if (c.message().equals(msg)) {
                System.out.println(c.id());
                found = true;
            }
        }
        if (!found) {
            Utils.messageq("Found no commit with that message.");
        }
    }

    /**
     * status.
     */
    public void status() {
        System.out.println("=== Branches ===");
        Object[] branch = namebranch.keySet().toArray();
        Arrays.sort(branch);
        for (Object b : branch) {
            if (b.equals(currBranch)) {
                System.out.println("*" + b);
            } else {
                System.out.println(b);
            }
        }
        System.out.println();
        System.out.println("=== Staged Files ===");
        Object[] stage = _staging.keySet().toArray();
        Arrays.sort(stage);
        for (Object s : stage) {
            System.out.println(s);
        }
        System.out.println();
        System.out.println("=== Removed Files ===");
        Object[] remove = _removed.toArray();
        Arrays.sort(remove);
        for (Object r : remove) {
            System.out.println(r);
        }
        System.out.println();
        System.out.println("=== Modifications Not Staged For Commit ===");
        String all = System.getProperty("user.dir");
        File[] files = new File(all).listFiles();
        for (File file : files) {
            if (!file.isDirectory()) {
                String fileContent = Utils.readContentsAsString(file);
                String fileSha1 = Utils.sha1(fileContent);
                String filename = file.getName();
                if (_head.allfiles().containsKey(filename)
                        && !_head.allfiles().get(filename).equals(fileSha1)
                        && !_staging.containsKey(filename)) {
                    System.out.println(file.getName() + " (modified)");
                } else if (_staging.containsKey(filename)
                        && !_staging.get(filename).equals(fileSha1)) {
                    System.out.println(file.getName() + " (modified)");
                }
            }
        }
        status2();
    }

    /**
     * status.
     */
    public void status2() {
        String all = System.getProperty("user.dir");
        File[] files = new File(all).listFiles();
        for (String stagefile : _staging.keySet()) {
            File file = new File(stagefile);
            if (!file.exists()) {
                System.out.println(file.getName() + " (deleted)");
            }
        }
        for (String headfile : _head.allfiles().keySet()) {
            File file = new File(headfile);
            if (!_removed.contains(headfile) && !file.exists()) {
                System.out.println(file.getName() + " (deleted)");
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        for (File file : files) {
            if (!file.isDirectory()
                    && !_staging.containsKey(file.getName())
                    && !_head.allfiles().containsKey(file.getName())) {
                System.out.println(file.getName());
            }
        }
    }

    /**
     * checkout.
     * @param filename msg.
     */
    void checkoutid(String filename) {
        checkoutcommit(_head.id(), filename);
    }

    /**
     * checkout.
     * @param filename msg.
     * @param commitid id.
     */
    void checkoutcommit(String commitid, String filename) {
        if (commitid.length() != Utils.UID_LENGTH) {
            for (String id : shaCommit.keySet()) {
                if (id.contains(commitid)) {
                    commitid = id;
                }
            }
        }
        File file = new File(filename);
        if (!shaCommit.containsKey(commitid)) {
            Utils.messageq("No commit with that id exists.");
        } else if (!shaCommit.get(commitid).allfiles().containsKey(filename)) {
            Utils.messageq("File does not exist in that commit.");
        }
        String ks = shaCommit.get(commitid).allfiles().get(filename);
        File stagef = new File(".gitlet/staged/" + ks);
        String f = Utils.readContentsAsString(stagef);
        Utils.writeContents(file, f);

    }

    /**
     * checkout.
     * @param branchname msg.
     */
    void checkoutBranch(String branchname) {
        if (!namebranch.containsKey(branchname)) {
            Utils.messageq("No such branch exists.");
        } else if (namebranch.get(branchname).name().equals(currBranch)) {
            Utils.messageq("No need to checkout the current branch.");
        } else {
            Branch branch = namebranch.get(branchname);
            for (String s : branch.last().allfiles().keySet()) {
                File file = new File(s);
                if (!_head.allfiles().containsKey(s) && file.exists()) {
                    Utils.messageq("There is an "
                            + "untracked file in the way; "
                            + "delete it or add it first.");
                }
            }
            for (String file : branch.last().allfiles().keySet()) {
                checkoutcommit(branch.last().id(), file);
            }
            for (String file : _head.allfiles().keySet()) {
                File filein = new File(file);
                if (!branch.last().allfiles().containsKey(file)
                        && filein.exists()) {
                    filein.delete();
                }
            }
            currBranch = branch.name();
            _head = branch.last();
            _staging.clear();
        }
    }

    /**
     * checkout.
     *
     * @param branchname msg.
     */
    public void branch(String branchname) {
        if (namebranch.containsKey(branchname)) {
            Utils.messageq("A branch with that name already exists.");
        } else {
            Branch branch = new Branch(branchname, _head);
            namebranch.put(branchname, branch);
        }
    }

    /**
     * checkout.
     * @param branchname msg.
     */
    public void rmBranch(String branchname) {
        if (!namebranch.containsKey(branchname)) {
            Utils.messageq("A branch with that name does not exist.");
        } else if (namebranch.get(branchname).name().equals(currBranch)) {
            Utils.messageq("Cannot remove the current branch.");
        } else {
            namebranch.remove(branchname);
        }
    }

    /**
     * checkout.
     * @param commit msg.
     */
    public void reset(String commit) {
        if (commit.length() != Utils.UID_LENGTH) {
            for (String id : shaCommit.keySet()) {
                if (id.contains(commit)) {
                    commit = id;
                }
            }
        }
        File[] files = new File("./").listFiles();

        if (!shaCommit.containsKey(commit)) {
            Utils.messageq("No commit with that id exists.");
        }

        HashMap<String, String> alltrack = shaCommit.get(commit).allfiles();
        for (String name : alltrack.keySet()) {
            checkoutcommit(commit, name);
        }
        for (String name : _head.allfiles().keySet()) {
            if (!alltrack.containsKey(name)) {
                File untrack = new File(name);
                if (untrack.exists()) {
                    untrack.delete();
                }
            }
        }
        for (File file : files) {
            String fileName = file.getName();
            if (!_head.allfiles().containsKey(fileName)
                    && alltrack.containsKey(fileName)) {
                Utils.messageq("There is an untracked file "
                        + "in the way; delete it or add it first.");
            }
        }
        namebranch.get(currBranch).clast(shaCommit.get(commit));
        _head = namebranch.get(currBranch).last();
        _staging.clear();
        _removed.clear();
    }

    /**
     * checkout.
     * @param branchname msg.
     */
    public void merge(String branchname) {
        if (!namebranch.containsKey(branchname)) {
            Utils.messageq("A branch with that name does not exist.");
        } else if (!_staging.isEmpty() || !_removed.isEmpty()) {
            Utils.messageq("You have uncommitted changes.");
        } else if (namebranch.get(branchname).name().equals(currBranch)) {
            Utils.messageq("Cannot merge a branch with itself.");
        }
        Branch branch = namebranch.get(branchname);
        for (String s : branch.last().allfiles().keySet()) {
            if (!_head.allfiles().containsKey(s) && new File(s).exists()) {
                Utils.messageq("There is an untracked "
                        + "file in the way; delete it or add it first.");
            }
        }
        Commit split = splitpoint(branch);
        if (split.id().equals(_head.id())) {
            _head = branch.last();
            Utils.message("Current branch fast-forwarded.");
            for (String file : namebranch.get(currBranch)
                    .last().allfiles().keySet()) {
                if (!branch.last().allfiles().containsKey(file)) {
                    File theFile = new File(file);
                    if (theFile.exists()) {
                        theFile.delete();
                    }
                }
            }
            namebranch.get(currBranch).clast(branch.last());
            return;
        } else if (split.id().equals(branch.last().id())) {
            branch.clast(_head);
            Utils.message("Given branch is an "
                    + "ancestor of the current branch.");
            branch.clast(_head);
            return;
        }
        merge2(branchname);
    }

    /**
     * checkout.
     * @param branchname msg.
     */
    public void merge2(String branchname)  {
        Branch branch = namebranch.get(branchname);
        Commit split = splitpoint(branch);
        HashMap<String, String> bf = branch.last().allfiles();
        HashMap<String, String> cf = _head.allfiles();
        HashMap<String, String> sf = split.allfiles();
        for (String f : sf.keySet()) {
            if (bf.containsKey(f) && cf.containsKey(f)) {
                if (!bf.get(f).equals(sf.get(f))
                        && cf.get(f).equals(sf.get(f))) {
                    checkoutcommit(branch.last().id(), f);
                    add(f);
                } else if (!bf.get(f).equals(sf.get(f))
                        && !cf.get(f).equals(sf.get(f))
                        && !cf.get(f).equals(bf.get(f))) {
                    conflict(f, branchname);
                    Utils.message("Encountered a merge conflict.");
                }
            } else if (!bf.containsKey(f) && cf.containsKey(f)) {
                if (sf.get(f).equals(cf.get(f))) {
                    rm(f);
                } else {
                    conflict(f, branchname);
                    Utils.message("Encountered a merge conflict.");
                }
            } else if (bf.containsKey(f) && !cf.containsKey(f)) {
                if (!split.allfiles().get(f).equals(bf.get(f))) {
                    conflict(f, branchname);
                    Utils.message("Encountered a merge conflict.");
                }
            }
        }
        for (String file : bf.keySet()) {
            if (!sf.containsKey(file)) {
                if (cf.containsKey(file)) {
                    if (!cf.get(file).equals(bf.get(file))) {
                        conflict(file, branchname);
                        Utils.message("Encountered a merge conflict.");
                    }
                } else if (!cf.containsKey(file)) {
                    checkoutcommit(branch.last().id(), file);
                    add(file);
                }
            }
        }
        mcommit(branchname);
    }

    /**
     * checkout.
     * @param branchname msg.
     */
    public void mcommit(String branchname) {
        if (_staging.isEmpty() && _removed.isEmpty()) {
            Utils.messageq("No changes added to the commit.");
        } else {
            for (String name : _head.allfiles().keySet()) {
                if (!_staging.containsKey(name)) {
                    String file = _head.allfiles().get(name);
                    _staging.put(name, file);
                }
            }
            for (String name : _head.allfiles().keySet()) {
                if (_removed.contains(name)) {
                    _staging.remove(name);
                }
            }
            String msg = "Merged " + branchname
                    + " into " + currBranch + ".";
            String p1 = _head.id();
            String p2 = namebranch.get(branchname).last().id();

            Commit curr = new Commit(msg,
                    p1, p2, _staging);
            shaCommit.put(curr.id(), curr);
            namebranch.get(currBranch).clast(curr);
            _head = curr;

            _staging.clear();
            _removed.clear();
        }
    }

    /**
     * checkout.
     * @param branchname msg.
     * @param filename   asd.
     */
    private void conflict(String filename, String branchname) {
        String old = "";
        String change = "";

        File file = new File(filename);

        String changesha1 = namebranch.get(branchname)
                .last().allfiles().get(filename);
        String oldsha1 = _head.allfiles().get(filename);
        File olddir = new File(".gitlet/staged/" + oldsha1);
        File newdir = new File(".gitlet/staged/" + changesha1);

        if (olddir.exists()) {
            old = Utils.readContentsAsString(olddir);
        }
        if (newdir.exists()) {
            change = Utils.readContentsAsString(newdir);
        }
        String conff = "<<<<<<< HEAD\n";
        conff += old;
        conff += "=======\n";
        conff += change;
        conff += ">>>>>>>\n";
        if (file.exists()) {
            file.delete();
        }
        Utils.writeContents(file, conff);
        add(filename);

    }

    /**
     * splitpoint.
     * this idea is largely from zang zixiang.
     * usage of bfs and the general idea on geting
     * all the history of parents.
     * @param branchname msg.
     * @return dew.
     */
    private Commit splitpoint(Branch branchname) {
        HashSet<String> history = new HashSet<>();
        Stack<Commit> zang = new Stack<>();
        zang.push(branchname.last());
        while (!zang.empty()) {
            Commit begin = zang.pop();
            history.add(begin.id());
            for (String parent : begin.parents()) {
                if (!history.contains(parent)
                        && (!parent.equals(""))) {
                    zang.push(shaCommit.get(parent));
                }
            }
        }
        HashSet<String> visited = new HashSet<>();
        LinkedList<Commit> help = new LinkedList<>();
        help.add(_head);
        while (!help.isEmpty()) {
            Commit start = help.poll();
            if (visited.contains(start.id())) {
                continue;
            }
            if (history.contains(start.id())) {
                return start;
            }
            HashSet<String> parents = start.parents();
            for (String parent : parents) {
                help.add(shaCommit.get(parent));
            }
            visited.add(start.id());
        }
        throw new GitletException("No split point found!");
    }


    /**
     * checkout.
     * @param dir  msg.
     * @param name e.
     */
    void addRemote(String name, String dir) {
        if (_remotenamedir.containsKey(name)) {
            Utils.messageq("A remote with that name already exists.");
        } else {
            _remotenamedir.put(name, dir);

        }
    }

    /**
     * checkout.
     * @param name msg.
     */
    void rmRemote(String name) {
        if (!_remotenamedir.containsKey(name)) {
            Utils.messageq("A remote with that name does not exist.");
        } else {
            _remotenamedir.remove(name);
        }
    }

    /**
     * checkout.
     * @param remote     msg.
     * @param branchname sd.
     */
    void push(String remote, String branchname) {
        String remotedir = _remotenamedir.get(remote);
        if (!(new File(remotedir).exists())) {
            Utils.messageq("Remote directory not found.");
        } else {
            File repoPath = new File(remotedir + "/repo");
            Repo rrepo = Utils.readObject(repoPath, Repo.class);

            Commit copyhead = _head;
            HashSet<String> localparents = new HashSet<>();
            while (!copyhead.parent().equals("")) {
                localparents.add(copyhead.parent());
                if (copyhead.parent2() != null
                        && !copyhead.parent2().equals("")) {
                    localparents.add(copyhead.parent2());
                }
                copyhead = shaCommit.get(copyhead.parent());
            }
            Commit rhead = rrepo.namebranch.get(branchname).last();
            if (!localparents.contains(rhead.id())) {
                Utils.messageq("Please pull "
                       + "down remote changes before pushing.");
            }
            copyhead = _head;
            while (!copyhead.id().equals(rhead.id())) {
                rrepo.shaCommit.put(copyhead.id(), copyhead);
                for (String file : copyhead.allfiles().values()) {
                    File local = new File(".gitlet/staged/" + file);
                    File rstage = new File(remotedir + "/staged/" + file);
                    String content = Utils.readContentsAsString(local);
                    Utils.writeObject(rstage, content);
                }
                copyhead = shaCommit.get(copyhead.parent());
            }

            if (!rrepo.namebranch.containsKey(branchname)) {
                Branch newBranch = new Branch(branchname,
                        rrepo.namebranch.get(rrepo.currBranch).last());
                rrepo.namebranch.put(branchname, newBranch);
            }
            rrepo.namebranch.get(branchname).
                    clast(rrepo.shaCommit.get(_head.id()));
            rrepo._head = rrepo.namebranch.get(branchname).last();
            Utils.writeObject(repoPath, rrepo);

        }
    }

    /**
     * checkout.
     *
     * @param remote msg.
     * @param branchname sd.
     */
    void fetch(String remote, String branchname) {
        String remotedir = _remotenamedir.get(remote);
        if (!new File(remotedir).exists()) {
            Utils.messageq("Remote directory not found.");
        }
        File repoPath = new File(remotedir + "/repo");
        Repo rrepo = Utils.readObject(repoPath, Repo.class);
        if (!rrepo.namebranch.containsKey(branchname)) {
            Utils.messageq("That remote does not have that branch.");
        }
        Commit rhead = rrepo.namebranch.get(branchname).last();
        Commit copyrhead = rhead;
        while (!shaCommit.containsKey(copyrhead.id())) {
            shaCommit.put(copyrhead.id(), copyrhead);
            for (String file : copyrhead.allfiles().values()) {
                File rstage = new File(remotedir + "/staged/" + file);
                File local = new File(".gitlet/staged/" + file);
                String content = Utils.readContentsAsString(rstage);
                Utils.writeObject(local, content);
            }
            copyrhead = rrepo.shaCommit.get(copyrhead.parent());
        }
        String rbranchname = remote + "/" + branchname;
        Branch branch = new Branch(rbranchname, shaCommit.get(rhead.id()));
        namebranch.put(rbranchname, branch);
    }


    /**
     * checkout.
     * @param remote msg.
     * @param branchname sd.
     */
    void pull(String remote, String branchname) {
        String rbranchname = remote + "/" + branchname;
        fetch(remote, branchname);
        merge(rbranchname);
    }
}
