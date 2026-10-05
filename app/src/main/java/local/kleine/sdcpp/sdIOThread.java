package local.kleine.sdcpp;

import android.app.Activity;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

class sdIOThread extends Thread implements Runnable {
    public static Process process = null;
    private volatile SDActivity myActivity;

    sdIOThread(SDActivity parent, List<String> arguments, String sdWorkPath, String sdLibraryPath ) {
        myActivity = parent;
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(arguments);
            if (!sdLibraryPath.isEmpty()) {
                Map<String, String> environment = processBuilder.environment();
                environment.put("LD_LIBRARY_PATH", sdLibraryPath);
            }
            processBuilder.directory(new File(sdWorkPath));
            processBuilder.redirectErrorStream(true);
            process = processBuilder.start();
            myActivity.processInfo("sd.cpp started");
        } catch (Exception e) {
            Toast.makeText(myActivity, e.toString(), Toast.LENGTH_SHORT).show();
            myActivity.subFinished(SDActivity.EXIT_CODE_CAN_NOT_RUN);
            myActivity.setResult(Activity.RESULT_CANCELED);
            myActivity.finishAndRemoveTask();
        }
    }

    public void updateActivity(SDActivity a) {
        myActivity = a;
    }

    public void processDestroy() {
        if (process != null) {
            try {
                process.destroy();
            } catch (Exception ignored) {
            } finally {
                process = null;
            }
        }
    }

    @Override
    public void run() {
        myActivity.lockScreenDim();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))){
            String line;
            while (process != null && (line = reader.readLine()) != null) {
                if (!line.isEmpty()) {
                    myActivity.debugMsg(line);
                }
            }
            int exitCode = (process == null) ? SDActivity.EXIT_CODE_CANCELLED : process.waitFor();
            myActivity.subFinished(exitCode);
        } catch (java.io.IOException e) {
            myActivity.subFinished(SDActivity.EXIT_CODE_CANCELLED);
        } catch (Exception e) {
            SDActivity a = myActivity;  // instead of synchronized()
            a.exceptionDescription = e.getMessage();
            a.subFinished(SDActivity.EXIT_CODE_EXCEPTION);
        } finally {
            processDestroy();
            myActivity.restoreScreenBrightness();
        }
    }
}

