package malelm.com.gquery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class QueryReceiver extends BroadcastReceiver {
    public QueryReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context , QueryService.class);
        Toast.makeText(context,   "From the Receiver"  , Toast.LENGTH_SHORT).show();
        Log.d("QueryReceiver", "From the Receiver");
        context.startService(i);
    }
}
