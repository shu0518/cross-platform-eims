<template>
    <tr>
        <td>{{ formatDate(exported.date) }}</td>
        <td>{{ exported.data_from }}</td>
        <td><a href="#" @click="downloadExported">下載</a></td>
    </tr>
</template>

<script>
import { getDownloadExported } from "../../services/api";
import { format } from 'date-fns';

export default {
    props: {
        exported: Object,
        client_address: String
    },
    setup(props) {
        function downloadExported() {
            const contents = { export_id: props.exported.export_id}
            getDownloadExported(contents)
        }
        function formatDate(dateString) {
            return format(new Date(dateString), 'yyyy-MM-dd');
        }

        return {
            downloadExported,
            formatDate
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