const mysql = require("mysql2/promise");
require("dotenv").config();
const pool = mysql.createPool({
  host: process.env.DB_HOST, // 資料庫ip
  port: process.env.DB_PORT, // 資料庫port
  user: process.env.DB_USER, // 使⽤者名稱
  password: process.env.DB_PASSWORD, // 密碼
  database: process.env.DB_NAME, // 資料庫名稱
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0,
});
// 使⽤getConnection()嘗試從池中獲取⼀個連接
pool
  .getConnection()
  .then((conn) => {
    console.log("MySQL連接成功！");
    conn.release(); // 釋放連接
  })
  .catch((err) => {
    // 如果無法獲取連接，可能是連接訊息錯誤或資料庫尚未啟動
    console.error("無法連接到MySQL資料庫:", err);
  });
module.exports = pool;
