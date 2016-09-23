package name.kuznetsov.andrei.scoresightreading.views;

import android.graphics.Color;

import java.util.EnumMap;
import java.util.Map;

import name.kuznetsov.andrei.scoresightreading.model.AttrCheckedNote;
import name.kuznetsov.andrei.scoresightreading.model.CheckState;
import name.kuznetsov.andrei.scoresightreading.model.Note;

/**
 * Created by andrei on 9/23/16.
 */

public class CheckStateColorMapper implements ColorMapper {
    private static Map<CheckState, Integer> colormap = new EnumMap<>(CheckState.class);

    static {
        colormap.put(CheckState.ACCEPTABLE, Color.YELLOW);
        colormap.put(CheckState.ACTUAL, Color.BLUE);
        colormap.put(CheckState.CORRECT, Color.GREEN);
        colormap.put(CheckState.EXPECTED, Color.DKGRAY);
        colormap.put(CheckState.INCORRECT, Color.RED);
        colormap.put(CheckState.NONE, Color.BLACK);
    }

    @Override
    public int mapColor(Note note) {
        AttrCheckedNote<CheckState> attr = note.obtainAttributeOrNull(AttrCheckedNote.class);

        if (attr == null || attr.getCheckState() == null){
            return Color.BLACK;
        } else {
            Integer res = colormap.get(attr.getCheckState());
            if (res == null) {
                return Color.BLACK;
            } else {
                return res;
            }
        }
    }
}
