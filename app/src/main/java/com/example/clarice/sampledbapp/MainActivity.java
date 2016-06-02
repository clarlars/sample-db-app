package com.example.clarice.sampledbapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import org.opendatakit.database.DatabaseConsts;
import org.opendatakit.database.OdkDbSerializedInterface;
import org.opendatakit.database.service.OdkDbHandle;
import org.opendatakit.database.service.OdkDbInterface;

import java.util.List;

public class MainActivity extends AppCompatActivity implements ServiceConnection {

  Activity thisActivity;
  String appName = "default";
  private boolean mBound = false;
  private static final String TAG = MainActivity.class.getSimpleName();
  private OdkDbSerializedInterface databaseServiceInterface = null;

  @Override public void onServiceConnected(ComponentName name, IBinder service) {
    if (!name.getClassName().equals(DatabaseConsts.DATABASE_SERVICE_CLASS)) {
      Log.i(TAG, "[onServiceConnected] Unrecognized service");
      return;
    }

    databaseServiceInterface = (service == null) ? null :
            new OdkDbSerializedInterface(OdkDbInterface.Stub.asInterface(service));
    setBound(true);
    Log.i(TAG, "[onServiceConnected] Bound to database service");
  }

  @Override public void onServiceDisconnected(ComponentName name) {
    Log.i(TAG, "[onServiceDisconnected] Unbound to database service");
    setBound(false);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    thisActivity = this;

    Button getTablesButton = (Button) findViewById(R.id.button2);
    if (getTablesButton != null) {
      getTablesButton.setOnClickListener(new View.OnClickListener() {

        public void onClick(View v) {
          if (getBound()) {
            List<String> tableIds;
            OdkDbHandle db;
            try {
              db = databaseServiceInterface.openDatabase(appName);
              tableIds = databaseServiceInterface.getAllTableIds(appName, db);
              databaseServiceInterface.closeDatabase(appName, db);
              ListView listView = (ListView) findViewById(R.id.listView);
              ArrayAdapter adapter = new ArrayAdapter<>(thisActivity, android.R.layout.simple_list_item_1, android.R.id.text1, tableIds);
              if (listView != null) {
                listView.setAdapter(adapter);
              }
            } catch (RemoteException re) {
              re.printStackTrace();
            }
          }
        }
      });
    }

    try {
      Intent bind_intent = new Intent();
      bind_intent.setClassName(DatabaseConsts.DATABASE_SERVICE_PACKAGE,
              DatabaseConsts.DATABASE_SERVICE_CLASS);
      bindService(bind_intent, this,
              Context.BIND_AUTO_CREATE | ((Build.VERSION.SDK_INT >= 14) ?
                      Context.BIND_ADJUST_WITH_ACTIVITY :
                      0));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  protected void onDestroy() {

    if (getBound()) {
      unbindService(this);
      setBound(false);
      Log.i(TAG, " [onDestroy] Unbound to sync service");
    }

    super.onDestroy();
  }

  private void setBound (boolean bound) {
    mBound = bound;
  }

  private boolean getBound() {
    return mBound;
  }

}
