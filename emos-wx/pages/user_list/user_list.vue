<template>
	<view class="page" v-if="checkPermission(['ROOT', 'EMPLOYEE:SELECT'])">
		<view class="header">
			<icon type="search" size="15" class="search-icon" />
			<input type="text" confirm-type="search" v-model="keyword" @confirm="search" placeholder="输入要检索的员工姓名" maxlength="15" class="keyword" />
			<button class="btn" v-if="checkPermission(['ROOT', 'EMPLOYEE:INSERT'])" @tap="toPage">添加员工</button>
		</view>
		<view v-for="dept in list" :key="dept.id">
			<view class="list-title">{{ dept.deptName }}（ {{ dept.count }} 人 ）</view>
			<uni-list v-if="dept.count > 0">
				<uni-list-item v-for="member in dept.members" :key="member.userId" 
					:title="member.name" 
					link :to="'../user_info/user_info?userId=' + member.userId" 
				/>
			</uni-list>
		</view>
	</view>
</template>

<script>
	import uniList from '@/components/uni-list/uni-list.vue';
	import uniListItem from '@/components/uni-list-item/uni-list-item.vue';
	export default {
		components: {
			uniList,
			uniListItem
		},
		data() {
			return {
				keyword: null,
				list: []
			}
		},
		onShow() {
			this.keyword = null;
			this.loadData(this);
		},
		methods: {
			loadData: function(ref) {
				let that = ref;
				that.ajax(that.url.searchUserGroupByDept, "POST", {keyword: that.keyword}, function(resp){
					that.list = resp.data.result;
				});
			},
			search: function() {
				let that = this;
				if (that.keyword == '') {
					that.keyword = null;
				}
				if (that.keyword != null && /^[\u4e00-\u9fa5]{1,15}$/.test(that.keyword) == false) {
					uni.showToast({
						icon: "error",
						title: "关键字必须是15个以内汉字"
					});
				} else {
					that.loadData(that);
				}
			},
			toPage: function() {
				uni.navigateTo({
					url: "../user_info/user_info?opt=insert"
				});
			}
		}
	}
</script>

<style lang="less">
	@import url("user_list.less");
</style>
