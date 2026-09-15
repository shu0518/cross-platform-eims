<template>
    <div class="container">
        <main class="main-content">
            <div class="action-bar">
                <div class="info-card">
                    <p class="info-text">　</p>
                </div>
                <div class="search-container">
                    <img src="../assets/ic_search1.png" class="search-icon" alt="搜尋圖示" />
                    <input type="text" class="search-bar" placeholder="搜尋" v-model="searchQuery" />
                    <a @click="clearSearch">×</a>
                </div>
            </div>
            <!-- 總加工列表 List渲染 -->
            <div class="table-container">
                <table class="contractor-table">
                    <thead>
                        <tr>
                            <th @click="toggleSort('machine_date')">加工日期
                                <i :class="sortDirection === 'asc' ? 'fa fa-arrow-up' : 'fa fa-arrow-down'"></i>
                            </th>
                            <th>
                                加工狀態
                                <select v-model="selectedStatus" class="custom-select">
                                    <option value="">全部</option>
                                    <option value="準備">準備</option>
                                    <option value="處理">處理</option>
                                    <option value="完成">完成</option>
                                </select>
                            </th>
                            <th>加工師傅</th>
                            <th>加工內容</th>
                            <th>詳細資訊</th>
                        </tr>
                    </thead>
                    <tbody>
                        <template v-if="filteredMachines && filteredMachines.length > 0">
                            <machineList v-for="machine in filteredMachines" :key="machine.machine_id"
                                :showCheckBox="false" :machine="machine" :members="memberData"
                                :measureData="measureData" @view-machine="openMachineId"
                                @emitTimeout="refreshMachineList" />
                        </template>
                        <tr v-else>
                            <td colspan="7" style="text-align: center; color: gray">無符合條件的結果</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </main>
    </div>
</template>

<script>
import { format } from "date-fns";
import { ref, computed, nextTick } from "vue";
import machineList from "./List/machineList.vue";
import { getMachine, getUser, getMeasure } from "../services/api";

