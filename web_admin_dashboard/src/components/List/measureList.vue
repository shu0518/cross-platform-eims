<template>
    <tr v-if="measure">
        <td><input type="checkbox" v-model="measure.selected" @change="selected" class="checkbox-title" /></td>
        <td>{{ formatDate(measure.measure_date) }}</td>
        <td>{{ measure.position }}</td>
        <td>{{ measure.specification.windoor }}</td>
        <td>{{ measure.width }} × {{ measure.length }}</td>
        <td>
            <span v-for="(type, index) in specTypeList" :key="index" style="display: block;">
                {{ measure.specification.type[type] }}
            </span>
        </td>
        <td><a href="#" @click="viewDetails">更多</a></td>
    </tr>
    <tr v-else>
        <td colspan="7">資料尚未載入或格式不正確</td>
    </tr>

</template>

<script>
import { format } from 'date-fns';
import { computed } from 'vue';
export default {
    props: {
        measure: Object
    },
    setup(props, { emit }) {
        props.measure.specification = JSON.stringify(props.measure.specification)
        props.measure.specification = JSON.parse(props.measure.specification)
        let specTypeList = []
        if (props.measure.specification.windoor == "窗") {
            specTypeList = ["piece", "option"]
        } else {
            specTypeList = ["frame", "piece", "model"]
        }
        function formatDate(dateString) {
            return format(new Date(dateString), 'yyyy-MM-dd');
        }
        function viewDetails() {
            emit('view-measure', props.measure.measure_id);
        }
        function selected() {
            emit('updateSelected', props.measure);
        }

        return {
            specTypeList,
            selected,
            formatDate,
            viewDetails,
        };
    }

}
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
</style>
