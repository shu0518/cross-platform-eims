<template>
    <tr v-if="machine">
        <td v-if="showCheckBox"><input type="checkbox" v-model="machine.selected" @change="selected"
                class="checkbox-title" /></td>
        <td>{{ formatDate(machine.machine_date) }}</td>
        <td><span :class="getStatusClass(machine.status)">{{ machine.status }}</span></td>
        <td>{{ memberName }}</td>
        <td>
            <span v-for="(type, index) in specTypeList" :key="index" style="display: block;">
                {{ measureType[type] }}
            </span>
        </td>
        <td v-if="showCheckBox"><a href="#" @click="viewDetails">更多</a></td>
        <td v-else><a href="#" @click="viewDetails">檢視</a></td>
    </tr>
    <tr v-else>
        <td colspan="7">資料尚未載入或格式不正確</td>
    </tr>

</template>

<script>
import { format } from 'date-fns';
import { ref, onMounted, onUnmounted } from 'vue';

export default {
    props: {
        machine: Object,
        members: Object,
        measureData: Object,
        showCheckBox: Boolean
    },
    setup(props, { emit }) {
        function formatDate(dateString) {
            return format(new Date(dateString), 'yyyy-MM-dd');
        }
        function viewDetails() {
            emit('view-machine', props.machine.machine_id);
        }
        function selected() {
            emit('updateSelected', props.machine);
        }
        function getStatusClass(status) {
            switch (status) {
                case '準備':
                    return 'status-ready';
                case '處理':
                    return 'status-processing';
                case '完成':
                    return 'status-completed';
                default:
                    return '';
            }
        }

        const members = ref();
        members.value = JSON.parse(JSON.stringify(props.members));
        const memberName = ref();
        const measureType = ref();
        const specTypeList = [];
        const intervalId = ref(null);
        const checkIntervalId = ref(null); // 新增的定時器 ID
        const listFinished = ref(false);

        function updatePropsData() {
            const member = members.value.find(member => member.account == props.machine.account);
            memberName.value = member ? member.member_name : '載入中';

            const measure = props.measureData.find(item => item.measure_id == props.machine.measure_id);
            measureType.value = measure ? measure.specification.type : '未知數據';

            if (measure && measure.specification) {
                if (measure.specification.windoor === "窗") {
                    Object.assign(specTypeList, ["piece", "option"]);
                } else {
                    Object.assign(specTypeList, ["frame", "piece", "model"]);
                }
            }

            if (memberName.value != '載入中' && measureType.value != "未知數據" && measureType.value.length != 0) {
                clearInterval(intervalId.value);
                clearInterval(checkIntervalId.value);
                emit("listFinished");
            }
        }

        // 新增一個函數來檢查定時器是否已清除
        function checkInterval() {
            if (!listFinished.value) {
                emit('emitTimeout'); // 向父組件發送事件
            }
        }

        // 使用 onMounted 啟動兩個定時器
        onMounted(() => {
            intervalId.value = setInterval(updatePropsData, 1);
            checkIntervalId.value = setInterval(checkInterval, 10); // 1秒後檢查是否清除
        });

        // 清理定時器
        onUnmounted(() => {
            clearInterval(intervalId.value);
            clearInterval(checkIntervalId.value);
        });

        return {
            selected,
            formatDate,
            viewDetails,
            getStatusClass,
            memberName,
            measureType,
            specTypeList
        };
    }
};
</script>
<style scoped>
table {
    width: 100%;
    border-collapse: collapse;
    margin: 10px 0;
    background-color: #fff;
}

thead th {
    padding: 10px;
    border-bottom: 2px solid #000;
}

tbody td {
    padding: 10px;
    border-bottom: 1px solid #ddd;
    text-align: center;
}

tbody td a {
    color: #007bff;
    text-decoration: none;
    cursor: pointer;
}

.checkbox-title {
    -webkit-appearance: none;
    /* 移除瀏覽器的預設外觀 */
    appearance: none;
    /* margin-right: 8px; */
    width: 16px;
    height: 16px;
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

.status-ready {
    display: inline-block;
    background-color: #737eb7;
    /* 藍色 */
    color: white;
    font-weight: bold;
    padding: 5px 15px;
    border-radius: 25px;
    text-align: center;
    font-size: 16px;
}

.status-processing {
    display: inline-block;
    background-color: #6cae5b;
    /* 綠色 */
    color: white;
    padding: 5px 15px;
    border-radius: 25px;
    text-align: center;
    font-size: 16px;
    font-weight: bold;
}

.status-completed {
    display: inline-block;
    background-color: #9e9e9e;
    /* 灰色 */
    color: white;
    padding: 5px 15px;
    border-radius: 25px;
    text-align: center;
    font-size: 16px;
    font-weight: bold;
}
</style>
