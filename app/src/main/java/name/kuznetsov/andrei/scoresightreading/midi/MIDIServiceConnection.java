package name.kuznetsov.andrei.scoresightreading.midi;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by andrei on 9/23/16.
 */

public class MIDIServiceConnection implements ServiceConnection {

    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_MIDI_EVENT = 3;
    private static final String TAG = "MIDIServiceConnection";
    public static final String MIDI_SERVICE_APP_NAME = "name.kuznetsov.andrei.usbtest";
    public static final String MIDI_SERVICE_CLASS_NAME = "name.kuznetsov.andrei.usbtest.MidiService";

    private Messenger mService;
    private final Context context;

    public MIDIServiceConnection(Context context) {
        this.context = context;
    }

    private MIDILooperThread looperThread;

    @Override
    public void onServiceConnected(ComponentName className, IBinder service) {
        mService = new Messenger(service);
        try {
            Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
            msg.replyTo = looperThread.getMessenger();
            mService.send(msg);
            Toast.makeText(context, "MIDI service connected", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "MIDI service connected");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName className) {
        mService = null;
        Toast.makeText(context, "MIDI service disconnected", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "MIDI service disconnected");
    }

    public void disconnectMidiService() {
        if (mService != null) {
            try {
                Message msg = Message.obtain(null, MSG_REGISTER_CLIENT);
                msg.replyTo = looperThread.getMessenger();
                ;
                mService.send(Message.obtain(null, MSG_UNREGISTER_CLIENT));
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            mService = null;
            context.unbindService(this);
        }

        setListener(null);
    }

    public void connectMidiService() {
        Intent i = getIntent();
        boolean res = context.bindService(i, this, Context.BIND_AUTO_CREATE);
        if (res == false) {
            Toast.makeText(context, "Cannot connect to MIDI service", Toast.LENGTH_SHORT).show();
            return;
        }
        if (looperThread == null) {
            looperThread = new MIDILooperThread();
            looperThread.start();
        }
    }

    @NonNull
    public static  Intent getIntent() {
        Intent i = new Intent();
        i.setClassName(MIDI_SERVICE_APP_NAME, MIDI_SERVICE_CLASS_NAME);
        return i;
    }

    public void setListener(MIDIEventListener listener) {
        looperThread.setListener(listener);
    }
}
