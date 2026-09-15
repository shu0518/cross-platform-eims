// app.js
// 引入Express模塊
const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const corsOptions = {
    exposedHeaders: ["Content-Disposition"], // 允許回應攜帶 Content-Disposition 標頭
};
const routes = require("./src/routes/tiaotiaokuangkuangRoutes");
require("dotenv").config();
// 創建⼀個Express應⽤。這是所有Express應⽤的起點。
const app = express();
// 定義⼀個端⼝號，⽤於服務器監聽。這個值可以是任何未被佔⽤的端⼝。
const port = process.env.PORT;

app.use(cors());
app.use(cors(corsOptions));
app.use(bodyParser.json());
// 啟動服務器，並監聽之前定義的端⼝（8000）。服務器啟動後，會執⾏回調函數中的代碼。
app.listen(port, () => {
  // 在控制台打印⼀條消息，表⽰服務器已經成功啟動。
  console.log(`App listening at http://localhost:${port}`);
});
const path = require("path");
app.use("/public", express.static(path.join(__dirname, "public")));

// 設置 API 路由
app.use("/api", routes); // 所有 API 路徑都會有前綴 "/api"