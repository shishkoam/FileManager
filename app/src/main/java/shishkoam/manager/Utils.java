package shishkoam.manager;

import android.os.Build;
import android.os.Environment;
import android.os.StatFs;

import java.io.File;

/**
 * Created by ав on 27.12.2015.
 */
public class Utils {
    public static float megabytesAvailableInExternalStorage() {
        return megabytesAvailable(Environment.getExternalStorageDirectory());
    }

    public static float  TotalExtMemory()  {
        return TotalMemory(Environment.getExternalStorageDirectory());
    }

    public static float megabytesAvailableInMobileStorage() {
        return megabytesAvailable(Environment.getDataDirectory());
    }

    public static float  TotalMobileMemory()  {
        return TotalMemory(Environment.getDataDirectory());
    }

    public static float megabytesAvailable(File file) {
        long bytesAvailable = 0;
        StatFs stat = new StatFs(file.getPath());
        if(Build.VERSION.SDK_INT >= 18){
            bytesAvailable = (long)stat.getBlockSizeLong() * (long)stat.getAvailableBlocksLong();
        }
        else{
            bytesAvailable = (long)stat.getBlockSize() * (long)stat.getAvailableBlocks();
        }
        return bytesAvailable / (1024.f * 1024.f);
    }


    public static float  TotalMemory(File file) {
        StatFs statFs = new StatFs(file.getAbsolutePath());
        float total = ((long)statFs.getBlockCount() * (long) statFs.getBlockSize()) / (1024.f * 1024.f);
        return total;
    }
}
