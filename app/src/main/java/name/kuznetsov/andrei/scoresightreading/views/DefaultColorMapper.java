package name.kuznetsov.andrei.scoresightreading.views;

import android.graphics.Color;

import name.kuznetsov.andrei.scoresightreading.model.Note;

/**
 * Created by andrei on 9/23/16.
 */

public class DefaultColorMapper implements ColorMapper {
    @Override
    public int mapColor(Note attr) {
        return Color.BLACK;
    }
}
