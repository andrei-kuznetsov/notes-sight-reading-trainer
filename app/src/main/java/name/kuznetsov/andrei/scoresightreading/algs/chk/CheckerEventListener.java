package name.kuznetsov.andrei.scoresightreading.algs.chk;

import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;

/**
 * Important! These callbacks can be invoked from background thread.
 */
public interface CheckerEventListener {
    void endOfExerciese();
    void modelChanged(PolySeqRep seq);
}
