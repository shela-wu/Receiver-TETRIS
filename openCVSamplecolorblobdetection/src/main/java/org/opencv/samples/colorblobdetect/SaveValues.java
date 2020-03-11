package org.opencv.samples.colorblobdetect;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;

/**
 * Created by Matth on 7/11/2017.
 */

public class SaveValues {
    private int BlockSquare;
    private int SquareWidth;
    private int SquareHeight;
    private Calendar calendar;
    String filename = "";
    FileOutputStream outputStream;
    FileOutputStream fileOutputStream;

    public SaveValues(Context context, int BlockSize, int SquareWidth, int SquareHeight, String name) {
        this.BlockSquare = BlockSize;
        this.SquareWidth = SquareWidth;
        this.SquareHeight = SquareHeight;
        calendar = Calendar.getInstance();
        filename = "I_SAW_" + name + ".txt";
        try {
            fileOutputStream = new FileOutputStream(getAlbumStorageDir(context, filename));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void saveBarCode(final Activity activity, final Context context, byte[] colors) {
       try {
           Log.e("save","save");
           BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(getAlbumStorageDir(context, filename)));
           Long start = System.currentTimeMillis();
           bos.write(colors);
           Long duration = System.currentTimeMillis()-start;
           System.currentTimeMillis();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }


    public void close() {
        try {

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

    public File getAlbumStorageDir(Context context, String albumName) throws IOException {
        // Get the directory for the app's private pictures directory.
        File file = new File(context.getExternalFilesDir("CobraReceived"), albumName);
        if (!file.exists()) {
            file.createNewFile();
        }
        if (!file.mkdirs()) {
            Log.e("file error", "Directory not created");
        }
        return file;
    }
}