<template>
    <div class="login-container">
        <h1 class="title">條條框框</h1>
        <div class="login-box">
            <div class="background-image">
                <!-- 登入 -->
                <div>
                    <form class="login-form">
                        <div class="input-field">
                            <i class="icon-account"></i>
                            <input type="text" v-model="account" placeholder="帳號(電話)" autocomplete="username">
                            <!--autocomplete是自動輸入，可以設off-->
                        </div>
                        <div class="input-field">
                            <i class="icon-password"></i>
                            <input type="password" v-model="password" placeholder="密碼" autocomplete="current-password">
                            <!--autocomplete是自動輸入，可以設off-->
                        </div>
                        <div class="login-options">
                            <a href="#" @click="openForgotPassword">忘記密碼</a>
                            <a href="#" @click="openSignup">註冊帳號</a>
                        </div>
                        <button type="button" class="login-btn" @click="login">登入</button>
                    </form>
                </div>
                <div v-if="showForgotPassword" class="modal-overlay">
                    <div class="modal-content" @click.stop>
                        <h2>忘記密碼</h2>
                        <span class="close-btn" @click="closeForgotPassword">×</span>
                        <form class="form">
                            <!-- 輸入信箱 -->
                            <div class="input-group">
                                <label for="email" class="icon-label">
                                    <img src="../assets/email.png" alt="email-icon" class="icon">
                                </label>
                                <input v-model="member_mail" type="email" id="email" placeholder="e-mail" required>
                                <button type="button" class="send-code-button" @click="verifyEmail">發送驗證碼</button>
                            </div>

                            <!-- 輸入驗證碼 -->
                            <div class="input-group">
                                <label for="verification-code" class="icon-label">
                                    <img src="../assets/Captcha.png" alt="verification-icon" class="icon">
                                </label>
                                <input v-model="token" type="text" id="verification-code" placeholder="輸入驗證碼" required>
                                <button type="button" class="verify-button" @click="verifyToken">驗證</button>
                            </div>
                        </form>
                    </div>
                </div>
                <div v-if="showResetPassword" class="modal-overlay">
                    <div class="modal-content" @click.stop>
                        <h2>忘記密碼</h2>
                        <span class="close-btn" @click="closeResetPassword">×</span>
                        <form>
                            <div class="input-group">
                                <label for="password" class="icon-label">
                                    <img src="../assets/ic_password.png" alt="password-icon" class="icon">
                                </label>
                                <input type="password" id="password" placeholder="密碼" v-model="resetPassword">
                            </div>
                            <div class="input-group">
                                <label for="password" class="icon-label">
                                    <img src="../assets/ic_password.png" alt="password-icon" class="icon">
                                </label>
                                <input type="password" id="confirm-password" placeholder="確認密碼"
                                    v-model="resetPasswrodAgain">
                            </div>
                            <button type="button" class="verify-button" @click.prevent="updatePassword">儲存</button>
                        </form>
                    </div>
                </div>
                <div v-if="showSignup" class="modal-overlay">
                    <div class="modal-content" @click.stop>
                        <h2>註冊帳號</h2>
                        <span class="close-btn" @click="closeSignup">×</span>
                        <form class="register-form">
                            <div class="input-group">
                                <label for="name" class="icon-label">
                                    <img src="../assets/ic_account1.png" alt="user" class="icon">
                                </label>
                                <input type="text" id="name" placeholder="姓名" v-model="newMemberName">
                            </div>
                            <div class="input-group">
                                <label for="phone" class="icon-label">
                                    <img src="../assets/ic_phone1.png" alt="phone" class="icon">
                                </label>
                                <input type="text" id="phone" placeholder="帳號 ( 電話號碼 )" v-model="newAccount">
                            </div>
                            <div class="input-group">
                                <label for="email" class="icon-label">
                                    <img src="../assets/ic_mail.png" alt="email" class="icon">
                                </label>
                                <input type="email" id="email" placeholder="e-mail" v-model="newMemberMail">
                            </div>
                            <div class="input-group">
                                <label for="password" class="icon-label">
                                    <img src="../assets/ic_password.png" alt="password" class="icon">
                                </label>
                                <input type="text" id="password" placeholder="密碼" v-model="newPassword">
                            </div>
                            <div class="input-group">
                                <label for="confirm-password" class="icon-label">
                                    <img src="../assets/ic_password.png" alt="confirm-password" class="icon">
                                </label>
                                <input type="text" id="confirm-password" placeholder="確認密碼"
                                    v-model="newPasswordAgain">
                            </div>
                            <div class="button-group">
                                <button type="reset" class="verify-button">清空</button>
                                <button type="button" class="send-code-button" @click="signup">註冊</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { postUser, sendVerifyEmail, sendVerifyToken, patchPassword, postUserSignup } from "../services/api";
