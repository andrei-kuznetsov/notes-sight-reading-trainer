package name.kuznetsov.andrei.scoresightreading.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.LinkedList;
import java.util.List;

import name.kuznetsov.andrei.scoresightreading.model.Note;
import name.kuznetsov.andrei.scoresightreading.model.NotesEnum;
import name.kuznetsov.andrei.scoresightreading.model.PolyChord;
import name.kuznetsov.andrei.scoresightreading.model.PolySeqRep;

/**
 * Created by andrei on 8/25/16.
 */
public class StaveView extends View {
    private static class InlineNote {
        int column;
        int noteLine;
        int voice;

        public InlineNote() {
        }

        public InlineNote(int column, int noteLine, int voice) {
            this.column = column;
            this.noteLine = noteLine;
            this.voice = voice;
        }
    }

    private List<InlineNote> notesToRender;

    public StaveView(Context context) {
        super(context);
        initStaveLinePoints();
    }

    public StaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initStaveLinePoints();
    }

    public StaveView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initStaveLinePoints();
    }

    private float staveLinePoints[];
    private int interlineInterval12 = 20; // 1/2nf of interline interval
    private int midLineOffset = interlineInterval12 * 2 * 7;

    // 1/2nd of interline interval
    private int getInterlineInterval12() {
        return interlineInterval12;
    }

    // interline interval
    private int getInterlineInterval() {
        return interlineInterval12 * 2;
    }

    private int getPosXInterval() {
        return getInterlineInterval() * 2;
    }

    private void initStaveLinePoints() {
        staveLinePoints = new float[5 * 4 + 5 * 4];
        for (int i = 0; i < staveLinePoints.length; i += 4) {
            staveLinePoints[i] = 0;
            staveLinePoints[i + 2] = 40;
            staveLinePoints[i + 1] = staveLinePoints[i + 3] = getInterlineInterval12() * (8 + i + ((i < 5 * 4) ? 0 : 4)) / 2;
        }

        initSample();
    }

    private void initSample() {
        List<PolyChord> chords = new LinkedList<>();
        PolySeqRep seq = new PolySeqRep(chords);

        int v0 = 6 * 7;
        int v1 = 2 * 7;
        for (int i = 0; i < 16; i++, v0--, v1++) {
            NotesEnum v0note = NotesEnum.values()[v0 % 7];
            int v0octave = v0 / 7;

            NotesEnum v1note = NotesEnum.values()[v1 % 7];
            int v1octave = v1 / 7;

            chords.add(PolyChord.mkChord(
                    new Note(v0note, v0octave, 0, 0),
                    new Note(v1note, v1octave, 0, 1)
            ));

            for (NotesEnum nn : NotesEnum.values()) {
//                chords.add(PolyChord.mkChord(new Note(nn, i, -1)));
//                chords.add(PolyChord.mkChord(new Note(nn, i, 1)));
            }
        }

        renderNotes(seq);
    }

    private static Paint paint = new Paint();

    static {
        paint.setColor(Color.BLACK);
        paint.setStrokeWidth(2.f);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        midLineOffset = (bottom + top) / 2;
        interlineInterval12 = (bottom - top) / ((7 * 2 + 5) * 2);
        for (int i = 0; i < 5; i++) {
            final int idxBase = i * 8;
            staveLinePoints[idxBase + 2] = staveLinePoints[idxBase + 6] = right - left;
            staveLinePoints[idxBase + 1] = staveLinePoints[idxBase + 3] = midLineOffset + ((i + 1) * getInterlineInterval());
            staveLinePoints[idxBase + 5] = staveLinePoints[idxBase + 7] = midLineOffset - ((i + 1) * getInterlineInterval());
        }
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        canvas.drawLines(staveLinePoints, paint);

        canvas.translate(getInterlineInterval(), 0);
        for (InlineNote i : notesToRender) {
            float cx = i.column * getPosXInterval();
            float cy = midLineOffset - i.noteLine * getInterlineInterval12();
            canvas.drawCircle(cx, cy, getInterlineInterval12() - 2, paint);

            // draw note flags
            boolean flagUp = ((i.voice & 1) == 0);
            if (flagUp) {
                float lnx = cx + getInterlineInterval12() - 3;

                canvas.drawLine(lnx, cy, lnx, cy - getInterlineInterval12() * 5, paint);
            } else {
                float lnx = cx - getInterlineInterval12() + 3;
                canvas.drawLine(lnx, cy, lnx, cy + getInterlineInterval12() * 5, paint);
            }

            // draw additional lines if needed
            if (i.noteLine < -11 || i.noteLine > 11 || i.noteLine == 0) {
                final int lnStart, lnEnd;
                if (i.noteLine == 0) {
                    lnStart = lnEnd = 0;
                } else if (i.noteLine < -11) {
                    lnStart = (i.noteLine | 1) - 1;
                    lnEnd = -11;
                } else {
                    lnStart = 12;
                    lnEnd = (i.noteLine | 1) - 1;
                }

                for (int exln = lnStart; exln <= lnEnd; exln += 2) {
                    final int lny = midLineOffset - exln * getInterlineInterval12();
                    canvas.drawLine(cx - getInterlineInterval() * 3 / 4, lny, cx + getInterlineInterval() * 3 / 4, lny, paint);
                }
            }
        }
        canvas.translate(-getInterlineInterval(), 0);
    }

    public void renderNotes(PolySeqRep seq) {
        int posX = 0;

        notesToRender = new LinkedList<>();
        for (PolyChord chord : seq.getChords()) {
            for (Note n : chord.getNotes()) {
                int offset = n.getNoteName().ordinal();
                int octaveBaseLine = (n.getOctave() - 4) * 7;
                int noteLine = octaveBaseLine + offset;
                notesToRender.add(new InlineNote(posX, noteLine, n.getVoice()));
            }

            posX++;
        }

        invalidate();
    }
}
