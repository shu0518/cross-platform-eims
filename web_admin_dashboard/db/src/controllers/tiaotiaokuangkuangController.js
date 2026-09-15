// 引入Contract模型
const model = require("../models/tiaotiaokuangkuangModel");
const ExcelJS = require("exceljs");
const { Document, Packer, Paragraph, TextRun } = require("docx");
const fs = require("fs");
const path = require("path");
// 引入驗證碼必要模組
const nodemailer = require("nodemailer");
const crypto = require("crypto");
const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
const { format } = require("date-fns");

exports.getTest = async (req, res) => {
    return res.status(200).json("hi");
};
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 獲取所有帳號
exports.getUser = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const member = await model.getAllMemberViaToken(token);
        if (!member) {
            return res.status(400).json({ success: false, error: "獲取帳號失敗。" });
        }
        return res.status(200).json({ success: true, member });
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法獲取帳號列表。" });
    }
};
// 確認登入的帳號密碼
exports.postUser = async (req, res) => {
    try {
        const { account, password } = req.body;
        if (!account) {
            return res.status(400).json({ success: false, error: "帳號不可為空" });
        } else if (!password) {
            return res.status(400).json({ success: false, error: "密碼不可為空" });
        }
        const member = await model.checkMemberAccountPassword(account, password);
        if (!member) {
            return res.status(400).json({ success: false, error: "找不到該帳號/密碼。" });
        } else if (member == "NA") {
            return res.status(403).json({ success: false, error: "該用戶無權限。" });
        }
        return res.status(200).json({ success: true, member });
    } catch (err) {
        console.error("伺服器錯誤:", err); // 確認錯誤情況
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法完成登入。" });
    }
};
// 根據刪除帳號
exports.deleteUser = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { account } = req.body;
        const checkAccount = await model.checkMemberExist(account);
        if (!checkAccount) {
            return res.status(400).json({ success: false, error: "刪除的帳號不存在。" });
        }
        if (account == 3141592654) {
            return res.status(403).json({ success: false, error: "超級使用者無法被刪除。" });
        }
        const resp = await model.deleteMemberViaToken(account);
        if (!resp) {
            return res.status(400).json({ success: false, error: "無法刪除帳號。" });
        }
        return res.status(200).json({ success: true, member: "成功刪除帳號。" });
    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, error: "伺服器錯誤，無法刪除帳號。" });
    }
};
// 獲取token帳號資訊
exports.getUserInfo = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const member = await model.getMemberInfoViaToken(token);
        if (!member) {
            return res.status(400).json({ success: false, error: "找不到該帳號。" });
        }
        return res.status(200).json({ success: true, member });
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法獲取帳號列表。" });
    }
};
// 獲取要求的帳號資訊
exports.postUserInfo = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { account } = req.body;
        const member = await model.postMemberInfo(account);
        if (!member) {
            return res.status(400).json({ success: false, error: "找不到該帳號。" });
        }
        return res.status(200).json({ success: true, member });
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法獲取帳號列表。" });
    }
};
// 修改帳號資料
exports.putUserInfo = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { account, member_name, member_mail, password } = req.body;
        if (!account) {
            return res.status(400).json({ success: false, error: "帳號不可為空" });
        } else if (!member_name) {
            return res.status(400).json({ success: false, error: "姓名不可為空" });
        } else if (!member_mail) {
            return res.status(400).json({ success: false, error: "信箱不可為空" });
        } else if (!password) {
            return res.status(400).json({ success: false, error: "密碼不可為空" });
        } else if (account.length > 10 || account.length < 9) {
            return res.status(400).json({ success: false, error: "請確認電話號碼。" });
        } else if (!emailPattern.test(member_mail)) {
            return res.status(400).json({ success: false, error: "請確認電子信箱。" });
        }
        const checkAccount = await model.checkMemberExist(account);
        if (!checkAccount) {
            return res.status(400).json({ success: false, error: "修改的帳號不存在。" });
        }
        const resp = await model.putMemberInfo(account, member_name, member_mail, password);
        if (!resp) {
            return res.status(400).json({ success: false, error: "修改帳號發生錯誤。" });
        }
        return res.status(200).json({ success: true, member: "帳號資料修改成功。" });
    } catch (err) {
        console.error("伺服器錯誤:", err); // 確認錯誤情況
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法完成登入。" });
    }
};
// 創建新帳號
exports.postUserSignUp = async (req, res) => {
    try {
        const { account, member_name, member_mail, password } = req.body;
        if (!account) {
            return res.status(400).json({ success: false, error: "帳號不可為空" });
        } else if (!member_name) {
            return res.status(400).json({ success: false, error: "姓名不可為空" });
        } else if (!member_mail) {
            return res.status(400).json({ success: false, error: "信箱不可為空" });
        } else if (!password) {
            return res.status(400).json({ success: false, error: "密碼不可為空" });
        } else if (account.length > 10 || account.length < 9) {
            return res.status(400).json({ success: false, error: "請確認電話號碼。" });
        } else if (!emailPattern.test(member_mail)) {
            return res.status(400).json({ success: false, error: "請確認電子信箱。" });
        }
        const checkAccount = await model.checkMemberExist(account);
        if (checkAccount) {
            return res.status(409).json({ success: false, error: "該帳號已存在。" });
        }
        const member = await model.postMember(account, member_name, member_mail, password);
        if (!member) {
            return res.status(409).json({ success: false, error: "無法創建帳號。" });
        }
        return res.json({ success: true, member });
    } catch (err) {
        res.status(500).json({ success: false, error: "伺服器錯誤，無法創建帳號。" });
    }
};
// 發送驗證信
exports.postUserForgotEmail = async (req, res) => {
    try {
        const { member_mail } = req.body;
        if (!member_mail) {
            return res.status(400).json({ success: false, error: "信箱不可為空" });
        } else if (!emailPattern.test(member_mail)) {
            return res.status(400).json({ success: false, error: "請確認電子信箱。" });
        }
        const token = await model.postMemberViaMail(member_mail);
        if (!token) {
            return res.status(400).json({ success: false, error: "找不到該帳號。" });
        }
        // 設置郵件內容
        const transporter = nodemailer.createTransport({
            service: "Gmail", // 使用 Gmail 作為發信服務
            auth: {
                user: process.env.EMAIL_USER, // 發信帳號
                pass: process.env.EMAIL_PASSWORD, // 發信密碼
            },
        });
        const mailContent = {
            from: process.env.EMAIL_USER,
            to: member_mail,
            subject: "條條框框",
            text: `驗證碼：${token}`,
        };
        // 發送郵件
        transporter.sendMail(mailContent, (error, info) => {
            if (error) {
                console.error("郵件發送失敗:", error);
                return res.status(500).json({ success: false, message: "郵件發送失敗" });
            }
            // 可在這裡儲存 token 及其到期時間至數據庫
            return res.status(200).json({ success: true, member: "驗證信已發送" });
        });
    } catch (err) {
        console.error("伺服器錯誤:", err);
        return res.status(500).json({ success: false, error: err });
    }
};
// 檢查驗證碼
exports.postUserForgotToken = async (req, res) => {
    const { member_mail, token } = req.body;
    try {
        if (!token) {
            return res.status(400).json({ success: false, error: "驗證碼不可為空" });
        } else if (!member_mail) {
            return res.status(400).json({ success: false, error: "信箱不可為空" });
        }
        const checkToken = await model.postMemberViaToken(member_mail, token);
        if (checkToken == false) {
            return res.status(400).json({ success: false, error: "信箱錯誤。" });
        } else if (!checkToken) {
            return res.status(400).json({ success: false, error: "驗證碼錯誤。" });
        }
        return res.status(200).json({ success: true, member: "驗證成功" });
    } catch (err) {
        console.error("伺服器錯誤:", err);
        return res.status(500).json({ success: false, error: err });
    }
};
// 修改密碼
exports.patchUserForgotPassword = async (req, res) => {
    const { member_mail, password } = req.body;
    try {
        if (!member_mail || !password) {
            return res.status(400).json({ success: false, error: "帳號/密碼不可為空" });
        }
        const patchPassword = await model.patchPassword(member_mail, password);
        if (patchPassword == "emailError") {
            return res.status(400).json({ success: false, error: "該信箱不存在。" });
        }
        if (!patchPassword.success) {
            return res.status(500).json({ success: false, error: "重設密碼錯誤。" });
        }
        return res.status(200).json({ success: true, member: "密碼已成功更新。" });
    } catch (err) {
        console.error("伺服器錯誤:", err);
        return res.status(500).json({ success: false, error: err });
    }
};
// --------------------------------------------------------------------------------------------------------------------------------------------------
// 獲取所有承包商
exports.getContract = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const contract = await model.findAllContract();
        return res.json({ success: true, contract });
    } catch (err) {
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法獲取承包商列表。" });
    }
};
// 新增承包商
exports.postContract = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { contract_name, contract_phone, contract_address, contract_mail, contract_business_number } = req.body;
        if (!contract_name) {
            return res.status(400).json({ success: false, error: "姓名不可為空。" });
        } else if (!contract_phone) {
            return res.status(400).json({ success: false, error: "電話不可為空。" });
        } else if (contract_phone.length > 10 || contract_phone.length < 9) {
            return res.status(400).json({ success: false, error: "請檢查電話號碼。" });
        } else if (contract_mail && !emailPattern.test(contract_mail)) {
            return res.status(400).json({ success: false, error: "請檢查電子信箱。" });
        }
        const contract = await model.createContract(
            contract_name,
            contract_phone,
            contract_address,
            contract_mail,
            contract_business_number
        );
        if (!contract) {
            return res.status(400).json({ success: false, error: "無法創建承包商。" });
        }
        return res.json({ success: true, contract });
    } catch (err) {
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法創建承包商。" });
    }
};
// 根據id獲取承包商資訊
exports.getContractContractId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { contract_id } = req.params;
        const contract = await model.getContractViaContractId(contract_id);
        if (!contract) {
            return res.status(400).json({ success: false, error: "找不到指定的承包商。" });
        } else if (contract == "contractError") {
            return res.status(400).json({ success: false, error: "更新承包商記錄失敗" });
        }
        res.json({ success: true, contract });
    } catch (err) {
        res.status(500).json({ success: false, error: "伺服器錯誤，無法獲取指定的承包商資訊。" });
    }
};
// 根據id更新承包商資訊
exports.putContractContractId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { contract_id } = req.params;
        const { contract_name, contract_phone, contract_address, contract_mail, contract_business_number } = req.body;
        if (!contract_name) {
            return res.status(400).json({ success: false, error: "姓名不可為空。" });
        } else if (!contract_phone) {
            return res.status(400).json({ success: false, error: "電話不可為空。" });
        } else if (contract_phone.length > 10 || contract_phone.length < 9) {
            return res.status(400).json({ success: false, error: "請檢查電話號碼。" });
        } else if (contract_mail && !emailPattern.test(contract_mail)) {
            return res.status(400).json({ success: false, error: "請檢查電子信箱。" });
        }
        const contract = await model.updateContract(
            contract_id,
            contract_name,
            contract_phone,
            contract_address,
            contract_mail,
            contract_business_number
        );
        if (!contract) {
            return res.status(400).json({ success: false, error: "無法更新承包商資訊。" });
        }
        return res.json({ success: true, contract });
    } catch (err) {
        res.status(500).json({ success: false, error: "伺服器錯誤，無法更新承包商資訊。" });
    }
};
// 根據id刪除承包商
exports.deleteContractContractId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { contract_id } = req.params;
        const checkContractExist = await model.checkContractViaContractId(contract_id);
        if (!checkContractExist) {
            return res.status(404).json({ success: false, error: "該承包商不存在。" });
        }
        const checkPersonalContract = await model.getContractViaContractId(contract_id);
        if (checkPersonalContract.contract_name == "個體戶") {
            return res.status(400).json({ success: false, error: "不可刪除個體戶。" });
        }
        const checkContainClient = await model.getAllClientViaContractName(checkPersonalContract.contract_name);
        if (checkContainClient) {
            for (let client of checkContainClient) {
                await model.putClientContractNameAsPersonal(client.client_id);
            }
        }
        const contract = await model.deleteContract(contract_id);
        if (!contract) {
            return res.status(400).json({ success: false, error: "無法刪除承包商。" });
        }
        res.json({ success: true, contract: "承包商刪除成功！" });
    } catch (err) {
        res.status(500).json({ success: false, error: "伺服器錯誤，無法刪除承包商。" });
        console.error(err);
    }
};
// --------------------------------------------------------------------------------------------------------------------------------------------------
exports.getClient = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const client = await model.getAllClient();
        return res.status(200).json({ success: true, client });
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法獲取指定的客戶資訊。",
        });
    }
};
exports.postClient = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address, client_name, client_phone, client_mail, business_number, contract_name } = req.body;
        if (!client_name) {
            return res.status(400).json({ success: false, error: "姓名不可為空。" });
        } else if (!client_address) {
            return res.status(400).json({ success: false, error: "地址不可為空。" });
        } else if (client_phone && (client_phone.length > 10 || client_phone < 9)) {
            return res.status(400).json({ success: false, error: "請檢查電話號碼。" });
        } else if (client_mail && !emailPattern.test(client_mail)) {
            return res.status(400).json({ success: false, error: "請檢查電子信箱。" });
        } else if (contract_name == "") {
            return res.status(400).json({ success: false, error: "承包商不可為空。" });
        }
        const client = await model.postNewClient(
            client_address,
            client_name,
            client_phone,
            client_mail,
            business_number,
            contract_name
        );
        if (!client) {
            return res.status(400).json({ success: false, error: "無法新增客戶。" });
        } else {
            return res.status(200).json({ success: true, client });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法新增客戶資訊。",
        });
    }
};
exports.postClientClientAddress = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address } = req.body;
        if (!client_address) {
            return res.status(400).json({ success: false, error: "地址不可為空。" });
        }
        const client = await model.getClientIdViaClientAddress(client_address);
        if (!client) {
            return res.status(400).json({ success: false, error: "無法取得客戶資料。" });
        } else {
            return res.status(200).json({ success: true, client });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法新增客戶資訊。",
        });
    }
};
exports.getClientClientid = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_id } = req.params;
        const client = await model.getClientId(client_id);
        if (!client) {
            return res.status(400).json({ success: false, error: "無此客戶資料。" });
        } else {
            return res.status(200).json({ success: true, client });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法取得客戶資訊。",
        });
    }
};
exports.putClientClientid = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address, client_name, client_phone, client_mail, business_number, contract_name } = req.body;
        if (!client_address) {
            return res.status(400).json({ success: false, error: "地址不可為空。" });
        } else if (!client_name) {
            return res.status(400).json({ success: false, error: "姓名不可為空。" });
        } else if (client_phone && (client_phone.length > 10 || client_phone < 9)) {
            return res.status(400).json({ success: false, error: "請檢查電話號碼。" });
        } else if (client_mail && !emailPattern.test(client_mail)) {
            return res.status(400).json({ success: false, error: "請檢查電子信箱。" });
        }
        const { client_id } = req.params;
        const client = await model.putClientId(
            client_id,
            client_address,
            client_name,
            client_phone,
            client_mail,
            business_number,
            contract_name
        );
        if (!client) {
            return res.status(400).json({ success: false, error: "無法更新客戶資料。" });
        } else {
            return res.status(200).json({ success: true, client });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法更新客戶資訊。" });
    }
};
exports.deleteClientClientid = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_id } = req.params;
        const client = await model.deleteClientId(client_id);
        if (!client) {
            return res.status(400).json({ success: false, error: "無法刪除客戶資料。" });
        } else {
            return res.status(200).json({ success: true, client });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法刪除客戶資訊。" });
    }
};
// --------------------------------------------------------------------------------------------------------------------------------------------------
exports.getMeasure = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const measure = await model.getAllMeasure();
        return res.status(200).json({ success: true, measure });
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法獲加工資訊。",
        });
    }
};
exports.postMeasure = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address } = req.body;
        const measure = await model.getAllMeasureViaClientAddress(client_address);
        return res.status(200).json({ success: true, measure });
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法獲取指定的數據。" });
    }
};
exports.postMeasureNewMeasure = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address, length, width, specification, measure_date, position, quantity } = req.body;
        if (!client_address) {
            return res.status(400).json({ success: false, error: "地址不可為空。" });
        } else if (!length) {
            return res.status(400).json({ success: false, error: "長度不可為空。" });
        } else if (!width) {
            return res.status(400).json({ success: false, error: "寬度不可為空。" });
        } else if (!specification) {
            return res.status(400).json({ success: false, error: "種類不可為空。" });
        } else if (!measure_date) {
            return res.status(400).json({ success: false, error: "測量日期不可為空。" });
        } else if (!position) {
            return res.status(400).json({ success: false, error: "位置不可為空。" });
        } else if (!quantity) {
            return res.status(400).json({ success: false, error: "數量不可為空。" });
        }
        const measure = await model.postNewMeasure(
            client_address,
            length,
            width,
            specification,
            measure_date,
            position,
            quantity
        );
        if (!measure) {
            return res.status(400).json({ success: false, error: "無法新增數據。" });
        } else {
            return res.status(200).json({ success: true, measure });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法新增數據。",
        });
    }
};
exports.getMeasureMeasureId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { measure_id } = req.params;
        const measure = await model.getMeasureId(measure_id);
        if (!measure) {
            return res.status(400).json({ success: false, error: "無此數據。" });
        } else {
            return res.status(200).json({ success: true, measure });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法取得數據。",
        });
    }
};
exports.putMeasureMeasureId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address, length, width, specification, measure_date, position, quantity } = req.body;
        if (!client_address) {
            return res.status(400).json({ success: false, error: "地址不可為空。" });
        } else if (!length) {
            return res.status(400).json({ success: false, error: "長度不可為空。" });
        } else if (!width) {
            return res.status(400).json({ success: false, error: "寬度不可為空。" });
        } else if (!specification) {
            return res.status(400).json({ success: false, error: "種類不可為空。" });
        } else if (!measure_date) {
            return res.status(400).json({ success: false, error: "測量日期不可為空。" });
        } else if (!position) {
            return res.status(400).json({ success: false, error: "位置不可為空。" });
        } else if (!quantity) {
            return res.status(400).json({ success: false, error: "數量不可為空。" });
        }
        const { measure_id } = req.params;
        const measure = await model.putMeasureId(
            measure_id,
            client_address,
            length,
            width,
            specification,
            measure_date,
            position,
            quantity
        );
        if (!measure) {
            return res.status(400).json({ success: false, error: "無法更新數據。" });
        } else {
            return res.status(200).json({ success: true, measure });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法更新數據。" });
    }
};
exports.deleteMeasureMeasureId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { measure_id } = req.params;
        const measure = await model.deleteMeasureId(measure_id);
        if (!measure) {
            return res.status(400).json({ success: false, error: "無法刪除數據。" });
        } else {
            return res.status(200).json({ success: true, measure });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法刪除數據。" });
    }
};
// --------------------------------------------------------------------------------------------------------------------------------------------------
exports.downloadQuotation = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const fileDir = path.join(__dirname, "../template");
        // 如果 template 資料夾不存在則建立
        if (!fs.existsSync(fileDir)) {
            fs.mkdirSync(fileDir, { recursive: true });
        }
        const { measure_id } = req.body;
        if (measure_id.length == 0) {
            return res.status(400).json({ success: false, error: "請選擇數據" });
        }
        const [client] = await model.getClientDataViaMeasureId(measure_id[0]);
        const uniqueFileName = `${formatDate(Date.now())}_${client.client_name}_報價單.xlsx`; //建立檔名
        const filePath = path.join(fileDir, uniqueFileName); //檔案路徑

        // 建立新的 Excel 工作簿，並從範本文件中載入
        const workbook = new ExcelJS.Workbook();
        const templateFilePath = path.join(__dirname, "../template/報價單.xlsx"); // 你的範本檔案路徑
        await workbook.xlsx.readFile(templateFilePath); // 讀取範本文件
        const worksheet = workbook.worksheets[0]; // 獲取第一個工作表

        // 填入資料到指定的儲存格
        worksheet.getCell("B4").value = `${client.client_name}`;
        worksheet.getCell("B6").value = `${client.client_address}`;
        worksheet.getCell("D2").value = `${formatTaiwanDate(Date.now())}`;

        for (let i = 8; i < 8 + measure_id.length; i++) {
            const data = await model.getQuotationData(measure_id[`${i - 8}`]);
            worksheet.getCell(`A${i}`).value = `${data.description}`;
            worksheet.getCell(`B${i}`).value = data.quantity;
            worksheet.getCell(`C${i}`).value = data.cost;
            worksheet.getCell(`D${i}`).value = data.cost * data.quantity;
        }

        // 寫入 Excel 文件
        await workbook.xlsx.writeFile(filePath);
        // 將 Excel 寫入 Buffer，並將 Buffer 儲存到資料庫
        const buffer = await workbook.xlsx.writeBuffer();
        await model.saveExportedFile("報價單", buffer, client.client_address);

        // 回傳檔案下載
        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", `attachment; filename*=UTF-8''${encodeURIComponent(uniqueFileName)}`);
        return res.download(filePath, uniqueFileName, (err) => {
            if (err) {
                console.error("下載失敗:", err);
                return res.status(500).json({ success: false, error: "無法下載報價單。" });
            }
            // （選用）下載完成後刪除檔案
            fs.unlink(filePath, (unlinkErr) => {
                if (unlinkErr) {
                    console.error("無法刪除暫存檔案:", unlinkErr);
                }
            });
        });
    } catch (err) {
        console.error("發生錯誤：", err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法生成報價單。" });
    }
};
exports.downloadMachine = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken === "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }

        const fileDir = path.join(__dirname, "../template");
        if (!fs.existsSync(fileDir)) {
            fs.mkdirSync(fileDir, { recursive: true });
        }

        const { machine_id } = req.body;
        if (machine_id.length === 0) {
            return res.status(400).json({ success: false, error: "請選擇數據" });
        }

        const [machine] = await model.getMachineViaMachineId(machine_id[0]);
        const [client] = await model.getClientDataViaMeasureId(machine["measure_id"]);
        const uniqueFileName = `${formatDate(Date.now())}_${client.client_name}_加工單.xlsx`;
        const filePath = path.join(fileDir, uniqueFileName);

        const workbook = new ExcelJS.Workbook();
        const templateFilePath = path.join(__dirname, "../template/加工單.xlsx");
        await workbook.xlsx.readFile(templateFilePath);

        const fieldMappings = [
            { key: "bottom_flower_board_height", label: "下花板高" },
            { key: "bottom_flower_board_width", label: "下花板寬" },
            { key: "bottom_line_1", label: "下台畫線" },
            { key: "bottom_line_2", label: "下台畫線2" },
            { key: "door_height", label: "門框高" },
            { key: "door_piece_height", label: "門片高" },
            { key: "door_piece_width", label: "門片橫料" },
            { key: "door_width", label: "門框橫料" },
            { key: "exter_branch", label: "外片站支" },
            { key: "flower_board_height", label: "花板高" },
            { key: "flower_board_width", label: "花板寬" },
            { key: "flower_grid_height", label: "花格料高" },
            { key: "flower_grid_width", label: "花格料寬" },
            { key: "flower_height", label: "門花高" },
            { key: "flower_net_height", label: "花格網高" },
            { key: "flower_net_width", label: "花格網寬" },
            { key: "flower_width", label: "門花寬" },
            { key: "glass_height", label: "玻璃高度" },
            { key: "glass_second_height", label: "門下玻璃高" },
            { key: "glass_second_width", label: "三拉第二玻璃寬度/門下玻璃寬" },
            { key: "glass_width", label: "玻璃寬度" },
            { key: "inner_line", label: "內片大勾" },
            { key: "inter_branch", label: "內片站支" },
            { key: "leaf_height", label: "百葉高" },
            { key: "leaf_number", label: "百葉數量" },
            { key: "leaf_width", label: "百葉寬" },
            { key: "left_line", label: "內左量起畫線" },
            { key: "mid_flower_net_height", label: "中花格網高" },
            { key: "mid_flower_net_thing_height", label: "中花格網料高" },
            { key: "out_window_hori", label: "外片窗戶橫料" },
            { key: "screen_window_branch", label: "紗窗站支" },
            { key: "screen_window_hori", label: "紗窗橫料" },
            { key: "screen_window_mid", label: "紗窗中腰" },
            { key: "top_flower_net_height", label: "上花格網高" },
            { key: "top_flower_net_thing_height", label: "上花格網料高" },
            { key: "top_flower_net_thing_width", label: "上花格網料寬" },
            { key: "top_flower_net_width", label: "上花格網寬" },
            { key: "window_hori", label: "窗戶橫料" },
        ];

        for (let i = 0; i < machine_id.length; i++) {
            let worksheet;
            const templateWorksheet = workbook.getWorksheet(1);

            if (i === 0) {
                // 對於第一筆資料，直接使用已讀取的模板工作表
                worksheet = templateWorksheet;
                worksheet.name = `加工單_${i + 1}`; // 重命名第一個工作表為 加工單_1
            } else {
                // 為其他資料創建新的工作表
                worksheet = workbook.addWorksheet(`加工單_${i + 1}`);

                // 複製模板的合併單元格設置
                templateWorksheet.model.merges.forEach((merge) => {
                    worksheet.mergeCells(merge);
                });

                // 複製模板的欄寬設置
                templateWorksheet.columns.forEach((col, index) => {
                    const newCol = worksheet.getColumn(index + 1);
                    newCol.width = col.width;
                });

                // 複製每個單元格的內容、樣式和框線
                templateWorksheet.eachRow({ includeEmpty: true }, (row, rowNumber) => {
                    // 只複製前 20 行
                    if (rowNumber > 20) return;

                    const newRow = worksheet.getRow(rowNumber);
                    row.eachCell({ includeEmpty: true }, (cell, colNumber) => {
                        // 只複製前 4 列 (A 到 D)
                        if (colNumber > 4) return;

                        const newCell = newRow.getCell(colNumber);
                        newCell.value = cell.value;
                        newCell.style = { ...cell.style };

                        // 複製框線樣式
                        if (cell.border) {
                            newCell.border = { ...cell.border };
                        }
                    });
                    newRow.commit();
                });
                for (let i = 8; i < 21; i++) {
                    worksheet.getCell(`A${i}`).value = "";
                    worksheet.getCell(`B${i}`).value = "";
                    worksheet.getCell(`C${i}`).value = "";
                    worksheet.getCell(`D${i}`).value = "";
                }
            }

            const [machine] = await model.getMachineViaMachineId(machine_id[i]);
            const measure = await model.getMeasureId(machine["measure_id"]);

            worksheet.getCell("B4").value = `${client.client_name}`;
            worksheet.getCell("B5").value = `${client.client_address}`;
            worksheet.getCell("B1").value = `${formatTaiwanDate(Date.now())}`;
            if (measure.specification.windoor === "窗") {
                worksheet.getCell(
                    "B6"
                ).value = `${measure.specification.type.piece} ${measure.specification.type.option} ${measure.width}×${measure.length}`;
            } else {
                worksheet.getCell(
                    "B6"
                ).value = `${measure.specification.type.piece} ${measure.specification.type.frame} ${measure.width}×${measure.length}`;
            }

            let breakLine = false;
            let row = 8;
            const [data] = await model.getMachineViaMachineId(machine_id[i]);

            fieldMappings.forEach(({ key, label }) => {
                if (data[key] !== null && data[key] !== undefined && data[key] !== 0 && data[key] !== "0.00") {
                    if (!breakLine) {
                        worksheet.getCell(`A${row}`).value = label;
                        worksheet.getCell(`B${row}`).value = data[key];
                        breakLine = true;
                    } else {
                        worksheet.getCell(`C${row}`).value = label;
                        worksheet.getCell(`D${row}`).value = data[key];
                        row += 1;
                        breakLine = false;
                    }
                }
            });
        }

        await workbook.xlsx.writeFile(filePath);
        // 將 Excel 寫入 Buffer，並將 Buffer 儲存到資料庫
        const buffer = await workbook.xlsx.writeBuffer();
        await model.saveExportedFile("加工單", buffer, client.client_address);

        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", `attachment; filename*=UTF-8''${encodeURIComponent(uniqueFileName)}`);
        return res.download(filePath, uniqueFileName, (err) => {
            if (err) {
                console.error("下載失敗:", err);
                return res.status(500).json({ success: false, error: "無法下載加工單。" });
            }
            fs.unlink(filePath, (unlinkErr) => {
                if (unlinkErr) {
                    console.error("無法刪除暫存檔案:", unlinkErr);
                }
            });
        });
    } catch (err) {
        console.error("發生錯誤：", err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法生成加工單。" });
    }
};
exports.downloadMaterial = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken === "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }

        const fileDir = path.join(__dirname, "../template");
        if (!fs.existsSync(fileDir)) {
            fs.mkdirSync(fileDir, { recursive: true });
        }

        const { machine_id } = req.body;
        if (machine_id.length === 0) {
            return res.status(400).json({ success: false, error: "請選擇數據" });
        }

        const [machine] = await model.getMachineViaMachineId(machine_id[0]);
        const [client] = await model.getClientDataViaMeasureId(machine["measure_id"]);
        const uniqueFileName = `${formatDate(Date.now())}_${client.client_name}_叫料單.xlsx`;
        const filePath = path.join(fileDir, uniqueFileName);

        const workbook = new ExcelJS.Workbook();
        const templateFilePath = path.join(__dirname, "../template/叫料單.xlsx");
        await workbook.xlsx.readFile(templateFilePath);

        let worksheet;
        const templateWorksheet = workbook.getWorksheet(1);
        worksheet = templateWorksheet;
        worksheet.name = `叫料單`;
        worksheet.getCell("C4").value = `${client.client_name}`;
        worksheet.getCell("D2").value = `${formatTaiwanDate(Date.now())}`;

        let data = {};
        for (let i = 0; i < machine_id.length; i++) {
            const [machine] = await model.getMachineViaMachineId(machine_id[i]);
            const measure = await model.getMeasureId(machine["measure_id"]);
            switch (measure.specification.type.piece) {
                case "2拉": {
                    switch (measure.specification.type.option) {
                        case "866": {
                            data["8601(866 上橫)"] = (data["8601(866 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["8602(866 下台)"] = (data["8602(866 下台)"] + 1 || 0) + (measure.width - 4);
                            data["8603(866 站支)"] = (data["8603(866 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 3;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"];
                            data["1656A(1066 大勾)"] = (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"];
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"];
                            data["8655(1066 邊支)"] =
                                (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] + machine["exter_branch"];
                            data["8621(866 紗橫)"] =
                                (data["8621(866 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 2;
                            data["8622(866 紗站)"] =
                                (data["8622(866 紗站)"] + 1 || 0) + machine["screen_window_branch"] * 2;
                            break;
                        }
                        case "866水泥窗": {
                            data["8601(866 上橫)"] = (data["8601(866 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["8602(866 下台)"] = (data["8602(866 下台)"] + 1 || 0) + (measure.width - 4);
                            data["8603(866 站支)"] = (data["8603(866 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 3;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"];
                            data["1656A(1066 大勾)"] = (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"];
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"];
                            data["8655(1066 邊支)"] =
                                (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] + machine["exter_branch"];
                            data["4201(1200型紗窗)"] =
                                (data["4201(1200型紗窗)"] || 0) +
                                machine["screen_window_hori"] * 2 +
                                machine["screen_window_branch"] * 2;

                            break;
                        }
                        case "1066": {
                            data["1601(1066 上橫)"] = (data["1601(1066 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["1602(1066 下台)"] = (data["1602(1066 下台)"] + 1 || 0) + (measure.width - 4);
                            data["1603(1066 站支)"] = (data["1603(1066 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 3;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"];
                            data["1656A(1066 大勾)"] = (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"];
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"];
                            data["8655(1066 邊支)"] =
                                (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] + machine["exter_branch"];
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 2;
                            data["1622紗站有刷"] = (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"];
                            data["1622紗站無刷"] = (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"];
                            break;
                        }
                        case "1068": {
                            data["10601(1068 上橫)"] = (data["10601(1068 上橫)"] + 1 || 0) + (measure.width - 5);
                            data["10604A"] = (data["10604A"] + 1 || 0) + (measure.width - 5);
                            data["1604A(1068 中領)"] = (data["1604A(1068 中領)"] + 1 || 0) + measure.length;
                            data["1604B(1068 偏領)"] = (data["1604B(1068 偏領)"] + 1 || 0) + measure.length;
                            data["1851(1068 上橫 6)"] =
                                (data["1851(1068 上橫 6)"] + 1 || 0) + machine["window_hori"] * 2;
                            data["1862(1068 下橫 6.2)"] =
                                (data["1862(1068 下橫 6.2)"] + 1 || 0) + machine["window_hori"];
                            data["1852(1068 下橫 8.8)"] =
                                (data["1852(1068 下橫 8.8)"] + 1 || 0) + machine["window_hori"];
                            data["10664(1068 大勾)"] = (data["10664(1068 大勾)"] + 1 || 0) + machine["inter_branch"];
                            data["10665(1068 大碰)"] = (data["10665(1068 大碰)"] + 1 || 0) + machine["inter_branch"];
                            data["10654(1068 小勾)"] = (data["10654(1068 小勾)"] + 1 || 0) + machine["exter_branch"];
                            data["1653A(1068 邊支)"] = (data["1653A(1068 邊支)"] + 1 || 0) + machine["exter_branch"];
                            data["8655(1066 邊支)"] =
                                (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] + machine["exter_branch"];
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 2;
                            data["1622紗站有刷"] = (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"];
                            data["1622紗站無刷"] = (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"];

                            break;
                        }
                        case "1066-866流理台反裝": {
                            data["1601(1066 上橫)"] = (data["1601(1066 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["1602(1066 下台)"] = (data["1602(1066 下台)"] + 1 || 0) + (measure.width - 4);
                            data["1603(1066 站支)"] = (data["1603(1066 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 3;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"];
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["inter_branch"];
                            data["1656A(1066 大勾)"] = (data["1656A(1066 大勾)"] + 1 || 0) + machine["exter_branch"];
                            data["8655(1066 邊支)"] =
                                (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] + machine["exter_branch"];

                            break;
                        }
                        case "1066加1200型鎖": {
                            data["1601(1066 上橫)"] = (data["1601(1066 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["1602(1066 下台)"] = (data["1602(1066 下台)"] + 1 || 0) + (measure.width - 4);
                            data["1603(1066 站支)"] = (data["1603(1066 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 3;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"];
                            data["1656A(1066 大勾)"] = (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"];
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"];
                            data["8655(1066 邊支)"] =
                                (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] + machine["exter_branch"];
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 2;
                            data["1622紗站有刷"] = (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"];
                            data["1622紗站無刷"] = (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"];

                            break;
                        }
                    }
                    break;
                }
                case "3拉": {
                    switch (measure.specification.type.option) {
                        case "866": {
                            data["8601(866 上橫)"] = (data["8601(866 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["8602(866 下台)"] = (data["8602(866 下台)"] + 1 || 0) + (measure.width - 4);
                            data["8603(866 站支)"] = (data["8603(866 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) +
                                machine["window_hori"] * 4 +
                                machine["out_window_hori"];
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["out_window_hori"];
                            data["1656A(1066 大勾)"] =
                                (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1655A(1066 大碰)"] =
                                (data["1655A(1066 大碰)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["8621(866 紗橫)"] =
                                (data["8621(866 紗橫)"] + 1 || 0) + machine["screen_window_branch"] * 4;
                            data["8622(866 紗站)"] =
                                (data["8622(866 紗站)"] + 1 || 0) + machine["screen_window_branch"] * 4;
                            break;
                        }
                        case "1066": {
                            data["1601(1066 上橫)"] = (data["1601(1066 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["1602(1066 下台)"] = (data["1602(1066 下台)"] + 1 || 0) + (measure.width - 4);
                            data["1603(1066 站支)"] = (data["1603(1066 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) +
                                machine["window_hori"] * 4 +
                                machine["out_window_hori"];
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["out_window_hori"];
                            data["1656A(1066 大勾)"] =
                                (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1655A(1066 大碰)"] =
                                (data["1655A(1066 大碰)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["8655(1066 邊支)"] = (data["8655(1066 邊支)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 4;
                            data["1622紗站有刷"] =
                                (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"] * 2;
                            data["1622紗站無刷"] =
                                (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"] * 2;
                            break;
                        }
                        case "1068": {
                            data["10601(1068 上橫)"] = (data["10601(1068 上橫)"] + 1 || 0) + (measure.width - 5);
                            data["10604A"] = (data["10604A"] + 1 || 0) + (measure.width - 5);
                            data["1604B(1068 偏領)"] = (data["1604B(1068 偏領)"] + 1 || 0) + measure.length * 2;
                            data["1851(1068 上橫 6)"] =
                                (data["1851(1068 上橫 6)"] + 1 || 0) +
                                machine["window_hori"] * 2 +
                                machine["out_window_hori"];
                            data["1852(1068 下橫 8.8)"] =
                                (data["1852(1068 下橫 8.8)"] + 1 || 0) + machine["out_window_hori"];
                            data["1862(1068 下橫 6.2)"] =
                                (data["1862(1068 下橫 6.2)"] + 1 || 0) + machine["window_hori"] * 2;
                            data["10664(1068 大勾)"] =
                                (data["10664(1068 大勾)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["10665(1068 大碰)"] =
                                (data["10665(1068 大碰)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["10654(1068 小勾)"] =
                                (data["10654(1068 小勾)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 2;
                            data["1622紗站有刷"] =
                                (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"] * 2;
                            data["1622紗站無刷"] =
                                (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"] * 2;

                            break;
                        }
                    }
                    break;
                }
                case "4拉": {
                    switch (measure.specification.type.option) {
                        case "866": {
                            data["8601(866 上橫)"] = (data["8601(866 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["8602(866 下台)"] = (data["8602(866 下台)"] + 1 || 0) + (measure.width - 4);
                            data["8603(866 站支)"] = (data["8603(866 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 6;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"] * 2;
                            data["1656A(1066 大勾)"] =
                                (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"] * 4;
                            data["1655A(1066 大碰)"] =
                                (data["1655A(1066 大碰)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["8655(1066 邊支)"] = (data["8655(1066 邊支)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["8621(866 紗橫)"] =
                                (data["8621(866 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 4;
                            data["8622(866 紗站)"] =
                                (data["8622(866 紗站)"] + 1 || 0) + machine["screen_window_branch"] * 4;
                            break;
                        }
                        case "1066": {
                            data["1601(1066 上橫)"] = (data["1601(1066 上橫)"] + 1 || 0) + (measure.width - 4);
                            data["1602(1066 下台)"] = (data["1602(1066 下台)"] + 1 || 0) + (measure.width - 4);
                            data["1603(1066 站支)"] = (data["1603(1066 站支)"] + 1 || 0) + measure.length * 2;
                            data["1652(1066 6.5橫料)"] =
                                (data["1652(1066 6.5橫料)"] + 1 || 0) + machine["window_hori"] * 6;
                            data["1652A(1066 9.2橫料)"] =
                                (data["1652A(1066 9.2橫料)"] + 1 || 0) + machine["window_hori"] * 2;
                            data["1656A(1066 大勾)"] =
                                (data["1656A(1066 大勾)"] + 1 || 0) + machine["inter_branch"] * 4;
                            data["1655A(1066 大碰)"] =
                                (data["1655A(1066 大碰)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["1656(1066 小勾)"] = (data["1656(1066 小勾)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["8655(1066 邊支)"] = (data["8655(1066 邊支)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 4;
                            data["1622紗站有刷"] =
                                (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"] * 2;
                            data["1622紗站無刷"] =
                                (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"] * 2;

                            break;
                        }
                        case "1068": {
                            data["10601(1068 上橫)"] = (data["10601(1068 上橫)"] + 1 || 0) + (measure.width - 5);
                            data["10604A"] = (data["10604A"] + 1 || 0) + (measure.width - 5);
                            data["1604A(1068 中領)"] = (data["1604A(1068 中領)"] + 1 || 0) + measure.length * 2;
                            data["1851(1068 上橫 6)"] =
                                (data["1851(1068 上橫 6)"] + 1 || 0) + machine["window_hori"] * 4;
                            data["1862(1068 下橫 6.2)"] =
                                (data["1862(1068 下橫 6.2)"] + 1 || 0) + machine["window_hori"] * 2;
                            data["1852(1068 下橫 8.8)"] =
                                (data["1852(1068 下橫 8.8)"] + 1 || 0) + machine["window_hori"] * 2;
                            data["10664(1068 大勾)"] =
                                (data["10664(1068 大勾)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["10665(1068 大碰)"] =
                                (data["10665(1068 大碰)"] + 1 || 0) + machine["inter_branch"] * 2;
                            data["10654(1068 小勾)"] =
                                (data["10654(1068 小勾)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["1653A(1068 邊支)"] =
                                (data["1653A(1068 邊支)"] + 1 || 0) + machine["exter_branch"] * 2;
                            data["1621(1066 紗橫)"] =
                                (data["1621(1066 紗橫)"] + 1 || 0) + machine["screen_window_hori"] * 4;
                            data["1622紗站有刷"] =
                                (data["1622紗站有刷"] + 1 || 0) + machine["screen_window_branch"] * 2;
                            data["1622紗站無刷"] =
                                (data["1622紗站無刷"] + 1 || 0) + machine["screen_window_branch"] * 2;

                            break;
                        }
                    }
                    break;
                }
            }
        }
        let breakLine = false;
        let row = 6;
        for (let key in data) {
            if (data.hasOwnProperty(key)) {
                let value = data[key];
                let quantity600 = 0,
                    quantity640 = 0;
                [value, quantity600, quantity640] = calculateMaterialQuantity(value, quantity600, quantity640);
                if (quantity600 != 0) {
                    if (!breakLine) {
                        worksheet.getCell(`A${row}`).value = key;
                        worksheet.getCell(`B${row}`).value = "600";
                        worksheet.getCell(`C${row}`).value = quantity600;
                        breakLine = true;
                    } else {
                        worksheet.getCell(`D${row}`).value = key;
                        worksheet.getCell(`E${row}`).value = "600";
                        worksheet.getCell(`F${row}`).value = quantity600;
                        row += 1;
                        breakLine = false;
                    }
                }
                if (quantity640 != 0) {
                    if (!breakLine) {
                        worksheet.getCell(`A${row}`).value = key;
                        worksheet.getCell(`B${row}`).value = "640";
                        worksheet.getCell(`C${row}`).value = quantity640;
                        breakLine = true;
                    } else {
                        worksheet.getCell(`D${row}`).value = key;
                        worksheet.getCell(`E${row}`).value = "640";
                        worksheet.getCell(`F${row}`).value = quantity640;
                        row += 1;
                        breakLine = false;
                    }
                }
            }
        }

        await workbook.xlsx.writeFile(filePath);
        // 將 Excel 寫入 Buffer，並將 Buffer 儲存到資料庫
        const buffer = await workbook.xlsx.writeBuffer();
        await model.saveExportedFile("叫料單", buffer, client.client_address);

        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", `attachment; filename*=UTF-8''${encodeURIComponent(uniqueFileName)}`);
        return res.download(filePath, uniqueFileName, (err) => {
            if (err) {
                console.error("下載失敗:", err);
                return res.status(500).json({ success: false, error: "無法下載叫料單。" });
            }
            fs.unlink(filePath, (unlinkErr) => {
                if (unlinkErr) {
                    console.error("無法刪除暫存檔案:", unlinkErr);
                }
            });
        });
    } catch (err) {
        console.error("發生錯誤：", err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法生成叫料單。" });
    }
};
exports.downloadExported = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken === "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }

        const { export_id } = req.body;
        if (!export_id) {
            return res.status(400).json({ success: false, error: "請提供 export_id。" });
        }

        // 使用 getExportId 函數取得資料
        const [exportedData] = await model.getExportId(export_id);
        if (!exportedData) {
            return res.status(404).json({ success: false, error: "找不到對應的資料。" });
        }

        // 將 BLOB 資料轉換回 Excel 文件
        const buffer = Buffer.from(exportedData.data, "binary");
        const workbook = new ExcelJS.Workbook();
        await workbook.xlsx.load(buffer);

        // 取得 client 資料
        const client = await model.getClientIdViaClientAddress(exportedData.client_address);

        if (!client) {
            return res.status(404).json({ success: false, error: "找不到對應的客戶資料。" });
        }

        // 建立檔名
        const fileName = `${formatDate(exportedData.date)}_${client.client_name}_${exportedData.data_from}.xlsx`;
        const filePath = path.join(__dirname, "../template", fileName);

        // 將 Excel 寫入檔案
        await workbook.xlsx.writeFile(filePath);

        // 回傳檔案下載
        res.setHeader("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        res.setHeader("Content-Disposition", `attachment; filename*=UTF-8''${encodeURIComponent(fileName)}`);
        return res.download(filePath, fileName, (err) => {
            if (err) {
                console.error("下載失敗:", err);
                return res.status(500).json({ success: false, error: "無法下載檔案。" });
            }
            // 刪除暫存檔案
            fs.unlink(filePath, (unlinkErr) => {
                if (unlinkErr) {
                    console.error("無法刪除暫存檔案:", unlinkErr);
                }
            });
        });
    } catch (err) {
        console.error("發生錯誤：", err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法下載檔案。" });
    }
};

function formatDate(dateString) {
    return format(new Date(dateString), "yyyy-MM-dd");
}
function calculateMaterialQuantity(length, quantity600 = 0, quantity640 = 0) {
    while (length > 0) {
        if (length <= 600) {
            quantity600 += 1;
            length -= 600;
        } else if (length > 600 && length <= 640) {
            quantity640 += 1;
            length -= 640;
        } else {
            quantity640 += 1;
            length -= 640;
        }
    }
    return [length, quantity600, quantity640];
}
function formatTaiwanDate(date) {
    // 如果 date 不是 Date 物件，先將其轉換為 Date 物件
    if (!(date instanceof Date)) {
        date = new Date(date);
    }

    const year = date.getFullYear() - 1911; // 將西元年轉換成民國年
    const month = date.getMonth() + 1; // getMonth() 返回 0-11，所以要加 1
    const day = date.getDate();
    return `${year}年${month}月${day}日`;
}

// --------------------------------------------------------------------------------------------------------------------------------------------------
exports.getMachine = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const machine = await model.getAllMachine();
        return res.status(200).json({ success: true, machine });
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法獲加工資訊。",
        });
    }
};
exports.postMachine = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address } = req.body;
        const machine = await model.getAllMachineViaClientAddress(client_address);
        return res.status(200).json({ success: true, machine });
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法獲加工資訊。",
        });
    }
};
exports.postNewMachine = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const measure_id = req.body;
        if (measure_id.length == 0) {
            return res.status(400).json({ success: false, error: "請選擇數據" });
        }
        for (const id of measure_id) {
            const resp = await model.postMachineViaMeasure(id, token);
            if (!resp) {
                return res.status(400).json({ success: false, error: "無此數據資料。" });
            }
        }
        return res.status(200).json({ success: true, machine: "新增加工數據成功" });
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法建立加工資訊。",
        });
    }
};
exports.putMachineMachineId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const contents = req.body;
        const { machine_id } = req.params;
        const machine = await model.putMachineId(machine_id, contents);
        if (!machine) {
            return res.status(400).json({ success: false, error: "無法更新數據。" });
        } else {
            return res.status(200).json({ success: true, machine });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法更新數據。" });
    }
};
exports.deleteMachineMachineId = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { machine_id } = req.params;
        const machine = await model.deleteMachine(machine_id);
        if (!machine) {
            return res.status(400).json({ success: false, error: "無法刪除數據。" });
        } else {
            return res.status(200).json({ success: true, machine });
        }
    } catch (err) {
        console.error(err);
        return res.status(500).json({ success: false, error: "伺服器錯誤，無法刪除數據。" });
    }
};
// --------------------------------------------------------------------------------------------------------------------------------------------------
exports.postExported = async (req, res) => {
    try {
        const token = req.headers.authorization;
        const checkToken = await model.checkMemberLoginViaToken(token);
        if (!checkToken) {
            return res.status(401).json({ success: false, error: "請先登入。" });
        } else if (checkToken == "NA") {
            return res.status(403).json({ success: false, error: "帳號無效(未登入/無權限)。" });
        }
        const { client_address } = req.body;
        const exported = await model.getAllExportedViaClientAddress(client_address);
        return res.status(200).json({ success: true, exported });
    } catch (err) {
        console.error(err);
        return res.status(500).json({
            success: false,
            error: "伺服器錯誤，無法獲加工資訊。",
        });
    }
};
