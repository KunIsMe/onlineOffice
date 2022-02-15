<template>
	<view class="page" v-if="checkPermission(['ROOT', 'MEETING:INSERT', 'MEETING:UPDATE'])">
		<view class="header">
			<input type="text" class="title" v-model="title" placeholder="输入会议标题" placeholder-class="title-placeholder" />
			<image src="../../static/icon-18.png" class="edit-icon" mode="widthFix" />
		</view>
		<view class="attr">
			<view class="list">
				<view class="item">
					<view class="key">日期</view>
					<picker mode="date" :value="date" v-if="canEdit" @change="dateChange">
						<view class="uni-input">{{ date }}</view>
					</picker>
					<text class="value" v-if="!canEdit">{{ date }}</text>
				</view>
				<view class="item">
					<view class="key">开始时间</view>
					<picker mode="time" :value="start" v-if="canEdit" @change="startChange">
						<view class="uni-input">{{ start }}</view>
					</picker>
					<text class="value" v-if="!canEdit">{{ start }}</text>
				</view>
				<view class="item">
					<view class="key">结束时间</view>
					<picker mode="time" :value="end" v-if="canEdit" @change="endChange">
						<view class="uni-input">{{ end }}</view>
					</picker>
					<text class="value" v-if="!canEdit">{{ end }}</text>
				</view>
				<view class="item">
					<view class="key">会议类型</view>
					<picker :value="typeIndex" :range="typeArray" v-if="canEdit" @change="typeChange">{{ typeArray[typeIndex] }}</picker>
					<text class="value" v-if="!canEdit">{{ typeArray[typeIndex] }}</text>
				</view>
				<view class="item" v-if="typeArray[typeIndex] == '线下会议'" @tap="editPlace">
					<view class="key">会议地点</view>
					<text class="value">{{ place }}</text>
				</view>
			</view>
			<view @tap="editDesc">
				<text class="desc">会议内容： {{ desc }}</text>
			</view>
		</view>
		<view class="members">
			<view class="number">参会者（ {{ members.length }} 人 ）</view>
			<view class="member">
				<view class="user" v-for="one in members" :key="one.id" @longpress="deleteMember(one.id)">
					<image :src="one.photo" class="photo" mode="widthFix" />
					<text class="name">{{ one.name }}</text>
				</view>
				<view class="add">
					<image src="../../static/icon-19.png" class="add-icon" mode="widthFix" @tap="toMembersPage()" />
				</view>
			</view>
		</view>
		<button class="btn" @tap="save">保存</button>
		<uni-popup ref="popupPlace" type="dialog">
			<uni-popup-dialog mode="input" title="编辑文字内容" placeholder="输入会议地点" :value="place" @confirm="submitPlace"></uni-popup-dialog>
		</uni-popup>
		<uni-popup ref="popupDesc" type="dialog">
			<uni-popup-dialog mode="input" title="编辑文字内容" placeholder="输入会议内容" :value="desc" @confirm="submitDesc"></uni-popup-dialog>
		</uni-popup>
	</view>
</template>

