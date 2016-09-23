package name.kuznetsov.andrei.scoresightreading.midi;

/**
 * Created by andrei on 9/23/16.
 */

public interface MIDIEventListener {
    void onShortMessage(MIDIShortMessage msg);
}
