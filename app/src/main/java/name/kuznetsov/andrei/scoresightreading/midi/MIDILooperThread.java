package name.kuznetsov.andrei.scoresightreading.midi;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

/**
 * Created by andrei on 9/23/16.
 */

class MIDILooperThread extends Thread {
    private static final String TAG = "MIDILooperThread";
    public Handler mHandler;

    private volatile Messenger mMessenger;
    private volatile MIDIEventListener listener = null;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MIDIServiceConnection.MSG_MIDI_EVENT:
                    MIDIShortMessage midimsg = MIDIShortMessage.deserialize(msg.arg1);
                    Log.d(TAG, midimsg.toString());
                    if (listener != null) {
                        listener.onShortMessage(midimsg);
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    public void run() {
        Looper.prepare();
        mHandler = new IncomingHandler();
        mMessenger = new Messenger(mHandler);
        Looper.loop();
    }

    public void setListener(MIDIEventListener listener) {
        this.listener = listener;
    }

    public Messenger getMessenger() {
        return mMessenger;
    }
}