<template>
    <div class="container">
        <!-- 師傅List渲染 -->
        <div class="main-content">
            <div class="action-bar">
                <button class="add-btn" @click="openCreateMember">
                    <i class="fas fa-plus"></i>新增師傅
                </button>
                <div class="info-card">
                    <i class="fas fa-info-circle info-icon"></i>
                    <p class="info-text">　</p>
                </div>
                <div class="search-container">
                    <img src="../assets/ic_search1.png" class="search-icon" alt="搜尋圖示" />
                    <input type="text" class="search-bar" placeholder="搜尋" v-model="searchQuery" />
                    <a @click="clearSearch">×</a>
                </div>
            </div>
            <div class="table-container">
                <table class="contractor-table">
                    <thead>
                        <tr>
                            <th>師傅名稱</th>
                            <th>聯絡電話</th>
                            <th>E-Mail</th>
                            <th>詳細資訊</th>
                        </tr>
                    </thead>
                    <tbody> <!-- 在這裡渲染每個 List 組件中的 tr -->
                        <template v-if="filteredMembers && filteredMembers.length > 0">
                            <memberList v-for="item in filteredMembers" :key="item.account" :item="item"
                                @view-member="openMemberInfo" />
                        </template>
                        <tr v-else>
                            <td colspan="4">無符合條件的結果</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <!-- 新增師傅模態框 -->
        <div v-if="showCreateMember" class="modal-overlay">
            <div class="modal-content" @click.stop>
                <h2>新增師傅</h2>
                <span class="close-btn" @click="closeCreateMember">×</span>
                <form class="form">
                    <div class="form-group">
                        <img src="../assets/ic_worker2.png" class="icon" /> <!-- 姓名圖示 -->
                        <span class="icon-text">師傅姓名</span>
                        <input v-model="newMember_name" placeholder="輸入姓名" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_phone1.png" class="icon" /> <!-- 電話圖示 -->
                        <span class="icon-text">電話號碼</span>
                        <input v-model="newMember_account" placeholder="輸入帳號(電話、不可更改)" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_mail.png" class="icon" /> <!-- 電子信箱圖示 -->
                        <span class="icon-text">電子信箱</span>
                        <input v-model="newMember_mail" placeholder="輸入電子郵件" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_password.png" class="icon" /> <!-- 密碼圖示 -->
                        <span class="icon-text">設定密碼</span>
                        <input v-model="newMember_password" placeholder="輸入密碼" />
                    </div>
                    <div class="form-group">
                        <img src="../assets/ic_password.png" class="icon" /> <!-- 密碼圖示 -->
                        <span class="icon-text">確認密碼</span>
                        <input v-model="newMember_password_again" placeholder="再次確認密碼" />
                    </div>
                    <div class="button-group">
                        <button type="reset" class="btn_clear">清空</button>
                        <button type="button" class="btn add" @click="createMember">新增</button>
                    </div>
                </form>
            </div>
        </div>
        <!-- 師傅資訊模態框 -->
        <div v-if="showMemberInfo" class="modal-overlay">
            <div class="modal-content" @click.stop>
                <h2>{{ currentMember.member_name }}</h2>
                <span class="close-btn" @click="closeMemberInfo">×</span>
                <div class="form-group">
                    <img src="../assets/ic_worker2.png" class="icon" /> <!-- 姓名圖示 -->
                    <span class="icon-text">師傅姓名</span>
                    <a v-if="!isEdit">{{ currentMember.member_name }}</a>
                    <input v-else type="text" class="textbox" v-model="currentMember.member_name" />
                </div>

                <div class="form-group">
                    <img src="../assets/ic_phone1.png" class="icon" /> <!-- 電話圖示 -->
                    <span class="icon-text">電話號碼</span>
                    <a v-if="!isEdit">{{ currentMember.account }}</a>
                    <a v-else class="phone-number">{{ currentMember.account }}(不可更改)</a>
                </div>

                <div class="form-group">
                    <img src="../assets/ic_mail.png" class="icon" /> <!-- 電子信箱圖示 -->
                    <span class="icon-text">電子信箱</span>
                    <a v-if="!isEdit">{{ currentMember.member_mail }}</a>
                    <input v-else type="text" class="textbox" v-model="currentMember.member_mail" />
                </div>

                <div class="form-group">
                    <img src="../assets/ic_password.png" class="icon" /> <!-- 密碼圖示 -->
                    <span class="icon-text">密碼　　</span>
                    <a v-if="!isEdit">{{ '*'.repeat(currentMember.password.length) }}</a>
                    <input v-else type="text" class="textbox" v-model="currentMember.password" />
                </div>
                <div class="form-group" v-if="isEdit">
                    <img src="../assets/ic_password.png" class="icon" /> <!-- 密碼圖示 -->
                    <span class="icon-text">輸入密碼</span>
                    <input placeholder="如須完成更改，請再次輸入密碼" type="text" class="textbox"
                        v-model="currentMemberPasswordAgain" />
                </div>

                <div class="button-group">
                    <button class="btn_clear" v-if="!isEdit" @click="deleteMember">刪除</button>
                    <button class="btn_clear" v-if="isEdit" @click="cancelEdit">取消</button>

                    <!-- 點擊時調用 deleteMember -->
                    <button class="btn edit" v-if="!isEdit" @click="startEditing">編輯</button>
                    <button class="btn fin" v-if="isEdit" @click="updateMember()">確認</button>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref, computed } from 'vue';
