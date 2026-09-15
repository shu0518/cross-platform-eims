<template>
    <div class="container">
        <!-- 承包商List渲染 -->
        <div class="main-content">
            <div class="action-bar">
                <button class="add-btn" @click="openCreateContract">
                    <i class="fas fa-plus"></i>新增承包商
                </button>
                <div class="info-card">
                    <i class="fas fa-info-circle info-icon"></i>
                    <p class="info-text">點選承包商可查看該承包商的客戶列表</p>
                </div>
                <div class="search-container">
                    <img src="../assets/ic_search1.png" class="search-icon" alt="搜尋圖示" />
                    <input type="text" class="search-bar" placeholder="搜尋" v-model="searchQuery" />
                    <a @click="clearSearch">×</a>
                </div>
            </div>
            <div class="table-container">
                <table class="contract-table">
                    <thead>
                        <tr>
                            <th>承包商名稱</th>
                            <th>聯絡電話</th>
                            <th>地址</th>
                            <th>E-Mail</th>
                            <th>詳細資訊</th>
                        </tr>
                    </thead>
                    <tbody> <!-- 在這裡渲染每個 List 組件中的 tr -->
                        <template v-if="filteredContracts && filteredContracts.length > 0">
                            <contractList v-for="item in filteredContracts" :key="item.contract_id" :item="item"
                                @view-contract="openContractId" @viewContractClient="viewContractClient" />
                        </template>
                        <tr v-else>
                            <td colspan="5" style="text-align: center; color: gray;">無符合條件的結果</td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- 將提醒訊息在底部的程式碼，因用不到先註解 -->
            <!--<div class="fixed-footer">
                <i class="fas fa-info-circle info-icon"></i>
                <p class="info-text">點選承包商可查看該承包商的客戶列表</p>
            </div> -->

        </div>
        <!-- 新增承包商模態框 -->
        <div v-if="showCreateContract" class="modal-overlay">
            <div class="modal-content" @click.stop>
                <h2>新增承包商</h2>
                <span class="close-btn" @click="closeCreateContract">×</span>
                <form class="form">
                    <div class="form-group">
                        <img src="../assets/ic_account1.png" class="icon" /> <!-- 姓名圖示 -->
                        <span class="icon-text">廠商名稱</span>
                        <input v-model="newContract_name" placeholder="必填、不可重複" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_phone1.png" class="icon" /> <!-- 電話圖示 -->
                        <span class="icon-text">電話號碼</span>
                        <input v-model="newContract_phone" placeholder="必填" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_house.png" class="icon" /> <!-- 地址圖示 -->
                        <span class="icon-text">公司地址</span>
                        <input v-model="newContract_address" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_mail.png" class="icon" /> <!-- 電子信箱圖示 -->
                        <span class="icon-text">電子信箱</span>
                        <input v-model="newContract_mail" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_tax_ID_number.png" class="icon" /> <!-- 統一編號圖示 -->
                        <span class="icon-text">統一編號</span>
                        <input v-model="newContract_business_nubmer" />
                    </div>
                    <div class="button-group">
                        <button type="reset" class="btn_clear">清空</button>
                        <button type="button" class="btn add" @click="createContract">新增</button>
                    </div>
                </form>
            </div>
        </div>
        <!-- 承包商資訊模態框 -->
        <div v-if="showContractId" class="modal-overlay">
            <div class="modal-content" @click.stop>
                <h2>{{ currentContract.contract_name }}</h2>
                <span class="close-btn" @click="closeContractId">×</span>
                <div class="form-group">
                    <img src="../assets/ic_account1.png" class="icon" /> <!-- 姓名圖示 -->
                    <span class="icon-text">廠商名稱</span>
                    <a v-if="!isEdit">{{ currentContract.contract_name }}</a>
                    <input v-else type="text" class="textbox" placeholder="必填、不可重複"
                        v-model="currentContract.contract_name" />
                </div>
                <div class="form-group">
                    <img src="../assets/ic_phone1.png" class="icon" /> <!-- 電話圖示 -->
                    <span class="icon-text">電話號碼</span>
                    <a v-if="!isEdit">{{ currentContract.contract_phone }}</a>
                    <input v-else type="text" class="textbox" placeholder="必填"
                        v-model="currentContract.contract_phone" />
                </div>
                <div class="form-group">
                    <img src="../assets/ic_mail.png" class="icon" /> <!-- 電子信箱圖示 -->
                    <span class="icon-text">電子信箱</span>
                    <a v-if="!isEdit">{{ currentContract.contract_mail || "無資料" }}</a>
                    <input v-else type="text" class="textbox" v-model="currentContract.contract_mail" />
                </div>
                <div class="form-group">
                    <img src="../assets/ic_house.png" class="icon" /> <!-- 地址圖示 -->
                    <span class="icon-text">公司地址</span>
                    <a v-if="!isEdit">{{ currentContract.contract_address || "無資料" }}</a>
                    <input v-else type="text" class="textbox" v-model="currentContract.contract_address" />
                </div>
                <div class="form-group">
                    <img src="../assets/ic_tax_ID_number.png" class="icon" /> <!-- 統一編號圖示 -->
                    <span class="icon-text">統一編號</span>
                    <a v-if="!isEdit">{{ currentContract.contract_business_number || "無資料" }}</a>
                    <input v-else type="text" class="textbox" v-model="currentContract.contract_business_number" />
                </div>

                <div class="button-group">
                    <button class="btn_clear" v-if="!isEdit" @click="deleteContract">刪除</button>
                    <button class="btn_clear" v-if="isEdit" @click="cancelEdit">取消</button>

                    <!-- 點擊時調用 deleteContract -->
                    <button class="btn edit" v-if="!isEdit" @click="startEditing">編輯</button>
                    <button class="btn fin" v-if="isEdit" @click="updateContract">確認</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, computed } from 'vue';