export default {
    components: {
        machineList,
    },
    props: {
        client_address: String,
    },
    setup(props, { emit }) {
        // 取得客戶頁面設定
        const client_address = ref("");
        client_address.value = props.client_address;
        const machineData = ref([]);
        const memberData = []
        const measureData = []
        async function getAllMachineViaClientAddress() {
            await getMachine().then((resp) => {
                machineData.value = resp.machine.map((item) => {
                    return Object.fromEntries(Object.entries(item).filter(([_, value]) => value !== null));
                });
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        }
        async function getAllUser() {
            getUser().then((resp) => {
                memberData.splice(0, memberData.length, ...resp.member.map(({ account, member_name }) => ({ account, member_name })));
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            })
        }
        async function getAllMeasure() {
            getMeasure().then((resp) => {
                Object.assign(measureData, resp.measure)
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        }
        getAllMachineViaClientAddress()
        getAllUser()
        getAllMeasure()
        const searchQuery = ref("");
        function clearSearch() {
            searchQuery.value = null;
        }
        const sortField = ref("machine_date"); // 預設排序欄位
        const sortDirection = ref("asc"); // 預設排序方向
        const selectedStatus = ref("")

        const filteredMachines = computed(() => {
            // 如果搜尋欄是空的，返回全部資料
            let machines = machineData.value;

            // 搜尋邏輯
            if (searchQuery.value) {
                const query = searchQuery.value.trim().toLowerCase();
                machines = machines.filter((machine) => {
                    const matchesQuery = (field) => {
                        if (field === null || field === undefined) return false;
                        if (typeof field === "string") {
                            return field.toLowerCase().includes(query);
                        } else if (typeof field === "number") {
                            return field.toString().includes(query);
                        }
                        return false;
                    };
                    const matchesMemberName = () => {
                        const matchedMember = memberData.find(
                            (member) => member.member_name.toLowerCase().includes(query)
                        );
                        return matchedMember ? matchedMember.account === machine.account : false;
                    };
                    return (
                        matchesMemberName() ||
                        matchesQuery(machine.status) ||
                        matchesQuery(machine.machine_date) ||
                        matchesQuery(machine.remark)
                    );
                });
            }

            // 狀態篩選邏輯
            if (selectedStatus.value) {
                machines = machines.filter(machine => machine.status === selectedStatus.value);
            }

            // 自訂的排序邏輯
            const statusPriority = { "準備": 1, "處理": 2, "完成": 3 };

            return machines.slice().sort((a, b) => {
                const statusA = statusPriority[a.status] || 4;
                const statusB = statusPriority[b.status] || 4;

                if (statusA !== statusB) {
                    return statusA - statusB;
                }

                const aValue = a[sortField.value];
                const bValue = b[sortField.value];
                if (aValue < bValue) return sortDirection.value === "asc" ? -1 : 1;
                if (aValue > bValue) return sortDirection.value === "asc" ? 1 : -1;
                return 0;
            });
        });

        function toggleSort(field) {
            if (sortField.value === field) {
                sortDirection.value = sortDirection.value === "asc" ? "desc" : "asc";
            } else {
                sortField.value = field;
                sortDirection.value = "asc";
            }
        }

        function formatDate(dateString) {
            return format(new Date(dateString), "yyyy-MM-dd");
        }

        // 數據資訊頁設定
        const openMachineId = (machine_id) => {
            const content = {
                machineId: machine_id,
                clientAddress: machineData.value.find(item => item.machine_id == machine_id).client_address
            }
            emit('viewMachineMachineId', content);
        };

        function refreshMachineList() {
            const originSearchQuery = searchQuery.value
            searchQuery.value = "qwertyuioplkjhgfdsazxcvbnm"
            nextTick(() => {
                searchQuery.value = originSearchQuery; // 重置搜索条件为空
            });
        }

        return {
            client_address, //List設定
            machineData,
            memberData,
            measureData,
            searchQuery,
            clearSearch,
            toggleSort,
            selectedStatus,
            filteredMachines,
            formatDate,
            sortDirection,
            openMachineId,
            refreshMachineList,
        };
    },
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
    position: relative;
}

.search-container:focus-within {
    border-color: #42a5f5;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    transform: translateY(-2px);
}

.search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    width: 20px;
    height: 20px;
    pointer-events: none;
    transition: all 0.3s ease;
    z-index: 1;
    color: #888;
}

.search-container:focus-within .search-icon {
    color: #42a5f5;
}

.search-container a{
    cursor: pointer;
}

.search-bar {
    flex-grow: 1;
    padding: 10px 10px 10px 40px;
    border-radius: 5px;
    border: 1px solid transparent;
    font-size: 16px;
    background-color: transparent;
    outline: none;
    transition: all 0.3s ease;
    box-sizing: border-box;
}

.table-container {
    max-height: 450px;
    overflow-y: auto;
    margin-top: 10px;
    border: 1px solid #ddd;
}

.contractor-table {
    width: 100%;
    border-collapse: collapse;
    font-size: 17px;
    cursor: default;
}

.contractor-table th,
.contractor-table td {
    padding: 15px;
    text-align: center;
    border-bottom: 1px solid #ddd;
}

.contractor-table th {
    background-color: #e7f2fc;
    position: sticky;
    top: 0;
    z-index: 1;
}

.contractor-table tr:hover {
    background-color: #f1f1f1;
}

.contractor-table i{
    cursor: pointer;
}

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

.modal-content {
    background-color: white;
    padding: 20px;
    border-radius: 10px;
    width: 450px;
    box-shadow: none;
    border: none;
    position: relative;
    text-align: center;
}

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
}

.custom-select {
    width: 70px; /* 調整寬度變窄 */
    padding: 4px 8px; /* 減小內邊距 */
    font-size: 14px; /* 調整字體大小 */
    border: 1px solid #ccc; /* 邊框顏色 */
    border-radius: 6px; /* 減小圓角 */
    background-color: #ffffff; /* 背景色 */
    color: #333; /* 文字顏色 */
    transition: border-color 0.2s; /* 邊框顏色過渡效果 */
    outline: none; /* 移除預設聚焦樣式 */
    height: 28px;
    cursor: pointer; /* 滑鼠懸停時變成手形 */
}

.custom-select:hover {
    border-color: #888; /* 懸停時改變邊框顏色 */
}

.custom-select:focus {
    border-color: #42a5f5; /* 聚焦時邊框顏色 */
}
</style>