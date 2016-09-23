package name.kuznetsov.andrei.scoresightreading.midi;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by andrei on 9/22/16.
 */

public class MIDIShortMessage {

    public static final int CMD_MASK = 0x0F0;
    public static final int NOTE_ON = 0x090;
    public static final int NOTE_OFF = 0x080;
    public static final int SYSTEM_RESET = 0x0FF;
    public static final String TAG = "MSM";

    private byte status;
    private byte b1;
    private byte b2;

    public MIDIShortMessage() {
    }

    public MIDIShortMessage(byte status, byte b1, byte b2) {
        this.status = status;
        this.b1 = b1;
        this.b2 = b2;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public byte getB1() {
        return b1;
    }

    public void setB1(byte b1) {
        this.b1 = b1;
    }

    public byte getB2() {
        return b2;
    }

    public void setB2(byte b2) {
        this.b2 = b2;
    }

    public int getChannelCommand() {
        return getChannelCommand(status);
    }

    public static int getChannelCommand(int status) {
        return (status & CMD_MASK);
    }

    public static boolean isStatusByte(byte status) {
        return (status & 0x080) != 0;
    }

    public int serializeToInt() {
        return ((status & 0x0FF) << 16) | ((b1 & 0x0FF) << 8) | ((b2 & 0x0FF));
    }

    public static MIDIShortMessage deserialize(int serial) {
        return new MIDIShortMessage((byte) ((serial >> 16) & 0x0FF), (byte) ((serial >> 8) & 0x0FF), (byte) (serial & 0x0FF));
    }

    public static List<MIDIShortMessage> assembleMidiMessage(Deque<Byte> assembledInput, byte[] bufferIn, int numBytesRead) {
        if (numBytesRead == 0) {
            return Collections.EMPTY_LIST;
        } else {
            for (int i = 0; i < numBytesRead; i++) {
                assembledInput.offer(bufferIn[i]);
            }

            if (assembledInput.size() >= 3) {
                List<MIDIShortMessage> res = new LinkedList<>();

                while (assembledInput.size() >= 3) {
                    byte status = assembledInput.poll();
                    switch (getChannelCommand(status)) {
                        case NOTE_ON:
                        case NOTE_OFF:
                            byte b1 = assembledInput.pollFirst();
                            if (isStatusByte(b1)){
                                assembledInput.addFirst(b1);
                                continue; // this is not a valid note event. Continue with the other midi message.
                            }
                            byte b2 = assembledInput.pollFirst();
                            if (isStatusByte(b2)){
                                assembledInput.addFirst(b2);
                                continue; // this is not a valid note event. Continue with the other midi message.
                            }
                            res.add(new MIDIShortMessage(status, b1, b2));

                            // remember about running status
                            assembledInput.addFirst(status);
                            break;
                        default:
                            // just ignore unknown
                    }
                }

                return res;
            } else {
                return Collections.EMPTY_LIST;
            }
        }
    }

    @Override
    public String toString() {
        return "MIDIShortMessage{" +
                "status=" + Integer.toHexString(status & 0x0FF) +
                ", b1=" + Integer.toHexString(b1 & 0x0FF) +
                ", b2=" + Integer.toHexString(b2 & 0x0FF) +
                '}';
    }
}
