<template>
    <div class="container">
        <main class="content">
            <div class="top-bar">
                <div class="info-card">
                    <i class="fas fa-user info-icon"></i>
                    <h3>客戶：{{ clientInfo.client_name }}　地址：{{ clientInfo.client_address }}</h3>
                </div>
                <div class="search-container">
                    <img src="../assets/ic_search1.png" class="search-icon" alt="搜尋圖示" />
                    <input type="text" class="search-bar" placeholder="搜尋" v-model="searchQuery" />
                    <a @click="clearSearch">×</a>
                </div>
            </div>
            <!-- 單據 List渲染 -->
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>日期</th>
                            <th>單據種類</th>
                            <th>下載</th>
                        </tr>
                    </thead>
                    <tbody>
                        <template v-if="filteredExported && filteredExported.length > 0">
                            <exportedList v-for="exported in filteredExported" :key="exported.measure_id"
                                :exported="exported" :client_address="client_address" />
                        </template>
                        <tr v-else>
                            <td colspan="3" style="text-align: center; color: gray">無符合條件的結果</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <!-- 數據資訊 -->
            <div v-if="showMeasureId" class="modal-overlay">
                <div class="modal-content" @click.stop>
                    <h2>
                        查看數據
                        <span class="close-btn" @click="closeMeasureId">×</span>
                    </h2>
                    <div class="form-group">
                        <img src="../assets/ic_date.png" class="icon" />
                        <span class="icon-text">日期</span>
                        <a v-if="!isEdit">{{ formatDate(currentMeasure.measure_date) }}</a>
                        <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                            v-model="currentMeasure.measure_date" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_house.png" class="icon" />
                        <span class="icon-text">位置</span>
                        <a v-if="!isEdit">{{ currentMeasure.position }}</a>
                        <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                            v-model="currentMeasure.position" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_windows.png" class="icon" />
                        <span class="icon-text">類型</span>
                        <a v-if="!isEdit">{{ currentMeasure.specification.windoor }}</a>
                        <select v-else v-model="currentMeasure.specification.windoor" id="contract"
                            class="styled-select-short">
                            <option value="" disabled selected>請選擇門框或窗框</option>
                            <!-- 模擬 placeholder -->
                            <option>門</option>
                            <option>窗</option>
                        </select>
                        <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_width.png" class="icon" />
                        <span class="icon-text">寬度</span>
                        <a v-if="!isEdit">{{ currentMeasure.width }}</a>
                        <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                            v-model="currentMeasure.width" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_height.png" class="icon" />
                        <span class="icon-text">高度</span>
                        <a v-if="!isEdit">{{ currentMeasure.length }}</a>
                        <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                            v-model="currentMeasure.length" />
                    </div>
                    <div class="form-group">
                        <i class="fas fa-list-ol"></i>
                        <span class="icon-text">數量</span>
                        <a v-if="!isEdit">{{ currentMeasure.quantity }}</a>
                        <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                            v-model="currentMeasure.quantity" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_color.png" class="icon" />
                        <span class="icon-text">顏色</span>
                        <a v-if="!isEdit">{{ currentMeasure.specification.color }}</a>
                        <select v-else v-model="currentMeasure.specification.color" id="color-select"
                            class="styled-select-short">
                            <option value="" disabled selected>請選擇顏色</option>
                            <!-- 模擬 placeholder -->
                            <option v-for="(color, index) in colorList" :key="index" :value="color">
                                {{ color }}
                            </option>
                        </select>
                        <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_wall.png" class="icon" />
                        <span class="checkbox-icon-text">牆體</span>
                        <a v-if="!isEdit">{{ currentMeasure.specification.wall.join(", ") }}</a>
                        <div v-else class="checkbox-group">
                            <label v-for="(wall, index) in wallList" :key="index">
                                <input type="checkbox" :value="wall" v-model="currentMeasure.specification.wall" />
                                {{ wall }}
                            </label>
                        </div>
                    </div>
                    <!-- 窗 -->
                    <div v-if="currentMeasure.specification.windoor == '窗'">
                        <!-- 要求 -->
                        <div class="form-group">
                            <img src="../assets/ic_comments.png" class="icon" />
                            <span class="checkbox-icon-text">客戶要求</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.request.join(", ") }}</a>
                            <div v-else class="checkbox-group">
                                <label v-for="(request, index) in windowRequestList" :key="index">
                                    <input type="checkbox" :value="request"
                                        v-model="currentMeasure.specification.request" />
                                    {{ request }}
                                </label>
                                <label>
                                    <input type="checkbox" v-model="requestOtherChecked" />
                                    <!-- 新增的CheckBox -->
                                    <input :class="{ 'no-border': !requestOtherChecked, 'active': requestOtherChecked }"
                                        :disabled="!requestOtherChecked"
                                        v-model="currentMeasure.specification.requestOther" placeholder="請輸入其他要求" />
                                </label>
                            </div>
                        </div>
                        <!-- 234 -->
                        <div class="form-group">
                            <img src="../assets/ic_open_window.png" class="icon" />
                            <span class="icon-text">窗框款式</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.type.piece }}</a>
                            <select v-else v-model="currentMeasure.specification.type.piece" id="contract"
                                class="styled-select">
                                <option value="" disabled selected>請選擇款式</option>
                                <!-- 模擬 placeholder -->
                                <option v-for="(type, index) in windowTypeList" :key="index" :value="type">
                                    {{ type }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 866 1066 1068 -->
                        <div class="form-group">
                            <img src="../assets/ic_information.png" class="icon" />
                            <span class="icon-text">窗框細項</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.type.option }}</a>
                            <select v-else v-model="currentMeasure.specification.type.option" class="styled-select">
                                <option value="" disabled selected>請選擇細項</option>
                                <!-- 模擬 placeholder -->
                                <option
                                    v-for="(option, index) in windowOptionList[currentMeasure.specification.type.piece]"
                                    :key="index" :value="option">
                                    {{ option }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 8mm 5mm -->
                        <div class="form-group">
                            <img src="../assets/ic_thickness.png" class="icon" />
                            <span class="icon-text">玻璃厚度</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.type.thick }}</a>
                            <select v-else v-model="currentMeasure.specification.type.thick" class="styled-select">
                                <option value="" disabled selected>請選擇玻璃厚度</option>
                                <!-- 模擬 placeholder -->
                                <option value="8mm玻璃">8mm玻璃</option>
                                <option value="5mm玻璃">5mm玻璃</option>
                                <option value="複合玻璃">複合玻璃</option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 灰玻 茶玻 -->
                        <div class="form-group">
                            <img src="../assets/ic_glass.png" class="icon" />
                            <span class="icon-text">玻璃款式</span>
                            <a v-if="!isEdit">{{
                                currentMeasure.specification.glass[currentMeasure.specification.type.thick]
                                }}</a>
                            <select v-else
                                v-model="currentMeasure.specification.glass[currentMeasure.specification.type.thick]"
                                class="styled-select">
                                <option value="" disabled selected>請選擇玻璃款式</option>
                                <!-- 模擬 placeholder -->
                                <option v-for="(type, index) in glassTypeList[currentMeasure.specification.type.thick]"
                                    :key="index" :value="type">
                                    {{ type }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                    </div>
                    <!-- 門 -->
                    <div v-else>
                        <!-- 要求 -->
                        <div class="form-group">
                            <img src="../assets/ic_comments.png" class="icon" />
                            <span class="checkbox-icon-text">客戶要求</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.request.join(", ") }}</a>
                            <div v-else class="checkbox-group">
                                <label v-for="(request, index) in doorRequestList" :key="index">
                                    <input type="checkbox" :value="request"
                                        v-model="currentMeasure.specification.request" />
                                    {{ request }}
                                </label>
                                <label>
                                    <input type="checkbox" v-model="requestOtherChecked" />
                                    <!-- 新增的CheckBox -->
                                    <input :class="{ 'no-border': !requestOtherChecked, 'active': requestOtherChecked }"
                                        :disabled="!requestOtherChecked"
                                        v-model="currentMeasure.specification.requestOther" placeholder="請輸入其他要求" />
                                </label>
                            </div>
                        </div>
                        <!-- 配件 -->
                        <div class="form-group">
                            <i class="fas fa-lock"></i>
                            <span class="checkbox-icon-text">配件</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.asset.join(", ") }}</a>
                            <div v-else class="checkbox-group">
                                <label v-for="(request, index) in doorAssetsList" :key="index">
                                    <input type="checkbox" :value="request"
                                        v-model="currentMeasure.specification.asset" />
                                    {{ request }}
                                </label>
                            </div>
                        </div>
                        <!-- 現場補料 -->
                        <div class="form-group">
                            <img src="../assets/ic_door_frame.png" class="icon" />
                            <span class="icon-text">現場補料</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.onSite }}</a>
                            <select v-else v-model="currentMeasure.specification.onSite" id="contract"
                                class="styled-select">
                                <option value="" disabled selected>請選擇款式</option>
                                <!-- 模擬 placeholder -->
                                <option v-for="(onSite, index) in doorOnSiteList" :key="index" :value="onSite">
                                    {{ onSite }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 門框 -->
                        <div class="form-group">
                            <img src="../assets/ic_door_frame.png" class="icon" />
                            <span class="icon-text">門框樣式</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.type.frame }}</a>
                            <select v-else v-model="currentMeasure.specification.type.frame" id="contract"
                                class="styled-select">
                                <option value="" disabled selected>請選擇款式</option>
                                <!-- 模擬 placeholder -->
                                <option v-for="(frame, index) in doorFramesList" :key="index" :value="frame">
                                    {{ frame }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 門片 -->
                        <div class="form-group">
                            <img src="../assets/ic_size.png" class="icon" />
                            <span class="icon-text">門片樣式</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.type.piece }}</a>
                            <select v-else v-model="currentMeasure.specification.type.piece" id="contract"
                                class="styled-select">
                                <option value="" disabled selected>請選擇款式</option>
                                <!-- 模擬 placeholder -->
                                <option
                                    v-for="(piece, index) in doorPiecesList[currentMeasure.specification.type.frame]"
                                    :key="index" :value="piece">
                                    {{ piece }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 模型 -->
                        <div class="form-group">
                            <img src="../assets/ic_door.png" class="icon" />
                            <span class="icon-text">門片模式</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.type.model }}</a>
                            <select v-else v-model="currentMeasure.specification.type.model" id="contract"
                                class="styled-select">
                                <option value="" disabled selected>請選擇款式</option>
                                <!-- 模擬 placeholder -->
                                <option v-for="(model, index) in doorModelsList" :key="index" :value="model">
                                    {{ model }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                        </div>
                        <!-- 模型。上 -->
                        <div v-if="currentMeasure.specification.type.model == '上下'">
                            <div class="form-group">
                                <img src="../assets/ic_information.png" class="icon" />
                                <span class="icon-text">上　　</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.option.up }}</a>
                                <select v-else v-model="currentMeasure.specification.type.option.up" id="contract"
                                    class="styled-select-short">
                                    <option value="" disabled selected>請選擇款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(model, index) in doorUpTypeList" :key="index" :value="model">
                                        {{ model }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                            </div>
                            <div class="form-group" v-if="
                                currentMeasure.specification.type.option.up == '8mm玻璃' ||
                                currentMeasure.specification.type.option.up == '5mm玻璃'
                            ">
                                <img src="../assets/ic_glass.png" class="icon" />
                                <span class="icon-text">上玻璃</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.glass["up"] }}</a>
                                <select v-else v-model="currentMeasure.specification.glass['up']"
                                    class="styled-select-short">
                                    <option value="" disabled selected>請選擇上玻璃款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(type, index) in glassTypeList[
                                        currentMeasure.specification.type.option.up
                                    ]" :key="index" :value="type">
                                        {{ type }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                            </div>
                            <!-- 模型。下 -->
                            <div class="form-group">
                                <img src="../assets/ic_information.png" class="icon" />
                                <span class="icon-text">下　　</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.option.down }}</a>
                                <div v-else>
                                    <span v-if="
                                        currentMeasure.specification.type.option.up == '混色花板' ||
                                        currentMeasure.specification.type.option.up == '全門花' ||
                                        currentMeasure.specification.type.option.up == '全百葉片(700型葉片)'
                                    ">{{ currentMeasure.specification.type.option.up }}</span>
                                    <select v-else v-model="currentMeasure.specification.type.option.down" id="contract"
                                        class="styled-select-short">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(model, index) in doorDownTypeList" :key="index" :value="model">
                                            {{ model }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow-short" v-if="
                                        isEdit &&
                                        !['混色花板', '全門花', '全百葉片(700型葉片)'].includes(
                                            currentMeasure.specification.type.option.up
                                        )
                                    ">
                                    </i>
                                </div>
                            </div>
                            <div class="form-group" v-if="
                                !['混色花板', '全門花', '全百葉片(700型葉片)'].includes(
                                    currentMeasure.specification.type.option.up
                                ) &&
                                (currentMeasure.specification.type.option.down == '8mm玻璃' ||
                                    currentMeasure.specification.type.option.down == '5mm玻璃')
                            ">
                                <img src="../assets/ic_glass.png" class="icon" />
                                <span class="icon-text">下玻璃</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.glass["down"] }}</a>
                                <select v-else v-model="currentMeasure.specification.glass['down']"
                                    class="styled-select-short">
                                    <option value="" disabled selected>請選擇下玻璃款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(type, index) in glassTypeList[
                                        currentMeasure.specification.type.option.down
                                    ]" :key="index" :value="type">
                                        {{ type }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow-short" v-if="
                                    isEdit &&
                                    !['混色花板', '全門花', '全百葉片(700型葉片)'].includes(
                                        currentMeasure.specification.type.option.up
                                    )
                                ">
                                </i>
                            </div>
                        </div>
                        <!-- 模型。上中下 -->
                        <div v-else>
                            <div class="form-group">
                                <img src="../assets/ic_information.png" class="icon" />
                                <span class="icon-text">上</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.option.up }}</a>
                                <a v-else>花格網</a>
                            </div>
                            <div class="form-group">
                                <img src="../assets/ic_information.png" class="icon" />
                                <span class="icon-text">中</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.option.middle }}</a>
                                <a v-else>花格網</a>
                            </div>
                            <div class="form-group">
                                <img src="../assets/ic_information.png" class="icon" />
                                <span class="icon-text">下</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.option.down }}</a>
                                <a v-else>花板</a>
                            </div>
                        </div>
                    </div>

                    <div class="button-group">
                        <button class="btn_clear" v-if="!isEdit" @click="deleteMeasure">刪除</button>
                        <button class="btn_clear" v-if="isEdit" @click="cancelEdit">取消</button>

                        <!-- 點擊時調用 deleteMeasure -->
                        <button class="btn edit" v-if="!isEdit" @click="startEditing">編輯</button>
                        <button class="btn fin" v-if="isEdit" @click="putMeasure">確認</button>
                    </div>
                </div>
            </div>
        </main>
    </div>
