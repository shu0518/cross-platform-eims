// contractModel.js
const db = require("../config/database");
const { format } = require("date-fns");

const Contract = {
    // 驗證 token 是否存在於 member 資料表
    checkMemberLoginViaToken: async function (token) {
        const check = "SELECT * FROM member WHERE token = ?";
        try {
            const [checkAccount] = await db.query(check, [token]);
            if (checkAccount.length === 0) {
                console.error("驗證帳號token不存在");
                return false; // 找不到對應的 token
            } else if (checkAccount[0] == 20) {
                console.error("帳號token無效(未登入/無權限)。");
                return "NA";
            }
            return true; // 找到 token
        } catch (err) {
            console.error("資料庫查詢錯誤:", err);
            throw new Error("資料庫查詢失敗");
        }
    },
    checkMemberExist: async function (account) {
        const sql = "SELECT * FROM member WHERE account = ?";
        try {
            const [resp] = await db.query(sql, [account]);
            if (resp.length > 0) {
                return true;
            } else {
                return false;
            }
        } catch (err) {
            console.error(err);
            return "error";
        }
    },
    // ----------------------------------------------------------------------------------------------------------------------------------------------
    // 取得所有用戶資料，但先檢查 token 是否有效
    getAllMemberViaToken: async function (token) {
        const sql = "SELECT * FROM member"; // 從 member 資料表中取得所有資料
        try {
            const [members] = await db.query(sql);
            return members; // 返回會員資料
        } catch (err) {
            console.error("獲取帳號記錄失敗：", err);
            return null;
        }
    },
    // 用get獲取account與password判斷資料庫是否有此帳號
    checkMemberAccountPassword: async function (account, password) {
        try {
            const query = "SELECT * FROM member WHERE account = ? AND password = ?";
            const queryToken = "UPDATE member SET token = ? WHERE account = ?";
            const [check] = await db.query(query, [account, password]);
            if (check.length > 0) {
                let token = 0;
                if (check[0].permission == 1) {
                    token = generateToken(64, "str");
                } else {
                    return "NA";
                }
                await db.query(queryToken, [token, account]);
                return token;
            } else {
                // 帳號或密碼不正確，回傳 null
                return null;
            }
        } catch (err) {
            // 處理錯誤
            console.error("資料庫查詢錯誤:", err);
            throw new Error("資料庫查詢錯誤");
        }
    },
    // 根據id刪除指定承包商記錄
    deleteMemberViaToken: async function (account) {
        const sql = "DELETE FROM member WHERE account = ?";
        try {
            await db.query(sql, [account]);
            return true;
        } catch (err) {
            console.error("刪除帳號失敗：", err);
            return false;
        }
    },
    // 取得 token 帳號的資料
    getMemberInfoViaToken: async function (token) {
        const check = "SELECT * FROM member WHERE token = ?";
        try {
            const [checkAccount] = await db.query(check, [token]);
            if (checkAccount.length === 0) {
                return false; // 找不到對應的 token
            }
            return checkAccount[0]; // 找到 token
        } catch (err) {
            console.error("資料庫查詢錯誤:", err);
            throw new Error("資料庫查詢失敗");
        }
    },
    // 用get獲取account與password判斷資料庫是否有此帳號
    postMemberInfo: async function (account) {
        const query = "SELECT * FROM member WHERE account = ?";
        try {
            // 取得帳號資訊
            const [resp] = await db.query(query, [account]);
            if (resp.length > 0) {
                return resp[0];
            } else {
                return false;
            }
        } catch (err) {
            // 處理錯誤
            console.error("資料庫查詢錯誤:", err);
            throw new Error("資料庫查詢錯誤");
        }
    },
    // 修改帳號資料
    putMemberInfo: async function (account, member_name, member_mail, password) {
        const sql = `UPDATE member SET  member_name = ?, member_mail = ?, password = ? WHERE account = ?`;
        try {
            // 更新帳號資訊
            await db.query(sql, [member_name, member_mail, password, account]);
            return true;
        } catch (err) {
            console.error("修改帳號失敗：", err);
            return false;
        }
    },
    //新增帳號
    postMember: async function (account, member_name, member_mail, password) {
        const sql = "INSERT INTO member (account, member_name, member_mail, password) VALUES (?, ?, ?, ?)";
        const check = "SELECT * FROM member WHERE account = ?";
        try {
            // 確認帳號不存在
            const [checkResp] = await db.query(check, [account]);
            if (checkResp.length > 0) {
                return null;
            }
            // 取得帳號資訊
            await db.query(sql, [account, member_name, member_mail, password]);
            return "新增帳號成功";
        } catch (err) {
            console.error("創建帳號失敗：", err);
            return { success: false, error: err };
        }
    },
    // 驗證信箱並寄驗證碼
    postMemberViaMail: async function (member_mail) {
        const query = "SELECT * FROM member WHERE member_mail = ?";
        const queryToken = "UPDATE member SET token = ? WHERE member_mail = ?";
        const token = generateToken(6, "num");
        try {
            const [resp] = await db.query(query, [member_mail]);
            if (resp.length > 0) {
                // 帳號密碼正確，生成並儲存 token
                await db.query(queryToken, [token, member_mail]);
                // 回傳使用者資訊和生成的 token
                return token;
            } else {
                // 帳號或密碼不正確，回傳 null
                return null;
            }
        } catch (err) {
            // 處理錯誤
            console.error("資料庫查詢錯誤:", err);
            throw new Error("資料庫查詢錯誤");
        }
    },
    //驗證帳號token
    postMemberViaToken: async function (member_mail, token) {
        const queryMail = "SELECT * FROM member WHERE member_mail = ?";
        const query = "SELECT * FROM member WHERE member_mail = ? AND token = ?";
        try {
            // 驗證帳號
            const [respMail] = await db.query(queryMail, [member_mail]);
            if (respMail.length == 0) {
                return false;
            }
            const [resp] = await db.query(query, [member_mail, token]);
            if (resp.length > 0) {
                return { success: true };
            } else {
                // 帳號或驗證碼不正確，回傳 null
                return null;
            }
        } catch (error) {
            // 處理錯誤
            console.error("資料庫查詢錯誤:", error);
            throw new Error("資料庫查詢錯誤");
        }
    },
    //重設密碼
    patchPassword: async function (member_mail, password) {
        const sql = "UPDATE member SET password = ? WHERE member_mail = ?";
        const check = "SELECT * FROM member WHERE member_mail = ?";
        try {
            const [checkAccount] = await db.query(check, [member_mail]);
            if (checkAccount.length == 0) {
                return "emailError";
            }
            await db.query(sql, [password, member_mail]);
            return { success: true };
        } catch (err) {
            console.error("重設密碼失敗：", err);
            return { success: false, error: err.message }; // 統一返回格式
        }
    },
    // --------------------------------------------------------------------------------------------------------------------------------------------------
    // 驗證 token 是否存在於 member 資料表
    checkContractViaContractId: async function (contract_id) {
        const check = "SELECT * FROM contract WHERE contract_id = ?";
        try {
            const [checkAccount] = await db.query(check, [contract_id]);
            if (checkAccount.length === 0) {
                return false; // 找不到
            }
            return true; // 找到
        } catch (error) {
            console.error("資料庫查詢錯誤:", error);
            throw new Error("資料庫查詢失敗");
        }
    },
    // 獲取所有承包商記錄
    findAllContract: async () => {
        const sql = "SELECT * FROM contract"; //從contract資料表取得所有資料
        try {
            const [rows] = await db.query(sql);
            return rows;
        } catch (err) {
            console.error("獲取承包商記錄失敗：", err);
            return [];
        }
    },
    // 根據給定信息創建新承包商記錄
    createContract: async function (
        contract_name,
        contract_phone,
        contract_address,
        contract_mail,
        contract_business_number
    ) {
        const sql =
            "INSERT INTO contract (contract_name, contract_phone, contract_address, contract_mail, contract_business_number) VALUES (?, ?, ?, ?, ?)";
        try {
            await db.query(sql, [
                contract_name,
                contract_phone,
                contract_address,
                contract_mail,
                contract_business_number,
            ]);
            return "新增承包商成功。";
        } catch (err) {
            console.error("創建承包商記錄失敗：", err);
            return false;
        }
    },
    // 根據id獲取指定承包商記錄
    getContractViaContractId: async (contract_id) => {
        const sql = "SELECT * FROM contract WHERE contract_id = ?";
        try {
            const [rows] = await db.query(sql, [contract_id]);
            if (rows.length > 0) {
                return rows[0];
            } else {
                return null;
            }
        } catch (err) {
            console.error("更新承包商記錄失敗：", err);
            return "contractError";
        }
    },
    // 根據id更新指定承包商記錄
    updateContract: async (
        contract_id,
        contract_name,
        contract_phone,
        contract_address,
        contract_mail,
        contract_business_number
    ) => {
        const sql =
            "UPDATE contract SET contract_name = ?, contract_phone = ?, contract_address = ?, contract_mail = ?, contract_business_number = ? WHERE contract_id = ?";
        try {
            await db.query(sql, [
                contract_name,
                contract_phone,
                contract_address,
                contract_mail,
                contract_business_number,
                contract_id,
            ]);
            return "承包商資訊更新成功！";
        } catch (err) {
            console.error("更新承包商記錄失敗：", err);
            return false;
        }
    },
    // 根據id刪除指定承包商記錄
    deleteContract: async (contract_id) => {
        const sql = "DELETE FROM contract WHERE contract_id = ?";
        try {
            await db.query(sql, [contract_id]);
            return "承包商刪除成功！";
        } catch (err) {
            console.error("刪除承包商記錄失敗：", err);
            return null;
        }
    },
    // ----------------------------------------------------------------------------------------------------------------------------------------------
    getAllClient: async () => {
        const sql = "SELECT * FROM client";
        try {
            const [resp] = await db.query(sql);
            return resp;
        } catch (err) {
            console.error("查詢客戶失敗：", err);
            return "clientError";
        }
    },
    postNewClient: async (client_address, client_name, client_phone, client_mail, business_number, contract_name) => {
        const sql =
            "INSERT INTO client (client_address, client_name, client_phone, client_mail, business_number, contract_name) VALUES (?,?,?,?,?,?)";
        try {
            await db.query(sql, [
                client_address,
                client_name,
                client_phone,
                client_mail,
                business_number,
                contract_name,
            ]);
            return "新增客戶成功";
        } catch (err) {
            console.error("新增客戶失敗：", err);
            return "clientError";
        }
    },
    getClientId: async (client_id) => {
        const sql = "SELECT * FROM client WHERE client_id = ?";
        try {
            const [resp] = await db.query(sql, [client_id]);
            if (resp.length > 0) {
                return resp[0];
            } else {
                return null;
            }
        } catch (err) {
            console.error("查詢客戶失敗：", err);
            return "clientError";
        }
    },
    getAllClientViaContractName: async (contract_name) => {
        const sql = "SELECT * FROM client WHERE contract_name = ?";
        try {
            const [resp] = await db.query(sql, [contract_name]);
            if (resp.length > 0) {
                return resp;
            } else {
                return null;
            }
        } catch (err) {
            console.error("查詢客戶失敗：", err);
            return "clientError";
        }
    },
    getClientIdViaClientAddress: async (client_address) => {
        const sql = "SELECT * FROM client WHERE client_address = ?";
        try {
            const [resp] = await db.query(sql, [client_address]);
            if (resp.length > 0) {
                return resp[0];
            } else {
                return null;
            }
        } catch (err) {
            console.error("查詢客戶失敗：", err);
            return "clientError";
        }
    },
    putClientId: async (
        client_id,
        client_address,
        client_name,
        client_phone,
        client_mail,
        business_number,
        contract_name
    ) => {
        const sql =
            "UPDATE client SET client_address=?, client_name=?, client_phone=?, client_mail=?,business_number=?, contract_name=? WHERE client_id = ?";
        try {
            await db.query(sql, [
                client_address,
                client_name,
                client_phone,
                client_mail,
                business_number,
                contract_name,
                client_id,
            ]);
            return "更新客戶成功";
        } catch (err) {
            console.error("更新客戶失敗：", err);
            return null;
        }
    },
    putClientContractNameAsPersonal: async (client_id) => {
        const sql = "UPDATE client SET contract_name=? WHERE client_id = ?";
        const contract = "個體戶";
        try {
            await db.query(sql, [contract, client_id]);
            return "更新客戶成功";
        } catch (err) {
            console.error("更新客戶失敗：", err);
            return null;
        }
    },
    deleteClientId: async (client_id) => {
        const sql = "DELETE FROM client WHERE client_id = ?";
        try {
            await db.query(sql, [client_id]);
            return "刪除客戶成功";
        } catch (err) {
            console.error("刪除客戶失敗：", err);
            return null;
        }
    },
    // ----------------------------------------------------------------------------------------------------------------------------------------------
    getAllMeasure: async () => {
        const sql = "SELECT * FROM measure ";
        try {
            const [resp] = await db.query(sql);
            return resp;
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗。";
        }
    },
    getAllMeasureViaClientAddress: async (client_address) => {
        const sql = "SELECT * FROM measure WHERE client_address=?";
        try {
            const [resp] = await db.query(sql, [client_address]);
            return resp;
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗。";
        }
    },
    postNewMeasure: async (client_address, length, width, specification, measure_date, position, quantity) => {
        const sql =
            "INSERT INTO measure (client_address,length,width,specification,measure_date,position,quantity) VALUES (?,?,?,?,?,?,?)";
        try {
            specification = JSON.stringify(specification);
            await db.query(sql, [client_address, length, width, specification, measure_date, position, quantity]);
            return "新增數據成功。";
        } catch (err) {
            console.error("新增數據失敗：", err);
            return "新增數據失敗。";
        }
    },
    getMeasureId: async (measure_id) => {
        const sql = "SELECT * FROM measure WHERE measure_id = ?";
        try {
            const [resp] = await db.query(sql, [measure_id]);
            if (resp.length > 0) {
                return resp[0];
            } else {
                return null;
            }
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗";
        }
    },
    putMeasureId: async (
        measure_id,
        client_address,
        length,
        width,
        specification,
        measure_date,
        position,
        quantity
    ) => {
        const sql =
            "UPDATE measure SET client_address=?,length=?,width=?,specification=?,measure_date=?,position=?,quantity=? WHERE measure_id = ?";
        try {
            specification = JSON.stringify(specification);
            await db.query(sql, [
                client_address,
                length,
                width,
                specification,
                measure_date,
                position,
                quantity,
                measure_id,
            ]);
            return "更新數據成功";
        } catch (err) {
            console.error("更新數據失敗：", err);
            return null;
        }
    },
    deleteMeasureId: async (measure_id) => {
        const sql = "DELETE FROM measure WHERE measure_id = ?";
        try {
            await db.query(sql, [measure_id]);
            return "刪除數據成功";
        } catch (err) {
            console.error("刪除刪除失敗：", err);
            return null;
        }
    },
    // ----------------------------------------------------------------------------------------------------------------------------------------------
    getQuotationData: async (measure_id) => {
        const sql = "SELECT * FROM measure WHERE measure_id = ?";
        const clientSql = "SELECT * FROM client WHERE client_address = ?";
        try {
            const [rows] = await db.query(sql, [measure_id]);
            if (rows.length === 0) {
                throw new Error("找不到對應的報價資料。");
            }
            const row = rows[0];
            const [clientData] = await db.query(clientSql, [row.client_address]);
            const client = clientData[0];
            if (client.length === 0) {
                throw new Error("找不到對應的客戶資料。");
            }
            let items = {};
            if (row.specification.windoor == "門") {
                items = {
                    description: `${row.specification.type.frame}/${row.specification.type.piece}　${Math.floor(
                        row.width
                    )}×${Math.floor(row.length)}`,
                    quantity: row.quantity,
                };
                if (row.specification.type.model == "上下") {
                    items.cost = Math.ceil((row.width * row.length) / 918.08) * 400 + 4000;
                } else {
                    items.cost = Math.ceil((row.width * row.length) / 918.08) * 500 + 4500;
                }
            } else {
                items = {
                    description: `${row.specification.type.piece}/${row.specification.type.option}　${Math.floor(
                        row.width
                    )}×${Math.floor(row.length)}`,
                    quantity: row.quantity,
                    cost: Math.ceil((row.width * row.length) / 918.08) * 300 + 3600,
                };
            }
            return items;
        } catch (error) {
            console.error("取得報價資料時發生錯誤：", error);
            throw error;
        }
    },
    getClientDataViaMeasureId: async (measure_id) => {
        const sql = "SELECT client_address FROM measure WHERE measure_id = ?";
        const clientSql = "SELECT * FROM client WHERE client_address = ?";
        const [rows] = await db.query(sql, [measure_id]);
        if (rows.length === 0) {
            throw new Error("找不到對應的報價資料。");
        }
        const row = rows[0];
        const clientData = await db.query(clientSql, [row.client_address]);
        const client = clientData[0];
        if (client.length == 0) {
            throw new Error("找不到對應的客戶資料。");
        }
        return client;
    },
    // ----------------------------------------------------------------------------------------------------------------------------------------------
    getAllMachineViaClientAddress: async (client_address) => {
        const sql = "SELECT * FROM machine WHERE client_address=?";
        try {
            const [resp] = await db.query(sql, [client_address]);
            return resp;
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗。";
        }
    },
    getAllMachine: async () => {
        const sql = "SELECT * FROM machine ";
        try {
            const [resp] = await db.query(sql);
            return resp;
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗。";
        }
    },
    getMachineViaMachineId: async (machine_id) => {
        const sql = "SELECT * FROM machine WHERE machine_id=?";
        try {
            const [resp] = await db.query(sql, [machine_id]);
            return resp;
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗。";
        }
    },
    postMachineViaMeasure: async function (measure_id, token) {
        try {
            const measureData = await this.getMeasureId(measure_id); // 使用 this 引用
            const client_address = measureData.client_address;
            const accountData = await this.getMemberInfoViaToken(token);
            const account = accountData.account;
            const machine_date = formatDate(Date.now());
            const context = { measure_id, client_address, account, machine_date }; // 將共用變數打包成物件

            const checkCreated = await this.getAllMachineViaClientAddress(client_address);
            // 使用 filter() 找出所有符合條件的資料（回傳陣列）
            const results = checkCreated.filter((item) => item.measure_id === measure_id);
            if (results || results.length > 0) {
                const deleteSql = "DELETE FROM machine WHERE measure_id = ?";
                try {
                    await db.query(deleteSql, [measure_id]);
                } catch (err) {
                    console.error("刪除重複加工數據失敗：", err);
                    throw new Error("刪除失敗");
                }
            }
            if (measureData.specification.windoor == "窗") {
                if (measureData.specification.type.piece == "2拉") {
                    const windowResp = await window2(
                        measureData.specification.type.option,
                        measureData.width,
                        measureData.length,
                        context
                    );
                    await glass(
                        measureData.specification.type.option,
                        measureData.specification.type.thick,
                        windowResp,
                        context
                    );
                    return "新增加工數據成功";
                } else if (measureData.specification.type.piece == "3拉") {
                    const windowResp = await window3(
                        measureData.specification.type.option,
                        measureData.width,
                        measureData.length,
                        context
                    );
                    await glass3(
                        measureData.specification.type.option,
                        measureData.specification.type.thick,
                        windowResp,
                        context
                    );
                    return "新增加工數據成功";
                } else if (measureData.specification.type.piece == "4拉") {
                    const windowResp = await window4(
                        measureData.specification.type.option,
                        measureData.width,
                        measureData.length,
                        context
                    );
                    await glass(
                        measureData.specification.type.option,
                        measureData.specification.type.thick,
                        windowResp,
                        context
                    );
                    return "新增加工數據成功";
                }
            } else {
                const doorResp = door(
                    measureData.specification.type.frame,
                    measureData.specification.type.piece,
                    measureData.width,
                    measureData.length
                );
                await doorModel(
                    measureData.specification.type.model,
                    measureData.specification.type.option.up,
                    measureData.specification.type.option.down,
                    doorResp,
                    context
                );
                return "新增加工數據成功";
            }
        } catch (err) {
            console.error("新增加工資料失敗:", err);
            return null;
        }
    },
    putMachineId: async (machine_id, contents) => {
        try {
            // 檢查 contents 是否為空，如果是則設定為空對象
            contents = contents || {};
            // 如果 contents 為空，則不執行更新
            if (Object.keys(contents).length === 0) {
                return "沒有要更新的欄位";
            }

            // 動態構建 SQL 語句和參數
            let sql = "UPDATE machine SET ";
            const params = [];

            // 根據 contents 的鍵生成 SQL 和參數
            for (const [key, value] of Object.entries(contents)) {
                sql += `${key} = ?, `;
                params.push(value);
            }

            // 移除最後的逗號，並添加 WHERE 條件
            sql = sql.slice(0, -2); // 移除最後的 ", "
            sql += " WHERE machine_id = ?";
            params.push(machine_id);
            // 執行查詢
            await db.query(sql, params);
            return "更新數據成功";
        } catch (err) {
            console.error("更新數據失敗：", err);
            return null;
        }
    },
    deleteMachine: async (machine_id) => {
        const sql = "DELETE FROM machine WHERE machine_id = ?";
        try {
            await db.query(sql, [machine_id]);
            return "刪除數據成功";
        } catch (err) {
            console.error("刪除刪除失敗：", err);
            return null;
        }
    },
    // ----------------------------------------------------------------------------------------------------------------------------------------------
    saveExportedFile: async (dataFrom, fileContent, client_address) => {
        const sql = `INSERT INTO exported (data_from, data, date,client_address) VALUES (?, ?, ?, ?)`;
        const values = [dataFrom, fileContent, formatDate(Date.now()), client_address];
        try {
            await db.query(sql, values);
            return true;
        } catch (error) {
            console.error("無法儲存資料：", error);
            return false;
        }
    },
    getExportId: async (exportId) => {
        const sql = `SELECT * FROM exported WHERE export_id = ?`;
        try {
            const results = await db.query(sql, [exportId]);
            return results.length > 0 ? results[0] : null;
        } catch (error) {
            console.error("無法取得資料：", error);
            return null;
        }
    },
    getAllExportedViaClientAddress: async (client_address) => {
        const sql = "SELECT * FROM exported WHERE client_address=?";
        try {
            const [resp] = await db.query(sql, [client_address]);
            return resp;
        } catch (err) {
            console.error("查詢數據失敗：", err);
            return "查詢數據失敗。";
        }
    },
};
module.exports = Contract;

