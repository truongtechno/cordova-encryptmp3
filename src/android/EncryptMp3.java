package cordova.plugin.encryptmp3;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaResourceApi;
import org.json.JSONArray;
import org.json.JSONException;

import android.net.Uri;
/**
 * This class echoes a string called from JavaScript.
 */
public class EncryptMp3 extends CordovaPlugin {

    private final String FILE_ENCRYPT = "encrypt";
    private final String FILE_DECRYPT = "decrypt";
    public final String FILE_DELETE = "delete";
    private final String MP3 = ".mp3";
    private final String TAG = "ks";
    private final int LENGHT_TRACK = 10 * 1024;


    @Override
    public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
        if (action.equals(FILE_ENCRYPT)) {
            String argument = args.getString(0);
            if (argument != null) {
                String name = getFileMp3(argument);
                String path = getPathMp3(argument);
                final String pathInput = path + name + MP3;
                final String pathOutput = path + name + TAG + MP3;
                this.cordova.getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {
                        handleFileBeforeReverse(pathInput.trim(),
                                pathOutput.trim(), callbackContext, true);
                    }
                });
                return true;
            }
        } else if (action.equals(FILE_DECRYPT)) {
            String argument = args.getString(0);
            if (argument != null) {
                String name = getFileMp3KS(argument);
                final String path = getPathMp3(argument);
                final String pathInput = path + name + MP3;
                final String pathOutput = path + name + TAG + MP3;
                this.cordova.getThreadPool().execute(new Runnable() {

                    @Override
                    public void run() {
//	                        CordovaResourceApi resourceApi = webView
//	                                .getResourceApi();
//	                        Uri uriInput = getUriForArg(path);
//	                        File folder = resourceApi.mapUriToFile(uriInput);
//	                        if (folder != null && folder.exists()) {
//	                            File[] listFile = folder.listFiles();
//	                            if (listFile.length > 0) {
//	                                for (int i = 0; i < listFile.length; i++) {
//	                                    File file = listFile[i];
//	                                    if (!file.getName().contains("ks")) {
//	                                        file.delete();
//	                                    }
//	                                }
//	                            }
//	                        }
                        handleFileBeforeReverse(pathOutput.trim(),
                                pathInput.trim(), callbackContext, false);
                    }
                });
                return true;
            }
        } else if (action.equals(FILE_DELETE)) {
            String argument = args.getString(0);
            final String path = getPathMp3(argument);
            this.cordova.getThreadPool().execute(new Runnable() {

                @Override
                public void run() {
                    CordovaResourceApi resourceApi = webView.getResourceApi();
                    Uri uriInput = getUriForArg(path);
                    File folder = resourceApi.mapUriToFile(uriInput);
                    if (folder != null && folder.exists()) {
                        File[] listFile = folder.listFiles();
                        if (listFile.length > 0) {
                            for (int i = 0; i < listFile.length; i++) {
                                File file = listFile[i];
                                if (!file.getName().contains("ks")) {
                                    file.delete();
                                }
                            }
                        }
                    }
                }
            });
            
            return true;
        }
        return false;
    }

    private String getFileMp3(String url) {
        String name = "";
        name = url.substring(url.lastIndexOf("/") + 1, url.length() - 4);
        return name;
    }

    private String getFileMp3KS(String url) {
        String name = "";
        name = url.substring(url.lastIndexOf("/") + 1, url.length() - 6);
        return name;
    }

    private String getPathMp3(String url) {
        String path = "";
        path = url.substring(0, url.lastIndexOf("/") + 1);
        return path;
    }

    private void handleFileBeforeReverse(String fileInput, String fileOutput,
            CallbackContext callback, boolean isEncrypt) {
        try {
            CordovaResourceApi resourceApi = webView.getResourceApi();
            Uri uriInput = getUriForArg(fileInput);
            File fileIn = resourceApi.mapUriToFile(uriInput);
            if (fileIn == null || !fileIn.exists()) {
                callback.error("File not exit");
                return;
            }
            File fileOut = resourceApi.mapUriToFile(getUriForArg(fileOutput));
            fileOut.createNewFile();
            reverseFile(fileIn, fileOut, LENGHT_TRACK, callback, isEncrypt);
        } catch (Exception e) {
            callback.error("Exception:" + e.getMessage());
        }
    }

    public void reverseFile(File en, File de, int lenTrack,
            CallbackContext callbackContext, boolean isEncrypt) {
        System.out.println("Start decrypt=========>" + new Date().toString());
        InputStream fin = null;
        OutputStream bos = null;
        try {
            if (en.exists()) {
                System.out.println("File exits");
            }
            if (de.exists()) {
                de.delete();
            } else {
                de.createNewFile();
            }
            byte[] fileContent = new byte[lenTrack];
            byte[] reverseContent = new byte[lenTrack];
            try {
                fin = new FileInputStream(en);
                bos = new FileOutputStream(de, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (fin.read(fileContent) > 0) {
                for (int j = 0; j < lenTrack; j++) {
                    reverseContent[j] = fileContent[lenTrack - j - 1];
                }
                bos.write(reverseContent);
                bos.flush();
            }

            bos.close();
            fin.close();
            if (isEncrypt) {
                if (en.exists()) {
                    en.delete();
                }
            }
            callbackContext.success("success");
            System.out.println("End decrypt=========>" + new Date().toString());
        } catch (IOException e) {
            callbackContext.error("failed");
        }
    }

    private Uri getUriForArg(String arg) {
        CordovaResourceApi resourceApi = webView.getResourceApi();
        Uri tmpTarget = Uri.parse(arg);
        return resourceApi.remapUri(tmpTarget.getScheme() != null ? tmpTarget
                : Uri.fromFile(new File(arg)));
    }
}