import memberList from "./List/memberList.vue";
import { getUser, postUserSignup, putUserInfo, deleteUser } from "../services/api";

export default {
    components: {
        memberList
    },

    setup() {
        // 取得師傅頁面設定
        const memberData = ref([]);
        function getAllMember() {
            getUser().then(resp => {
                memberData.value = resp.member;
            }).catch(err => {
                console.error(err.response.data.error);
            });
        }
        getAllMember()
        const searchQuery = ref('');
        const filteredMembers = computed(() => {
            if (!searchQuery.value) {
                return memberData.value; // 如果搜尋欄是空的，返回全部師傅資料
            }
            const query = searchQuery.value.trim().toLowerCase();
            return memberData.value.filter(item => {
                // 確保 item 中的屬性不是 undefined 或 null，並使用空字串代替，避免錯誤
                const memberName = item.member_name ? item.member_name.toLowerCase() : '';
                const account = item.account ? item.account.toLowerCase() : '';
                const memberMail = item.member_mail ? item.member_mail.toLowerCase() : '';

                // 搜尋師傅名稱、帳號、電子郵件
                return memberName.includes(query) ||
                    account.includes(query) ||
                    memberMail.includes(query);
            });
        });
        function clearSearch() {
            searchQuery.value = null;
        }

        // 新增師傅頁面設定
        const newMember_name = ref('');
        const newMember_account = ref('');
        const newMember_mail = ref('');
        const newMember_password = ref('');
        const newMember_password_again = ref('');
        const showCreateMember = ref(false);
        const openCreateMember = () => {
            newMember_name.value = null;
            newMember_account.value = null;
            newMember_mail.value = null;
            newMember_password.value = null;
            newMember_password_again.value = null;
            showCreateMember.value = true;
        };
        const closeCreateMember = () => {
            showCreateMember.value = false;
        };
        // 新增師傅
        const createMember = () => {
            if (!newMember_password_again.value) {
                alert("請輸入確認密碼")
            } else if (newMember_password.value != newMember_password_again.value) {
                alert("請確認密碼一致")
            } else {
                postUserSignup({
                    account: newMember_account.value,
                    member_name: newMember_name.value,
                    member_mail: newMember_mail.value,
                    password: newMember_password.value,
                }).then((resp) => {
                    getAllMember()
                    showCreateMember.value = false;
                }).catch((err) => {
                    console.error("資料更新失敗:", err.response.data.error);
                });
            }
        };


        // 師傅資訊頁設定
        const isEdit = ref(false);
        const showMemberInfo = ref(false);
        const currentMember = ref({});
        const currentMemberPasswordAgain = ref("");
        const openMemberInfo = (account) => {
            currentMember.value = memberData.value.find(item => item.account === account);
            if (currentMember.value) {
                currentMemberPasswordAgain.value = null;
                showMemberInfo.value = true;
            }
        };
        const closeMemberInfo = () => {
            getAllMember()
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
        // 修改師傅
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
                });
                currentMemberPasswordAgain.value = null
            }
        };
        // 刪除師傅資料
        const deleteMember = () => {
            if (confirm("你確定要刪除這個師傅嗎？")) {
                deleteUser(currentMember.value.account).then((resp) => {
                    showMemberInfo.value = false;
                    getAllMember()
                }).catch((err) => {
                    console.error("資料刪除失敗:", err.response.data.error);
                });
            }
        };

        return {
            searchQuery,
            memberData,
            newMember_name,
            newMember_account,
            newMember_mail,
            newMember_password,
            newMember_password_again,
            showCreateMember,
            showMemberInfo,
            currentMember,
            isEdit,
            filteredMembers,
            currentMemberPasswordAgain,
            openCreateMember,
            closeCreateMember,
            createMember,
            openMemberInfo,
            closeMemberInfo,
            startEditing,
            cancelEdit,
            updateMember,
            deleteMember,
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

/* 提示訊息CSS */
.info-card {
    display: flex;
    align-items: center;
    background-color: white;
    padding: 10px 20px;
    border-radius: 8px;
    /* box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); */
    margin: 10px 0;
}

/* 圖示樣式 */
.info-icon {
    color: white;
    font-size: 20px;
    margin-right: 10px;
}

/* 提醒文字樣式 */
.info-text {
    margin: 0;
    font-size: 16px;
    color: white;
}

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

.table-container {
    max-height: 450px; /* 設定最大高度，讓表格區域有固定高度 */
    overflow-y: auto;  /* 啟用垂直滾動條 */
    margin-top: 10px;
    border: 1px solid #ddd; /* 添加邊框區分表格 */
}

.contractor-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 17px;
}

