package gitlet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import static gitlet.Utils.sha1;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Cindy Chen
 */
public class Commit implements Serializable {

    /**
     * checkout.
     */
    private String _parent;
    /**
     * checkout.
     */
    private String _parent2;
    /**
     * checkout.
     */
    private String _message;
    /**
     * checkout.
     */
    private HashMap<String, String> _files;
    /**
     * checkout.
     */
    private Date _time;
    /**
     * checkout.
     */
    private String _id;

    /**
     * checkout.
     *
     * @param message1      fs.
     * @param parent1       d.
     * @param trackedFiles1 ad.
     */
    public Commit(String message1, String parent1,
                  HashMap<String, String> trackedFiles1) {
        _parent = parent1;
        _parent2 = null;
        _time = new Date();
        _message = message1;
        _files = new HashMap<>();
        _files.putAll(trackedFiles1);

        _id = sha1(_message, _time.toString(), _parent);

    }

    /**
     * checkout.
     *
     * @param message1      fs.
     * @param parent1       d.
     * @param parent2i      da.
     * @param trackedFiles1 ad.
     */
    public Commit(String message1, String parent1,
                  String parent2i, HashMap<String,
            String> trackedFiles1) {
        _parent = parent1;
        _parent2 = parent2i;
        _time = new Date();
        _message = message1;
        _files = new HashMap<>();
        _files.putAll(trackedFiles1);
        _id = sha1(_message, _time.toString(), _parent, _parent2);
    }


    /**
     * checkout.
     */
    public Commit() {
        _parent = "";
        _parent2 = "";
        _message = "initial commit";
        _time = new Date(0);
        _files = new HashMap<>();
        _id = sha1(_message, _time.toString(), _parent);
    }

    /**
     * checkout.
     *
     * @return dfs.
     */
    public HashSet<String> parents() {
        HashSet<String> parents = new HashSet<>();
        if (_parent != null) {
            parents.add(_parent);
        }
        if (_parent2 != null) {
            parents.add(_parent2);
        }
        return parents;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public String message() {
        return _message;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public HashMap<String, String> allfiles() {
        return _files;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public Date time() {
        return _time;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public String parent() {
        return _parent;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public String parent2() {
        return _parent2;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public String id() {
        return _id;
    }
}