<script>
	import uniPopup from '@/components/uni-popup/uni-popup.vue';
	import uniPopupMessage from '@/components/uni-popup/uni-popup-message.vue';
	import uniPopupDialog from '@/components/uni-popup/uni-popup-dialog.vue';
	export default {
		components: {
			uniPopup,
			uniPopupMessage,
			uniPopupDialog
		},
		data() {
			return {
				opt: null,
				id: null,
				uuid: null,
				canEdit: true,
				title: '',
				date: '',
				start: '',
				end: '',
				typeArray: ['线上会议', '线下会议'],
				typeIndex: 0,
				place: '',
				desc: '',
				members: [],
				instanceId: null
			}
		},
		onShow() {
			let that = this;
			// 获取页面栈
			let pages = getCurrentPages();
			// 获取当前页
			let currPage = pages[pages.length - 1];
			// 判断是从哪个页面跳转过来的（会议列表 / 选择成员）
			if (!currPage.hasOwnProperty('finishMembers') || !currPage.finishMembers) {
				// 会议列表 页面
				if (that.opt == 'insert') {
					let now = new Date();
					// 时间向后偏移 30 分钟
					now.setTime(now.getTime() + 30 * 60 * 1000);
					that.date = now.format("yyyy-MM-dd");
					that.start = now.format("hh:mm");
					// 根据 start 时间，再向后偏移 60 分钟
					now.setTime(now.getTime() + 60 * 60 * 1000);
					that.end = now.format("hh:mm");
				} else if (that.opt == 'edit') {
					that.ajax(that.url.searchMeetingById, "POST", {id: that.id}, function(resp){
						let result = resp.data.result;
						that.uuid = result.uuid;
						that.title = result.title;
						that.date = result.date;
						that.start = result.start;
						that.end = result.end;
						that.typeIndex = result.type - 1;
						that.place = result.place;
						let desc = result.desc;
						if (desc != null && desc != '') {
							that.desc = desc;
						}
						that.members = result.members;
						that.instanceId = result.instanceId;
					});
				}
			} else {
				// 选择成员 页面
				let members = [];
				for (let one of currPage.members) {
					members.push(Number (one));
				}
				that.ajax(that.url.searchMembers, "POST", {members: JSON.stringify(members)}, function(resp){
					let result = resp.data.result;
					that.members = result;
				});
			}
		},
		onLoad(options) {
			this.id = options.id;
			this.opt = options.opt;
		},
		methods: {
			toMembersPage: function() {
				let that = this;
				let array = [];
				for (let one of that.members) {
					array.push(one.id);
				}
				uni.navigateTo({
					url: "../members/members?members=" + array.join(",")
				})
			},
			dateChange: function(e) {
				this.date = e.detail.value;
			},
			startChange: function(e) {
				this.start = e.detail.value;
			},
			endChange: function(e) {
				this.end = e.detail.value;
			},
			typeChange: function(e) {
				this.typeIndex = e.detail.value;
			},
			editPlace: function() {
				if (!this.canEdit) {
					return;
				}
				this.$refs.popupPlace.open();
			},
			editDesc: function() {
				if (!this.canEdit) {
					return;
				}
				this.$refs.popupDesc.open();
			},
			submitPlace: function(done, value) {
				if (value != null && value != '') {
					this.place = value;
					done();
				} else {
					uni.showToast({
						icon: "error",
						title: "会议地点不能为空"
					});
				}
			},
			submitDesc: function(done, value) {
				if (value != null && value != '') {
					this.desc = value;
					done();
				} else {
					uni.showToast({
						icon: "error",
						title: "会议内容不能为空"
					});
				}
			},
			save: function() {
				let that = this;
				let array = [];
				for (let one of that.members) {
					array.push(one.id);
				}
				if (
					that.checkBlank(that.title, "会议标题") || 
					that.checkValidStartAndEnd(that.start, that.end) || 
					(that.typeIndex == "1" && that.checkBlank(that.place, "会议地点")) || 
					that.checkBlank(that.desc, "会议内容") || 
					array.length == 0
				) {
					return;
				}
				let data = {
					title: that.title,
					date: that.date,
					start: that.start,
					end: that.end,
					type: Number(that.typeIndex) + 1,
					members: JSON.stringify(array),
					desc: that.desc,
					id: that.id,
					instanceId: that.instanceId
				}
				if (that.typeIndex == "1") {
					data.place = that.place;
				}
				let url = "";
				if (that.opt == "insert") {
					url = that.url.insertMeeting;
				} else if (that.opt == "edit") {
					url = that.url.updateMeetingInfo;
				}
				that.ajax(url, "POST", data, function(resp){
					uni.showToast({
						icon: "success",
						title: "保存成功",
						complete: function() {
							setTimeout(() => {
								uni.navigateBack({});
							}, 2000);
						}
					});
				});
			},
			deleteMember: function(id) {
				let that = this;
				// 手机短振动
				uni.vibrateShort({});
				uni.showModal({
					title: "提示信息",
					content: "是否删除该名参会人员？",
					success: function(resp) {
						if (resp.confirm) {
							let position = null;
							for (let i = 0; i < that.members.length; i++) {
								let one = that.members[i];
								if (one.id == id) {
									position = i;
									break;
								}
							}
							that.members.splice(position, 1);
						}
					}
				});
			}
		}
	}
</script>

<style lang="less">
	@import url("meeting.less");
</style>