</template>

<script>
import { format } from "date-fns";
import { ref, computed } from "vue";
import exportedList from "./List/exportedList.vue";
import { postExported } from "../services/api";

export default {
    components: {
        exportedList,
    },
    props: {
        client_address: String,
        clientInfo: Object
    },
    setup(props, { emit }) {
        // 取得客戶頁面設定
        const client_address = ref("");
        client_address.value = props.client_address;
        const exportedData = ref([]);
        postExported(client_address.value).then((resp) => {
            exportedData.value = resp.exported;
        }).catch(err => {
            console.error(err.response.data.error)
            alert(err.response.data.error)
        });

        const searchQuery = ref("");
        function clearSearch() {
            searchQuery.value = null;
        }
        // 根據搜尋欄輸入值進行篩選的計算屬性
        const filteredExported = computed(() => {
            if (!searchQuery.value) {
                return exportedData.value; // 如果搜尋欄是空的，返回全部資料
            }
            const query = searchQuery.value.trim().toLowerCase();

            return exportedData.value.filter((exported) => {
                // 定義一個函數來安全處理欄位的型態轉換與比對
                const matchesQuery = (field) => {
                    if (field === null || field === undefined) {
                        return false;
                    }
                    if (typeof field === "string") {
                        return field.toLowerCase().includes(query);
                    }
                    return false;
                };

                // 日期比對邏輯，支持部分日期和年份搜尋
                const isDateMatch = (() => {
                    // 嘗試將輸入的 query 拆分，處理部分日期格式 (如 11/9) 或年份
                    const [month, day] = query.split("/").map((part) => parseInt(part, 10));
                    const year = parseInt(query, 10);

                    // 將 exported.date 轉換成日期物件
                    const exportedDate = new Date(exported.date);
                    if (isNaN(exportedDate)) return false; // 如果 exported.date 不是有效日期，返回 false

                    // 檢查是否輸入了有效的月份和日期
                    const isMonthDayMatch = !isNaN(month) && !isNaN(day) &&
                        exportedDate.getMonth() + 1 === month && // getMonth() 返回 0-11，所以加 1
                        exportedDate.getDate() === day;

                    // 檢查是否輸入了有效的年份
                    const isYearMatch = !isNaN(year) && exportedDate.getFullYear() === year;

                    // 返回部分日期匹配或年份匹配
                    return isMonthDayMatch || isYearMatch;
                })();

                // 比對 date 和 data_from 欄位
                return isDateMatch || matchesQuery(exported.data_from);
            });
        });

        function formatDate(dateString) {
            return format(new Date(dateString), "yyyy-MM-dd");
        }


        return {
            client_address,
            exportedData,
            filteredExported,
            searchQuery,
            clearSearch,
            formatDate
        };
    },
};
</script>

