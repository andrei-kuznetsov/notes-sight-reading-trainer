package name.kuznetsov.andrei.scoresightreading.algs;

import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;

/**
 * Created by andrei on 9/1/16.
 */
public interface PolySeqGenerator {
    public PolySeqRep genPolySeq(int nVoices, final Note minNote, final Note maxNote, final int maxSplit, int notesCount);
}
