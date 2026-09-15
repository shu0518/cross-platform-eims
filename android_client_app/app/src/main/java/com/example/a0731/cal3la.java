package com.example.a0731;

public class cal3la {
    public static float[] calculate1(float width, float height) {//866 3拉
        float[] results = new float[14];

        // 內片窗戶橫料
        results[0] = ((width - 26) / 4);
        // 紗窗橫料
        results[1] = results[0] + 6.5f;
        //外片窗戶橫料
        results[2]=results[0]*2+7.6f;
        // 內片站支
        results[3] = height - 9.7f;
        // 外片站支
        results[4] = results[2] + 2.7f;
        // 紗窗站支
        results[5] = results[3] + 1.2f;
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[6]=(width-4)/2;
        //下台 畫線2  要畫2條線
        results[7]=(width-4)/4;
        //內片大勾 畫線
        results[8]=(results[3]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[5]>150){
            results[9]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate2(float width, float height) {//1066 3拉
        float[] results = new float[14];

        // 內片窗戶橫料
        results[0] = ((width - 26) / 4);
        // 紗窗橫料
        results[1] = results[0] + 4.5f;
        //外片窗戶橫料
        results[2]=results[0]*2+7.6f;
        // 內片站支
        results[3] = height - 9.7f;
        // 外片站支
        results[4] = results[2] + 2.7f;
        // 紗窗站支
        results[5] = results[3] + 1.2f;
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[6]=(width-4)/2;
        //下台 畫線2  要畫2條線
        results[7]=(width-4)/4;
        //內片大勾 畫線
        results[8]=(results[3]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[5]>150){
            results[9]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate3(float width, float height) {//1068 3拉
        float[] results = new float[14];

        // 內片窗戶橫料
        results[0] = ((width -35.6f) / 4);
        // 紗窗橫料
        results[1] = results[0] + 6.3f;
        // 外片窗戶橫料
        results[2]=results[0]*2+11.8f;
        // 內片站支
        results[3] = height - 9.7f;
        // 外片站支
        results[4] = results[2] + 2.7f;
        // 紗窗站支
        results[5] = results[3] + 1.2f;
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[6]=(width-4)/2;
        //下台 畫線2  要畫2條線
        results[7]=(width-4)/4;
        //內片大勾 畫線
        results[8]=(results[3]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[5]>150){
            results[9]=results[1]-7.7f;
        }
        return results;
    }
}