<style scoped>
* {
    margin: 0;
    padding: 0;
    box-sizing: border-box;
}

html,
body {
    height: 100%;
    font-family: "Arial", sans-serif;
    background-color: #f5f5f5;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #b0c4de;
    padding: 15px 20px;
}

.header h1 {
    color: white;
    font-size: 24px;
}

.nav a {
    margin-right: 20px;
    text-decoration: none;
    color: black;
    font-size: 18px;
}

.nav .profile-icon {
    width: 35px;
    height: 35px;
    background-color: #e0e0e0;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 22px;
}

.container {
    display: flex;
    flex-direction: row;
    height: calc(100vh - 70px);
    /* 確保 main 填滿頁面剩餘高度 */
    overflow: hidden;
    /* 避免溢出 */
}

.sidebar {
    width: 250px;
    background-color: #e0e0e0;
    padding: 20px;
    display: flex;
    flex-direction: column;
}

.sidebar-btn {
    background: none;
    border: none;
    text-align: left;
    font-size: 20px;
    padding: 15px;
    cursor: pointer;
    margin-bottom: 10px;
}

.sidebar-btn.active {
    font-weight: bold;
    background-color: #d6d6d6;
    border-radius: 5px;
}

.content {
    flex: 1;
    overflow-y: auto;
    background-color: #fff;
}

