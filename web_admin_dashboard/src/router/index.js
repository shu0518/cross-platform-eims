import { createRouter, createWebHistory } from "vue-router";
import login from "../views/login.vue";
import content from "../views/content.vue";

const routes = [
    // 登入
    {
        path: "/",
        name: "login",
        component: login,
    },
    // 名單首頁
    {
        path: "/content",
        name: "content",
        component: content,
    },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;
