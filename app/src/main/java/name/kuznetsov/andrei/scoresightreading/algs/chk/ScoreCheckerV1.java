package name.kuznetsov.andrei.scoresightreading.algs.chk;

import java.util.ListIterator;

import name.kuznetsov.andrei.scoresightreading.midi.MIDIEventListener;
import name.kuznetsov.andrei.scoresightreading.midi.MIDIShortMessage;
import name.kuznetsov.andrei.scoresightreading.model.AttrCheckedNote;
import name.kuznetsov.andrei.scoresightreading.model.CheckState;
import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.PolyChord;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;

/**
 * Created by andrei on 9/23/16.
 */

public class ScoreCheckerV1 {
    private final ListIterator<PolyChord> it;
    private PolyChord current;

    private PolySeqRep seq;

    private CheckerEventListener checkerEventListener;

    private final MIDIEventListener midiListenerImpl = new MIDIEventListener() {
        @Override
        public void onShortMessage(MIDIShortMessage msg) {
            boolean allSet = chkAllSet(current, msg);
            if (allSet) {
                if (it.hasNext()) {
                    current = it.next();
                } else {
                    checkerEventListener.endOfExerciese();
                }
            }

            checkerEventListener.modelChanged(seq);
        }
    };

    private static boolean chkAllSet(PolyChord current, MIDIShortMessage msg) {
        if (msg.getChannelCommand() == MIDIShortMessage.NOTE_ON) {
            int midiPitch = msg.getB1();

            for (Note i : current.getNotes()) {
                if (i.getMidiPitch() == midiPitch) {
                    AttrCheckedNote cn = i.obtainAttributeOrNull(AttrCheckedNote.class);
                    if (cn == null) {
                        cn = new AttrCheckedNote<CheckState>(CheckState.NONE);
                        i.attachAttribute(AttrCheckedNote.class, cn);
                    }
                    cn.setCheckState(CheckState.CORRECT);
                }
            }
        }

        for (Note i : current.getNotes()) {
            AttrCheckedNote cn = i.obtainAttributeOrNull(AttrCheckedNote.class);
            if (cn == null || cn.getCheckState() == null || cn.getCheckState() != CheckState.CORRECT) {
                return false;
            }
        }

        return true;
    }

    public ScoreCheckerV1(PolySeqRep seq) {
        this.seq = seq;
        it = seq.getChords().listIterator();
        if (it.hasNext()) {
            current = it.next();
        }
    }

    public MIDIEventListener getMidiListenerImpl() {
        return midiListenerImpl;
    }

    public void setCheckerEventListener(CheckerEventListener checkerEventListener) {
        this.checkerEventListener = checkerEventListener;
    }
}
