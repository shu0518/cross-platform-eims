// 匯入套件
import axios from "axios";
import router from "@/router";
// 設定api固定的網址(不包括後端會改變的router 路徑)
const apiUrl = "http://127.0.0.1:8080/api";

function checkToken() {
    const token = localStorage.getItem("token");
    if (!token) {
        // Token 不存在，清除 localStorage 並返回 null
        localStorage.clear();
        alert("請重新登入");
        router.push({ name: "login" });
        return null;
    }
    const expireTime = localStorage.getItem("expireTime");
    const now = new Date();
    // 驗證 expireTime 是否存在，並檢查是否已過期
    if (!expireTime || now.getTime() > expireTime) {
        // expireTime 不存在或已過期，清除 localStorage 並返回 null
        localStorage.clear();
        alert("請重新登入");
        router.push({ name: "login" });
        return null;
    }

    return token; // Token 和 expireTime 均有效，返回 token
}


// 取得所有帳號
export async function getUser() {
    const api = `${apiUrl}/user`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (err) {
        console.error("API 請求失敗:", err); // 捕獲並處理錯誤
        // throw err; // 向上層拋出錯誤
    }
}
// 登入帳號
export async function postUser(contents) {
    const api = `${apiUrl}/user`;
    const content = {
        account: contents.account,
        password: contents.password,
    };
    try {
        const res = await axios.post(api, content); // 等待 axios 請求完成
        localStorage.setItem("token", res.data.member);
        const expireTimeInHours = 8;
        const now = new Date();
        const expireTime = now.getTime() + expireTimeInHours * 60 * 60 * 1000; // 8 小時轉換為毫秒
        localStorage.setItem("expireTime", expireTime);
        return res.data; // 返回伺服器回應的數據
    } catch (err) {
        console.error("API 請求失敗:", err); // 捕獲並處理錯誤
        throw err; // 向上層拋出錯誤
    }
}
// 透過token取得帳號資料
export async function getUserInfo() {
    const api = `${apiUrl}/user/info`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (err) {
        console.error("API 請求失敗:", err.response.data.error); // 捕獲並處理錯誤
        throw err; // 向上層拋出錯誤
    }
}
// 取得帳號資料
export async function postUserInfo(account) {
    const api = `${apiUrl}/user/info`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.post(api, { headers: { Authorization: `${token}` }, data: { account } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 修改帳號資料
export async function putUserInfo(contents) {
    const api = `${apiUrl}/user/info`;
    const token = checkToken();
    if (!token) return;
    const content = {
        account: contents.account,
        member_name: contents.member_name,
        member_mail: contents.member_mail,
        password: contents.password,
    };
    try {
        const res = await axios.put(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 刪除帳號資料
export async function deleteUser(account) {
    const api = `${apiUrl}/user`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.delete(api, { headers: { Authorization: `${token}` }, data: { account } });
        return res.data;
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 註冊帳號
export async function postUserSignup(contents) {
    const api = `${apiUrl}/user/signup`;
    const content = {
        account: contents.account,
        member_name: contents.member_name,
        member_mail: contents.member_mail,
        password: contents.password,
    };
    return axios.post(api, content).then((res) => {
        return res.data;
    });
}
// 忘記密碼 驗證信箱&寄驗證碼
export async function sendVerifyEmail(member_mail) {
    const api = `${apiUrl}/user/forgot_mail`;
    return axios.post(api, member_mail).then((res) => {
        return res.data; // 返回伺服器回應的數據，而不是整個回應物件
    });
}
// 忘記密碼 驗證碼
export async function sendVerifyToken(contents) {
    const api = `${apiUrl}/user/forgot_token`;
    const content = {
        member_mail: contents.member_mail,
        token: contents.token,
    };
    return axios.post(api, content).then((res) => {
        return res.data; // 返回伺服器回應的數據，而不是整個回應物件
    });
}
// 重設密碼
export async function patchPassword(contents) {
    const api = `${apiUrl}/user/forgot_password`;
    const content = {
        member_mail: contents.member_mail,
        password: contents.password,
    };
    return axios.patch(api, content).then((res) => {
        return res.data; // 返回伺服器回應的數據，而不是整個回應物件
    });
}
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 撈所有承包商
export async function getContract() {
    const api = `${apiUrl}/contract`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 新增承包商
export async function postContract(contents) {
    const api = `${apiUrl}/contract`;
    const content = {
        contract_name: contents.contract_name,
        contract_phone: contents.contract_phone,
        contract_address: contents.contract_address,
        contract_mail: contents.contract_mail,
        contract_business_number: contents.contract_business_number,
    };
    const token = checkToken();
    if (!token) return;
    const res = await axios.post(api, content, { headers: { Authorization: `${token}` } });
    return res.data;
}
// 取得承包商資訊
export async function getContractContractId(contract_id) {
    const api = `${apiUrl}/contract/${contract_id}`;
    const token = checkToken();
    if (!token) return;
    const res = await axios.get(api, { headers: { Authorization: `${token}` } });
    return res.data;
}
// 修改承包商
export async function putContractContractId(contents) {
    const api = `${apiUrl}/contract/${contents.contract_id}`;
    const token = checkToken();
    if (!token) return;
    const content = {
        contract_name: contents.contract_name,
        contract_phone: contents.contract_phone,
        contract_address: contents.contract_address,
        contract_mail: contents.contract_mail,
        contract_business_number: contents.contract_business_number,
    };
    const res = await axios.put(api, content, { headers: { Authorization: `${token}` } });
    return res.data;
}
// 刪除承包商
export async function deleteContractContractId(contract_id) {
    const api = `${apiUrl}/contract/${contract_id}`;
    const token = checkToken();
    if (!token) return;
    const resp = await axios.delete(api, { headers: { Authorization: `${token}` } });
    return resp.data;
}
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 取得所有客戶
export async function getClient() {
    const api = `${apiUrl}/client`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 新增客戶
export async function postClient(contents) {
    const api = `${apiUrl}/client/newClient`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: contents.client_address,
        client_name: contents.client_name,
        client_phone: contents.client_phone,
        client_mail: contents.client_mail,
        business_number: contents.business_number,
        contract_name: contents.contract_name,
    };
    try {
        const res = await axios.post(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 取得客戶資訊
export async function getClientClientid(client_id) {
    const api = `${apiUrl}/client/${client_id}`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 取得客戶資訊
export async function postClientClientAddress(clientAddress) {
    const api = `${apiUrl}/client`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: clientAddress,
    };
    try {
        const res = await axios.post(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
} // 修改客戶資訊
export async function putClientClientid(contents) {
    const api = `${apiUrl}/client/${contents.client_id}`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: contents.client_address,
        client_name: contents.client_name,
        client_phone: contents.client_phone,
        client_mail: contents.client_mail,
        business_number: contents.business_number,
        contract_name: contents.contract_name,
    };
    try {
        const res = await axios.put(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 刪除客戶
export async function deleteClientClientid(client_id) {
    const api = `${apiUrl}/client/${client_id}`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.delete(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 取得所有數據
export async function getMeasure() {
    const api = `${apiUrl}/measure`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 取得所有客戶的數據
export async function postMeasure(client_address) {
    const api = `${apiUrl}/measure`;
    const token = checkToken();
    if (!token) return;
    const content = { client_address: client_address };
    try {
        const res = await axios.post(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 新增數據
export async function postMeasureNewMeasure(contents) {
    const api = `${apiUrl}/measure/newMeasure`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: contents.client_address,
        length: contents.length,
        width: contents.width,
        specification: contents.specification,
        measure_date: contents.measure_date,
        position: contents.position,
        quantity: contents.quantity,
        number: contents.number,
    };
    try {
        const res = await axios.post(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 取得特定數據
export async function getMeasureMeasureId(measure_id) {
    const api = `${apiUrl}/measure/measureId/${measure_id}`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 修改數據
export async function putMeasureMeasureId(contents) {
    const api = `${apiUrl}/measure/measureId/${contents.measure_id}`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: contents.client_address,
        length: contents.length,
        width: contents.width,
        specification: contents.specification,
        measure_date: contents.measure_date,
        position: contents.position,
        quantity: contents.quantity,
    };
    try {
        const res = await axios.put(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 刪除數據
export async function deleteMeasureMeasureId(measure_id) {
    const api = `${apiUrl}/measure/measureId/${measure_id}`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.delete(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 下載報價單數據
export async function getDownloadQuotation(measure_id) {
    const api = `${apiUrl}/download/quotation`;
    const token = checkToken();
    if (!token) return;
    try {
        const content = {
            measure_id: measure_id,
        };
        const res = await axios.post(api, content, {
            headers: { Authorization: `${token}` },
            responseType: "blob", // 重要：確保接收的是二進位文件
        });
        // 取得 Content-Disposition 標頭
        const contentDisposition = res.headers["content-disposition"];
        let filename = "quotation.xlsx"; // 預設檔案名稱

        // 從 Content-Disposition 解析檔名 (filename* 為優先)
        if (contentDisposition) {
            const utf8FilenameMatch = contentDisposition.match(/filename\*=\s*UTF-8''([^;]+)/);
            const asciiFilenameMatch = contentDisposition.match(/filename="([^"]+)"/);
            if (utf8FilenameMatch) {
                filename = decodeURIComponent(utf8FilenameMatch[1]); // UTF-8 編碼的檔名
            } else if (asciiFilenameMatch) {
                filename = asciiFilenameMatch[1]; // ASCII 檔名
            }
        }
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename); // 設置下載檔名
        document.body.appendChild(link);
        link.click();
        link.remove(); // 點擊後移除連結
    } catch (error) {
        console.error("API 請求失敗:", error);
        alert("下載失敗，請稍後再試。");
    }
}
export async function getDownloadMachine(machine_id) {
    const api = `${apiUrl}/download/machine`;
    const token = checkToken();
    if (!token) return;
    try {
        const content = { machine_id: machine_id };
        // 發送請求至後端以獲取 Word 文件
        const res = await axios.post(api, content, {
            headers: { Authorization: `${token}` },
            responseType: "blob", // 重要：確保接收的是二進位文件
        });
        // 取得 Content-Disposition 標頭
        const contentDisposition = res.headers["content-disposition"];
        let filename = "machine.xlsx"; // 預設檔案名稱

        // 從 Content-Disposition 解析檔名 (filename* 為優先)
        if (contentDisposition) {
            const utf8FilenameMatch = contentDisposition.match(/filename\*=\s*UTF-8''([^;]+)/);
            const asciiFilenameMatch = contentDisposition.match(/filename="([^"]+)"/);
            if (utf8FilenameMatch) {
                filename = decodeURIComponent(utf8FilenameMatch[1]); // UTF-8 編碼的檔名
            } else if (asciiFilenameMatch) {
                filename = asciiFilenameMatch[1]; // ASCII 檔名
            }
        }
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename); // 設置下載檔名
        document.body.appendChild(link);
        link.click();
        link.remove(); // 點擊後移除連結
    } catch (error) {
        console.error("API 請求失敗:", error);
        alert("下載失敗，請稍後再試。");
    }
}
export async function getDownloadMaterial(machine_id) {
    const api = `${apiUrl}/download/material`;
    const token = checkToken();
    if (!token) return;
    try {
        const content = { machine_id: machine_id };
        // 發送請求至後端以獲取 Word 文件
        const res = await axios.post(api, content, {
            headers: { Authorization: `${token}` },
            responseType: "blob", // 重要：確保接收的是二進位文件
        });
        // 取得 Content-Disposition 標頭
        const contentDisposition = res.headers["content-disposition"];
        let filename = "material.xlsx"; // 預設檔案名稱

        // 從 Content-Disposition 解析檔名 (filename* 為優先)
        if (contentDisposition) {
            const utf8FilenameMatch = contentDisposition.match(/filename\*=\s*UTF-8''([^;]+)/);
            const asciiFilenameMatch = contentDisposition.match(/filename="([^"]+)"/);
            if (utf8FilenameMatch) {
                filename = decodeURIComponent(utf8FilenameMatch[1]); // UTF-8 編碼的檔名
            } else if (asciiFilenameMatch) {
                filename = asciiFilenameMatch[1]; // ASCII 檔名
            }
        }
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename); // 設置下載檔名
        document.body.appendChild(link);
        link.click();
        link.remove(); // 點擊後移除連結
    } catch (error) {
        console.error("API 請求失敗:", error);
        alert("下載失敗，請稍後再試。");
    }
}
export async function getDownloadExported(contents) {
    const api = `${apiUrl}/download/exported`;
    const token = checkToken();
    if (!token) return;
    const content = { export_id: contents.export_id };
    try {
        // 發送請求至後端以獲取 Word 文件
        const res = await axios.post(api, content, {
            headers: { Authorization: `${token}` },
            responseType: "blob", // 重要：確保接收的是二進位文件
        });
        // 取得 Content-Disposition 標頭
        const contentDisposition = res.headers["content-disposition"];
        let filename = "exportedFile.xlsx"; // 預設檔案名稱

        // 從 Content-Disposition 解析檔名 (filename* 為優先)
        if (contentDisposition) {
            const utf8FilenameMatch = contentDisposition.match(/filename\*=\s*UTF-8''([^;]+)/);
            const asciiFilenameMatch = contentDisposition.match(/filename="([^"]+)"/);
            if (utf8FilenameMatch) {
                filename = decodeURIComponent(utf8FilenameMatch[1]); // UTF-8 編碼的檔名
            } else if (asciiFilenameMatch) {
                filename = asciiFilenameMatch[1]; // ASCII 檔名
            }
        }
        const url = window.URL.createObjectURL(new Blob([res.data]));
        const link = document.createElement("a");
        link.href = url;
        link.setAttribute("download", filename); // 設置下載檔名
        document.body.appendChild(link);
        link.click();
        link.remove(); // 點擊後移除連結
    } catch (error) {
        console.error("API 請求失敗:", error);
        alert("下載失敗，請稍後再試。");
    }
}
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 新增數據
export async function postNewMachine(measure_ids) {
    const api = `${apiUrl}/machine/newMachine`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.post(api, measure_ids, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 取得所有加工數據
export async function getMachine() {
    const api = `${apiUrl}/machine`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.get(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 取得所有客戶的加工數據
export async function postMachine(client_address) {
    const api = `${apiUrl}/machine`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: client_address,
    };
    try {
        const res = await axios.post(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 修改加工數據
export async function putMachineMachineId(content) {
    const api = `${apiUrl}/machine/${content.machine_id}`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.put(api, content.contents, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// 刪除加工數據
export async function deleteMachineMachineId(machine_id) {
    const api = `${apiUrl}/machine/${machine_id}`;
    const token = checkToken();
    if (!token) return;
    try {
        const res = await axios.delete(api, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 取得所有歷史單據
export async function postExported(client_address) {
    const api = `${apiUrl}/exported`;
    const token = checkToken();
    if (!token) return;
    const content = {
        client_address: client_address,
    };
    try {
        const res = await axios.post(api, content, { headers: { Authorization: `${token}` } }); // 等待 axios 請求完成
        return res.data; // 返回伺服器回應的數據
    } catch (error) {
        console.error("API 請求失敗:", error); // 捕獲並處理錯誤
        throw error; // 向上層拋出錯誤
    }
}
