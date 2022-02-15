<template>
	<view class="page">
		<view class="summary-container">
			<view class="user-info">
				<image :src="photo" class="photo" mode="widthFix" />
				<view class="info">
					<text class="username">{{ name }}</text>
					<text class="dept">部门：{{ deptName.length > 0 ? deptName : "暂无" }}</text>
				</view>
			</view>
			<view class="date">{{ date }}</view>
		</view>
		<view class="result-container">
			<view class="left">
				<image src="../../static/icon-6.png" class="icon-timer" mode="widthFix" />
				<image src="../../static/icon-6.png" class="icon-timer" mode="widthFix" />
				<view class="line"></view>
			</view>
			<view class="right">
				<view class="row">
					<text class="time">上班时间：{{ attendanceTime }}</text>
				</view>
				<view class="row">
					<text class="checkin-time">签到时间：{{ checkinTime }}</text>
					<text class="checkin-result green other" v-if="status == '正常'">{{ status }}</text>
					<text class="checkin-result yellow other" v-if="status == '迟到'">{{ status }}</text>
				</view>
				<view class="row">
					<image src="../../static/icon-7.png" class="icon-small" mode="widthFix" />
					<text class="desc max-width">{{ address }}</text>
					<text class="checkin-result green" v-if="risk == '低风险'">{{ risk }}</text>
					<text class="checkin-result yellow" v-if="risk == '中风险'">{{ risk }}</text>
					<text class="checkin-result red" v-if="risk == '高风险'">{{ risk }}</text>
				</view>
				<view class="row">
					<image src="../../static/icon-8.png" class="icon-small" mode="widthFix" />
					<text class="desc">身份验证</text>
					<text class="checkin-result green">已通过</text>
				</view>
				<view class="row">
					<text class="time">下班时间：{{ closingTime }}</text>
				</view>
			</view>
		</view>
		<view class="checkin-report">
			<image src="../../static/big-icon-1.png" class="big-icon" mode="widthFix" />
			<view class="report-title">
				<text class="days">{{ checkinDays }}</text>
				<text class="unit">天</text>
			</view>
			<view class="sub-title">
				<text>累计签到</text>
				<view class="line"></view>
			</view>
			<view class="calendar-container">
				<view class="calendar" v-for="value in weekCheckin" :key="value">
					<image src="../../static/icon-9.png" class="calendar-icon" mode="widthFix" v-if="value.type == '工作日'" />
					<image src="../../static/icon-10.png" class="calendar-icon" mode="widthFix" v-if="value.type == '节假日'" />
					<text class="day">{{ value.day }}</text>
					<text class="result green" v-if="value.status == '正常'">{{ value.status }}</text>
					<text class="result yellow" v-if="value.status == '迟到'">{{ value.status }}</text>
					<text class="result red" v-if="value.status == '缺勤'">{{ value.status }}</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				photo: "",
				name: "",
				deptName: "",
				address: "",
				status: "",
				risk: "",
				checkinTime: "",
				date: "",
				attendanceTime: "",
				closingTime: "",
				checkinDays: 0,
				weekCheckin: []
			}
		},
		onShow() {
			let that = this;
			that.ajax(that.url.searchTodayCheckin, "GET", null, function(resp){
				let result = resp.data.result;
				that.photo = result.photo;
				that.name = result.name;
				that.deptName = result.deptName;
				that.address = result.address;
				that.status = result.status;
				that.risk = result.risk;
				that.checkinTime = result.checkinTime;
				that.date = result.date;
				that.attendanceTime = result.attendanceTime;
				that.closingTime = result.closingTime;
				that.checkinDays = result.checkinDays;
				that.weekCheckin = result.weekCheckin;
			});
		},
		methods: {
			
		}
	}
</script>

<style lang="less">
	@import url("checkin_result.less");
</style>
