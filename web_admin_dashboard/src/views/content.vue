<template>
    <div class="container">
        <header class="header">
            <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
            <h1 class="title">條條框框</h1>
            <nav class="nav">
                <ul>
                    <li>
                        <h3 @click="showContract" :class="{ active: showContent === 'contract' }">承包商列表</h3>
                    </li>
                    <li>
                        <h3 @click="showClient" :class="{ active: showContent === 'client' }">客戶列表</h3>
                    </li>
                    <li>
                        <h3 @click="showMember" :class="{ active: showContent === 'member' }">師傅列表</h3>
                    </li>
                    <li>
                        <h3 @click="showMachine" :class="{ active: showContent === 'machine' }">總加工列表</h3>
                    </li>
                    <li>
                        <img src="../assets/ic_supper_user.png" alt="Profile" @click="openMemberInfo">
                    </li>
                </ul>
            </nav>
        </header>
        <!-- 承包商List -->
        <div class="main-content" v-if="showContent == 'contract'">
            <contractContent @viewContractClient="viewContractClient" />
        </div>
        <!-- 客戶List -->
        <div class="main-content" v-if="showContent == 'client'">
            <clientContent :contract_name="contract_name" @viewClientFunction="viewClientFunction" />
        </div>
        <!-- 師傅List -->
        <div class="main-content" v-if="showContent == 'member'">
            <memberContent />
        </div>
        <!-- 總加工List -->
        <div class="main-content" v-if="showContent == 'machine'">
            <machineContent @viewMachineMachineId="viewMachineMachineId" />
        </div>
        <!--使用者資料-->
        <div v-if="showMemberInfo" class="modal-overlay">
            <div class="modal-content" @click.stop>
                <h2>{{ currentMember.member_name }}</h2>
                <span class="close-btn" @click="closeMemberInfo">×</span>
                <div class="form-group">
                    <img src="../assets/ic_Crown.png" class="icon" />
                    <span class="icon-text">使用者　</span>
                    <a v-if="!isEdit">{{ currentMember.member_name }}</a>
                    <input v-else type="text" class="textbox" placeholder="請輸入姓名" v-model="currentMember.member_name" />
                </div>

                <div class="form-group">
                    <img src="../assets/ic_yellow_user.png" class="icon" />
                    <span class="icon-text">帳號　　</span>
                    <a v-if="!isEdit">{{ currentMember.account }}</a>
                    <a v-else class="phone-number">{{ currentMember.account }}(不可更改)</a>
                </div>

                <div class="form-group">
                    <img src="../assets/ic_yellow_email.png" class="icon" />
                    <span class="icon-text">電子信箱</span>
                    <a v-if="!isEdit">{{ currentMember.member_mail }}</a>
                    <input v-else type="text" class="textbox" placeholder="請輸入信箱" v-model="currentMember.member_mail" />
                </div>

                <div class="form-group">
                    <img src="../assets/ic_key.png" class="icon" />
                    <span class="icon-text">密碼　　</span>
                    <a v-if="!isEdit">{{ "*".repeat(currentMember.password.length) }}</a>
                    <input v-else type="text" class="textbox" placeholder="請輸入密碼" v-model="currentMember.password" />
                </div>
                <div class="form-group" v-if="isEdit">
                    <img src="../assets/ic_yellow_password.png" class="icon" />
                    <span class="icon-text">確認密碼</span>
                    <input type="text" class="textbox" placeholder="如須完成更改，請再次輸入密碼"
                        v-model="currentMemberPasswordAgain" />
                </div>

                <div class="button-group">
                    <button class="btn_clear" v-if="!isEdit" @click="logout">登出</button>
                    <button class="btn_clear" v-if="isEdit" @click="cancelEdit">取消</button>

                    <button class="btn" v-if="!isEdit" @click="startEditing">編輯</button>
                    <button class="btn" v-if="isEdit" @click="updateMember">確認</button>
                </div>
            </div>
        </div>
        <!-- 功能Content -->
        <div class="main-content" v-if="showContent == 'function'">
            <funcContent :client_address="client_address" :viewMachineIdContent="viewMachineIdContent"
                :clientInfo="clientInfo" />
        </div>
    </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { getUserInfo, putUserInfo, getClient, postClientClientAddress } from "../services/api"