function generateToken(length, type) {
    // str做token，其他做驗證碼
    const characters = "0123456789QWERTYUIOPASDFGHJKLZXCVBNM";
    const num = "0123456789";
    let token = "";
    if (type == "str") {
        for (let i = 0; i < length; i++) {
            token += characters.charAt(Math.floor(Math.random() * characters.length));
        }
    } else {
        for (let i = 0; i < length; i++) {
            token += num.charAt(Math.floor(Math.random() * num.length));
        }
    }
    return token;
}
// --窗加工------------------------------------------------------------------------------------------------------------------------------------------
const window2 = async (typeOption, width, height, context) => {
    const sql866To1068 =
        "INSERT INTO machine (client_address, measure_id, account, machine_date, window_hori, screen_window_hori, inter_branch, exter_branch, screen_window_branch, bottom_line_1, inner_line, screen_window_mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    const sql1066866 =
        "INSERT INTO machine (client_address, measure_id, account, machine_date, window_hori, inter_branch, exter_branch, bottom_line_1, inner_line, flower_grid_width, flower_grid_height, flower_board_width, flower_board_height, flower_net_width, flower_net_height, screen_window_mid ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    const sql10661200 =
        "INSERT INTO machine (client_address, measure_id, account, machine_date, window_hori, screen_window_hori, inter_branch, exter_branch, screen_window_branch, left_line ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try {
        if (typeOption == "866") {
            let results = new Array(14).fill(0);
            results[0] = (width - 15.1) / 2; // 窗戶橫料
            results[1] = results[0] + 6; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 4) / 2; // 下台畫線
            results[6] = results[2] / 2; // 內片大勾 畫線
            // （紗窗站支高150以上）加裝紗窗中腰
            if (results[4] > 150) {
                results[7] = results[1] - 7.7;
            }
            await db.query(sql866To1068, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
            ]);
            return roundResults(results);
        } else if (typeOption == "866水泥窗") {
            let results = new Array(14).fill(0);
            results[0] = (width - 15.1) / 2; // 窗戶橫料
            results[1] = results[0] - 7.9; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] - 9; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 4) / 2; // 下台 畫線
            results[6] = results[2] / 2; // 內片大勾 畫線
            // （紗窗站支高150以上）加裝紗窗中腰
            if (results[4] > 150) {
                results[7] = results[1] - 7.7;
            }
            await db.query(sql866To1068, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
            ]);
            return roundResults(results);
        } else if (typeOption == "1066") {
            let results = new Array(14).fill(0);
            results[0] = (width - 15.1) / 2; // 窗戶橫料
            results[1] = results[0] + 4.5; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 4.0) / 2; // 下台 畫線
            results[6] = results[2] / 2; // 內片大勾 畫線
            // （紗窗站支高150以上）加裝紗窗中腰
            if (results[4] > 150) {
                results[7] = results[1] - 7.7;
            }
            await db.query(sql866To1068, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
            ]);
            return roundResults(results);
        } else if (typeOption == "1068") {
            let results = new Array(14).fill(0);
            results[0] = (width - 20.6) / 2; // 窗戶橫料
            results[1] = results[0] + 6.3; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.5; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 4.0) / 2; // 下台 畫線
            results[6] = results[2] / 2; // 內片大勾 畫線
            // （紗窗站支高150以上）加裝紗窗中腰
            if (results[4] > 150) {
                results[7] = results[1] - 7.7;
            }
            await db.query(sql866To1068, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
            ]);
            return roundResults(results);
        } else if (typeOption == "1066-866流理台反裝") {
            let results = new Array(14).fill(0);
            results[0] = (width - 15.1) / 2; // 窗戶橫料
            results[1] = height - 9.7; // 內片站支
            results[2] = results[1] + 2.7; // 外片站支
            results = roundResults(results);
            results[3] = (width - 4.0) / 2; // 下台畫線
            results[4] = results[2] / 2; // 大勾畫線
            results[5] = results[0] - 9.5; // 花格料寬
            results[6] = (results[2] - 13) / 2 - 4.5; // 花格料高
            results[7] = results[0] - 0.5; // 花板寬
            results[8] = results[2] - 13 + 2.4 - 9.5; // 花板高
            results[9] = results[7] + 3.3; // 花格網寬
            results[10] = results[8] + 3.3; // 花格網高
            // （紗窗站支高150以上）加裝紗窗中腰
            if (results[4] > 150) {
                results[11] = results[1] - 7.7;
            }
            await db.query(sql1066866, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
                results[9],
                results[10],
                results[11],
            ]);
            return roundResults(results);
        } else if (typeOption == "1066加1200型鎖") {
            let results = new Array(14).fill(0);
            results[0] = (width - 7.5 - 15.1) / 2 + 0.2; // 窗戶橫料
            results[1] = results[0] + 1.2; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 7.5 - 4) / 2; // 內左量起畫線
            await db.query(sql10661200, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
            ]);
            return roundResults(results);
        }
    } catch (err) {
        console.error("建立測量資料失敗：", err);
        throw new Error("建立測量資料失敗");
    }
};
const window3 = async (typeOption, width, height, context) => {
    const sql =
        "INSERT INTO machine (client_address, measure_id, account, machine_date, window_hori, screen_window_hori, out_window_hori, inter_branch, exter_branch, screen_window_branch, bottom_line_1, bottom_line_2, inner_line, screen_window_mid) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try {
        if (typeOption == "866") {
            let results = new Array(14).fill(0);
            results[0] = (width - 26) / 4; // 窗戶橫料
            results[1] = results[0] + 6.5; // 紗窗橫料
            results[2] = results[0] * 2 + 7.6; // 外片窗戶橫料
            results[3] = height - 9.7; // 內片站支
            results[4] = results[2] + 2.7; // 外片站支
            results[5] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[6] = (width - 4) / 2; // 下台畫線
            results[7] = (width - 4) / 4; // 下台畫線2（要畫2條線）
            results[8] = results[3] / 2; // 內片大勾畫線
            if (results[5] > 150) {
                // 紗窗站支高於150，加裝紗窗中腰
                results[9] = results[1] - 7.7;
            }
            await db.query(sql, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
                results[9],
            ]);
            return results;
        } else if (typeOption == "1066") {
            let results = new Array(14).fill(0);
            results[0] = (width - 26) / 4; // 窗戶橫料
            results[1] = results[0] + 4.5; // 紗窗橫料
            results[2] = results[0] * 2 + 7.6; // 外片窗戶橫料
            results[3] = height - 9.7; // 內片站支
            results[4] = results[2] + 2.7; // 外片站支
            results[5] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[6] = (width - 4) / 2; // 下台畫線
            results[7] = (width - 4) / 4; // 下台畫線2（要畫2條線）
            results[8] = results[3] / 2; // 內片大勾畫線
            if (results[5] > 150) {
                // 紗窗站支高於150，加裝紗窗中腰
                results[9] = results[1] - 7.7;
            }
            await db.query(sql, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
                results[9],
            ]);
            return results;
        } else if (typeOption == "1068") {
            let results = new Array(14).fill(0);
            results[0] = (width - 35.6) / 4; // 窗戶橫料
            results[1] = results[0] + 6.3; // 紗窗橫料
            results[2] = results[0] * 2 + 11.8; // 外片窗戶橫料
            results[3] = height - 9.7; // 內片站支
            results[4] = results[2] + 2.7; // 外片站支
            results[5] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[6] = (width - 4) / 2; // 下台畫線
            results[7] = (width - 4) / 4; // 下台畫線2（要畫2條線）
            results[8] = results[3] / 2; // 內片大勾畫線
            if (results[5] > 150) {
                // 紗窗站支高於150，加裝紗窗中腰
                results[9] = results[1] - 7.7;
            }
            await db.query(sql, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
                results[9],
            ]);
            return results;
        }
    } catch (err) {
        console.error("新增加工資料失敗:", err);
        return null;
    }
};
const window4 = async (typeOption, width, height, context) => {
    const sql =
        "INSERT INTO machine (client_address, measure_id, account, machine_date, window_hori, screen_window_hori, inter_branch, exter_branch, screen_window_branch, bottom_line_1, bottom_line_2, inner_line, screen_window_mid) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try {
        if (typeOption == "866") {
            let results = new Array(14).fill(0);
            results[0] = (width - 29.4) / 4; // 窗戶橫料
            results[1] = results[0] + 7.5; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 4) / 4 - 1; // 下台劃線1
            results[6] = (width - 4) / 2; // 下台劃線2
            results[7] = results[2] / 2; // 大勾劃線
            if (results[4] > 150) {
                // 紗窗站支高於150，加裝紗窗中腰
                results[8] = results[1] - 7.7;
            }
            await db.query(sql, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
            ]);
            return results;
        } else if (typeOption == "1066") {
            let results = new Array(14).fill(0);
            results[0] = (width - 29.4) / 4; // 窗戶橫料
            results[1] = results[0] + 5.9; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.2; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 4) / 4 - 1; // 下台劃線1
            results[6] = (width - 4) / 2; // 下台劃線2
            results[7] = results[2] / 2; // 大勾劃線
            if (results[4] > 150) {
                // 紗窗站支高於150，加裝紗窗中腰
                results[8] = results[1] - 7.7;
            }
            await db.query(sql, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
            ]);
            return results;
        } else if (typeOption == "1068") {
            let results = new Array(14).fill(0);
            results[0] = (width - 20.6) / 4; // 窗戶橫料
            results[1] = results[0] + 6.3; // 紗窗橫料
            results[2] = height - 9.7; // 內片站支
            results[3] = results[2] + 2.7; // 外片站支
            results[4] = results[3] + 1.5; // 紗窗站支
            results = roundResults(results);
            results[5] = (width - 5) / 4 - 1; // 下台劃線1
            results[6] = (width - 4) / 2; // 下台劃線2
            results[7] = results[2] / 2; // 大勾劃線
            if (results[4] > 150) {
                // 紗窗站支高於150，加裝紗窗中腰
                results[8] = results[1] - 7.7;
            }
            await db.query(sql, [
                context.client_address,
                context.measure_id,
                context.account,
                context.machine_date,
                results[0],
                results[1],
                results[2],
                results[3],
                results[4],
                results[5],
                results[6],
                results[7],
                results[8],
            ]);
            return results;
        }
    } catch (err) {
        console.error("新增加工資料失敗:", err);
        return null;
    }
};
const glass = async (typeOption, glassThick, results, context) => {
    try {
        let glassWidth = 0,
            glassHeight = 0;
        const sql = "UPDATE machine SET glass_width=?,glass_height=? WHERE measure_id = ?";
        if (typeOption === "1068") {
            if (glassThick === "5mm玻璃") {
                // 一般玻璃算法 5mm
                glassWidth = results[0] - 1.4;
                glassHeight = results[2] - 6 - 6.2 + 1.5;
            } else if (glassThick === "8mm玻璃" || glassThick === "複合玻璃") {
                // 強化玻璃算法 8mm玻璃 或膠合
                glassWidth = results[0] - 0.5;
                glassHeight = results[2] - 6 - 6.2 + 1.7;
            }
        } else {
            if (glassThick === "5mm玻璃") {
                // 一般玻璃算法 5mm
                glassWidth = results[0] - 1.4;
                glassHeight = results[2] - 6.5 - 6.5 + 1.5;
            } else if (glassThick === "8mm玻璃" || glassThick === "複合玻璃") {
                // 強化玻璃算法 8mm玻璃 或膠合
                glassWidth = results[0] - 0.5;
                glassHeight = results[2] - 6.5 - 6.5 + 1.7;
            }
        }
        await db.query(sql, [glassWidth, glassHeight, context.measure_id]);
        return [glassWidth, glassHeight];
    } catch (err) {
        console.error("新增加工資料失敗:", err);
        return null;
    }
};
const glass3 = async (typeOption, glassThick, results, context) => {
    // 3拉的窗戶
    let glassWidth = 0,
        glassHeight = 0,
        glassWidth2 = 0;
    const sql = "UPDATE machine SET glass_width=?, glass_second_width=?, glass_height=? WHERE measure_id = ?";
    try {
        if (typeOption === "866" || typeOption === "1066") {
            // 866 或 1066 的窗戶
            if (glassThick === "5mm玻璃") {
                // 一般玻璃算法 5mm
                glassWidth = results[0] - 1.4;
                glassWidth2 = results[2] - 1.4;
                glassHeight = results[3] - 6.5 - 6.5 + 1.5;
            } else if (glassThick === "8mm玻璃" || glassThick === "複合玻璃") {
                // 強化玻璃算法 8mm玻璃 或膠合
                glassWidth = results[0] - 0.5;
                glassWidth2 = results[2] - 0.5;
                glassHeight = results[3] - 6.5 - 6.5 + 1.7;
            }
        }
        if (typeOption === "1068") {
            if (glassThick === "5mm玻璃") {
                // 一般玻璃算法 5mm玻璃
                glassWidth = results[0] - 1.4;
                glassWidth2 = results[2] - 1.4;
                glassHeight = results[3] - 6 - 6.2 + 1.5;
            } else if (glassThick === "8mm玻璃" || glassThick === "複合玻璃") {
                // 強化玻璃算法 8mm玻璃 或膠合
                glassWidth = results[0] - 0.5;
                glassWidth2 = results[2] - 0.5;
                glassHeight = results[3] - 6 - 6.2 + 1.7;
            }
        }
        await db.query(sql, [glassWidth, glassWidth2, glassHeight, context.measure_id]);
        return [glassWidth, glassWidth2, glassHeight];
    } catch (err) {
        console.error("新增加工資料失敗:", err);
        return null;
    }
};
// --門加工------------------------------------------------------------------------------------------------------------------------------------------
const door = (typeFrame, typePiece, width, height) => {
    // 使用有意義的屬性名稱替代索引，提升可讀性
    let results = {
        door_width: 0, // 門框橫料
        door_height: height, // 門框橫料
        door_piece_width: 0, // 門片橫料
        door_piece_height: 0, // 門片高
    };
    try {
        switch (typeFrame) {
            case "L型兩孔門框":
                results.door_width = width - 4.0;
                if (typePiece === "700型") {
                    results.door_piece_width = results.door_width - 7.8;
                } else if (typePiece === "700型(700加大)") {
                    results.door_piece_width = results.door_width - 19.8;
                }
                results.door_piece_height = height - 2 - 1.5;
                break;
            case "7公分":
                results.door_width = width - 7.0;
                if (typePiece === "700型") {
                    results.door_piece_width = results.door_width - 7.8;
                } else if (typePiece === "700型(700加大)") {
                    results.door_piece_width = results.door_width - 19.8;
                }
                results.door_piece_height = height - 3.5 - 1.5;
                break;
            case "3×10框偏領":
                results.door_width = width - 6.0;
                if (typePiece === "700型") {
                    results.door_piece_width = results.door_width - 7.8;
                } else if (typePiece === "700型(700加大)") {
                    results.door_piece_width = results.door_width - 19.8;
                }
                results.door_piece_height = height - 3 - 1.5;
                break;
            case "3×10框中領":
                results.door_width = width - 6.0;
                results.door_piece_width = results.door_width - 19;
                results.door_piece_height = height - 3 - 1.5;
                break;
            case "4.5×10框中領":
                results.door_width = width - 9.0;
                results.door_piece_width = results.door_width - 19;
                results.door_piece_height = height - 4.5 - 1.5;
                break;
        }
        return results;
    } catch (err) {
        console.error("新增加工資料失敗:", err);
        return null;
    }
};
const doorModel = async (typeModel, typeOptionUp, typeOptionDown, results, context) => {
    const sql = `
    INSERT INTO machine (
        client_address, measure_id, account, machine_date, 
        door_width, door_height, door_piece_width, door_piece_height, 
        glass_width, glass_height, glass_second_width, 
        glass_second_height, leaf_width, leaf_height, leaf_number, 
        flower_width, flower_height, flower_board_width, 
        flower_board_height, flower_grid_width, flower_grid_height, 
        flower_net_width, flower_net_height, 
        mid_flower_net_height, mid_flower_net_thing_height
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
`;
    try {
        // 新增所需的物件屬性，預設為0
        Object.assign(results, {
            glass_width: 0,
            glass_height: 0,
            glass_second_width: 0,
            glass_second_height: 0,
            leaf_width: 0,
            leaf_height: 0,
            leaf_number: 0,
            flower_width: 0,
            flower_height: 0,
            flower_board_width: 0,
            flower_board_height: 0,
            flower_grid_width: 0,
            flower_grid_height: 0,
            flower_net_width: 0,
            flower_net_height: 0,
            mid_flower_net_height: 0,
            mid_flower_net_thing_height: 0,
        });
        if (typeModel === "上下") {
            switch (typeOptionUp) {
                case "5mm玻璃":
                    results.glass_width = results.door_piece_width - 1.4;
                    results.glass_height = results.door_piece_height - 90 - 13.5;
                    break;
                case "8mm玻璃" || "複合玻璃":
                    results.glass_width = results.door_piece_width - 0.5;
                    results.glass_height = results.door_piece_height - 90 - 12.5;
                    break;
                case "花板":
                    results.flower_board_width = results.door_piece_width - 0.5;
                    results.flower_board_height = results.door_piece_height - 12.5;
                    break;
                case "混色花板":
                    results.flower_board_width = results.door_piece_width - 0.5;
                    results.flower_board_height = results.door_piece_height - 20 + 2.5;
                    break;
                case "門花":
                    results.flower_width = results.door_piece_width - 3 - 0.2;
                    results.flower_height = results.door_piece_height - 90 - 15 - 0.2;
                    break;
                case "全門花":
                    results.flower_width = results.door_piece_width - 3 - 0.2;
                    results.flower_height = results.door_piece_height - 20 - 0.1;
                    break;
                case "上百葉":
                    results.leaf_width = results.door_piece_width - 3 - 0.3;
                    results.leaf_height = results.door_piece_height - 90 - 15 - 0.1;
                    results.leaf_number = Math.ceil(results.leaf_height / 4);
                    break;
            }
            switch (typeOptionDown) {
                case "5mm玻璃":
                    results.glass_second_width = results.door_piece_width - 1.4;
                    results.glass_second_height = 90 - 13.5;
                    break;
                case "8mm玻璃" || "複合玻璃":
                    results.glass_second_width = results.door_piece_width - 0.5;
                    results.glass_second_height = 90 - 12.5;
                    break;
                case "花板":
                    results.flower_board_width = results.door_piece_width - 0.5;
                    results.flower_board_height = 90 - 12.5;
                    break;
                case "下百葉":
                    results.leaf_width = results.door_piece_width - 3 - 0.3;
                    results.leaf_height = 90 - 15 - 0.1;
                    results.leaf_number = Math.ceil(results.leaf_height / 4);
                    break;
            }
        } else if (typeModel === "上中下") {
            // 上中下模式的計算邏輯
            results.flower_grid_width = results.door_piece_width - 9; // 花格料寬
            results.flower_net_width = results.flower_grid_width + 3.3; // 上花格網寬
            results.flower_grid_height = results.door_piece_height - 90 - 15 - 6; // 花格料高
            results.flower_net_height = results.flower_grid_height + 3.3; // 上花格網高
            results.mid_flower_net_height = 90 - 35.6; // 中花格網料高
            results.mid_flower_net_thing_height = results.mid_flower_net_height + 3.3; // 中花格網高
            results.flower_board_width = results.door_piece_width - 0.5; // 花板寬
            results.flower_board_height = 12.5; // 花板高，固定 12.5
        }
        await db.query(sql, [
            context.client_address,
            context.measure_id,
            context.account,
            context.machine_date,
            results.door_width,
            results.door_height,
            results.door_piece_width,
            results.door_piece_height,
            results.glass_width,
            results.glass_height,
            results.glass_second_width,
            results.glass_second_height,
            results.leaf_width,
            results.leaf_height,
            results.leaf_number,
            results.flower_width,
            results.flower_height,
            results.flower_board_width,
            results.flower_board_height,
            results.flower_grid_width,
            results.flower_grid_height,
            results.flower_net_width,
            results.flower_net_height,
            results.mid_flower_net_height,
            results.mid_flower_net_thing_height,
        ]);
        return results;
    } catch (err) {
        console.error("新增加工資料失敗：", err);
        return null;
    }
};

function roundResults(results) {
    return results.map((value) => Math.floor(value * 10) / 10);
}
function formatDate(dateString) {
    return format(new Date(dateString), "yyyy-MM-dd");
}
