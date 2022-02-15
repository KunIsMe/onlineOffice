<template>
	<view>
		<image src="../../static/logo-2.png" class="logo" mode="widthFix"></image>
		<view class="register-container">
			<input type="text" class="register-code" placeholder="输入你的注册激活码" maxlength="6" v-model="registerCode" />
			<view class="register-desc">管理员创建好员工账号之后，你可以从你的个人邮箱中获得该注册激活码</view>
			<button class="register-btn" open-type="getUserInfo" @tap="register()">注册</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				registerCode: '',
				code: null
			}
		},
		methods: {
			register: function() {
				let that = this;
				if (that.registerCode == null || that.registerCode.length == 0) {
					uni.showToast({
						icon: 'error',
						title: '注册激活码不能为空'
					});
					return;
				} else if (/^[0-9]{6}$/.test(that.registerCode) == false) {
					uni.showToast({
						icon: 'error',
						title: '注册激活码必须是6位数字'
					});
					return;
				}
				uni.login({
					provider: "weixin",
					success: function(resp){
						// 临时授权码
						that.code = resp.code;
					}
				});
				uni.getUserProfile({
					desc: "获取用户信息",
					success: function(resp) {
						let nickName = resp.userInfo.nickName;
						let avatarUrl = resp.userInfo.avatarUrl;
						let data = {
							code: that.code,
							nickname: nickName,
							photo: avatarUrl,
							registerCode: that.registerCode
						};
						that.ajax(that.url.register, "POST", data, function(resp){
							let permission = resp.data.permission;
							uni.setStorageSync("permission", permission);
							// 跳转到index页面
							uni.switchTab({
								url: "../index/index"
							});
						});
					}
				});
			}
		}
	}
</script>

<style lang="less">
	@import url("register.less");
</style>