import contractList from "./List/contractList.vue";
import { getContract, postContract, putContractContractId, deleteContractContractId } from "../services/api";

export default {
    components: {
        contractList
    },
    setup(props, { emit }) {
        // 取得承包商頁面設定
        const contractData = ref([]);
        function getAllContract() {
            getContract().then(resp => {
                contractData.value = resp.contract;
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        }
        getAllContract()
        const searchQuery = ref('');
        const filteredContracts = computed(() => {
            if (!searchQuery.value) {
                return contractData.value; // 如果沒有輸入搜尋值，顯示所有資料
            }
            const query = searchQuery.value.trim().toLowerCase();
            return contractData.value.filter(item => {
                // 確保 item 中的屬性不是 undefined 或 null，並使用空字串代替，避免錯誤
                const contractName = item.contract_name ? item.contract_name.toLowerCase() : '';
                const contractPhone = item.contract_phone ? item.contract_phone.toLowerCase() : '';
                const contractMail = item.contract_mail ? item.contract_mail.toLowerCase() : '';
                const contractAddress = item.contract_address ? item.contract_address.toLowerCase() : '';

                // 搜尋承包商名稱、電話、電子郵件、地址、公司編號
                return contractName.includes(query) ||
                    contractPhone.includes(query) ||
                    contractMail.includes(query) ||
                    contractAddress.includes(query);
            });
        });
        function clearSearch() {
            searchQuery.value = null;
        }
        const viewContractClient = (contract_name) => {
            emit('viewContractClient', contract_name)
        }

        // 新增承包商頁面設定
        const newContract_name = ref('');
        const newContract_phone = ref('');
        const newContract_address = ref('');
        const newContract_mail = ref('');
        const newContract_business_nubmer = ref('');
        const showCreateContract = ref(false);
        const openCreateContract = () => {
            newContract_name.value = null;
            newContract_phone.value = null;
            newContract_address.value = null;
            newContract_mail.value = null;
            newContract_business_nubmer.value = null;
            showCreateContract.value = true;
        };
        const closeCreateContract = () => {
            showCreateContract.value = false;
        };
        // 新增承包商
        const createContract = () => {
            postContract({
                contract_name: newContract_name.value,
                contract_phone: newContract_phone.value,
                contract_address: newContract_address.value,
                contract_mail: newContract_mail.value,
                contract_business_number: newContract_business_nubmer.value,
            }).then((resp) => {
                getAllContract()
                closeCreateContract();
            }).catch((err) => {
                console.error("資料更新失敗:", err.response.data.error);
                alert(err.response.data.error)
            });
        };

        // 承包商資訊頁設定
        const isEdit = ref(false);
        const showContractId = ref(false);
        const currentContract = ref({});
        const openContractId = (contract_id) => {
            currentContract.value = contractData.value.find(item => item.contract_id === contract_id);
            if (currentContract.value) {
                showContractId.value = true;
            }
        };
        const closeContractId = () => {
            getAllContract()
            isEdit.value = false;
            showContractId.value = false;
        };
        // 定義備份數據，用來恢復編輯前的值
        const original_contract = {};
        const startEditing = () => {
            Object.assign(original_contract, currentContract.value)
            isEdit.value = true;  // 進入編輯模式
        };
        const cancelEdit = () => {
            currentContract.value = { ...original_contract };
            isEdit.value = false;  // 退出編輯模式
        };
        // 修改承包商
        const updateContract = () => {
            const data = {
                contract_id: currentContract.value.contract_id,
                contract_name: currentContract.value.contract_name,
                contract_phone: currentContract.value.contract_phone,
                contract_mail: currentContract.value.contract_mail,
                contract_address: currentContract.value.contract_address,
            };
            putContractContractId(data).then((res) => {
                isEdit.value = false;
            }).catch((err) => {
                console.error("資料更新失敗:", err.response.data.error);
                alert(err.response.data.error)
            });
        };
        // 刪除承包商資料
        const deleteContract = () => {
            if (confirm("你確定要刪除這個承包商嗎？\n\n該承包商的客戶將轉移至：個體戶")) {
                deleteContractContractId(currentContract.value.contract_id).then((resp) => {
                    showContractId.value = false;
                    getAllContract()
                }).catch((err) => {
                    console.error("資料刪除失敗:", err.response.data.error);
                    alert(err.response.data.error)
                });
            }
        };

        return {
            contractData,
            searchQuery,
            newContract_name,
            newContract_phone,
            newContract_mail,
            newContract_address,
            newContract_business_nubmer,
            showCreateContract,
            showContractId,
            currentContract,
            isEdit,
            filteredContracts,
            openCreateContract,
            closeCreateContract,
            createContract,
            openContractId,
            closeContractId,
            startEditing,
            cancelEdit,
            updateContract,
            deleteContract,
            viewContractClient,
            clearSearch,
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

body {
    font-family: Arial, sans-serif;
    background-color: #f4f4f4;
    color: #333;
}

.container {
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    /*padding-bottom: 60px;*/
    /*此為預留空間給頁尾放提示資訊的，但目前用不到*/
}

.header {
    background-color: #b0bec5;
    padding: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.header h1 {
    color: white;
    font-size: 24px;
}

.nav ul {
    list-style: none;
    display: flex;
    gap: 20px;
}

.nav ul li a {
    color: white;
    text-decoration: none;
}

.profile-icon img {
    width: 30px;
    height: 30px;
}

.main-content {
    background-color: white;
    padding: 20px;
    margin-top: 20px;
}

.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-btn {
    background: linear-gradient(135deg, #42a5f5, #1e88e5);
    /* 漸層背景 */
    color: white;
    padding: 12px 24px;
    border: none;
    border-radius: 30px;
    /* 圓角按鈕 */
    cursor: pointer;
    font-size: 18px;
    font-weight: bold;
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    /* 圖示和文字的間距 */
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 陰影效果 */
}

.add-btn:hover {
    background: linear-gradient(135deg, #64b5f6, #1e88e5);
    /* 懸停變化 */
    transform: translateY(-2px);
    /* 懸浮效果 */
    box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
    /* 加強陰影 */
}

.add-btn i {
    font-size: 20px;
    /* 調整圖示大小 */
}

/*fixed-footer和.main-content，這兩個是將提示資訊顯示在頁尾的CSS，但目前沒有用到*/
/* 卡片式提示訊息，固定在畫面底部。*/
.fixed-footer {
    position: fixed;
    bottom: 0;
    left: 0;
    width: 100%;
    background-color: #e3f2fd;
    display: flex;
    align-items: center;
    /* 垂直置中 */
    justify-content: center;
    /* 水平置中 */
    padding: 10px 20px;
    box-shadow: 0 -2px 5px rgba(0, 0, 0, 0.1);
    z-index: 999;
    /* 確保它在最上層顯示 */
}

/* 調整主要內容的樣式，避免被頁尾提示訊息遮擋 */
.main-content {
    margin-bottom: 60px;
    /* 避免與固定頁尾重疊 */
}

.info-card {
    display: flex;
    align-items: center;
    background-color: #e3f2fd;
    padding: 10px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin: 10px 0;
    cursor: default;
}

/* 圖示樣式 */
.info-icon {
    color: #42a5f5;
    font-size: 20px;
    margin-right: 10px;
}

/* 提醒文字樣式 */
.info-text {
    margin: 0;
    font-size: 16px;
    color: #333;
    cursor: default;
}

/* 搜尋功能CSS */
.search-container {
    display: flex;
    align-items: center;
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

.search-container a {
    cursor: pointer; /* 滑鼠懸停時變成手形 */
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

.table-container {
    max-height: 450px;
    /* 設定最大高度，讓表格區域有固定高度 */
    overflow-y: auto;
    /* 啟用垂直滾動條 */
    margin-top: 10px;
    border: 1px solid #ddd;
    /* 添加邊框區分表格 */
}

.contract-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 17px;
}

.contract-table th,
.contract-table td {
    padding: 15px;
    text-align: center;
    border-bottom: 1px solid #ddd;
}

.contract-table th {
    background-color: #e7f2fc;
    position: sticky;
    /* 讓表頭固定在頂部 */
    top: 0;
    z-index: 1;
}

.contract-table td {
    background-color: #e7f2fc;
    position: sticky;
    /* 讓表頭固定在頂部 */
    top: 0;
    z-index: 1;
}

.contract-table tr:hover {
    background-color: #f1f1f1;
}

.contract-table tbody {
    cursor: pointer;
}

/* 模態框背景遮罩 */
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
    background-color: #b3cee5;
    color: #282828;
    padding: 10px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    margin-bottom: 20px;
    text-align: center;
    font-size: 18px;
    position: relative;
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
    padding: 12px;
    /* 增加內邊距 */
    font-size: 16px;
    /* 增加字體大小 */
    height: 40px;
    /* 增加輸入框的高度 */
}

/* 表單欄位樣式 */
.form-group {
    display: flex;
    align-items: center;
    background-color: #f2f6fa;
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

.icon-text {
    font-weight: bold;
    margin-right: 10px;
    font-size: 16px;
}

.form-group i {
    margin-right: 10px;
    color: #888;
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
    top: 21px;
    right: 30px;
    cursor: pointer;
    color: #282828;
}

.close-btn:hover {
    color: #ff0000;
    /* 當滑鼠懸停時，顏色變成紅色 */
}
</style>