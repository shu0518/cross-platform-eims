package com.example.a0731;

public class Calculator4la {
    public static float[] calculate1(float width, float height) {//866 4拉
        float[] results = new float[14];

        // 窗戶橫料
        results[0] = (float) ((width - 29.4 ) / 4);
        // 紗窗橫料
        results[1] = results[0] + 7.5f;
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (float) (results[3] + 1.2);

        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台劃線1
        results[5] =  ((width -4 )/4)-1;
        //下台劃線2
        results[6]= ((width -4 )/2);
        //大勾劃線
        results[7]=results[2]/2;
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[8]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate2(float width, float height) {//1066 4拉
        float[] results = new float[14];

        // 窗戶橫料
        results[0] = (float) ((width - 29.4 ) / 4);
        // 紗窗橫料
        results[1] = results[0] + 5.9f;
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (float) (results[3] + 1.2);

        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台劃線1
        results[5] =  ((width -4 )/4)-1;
        //下台劃線2
        results[6]= (width -4 )/2;
        //大勾劃線
        results[7]=results[2]/2;
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[8]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate3(float width, float height) {//1068 4拉
        float[] results = new float[14];

        // 窗戶橫料
        results[0] = (float) ((width - 20.6 ) / 4);
        // 紗窗橫料
        results[1] = results[0] + 6.3f;
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (float) (results[3] + 1.5);

        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台劃線1
        results[5] =  ((width -5 )/4)-1;
        //下台劃線2
        results[6]= (width -4 )/2;
        //大勾劃線
        results[7]=results[2]/2;
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[8]=results[1]-7.7f;
        }
        return results;
    }

}