export default {
    setup() {
        localStorage.clear();
        const account = ref("");
        const password = ref("");
        const router = useRouter();

        const login = () => {
            postUser({
                account: account.value,
                password: password.value,
            }).then((resp) => {
                if (resp && resp.success) {
                    postMessage(resp.member)
                    router.push({ name: 'content' });
                } else {
                    alert("登入失敗：" + (resp ? resp.message : "無法取得伺服器回應"));
                }
            }).catch((err) => {
                console.error("資料更新失敗:", err.response.data.error);
                alert(err.response.data.error);
            });
        };

        const member_mail = ref('');
        const token = ref('');
        const showForgotPassword = ref(false);
        const openForgotPassword = () => {
            showForgotPassword.value = true;
        };
        const closeForgotPassword = () => {
            showForgotPassword.value = false;
        };
        const verifyEmail = () => {
            // 該API要的參數
            sendVerifyEmail({
                member_mail: member_mail.value,
            }).then((resp) => {
            }).catch((err) => {
                console.error("資料更新失敗:", err.response.data.error);
                alert(err.response.data.error)
            });
        };
        const verifyToken = () => {
            // 檢查是否都已輸入值
            if (!token.value) {
                alert("請檢查驗證碼不可為空")
            } else {
                // 該API要的參數
                sendVerifyToken({
                    member_mail: member_mail.value,
                    token: token.value,
                }).then((resp) => {
                    if (resp.success) {  // 確保 resp 存在且有 success 屬性
                        showForgotPassword.value = false;
                        showResetPassword.value = true;
                    } else {
                        alert("驗證失敗：" + (resp ? resp.message : "無法取得伺服器回應"));
                    }
                }).catch((error) => {
                    console.error("資料更新失敗:", err.response.data.error);
                    alert(err.response.data.error);
                });
            }
        };

        const showResetPassword = ref(false);
        const openResetPassword = () => {
            showForgotPassword.value = true;
        };
        const closeResetPassword = () => {
            showResetPassword.value = false;
        };
        const resetPassword = ref("");
        const resetPasswrodAgain = ref("");

        const updatePassword = () => {
            // 檢查是否都已輸入值
            if (resetPassword.value != resetPasswrodAgain.value) {
                alert("請檢查確認密碼一致")
            } else {
                // 該API要的參數
                patchPassword({
                    member_mail: member_mail.value,
                    password: resetPassword.value,
                }).then((resp) => {
                    if (resp.success) {  // 確保 resp 存在且有 success 屬性
                        alert("重設密碼成功");
                        closeResetPassword()
                    } else {
                        alert("重設密碼失敗：" + (resp ? resp.message : "無法取得伺服器回應"));
                    }
                }).catch((error) => {
                    console.error("資料更新失敗:", err.response.data.error);
                    alert(err.response.data.error);

                });
            }
        };

        const newAccount = ref("");
        const newMemberName = ref("");
        const newMemberMail = ref("");
        const newPassword = ref("");
        const newPasswordAgain = ref("");
        const showSignup = ref(false);
        const openSignup = () => {
            showSignup.value = true;
        };
        const closeSignup = () => {
            showSignup.value = false;
        };
        const signup = () => {
            if (newPassword.value != newPasswordAgain.value) {
                alert("請確認驗證密碼相符")
            } else {
                const data = { account: newAccount.value, member_name: newMemberName.value, member_mail: newMemberMail.value, password: newPassword.value }
                postUserSignup(data).then((resp) => {
                    showSignup.value = false;
                }).catch((err) => {
                    console.error("資料更新失敗:", err.response.data.error);
                    alert(err.response.data.error);
                });
            }
        }

        return {
            account,
            password,
            showForgotPassword,
            showResetPassword,
            member_mail,
            token,
            resetPassword,
            resetPasswrodAgain,
            newAccount,
            newMemberName,
            newMemberMail,
            newPassword,
            newPasswordAgain,
            showSignup,
            closeSignup,
            openSignup,
            signup,
            login,
            openForgotPassword,
            closeForgotPassword,
            openResetPassword,
            closeResetPassword,
            verifyEmail,
            verifyToken,
            updatePassword
        }
    }
}
</script>