import contractContent from "../components/contractContent.vue";
import memberContent from "../components/memberContent.vue";
import clientContent from "../components/clientContent.vue";
import machineContent from "../components/machineContent.vue";
import funcContent from "../components/funcContent.vue";

export default {
    components: {
        contractContent,
        memberContent,
        clientContent,
        machineContent,
        funcContent,
    },
    setup() {
        const router = useRouter();
        const showContent = ref("contract")
        const showContract = () => {
            showContent.value = "contract"
            contract_name.value = null
            viewMachineIdContent.value = ""
        }
        const showClient = () => {
            showContent.value = "client"
            viewMachineIdContent.value = ""
        }
        const showMember = () => {
            showContent.value = "member"
            contract_name.value = null
            viewMachineIdContent.value = ""
        }
        const showMachine = () => {
            showContent.value = "machine"
            contract_name.value = null
            viewMachineIdContent.value = ""
        }
        const showFunction = () => {
            contract_name.value = null
            showContent.value = "function"
        }
        const contract_name = ref("")
        const viewContractClient = (viewClient) => {
            contract_name.value = viewClient
            showClient()
        }
        const client_name = ref("")
        const client_address = ref("")
        const clientInfo = ref()
        const viewClientFunction = (client_id) => {
            getClient().then(resp => {
                const client = resp.client.find(client => client.client_id === client_id);
                client_name.value = client ? client.client_name : ''
                client_address.value = client ? client.client_address : ''
                clientInfo.value = { client_name: client_name.value, client_address: client_address.value }
                showFunction()
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        }
        const viewMachineIdContent = ref()
        function viewMachineMachineId(content) {
            postClientClientAddress(content.clientAddress).then(resp => {
                client_name.value = resp.client.client_name
                client_address.value = resp.client.client_address
                clientInfo.value = { client_name: client_name.value, client_address: client_address.value }
            })
            viewMachineIdContent.value = content
            showFunction()
        }

        // 師傅資訊頁設定
        const memberData = ref({});
        function fetchUserInfo() {
            getUserInfo().then(resp => {
                memberData.value = resp.member;
            }).catch((err) => {
                console.error(err.response.data.error);
                alert(err.response.data.error)
            });
        }
        fetchUserInfo()
        const isEdit = ref(false);
        const showMemberInfo = ref(false);
        const currentMember = ref({});
        const currentMemberPasswordAgain = ref("");
        const openMemberInfo = () => {
            if (memberData.value) {
                currentMember.value = memberData.value;
                showMemberInfo.value = true;
            }
        };
        const closeMemberInfo = () => {
            fetchUserInfo()
            showMemberInfo.value = false;
            isEdit.value = false;
        };
        // 定義備份數據，用來恢復編輯前的值
        const original_member = {};
        const startEditing = () => {
            Object.assign(original_member, currentMember.value);
            isEdit.value = true;  // 進入編輯模式
        };
        const cancelEdit = () => {
            currentMember.value = { ...original_member };
            currentMemberPasswordAgain.value = null
            isEdit.value = false;  // 退出編輯模式
        };
        // 修改主帳號資料
        const updateMember = () => {
            if (!currentMemberPasswordAgain.value) {
                alert("請輸入確認密碼")
            } else if (currentMember.value.password != currentMemberPasswordAgain.value) {
                alert("請確認密碼一致")
            } else {
                const data = {
                    account: currentMember.value.account,
                    member_name: currentMember.value.member_name,
                    member_mail: currentMember.value.member_mail,
                    password: currentMember.value.password,
                };
                putUserInfo(data).then((res) => {
                    isEdit.value = false;
                }).catch((err) => {
                    console.error("資料更新失敗:", err.response.data.error);
                    alert(err.response.data.error)
                });
                currentMemberPasswordAgain.value = null
            }
        };
        // 登出
        const logout = () => {
            if (confirm("你確定要登出嗎？")) {
                localStorage.clear();
                router.push({ name: 'login' });
            }
        };


        return {
            client_address,
            showContent,
            client_name,
            clientInfo,
            showContract,
            showClient,
            showMember,
            showMachine,
            currentMemberPasswordAgain,
            memberData,
            isEdit,
            showMemberInfo,
            currentMember,
            contract_name,
            openMemberInfo,
            closeMemberInfo,
            startEditing,
            cancelEdit,
            updateMember,
            logout,
            viewClientFunction,
            viewContractClient,
            viewMachineMachineId,
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

body {
    font-family: Arial, sans-serif;
    background-color: #f4f4f4;
    color: #333;
    overflow: hidden;
    /* 禁用頁面滾動 */
}

.container {
    width: 100%;
    /* max-width: 1200px; */
    margin: 0 auto;
    min-height: 100vh;
    /* 讓容器最小高度等於瀏覽器高度 */
    display: flex;
    flex-direction: column;
}

.header {
    background-color: #b3cee5;
    padding: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    position: sticky;
    /* 讓 header 固定在滾動時 */
    top: 0;
    /* 固定在頂部 */
    z-index: 500;
    /* 確保 header 在其他內容之上 */
}

.header h1 {
    color: #282828;
    font-size: 28px;
}

.title {
    margin-left: 10px;
    /* 向右微調位置 */
    cursor: default;
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

.nav img {
    width: 33px;
    height: 33px;
    margin-top: 3px;
    /* 向下微調位置 */
    margin-right: 15px;
    /* 向左微調位置 */
    cursor: pointer; /* 滑鼠懸停時變成手形 */
}

.nav ul li h3 {
    cursor: pointer;
    /* 滑鼠變成點擊手指的樣式 */
    transition: color 0.3s, background-color 0.3s;
    /* 平滑過渡效果 */
    padding: 5px 10px;
    border-radius: 5px;
    /* 圓角邊框 */
}

.nav ul li h3:hover {
    color: #1e88e5;
    /* 滑鼠懸停時的文字顏色 */
    background-color: #f0f0f0;
    /* 滑鼠懸停時的背景顏色 */
}

.nav ul li h3.active {
    color: #ffffff;
    /* 選中按鈕的文字顏色 */
    background-color: #1e88e5;
    /* 選中按鈕的背景顏色 */
    font-weight: bold;
    /* border-bottom: 3px solid #1565c0; */
    /* 增加底部邊框提示 */
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
    /* 輕微陰影效果 */
    border: none;
}

.main-content {
    background-color: white;
    padding: 20px;
    flex-grow: 1;
    /* 讓主內容填滿剩餘空間 */
    max-height: calc(100vh - 60px);
    /* 減去 header 和其他元素的高度 */
    overflow: hidden;
    /* 禁用滾動 */
}

.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-btn {
    background-color: #2196f3;
    color: white;
    padding: 10px 20px;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.add-btn:hover {
    background-color: #1e88e5;
}

.search-bar {
    width: 300px;
    padding: 10px;
    border-radius: 5px;
    border: 1px solid #ccc;
}

.contractor-table {
    width: 100%;
    border-collapse: collapse;
}

.contractor-table th,
.contractor-table td {
    padding: 15px;
    text-align: left;
    border-bottom: 1px solid #ddd;
}

.contractor-table th {
    background-color: #eeeeee;
}

.contractor-table tr:hover {
    background-color: #f1f1f1;
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

/* 電話號碼樣式 */
.phone-number {
    font-weight: bold;
    /* 加粗字體 */
    color: #2c3e50;
    /* 深藍色，提高對比度 */
    padding: 5px 10px;
    /* 增加內邊距 */
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