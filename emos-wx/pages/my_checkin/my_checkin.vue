<template>
	<view class="page">
		<view class="statistics">
			<image src="../../static/big-icon-1.png" class="big-icon" mode="widthFix" />
			<view class="report-title">
				<text class="days">{{ normal + late }}</text>
				<text class="unit">天</text>
			</view>
			<view class="sub-title">
				<text>本月签到</text>
			</view>
			<view class="report">
				<view class="column green">
					<text class="column-title">正常</text>
					<text class="number">{{ normal }}</text>
				</view>
				<view class="column orange">
					<text class="column-title">迟到</text>
					<text class="number">{{ late }}</text>
				</view>
				<view class="column red">
					<text class="column-title">缺勤</text>
					<text class="number">{{ lack }}</text>
				</view>
			</view>
		</view>
		<view class="calendar-container">
			<uni-calendar :insert="true" :lunar="false" :selected="list" @monthSwitch="changeMonth" @confirm="changeMonth" @backtoday="changeMonth" />
		</view>
	</view>
</template>

<script>
	import uniCalendar from '@/components/uni-calendar/uni-calendar.vue';
	export default {
		components: {
			uniCalendar
		},
		data() {
			return {
				list: [],
				normal: 0,
				late: 0,
				lack: 0
			}
		},
		onShow() {
			let that = this;
			let date = new Date();
			let year = date.getFullYear();
			let month = date.getMonth() + 1;
			that.searchCheckin(that, year, month);
		},
		methods: {
			searchCheckin: function(ref, year, month) {
				let that = ref;
				that.normal = 0;
				that.late = 0;
				that.lack = 0;
				that.list.length = 0;
				that.ajax(that.url.searchMonthCheckin, "POST", {year: year, month: month}, function(resp){
					for (let value of resp.data.list) {
						if (value.status != null && value.status != '') {
							let color = '';
							if (value.status == '正常') {
								color = 'green';
							} else if (value.status == '迟到') {
								color = 'orange';
							} else if (value.status == '缺勤') {
								color = 'red';
							}
							that.list.push({
								date: value.date,
								info: value.status,
								color: color
							});
						}
					}
					that.normal = resp.data.normal;
					that.late = resp.data.late;
					that.lack = resp.data.lack;
				})
			},
			changeMonth: function(e) {
				let that = this;
				let year = e.year;
				let month = e.month;
				that.searchCheckin(that, year, month);
			}
		}
	}
</script>

<style lang="less">
	@import url("my_checkin.less");
</style>