.top-bar {
    padding-top: 10px;
    padding-left: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-btn {
    background: linear-gradient(135deg, #66bb6a, #43a047);
    /* 漸層背景 */
    color: white;
    border: none;
    padding: 8px 18px;
    /* 縮小內邊距 */
    font-size: 20px;
    /* 調整字體大小 */
    font-weight: bold;
    /* 文字加粗 */
    cursor: pointer;
    border-radius: 25px;
    /* 圓角按鈕 */
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 陰影增加立體感 */
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    /* 圖示與文字之間的間距 */
    width: 180px;
    /* 調整按鈕寬度 */
    height: 50px;
    /* 減少高度提升美觀 */
}

.custom-icon {
    color: #ffffff;
    font-size: 20px;
    /* 調整圖示大小 */
    transition: transform 0.3s ease;
}

.add-btn:hover {
    background: linear-gradient(135deg, #81c784, #388e3c);
    /* 改變背景色 */
    box-shadow: 0 8px 10px rgba(0, 0, 0, 0.2);
    transform: scale(1.05);
    /* 輕微放大 */
}

/* 懸停時圖示旋轉 */
.add-btn:hover .custom-icon {
    transform: rotate(90deg);
    /* 懸停時旋轉圖示 */
}

/* 按下按鈕的效果 */
.add-btn:active {
    transform: scale(0.95);
    /* 按下時縮小 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    /* 陰影變小 */
}

/* 提示訊息CSS */
.info-card {
    display: flex;
    align-items: center;
    background-color: #e3f2fd;
    padding: 10px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin: 10px 0;
    margin-left: 205px;
    cursor: default;
}

/* 圖示樣式 */
.info-icon {
    color: #42a5f5;
    font-size: 20px;
    margin-right: 10px;
}

/* 提醒文字樣式 */
.info-card  h3{
    margin: 0;
    color: #333;
}

.search-container {
    display: flex;
    align-items: center;
    margin-left: auto;
    margin-right: 10px;
    width: 400px;
    height: 40px;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 5px;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    position: relative;
}

/* 當輸入框聚焦時整個容器的效果 */
.search-container:focus-within {
    border-color: #42a5f5;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    /* 增強陰影效果 */
    transform: translateY(-2px);
    /* 浮起效果 */
}

/* 搜尋圖示的樣式 */
.search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    /* 垂直置中 */
    width: 20px;
    height: 20px;
    pointer-events: none;
    /* 防止圖示阻擋輸入 */
    transition: all 0.3s ease;
    /* 圖示平滑效果 */
    z-index: 1;
    /* 提升圖示層級 */
    color: #888;
    /* 預設圖示顏色 */
}

/* 當輸入框聚焦時，讓圖示也改變顏色 */
.search-container:focus-within .search-icon {
    color: #42a5f5;
    /* 圖示顏色變化 */
}

/* 搜尋欄的樣式 */
.search-bar {
    flex-grow: 1;
    padding: 10px 10px 10px 40px;
    /* 左側預留空間給圖示 */
    border-radius: 5px;
    border: 1px solid transparent;
    /* 預設為透明，避免邊框重疊 */
    font-size: 16px;
    background-color: transparent;
    outline: none;
    /* 移除預設聚焦框 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    box-sizing: border-box;
}

/* 當輸入框聚焦時的效果 */
.search-bar:focus {
    border-color: transparent;
    /* 避免與外部容器邊框重疊 */
}

.search-container a{
    cursor: pointer;
}

/* 表格容器樣式，讓表格區域有固定高度並啟用滾動條 */
.table-container {
    max-height: 450px;
    /* 固定表格容器的最大高度 */
    overflow-y: auto;
    /* 啟用垂直滾動條 */
    margin-top: 10px;
    margin-bottom: 15px;
    border: 1px solid #ddd;
    /* 表格邊框 */
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
    background-color: #fafafa;
    text-align: center;
    /* 水平置中 */
    cursor: default;
}

.data-table th,
.data-table td {
    padding: 15px;
    text-align: left;
    font-size: 18px;
    text-align: center;
    /* 水平置中 */
    vertical-align: middle;
    /* 垂直置中 */
}

.data-table th {
    background-color: #e7f2fc;
    position: sticky;
    top: 0;
    z-index: 1;
    /* 確保表頭顯示在最前 */
    font-weight: bold;
}

/* 表格列的懸停效果 */
.data-table tr:hover {
    background-color: #f1f1f1;
}

.more-btn {
    background-color: #2196f3;
    color: white;
    border: none;
    padding: 8px 12px;
    cursor: pointer;
    border-radius: 5px;
    transition: background-color 0.3s;
}

.more-btn:hover {
    background-color: #1e88e5;
}

.export-btn {
    margin-left: 10px;
    background-color: #ff7043;
    /* 改為更亮眼的橘色 */
    color: white;
    border: none;
    padding: 8px 18px;
    /* 縮小內邊距 */
    font-size: 20px;
    /* 調整字體大小 */
    font-weight: bold;
    /* 文字加粗 */
    cursor: pointer;
    border-radius: 25px;
    /* 圓角按鈕 */
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 陰影增加立體感 */
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    /* 圖示與文字之間的間距 */
    width: 180px;
    /* 調整按鈕寬度 */
    height: 50px;
    /* 減少高度提升美觀 */
}

.export-btn i {
    font-size: 20px;
    /* 調整圖示大小 */
    transition: transform 0.3s ease;
    color: #ffffff;
    /* 圖示顏色為白色 */
}

.export-btn:hover {
    background-color: #ff5722;
    /* 懸停時顏色變深 */
    box-shadow: 0 8px 10px rgba(0, 0, 0, 0.2);
    transform: scale(1.05);
    /* 輕微放大 */
}

/* 懸停時圖示旋轉 */
.export-btn:hover i {
    transform: rotate(90deg);
    /* 懸停時旋轉圖示 */
}

.export-btn:active {
    transform: scale(0.95);
    /* 按下時縮小 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    /* 陰影變小 */
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.5);
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
}

/* 模態框內容 */
.modal-content {
    background-color: white;
    padding: 20px;
    border-radius: 10px;
    max-height: 700px;
    width: 450px;
    /* 移除 box-shadow 和任何 border */
    box-shadow: none;
    /* 或直接刪除這一行 */
    border: none;
    /* 如果有 border 設定，刪除或設置為 none */
    position: relative;
    text-align: center;
    overflow-y: auto;
    /* 啟用垂直滾動條 */
    scrollbar-width: thin;
    /* Firefox 支援 */
    scrollbar-color: #a0a0a0 transparent;
    /* 滾動條的顏色 */
}

/* 標題樣式 */
.modal-content h2 {
    background-color: #b3cee5;
    color: #282828;
    padding: 10px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    margin-bottom: 20px;
    text-align: center;
    font-size: 18px;
    position: relative;
    position: sticky;
    top: 0;
    /* 固定在模態框的頂部 */
    left: 0;
    right: 0;
    z-index: 10;
    /* 確保它顯示在最前方 */
    outline: 20px solid white;

}

/* 輸入欄位樣式 */
.modal-content input {
    border: 2px solid #42a5f5;
    /* 顯眼的邊框顏色 */
    background-color: #f0f8ff;
    /* 顯眼的背景顏色 */
    box-shadow: 0 0 5px #42a5f5;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    padding: 8px;
    /* 增加內邊距 */
    font-size: 16px;
    /* 增加字體大小 */
    height: 40px;
    /* 增加輸入框的高度 */
}

/* Chrome 和 Edge 的滾動條設計 */
.modal-content::-webkit-scrollbar {
    width: 10px;
    /* 調整滾動條寬度 */
}

.modal-content::-webkit-scrollbar-track {
    background: transparent;
    /* 滾動條軌道背景變透明 */
}

.modal-content::-webkit-scrollbar-thumb {
    background-color: #b0bec5;
    /* 滾動條本體顏色 */
    border-radius: 8px;
    /* 圓角滾動條 */
    border: 2px solid white;
    /* 留白效果，模擬內嵌背景 */
}

.modal-content::-webkit-scrollbar-thumb:hover {
    background-color: #90a4ae;
    /* 滑鼠懸停變色 */
}

/* 表單欄位樣式 */
.form-group {
    display: flex;
    align-items: center;
    background-color: #f2f6fa;
    border-radius: 5px;
    margin-bottom: 15px;
    padding: 10px;
    position: relative;
}

.form-group input {
    border: none;
    background-color: transparent;
    flex-grow: 1;
    font-size: 16px;
    margin-left: 10px;
}

/* 圖示 */
.form-group img {
    width: 24px;
    height: 24px;
}

.form-group .icon {
    width: 20px;
    height: 20px;
    margin-right: 10px;
    vertical-align: middle;
}

.form-group i {
    width: 20px;
    height: 20px;
    margin-top: 5px;
    margin-right: 10px;
    vertical-align: middle;
}

.icon-text {
    font-weight: bold;
    margin-right: 10px;
    font-size: 16px;
}

.textbox {
    padding: 12px;
    /* 增加內邊距 */
    font-size: 16px;
    /* 增加字體大小 */
    height: 40px;
    /* 增加輸入框的高度 */
}

.edit-mode {
    border: 2px solid #42a5f5;
    /* 顯眼的邊框顏色 */
    background-color: #f0f8ff;
    /* 顯眼的背景顏色 */
    box-shadow: 0 0 5px #42a5f5;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
}

.checkbox-icon-text {
    font-weight: bold;
    margin-right: 10px;
    font-size: 16px;
    flex-shrink: 0;
    /* 防止文字縮小 */
    text-align: left;
    /* 向左對齊 */
    width: 35px;
    /* 設定固定寬度 */
}

/* 美化下拉選單 */
.styled-select {
    width: 275px;
    padding: 12px;
    font-size: 16px;
    border: 2px solid #96cbf6;
    /* 顯眼的邊框顏色 */
    box-shadow: 0 0 5px #96cbf6;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    border-radius: 25px;
    /* 圓角 */
    background: linear-gradient(135deg, #f2f6fa, #e6eef4);
    /* 漸層背景 */
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    cursor: pointer;
    padding-right: 40px;
    /* 預留箭頭空間 */
    transition: all 0.3s ease;
    box-sizing: border-box;
    color: #333;
}

/* 移除 IE 預設箭頭 */
.styled-select::-ms-expand {
    display: none;
}

/* Font Awesome 箭頭圖示樣式 */
.select-arrow {
    position: absolute;
    right: 15px;
    top: 46%;
    transform: translateY(-50%);
    pointer-events: none;
    color: #888;
    font-size: 18px;
    transition: all 0.3s ease;
}

/* 下拉選單聚焦效果 */
.styled-select:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

.styled-select:hover {
    background: linear-gradient(135deg, #d8e2ec, #c8d4e0);
    /* 背景變亮 */
}

/* 當選單展開時的動畫效果 */
.styled-select {
    transition: all 0.3s ease;
}

/* 聚焦效果 */
.styled-select:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

/* 聚焦時箭頭變色 */
.styled-select:focus+.select-arrow {
    color: #42a5f5;
}

/* 下拉選單內的選項樣式 */
.styled-select option {
    padding: 10px;
    background-color: #fff;
    font-size: 16px;
}

.styled-select option[value=""] {
    color: #aaa;
}

.styled-select-short {
    width: 275px;
    padding: 12px;
    font-size: 16px;
    border: 2px solid #96cbf6;
    /* 顯眼的邊框顏色 */
    box-shadow: 0 0 5px #96cbf6;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    border-radius: 25px;
    /* 圓角 */
    background: linear-gradient(135deg, #f2f6fa, #e6eef4);
    /* 漸層背景 */
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    cursor: pointer;
    padding-right: 40px;
    /* 預留箭頭空間 */
    transition: all 0.3s ease;
    box-sizing: border-box;
    color: #333;
}

/* 移除 IE 預設箭頭 */
.styled-select-short::-ms-expand {
    display: none;
}

/* Font Awesome 箭頭圖示樣式 */
.select-arrow-short {
    position: absolute;
    right: 12%;
    top: 46%;
    transform: translateY(-50%);
    pointer-events: none;
    color: #888;
    font-size: 18px;
    transition: all 0.3s ease;
}

/* 下拉選單聚焦效果 */
.styled-select-short:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

.styled-select-short:hover {
    background: linear-gradient(135deg, #d8e2ec, #c8d4e0);
    /* 背景變亮 */
}

/* 當選單展開時的動畫效果 */
.styled-select-short {
    transition: all 0.3s ease;
}

/* 聚焦效果 */
.styled-select-short:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

/* 聚焦時箭頭變色 */
.styled-select-short:focus+.select-arrow-short {
    color: #42a5f5;
}

/* 下拉選單內的選項樣式 */
.styled-select-short option {
    padding: 10px;
    background-color: #fff;
    font-size: 16px;
}

.styled-select-short option[value=""] {
    color: #aaa;
}

.checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    /* 每個選項之間增加間距 */
    margin-top: 5px;
    max-width: auto;
}

.checkbox-group label {
    background: linear-gradient(135deg, #d4e4f8, #dfedf8);
    padding: 8px 14px;
    border-radius: 25px;
    /* 圓角設計 */
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    /* 增加輕微陰影 */
}

.checkbox-group label:hover {
    background: linear-gradient(135deg, #d8e2ec, #c8d4e0);
    ;
}

.checkbox-group input[type="checkbox"] {
    -webkit-appearance: none;
    /* 移除瀏覽器的預設外觀 */
    appearance: none;
    margin-right: 8px;
    width: 18px;
    height: 18px;
    border: 2px solid #42a5f5;
    /* 設置邊框顏色 */
    border-radius: 5px;
    /* 圓角方塊 */
    background-color: transparent;
    /* 背景透明 */
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
    /* 輕微陰影 */
}

.checkbox-group input[type="checkbox"]:checked {
    background-color: #42a5f5;
    /* 勾選時的背景顏色 */
    border: 2px solid #42a5f5;
    /* 勾選時的邊框顏色 */
    position: relative;
}

.checkbox-group input[type="checkbox"]:checked::after {
    content: "✓";
    /* 使用 Unicode 字符來顯示勾 */
    color: white;
    /* 勾的顏色設為白色 */
    font-size: 14px;
    font-weight: bold;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

/* 隱藏邊框和陰影 */
.no-border {
    border: none !important;
    /* 強制移除邊框 */
    box-shadow: none !important;
    /* 強制移除陰影 */
    background-color: transparent !important;
    /* 背景設置為透明 */
    outline: none !important;
    /* 移除任何輪廓 */
}

/* 勾選後顯示的邊框和陰影 */
.active {
    border: 2px solid #42a5f5 !important;
    /* 顯示邊框 */
    box-shadow: 0 0 5px #42a5f5 !important;
    /* 顯示陰影 */
}

/* 按鈕組樣式 */
.button-group {
    display: flex;
    justify-content: center;
    gap: 20px;
    /* 增加按鈕之間的距離 */
    margin-top: 20px;
}

.btn {
    background-color: #34a9e4;
    color: white;
    padding: 10px 30px;
    border: none;
    border-radius: 25px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
}

.btn:hover {
    background-color: #1373a3;
}

.btn_clear {
    background-color: #9575CD;
    color: white;
    padding: 10px 30px;
    border: none;
    border-radius: 25px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
}

.btn_clear:hover {
    background-color: #7542cc;
}

.close-btn {
    font-size: 30px;
    position: absolute;
    top: 1px;
    right: 13px;
    cursor: pointer;
    color: #282828;
}

.close-btn:hover {
    color: #ff0000;
    /* 當滑鼠懸停時，顏色變成紅色 */
}

@media (max-width: 600px) {
    .checkbox-group {
        flex-direction: column;
    }

    .checkbox-group label {
        width: 100%;
        justify-content: center;
    }
}
</style>
