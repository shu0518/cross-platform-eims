<template>
    <tr @click="viewClientFunction">
        <td @click="viewClientFunction">{{ client.client_name }}</td>
        <td @click="viewClientFunction">{{ client.client_phone || "無資料" }}</td>
        <td @click="viewClientFunction">{{ client.client_address }}</td>
        <td @click="viewClientFunction">{{ client.client_mail || "無資料" }}</td>
        <td @click="viewClientFunction">{{ client.contract_name }}</td>
        <td @click="viewClientFunction"><a href="#" @click.stop.prevent="viewDetails">更多</a></td>
    </tr>
</template>

<script>
export default {
    props: {
        client: Object,
    },
    setup(props, { emit }) {
        function viewDetails() {
            emit('view-client', props.client.client_id);
        };
        function viewClientFunction() {
            emit('viewClientFunction', props.client.client_id)
        }

        return {
            viewDetails,
            viewClientFunction
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