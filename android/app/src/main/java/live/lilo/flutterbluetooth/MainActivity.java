package live.lilo.flutterbluetooth;

import io.flutter.embedding.android.FlutterActivity;
import com.datecs.pinpad.DeviceInfo;
import com.paypad.cardreader.controllers.PinpadManager;
import com.paypad.cardreader.extras.ReferenceList;
import com.paypad.cardreader.facade.PinpadFacade;
import com.paypad.cardreader.utils.Globals;
import com.paypad.cardreader.utils.MiscUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.TwoLineListItem;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.paypad.cardreader.facade.PinpadFacade;
import com.paypad.impl.Paypad;

import java.io.IOException;

public class MainActivity extends FlutterActivity {

}
