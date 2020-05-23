package gitlet;

import java.io.Serializable;


/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Cindy Chen
 */
public class Branch implements Serializable {

    /**
     * checkout.
     */
    private String _name;
    /**
     * checkout.
     */
    private Commit _last;

    /**
     * checkout.
     *
     * @param namei asd.
     * @param headi da.
     */
    public Branch(String namei, Commit headi) {
        _name = namei;
        _last = headi;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public String name() {
        return _name;
    }

    /**
     * checkout.
     *
     * @return ads.
     */
    public Commit last() {
        return _last;
    }

    /**
     * checkout.
     *
     * @param o das.
     */
    public void clast(Commit o) {
        _last = o;
    }

}
