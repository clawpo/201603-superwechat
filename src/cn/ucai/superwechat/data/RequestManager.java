package cn.ucai.superwechat.data;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;
import com.jakewharton.disklrucache.DiskLruCache;

import java.io.File;
import java.io.IOException;

public class RequestManager {
	private static RequestQueue mRequestQueue;
	private static ImageLoader mImageLoader;

    /**
     * 图片硬盘缓存核心类。
     */
    private static DiskLruCache mDiskLruCache;

	private RequestManager() {
		// no instances
	}

	public static void init(Context context) {
		mRequestQueue = Volley.newRequestQueue(context);

		int memClass = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE))
				.getMemoryClass();
		// Use 1/8th of the available memory for this memory cache.
		int cacheSize = 1024 * 1024 * memClass / 8;
		mImageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache(cacheSize));
        try {
            // 获取图片缓存路径
            File cacheDir = getDiskCacheDir(context, "thumb");
            if (!cacheDir.exists()) {
                cacheDir.mkdirs();
            }
            // 创建DiskLruCache实例，初始化缓存数据
            mDiskLruCache = DiskLruCache
                    .open(cacheDir, getAppVersion(context), 1, 10 * 1024 * 1024);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public static RequestQueue getRequestQueue() {
		if (mRequestQueue != null) {
			return mRequestQueue;
		} else {
			throw new IllegalStateException("RequestQueue not initialized");
		}
	}
	
	public static void addRequest(Request<?> request, Object tag) {
        if (tag != null) {
            request.setTag(tag);
        }
        mRequestQueue.add(request);
    }
	
	public static void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

	/**
	 * Returns instance of ImageLoader initialized with {@see FakeImageCache}
	 * which effectively means that no memory caching is used. This is useful
	 * for images that you know that will be show only once.
	 * 
	 * @return
	 */
	public static ImageLoader getImageLoader() {
		if (mImageLoader != null) {
			return mImageLoader;
		} else {
			throw new IllegalStateException("ImageLoader not initialized");
		}
	}

    public static DiskLruCache getDiskLruCache(){
        if (mDiskLruCache != null) {
            return mDiskLruCache;
        } else {
            throw new IllegalStateException("DiskLruCache not initialized");
        }
    }

    /**
     * 将缓存记录同步到journal文件中。
     */
    public static void fluchCache() {
        if (mDiskLruCache != null) {
            try {
                mDiskLruCache.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据传入的uniqueName获取硬盘缓存的路径地址。
     */
    private static File getDiskCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * 获取当前应用程序的版本号
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(),0);
            return info.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }
}
