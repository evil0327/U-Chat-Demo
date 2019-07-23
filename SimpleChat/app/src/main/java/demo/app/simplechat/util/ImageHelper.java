package demo.app.simplechat.util;

import demo.app.simplechat.R;

public class ImageHelper {
    private static int[] mAvatarResoures = new int[]{
            R.mipmap.a1, R.mipmap.a2,R.mipmap.a3,R.mipmap.a4,R.mipmap.a5
    };

    public static int getResource(int img){
        return mAvatarResoures[img];
    }

    public static int[] getNextResoure(int current){
        int next = ++current;
        int resoure = mAvatarResoures[next % mAvatarResoures.length];

        return new int[]{next % mAvatarResoures.length, resoure};
    }
}
