package com.example.a0731;

import java.util.Objects;
public class windowglass {
    public static float[] glasscal(String windowType, String glassThick, float[] results) {
        float glasswidth=0,glassheight=0;
        if (windowType.equals("866") || windowType.equals("1066") || windowType.equals("866水泥窗") || windowType.equals("1066-866流理台反裝") || windowType.equals("1066加1200型鎖")) {//先判斷是不是 866 或1066 的窗戶，這2種玻璃算法一樣
            if (glassThick.equals("5mm玻璃")) {//一般玻璃算法 5mm
                glasswidth = results[0] - 1.4f;
                glassheight = (float) (results[2] - 6.5 - 6.5 + 1.5);
            } else if (glassThick.equals("8mm玻璃") || glassThick.equals("複合玻璃")) {//強化玻璃算法 8mm 或膠合
                glasswidth = results[0] - 0.5f;
                glassheight = (float) (results[2] - 6.5 - 6.5 + 1.7);
            }
            for(int i = 0; i < results.length; i++) {
                if(results[i]==0){
                    results[i]=glasswidth;
                    results[i+1]=glassheight;
                    break;
                }
            }
        }
        if (windowType.equals("1068")) {
            if (Objects.equals(glassThick, "5mm玻璃")) {//一般玻璃算法 5mm
                glasswidth = results[0] - 1.4f;
                glassheight = (float) (results[2] - 6 - 6.2 + 1.5);
            } else if (glassThick.equals("8mm玻璃") || glassThick.equals("複合玻璃")) {//強化玻璃算法 8mm 或膠合
                glasswidth = results[0] - 0.5f;
                glassheight = (float) (results[2] - 6 - 6.2 + 1.7);
            }
            for(int i = 0; i < results.length; i++) {
                if(results[i]==0){
                    results[i]=glasswidth;
                    results[i+1]=glassheight;
                    break;
                }
            }
        }
        return results;
    }
    public static float[] glasscal3la(String windowType, String glassThick, float[] results) {//3拉的窗戶 1個窗會有玻璃寬數據2組
        float glasswidth=0,glassheight=0,glasswidth2=0;
        if (windowType.equals("866") || windowType.equals("1066")) {//先判斷是不是 866 或1066 的窗戶，這2種玻璃算法一樣
            if (glassThick.equals("5mm玻璃")) {//一般玻璃算法 5mm
                glasswidth = results[0] - 1.4f;
                glasswidth2=results[2]-1.4f;
                glassheight = (float) (results[3] - 6.5 - 6.5 + 1.5);
            } else if (glassThick.equals("8mm玻璃") || glassThick.equals("複合玻璃")) {//強化玻璃算法 8mm 或膠合
                glasswidth = results[0] - 0.5f;
                glasswidth2=results[2]-0.5f;
                glassheight = (float) (results[3] - 6.5 - 6.5 + 1.7);
            }
            for(int i = 0; i < results.length; i++) {
                if(results[i]==0){
                    results[i]=glasswidth;
                    results[i+1]=glasswidth2;
                    results[i+2]=glassheight;
                    break;
                }
            }
        }
        if (windowType.equals("1068")) {
            if (Objects.equals(glassThick, "5mm玻璃")) {//一般玻璃算法 5mm
                glasswidth = results[0] - 1.4f;
                glasswidth2=results[2]-1.4f;
                glassheight = (float) (results[3] - 6 - 6.2 + 1.5);
            } else if (glassThick.equals("8mm玻璃") || glassThick.equals("複合玻璃")) {//強化玻璃算法 8mm 或膠合
                glasswidth = results[0] - 0.5f;
                glasswidth2=results[2]-0.5f;
                glassheight = (float) (results[3] - 6 - 6.2 + 1.7);
            }
            for(int i = 0; i < results.length; i++) {
                if(results[i]==0){
                    results[i]=glasswidth;
                    results[i+1]=glasswidth2;
                    results[i+2]=glassheight;
                    break;
                }
            }
        }
        return results;
    }
}
