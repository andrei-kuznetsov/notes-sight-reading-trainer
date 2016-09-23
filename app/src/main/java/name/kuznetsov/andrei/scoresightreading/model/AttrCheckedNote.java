package name.kuznetsov.andrei.scoresightreading.model;

/**
 * Created by andrei on 9/23/16.
 */

public class AttrCheckedNote<T> {
    private T checkState;

    public AttrCheckedNote(T checkState) {
        this.checkState = checkState;
    }

    public T getCheckState() {
        return checkState;
    }

    public void setCheckState(T checkState) {
        this.checkState = checkState;
    }

}
