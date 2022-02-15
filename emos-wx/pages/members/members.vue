<template>
	<view class="page">
		<checkbox-group @change="selected">
			<block v-for="dept in list" :key="dept.id">
				<view class="list-title">{{ dept.deptName }} ( {{ dept.count }} 人 )</view>
				<view class="item" v-for="member in dept.members">
					<view class="key">{{ member.name }}</view>
					<checkbox class="value" :value="member.userId" :checked="member.checked"></checkbox>
				</view>
			</block>
		</checkbox-group>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				list: [],
				members: []
			}
		},
		onShow() {
			this.loadData(this);
		},
		onLoad(options) {
			if (options.hasOwnProperty("members")) {
				let members = options.members;
				this.members = members == '' ? [] : members.split(',');
				let pages = getCurrentPages();
				let prevPage = pages[pages.length - 2];
				prevPage.members = this.members;
				prevPage.finishMembers = true;
			}
		},
		methods: {
			loadData: function(ref) {
				let that = ref;
				that.ajax(that.url.searchUserGroupByDept, "POST", {keyword: that.keyword}, function(resp){
					let result = resp.data.result;
					that.list = result;
					for (let dept of that.list) {
						for (let member of dept.members) {
							if (that.members.indexOf(member.userId + '') != -1) {
								member.checked = true;
							} else {
								member.checked = false;
							}
						}
					}
				})
			},
			selected: function(e) {
				let that = this;
				that.members = e.detail.value;
				// 获取页面栈
				let pages = getCurrentPages();
				// 获取上一页面
				let prevPage = pages[pages.length - 2];
				// 为上一页面赋值（绑定数据）
				prevPage.members = that.members;
				prevPage.finishMembers = true;
			}
		}
	}
</script>

<style lang="less">
	@import url("members.less");
</style>
