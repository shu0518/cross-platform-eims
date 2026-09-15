const { defineConfig } = require('@vue/cli-service')
module.exports = defineConfig({
  transpileDependencies: true,
  // 添加頁面配置
  pages: {
    index: {
      entry: 'src/main.js', // 入口文件
      template: 'public/index.html', // 模板文件
      filename: 'index.html', // 輸出的文件名
      title: '條條框框', // 修改這裡的網站標題
    }
  }
})
