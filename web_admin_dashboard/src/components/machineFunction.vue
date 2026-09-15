<template>
    <div class="container">
        <main class="content">
            <div class="top-bar">
                <div class="info-card">
                    <i class="fas fa-user info-icon"></i>
                    <h3>客戶：{{ clientInfo.client_name }}　地址：{{ clientInfo.client_address }}</h3>
                </div>
                <div class="search-container">
                    <img src="../assets/ic_search1.png" class="search-icon" alt="搜尋圖示" />
                    <input type="text" class="search-bar" placeholder="搜尋" v-model="searchQuery" />
                    <a @click="clearSearch">×</a>
                </div>
            </div>
            <!-- 加工數據 List渲染 -->
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>
                                全選
                                <input
                                    type="checkbox"
                                    @change="toggleSelectAll"
                                    :checked="allSelected"
                                    class="checkbox-title"
                                />
                            </th>
                            <th @click="toggleSort('machine_date')">
                                加工日期
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
                            <machineList
                                v-for="machine in filteredMachines"
                                :key="machine.machine_id"
                                :machine="machine"
                                :members="memberData"
                                :measureData="measureData"
                                :showCheckBox="true"
                                @view-machine="openMachineId"
                                @updateSelected="updateSelected"
                                @listFinished="listFinished"
                                @emitTimeout="refreshMachineList"
                            />
                        </template>
                        <tr v-else>
                            <td colspan="7" style="text-align: center; color: gray">無符合條件的結果</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <!-- 數據資訊 -->
            <div v-if="showMachineId" class="modal-overlay">
                <div class="modal-content" @click.stop>
                    <h2>
                        <a class="view-data-btn" @click="showMachineMeasureId">
                            <i class="icon fas fa-eye"></i>
                            檢視該門窗數據
                        </a>
                        查看數據
                        <span class="checking-close-btn" @click="closeMachineId">×</span>
                    </h2>
                    <div class="form-group-container">
                        <div class="form-group">
                            <img src="../assets/ic_loading.png" class="icon" />
                            <span class="icon-text">加工狀態</span>
                            <a v-if="!isEdit">{{ currentMachine.status }}</a>
                            <div v-else class="select-container">
                                <select v-model="currentMachine.status" id="contract" class="styled-select">
                                    <!-- 使用 v-for 根據 contractData 渲染選項 -->
                                    <option value="準備">準備</option>
                                    <option value="處理">處理</option>
                                    <option value="完成">完成</option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow"></i>
                                <!-- Font Awesome 箭頭圖示 -->
                            </div>
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_date.png" class="icon" />
                            <span class="icon-text">加工日期</span>
                            <a v-if="!isEdit">{{ formatDate(currentMachine.machine_date) }}</a>
                            <input
                                v-else
                                type="text"
                                class="textbox"
                                placeholder="yyyy-MM-dd"
                                v-model="currentMachine.machine_date"
                            />
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_worker2.png" class="icon" />
                            <span class="icon-text">加工師傅</span>
                            <a v-if="!isEdit">{{ currentMachineMemberName }}</a>
                            <div v-else class="select-container">
                                <select v-model="currentMachine.account" id="contract" class="styled-select">
                                    <!-- 使用 v-for 根據 contractData 渲染選項 -->
                                    <option v-for="member in memberData" :key="member.account" :value="member.account">
                                        {{ member.member_name }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow"></i>
                                <!-- Font Awesome 箭頭圖示 -->
                            </div>
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_comments.png" class="icon" />
                            <span class="icon-text">備註　　</span>
                            <a v-if="!isEdit">{{ currentMachine.remark }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.remark" />
                        </div>

                        <div class="form-group" v-if="currentMachine.window_hori">
                            <img src="../assets/ic_windows.png" class="icon" />
                            <span class="icon-text">窗戶橫料</span>
                            <a v-if="!isEdit">{{ currentMachine.window_hori }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.window_hori" />
                        </div>
                        <div class="form-group" v-if="currentMachine.screen_window_hori">
                            <img src="../assets/ic_yarn_windows.png" class="icon" />
                            <span class="icon-text">紗窗橫料</span>
                            <a v-if="!isEdit">{{ currentMachine.screen_window_hori }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.screen_window_hori" />
                        </div>
                        <div class="form-group" v-if="currentMachine.glass_width">
                            <img src="../assets/ic_glass.png" class="icon" />
                            <span class="icon-text">玻璃寬度</span>
                            <a v-if="!isEdit">{{ currentMachine.glass_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.glass_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.inter_branch">
                            <img src="../assets/ic_thickness.png" class="icon" />
                            <span class="icon-text">內片站支</span>
                            <a v-if="!isEdit">{{ currentMachine.inter_branch }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.inter_branch" />
                        </div>
                        <div class="form-group" v-if="currentMachine.exter_branch">
                            <img src="../assets/ic_thickness.png" class="icon" />
                            <span class="icon-text">外片站支</span>
                            <a v-if="!isEdit">{{ currentMachine.exter_branch }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.exter_branch" />
                        </div>
                        <div class="form-group" v-if="currentMachine.screen_window_branch">
                            <img src="../assets/ic_thickness.png" class="icon" />
                            <span class="icon-text">紗窗站支</span>
                            <a v-if="!isEdit">{{ currentMachine.screen_window_branch }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.screen_window_branch" />
                        </div>
                        <div class="form-group" v-if="currentMachine.glass_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">玻璃高度</span>
                            <a v-if="!isEdit">{{ currentMachine.glass_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.glass_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.bottom_line_1">
                            <img src="../assets/ic_bottom_line.png" class="icon" />
                            <span class="icon-text">下台畫線</span>
                            <a v-if="!isEdit">{{ currentMachine.bottom_line_1 }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.bottom_line_1" />
                        </div>
                        <div class="form-group" v-if="currentMachine.bottom_line_2">
                            <img src="../assets/ic_bottom_line.png" class="icon" />
                            <span class="icon-text">下台畫線2</span>
                            <a v-if="!isEdit">{{ currentMachine.bottom_line_2 }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.bottom_line_2" />
                        </div>
                        <div class="form-group" v-if="currentMachine.inner_line">
                            <img src="../assets/ic_inside.png" class="icon" />
                            <span class="icon-text">內片大勾</span>
                            <a v-if="!isEdit">{{ currentMachine.inner_line }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.inner_line" />
                        </div>
                        <div class="form-group" v-if="currentMachine.screen_window_mid">
                            <img src="../assets/ic_middle.png" class="icon" />
                            <span class="icon-text">紗窗中腰</span>
                            <a v-if="!isEdit">{{ currentMachine.screen_window_mid }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.screen_window_mid" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">門花寬　</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">門花高　</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_board_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">花板高　</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_board_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_board_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_board_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">花板寬　</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_board_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_board_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_net_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">花格網高</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_net_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_net_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_net_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">花格網寬</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_net_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_net_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.left_line">
                            <img src="../assets/ic_left_to_right.png" class="icon" />
                            <span class="icon-text">內左量起畫線</span>
                            <a v-if="!isEdit">{{ currentMachine.left_line }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.left_line" />
                        </div>
                        <div class="form-group" v-if="currentMachine.door_width">
                            <img src="../assets/ic_door_frame.png" class="icon" />
                            <span class="icon-text">門框橫料</span>
                            <a v-if="!isEdit">{{ currentMachine.door_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.door_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.door_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">門框高　</span>
                            <a v-if="!isEdit">{{ currentMachine.door_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.door_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.door_piece_width">
                            <img src="../assets/ic_door.png" class="icon" />
                            <span class="icon-text">門片橫料</span>
                            <a v-if="!isEdit">{{ currentMachine.door_piece_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.door_piece_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.door_piece_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">門片高　</span>
                            <a v-if="!isEdit">{{ currentMachine.door_piece_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.door_piece_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.leaf_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">百葉寬　</span>
                            <a v-if="!isEdit">{{ currentMachine.leaf_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.leaf_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.leaf_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">百葉高　</span>
                            <a v-if="!isEdit">{{ currentMachine.leaf_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.leaf_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.leaf_number">
                            <i class="fas fa-list-ol"></i>
                            <span class="icon-text">百葉數量</span>
                            <a v-if="!isEdit">{{ currentMachine.leaf_number }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.leaf_number" />
                        </div>
                        <div class="form-group" v-if="currentMachine.top_flower_net_thing_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">上花格網料寬</span>
                            <a v-if="!isEdit">{{ currentMachine.top_flower_net_thing_width }}</a>
                            <input
                                v-else
                                type="text"
                                class="textbox"
                                v-model="currentMachine.top_flower_net_thing_width"
                            />
                        </div>
                        <div class="form-group" v-if="currentMachine.top_flower_net_thing_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">上花格網料高</span>
                            <a v-if="!isEdit">{{ currentMachine.top_flower_net_thing_height }}</a>
                            <input
                                v-else
                                type="text"
                                class="textbox"
                                v-model="currentMachine.top_flower_net_thing_height"
                            />
                        </div>
                        <div class="form-group" v-if="currentMachine.top_flower_net_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">上花格網寬</span>
                            <a v-if="!isEdit">{{ currentMachine.top_flower_net_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.top_flower_net_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.top_flower_net_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">上花格網高</span>
                            <a v-if="!isEdit">{{ currentMachine.top_flower_net_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.top_flower_net_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.mid_flower_net_thing_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">中花格網料高</span>
                            <a v-if="!isEdit">{{ currentMachine.mid_flower_net_thing_height }}</a>
                            <input
                                v-else
                                type="text"
                                class="textbox"
                                v-model="currentMachine.mid_flower_net_thing_height"
                            />
                        </div>
                        <div class="form-group" v-if="currentMachine.mid_flower_net_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">中花格網高　</span>
                            <a v-if="!isEdit">{{ currentMachine.mid_flower_net_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.mid_flower_net_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.bottom_flower_board_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">下花板寬</span>
                            <a v-if="!isEdit">{{ currentMachine.bottom_flower_board_width }}</a>
                            <input
                                v-else
                                type="text"
                                class="textbox"
                                v-model="currentMachine.bottom_flower_board_width"
                            />
                        </div>
                        <div class="form-group" v-if="currentMachine.bottom_flower_board_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">下花板高</span>
                            <a v-if="!isEdit">{{ currentMachine.bottom_flower_board_height }}</a>
                            <input
                                v-else
                                type="text"
                                class="textbox"
                                v-model="currentMachine.bottom_flower_board_height"
                            />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_grid_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">花格料寬　　</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_grid_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_grid_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.flower_grid_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">花格料高　　</span>
                            <a v-if="!isEdit">{{ currentMachine.flower_grid_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.flower_grid_height" />
                        </div>
                        <div class="form-group" v-if="currentMachine.out_window_hori">
                            <img src="../assets/ic_windows.png" class="icon" />
                            <span class="icon-text">外片窗戶橫料</span>
                            <a v-if="!isEdit">{{ currentMachine.out_window_hori }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.out_window_hori" />
                        </div>
                        <div class="form-group" v-if="currentMachine.glass_second_width">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text" v-if="currentMachine.glass_second_height">門下玻璃寬　</span>
                            <span class="icon-text" v-else>三拉第二玻璃寬度</span>
                            <a v-if="!isEdit">{{ currentMachine.glass_second_width }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.glass_second_width" />
                        </div>
                        <div class="form-group" v-if="currentMachine.glass_second_height">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">門下玻璃高　</span>
                            <a v-if="!isEdit">{{ currentMachine.glass_second_height }}</a>
                            <input v-else type="text" class="textbox" v-model="currentMachine.glass_second_height" />
                        </div>
                    </div>

                    <div class="button-group">
                        <button class="btn_clear" v-if="!isEdit" @click="deleteMachineId">刪除</button>
                        <button class="btn_clear" v-if="isEdit" @click="cancelEdit">取消</button>

                        <!-- 點擊時調用 deleteMachineId -->
                        <button class="btn edit" v-if="!isEdit" @click="startEditing">編輯</button>
                        <button class="btn fin" v-if="isEdit" @click="putMachineId">確認</button>
                    </div>
                </div>
            </div>
            <!-- 數據輸出 -->
            <div v-if="showExport" class="modal-overlay">
                <div class="modal-content extra-width" @click.stop>
                    <h2>
                        數據輸出
                        <span class="close-btn" @click="closeExport">×</span>
                    </h2>
                    <div>
                        <div class="form-group">
                            <img v-if="exportTo == ''" src="../assets/ic_information.png" class="icon1" />
                            <img v-if="exportTo == '加工單'" src="../assets/ic_processing_order.png" class="icon1" />
                            <img v-if="exportTo == '叫料單'" src="../assets/ic_bill.png" class="icon1" />
                            <select v-model="exportTo" id="contract" class="styled-select">
                                <option value="" disabled>請選擇輸出目標：</option>
                                <option>加工單</option>
                                <option>叫料單</option>
                            </select>
                        </div>
                    </div>
                    <div class="button-group">
                        <button class="btn_clear" @click="closeExport">取消</button>
                        <button class="btn fin" @click="exportData">確認</button>
                    </div>
                </div>
            </div>
            <button class="export-btn" @click="openExport"><i class="fas fa-cog"></i>數據輸出</button>
        </main>
    </div>
</template>

<script>
import { format } from "date-fns";
import { ref, computed, nextTick, watch } from "vue";
import machineList from "./List/machineList.vue";
import {
    postMachine,
    getUser,
    postMeasure,
    deleteMachineMachineId,
    putMachineMachineId,
    getDownloadMachine,
    getDownloadMaterial,
} from "../services/api";

export default {
    components: {
        machineList,
    },
    props: {
        client_address: String,
        viewMachineIdContent: Object,
        clientInfo: Object,
    },
    setup(props, { emit }) {
        // 取得客戶頁面設定
        const client_address = ref("");
        client_address.value = props.client_address;
        const viewMachineIdContent = ref(props.viewMachineIdContent);
        const machineData = ref([]);
        const memberData = [];
        const measureData = [];
        async function getAllMachineViaClientAddress() {
            await postMachine(client_address.value)
                .then((resp) => {
                    machineData.value = resp.machine.map((item) => {
                        return Object.fromEntries(Object.entries(item).filter(([_, value]) => value !== null));
                    });
                })
                .catch((err) => {
                    console.error(err.response.data.error);
                    alert(err.response.data.error);
                });
        }
        getAllMachineViaClientAddress();
        getUser()
            .then((resp) => {
                memberData.splice(
                    0,
                    memberData.length,
                    ...resp.member.map(({ account, member_name }) => ({ account, member_name }))
                );
            })
            .catch((err) => {
                console.error(err.response.data.error);
                alert(err.response.data.error);
            });
        postMeasure(client_address.value)
            .then((resp) => {
                Object.assign(measureData, resp.measure);
            })
            .catch((err) => {
                console.error(err.response.data.error);
                alert(err.response.data.error);
            });
        const searchQuery = ref("");
        function clearSearch() {
            searchQuery.value = null;
        }
        const sortField = ref("machine_date"); // 預設排序欄位
        const sortDirection = ref("asc"); // 預設排序方向
        const selectedStatus = ref("");

        // 切換排序欄位和方向的函數
        function toggleSort(field) {
            if (sortField.value === field) {
                sortDirection.value = sortDirection.value === "asc" ? "desc" : "asc";
            } else {
                sortField.value = field;
                sortDirection.value = "asc";
            }
        }
        function showMachineMeasureId() {
            emit("viewMachineMeasure", currentMachine.value.measure_id);
        }
        function formatDate(dateString) {
            return format(new Date(dateString), "yyyy-MM-dd");
        }

        const selectedMachineIds = ref(new Set()); // 用 Set 來存儲選中的 machine_id
        const allSelected = computed(() => {
            return (
                filteredMachines.value.length > 0 &&
                filteredMachines.value.every((machine) => selectedMachineIds.value.has(machine.machine_id))
            );
        });
        function updateSelected(machine) {
            if (machine.selected) {
                selectedMachineIds.value.add(machine.machine_id);
            } else {
                selectedMachineIds.value.delete(machine.machine_id);
            }
        }
        const toggleSelectAll = () => {
            const newState = !allSelected.value;
            filteredMachines.value.forEach((machine) => {
                machine.selected = newState;
                if (newState) {
                    selectedMachineIds.value.add(machine.machine_id);
                } else {
                    selectedMachineIds.value.delete(machine.machine_id);
                }
            });
        };

        // 數據資訊頁設定
        const isEdit = ref(false);
        const showMachineId = ref(false);
        const currentMachine = ref({});
        const currentMachineMemberName = ref("");
        const original_machine = {};
        const openMachineId = (machine_id) => {
            currentMachine.value = machineData.value.find((item) => item.machine_id === machine_id);
            currentMachine.value.machine_date = formatDate(currentMachine.value.machine_date);
            const members = JSON.parse(JSON.stringify(memberData));
            currentMachineMemberName.value = members.find(
                (member) => member.account == currentMachine.value.account
            ).member_name;
            if (currentMachine.value && currentMachineMemberName.value) {
                showMachineId.value = true;
            }
        };
        function listFinished() {
            if (viewMachineIdContent.value) {
                openMachineId(viewMachineIdContent.value.machineId);
            }
        }
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
                        const matchedMember = memberData.find((member) =>
                            member.member_name.toLowerCase().includes(query)
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

            // 當狀態篩選改變時，保留上一步已選中的數據
            watch(filteredMachines, (newMachines) => {
                // 更新每個機器項的選中狀態
                newMachines.forEach((machine) => {
                    machine.selected = selectedMachineIds.value.has(machine.machine_id);
                });

                // 檢查是否所有篩選後的數據都被選中
                if (!allSelected.value) {
                    // 如果不是全選，設置全選框為 false
                    const selectAllCheckbox = document.querySelector("#select-all-checkbox");
                    if (selectAllCheckbox) {
                        selectAllCheckbox.checked = false;
                    }
                }
            });
            // 狀態篩選邏輯
            if (selectedStatus.value) {
                viewMachineIdContent.value = null;
                machines = machines.filter((machine) => machine.status === selectedStatus.value);
            }

            // 自訂的排序邏輯
            const statusPriority = { 準備: 1, 處理: 2, 完成: 3 };

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
        const closeMachineId = () => {
            isEdit.value = false;
            getAllMachineViaClientAddress();
            showMachineId.value = false;
        };
        function refreshMachineList() {
            const originSearchQuery = searchQuery.value;
            searchQuery.value = "qwertyuioplkjhgfdsazxcvbnm";
            nextTick(() => {
                searchQuery.value = originSearchQuery; // 重置搜索条件为空
            });
        }
        const startEditing = () => {
            Object.assign(original_machine, currentMachine.value);
            isEdit.value = true; // 進入編輯模式
        };
        const cancelEdit = () => {
            currentMachine.value = { ...original_machine };
            isEdit.value = false; // 退出編輯模式
        };
        // 修改數據
        const putMachineId = () => {
            const { machine_id, ...restFields } = currentMachine.value;
            // 只保留有變動的欄位
            const contents = Object.keys(restFields).reduce((acc, key) => {
                if (restFields[key] !== original_machine[key]) {
                    acc[key] = restFields[key];
                }
                return acc;
            }, {});
            if (Object.keys(contents).length > 0) {
                putMachineMachineId({
                    machine_id: machine_id,
                    contents: contents,
                })
                    .then((res) => {
                        isEdit.value = false;
                        getAllMachineViaClientAddress();
                    })
                    .catch((err) => {
                        console.error(err.response.data.error);
                        alert(err.response.data.error);
                    });
            } else {
                isEdit.value = false;
            }
        };
        // 刪除數據資料
        const deleteMachineId = () => {
            if (confirm("你確定要刪除這份數據嗎？")) {
                deleteMachineMachineId(currentMachine.value.machine_id)
                    .then((resp) => {
                        showMachineId.value = false;
                        getAllMachineViaClientAddress();
                    })
                    .catch((err) => {
                        console.error(err.response.data.error);
                        alert(err.response.data.error);
                    });
            }
        };
        const showExport = ref(false);
        const exportTo = ref("");
        function openExport() {
            showExport.value = true;
        }
        function closeExport() {
            showExport.value = false;
            exportTo.value = "";
        }
        function exportData() {
            const selectedMachineIds = machineData.value
                .filter((item) => item.selected === true)
                .map((item) => item.machine_id);
            if (selectedMachineIds.length == 0) {
                alert("請選擇要輸出的資料");
            } else {
                switch (exportTo.value) {
                    case "加工單":
                        getDownloadMachine(selectedMachineIds);
                        break;
                    case "叫料單":
                        getDownloadMaterial(selectedMachineIds);
                        break;
                }
            }
            closeExport();
        }

        return {
            client_address, //List設定
            machineData,
            memberData,
            measureData,
            searchQuery,
            clearSearch,
            filteredMachines,
            formatDate,
            updateSelected,
            allSelected,
            toggleSelectAll,
            sortDirection,
            toggleSort,
            selectedStatus,
            showMachineMeasureId, //資訊框設定
            showMachineId,
            currentMachine,
            currentMachineMemberName,
            isEdit,
            openMachineId,
            closeMachineId,
            listFinished,
            refreshMachineList,
            startEditing,
            cancelEdit,
            putMachineId,
            deleteMachineId,
            showExport, //輸出加工單/叫料單
            exportTo,
            openExport,
            closeExport,
            exportData,
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

html,
body {
    height: 100%;
    font-family: "Arial", sans-serif;
    background-color: #f5f5f5;
}

.header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    background-color: #b0c4de;
    padding: 15px 20px;
}

.header h1 {
    color: white;
    font-size: 24px;
}

.nav a {
    margin-right: 20px;
    text-decoration: none;
    color: black;
    font-size: 18px;
}

.nav .profile-icon {
    width: 35px;
    height: 35px;
    background-color: #e0e0e0;
    border-radius: 50%;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 22px;
}

.container {
    display: flex;
    flex-direction: row;
    height: calc(100vh - 70px);
    /* 確保 main 填滿頁面剩餘高度 */
    overflow: hidden;
    /* 避免溢出 */
}

.sidebar {
    width: 250px;
    background-color: #e0e0e0;
    padding: 20px;
    display: flex;
    flex-direction: column;
}

.sidebar-btn {
    background: none;
    border: none;
    text-align: left;
    font-size: 20px;
    padding: 15px;
    cursor: pointer;
    margin-bottom: 10px;
}

.sidebar-btn.active {
    font-weight: bold;
    background-color: #d6d6d6;
    border-radius: 5px;
}

.content {
    flex: 1;
    overflow-y: auto;
    background-color: #fff;
}

.top-bar {
    padding-top: 10px;
    padding-left: 10px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.add-btn {
    background: linear-gradient(135deg, #66bb6a, #43a047);
    /* 漸層背景 */
    color: white;
    border: none;
    padding: 8px 18px;
    /* 縮小內邊距 */
    font-size: 20px;
    /* 調整字體大小 */
    font-weight: bold;
    /* 文字加粗 */
    cursor: pointer;
    border-radius: 25px;
    /* 圓角按鈕 */
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 陰影增加立體感 */
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    /* 圖示與文字之間的間距 */
    width: 180px;
    /* 調整按鈕寬度 */
    height: 50px;
    /* 減少高度提升美觀 */
}

.custom-icon {
    color: #ffffff;
    font-size: 20px;
    /* 調整圖示大小 */
    transition: transform 0.3s ease;
}

.add-btn:hover {
    background: linear-gradient(135deg, #81c784, #388e3c);
    /* 改變背景色 */
    box-shadow: 0 8px 10px rgba(0, 0, 0, 0.2);
    transform: scale(1.05);
    /* 輕微放大 */
}

/* 懸停時圖示旋轉 */
.add-btn:hover .custom-icon {
    transform: rotate(90deg);
    /* 懸停時旋轉圖示 */
}

/* 按下按鈕的效果 */
.add-btn:active {
    transform: scale(0.95);
    /* 按下時縮小 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    /* 陰影變小 */
}

/* 提示訊息CSS */
.info-card {
    display: flex;
    align-items: center;
    background-color: #e3f2fd;
    padding: 10px 20px;
    border-radius: 8px;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    margin: 10px 0;
    margin-left: 205px;
    cursor: default;
}

/* 圖示樣式 */
.info-icon {
    color: #42a5f5;
    font-size: 20px;
    margin-right: 10px;
}

/* 提醒文字樣式 */
.info-card h3 {
    margin: 0;
    color: #333;
}

.search-container {
    display: flex;
    align-items: center;
    margin-left: auto;
    margin-right: 10px;
    width: 400px;
    height: 40px;
    padding: 10px;
    border: 1px solid #ccc;
    border-radius: 5px;
    background-color: white;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    position: relative;
}

/* 當輸入框聚焦時整個容器的效果 */
.search-container:focus-within {
    border-color: #42a5f5;
    box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
    /* 增強陰影效果 */
    transform: translateY(-2px);
    /* 浮起效果 */
}

/* 搜尋圖示的樣式 */
.search-icon {
    position: absolute;
    left: 12px;
    top: 50%;
    transform: translateY(-50%);
    /* 垂直置中 */
    width: 20px;
    height: 20px;
    pointer-events: none;
    /* 防止圖示阻擋輸入 */
    transition: all 0.3s ease;
    /* 圖示平滑效果 */
    z-index: 1;
    /* 提升圖示層級 */
    color: #888;
    /* 預設圖示顏色 */
}

/* 當輸入框聚焦時，讓圖示也改變顏色 */
.search-container:focus-within .search-icon {
    color: #42a5f5;
    /* 圖示顏色變化 */
}

/* 搜尋欄的樣式 */
.search-bar {
    flex-grow: 1;
    padding: 10px 10px 10px 40px;
    /* 左側預留空間給圖示 */
    border-radius: 5px;
    border: 1px solid transparent;
    /* 預設為透明，避免邊框重疊 */
    font-size: 16px;
    background-color: transparent;
    outline: none;
    /* 移除預設聚焦框 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    box-sizing: border-box;
}

/* 當輸入框聚焦時的效果 */
.search-bar:focus {
    border-color: transparent;
    /* 避免與外部容器邊框重疊 */
}

.search-container a{
    cursor: pointer;
}

/* 表格容器樣式，讓表格區域有固定高度並啟用滾動條 */
.table-container {
    max-height: 470px;
    /* 固定表格容器的最大高度 */
    overflow-y: auto;
    /* 啟用垂直滾動條 */
    margin-top: 10px;
    margin-bottom: 15px;
    border: 1px solid #ddd;
    /* 表格邊框 */
}

.data-table {
    width: 100%;
    border-collapse: collapse;
    margin-bottom: 20px;
    background-color: #fafafa;
    text-align: center;
    /* 水平置中 */
    cursor: default;
}

.data-table th,
.data-table td {
    padding: 15px;
    text-align: left;
    font-size: 18px;
    text-align: center;
    /* 水平置中 */
    vertical-align: middle;
    /* 垂直置中 */
}

.data-table th {
    background-color: #e7f2fc;
    position: sticky;
    top: 0;
    z-index: 1;
    /* 確保表頭顯示在最前 */
    font-weight: bold;
}

/* 表格列的懸停效果 */
.data-table tr:hover {
    background-color: #f1f1f1;
}

.data-table i{
    cursor: pointer;
}

.more-btn {
    background-color: #2196f3;
    color: white;
    border: none;
    padding: 8px 12px;
    cursor: pointer;
    border-radius: 5px;
    transition: background-color 0.3s;
}

.more-btn:hover {
    background-color: #1e88e5;
}

.export-btn {
    margin-left: auto;
    margin-right: 10px;
    background-color: #ff7043;
    /* 改為更亮眼的橘色 */
    color: white;
    border: none;
    padding: 8px 18px;
    /* 縮小內邊距 */
    font-size: 20px;
    /* 調整字體大小 */
    font-weight: bold;
    /* 文字加粗 */
    cursor: pointer;
    border-radius: 25px;
    /* 圓角按鈕 */
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    /* 陰影增加立體感 */
    transition: all 0.3s ease;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    /* 圖示與文字之間的間距 */
    width: 180px;
    /* 調整按鈕寬度 */
    height: 50px;
    /* 減少高度提升美觀 */
}

.export-btn i {
    font-size: 20px;
    /* 調整圖示大小 */
    transition: transform 0.3s ease;
    color: #ffffff;
    /* 圖示顏色為白色 */
}

.export-btn:hover {
    background-color: #ff5722;
    /* 懸停時顏色變深 */
    box-shadow: 0 8px 10px rgba(0, 0, 0, 0.2);
    transform: scale(1.05);
    /* 輕微放大 */
}

/* 懸停時圖示旋轉 */
.export-btn:hover i {
    transform: rotate(90deg);
    /* 懸停時旋轉圖示 */
}

.export-btn:active {
    transform: scale(0.95);
    /* 按下時縮小 */
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.15);
    /* 陰影變小 */
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

/* 模態框內容 */
.modal-content {
    background-color: white;
    padding: 20px;
    border-radius: 10px;
    max-height: 700px;
    width: 822px;
    /* 移除 box-shadow 和任何 border */
    box-shadow: none;
    /* 或直接刪除這一行 */
    border: none;
    /* 如果有 border 設定，刪除或設置為 none */
    position: relative;
    text-align: center;
    overflow-y: auto;
    /* 啟用垂直滾動條 */
    scrollbar-width: thin;
    /* Firefox 支援 */
    scrollbar-color: #a0a0a0 transparent;
    /* 滾動條的顏色 */
}

.extra-width {
    width: 400px;
    /* 額外的寬度樣式 */
}

/* 標題樣式 */
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
    position: sticky;
    top: 0;
    /* 固定在模態框的頂部 */
    left: 0;
    right: 0;
    z-index: 10;
    /* 確保它顯示在最前方 */
    outline: 20px solid white;
}

/* 輸入欄位樣式 */
.modal-content input {
    border: 2px solid #42a5f5;
    /* 顯眼的邊框顏色 */
    background-color: #f0f8ff;
    /* 顯眼的背景顏色 */
    box-shadow: 0 0 5px #42a5f5;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    padding: 8px;
    /* 增加內邊距 */
    font-size: 16px;
    /* 增加字體大小 */
    height: 40px;
    /* 增加輸入框的高度 */
}

/* Chrome 和 Edge 的滾動條設計 */
.modal-content::-webkit-scrollbar {
    width: 10px;
    /* 調整滾動條寬度 */
}

.modal-content::-webkit-scrollbar-track {
    background: transparent;
    /* 滾動條軌道背景變透明 */
}

.modal-content::-webkit-scrollbar-thumb {
    background-color: #b0bec5;
    /* 滾動條本體顏色 */
    border-radius: 8px;
    /* 圓角滾動條 */
    border: 2px solid white;
    /* 留白效果，模擬內嵌背景 */
}

.modal-content::-webkit-scrollbar-thumb:hover {
    background-color: #90a4ae;
    /* 滑鼠懸停變色 */
}

/* 新增樣式：容器將資料分成兩欄 */
.form-group-container {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
}

/* 表單欄位樣式 */
.form-group {
    display: flex;
    align-items: center;
    background-color: #f2f6fa;
    border-radius: 5px;
    margin-bottom: 15px;
    padding: 10px;
    position: relative;
}

.form-group input {
    border: none;
    background-color: transparent;
    flex-grow: 1;
    font-size: 16px;
    margin-left: 10px;
}

/* 圖示 */
.form-group img {
    width: 24px;
    height: 24px;
}

.form-group .icon {
    width: 20px;
    height: 20px;
    margin-right: 10px;
    vertical-align: middle;
}

.form-group .icon1 {
    width: 50px;
    height: 50px;
    margin-right: 10px;
    vertical-align: middle;
}

.form-group i {
    width: 20px;
    height: 20px;
    margin-top: 5px;
    margin-right: 10px;
    vertical-align: middle;
}

.icon-text {
    font-weight: bold;
    margin-right: 10px;
    font-size: 16px;
}

.textbox {
    padding: 12px;
    /* 增加內邊距 */
    font-size: 16px;
    /* 增加字體大小 */
    height: 40px;
    /* 增加輸入框的高度 */
}

.edit-mode {
    border: 2px solid #42a5f5;
    /* 顯眼的邊框顏色 */
    background-color: #f0f8ff;
    /* 顯眼的背景顏色 */
    box-shadow: 0 0 5px #42a5f5;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
}

/* 美化下拉選單 */
.styled-select {
    width: 275px;
    padding: 12px;
    font-size: 16px;
    border: 2px solid #96cbf6;
    /* 顯眼的邊框顏色 */
    box-shadow: 0 0 5px #96cbf6;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    border-radius: 25px;
    /* 圓角 */
    background: linear-gradient(135deg, #f2f6fa, #e6eef4);
    /* 漸層背景 */
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    cursor: pointer;
    padding-right: 40px;
    /* 預留箭頭空間 */
    transition: all 0.3s ease;
    box-sizing: border-box;
    color: #333;
}

/* 移除 IE 預設箭頭 */
.styled-select::-ms-expand {
    display: none;
}

/* Font Awesome 箭頭圖示樣式 */
.select-arrow {
    position: absolute;
    right: 15px;
    top: 46%;
    transform: translateY(-50%);
    pointer-events: none;
    color: #888;
    font-size: 18px;
    transition: all 0.3s ease;
}

/* 下拉選單聚焦效果 */
.styled-select:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

.styled-select:hover {
    background: linear-gradient(135deg, #d8e2ec, #c8d4e0);
    /* 背景變亮 */
}

/* 當選單展開時的動畫效果 */
.styled-select {
    transition: all 0.3s ease;
}

/* 聚焦效果 */
.styled-select:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

/* 聚焦時箭頭變色 */
.styled-select:focus + .select-arrow {
    color: #42a5f5;
}

/* 下拉選單內的選項樣式 */
.styled-select option {
    padding: 10px;
    background-color: #fff;
    font-size: 16px;
}

.styled-select option[value=""] {
    color: #aaa;
}

.styled-select-short {
    width: 298px;
    padding: 12px;
    margin-left: auto;
    font-size: 16px;
    border: 2px solid #96cbf6;
    /* 顯眼的邊框顏色 */
    box-shadow: 0 0 5px #96cbf6;
    /* 增加陰影效果 */
    transition: all 0.3s ease;
    /* 平滑過渡效果 */
    border-radius: 25px;
    /* 圓角 */
    background: linear-gradient(135deg, #f2f6fa, #e6eef4);
    /* 漸層背景 */
    appearance: none;
    -webkit-appearance: none;
    -moz-appearance: none;
    cursor: pointer;
    padding-right: 40px;
    /* 預留箭頭空間 */
    transition: all 0.3s ease;
    box-sizing: border-box;
    color: #333;
}

/* 移除 IE 預設箭頭 */
.styled-select-short::-ms-expand {
    display: none;
}

/* Font Awesome 箭頭圖示樣式 */
.select-arrow-short {
    position: absolute;
    right: 5%;
    top: 46%;
    transform: translateY(-50%);
    pointer-events: none;
    color: #888;
    font-size: 18px;
    transition: all 0.3s ease;
}

/* 下拉選單聚焦效果 */
.styled-select-short:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

.styled-select-short:hover {
    background: linear-gradient(135deg, #d8e2ec, #c8d4e0);
    /* 背景變亮 */
}

/* 當選單展開時的動畫效果 */
.styled-select-short {
    transition: all 0.3s ease;
}

/* 聚焦效果 */
.styled-select-short:focus {
    border-color: #42a5f5;
    box-shadow: 0 0 5px rgba(66, 165, 245, 0.5);
    outline: none;
}

/* 聚焦時箭頭變色 */
.styled-select-short:focus + .select-arrow-short {
    color: #42a5f5;
}

/* 下拉選單內的選項樣式 */
.styled-select-short option {
    padding: 10px;
    background-color: #fff;
    font-size: 16px;
}

.styled-select-short option[value=""] {
    color: #aaa;
}

/* 檢視門窗數據按鈕樣式 */
.view-data-btn {
    margin-left: -45%;
    margin-right: 22%;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #6bbcff, #1e88e5); /* 漸層背景 */
    color: white; /* 文字顏色 */
    font-weight: bold; /* 粗體文字 */
    font-size: 16px; /* 字體大小，適中，原本 18px */
    padding: 8px 15px; /* 調整內邊距，介於太小和太大的範圍 */
    border: none; /* 移除邊框 */
    border-radius: 20px; /* 圓角按鈕，適中 */
    cursor: pointer; /* 指標顯示為手型 */
    box-shadow: 0 3px 5px rgba(0, 0, 0, 0.1); /* 適中的陰影效果 */
    transition: all 0.3s ease; /* 平滑過渡效果 */
    text-decoration: none; /* 移除下劃線 */
    gap: 6px; /* 圖標和文字的間距，適中 */
}

/* 懸停效果 */
.view-data-btn:hover {
    background: linear-gradient(135deg, #5aa9e6, #1565c0); /* 更深的背景色 */
    box-shadow: 0 5px 8px rgba(0, 0, 0, 0.2); /* 增強陰影效果 */
    transform: scale(1.05); /* 輕微放大 */
}

/* 按下效果 */
.view-data-btn:active {
    transform: scale(0.95); /* 輕微縮小 */
    box-shadow: 0 2px 3px rgba(0, 0, 0, 0.15); /* 陰影變小 */
}

/* 圖標樣式 */
.view-data-btn .icon {
    font-size: 18px; /* 調整圖標大小，適中 */
    color: white; /* 圖標顏色 */
    transition: transform 0.3s ease; /* 圖標平滑過渡 */
}

/* 按鈕組樣式 */
.button-group {
    display: flex;
    justify-content: center;
    gap: 20px;
    /* 增加按鈕之間的距離 */
    margin-top: 20px;
}

.btn {
    background-color: #34a9e4;
    color: white;
    padding: 10px 30px;
    border: none;
    border-radius: 25px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
}

.btn:hover {
    background-color: #1373a3;
}

.btn_clear {
    background-color: #9575cd;
    color: white;
    padding: 10px 30px;
    border: none;
    border-radius: 25px;
    cursor: pointer;
    font-size: 16px;
    font-weight: bold;
}

.btn_clear:hover {
    background-color: #7542cc;
}

.close-btn {
    font-size: 30px;
    position: absolute;
    top: 1px;
    right: 13px;
    cursor: pointer;
    color: #282828;
}

.close-btn:hover {
    color: #ff0000;
    /* 當滑鼠懸停時，顏色變成紅色 */
}

.checking-close-btn {
    font-size: 30px;
    position: absolute;
    top: 6px;
    right: 13px;
    cursor: pointer;
    color: #282828;
}

.checking-close-btn:hover {
    color: #ff0000;
    /* 當滑鼠懸停時，顏色變成紅色 */
}

.checkbox-title {
    -webkit-appearance: none;
    /* 移除瀏覽器的預設外觀 */
    appearance: none;
    /* margin-right: 8px; */
    width: 18px;
    height: 18px;
    border: 2px solid #42a5f5;
    /* 設置邊框顏色 */
    border-radius: 5px;
    /* 圓角方塊 */
    background-color: transparent;
    /* 背景透明 */
    cursor: pointer;
    transition: all 0.3s ease;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
    /* 輕微陰影 */
}

.checkbox-title:checked {
    background-color: #42a5f5;
    /* 勾選時的背景顏色 */
    border: 2px solid #42a5f5;
    /* 勾選時的邊框顏色 */
    position: relative;
}

.checkbox-title:checked::after {
    content: "✓";
    /* 使用 Unicode 字符來顯示勾 */
    color: white;
    /* 勾的顏色設為白色 */
    font-size: 14px;
    font-weight: bold;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
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

@media (max-width: 600px) {
    .checkbox-group {
        flex-direction: column;
    }

    .checkbox-group label {
        width: 100%;
        justify-content: center;
    }
}
</style>
