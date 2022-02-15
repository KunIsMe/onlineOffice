<template>
	<view class="page">
		<view class="user-info">
			<view class="border-outer">
				<view class="border-inner">
					<image :src="photo" class="photo" mode="widthFix" />
				</view>
			</view>
			<view class="summary">
				<view>
					<text class="title">姓名</text>
					<text class="content">{{ name }}</text>
				</view>
				<view>
					<text class="title">部门</text>
					<text class="content">{{ deptName.length > 0 ? deptName : "暂无" }}</text>
				</view>
				<view>
					<text class="title">状态</text>
					<text class="content">在职</text>
				</view>
			</view>
		</view>
		<view class="list-title">用户信息栏目</view>
		<uni-list>
			<uni-list-item title="个人信息" link to="../user_info/user_info?temp=self"></uni-list-item>
			<uni-list-item title="我的考勤" link to="../my_checkin/my_checkin"></uni-list-item>
		</uni-list>
		<view class="list-title">系统管理栏目</view>
		<uni-list>
			<uni-list-item title="员工管理" link to="../user_list/user_list" v-show="checkPermission(['ROOT', 'EMPLOYEE:SELECT'])" />
			<uni-list-item title="部门管理" link to="../dept_list/dept_list" v-show="checkPermission(['ROOT', 'DEPT:SELECT'])" />
			<uni-list-item title="权限管理" link to="../role_list/role_list" v-show="checkPermission(['ROOT', 'ROLE:SELECT'])" />
		</uni-list>
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
				photo: "",
				name: "",
				deptName: ""
			}
		},
		onShow() {
			let that = this;
			that.ajax(that.url.searchUserSummary, "GET", null, function(resp){
				let result = resp.data.result;
				that.photo = result.photo;
				that.name = result.name;
				that.deptName = result.deptName;
			});
		},
		methods: {
			
		}
	}
</script>

<style lang="less">
	@import url("mine.less");
</style>
