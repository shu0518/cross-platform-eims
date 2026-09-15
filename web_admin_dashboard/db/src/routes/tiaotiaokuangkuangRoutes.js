const express = require("express");
const router = express.Router();
const controller = require("../controllers/tiaotiaokuangkuangController");

router.get("/user", controller.getUser); // 取得所有帳號
router.post("/user", controller.postUser); // 登入
router.delete("/user", controller.deleteUser); // 刪除帳號
router.get("/user/info", controller.getUserInfo); // 取得帳號資料
router.post("/user/info", controller.postUserInfo); // 取得帳號資料
router.put("/user/info", controller.putUserInfo); // 修改帳號資料
router.post("/user/signup", controller.postUserSignUp); // 註冊帳號
router.post("/user/forgot_mail", controller.postUserForgotEmail); // 傳送驗證信箱
router.post("/user/forgot_token", controller.postUserForgotToken); // 傳送驗證碼
router.patch("/user/forgot_password", controller.patchUserForgotPassword); // 修改密碼

router.get("/contract", controller.getContract);
router.post("/contract", controller.postContract);
router.get("/contract/:contract_id", controller.getContractContractId); // 根據id獲取承包商訊息
router.put("/contract/:contract_id", controller.putContractContractId); // 根據id更新承包商訊息
router.delete("/contract/:contract_id", controller.deleteContractContractId); // 根據id刪除承包商

router.get("/client", controller.getClient);
router.post("/client/newClient", controller.postClient);
router.post("/client", controller.postClientClientAddress);
router.get("/client/:client_id", controller.getClientClientid);
router.put("/client/:client_id", controller.putClientClientid);
router.delete("/client/:client_id", controller.deleteClientClientid);

router.get("/measure", controller.getMeasure);
router.post("/measure", controller.postMeasure);
router.post("/measure/newMeasure", controller.postMeasureNewMeasure);
router.get("/measure/measureId/:measure_id", controller.getMeasureMeasureId);
router.put("/measure/measureId/:measure_id", controller.putMeasureMeasureId);
router.delete("/measure/measureId/:measure_id", controller.deleteMeasureMeasureId);

router.post("/download/quotation", controller.downloadQuotation);
router.post("/download/machine", controller.downloadMachine);
router.post("/download/material", controller.downloadMaterial);
router.post("/download/exported", controller.downloadExported);

router.get("/machine", controller.getMachine);
router.post("/machine", controller.postMachine);
router.post("/machine/newMachine", controller.postNewMachine);
router.put("/machine/:machine_id", controller.putMachineMachineId);
router.delete("/machine/:machine_id", controller.deleteMachineMachineId);

router.post("/exported", controller.postExported);

module.exports = router;
