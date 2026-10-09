package local.kleine.sdcpp;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

public class MainActivity extends AppCompatActivity {
    private Activity mainActivity;
    private ActivityResultLauncher<Intent> launcher;
    private boolean ready = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        long availMB = (long) mi.availMem / (1024 * 1024);
        Toast.makeText(mainActivity, "available Memory: " + availMB + " MB", Toast.LENGTH_LONG).show();
        launcher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // Toast.makeText(mainActivity, result.getData().getStringExtra("result"), Toast.LENGTH_SHORT).show();
                        mainActivity.finish();
                        ready = true;
                    } else {
                        Toast.makeText(mainActivity, "ERROR: process killed, not enough memory", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!ready) {
            Intent intent = new Intent(this, SDActivity.class);
            launcher.launch(intent);
        } else {
            mainActivity.finish();
        }
    }

}
