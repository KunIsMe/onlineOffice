<template>
	<view>
		<camera device-position="front" flash="off" @error="error" class="camera" v-if="showCamera"></camera>
		<image :src="photoPath" mode="widthFix" class="image" v-if="showImage" />
		<view class="operate-container">
			<button type="primary" class="btn" @tap="clickBtn" :disabled="!canCheckin">{{ btnText }}</button>
			<button type="warn" class="btn" @tap="afresh" :disabled="!canCheckin">重拍</button>
		</view>
		<view class="notice-container">
			<text class="notice">注意事项</text>
			<text class="desc">
				拍照签到的时候，必须要拍摄自己的正面照片，侧面照片会导致无法识别。另外，拍照的时候不要戴墨镜或者帽子，避免影响拍照签到的准确度。
			</text>
		</view>
	</view>
</template>

<script>
	var QQMapWX = require('../../lib/qqmap-wx-jssdk.min.js');
	var qqmapsdk;
	export default {
		data() {
			return {
				showCamera: true,
				showImage: false,
				photoPath: '',
				btnText: '拍照',
				canCheckin: true
			}
		},
		onLoad() {
			qqmapsdk = new QQMapWX({
				key: 'NFRBZ-T4KCO-PQAW6-SYBT3-HPDLF-3LFFE'
			})
		},
		onShow() {
			let that = this;
			that.ajax(that.url.validCanCheckin, "GET", null, function(resp){
				let message = resp.data.msg;
				if (message != "允许上班考勤") {
					that.canCheckin = false;
					setTimeout(() => {
						uni.showToast({
							icon: 'none',
							title: message
						})
					})
				}
			})
		},
		methods: {
			clickBtn: function() {
				let that = this;
				if (that.btnText == '拍照') {
					let ctx = uni.createCameraContext();
					ctx.takePhoto({
						quality: "high",
						success: function(resp) {
							that.photoPath = resp.tempImagePath;
							that.showCamera = false;
							that.showImage = true;
							that.btnText = '签到';
						}
					})
				} else {
					uni.showLoading({
						title: "正在签到，请稍后"
					});
					uni.getLocation({
						type: "wgs84",
						success: function(resp) {
							let longitude = resp.longitude;
							let latitude = resp.latitude;
							qqmapsdk.reverseGeocoder({
								location: {
									longitude: longitude,
									latitude: latitude
								},
								success: function(resp) {
									let address = resp.result.address;
									let addressComponent = resp.result.address_component;
									let nation = addressComponent.nation;
									let province = addressComponent.province;
									let city = addressComponent.city;
									let district = addressComponent.district;
									uni.uploadFile({
										url: that.url.checkin,
										filePath: that.photoPath,
										name: 'photo',
										header: {
											token: uni.getStorageSync("token")
										},
										formData: {
											address: address,
											country: nation,
											province: province,
											city: city,
											district: district
										},
										success: function(resp) {
											if (resp.statusCode == 500 && resp.data == '不存在人脸模型') {
												uni.hideLoading();
												uni.showModal({
													title: '提示信息',
													content: '“秒见”系统中不存在您的人脸识别模型，是否选用当前这张照片来作为人脸识别模型？',
													success: function(resp) {
														if (resp.confirm) {
															uni.showLoading({
																title: "正在创建，请稍后"
															});
															uni.uploadFile({
																url: that.url.createFaceModel,
																filePath: that.photoPath,
																name: 'photo',
																header: {
																	token: uni.getStorageSync("token")
																},
																success: function(resp) {
																	if (resp.statusCode == 500) {
																		uni.hideLoading();
																		uni.showToast({
																			icon: 'error',
																			title: resp.data
																		});
																	} else if (resp.statusCode == 200) {
																		uni.hideLoading();
																		uni.showToast({
																			icon: 'success',
																			title: '人脸建模成功'
																		})
																	}
																},
															});
														} else {
															uni.hideLoading();
														}
													}
												})
											} else if (resp.statusCode == 200) {
												let data = JSON.parse(resp.data);
												let code = data.code;
												let message = data.msg;
												if (code == 200) {
													uni.hideLoading();
													uni.showToast({
														icon: 'success',
														title: '签到成功',
														complete: function() {
															// 跳转到签到记录页面
															uni.navigateTo({
																url: '../checkin_result/checkin_result'
															})
														}
													})
												}
											} else if (resp.statusCode == 500) {
												uni.hideLoading();
												uni.showToast({
													icon: 'error',
													title: resp.data
												})
											}
										}
									})
								}
							})
						}
					})
				}
			},
			afresh: function() {
				let that = this;
				that.showCamera = true;
				that.showImage = false;
				that.btnText = '拍照';
			}
		}
	}
</script>

<style lang="less">
	@import url("checkin.less");
</style>
