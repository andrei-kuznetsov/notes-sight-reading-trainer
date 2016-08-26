package name.kuznetsov.andrei.scoresightreading.model;

import java.util.LinkedList;
import java.util.List;

/**
 * @author andrei
 */
public class PolySeqRep {

    private List<PolyChord> chords;

    public PolySeqRep() {
        this.chords = new LinkedList<>();
    }

    public PolySeqRep(List<PolyChord> chords) {
        this.chords = chords;
    }

    public List<PolyChord> getChords() {
        return chords;
    }

    public void setChords(List<PolyChord> chords) {
        this.chords = chords;
    }

}
