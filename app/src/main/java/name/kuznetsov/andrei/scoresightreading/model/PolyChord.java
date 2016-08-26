package name.kuznetsov.andrei.scoresightreading.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author andrei
 */
public class PolyChord {

    public static PolyChord mkChord(Note... notes) {
        return new PolyChord(Arrays.asList(notes));
    }

    private List<Note> notes;

    public PolyChord(List<Note> notes) {
        this.notes = notes;
    }

    public PolyChord() {
        this.notes = new LinkedList<>();
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }
}
