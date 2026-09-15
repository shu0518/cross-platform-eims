<template>
    <div class="container">
        <main class="content">
            <div class="top-bar">
                <button class="add-btn" @click="openCreateMeasure">
                    <i class="fas fa-plus custom-icon"></i>新增數據
                </button>
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
            <!-- 門窗數據List渲染 -->
            <div class="table-container">
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>全選 <input type="checkbox" @change="toggleSelectAll" :checked="allSelected"
                                    class="checkbox-title" /></th>
                            <th>日期</th>
                            <th>位置</th>
                            <th>類型</th>
                            <th>寬×高</th>
                            <th>內容</th>
                            <th>詳細資訊</th>
                        </tr>
                    </thead>
                    <tbody>
                        <template v-if="filteredMeasures && filteredMeasures.length > 0">
                            <measureList v-for="measure in filteredMeasures" :key="measure.measure_id"
                                :measure="measure" @view-measure="openMeasureId" @updateSelected="updateSelected" />
                        </template>
                        <tr v-else>
                            <td colspan="7" style="text-align: center; color: gray">無符合條件的結果</td>
                        </tr>
                    </tbody>
                </table>
            </div>
            <!-- 數據資訊 -->
            <div v-if="showMeasureId" class="modal-overlay">
                <div class="modal-content" @click.stop>
                    <h2>
                        查看數據
                        <span class="close-btn" @click="closeMeasureId">×</span>
                    </h2>
                    <div class="form-group-container">
                        <div class="form-group">
                            <img src="../assets/ic_date.png" class="icon" />
                            <span class="icon-text">日期</span>
                            <a v-if="!isEdit">{{ formatDate(currentMeasure.measure_date) }}</a>
                            <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                                v-model="currentMeasure.measure_date" />
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_house.png" class="icon" />
                            <span class="icon-text">位置</span>
                            <a v-if="!isEdit">{{ currentMeasure.position }}</a>
                            <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                                v-model="currentMeasure.position" />
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_windows.png" class="icon" />
                            <span class="icon-text">類型</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.windoor }}</a>
                            <select v-else v-model="currentMeasure.specification.windoor" id="contract"
                                class="styled-select-short">
                                <option value="" disabled selected>請選擇門框或窗框</option>
                                <!-- 模擬 placeholder -->
                                <option>門</option>
                                <option>窗</option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                        </div>
                        <div class="form-group">
                            <i class="fas fa-list-ol"></i>
                            <span class="icon-text">數量</span>
                            <a v-if="!isEdit">{{ currentMeasure.quantity }}</a>
                            <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                                v-model="currentMeasure.quantity" />
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_width.png" class="icon" />
                            <span class="icon-text">寬度</span>
                            <a v-if="!isEdit">{{ currentMeasure.width }}</a>
                            <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                                v-model="currentMeasure.width" />
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_height.png" class="icon" />
                            <span class="icon-text">高度</span>
                            <a v-if="!isEdit">{{ currentMeasure.length }}</a>
                            <input v-else type="text" :class="{ 'textbox': true, 'edit-mode': isEdit }"
                                v-model="currentMeasure.length" />
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_color.png" class="icon" />
                            <span class="icon-text">顏色</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.color }}</a>
                            <select v-else v-model="currentMeasure.specification.color" id="color-select"
                                class="styled-select-short">
                                <option value="" disabled selected>請選擇顏色</option>
                                <!-- 模擬 placeholder -->
                                <option v-for="(color, index) in colorList" :key="index" :value="color">
                                    {{ color }}
                                </option>
                            </select>
                            <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                        </div>
                        <div class="form-group">
                            <img src="../assets/ic_wall.png" class="icon" />
                            <span class="checkbox-icon-text">牆體</span>
                            <a v-if="!isEdit">{{ currentMeasure.specification.wall.join(", ") }}</a>
                            <div v-else class="checkbox-group">
                                <label v-for="(wall, index) in wallList" :key="index">
                                    <input type="checkbox" :value="wall" v-model="currentMeasure.specification.wall" />
                                    {{ wall }}
                                </label>
                            </div>
                        </div>
                    </div>
                    <!-- 窗 -->
                    <div v-if="currentMeasure.specification.windoor == '窗'">
                        <!-- 要求 -->
                        <div class="form-group-container">
                            <div class="form-group">
                                <img src="../assets/ic_comments.png" class="icon" />
                                <span class="checkbox-icon-text">客戶要求</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.request.join(", ") }}</a>
                                <div v-else class="checkbox-group">
                                    <label v-for="(request, index) in windowRequestList" :key="index">
                                        <input type="checkbox" :value="request"
                                            v-model="currentMeasure.specification.request" />
                                        {{ request }}
                                    </label>
                                    <label>
                                        <input type="checkbox" v-model="requestOtherChecked" />
                                        <!-- 新增的CheckBox -->
                                        <input
                                            :class="{ 'no-border': !requestOtherChecked, 'active': requestOtherChecked }"
                                            :disabled="!requestOtherChecked"
                                            v-model="currentMeasure.specification.requestOther" placeholder="請輸入其他要求" />
                                    </label>
                                </div>
                            </div>
                            <!-- 234 -->
                            <div class="form-group">
                                <img src="../assets/ic_open_window.png" class="icon" />
                                <span class="icon-text">窗框款式</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.piece }}</a>
                                <select v-else v-model="currentMeasure.specification.type.piece" id="contract"
                                    class="styled-select">
                                    <option value="" disabled selected>請選擇款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(type, index) in windowTypeList" :key="index" :value="type">
                                        {{ type }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                            <!-- 866 1066 1068 -->
                            <div class="form-group">
                                <img src="../assets/ic_information.png" class="icon" />
                                <span class="icon-text">窗框細項</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.option }}</a>
                                <select v-else v-model="currentMeasure.specification.type.option" class="styled-select">
                                    <option value="" disabled selected>請選擇細項</option>
                                    <!-- 模擬 placeholder -->
                                    <option
                                        v-for="(option, index) in windowOptionList[currentMeasure.specification.type.piece]"
                                        :key="index" :value="option">
                                        {{ option }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                            <!-- 8mm 5mm -->
                            <div class="form-group">
                                <img src="../assets/ic_thickness.png" class="icon" />
                                <span class="icon-text">玻璃厚度</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.thick }}</a>
                                <select v-else v-model="currentMeasure.specification.type.thick" class="styled-select">
                                    <option value="" disabled selected>請選擇玻璃厚度</option>
                                    <!-- 模擬 placeholder -->
                                    <option value="8mm玻璃">8mm玻璃</option>
                                    <option value="5mm玻璃">5mm玻璃</option>
                                    <option value="複合玻璃">複合玻璃</option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                            <!-- 灰玻 茶玻 -->
                            <div class="form-group">
                                <img src="../assets/ic_glass.png" class="icon" />
                                <span class="icon-text">玻璃款式</span>
                                <a v-if="!isEdit">{{
                                    currentMeasure.specification.glass[currentMeasure.specification.type.thick]
                                }}</a>
                                <select v-else
                                    v-model="currentMeasure.specification.glass[currentMeasure.specification.type.thick]"
                                    class="styled-select">
                                    <option value="" disabled selected>請選擇玻璃款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option
                                        v-for="(type, index) in glassTypeList[currentMeasure.specification.type.thick]"
                                        :key="index" :value="type">
                                        {{ type }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                        </div>
                    </div>
                    <!-- 門 -->
                    <div v-else>
                        <!-- 要求 -->
                        <div class="form-group-container">
                            <div class="form-group">
                                <img src="../assets/ic_comments.png" class="icon" />
                                <span class="checkbox-icon-text">客戶要求</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.request.join(", ") }}</a>
                                <div v-else class="checkbox-group">
                                    <label v-for="(request, index) in doorRequestList" :key="index">
                                        <input type="checkbox" :value="request"
                                            v-model="currentMeasure.specification.request" />
                                        {{ request }}
                                    </label>
                                    <label>
                                        <input type="checkbox" v-model="requestOtherChecked" />
                                        <!-- 新增的CheckBox -->
                                        <input
                                            :class="{ 'no-border': !requestOtherChecked, 'active': requestOtherChecked }"
                                            :disabled="!requestOtherChecked"
                                            v-model="currentMeasure.specification.requestOther" placeholder="請輸入其他要求" />
                                    </label>
                                </div>
                            </div>
                            <!-- 配件 -->
                            <div class="form-group">
                                <i class="fas fa-lock"></i>
                                <span class="checkbox-icon-text">配件</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.asset.join(", ") }}</a>
                                <div v-else class="checkbox-group">
                                    <label v-for="(request, index) in doorAssetsList" :key="index">
                                        <input type="checkbox" :value="request"
                                            v-model="currentMeasure.specification.asset" />
                                        {{ request }}
                                    </label>
                                </div>
                            </div>
                            <!-- 現場補料 -->
                            <div class="form-group">
                                <img src="../assets/ic_replenish.png" class="icon" />
                                <span class="icon-text">現場補料</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.onSite }}</a>
                                <select v-else v-model="currentMeasure.specification.onSite" id="contract"
                                    class="styled-select">
                                    <option value="" disabled selected>請選擇款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(onSite, index) in doorOnSiteList" :key="index" :value="onSite">
                                        {{ onSite }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                            <!-- 門框 -->
                            <div class="form-group">
                                <img src="../assets/ic_door_frame.png" class="icon" />
                                <span class="icon-text">門框樣式</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.frame }}</a>
                                <select v-else v-model="currentMeasure.specification.type.frame" id="contract"
                                    class="styled-select">
                                    <option value="" disabled selected>請選擇款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(frame, index) in doorFramesList" :key="index" :value="frame">
                                        {{ frame }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                            <!-- 門片 -->
                            <div class="form-group">
                                <img src="../assets/ic_size.png" class="icon" />
                                <span class="icon-text">門片樣式</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.piece }}</a>
                                <select v-else v-model="currentMeasure.specification.type.piece" id="contract"
                                    class="styled-select">
                                    <option value="" disabled selected>請選擇款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option
                                        v-for="(piece, index) in doorPiecesList[currentMeasure.specification.type.frame]"
                                        :key="index" :value="piece">
                                        {{ piece }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                            <!-- 模型 -->
                            <div class="form-group">
                                <img src="../assets/ic_door.png" class="icon" />
                                <span class="icon-text">門片模式</span>
                                <a v-if="!isEdit">{{ currentMeasure.specification.type.model }}</a>
                                <select v-else v-model="currentMeasure.specification.type.model" id="contract"
                                    class="styled-select">
                                    <option value="" disabled selected>請選擇款式</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(model, index) in doorModelsList" :key="index" :value="model">
                                        {{ model }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow" v-if="isEdit"></i>
                            </div>
                        </div>
                        <!-- 模型。上 -->
                        <div v-if="currentMeasure.specification.type.model == '上下'">
                            <div class="form-group-container">
                                <div class="form-group">
                                    <img src="../assets/ic_information.png" class="icon" />
                                    <span class="icon-text">上　　</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.type.option.up }}</a>
                                    <select v-else v-model="currentMeasure.specification.type.option.up" id="contract"
                                        class="styled-select-short">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(model, index) in doorUpTypeList" :key="index" :value="model">
                                            {{ model }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                                </div>
                                <div class="form-group" v-if="
                                    currentMeasure.specification.type.option.up == '8mm玻璃' ||
                                    currentMeasure.specification.type.option.up == '5mm玻璃'
                                ">
                                    <img src="../assets/ic_glass.png" class="icon" />
                                    <span class="icon-text">上玻璃</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.glass["up"] }}</a>
                                    <select v-else v-model="currentMeasure.specification.glass['up']"
                                        class="styled-select-short">
                                        <option value="" disabled selected>請選擇上玻璃款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(type, index) in glassTypeList[
                                            currentMeasure.specification.type.option.up
                                        ]" :key="index" :value="type">
                                            {{ type }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow-short" v-if="isEdit"></i>
                                </div>
                                <!-- 模型。下 -->
                                <div class="form-group">
                                    <img src="../assets/ic_information.png" class="icon" />
                                    <span class="icon-text">下　　</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.type.option.down }}</a>
                                    <div v-else>
                                        <span v-if="
                                            currentMeasure.specification.type.option.up == '混色花板' ||
                                            currentMeasure.specification.type.option.up == '全門花' ||
                                            currentMeasure.specification.type.option.up == '全百葉片(700型葉片)'
                                        ">{{ currentMeasure.specification.type.option.up }}</span>
                                        <select v-else v-model="currentMeasure.specification.type.option.down"
                                            id="contract" class="styled-select-short">
                                            <option value="" disabled selected>請選擇款式</option>
                                            <!-- 模擬 placeholder -->
                                            <option v-for="(model, index) in doorDownTypeList" :key="index"
                                                :value="model">
                                                {{ model }}
                                            </option>
                                        </select>
                                        <i class="fas fa-chevron-down select-arrow-short" v-if="
                                            isEdit &&
                                            !['混色花板', '全門花', '全百葉片(700型葉片)'].includes(
                                                currentMeasure.specification.type.option.up
                                            )
                                        ">
                                        </i>
                                    </div>
                                </div>
                                <div class="form-group" v-if="
                                    !['混色花板', '全門花', '全百葉片(700型葉片)'].includes(
                                        currentMeasure.specification.type.option.up
                                    ) &&
                                    (currentMeasure.specification.type.option.down == '8mm玻璃' ||
                                        currentMeasure.specification.type.option.down == '5mm玻璃')
                                ">
                                    <img src="../assets/ic_glass.png" class="icon" />
                                    <span class="icon-text">下玻璃</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.glass["down"] }}</a>
                                    <select v-else v-model="currentMeasure.specification.glass['down']"
                                        class="styled-select-short">
                                        <option value="" disabled selected>請選擇下玻璃款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(type, index) in glassTypeList[
                                            currentMeasure.specification.type.option.down
                                        ]" :key="index" :value="type">
                                            {{ type }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow-short" v-if="
                                        isEdit &&
                                        !['混色花板', '全門花', '全百葉片(700型葉片)'].includes(
                                            currentMeasure.specification.type.option.up
                                        )
                                    ">
                                    </i>
                                </div>
                            </div>
                        </div>
                        <!-- 模型。上中下 -->
                        <div v-else>
                            <div class="form-group-container">
                                <div class="form-group">
                                    <img src="../assets/ic_information.png" class="icon" />
                                    <span class="icon-text">上</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.type.option.up }}</a>
                                    <a v-else>花格網</a>
                                </div>
                                <div class="form-group">
                                    <img src="../assets/ic_information.png" class="icon" />
                                    <span class="icon-text">中</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.type.option.middle }}</a>
                                    <a v-else>花格網</a>
                                </div>
                                <div class="form-group">
                                    <img src="../assets/ic_information.png" class="icon" />
                                    <span class="icon-text">下</span>
                                    <a v-if="!isEdit">{{ currentMeasure.specification.type.option.down }}</a>
                                    <a v-else>花板</a>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="button-group">
                        <button class="btn_clear" v-if="!isEdit" @click="deleteMeasure">刪除</button>
                        <button class="btn_clear" v-if="isEdit" @click="cancelEdit">取消</button>

                        <!-- 點擊時調用 deleteMeasure -->
                        <button class="btn edit" v-if="!isEdit" @click="startEditing">編輯</button>
                        <button class="btn fin" v-if="isEdit" @click="putMeasure">確認</button>
                    </div>
                </div>
            </div>
            <!-- 新增數據模態框 -->
            <div v-if="showCreateMeasure" class="modal-overlay">
                <div class="modal-content" @click.stop>
                    <h2>
                        新增數據
                        <span class="close-btn" @click="closeCreateMeasure">×</span>
                    </h2>
                    <form class="form">
                        <div class="form-group-container">
                            <div class="form-group">
                                <img src="../assets/ic_date.png" class="icon" />
                                <span class="icon-text">測量日期</span>
                                <input v-model="newMeasureDate" placeholder="yyyy-mm-dd" />
                            </div>
                            <div class="form-group">
                                <img src="../assets/ic_house.png" class="icon" />
                                <span class="icon-text">位置</span>
                                <input v-model="newPotition" placeholder="樓層與位置" />
                            </div>
                            <div class="form-group">
                                <img src="../assets/ic_windows.png" class="icon" />
                                <span class="icon-text">門窗</span>
                                <select v-model="newWindoor" id="contract" class="styled-select-short">
                                    <option value="" disabled selected>請選擇門框或窗框</option>
                                    <!-- 模擬 placeholder -->
                                    <option>門</option>
                                    <option>窗</option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow-short"></i>
                            </div>
                            <div class="form-group">
                                <i class="fas fa-list-ol"></i>
                                <span class="icon-text">數量</span>
                                <input v-model="newQuantity" />
                            </div>
                            <div class="form-group">
                                <img src="../assets/ic_width.png" class="icon" />
                                <span class="icon-text">寬度</span>
                                <input v-model="newWidth" placeholder="小數點後一位" />
                            </div>
                            <div class="form-group">
                                <img src="../assets/ic_height.png" class="icon" />
                                <span class="icon-text">高度</span>
                                <input v-model="newLength" placeholder="小數點後一位" />
                            </div>
                            <!-- 顏色 -->
                            <div class="form-group">
                                <img src="../assets/ic_color.png" class="icon" />
                                <span class="icon-text">顏色</span>
                                <select v-model="newColor" id="color-select" class="styled-select-short">
                                    <option value="" disabled selected>請選擇顏色</option>
                                    <!-- 模擬 placeholder -->
                                    <option v-for="(color, index) in colorList" :key="index" :value="color">
                                        {{ color }}
                                    </option>
                                </select>
                                <i class="fas fa-chevron-down select-arrow-short"></i>
                            </div>
                            <!-- 牆體 -->
                            <div class="form-group">
                                <img src="../assets/ic_wall.png" class="icon" />
                                <span class="checkbox-icon-text">牆體</span>
                                <div class="checkbox-group">
                                    <label v-for="(wall, index) in wallList" :key="index">
                                        <input type="checkbox" :value="wall" v-model="newWall" />
                                        {{ wall }}
                                    </label>
                                </div>
                            </div>
                        </div>
                        <!-- 窗框 -->
                        <div v-if="newWindoor == '窗'">
                            <!-- 客戶要求 -->
                            <div class="form-group-container">
                                <div class="form-group">
                                    <img src="../assets/ic_comments.png" class="icon" />
                                    <span class="checkbox-icon-text">客戶要求</span>
                                    <div class="checkbox-group">
                                        <label v-for="(request, index) in windowRequestList" :key="index">
                                            <input type="checkbox" :value="request" v-model="newRequest" />
                                            {{ request }}
                                        </label>
                                        <label>
                                            <input type="checkbox" v-model="newRequestOtherChecked" />
                                            <!-- 新增的CheckBox -->
                                            <input :disabled="!newRequestOtherChecked"
                                                :class="{ 'no-border': !newRequestOtherChecked, 'active': newRequestOtherChecked }"
                                                v-model="newRequestOther" placeholder="請輸入其他要求" />
                                        </label>
                                    </div>
                                </div>
                                <!-- 234拉款式 -->
                                <div class="form-group">
                                    <img src="../assets/ic_open_window.png" class="icon" />
                                    <span class="icon-text">窗框款式</span>
                                    <select v-model="newWindowType" id="contract" class="styled-select">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(type, index) in windowTypeList" :key="index" :value="type">
                                            {{ type }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                                <div class="form-group">
                                    <img src="../assets/ic_information.png" class="icon" />
                                    <span class="icon-text">窗框細項</span>
                                    <select v-model="newWindowOption" class="styled-select">
                                        <option value="" disabled selected>請選擇細項</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(option, index) in windowOptionList[newWindowType]" :key="index"
                                            :value="option">
                                            {{ option }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                                <!-- 玻璃厚度 -->
                                <div class="form-group">
                                    <img src="../assets/ic_thickness.png" class="icon" />
                                    <label class="icon-text">玻璃厚度</label>
                                    <select v-model="newGlassThick" class="styled-select">
                                        <option value="" disabled selected>請選擇玻璃厚度</option>
                                        <!-- 模擬 placeholder -->
                                        <option value="8mm玻璃">8mm玻璃</option>
                                        <option value="5mm玻璃">5mm玻璃</option>
                                        <option value="複合玻璃">複合玻璃</option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                                <div class="form-group">
                                    <img src="../assets/ic_glass.png" class="icon" />
                                    <label class="icon-text">玻璃款式</label>
                                    <select v-model="newGlassType" class="styled-select">
                                        <option value="" disabled selected>請選擇玻璃款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(type, index) in glassTypeList[newGlassThick]" :key="index"
                                            :value="type">
                                            {{ type }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                            </div>
                        </div>
                        <!-- 門框 -->
                        <div v-if="newWindoor == '門'">
                            <!-- 客戶要求 -->
                            <div class="form-group-container">
                                <div class="form-group">
                                    <img src="../assets/ic_comments.png" class="icon" />
                                    <span class="checkbox-icon-text">客戶要求</span>
                                    <div class="checkbox-group">
                                        <label v-for="(request, index) in doorRequestList" :key="index">
                                            <input type="checkbox" :value="request" v-model="newRequest" />
                                            {{ request }}
                                        </label>
                                        <label>
                                            <input type="checkbox" v-model="newRequestOtherChecked" />
                                            <!-- 新增的CheckBox -->
                                            <input :disabled="!newRequestOtherChecked"
                                                :class="{ 'no-border': !newRequestOtherChecked, 'active': newRequestOtherChecked }"
                                                v-model="newRequestOther" placeholder="請輸入其他要求" />
                                        </label>
                                    </div>
                                </div>
                                <!-- 門配件 -->
                                <div class="form-group">
                                    <i class="fas fa-lock"></i>
                                    <span class="checkbox-icon-text">配件</span>
                                    <div class="checkbox-group">
                                        <label v-for="(asset, index) in doorAssetsList" :key="index">
                                            <input type="checkbox" :value="asset" v-model="newAsset" />
                                            {{ asset }}
                                        </label>
                                    </div>
                                </div>
                                <!-- 門現場補料 -->
                                <div class="form-group">
                                    <img src="../assets/ic_replenish.png" class="icon" />
                                    <span class="icon-text">現場補料</span>
                                    <select v-model="newOnSite" id="contract" class="styled-select">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(onSite, index) in doorOnSiteList" :key="index" :value="onSite">
                                            {{ onSite }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                                <!-- 門框 -->
                                <div class="form-group">
                                    <img src="../assets/ic_door_frame.png" class="icon" />
                                    <span class="icon-text">門框樣式</span>
                                    <select v-model="newDoorFrame" id="contract" class="styled-select">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(frame, index) in doorFramesList" :key="index" :value="frame">
                                            {{ frame }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                                <!-- 門片 -->
                                <div class="form-group">
                                    <img src="../assets/ic_size.png" class="icon" />
                                    <span class="icon-text">門片樣式</span>
                                    <select v-model="newDoorPiece" id="contract" class="styled-select">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(piece, index) in doorPiecesList[newDoorFrame]" :key="index"
                                            :value="piece">
                                            {{ piece }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                                <!-- 門模式 -->
                                <div class="form-group">
                                    <img src="../assets/ic_door.png" class="icon" />
                                    <span class="icon-text">門片模式</span>
                                    <select v-model="newDoorModel" id="contract" class="styled-select">
                                        <option value="" disabled selected>請選擇款式</option>
                                        <!-- 模擬 placeholder -->
                                        <option v-for="(model, index) in doorModelsList" :key="index" :value="model">
                                            {{ model }}
                                        </option>
                                    </select>
                                    <i class="fas fa-chevron-down select-arrow"></i>
                                </div>
                            </div>
                            <!-- 門模式-上下 -->
                            <div v-if="newDoorModel == '上下'">
                                <!-- 上模式 -->
                                <div class="form-group-container">
                                    <div class="form-group">
                                        <img src="../assets/ic_information.png" class="icon" />
                                        <span class="icon-text">上　　</span>
                                        <select v-model="newGlassUpType" id="contract" class="styled-select-short">
                                            <option value="" disabled selected>請選擇款式</option>
                                            <!-- 模擬 placeholder -->
                                            <option v-for="(model, index) in doorUpTypeList" :key="index"
                                                :value="model">
                                                {{ model }}
                                            </option>
                                        </select>
                                        <i class="fas fa-chevron-down select-arrow-short"></i>
                                    </div>
                                    <!-- 上玻璃厚度 -->
                                    <div class="form-group"
                                        v-if="newGlassUpType == '8mm玻璃' || newGlassUpType == '5mm玻璃' || newGlassUpType == '複合玻璃'">
                                        <img src="../assets/ic_glass.png" class="icon" />
                                        <span class="icon-text">上玻璃</span>
                                        <select v-model="newGlassUpOption" class="styled-select-short">
                                            <option value="" disabled selected>請選擇玻璃款式</option>
                                            <!-- 模擬 placeholder -->
                                            <option v-for="(type, index) in glassTypeList[newGlassUpType]" :key="index"
                                                :value="type">
                                                {{ type }}
                                            </option>
                                        </select>
                                        <i class="fas fa-chevron-down select-arrow-short"></i>
                                    </div>
                                    <!-- 下模式 -->
                                    <div class="form-group">
                                        <img src="../assets/ic_information.png" class="icon" />
                                        <span class="icon-text">下　　</span>
                                        <span v-if="
                                            newGlassUpType == '混色花板' ||
                                            newGlassUpType == '全門花' ||
                                            newGlassUpType == '全百葉片(700型葉片)'
                                        ">{{ newGlassUpType }}</span>
                                        <select v-else v-model="newGlassDownType" id="contract"
                                            class="styled-select-short">
                                            <option value="" disabled selected>請選擇款式</option>
                                            <!-- 模擬 placeholder -->
                                            <option v-for="(model, index) in doorDownTypeList" :key="index"
                                                :value="model">
                                                {{ model }}
                                            </option>
                                        </select>
                                        <i class="fas fa-chevron-down select-arrow-short" v-if="
                                            !(
                                                newGlassUpType == '混色花板' ||
                                                newGlassUpType == '全門花' ||
                                                newGlassUpType == '全百葉片(700型葉片)'
                                            )
                                        ">
                                        </i>
                                    </div>
                                    <!-- 下玻璃厚度 -->
                                    <div class="form-group"
                                        v-if="newGlassDownType == '8mm玻璃' || newGlassDownType == '5mm玻璃' || newGlassDownType == '複合玻璃'">
                                        <img src="../assets/ic_glass.png" class="icon" />
                                        <span class="icon-text">下玻璃</span>
                                        <select v-model="newGlassDownOption" class="styled-select-short">
                                            <option value="" disabled selected>請選擇玻璃款式</option>
                                            <!-- 模擬 placeholder -->
                                            <option v-for="(type, index) in glassTypeList[newGlassDownType]"
                                                :key="index" :value="type">
                                                {{ type }}
                                            </option>
                                        </select>
                                        <i class="fas fa-chevron-down select-arrow-short"></i>
                                    </div>
                                </div>
                            </div>
                            <!-- 門模式-上中下 -->
                            <div class="form-group" v-if="newDoorModel == '上中下'">
                                <div class="form-group-container">
                                    <div class="form-group">
                                        <img src="../assets/ic_information.png" class="icon" />
                                        <span class="icon-text">上</span>
                                        <a>花格網</a>
                                    </div>
                                    <div class="form-group">
                                        <img src="../assets/ic_information.png" class="icon" />
                                        <span class="icon-text">中</span>
                                        <a>花格網</a>
                                    </div>
                                    <div class="form-group">
                                        <img src="../assets/ic_information.png" class="icon" />
                                        <span class="icon-text">下</span>
                                        <a>花板</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div class="button-group">
                            <button type="reset" class="btn_clear" @click="clearCreate">清空</button>
                            <button type="button" class="btn add" @click="createMeasure">新增</button>
                        </div>
                    </form>
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
                            <img v-if="exportTo == '加工數據'" src="../assets/ic_wrench.png" class="icon1" />
                            <img v-if="exportTo == '報價單'" src="../assets/ic_report.png" class="icon1" />
                            <select v-model="exportTo" id="contract" class="styled-select-short">
                                <option value="" disabled>請選擇輸出目標</option>
                                <option>加工數據</option>
                                <option>報價單</option>
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
import { ref, computed, watch, onMounted } from "vue";
import measureList from "./List/measureList.vue";
import { postMeasure, postMeasureNewMeasure, putMeasureMeasureId, deleteMeasureMeasureId, getDownloadQuotation, postNewMachine } from "../services/api";

export default {
    components: {
        measureList,
    },
    props: {
        client_address: String,
        machineMeasureId: String,
        clientInfo: Object
    },
    setup(props, { emit }) {
        // 取得客戶頁面設定
        const client_address = ref("");
        client_address.value = props.client_address;
        const measureData = ref([]);
        function getAllMeasureViaClientAddress() {
            postMeasure(client_address.value).then((resp) => {
                measureData.value = resp.measure;
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        }
        getAllMeasureViaClientAddress()
        const loadMeasureData = async () => {
            const resp = await postMeasure(client_address.value);
            measureData.value = resp.measure;
        };
        onMounted(loadMeasureData);
        const searchQuery = ref("");
        function clearSearch() {
            searchQuery.value = null;
        }
        // 根據搜尋欄輸入值進行篩選的計算屬性
        const filteredMeasures = computed(() => {
            if (!searchQuery.value) {
                return measureData.value; // 如果搜尋欄是空的，返回全部資料
            }
            const query = searchQuery.value.trim().toLowerCase();

            return measureData.value.filter((measure) => {
                const spec = measure.specification;

                // 定義一個函數來安全處理欄位的型態轉換與比對
                const matchesQuery = (field) => {
                    if (field === null || field === undefined) {
                        return false;
                    }
                    if (typeof field === "string") {
                        return field.toLowerCase().includes(query);
                    } else if (typeof field === "number") {
                        return field.toString().includes(query);
                    }
                    return false;
                };

                // 遞迴搜尋函數，處理specification物件中的資料
                const searchInObject = (obj) => {
                    if (!obj || typeof obj !== "object") return false;
                    return Object.values(obj).some((value) => {
                        if (value === null || value === undefined) {
                            return false;
                        } else if (typeof value === "string") {
                            return value.toLowerCase().includes(query);
                        } else if (Array.isArray(value)) {
                            return value.some(
                                (item) =>
                                    item !== null && item !== undefined && item.toString().toLowerCase().includes(query)
                            );
                        } else if (typeof value === "object") {
                            return searchInObject(value);
                        }
                        return false;
                    });
                };

                // 比對主要欄位和specification中的內容
                return (
                    matchesQuery(measure.length) ||
                    matchesQuery(measure.width) ||
                    matchesQuery(measure.measure_date) ||
                    matchesQuery(measure.position) ||
                    matchesQuery(measure.quantity) ||
                    searchInObject(spec)
                ); // 搜尋specification物件
            });
        });

        function formatDate(dateString) {
            return format(new Date(dateString), "yyyy-MM-dd");
        }
        function updateSelected(measure) {
            const target = measureData.value.find((item) => item.measure_id == measure.measure_id);
            if (target) {
                target.selected = measure.selected;
            }
        }
        const allSelected = computed(
            () => measureData.value.length > 0 && measureData.value.every((item) => item.selected)
        );
        const toggleSelectAll = () => {
            const newState = !allSelected.value;
            measureData.value.forEach((item) => (item.selected = newState));
        };
        const showExport = ref(false);
        const exportTo = ref("")
        function openExport() {
            showExport.value = true
        }
        function closeExport() {
            showExport.value = false;
            exportTo.value = ""
        }
        function exportData() {
            const selectedMeasureIds = measureData.value
                .filter(item => item.selected === true) // 過濾出 selected 為 true 的資料
                .map(item => item.measure_id); // 提取 measure_id
            if (selectedMeasureIds.length == 0) {
                alert("請選擇要輸出的資料")
            } else {
                switch (exportTo.value) {
                    case "報價單":
                        getDownloadQuotation(selectedMeasureIds)
                        break;
                    case "加工數據":
                        postNewMachine(selectedMeasureIds).then().catch(err => {
                            console.error(err.response.data.error)
                            alert(err.response.data.error)
                        })
                        break
                }
            }
            closeExport()
        }
        const colorList = ref(["咖啡色", "牙白色", "純白色", "香檳色", "鐵灰色", "黑色霧面"]);
        const windowRequestList = ref(["包料", "拆舊框不清運", "要清運", "現場組裝", "做交", "包裝", "鎖鐵片"]);
        const doorRequestList = ref([
            "包料",
            "拆舊框不清運",
            "要清運",
            "框加門-只做門-包裝",
            "現場組裝",
            "外看外開",
            "內看內開",
        ]);
        const wallList = ref(["白鐵", "鋼構場", "磚牆", "混凝土", "木造"]);
        const doorAssetsList = ref([
            "哈樂",
            "門縫條",
            "無鎖",
            "有鎖",
            "喇叭鎖(60)",
            "喇叭鎖(85加蓋)",
            "127長",
            "白鐵門栓",
            "門弓器",
            "門檔",
            "毛刷蓋",
            "N字型",
        ]);
        const doorOnSiteList = ref(["料號4107A花板", "料號6205 - 3×6角鐵", "料號6214 - 2.5×3.8角鐵", "料號6202 - 2×2角鐵"]);
        const windowTypeList = ref(["2拉", "3拉", "4拉"]);
        const windowOptionList = {
            "2拉": ["866", "866水泥窗", "1066", "1068", "1066-866流理台反裝", "1066加1200型鎖"],
            "3拉": ["866", "1066", "1068"],
            "4拉": ["866", "1066", "1068"],
        };
        const glassTypeList = {
            "8mm玻璃": ["黑玻", "綠玻", "反射綠", "銀霞", "5光", "噴砂不沾手", "茶玻", "灰玻", "壓克力 8mm"],
            "5mm玻璃": ["方格", "黑玻", "綠玻", "反射綠", "銀霞", "5光", "噴砂不沾手", "茶玻", "灰玻", "壓克力 5mm"],
            "複合玻璃": [
                "膠合5光+5光T",
                "膠合5光+反射綠T",
                "膠合5光+茶玻T",
                "膠合5光+黑玻T",
                "膠合5光+噴砂T",
                "5光+綠玻T",
                "5光+灰玻T",
            ],
        };
        const doorFramesList = ref(["L型兩孔門框", "7公分", "3×10框偏領", "3×10框中領", "4.5×10框中領"]);
        const doorPiecesList = ref({
            "L型": ["700型"],
            "7公分": ["700型", "700型(700加大)"],
            "3×10框偏領": ["700型", "700型(700加大)"],
            "3×10框中領": ["1000型"],
            "4.5×10框中領": ["1000型"],
        });
        const doorModelsList = ref(["上下", "上中下"]);
        const doorUpTypeList = ref([
            "混色花板",
            "全門花",
            "全百葉片",
            "8mm玻璃",
            "5mm玻璃",
            "複合玻璃",
            "花板",
            "門花",
            "上百葉",
        ]);
        const doorDownTypeList = ref(["8mm玻璃", "5mm玻璃", "複合玻璃", "花板", "下百葉"]);

        // 數據資訊頁設定
        const isEdit = ref(false);
        const showMeasureId = ref(false);
        const currentMeasure = ref({});
        const openMeasureId = (measure_id) => {
            currentMeasure.value = measureData.value.find((item) => item.measure_id === measure_id);
            currentMeasure.value.measure_date = formatDate(currentMeasure.value.measure_date);
            if (currentMeasure.value) {
                showMeasureId.value = true;
            }
        };
        const closeMeasureId = () => {
            isEdit.value = false;
            getAllMeasureViaClientAddress()
            showMeasureId.value = false;
        };
        const hasOpenedMeasure = ref(false);
        const machineMeasureId = props.machineMeasureId
        watch(
            () => measureData.value,
            (newVal) => {
                // 只有在 measureData 有數據且 machineMeasureId 存在，並且尚未打開過時才執行
                if (newVal.length > 0 && machineMeasureId && !hasOpenedMeasure.value) {
                    openMeasureId(machineMeasureId);
                    hasOpenedMeasure.value = true; // 設定為 true，防止再次觸發
                }
            }
        );
        // 定義備份數據，用來恢復編輯前的值
        const original_measure = {};
        function requestOtherValue() {
            if (currentMeasure.value.specification.windoor == "窗") {
                return currentMeasure.value.specification.request.filter(
                    (item) => !windowRequestList.value.includes(item)
                ).join(", ");
            } else {
                return currentMeasure.value.specification.request.filter(
                    (item) => !doorRequestList.value.includes(item)
                ).join(", ");
            }
        }
        function requestValue() {
            const filteredRequest = currentMeasure.value.specification.request.filter(
                (item) =>
                    (currentMeasure.value.specification.windoor === "窗"
                        ? windowRequestList.value.includes(item)
                        : doorRequestList.value.includes(item)) && item !== currentMeasure.value.specification.requestOther
            );
            return filteredRequest;
        }
        const startEditing = () => {
            Object.assign(original_measure, currentMeasure.value);
            currentMeasure.value.specification.requestOther = requestOtherValue();
            currentMeasure.value.specification.request = requestValue();
            isEdit.value = true; // 進入編輯模式
        };
        const cancelEdit = () => {
            currentMeasure.value = { ...original_measure };
            isEdit.value = false; // 退出編輯模式
        };
        const requestOtherChecked = ref(false)
        const updateSpecification = () => {
            // 共用的屬性
            const baseSpecification = {
                windoor: currentMeasure.value.specification.windoor || "", // 防止 null/undefined
                color: currentMeasure.value.specification.color || "",
                request: currentMeasure.value.specification.request || [],
                wall: currentMeasure.value.specification.wall || [],
            };
            // 窗的處理
            if (currentMeasure.value.specification.windoor === "窗") {
                return {
                    ...baseSpecification,
                    type: {
                        piece: currentMeasure.value.specification.type.piece || "",
                        option: currentMeasure.value.specification.type.option || "",
                        thick: currentMeasure.value.specification.type.thick || "",
                    },
                    glass: {
                        [currentMeasure.value.specification.type.thick]:
                            currentMeasure.value.specification.glass[currentMeasure.value.specification.type.thick],
                    },
                };
            }
            // 門框的額外處理
            if (currentMeasure.value.specification.type.model === "上下") {
                let glass = {};
                // 設定上玻璃選項
                if (
                    currentMeasure.value.specification.type.option.up === "8mm玻璃" ||
                    currentMeasure.value.specification.type.option.up === "5mm玻璃"
                ) {
                    glass["up"] = currentMeasure.value.specification.glass["up"];
                }
                // 設定下玻璃選項
                if (
                    currentMeasure.value.specification.type.option.down === "8mm玻璃" ||
                    currentMeasure.value.specification.type.option.down === "5mm玻璃"
                ) {
                    glass["down"] = currentMeasure.value.specification.glass["down"];
                }
                if (
                    currentMeasure.value.specification.type.option.up == "混色花板" ||
                    currentMeasure.value.specification.type.option.up == "全門花" ||
                    currentMeasure.value.specification.type.option.up == "全百葉片(700型葉片)"
                ) {
                    newGlassDownType.value = newGlassUpType.value;
                }
                return {
                    ...baseSpecification,
                    type: {
                        frame: currentMeasure.value.specification.type.frame,
                        piece: currentMeasure.value.specification.type.piece,
                        model: currentMeasure.value.specification.type.model,
                        option: {
                            up: currentMeasure.value.specification.type.option.up,
                            down: currentMeasure.value.specification.type.option.down,
                        },
                    },
                    glass: glass,
                    asset: currentMeasure.value.specification.asset,
                    onSite: currentMeasure.value.specification.onSite,
                };
            }
            // 上中下模式
            return {
                ...baseSpecification,
                asset: currentMeasure.value.specification.asset,
                onSite: currentMeasure.value.specification.onSite,
                type: {
                    frame: currentMeasure.value.specification.type.frame,
                    piece: currentMeasure.value.specification.type.piece,
                    model: currentMeasure.value.specification.type.model,
                    option: {
                        up: "花格網",
                        middle: "花格網",
                        down: "花板",
                    },
                },
                glass: {},
            };
        };
        // 修改數據
        const putMeasure = () => {
            if (requestOtherChecked.value) {
                currentMeasure.value.specification.request = currentMeasure.value.specification.request.concat(currentMeasure.value.specification.requestOther);
            }
            const specification = updateSpecification();
            putMeasureMeasureId({
                measure_id: currentMeasure.value.measure_id,
                client_address: client_address.value,
                length: currentMeasure.value.length,
                width: currentMeasure.value.width,
                specification: specification,
                measure_date: formatDate(currentMeasure.value.measure_date),
                position: currentMeasure.value.position,
                quantity: currentMeasure.value.quantity,
            }).then((res) => {
                isEdit.value = false;
                currentMeasure.value.specification.requestOther = false
                getAllMeasureViaClientAddress()
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        };
        // 刪除數據資料
        const deleteMeasure = () => {
            if (confirm("你確定要刪除這份數據嗎？")) {
                deleteMeasureMeasureId(currentMeasure.value.measure_id).then((resp) => {
                    showMeasureId.value = false;
                    getAllMeasureViaClientAddress()
                }).catch(err => {
                    console.error(err.response.data.error)
                    alert(err.response.data.error)
                });
            }
        };

        // 新增客戶頁面設定
        const newMeasureDate = ref(""); //基礎
        const newWindoor = ref("");
        const newPotition = ref("");
        const newWidth = ref("");
        const newLength = ref("");
        const newQuantity = ref("");
        const newColor = ref("");
        const newRequest = ref([]);
        const newRequestOtherChecked = ref(false)
        const newRequestOther = ref("");
        const newWall = ref([]);
        const newAsset = ref([]);
        const newOnSite = ref("");
        const newWindowType = ref(""); //窗
        const newWindowOption = ref("");
        const newGlassThick = ref("");
        const newGlassType = ref("");
        const newDoorFrame = ref(""); //門
        const newDoorPiece = ref("");
        const newDoorModel = ref("");
        const newGlassUpType = ref("");
        const newGlassUpOption = ref("");
        const newGlassDownType = ref("");
        const newGlassDownOption = ref("");
        const showCreateMeasure = ref(false);
        const openCreateMeasure = () => {
            const now = new Date();
            newMeasureDate.value = format(now, "yyyy-MM-dd");
            newWindoor.value = null;
            newPotition.value = null;
            newWidth.value = null;
            newLength.value = null;
            newQuantity.value = null;
            newColor.value = null;
            newRequest.value = [];
            newRequestOtherChecked.value = false
            newRequestOther.value = null;
            newWall.value = [];
            newAsset.value = [];
            newOnSite.value = null;
            newWindowType.value = null;
            newWindowOption.value = null;
            newGlassThick.value = null;
            newGlassType.value = null;
            newDoorFrame.value = null;
            newDoorPiece.value = null;
            newDoorModel.value = null;
            newGlassUpType.value = null;
            newGlassUpOption.value = null;
            newGlassDownType.value = null;
            newGlassDownOption.value = null;
            showCreateMeasure.value = true;
        };
        const closeCreateMeasure = () => {
            showCreateMeasure.value = false;
        };
        const createSpecification = () => {
            // 共用的屬性
            const baseSpecification = {
                windoor: newWindoor.value || "", // 防止 null/undefined
                color: newColor.value || "",
                request: newRequest.value || [],
                wall: newWall.value || [],
            };
            // 窗的處理
            if (newWindoor.value === "窗") {
                return {
                    ...baseSpecification,
                    type: {
                        piece: newWindowType.value || "",
                        option: newWindowOption.value || "",
                        thick: newGlassThick.value || "",
                    },
                    glass: {
                        [newGlassThick.value]: newGlassType.value,
                    },
                };
            }
            if (
                newGlassUpType.value == "混色花板" ||
                newGlassUpType.value == "全門花" ||
                newGlassUpType.value == "全百葉片(700型葉片)"
            ) {
                newGlassDownType.value = newGlassUpType.value;
            }
            // 門框的額外處理
            if (newDoorModel.value === "上下") {
                let glass = {};
                // 設定上玻璃選項
                if (newGlassUpType.value === "8mm玻璃" || newGlassUpType.value === "5mm玻璃") {
                    glass["up"] = newGlassUpOption.value;
                }
                // 設定下玻璃選項
                if (newGlassDownType.value === "8mm玻璃" || newGlassDownType.value === "5mm玻璃") {
                    glass["down"] = newGlassDownOption.value;
                }
                return {
                    ...baseSpecification,
                    type: {
                        frame: newDoorFrame.value,
                        piece: newDoorPiece.value,
                        model: newDoorModel.value,
                        option: {
                            up: newGlassUpType.value,
                            // middle: "花格網", // 根據需求可加入此選項
                            down: newGlassDownType.value,
                        },
                    },
                    glass: glass,
                    asset: newAsset.value,
                    onSite: newOnSite.value,
                };
            }
            // 上中下模式
            return {
                ...baseSpecification,
                asset: newAsset.value,
                onSite: newOnSite.value,
                type: {
                    frame: newDoorFrame.value,
                    piece: newDoorPiece.value,
                    model: newDoorModel.value,
                    option: {
                        up: "花格網",
                        middle: "花格網",
                        down: "花板",
                    },
                },
                glass: {},
            };
        };

        // 新增客戶
        const createMeasure = () => {
            if (newRequestOtherChecked.value) {
                newRequest.value = newRequest.value.concat(newRequestOther.value);
            }
            const specification = createSpecification();
            postMeasureNewMeasure({
                client_address: client_address.value,
                length: newLength.value,
                width: newWidth.value,
                specification: specification,
                measure_date: formatDate(newMeasureDate.value),
                position: newPotition.value,
                quantity: newQuantity.value,
            }).then((resp) => {
                getAllMeasureViaClientAddress()
                showCreateMeasure.value = false;
            }).catch(err => {
                console.error(err.response.data.error)
                alert(err.response.data.error)
            });
        };
        function clearCreate() {
            newWindoor.value = null;
        }

        return {
            client_address, //List設定
            measureData,
            searchQuery,
            clearSearch,
            filteredMeasures,
            formatDate,
            updateSelected,
            allSelected,
            toggleSelectAll,
            colorList,
            windowRequestList,
            doorRequestList,
            wallList,
            doorAssetsList,
            doorOnSiteList,
            windowTypeList,
            doorFramesList,
            doorPiecesList,
            doorModelsList,
            doorUpTypeList,
            doorDownTypeList,
            windowOptionList,
            glassTypeList,
            showMeasureId, //資訊框設定
            currentMeasure,
            isEdit,
            openMeasureId,
            closeMeasureId,
            startEditing,
            cancelEdit,
            requestOtherChecked,
            putMeasure,
            deleteMeasure,
            newWindoor, //新增設定
            newLength,
            newWidth,
            newMeasureDate,
            newPotition,
            newGlassThick,
            newGlassType,
            newGlassUpOption,
            newGlassDownOption,
            newQuantity,
            newColor,
            newRequest,
            newRequestOtherChecked,
            newRequestOther,
            newWall,
            newAsset,
            newOnSite,
            newWindowType,
            newDoorFrame,
            newDoorPiece,
            newDoorModel,
            newGlassUpType,
            newGlassDownType,
            newWindowOption,
            showCreateMeasure,
            openCreateMeasure,
            closeCreateMeasure,
            createMeasure,
            clearCreate,
            showExport, // 數據輸出
            openExport,
            closeExport,
            exportTo,
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
    width: 400px; /* 額外的寬度樣式 */
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
    padding: 10px;
    margin-bottom: 15px;
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

.checkbox-icon-text {
    font-weight: bold;
    margin-right: 10px;
    font-size: 16px;
    flex-shrink: 0;
    /* 防止文字縮小 */
    text-align: left;
    /* 向左對齊 */
    width: 35px;
    /* 設定固定寬度 */
}

/* 美化下拉選單 */
.styled-select {
    width: 250px;
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
    top: 49%;
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
.styled-select:focus+.select-arrow {
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
    width: 272px;
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
.styled-select-short:focus+.select-arrow-short {
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

.checkbox-group {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    /* 每個選項之間增加間距 */
    margin-top: 5px;
    max-width: auto;
}

.checkbox-group label {
    background: linear-gradient(135deg, #d4e4f8, #dfedf8);
    padding: 8px 14px;
    border-radius: 25px;
    /* 圓角設計 */
    font-size: 14px;
    cursor: pointer;
    display: flex;
    align-items: center;
    transition: all 0.3s ease;
    box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
    /* 增加輕微陰影 */
}

.checkbox-group label:hover {
    background: linear-gradient(135deg, #d8e2ec, #c8d4e0);
    ;
}

.checkbox-group input[type="checkbox"] {
    -webkit-appearance: none;
    /* 移除瀏覽器的預設外觀 */
    appearance: none;
    margin-right: 8px;
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

.checkbox-group input[type="checkbox"]:checked {
    background-color: #42a5f5;
    /* 勾選時的背景顏色 */
    border: 2px solid #42a5f5;
    /* 勾選時的邊框顏色 */
    position: relative;
}

.checkbox-group input[type="checkbox"]:checked::after {
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
/* 隱藏邊框和陰影 */
.no-border {
    border: none !important;
    /* 強制移除邊框 */
    box-shadow: none !important;
    /* 強制移除陰影 */
    background-color: transparent !important;
    /* 背景設置為透明 */
    outline: none !important;
    /* 移除任何輪廓 */
}

/* 勾選後顯示的邊框和陰影 */
.active {
    border: 2px solid #42a5f5 !important;
    /* 顯示邊框 */
    box-shadow: 0 0 5px #42a5f5 !important;
    /* 顯示陰影 */
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
    background-color: #9575CD;
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