<style scoped>
body {
    margin: 0;
    padding: 0;
    width: 100vw;
    height: 100vh;
    display: flex;
    justify-content: center;
    align-items: center;
}

.title {
    font-size: 70px;
    font-weight: bold;
    background: linear-gradient(90deg, #3a7bd5, #3a6073);
    background-clip: text; /* 標準屬性 */
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent; /* 使用漸層填充文字 */
    text-shadow: 2px 2px 3px rgba(0, 0, 0, 0.2); /* 添加輕微陰影 */
    text-align: center;
    margin: -5% 0 20px 0;
    z-index: 2;
}

.login-container {
    background-image: url('../assets/loginBackground.png'); /* 確定圖片路徑正確 */
    background-size: cover; /* 讓背景圖片覆蓋整個頁面 */
    background-position: center;
    display: flex;
    flex-direction: column; /* 讓內容垂直排列 */
    justify-content: center;
    align-items: center;
    width: 100vw; /* 覆蓋整個視窗寬度 */
    height: 100vh; /* 覆蓋整個視窗高度 */
    position: relative;
    margin-left: -2px;
}

.login-container::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(255, 255, 255, 0.5); /* 半透明白色 */
    z-index: 1; /* 確保覆蓋層在背景圖片上方 */
}

.login-box {
    background-color: rgba(255, 255, 255, 0.9); /* 白色背景加點透明度 */
    border-radius: 20px;
    padding: 40px;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
    text-align: center;
    width: 350px; /* 調整盒子的寬度 */
    z-index: 2;
}

.input-field {
    position: relative;
    width: 100%;
    margin-bottom: 20px;
}

.input-field input {
    width: 100%;
    padding: 12px 20px 12px 45px; /* 調整 padding-left，讓文字離圖示更遠 */
    border-radius: 25px; 
    border: 1px solid #ccc;
    background-color: rgba(240, 240, 240, 0.8);
    font-size: 16px;
}

.input-field i {
    position: absolute;
    left: 15px; /* 圖示的位置 */
    top: 50%;
    transform: translateY(-50%);
    font-size: 18px;
    color: #666;
}

.icon-account,
.icon-password {
    width: 20px;
    height: 20px;
    display: inline-block;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}

.icon-account {
    background-image: url('../assets/user.png'); /* 固定圖片路徑 */
}

.icon-password {
    background-image: url('../assets/ic_password.png'); /* 固定圖片路徑 */
}

.login-options {
    display: flex;
    justify-content: space-between;
    margin-bottom: 20px;
    font-weight: bold;
}

.login-options a {
    color: #666;
    text-decoration: none;
    font-size: 14px;
}

.login-btn {
    padding: 12px 0;
    width: 100%;
    background-color: #3a6073; /* 按鈕的顏色 */
    border: none;
    border-radius: 25px;
    color: white;
    font-size: 20px;
    font-weight: bold;
    cursor: pointer;
    transition: background-color 0.3s ease;
}

