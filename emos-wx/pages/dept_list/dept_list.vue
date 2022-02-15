<template>
	<view class="page" v-if="checkPermission(['ROOT', 'DEPT:SELECT'])">
		<view class="list">
			<view class="item" v-for="one in list" :key="one.id" @longpress="deleteDept(one.id, one.deptName)">
				<text class="key">{{ one.deptName }}</text>
				<text class="value" @tap="updateDept(one.id, one.deptName)" v-if="checkPermission(['ROOT', 'DEPT:UPDATE'])">修改</text>
			</view>
		</view>
		<button class="btn" @tap="insertDept" v-if="checkPermission(['ROOT', 'DEPT:INSERT'])">添加</button>
		<uni-popup ref="popupDept" type="dialog">
			<uni-popup-dialog mode="input" title="编辑部门名称" :value="deptName" placeholder="输入部门名称" @confirm="finishDept" />
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
				deptId: null,
				deptName: '',
				list: []
			}
		},
		onShow() {
			this.opt = null;
			this.deptId = null;
			this.deptName = '';
			this.loadData(this);
		},
		methods: {
			loadData: function(ref) {
				let that = ref;
				that.opt = null;
				that.deptId = null;
				that.deptName = '';
				that.ajax(that.url.searchAllDept, "GET", null, function(resp){
					that.list = resp.data.result;
				});
			},
			insertDept: function() {
				this.deptName = '';
				this.opt = 'insert';
				this.$refs.popupDept.open();
			},
			updateDept: function(id, deptName) {
				this.opt = 'edit';
				this.id = id;
				this.deptName = deptName;
				this.$refs.popupDept.open();
			},
			finishDept: function(done, value) {
				let that = this;
				// 记录 对话框中输入的部门名称不能跟已有部门名称相同
				let flag = false;
				for (let one of that.list) {
					if (value == one.deptName) {
						flag = true;
						break;
					}
				}
				if (!/^[\u4e00-\u9fa5]{3,15}$/) {
					uni.showToast({
						icon: "error",
						title: "部门名称不符合规范要求"
					});
				} else if (flag) {
					uni.showToast({
						icon: "error",
						title: "部门名称已经存在"
					});
				} else {
					that.deptName = value;
					let url = null;
					if (that.opt == 'insert') {
						url = that.url.insertDept;
					} else {
						url = that.url.updateDeptById;
					}
					let data = {
						id: that.id,
						deptName: that.deptName
					};
					that.ajax(url, "POST", data, function(resp){
						uni.showToast({
							icon: "success",
							title: that.opt == 'insert' ? "添加成功" : "修改成功",
							complete: function() {
								that.loadData(that);
							}
						});
					});
					done();
				}
			},
			deleteDept: function(id, deptName) {
				let that = this;
				if (!that.checkPermission(['ROOT', 'DEPT:DELETE'])) {
					uni.showToast({
						icon: "error",
						title: "你不具备相关权限"
					});
					return;
				}
				uni.showModal({
					title: "提示信息",
					content: `确认是否删除${deptName}`,
					success: function(resp) {
						if (resp.confirm) {
							that.ajax(that.url.deleteDeptById, "POST", {id: id}, function(resp){
								uni.showToast({
									icon: "success",
									title: "删除成功",
									complete: function() {
										that.loadData(that);
									}
								});
							});
						}
					}
				});
			}
		}
	}
</script>

<style lang="less">
	@import url("dept_list.less");
</style>
