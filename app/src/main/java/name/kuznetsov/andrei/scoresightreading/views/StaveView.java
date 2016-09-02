package name.kuznetsov.andrei.scoresightreading.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
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
    private PolySeqRep notes;

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
    private float measureBarPoints[];
    private int interlineInterval12 = 20; // (1/2) half of interline interval
    private int midLineOffset = interlineInterval12 * 2 * 7;
    private int maxColumn = 16;
    private final int measureColumn = 4;

    public int getMaxColumn() {
        return maxColumn;
    }

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
        measureBarPoints = new float[2 * (4 * getMaxColumn() / measureColumn)];
        initSample();
    }

    private void initSample() {
        List<PolyChord> chords = new LinkedList<>();
        PolySeqRep seq = new PolySeqRep(chords);

        int v0 = 6 * 7;
        int v1 = 2 * 7;
        for (int i = 0; i < 24; i++, v0--, v1++) {
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

    private static Paint strokePaint = new Paint();
    private static Paint strokeAndFillPaint = new Paint();

    static {
        strokePaint.setColor(Color.BLACK);
        strokePaint.setStrokeWidth(2.f);
        strokePaint.setStyle(Paint.Style.STROKE);

        strokeAndFillPaint.setColor(Color.BLACK);
        strokeAndFillPaint.setStrokeWidth(2.f);
        strokeAndFillPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        midLineOffset = (bottom + top) / 2;
        maxColumn = (right - left) / getPosXInterval();

        interlineInterval12 = (bottom - top) / ((7 * 2 + 5) * 2);
        for (int i = 0; i < 5; i++) {
            final int idxBase = i * 8;
            staveLinePoints[idxBase + 2] = staveLinePoints[idxBase + 6] = right - left;
            staveLinePoints[idxBase + 1] = staveLinePoints[idxBase + 3] = midLineOffset + ((i + 1) * getInterlineInterval());
            staveLinePoints[idxBase + 5] = staveLinePoints[idxBase + 7] = midLineOffset - ((i + 1) * getInterlineInterval());
        }

        for (int i = 0; i < maxColumn / measureColumn; i++) {
            final int idxBase = i * 8;
            measureBarPoints[idxBase + 0] =
                    measureBarPoints[idxBase + 2] =
                            measureBarPoints[idxBase + 4] =
                                    measureBarPoints[idxBase + 6] =
                                            (i + 1) * getPosXInterval() * measureColumn;

            measureBarPoints[idxBase + 1] = midLineOffset - 5 * getInterlineInterval();
            measureBarPoints[idxBase + 3] = midLineOffset - 1 * getInterlineInterval();
            measureBarPoints[idxBase + 5] = midLineOffset + 5 * getInterlineInterval();
            measureBarPoints[idxBase + 7] = midLineOffset + 1 * getInterlineInterval();
        }
    }

    final static float _1sqrt2 = 0.707106781f; // 1/sqrt(2)

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // draw horizontal stave lines
        canvas.drawLines(staveLinePoints, strokePaint);

        // draw vertical measure bars
        canvas.drawLines(measureBarPoints, strokePaint);

        canvas.translate(getInterlineInterval(), 0);

        for (InlineNote i : notesToRender) {
            float cx = i.column * getPosXInterval();
            float cy = midLineOffset - i.noteLine * getInterlineInterval12();

            float ovalR = getInterlineInterval12() * 0.65f;
            float ovalRX = (ovalR * 1.3f) / 0.75f; //getInterlineInterval12() + 4;
            float ovalRY = ovalR; //getInterlineInterval12() - 4;

            Path bezierPath = new Path();

            bezierPath.moveTo(cx - ovalRY * _1sqrt2, cy - ovalRY * _1sqrt2);
            bezierPath.cubicTo(
                    cx + (ovalRX - ovalRY) * _1sqrt2, cy - (ovalRY + ovalRX) * _1sqrt2,
                    cx + (ovalRX + ovalRY) * _1sqrt2, cy + (ovalRY - ovalRX) * _1sqrt2,
                    cx + ovalRY * _1sqrt2, cy + ovalRY * _1sqrt2);

            bezierPath.cubicTo(
                    cx + (ovalRY - ovalRX) * _1sqrt2, cy + (ovalRX + ovalRY) * _1sqrt2,
                    cx - (ovalRX + ovalRY) * _1sqrt2, cy + (ovalRX - ovalRY) * _1sqrt2,
                    cx - ovalRY * _1sqrt2, cy - ovalRY * _1sqrt2
            );
            bezierPath.close();
            canvas.drawPath(bezierPath, strokeAndFillPaint);

            boolean flagUp = ((i.voice & 1) == 0);
            if (flagUp) {
                float lnx = cx + getInterlineInterval12() - 3;
                float ystem = cy - getInterlineInterval12() * 7 + 5;
                canvas.drawLine(lnx, cy, lnx, ystem, strokePaint);

                // draw note flags
                /// note flag 1
                bezierPath.moveTo(lnx, ystem);
                bezierPath.cubicTo(
                        lnx, ystem + getInterlineInterval12(),
                        lnx + getInterlineInterval12() * 2.5f, ystem + getInterlineInterval12() * 2,
                        lnx + getInterlineInterval12() * 1.5f, ystem + getInterlineInterval12() * 4
                );
                bezierPath.cubicTo(
                        lnx + getInterlineInterval12() * 2.5f, ystem + getInterlineInterval12() * 2,
                        lnx, ystem + 1.5f * getInterlineInterval12(),
                        lnx, ystem + 1.5f * getInterlineInterval12()
                );
                bezierPath.close();
                canvas.drawPath(bezierPath, strokeAndFillPaint);

                canvas.translate(0, getInterlineInterval12() * 1.5f);

                /// note flag 2
                bezierPath.reset();
                bezierPath.moveTo(lnx, ystem);
                bezierPath.cubicTo(
                        lnx, ystem + getInterlineInterval12(),
                        lnx + getInterlineInterval12() * 2.5f, ystem + getInterlineInterval12() * 2,
                        lnx + getInterlineInterval12() * 1.5f, ystem + getInterlineInterval12() * 4
                );
                bezierPath.cubicTo(
                        lnx + getInterlineInterval12() * 2.5f, ystem + getInterlineInterval12() * 2,
                        lnx, ystem + 1.5f * getInterlineInterval12(),
                        lnx, ystem + 1.5f * getInterlineInterval12()
                );
                bezierPath.close();
                canvas.drawPath(bezierPath, strokeAndFillPaint);
                /// end of note flag

                canvas.translate(0, -getInterlineInterval12() * 1.5f);
            } else {
                float lnx = cx - getInterlineInterval12() + 3;
                canvas.drawLine(lnx, cy, lnx, cy + getInterlineInterval12() * 5, strokePaint);
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
                    canvas.drawLine(cx - getInterlineInterval() * 3 / 4, lny, cx + getInterlineInterval() * 3 / 4, lny, strokePaint);
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

    public PolySeqRep getNotes() {
        return notes;
    }
}
