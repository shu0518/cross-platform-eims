<template>
    <tr>
        <td>{{ item.member_name }}</td>
        <td>{{ item.account }}</td>
        <td>{{ item.member_mail }}</td>
        <td><a href="#" @click="viewDetails">更多</a></td>
    </tr>
</template>

<script>
export default {
    props: {
        item: Object
    },
    setup(props, { emit }) {  // 從 setup 的第二個參數中解構出 emit

        function viewDetails() {
            // 通過 emit 傳遞承包商的 contract_id 給父組件
            emit('view-member', props.item.account);
        }

        return {
            viewDetails
        }
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
</style>