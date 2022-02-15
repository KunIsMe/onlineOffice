<template>
	<view class="page">
		<swiper circular="true" interval="8000" duration="1000" autoplay="true" class="swiper">
			<swiper-item>
				<image mode="widthFix" src="https://emos-1307152777.cos.ap-beijing.myqcloud.com/img/banner/swiper-1.JPG" />
			</swiper-item>
			<swiper-item>
				<image mode="widthFix" src="https://emos-1307152777.cos.ap-beijing.myqcloud.com/img/banner/swiper-2.JPG" />
			</swiper-item>
			<swiper-item>
				<image mode="widthFix" src="https://emos-1307152777.cos.ap-beijing.myqcloud.com/img/banner/swiper-3.JPG" />
			</swiper-item>
			<swiper-item>
				<image mode="widthFix" src="https://emos-1307152777.cos.ap-beijing.myqcloud.com/img/banner/swiper-4.JPG" />
			</swiper-item>
		</swiper>
		<view class="notify-container" @tap="toPage('邮箱', '/pages/message_list/message_list')">
			<view class="notify-title">
				<image mode="widthFix" src="../../static/icon-1.png" class="notify-icon" />
				消息提醒
			</view>
			<view class="notify-content">
				你有 {{ unreadRows }} 条未读消息
			</view>
			<image mode="widthFix" src="../../static/icon-2.png" class="more-icon" />
		</view>
		<view class="nav-container">
			<view class="nav-row">
				<view class="nav" @tap="toPage('在线签到', '../checkin/checkin')">
					<image src="../../static/nav-1.png" mode="widthFix" class="icon" />
					<text class="name">在线签到</text>
				</view>
				<view class="nav" @tap="toPage('会议审批', '../approval_list/approval_list')">
					<image src="../../static/nav-10.png" mode="widthFix" class="icon" />
					<text class="name">会议审批</text>
				</view>
				<view class="nav" @tap="toPage('员工信息', '../user_info/user_info?temp=self')">
					<image src="../../static/nav-5.png" mode="widthFix" class="icon" />
					<text class="name">修改信息</text>
				</view>
				<view class="nav" @tap="more">
					<image src="../../static/nav-11.png" mode="widthFix" class="icon" />
					<text class="name">更多功能</text>
				</view>
			</view>
		</view>
		<view class="calendar-container">
			<uni-calendar :insert="true" :lunar="true" :selected="calendar" @monthSwitch="changeMonth" @confirm="changeMonth" @backtoday="changeMonth" />
		</view>
		<uni-popup ref="popupMsg" type="top">
			<uni-popup-message type="success" :message="'接收到 ' + lastRows + ' 条新消息'" :duration="2000"></uni-popup-message>
		</uni-popup>
	</view>
</template>

<script>
	import uniPopup from '@/components/uni-popup/uni-popup.vue';
	import uniPopupMessage from '@/components/uni-popup/uni-popup-message.vue';
	import uniPopupDialog from '@/components/uni-popup/uni-popup-dialog.vue';
	import uniCalendar from '@/components/uni-calendar/uni-calendar.vue';
	export default {
		components: {
			uniPopup,
			uniPopupMessage,
			uniPopupDialog,
			uniCalendar
		},
		data() {
			return {
				unreadRows: 0,
				lastRows: 0,
				timer: null,
				calendar: []
			}
		},
		onLoad() {
			let that = this;
			uni.$on("showMessage", function(){
				that.$refs.popupMsg.open();
			});
			that.ajax(that.url.refreshMessage, "GET", null, function(resp){
				that.unreadRows = resp.data.unreadRows;
				that.lastRows = resp.data.lastRows;
				if (that.lastRows > 0) {
					uni.$emit("showMessage");
				}
			});
		},
		onUnload() {
			uni.$off("showMessage");
		},
		onShow() {
			let that = this;
			that.timer = setInterval(() => {
				that.ajax(that.url.refreshMessage, "GET", null, function(resp){
					that.unreadRows = resp.data.unreadRows;
					that.lastRows = resp.data.lastRows;
					if (that.lastRows > 0) {
						uni.$emit("showMessage");
					}
				});
			}, 5000);
			// 加载日历中的会议列表
			let date = new Date();
			that.loadMeetingInMonth(that, date.getFullYear(), date.getMonth() + 1);
		},
		onHide() {
			let that = this;
			clearInterval(that.timer);
		},
		methods: {
			toPage: function(name, url) {
				// 验证用户权限
				let flag = false;
				if (name == "会议审批") {
					flag = !this.checkPermission(['WORKFLOW:APPROVAL']);
				}
				if (flag) {
					uni.showToast({
						icon: "error",
						title: "你不具备相关权限"
					});
				} else {
					uni.navigateTo({
						url: url
					});
				}
			},
			loadMeetingInMonth: function(ref, year, month) {
				let that = ref;
				let data = {
					year: year,
					month: month
				};
				that.ajax(that.url.searchUserMeetingInMonth, "POST", data, function(resp){
					let result = resp.data.result;
					that.calendar = [];
					for (let one of result) {
						that.calendar.push({date: one, info: '会议'});
					};
				});
			},
			changeMonth: function(e) {
				let that = this;
				let year = e.year;
				let month = e.month;
				that.loadMeetingInMonth(that, year, month);
			},
			more: function() {
				uni.showToast({
					icon: "none",
					title: "敬请期待"
				});
			}
		}
	}
</script>

<style lang="less">
	@import url("index.less");
</style>
hangeMonth"