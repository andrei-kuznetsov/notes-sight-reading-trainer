package name.kuznetsov.andrei.scoresightreading.model;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author andrei
 */
public class PolyChord {

    public static PolyChord mkChord(DurationsEnum duration, Note... notes) {
        return new PolyChord(Arrays.asList(notes), duration);
    }

    public static PolyChord mkChord(Note... notes) {
        return new PolyChord(Arrays.asList(notes));
    }

    private List<Note> notes;
    private DurationsEnum duration;

    public PolyChord(List<Note> notes, DurationsEnum duration) {
        this.notes = notes;
        this.duration = duration;
    }

    public PolyChord(DurationsEnum duration) {
        this(new LinkedList<Note>(), duration);
    }

    public PolyChord(List<Note> notes) {
        this(notes, DurationsEnum.QUARTER_NOTE);
    }

    public PolyChord() {
        this(new LinkedList<Note>(), DurationsEnum.QUARTER_NOTE);
    }

    public List<Note> getNotes() {
        return notes;
    }

    public void setNotes(List<Note> notes) {
        this.notes = notes;
    }

    public DurationsEnum getDuration() {
        return duration;
    }

    public void setDuration(DurationsEnum duration) {
        this.duration = duration;
    }

    public void addNote(Note note){
        notes.add(note);
    }

    public PolyChord copy() {
        PolyChord copy = new PolyChord(duration);
        for (Note i : notes) {
            copy.addNote(i.copy());
        }
        return copy;
    }
}