.login-btn:hover {
    background-color: #2a4861;
}

.modal-overlay {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: rgba(0, 0, 0, 0.6); /* 提高背景透明度 */
    display: flex;
    justify-content: center;
    align-items: center;
    z-index: 999;
}

/* 模態框內容 */
.modal-content {
    background-color: white;
    padding: 12px 12px; /* 增加內部間距，讓佈局更寬鬆 */
    border-radius: 10px;
    width: 500px; /* 調整彈窗寬度 */
    text-align: center;
    box-shadow: none; /* 移除 box-shadow 和任何 border */
    border: none; /* 移除邊框 */
    position: relative;
    margin: 0; /* 移除外邊距 */
}

/* 彈窗標題樣式 */
.modal-content h2 {
    background-color: #718e9d;
    color: white;
    padding: 15px;
    border-top-left-radius: 10px;
    border-top-right-radius: 10px;
    margin-bottom: 20px;
    font-size: 18px;
    position: relative;
}

.close-btn {
    font-size: 30px;
    position: absolute;
    top: 18px;
    right: 30px;
    cursor: pointer;
    color: white;
}

.close-btn:hover {
    color: #ff0000; /* 當滑鼠懸停時，顏色變成紅色 */
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

/* 調整圖標和輸入框間距 */
.input-group {
    position: relative;
    display: flex; /* 使用 flex 布局確保按鈕和輸入框在同一行 */
    align-items: center;
    margin-bottom: 20px;
}

.input-group input {
    flex-grow: 1; /* 讓輸入框填滿剩餘空間 */
    padding-left: 40px; /* 增加 padding 以容納圖標 */
    padding-right: 10px; /* 確保右側文字不會與按鈕重疊 */
    padding-top: 10px;
    padding-bottom: 10px;
    font-size: 16px;
    border-radius: 5px;
    border: 1px solid #ccc;
    box-sizing: border-box; /* 確保 padding 不會影響寬度 */
}

.icon-label {
    position: absolute;
    top: 50%;
    left: 10px;
    transform: translateY(-50%); /* 讓圖標在輸入框內垂直居中 */
    width: 20px; /* 根據圖標大小進行調整 */
    height: 20px;
    background-size: contain;
    background-repeat: no-repeat;
    background-position: center;
}

/* 圖示 */
.form-group img {
    width: 24px;
    height: 24px;
}

.btn {
    background-color: #34a9e4; /* 按鈕顏色 */
    color: white;
    padding: 10px 30px;
    border: none;
    border-radius: 25px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
}

.btn:hover {
    background-color: #1373a3; /* hover 顏色 */
}

/* 調整發送驗證碼按鈕樣式 */
.send-code-button{
    margin-left: 10px; /* 與輸入框保持適當距離 */
    padding: 10px 15px;
    background-color: #34a9e4; /* 按鈕顏色 */
    border: none;
    border-radius: 25px;
    color: white;
    cursor: pointer;
    font-size: 14px;
    width: 100px;
    font-weight: bold;
}

.send-code-button:hover{
    background-color: #1373a3;
}

.verify-button {
    margin-left: 10px; /* 與輸入框保持適當距離 */
    padding: 10px 15px;
    background-color: #9575CD; /* 按鈕顏色 */
    border: none;
    border-radius: 25px;
    color: white;
    cursor: pointer;
    font-size: 14px;
    width: 100px;
    font-weight: bold;
}

.verify-button:hover {
    background-color: #7542cc;
}

/* 增大 "驗證" 按鈕的大小 */
.button-group .btn {
    padding: 12px 30px; /* 調整按鈕 padding */
    font-size: 16px;
    background-color: #34a9e4;
    border-radius: 25px;
    border: none;
    cursor: pointer;
}

.button-group .btn:hover {
    background-color: #1373a3;
}
</style>