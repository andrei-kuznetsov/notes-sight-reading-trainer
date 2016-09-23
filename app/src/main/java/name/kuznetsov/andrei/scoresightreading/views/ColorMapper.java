package name.kuznetsov.andrei.scoresightreading.views;

import android.graphics.Color;

import name.kuznetsov.andrei.scoresightreading.model.AttrCheckedNote;
import name.kuznetsov.andrei.scoresightreading.model.Note;

/**
 * Created by andrei on 9/23/16.
 */

public interface ColorMapper {
    int mapColor(Note attr);
}