.contractor-table th,
.contractor-table td {
    padding: 15px;
    text-align: center;
    border-bottom: 1px solid #ddd;
}

.contractor-table th {
    background-color: #e7f2fc;
    position: sticky; /* 讓表頭固定在頂部 */
    top: 0;
    z-index: 1;
}

.contractor-table tr:hover {
    background-color: #f1f1f1;
}

.contractor-table tbody {
    cursor: pointer;
}

/* 模態框背景遮罩--------------------------------------------------------------------------------------------------------- */
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
    border: 2px solid #42a5f5; /* 顯眼的邊框顏色 */
    background-color: #f0f8ff; /* 顯眼的背景顏色 */
    box-shadow: 0 0 5px #42a5f5; /* 增加陰影效果 */
    transition: all 0.3s ease; /* 平滑過渡效果 */
    padding: 12px; /* 增加內邊距 */
    font-size: 16px; /* 增加字體大小 */
    height: 40px; /* 增加輸入框的高度 */
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
    font-weight: bold; /* 加粗字體 */
    color: #2c3e50;    /* 深藍色，提高對比度 */
    padding: 5px 10px; /* 增加內邊距 */
}

.textbox {
    padding: 12px; /* 增加內邊距 */
    font-size: 16px; /* 增加字體大小 */
    height: 40px; /* 增加輸入框的高度 */
}

.edit-mode {
    border: 2px solid #42a5f5; /* 顯眼的邊框顏色 */
    background-color: #f0f8ff; /* 顯眼的背景顏色 */
    box-shadow: 0 0 5px #42a5f5; /* 增加陰影效果 */
    transition: all 0.3s ease; /* 平滑過渡效果 */
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