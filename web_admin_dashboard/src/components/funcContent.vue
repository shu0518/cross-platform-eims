<template>
    <div class="main-container">
        <!-- 左側 a (側邊欄) -->
        <aside :class="['sidebar', { collapsed: isCollapsed }]">

            <button class="toggle-btn" @click="toggleSidebar" :class="isCollapsed">
                <img src="../assets/ic_menu.png" class="img white-filter" />
            </button>

            <div class="sidebar-content" v-if="!isCollapsed">
                <button class="sidebar-btn" @click="showMeasure" :class="{ active: showFunc === 'measure' }">
                    <i class="fas fa-door-open"></i>門窗數據
                </button>
                <button class="sidebar-btn" @click="showMachine" :class="{ active: showFunc === 'machine' }">
                    <i class="fas fa-cogs"></i>加工數據
                </button>
                <button class="sidebar-btn" @click="showExported" :class="{ active: showFunc === 'exported' }">
                    <i class="fas fa-file-export"></i>歷史單據
                </button>
            </div>
        </aside>
        <!-- 右側 b (內容區域) -->
        <div class="content" v-if="showFunc == 'measure'">
            <measureContent :client_address="client_address" :machineMeasureId="machineMeasureId"
                :clientInfo="clientInfo" />
        </div>
        <div class="content" v-if="showFunc == 'machine'">
            <machineContent :client_address="client_address" :viewMachineIdContent="viewMachineIdContent"
                :clientInfo="clientInfo" @viewMachineMeasure="viewMachineMeasure" />
        </div>
        <div class="content" v-if="showFunc == 'exported'">
            <exportedContent :client_address="client_address" :clientInfo="clientInfo"/>
        </div>
    </div>
</template>

<script>
import { ref, computed } from 'vue';
import measureContent from "./measureFunction.vue";
import machineContent from "./machineFunction.vue";
import exportedContent from "./exportedFunction.vue";

