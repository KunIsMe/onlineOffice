<template>
	<view class="page">
		<view class="list">
			<view class="item" @tap="editName">
				<text class="key">姓名</text>
				<text class="value">{{ name }}</text>
			</view>
			<view class="item">
				<text class="key">性别</text>
				<picker v-if="canEdit" :value="sexIndex" :range="sexArray" @change="sexChange">{{ sexArray[sexIndex] }}</picker>
				<text class="value" v-if="!canEdit">{{ sexArray[sexIndex] }}</text>
			</view>
			<view class="item">
				<text class="key">所在部门</text>
				<picker v-if="canEdit && temp == null" :value="deptIndex" :range="deptArray" @change="deptChange">{{ deptArray[deptIndex] }}</picker>
				<text class="value" v-if="(!canEdit) || temp != null">{{ deptArray[deptIndex] }}</text>
			</view>
			<view class="item" @tap="editTel">
				<text class="key">联系电话</text>
				<text class="value">{{ tel }}</text>
			</view>
			<view class="item" @tap="editEmail">
				<text class="key">电子邮箱</text>
				<text class="value">{{ email }}</text>
			</view>
			<view class="item">
				<text class="key">入职日期</text>
				<picker v-if="canEdit && temp == null" mode="date" :value="hiredate" @change="hiredateChange">
					<view class="uni-input">{{ hiredate }}</view>
				</picker>
				<text class="value" v-if="(!canEdit) || temp != null">{{ hiredate }}</text>
			</view>
			<view class="item" @tap="selectRoles">
				<text class="key">所属角色</text>
				<text class="value">{{ roles }}</text>
			</view>
			<view class="item">
				<text class="key">当前状态</text>
				<picker v-if="canEdit && temp == null" :value="statusIndex" :range="statusArray" @change="statusChange">{{ statusArray[statusIndex] }}</picker>
				<text class="value" v-if="(!canEdit) || temp != null">{{ statusArray[statusIndex] }}</text>
			</view>
			<button class="save-btn" v-if="opt != 'read' && checkPermission(['ROOT', 'EMPLOYEE:INSERT', 'EMPLOYEE:UPDATE'])" @tap="save">保存</button>
			<button class="delete-btn" type="warn" v-if="opt != 'read' && opt != 'insert' && temp == null && checkPermission(['ROOT', 'EMPLOYEE:DELETE'])" @tap="deleteUser">删除</button>
			<uni-popup ref="popupName" type="dialog">
				<uni-popup-dialog mode="input" title="编辑文字内容" :value="name" placeholder="输入姓名" @confirm="finishName" />
			</uni-popup>
			<uni-popup ref="popupTel" type="dialog">
				<uni-popup-dialog mode="input" title="编辑文字内容" :value="tel" placeholder="输入联系电话" @confirm="finishTel" />
			</uni-popup>
			<uni-popup ref="popupEmail" type="dialog">
				<uni-popup-dialog mode="input" title="编辑文字内容" :value="email" placeholder="输入电子邮箱" @confirm="finishEmail" />
			</uni-popup>
			<view class="shadow" v-if="showRoleDialog">
				<scroll-view class="dialog" scroll-y="true">
					<view class="title">角色列表</view>
					<text class="confirm" @tap="confirm">确定</text>
					<checkbox-group @change="roleChange">
						<label class="list-item" v-for="one in roleArray" :key="one.id">
							<checkbox :value="one.id" :checked="roles.split('，').indexOf(one.roleName) != -1"></checkbox>
							<text>{{ one.roleName }}</text>
						</label>
					</checkbox-group>
				</scroll-view>
			</view>
		</view>
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
				userId: null,
				temp: null,
				name: '',
				nickname: '',
				photo: '',
				tel: '',
				email: '',
				dept: '',
				hiredate: '',
				roles: '',
				sexArray: ['男', '女'],
				sexIndex: 0,
				deptArray: [],
				deptIndex: 0,
				statusArray: ['在职', '离职'],
				statusIndex: 0,
				showRoleDialog: false,
				roleArray: [],
				selectedRole: [],
				canEdit: false,
				opt: null
			}
		},
		onShow() {
			let that = this;
			if (that.opt == 'insert') {
				// 添加员工
				let now = new Date();
				let year = now.getFullYear();
				let month = now.getMonth() + 1;
				month = month < 10 ? '0' + month : month;
				let day = now.getDate();
				day = day < 10 ? '0' + day : day;
				that.hiredate = year + '-' + month + '-' + day;
				that.loadDeptData(that);
				that.loadRoleData(that);
				return;
			} else if (that.userId != null) {
				// 查看或修改员工信息
				that.loadDeptData(that);
				that.loadRoleData(that);
				that.ajax(that.url.searchUserInfo, "POST", {userId: that.userId}, function(resp){
					let result = resp.data.result;
					that.name = result.name;
					that.tel = result.tel;
					that.email = result.email;
					that.sexIndex = that.sexArray.indexOf(result.sex);
					that.deptIndex = that.deptArray.indexOf(result.dept);
					that.roles = result.roles;
					let roles = that.roles.split('，');
					for (let one of that.roleArray) {
						if (roles.indexOf(one.roleName) != -1) {
							that.selectedRole.push(one.id);
						}
					}
					that.statusIndex = that.statusArray.indexOf(result.status);
					that.hiredate = result.hiredate;
				});
			} else {
				// 查看个人信息
				that.canEdit = true;
				that.loadDeptData(that);
				that.loadRoleData(that);
				that.ajax(that.url.searchUserSelfInfo, "GET", null, function(resp){
					let result = resp.data.result;
					that.name = result.name;
					that.tel = result.tel;
					that.email = result.email;
					that.sexIndex = that.sexArray.indexOf(result.sex);
					that.deptIndex = that.deptArray.indexOf(result.dept);
					that.roles = result.roles;
					let roles = that.roles.split('，');
					for (let one of that.roleArray) {
						if (roles.indexOf(one.roleName) != -1) {
							that.selectedRole.push(one.id);
						}
					}
					that.statusIndex = that.statusArray.indexOf(result.status);
					that.hiredate = result.hiredate;
					that.userId = result.userId;
				});
			}
		},
		onLoad(options) {
			if (options.hasOwnProperty('userId')) {
				this.userId = Number (options.userId);
			}
			if (options.hasOwnProperty('opt')) {
				this.opt = options.opt;
			}
			// temp=self：表示用户是查看个人信息
			if (options.hasOwnProperty('temp')) {
				this.temp = options.temp;
			}
			if (this.opt == 'read') {
				this.canEdit = false;
			} else {
				this.canEdit = this.checkPermission(['ROOT', 'EMPLOYEE:INSERT', 'EMPLOYEE:UPDATE']);
			}
		},
		methods: {
			loadDeptData: function(ref) {
				let that = ref;
				that.ajax(that.url.searchAllDept, "GET", null, function(resp){
					let result = resp.data.result;
					let deptArray = [];
					for (let one of result) {
						deptArray.push(one.deptName);
					}
					that.deptArray = deptArray;
					if (that.opt == 'insert') {
						that.deptIndex = 0;
					} else {
						that.deptIndex = that.deptArray.indexOf(that.dept);
					}
				});
			},
			loadRoleData: function(ref) {
				let that = ref;
				that.ajax(that.url.searchAllRole, "GET", null, function(resp){
					that.roleArray = resp.data.result;
				});
			},
			editName: function() {
				if (!this.canEdit) {
					return;
				}
				this.$refs.popupName.open();
			},
			editTel: function() {
				if (!this.canEdit) {
					return;
				}
				this.$refs.popupTel.open();
			},
			editEmail: function() {
				if (!this.canEdit) {
					return;
				}
				this.$refs.popupEmail.open();
			},
			finishName: function(done, value) {
				if (!this.checkValidName(value, '姓名')) {
					this.name = value;
					done();
				}
			},
			finishTel: function(done, value) {
				if (!this.checkValidTel(value, '联系电话')) {
					this.tel = value;
					done();
				}
			},
			finishEmail: function(done, value) {
				if (!this.checkValidEmail(value, '电子邮箱')) {
					this.email = value;
					done();
				}
			},
			sexChange: function(e) {
				this.sexIndex = e.detail.value;
			},
			deptChange: function(e) {
				this.deptIndex = e.detail.value;
			},
			hiredateChange: function(e) {
				this.hiredate = e.detail.value;
			},
			statusChange: function(e) {
				this.statusIndex = e.detail.value;
			},
			selectRoles: function() {
				if (!this.canEdit) {
					return;
				}
				if (this.temp != null) {
					return;
				}
				this.showRoleDialog = true;
			},
			roleChange: function(e) {
				let that = this;
				that.selectedRole = e.detail.value;
				let temp = [];
				for (let one of that.roleArray) {
					if (that.selectedRole.indexOf(one.id + '') != -1) {
						temp.push(one.roleName);
					}
				}
				that.roles = temp.join('，');
			},
			confirm: function() {
				if (this.selectedRole.length == 0) {
					uni.showToast({
						icon: "error",
						title: "至少选择一个角色"
					});
				} else {
					this.showRoleDialog = false;
				}
			},
			save: function() {
				let that = this;
				if (that.opt == 'insert') {
					if (
						that.checkValidName(that.name, '姓名') || 
						that.checkValidTel(that.tel, '联系电话') || 
						that.checkValidEmail(that.email, '电子邮箱') || 
						that.selectedRole.length == 0
					) {
						return;
					}
				}
				uni.showModal({
					title: "提示信息",
					content: "确认是否保存员工信息？",
					success: function(resp) {
						if (resp.confirm) {
							let sex = that.sexArray[that.sexIndex];
							let deptName = that.deptArray[that.deptIndex];
							let status = that.statusArray[that.statusIndex];
							status = status == '在职' ? 1 : 2;
							let roles = [];
							for (let one of that.roleArray) {
								if (that.roles.split('，').indexOf(one.roleName) != -1) {
									roles.push(one.id);
								}
							}
							let url = null;
							if (that.opt == 'insert') {
								url = that.url.insertUser;
							} else {
								url = that.url.updateUserInfo;
							}
							let data = {
								name: that.name,
								sex: sex,
								deptName: deptName,
								tel: that.tel,
								email: that.email,
								hiredate: that.hiredate,
								role: JSON.stringify(roles),
								status: status,
								userId: that.userId
							};
							that.ajax(url, "POST", data, function(resp){
								uni.showToast({
									icon: "success",
									title: "保存成功",
									complete: function() {
										setTimeout(() => {
											uni.navigateBack({
												delta: 1
											});
										}, 2000);
									}
								});
							});
						}
					}
				});
			},
			deleteUser: function() {
				let that = this;
				uni.showModal({
					title: "提示信息",
					content: `确认是否删除员工${that.name}？`,
					success: function(resp) {
						if (resp.confirm) {
							that.ajax(that.url.deleteUserById, "POST", {userId: that.userId}, function(resp){
								uni.showToast({
									icon: "success",
									title: "删除成功",
									complete: function() {
										setTimeout(() => {
											uni.navigateBack({
												delta: 1
											});
										}, 2000);
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
	@import url("user_info.less");
</style>
