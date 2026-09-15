package com.example.a0731;

import java.util.Objects;
//根據不同的窗框類型先呼叫  calculate1 2 3 4 5  之後在呼叫一次 calculateitem
public class DoorCal {
    public static float[] calculate1(float width, float height, String doorpiece) {//L型兩孔門框 doorpiece 先當作是門片的類型
        float[] results = new float[14];
        // 門框橫料。   門框高等於輸入高度 不用算
        results[0] = width-4.0f;
        //再來做門片類型判斷。
        if(Objects.equals(doorpiece, "700型")){
            //門片橫料
            results[1]=results[0]-7.8f;
        }
        if(Objects.equals(doorpiece, "700型(700加大)")){
            //門片橫料
            results[1]=results[0]-19.8f;
        }
        //門片高。700門片、700門片(700加大) 2種的門片高算法都一樣
        results[2]=height-2-1.5f;
        return results;
    }
    public static float[] calculate2(float width, float height, String doorpiece) {//7公分 doorpiece 先當作是門片的類型
        float[] results = new float[14];
        // 門框橫料。   門框高等於輸入高度 不用算
        results[0] = width-7.0f;
        //再來做門片類型判斷。
        if(Objects.equals(doorpiece, "700型")){
            //門片橫料
            results[1]=results[0]-7.8f;
        }
        if(Objects.equals(doorpiece, "700型(700加大)")){
            //門片橫料
            results[1]=results[0]-19.8f;
        }
        //門片高。700門片、700門片(700加大) 2種的門片高算法都一樣
        results[2]=height-3.5f-1.5f;
        return results;
    }
    public static float[] calculate3(float width, float height, String doorpiece) {//3x10 偏領 doorpiece 先當作是門片的類型
        float[] results = new float[14];
        // 門框橫料。         門框高等於輸入高度 不用算
        results[0] = width-6.0f;
        //再來做門片類型判斷。
        if(Objects.equals(doorpiece, "700型")){
            //門片橫料
            results[1]=results[0]-7.8f;
        }
        if(Objects.equals(doorpiece, "700型(700加大)")){
            //門片橫料
            results[1]=results[0]-19.8f;
        }
        //門片高。700門片、700門片(700加大) 2種的門片高算法都一樣
        results[2]=height-3-1.5f;
        return results;
    }
    public static float[] calculate4(float width, float height) {//3x10 中領
        float[] results = new float[14];
        // 門框橫料。         門框高等於輸入高度 不用算
        results[0] = width-6.0f;
        //門片橫料   他只有一種門片可以選所以直接往下走
        results[1]=results[0]-19;
        //門片高
        results[2]=height-3-1.5f;
        return results;
    }
    public static float[] calculate5(float width, float height) {//4.5x10 中領
        float[] results = new float[14];
        // 門框橫料。         門框高等於輸入高度 不用算
        results[0] = width-9.0f;
        //門片橫料   他只有一種門片可以選所以直接往下走
        results[1]=results[0]-19;
        //門片高
        results[2]=height-4.5f-1.5f;
        return results;
    }
    public static float[] calculateitem(String model, String topitem, String botitem, float[] results) {
        // model判斷他是上中下模式還是上下模式  然後因為上中下模式只有一種算法所以沒特別引入mid item，因為只要你選3item就只會帶那公式
        if (Objects.equals(model, "上下")) {//上下模式中  有的東西是全XX EX:混和花板、全百頁、全門花 也就是上下模式的物件都是這些
            switch (topitem) {
                case "5mm玻璃":
                    // 玻璃寬
                    results[3] = results[1] - 1.4f;
                    // 玻璃高
                    results[4] = results[2] - 90 - 13.5f;
                    break;
                case "8mm玻璃":
                    // 玻璃寬
                    results[3] = results[1] - 0.5f;
                    // 玻璃高
                    results[4] = results[2] - 90 - 12.5f;
                    break;
                case "複合玻璃":
                    // 玻璃寬
                    results[3] = results[1] - 0.5f;
                    // 玻璃高
                    results[4] = results[2] - 90 - 12.5f;
                    break;
                case "花板":
                    // 花板寬
                    results[3] = results[1] - 0.5f;
                    // 花板高
                    results[4] = results[2] - 12.5f;
                    break;
                case "門花":
                    // 門花寬
                    results[3] = results[1] - 3 - 0.2f;
                    // 門花高
                    results[4] = results[2] - 90 - 15 - 0.2f;
                    break;
                case "上百葉":
                    // 百葉寬
                    results[3] = results[1] - 3 - 0.3f;
                    // 百葉高
                    results[4] = results[2] - 90 - 15 - 0.1f;
                    //葉片數量，無條件進位
                    results[5] = (float) Math.ceil(results[4] / 4);
                    break;
                case "混色花板":
                    // 花板寬
                    results[3] = results[1] - 0.5f;
                    // 花板高
                    results[4] = results[2] - 20 + 2.5f;
                    break;
                case "全百葉片":
                    // 百葉片寬
                    results[3] = results[1] - 3 - 0.3f;
                    // 百葉站支高
                    results[4] = results[2];
                    // 葉片數量
                    results[5] = (float) Math.ceil(results[4] / 4);
                    break;
                case "全門花":
                    // 門花寬
                    results[3] = results[1] - 3 - 0.2f;
                    // 門花高
                    results[4] = results[2] - 20 - 0.1f;
                    break;
            }
            switch (botitem){
                case "5mm玻璃":
                    // 玻璃寬
                    results[6] = results[1] - 1.4f;
                    // 玻璃高
                    results[7] = 90 - 13.5f;
                    break;
                case "8mm玻璃":
                    // 玻璃寬
                    results[6] = results[1] - 0.5f;
                    // 玻璃高
                    results[7] =  90 - 12.5f;
                    break;
                case "複合玻璃":
                    // 玻璃寬
                    results[6] = results[1] - 0.5f;
                    // 玻璃高
                    results[7] = results[2] - 90 - 12.5f;
                    break;
                case "花板":
                    // 花板寬
                    results[6] = results[1] - 0.5f;
                    // 花板高
                    results[7] =90 - 12.5f;
                    break;
                //下面不會有門花
                case "下百葉":
                    // 百葉寬
                    results[6] = results[1] - 3 - 0.3f;
                    // 百葉高
                    results[7] = 90 - 15 - 0.1f;
                    //葉片數量，無條件進位
                    results[8] = (float) Math.ceil(results[4] / 4);
                    break;
                case "混色花板":
                case "全百葉片":
                case "全門花":
                    break;
            }
        }
        else if (Objects.equals(model, "上中下")){//上中下這個模式只有花格網這個選項
            //花格網料寬
            results[3]=results[1]-9;
            //花格網寬（上面花格網）
            results[4]=results[3]+3.3f;
            //花格網料高
            results[5]=results[2]-90-15-6;
            //花格網高（上面花格網）
            results[6]=results[5]+3.3f;
            //花格網料高(中間) 由於上中下模式裡 當上是花格網，中也必定是，所以就放一起搞吧
            results[7]=90-35.6f;
            //花格網高（中間花格網）
            results[8]=results[7]+3.3f;
            //花板寬   下面花板。由於上中下模式裡 當上是花格網，中也必定是，下面一定是花板
            results[9]=results[1]-0.5f;
            //花板高  固定12.5
            results[10]=12.5f;
        }
        return results ;
    }

}