export default {
    components: {
        measureContent,
        machineContent,
        exportedContent
    },
    props: {
        client_address: String,
        viewMachineIdContent: Object,
        clientInfo: Object,
    },
    setup(props, { emit }) {
        const client_address = ref(props.client_address)
        const showFunc = ref('measure')
        const isCollapsed = ref(false);  // 側邊欄的展開/收回狀態
        const machineMeasureId = ref("")

        const toggleSidebar = () => {
            isCollapsed.value = !isCollapsed.value; // 切換側邊欄狀態
        };

        function showMeasure() {
            client_address.value = props.client_address
            viewMachineIdContent.value = null
            showFunc.value = "measure"
        }
        function showMachine() {
            machineMeasureId.value = ""
            showFunc.value = "machine"
        }
        function showExported() {
            machineMeasureId.value = ""
            client_address.value = props.client_address
            viewMachineIdContent.value = null
            showFunc.value = "exported"
        }
        function viewMachineMeasure(measure_id) {
            machineMeasureId.value = measure_id
            showMeasure()
        }
        const viewMachineIdContent = ref(props.viewMachineIdContent)
        if (viewMachineIdContent.value) {
            client_address.value = viewMachineIdContent.value.clientAddress
            showMachine()
        }

        return {
            showFunc,
            client_address,
            isCollapsed,
            toggleSidebar,
            showMeasure,
            showMachine,
            showExported,
            machineMeasureId,
            viewMachineMeasure,
            viewMachineIdContent,
        };
    }
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
    font-family: 'Arial', sans-serif;
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

.main-container {
    display: flex;
    height: 100vh;
    overflow: hidden;
}

.sidebar {
    width: 250px;
    max-height: 600px;
    background: linear-gradient(135deg, #e0e0e0, #f5f5f5);
    padding: 20px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 20px;
    box-shadow: 2px 0 10px rgba(0, 0, 0, 0.1);
    transition: width 0.3s ease;
    border-radius: 10px;
    margin: 20px;
    position: relative;
    /* 確保 toggle 按鈕可相對定位 */
}

.sidebar.collapsed {
    width: 0px;
    /* 收回時的寬度 */
    height: 0px;
    /* 收回時的寬度 */
}

.sidebar-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    width: 100%;
    transition: opacity 0.3s ease;
}

.sidebar.collapsed .sidebar-content {
    opacity: 0;
    /* 收回時隱藏內容 */
    pointer-events: none;
}

.toggle-btn {
    position: absolute;
    top: -5px;
    left: -5px;
    background: linear-gradient(135deg, #42a5f5, #1e88e5);
    border: 1px solid #42a5f5;
    border-radius: 50%;
    width: 45px;
    height: 45px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.toggle-btn .img {
    width: 28px;
    height: 30px;
}

.white-filter {
    filter: brightness(0) invert(1);
}

.sidebar-btn {
    width: 90%;
    /* 按鈕填滿側邊欄寬度 */
    height: 60px;
    /* 增加按鈕高度 */
    background: linear-gradient(135deg, #90caf9, #64b5f6);
    /* 更亮眼的藍色漸層 */
    border: none;
    font-size: 20px;
    font-weight: bold;
    color: white;
    display: flex;
    align-items: center;
    /* 垂直置中 */
    justify-content: center;
    /* 水平置中 */
    cursor: pointer;
    transition: all 0.3s ease;
    border-radius: 20px;
    /* 圓角設計 */
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    /* 微妙陰影效果 */
    gap: 12px;
    /* 調整圖示與文字的間距 */
    padding: 10px;
    margin: 15px 0;
    /* 調整按鈕之間的間距 */
}

.sidebar-btn i {
    font-size: 24px;
    /* 調整圖示大小 */
}

.sidebar-btn:hover {
    background: linear-gradient(135deg, #64b5f6, #42a5f5);
    /* 更深色的藍色漸層 */
    transform: translateY(-3px);
    /* 輕微上移 */
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.15);
    /* 增加陰影 */
}

.sidebar-btn:active {
    transform: scale(0.97);
    /* 按下時縮小 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
    /* 減少陰影 */
}


.sidebar-btn.active {
    background: linear-gradient(135deg, #42a5f5, #1e88e5);
    /* 更亮的藍色漸層 */
    box-shadow: inset 0 3px 6px rgba(0, 0, 0, 0.2), 0 4px 8px rgba(0, 0, 0, 0.1);
    /* 減弱內部陰影並增加外部陰影 */
    font-weight: bold;
    transform: scale(1.03);
    /* 輕微放大 */
    color: #fff;
    transition: all 0.3s ease;
    border: 1px solid #1e88e5;
    /* 輕邊框加強效果 */
}

.content {
    flex: 1;
    padding: 10px;
    overflow-y: auto;
    background-color: #fff;
    transition: all 0.3s ease;
}

.top-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-btn {
    background-color: #4caf50;
    color: white;
    border: none;
    padding: 10px 20px;
    font-size: 18px;
    cursor: pointer;
    border-radius: 5px;
    transition: background-color 0.3s;
}

.add-btn:hover {
    background-color: #45a049;
}

.search-input {
    padding: 10px;
    width: 300px;
    font-size: 16px;
    border-radius: 5px;
    border: 1px solid #ccc;
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
    background-color: #fafafa;
}

.data-table th,
.data-table td {
    border: 1px solid #ddd;
    padding: 15px;
    text-align: left;
    font-size: 16px;
}

.data-table th {
    background-color: #f2f2f2;
    font-weight: bold;
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
    background-color: #ff5722;
    color: white;
    border: none;
    padding: 12px 20px;
    font-size: 18px;
    cursor: pointer;
    border-radius: 5px;
    transition: background-color 0.3s;
}

.export-btn:hover {
    background-color: #e64a19;
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
    width: 450px;
    /* 移除 box-shadow 和任何 border */
    box-shadow: none;
    /* 或直接刪除這一行 */
    border: none;
    /* 如果有 border 設定，刪除或設置為 none */
    position: relative;
    text-align: center;
}

/* 標題樣式 */
.modal-content h2 {
    background-color: #b0bec5;
    color: white;
    padding: 10px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    margin-bottom: 20px;
    text-align: center;
    font-size: 18px;
    position: relative;
}

/* 表單欄位樣式 */
.form-group {
    display: flex;
    align-items: center;
    background-color: #f0f0f0;
    border-radius: 5px;
    margin-bottom: 15px;
    padding: 10px;
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

/* 按鈕組樣式 */
.button-group {
    display: flex;
    justify-content: center;
    gap: 20px;
    /* 增加按鈕之間的距離 */
    margin-top: 20px;
}

.btn {
    background-color: #b49292;
    color: white;
    padding: 10px 30px;
    border: none;
    border-radius: 25px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
}

.btn:hover {
    background-color: #9d7878;
}

.close-btn {
    font-size: 30px;
    position: absolute;
    top: 21px;
    right: 30px;
    cursor: pointer;
    color: white;
}

.close-btn:hover {
    color: #ff6b6b;
    /* 當滑鼠懸停時，顏色變成紅色 */
}
</style>