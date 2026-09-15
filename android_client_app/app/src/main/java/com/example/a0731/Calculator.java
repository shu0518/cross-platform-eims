package com.example.a0731;

public class Calculator {
    public static float[] calculate1(float width, float height) {//866 2拉
        float[] results = new float[14];

        // 窗戶橫料
        results[0] = (float) ((width - 15.1) / 2);
        // 紗窗橫料
        results[1] = results[0] + 6;
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (float) (results[3] + 1.2);
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[5]= (width-4)/2;
        //內片大勾 畫線
        results[6]=(results[2]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[7]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate2(float width, float height) {//886，水泥窗-2拉
        float[] results = new float[14];
        // 窗戶橫料
        results[0] = (float) ((width - 15.1) / 2);
        // 紗窗橫料
        results[1] =(float) (results[0] - 7.9);
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (results[3] - 9);
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[5]= (width-4)/2;
        //內片大勾 畫線
        results[6]=(results[2]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[7]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate3(float width, float height) {//1066，2拉
        float[] results = new float[14];
        // 窗戶橫料
        results[0] = (float) ((width - 15.1) / 2);
        // 紗窗橫料
        results[1] =(float) (results[0] + 4.5);
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (float) (results[3] + 1.2);
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[5]=(float) (width-4.0)/2;
        //內片大勾 畫線
        results[6]=results[2]/2;
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[7]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate4(float width, float height) {//1068，2拉
        float[] results = new float[14];
        // 窗戶橫料
        results[0] = (float) ((width - 20.6) / 2);
        // 紗窗橫料
        results[1] =(float) (results[0] + 6.3);
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = results[3] + 1.5f;
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[5]=(float) (width-5)/2;
        //內片大勾 畫線
        results[6]=(results[2]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[7]=results[1]-7.7f;
        }
        return results;
    }
    public static float[] calculate5(float width, float height) {//(特殊)1066-866，流理台反裝-2拉

        float[] results = new float[14];

        // 窗戶橫料
        results[0] = (float) ((width - 15.1) / 2);
        // 內片站支
        results[1] = (float) (height - 9.7);
        // 外片站支
        results[2] = (float) (results[2] + 2.7);
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //下台 畫線
        results[3]=(float) (width-4.0)/2;
        //大勾 畫線
        results[4]=(results[2]/2);
        //這個白癡特殊型號很多新東西0.0
        //花格料寬
        results[5]=results[0]-9.5f;
        //花格料高
        results[6]=(float) (((results[2] - 6.5 - 6.5) /2)-4.5);
        // 花板寬
        results[7] = (float) (results[0] - 0.5);
        // 花板高
        results[8] = results[2]-6.5f-6.5f+2.4f-results[8]-9-0.5f;
        //花格網寬
        results[9]=results[7]+3.3f;
        //花格網高
        results[10]=results[8]+3.3f;
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[11]=results[1]-7.7f;
        }
        //顯示一坨備註
        // print("下花板上花格網,對半
        //防盜檔裝4拉方向,排水器裝2拉方向,把手檔不裝,下面風口不沖,配件正常裝
        //窗片沖孔,2拉方向
        //內片下橫6.5,外片下橫9.2")
        return results;
    }
    public static float[] calculate6(float width, float height) {//(特殊)-1066加1200型鎖，2拉
        float[] results = new float[14];

        // 窗戶橫料
        results[0] = (float) (((width -7.5 - 15.1) / 2)+0.2);
        // 紗窗橫料
        results[1] = (float) (results[0] + 1.2);
        // 內片站支
        results[2] = (float) (height - 9.7);
        // 外片站支
        results[3] = (float) (results[2] + 2.7);
        // 紗窗站支
        results[4] = (float) (results[3] + 1.2);
        for(int i = 0; i < results.length; i++) {
            results[i] = (float) Math.floor(results[i] * 10) / 10;
        }
        //內左量起畫線 (跟上面那個下台畫線很像 但她是特殊形所以不叫下台畫線
        results[5]=(width-7.5f-4)/2;
        //內片大勾 畫線
        results[6]=(results[2]/2);
        //（紗窗站支高150以上）加裝紗窗中腰
        if (results[4]>150){
            results[7]=results[1]-7.7f;
        }
        return results;
    }

}
