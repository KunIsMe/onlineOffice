<template>
	<view>
		<uni-indexed-list :options="list" :showSelect="false" @click="clickContact" />
	</view>
</template>

<script>
	import uniIndexedList from '@/components/uni-indexed-list/uni-indexed-list.vue';
	export default {
		components: {
			uniIndexedList
		},
		data() {
			return {
				list: []
			}
		},
		onShow() {
			let that = this;
			that.list = [];
			that.ajax(that.url.searchUserContactList, "GET", null, function(resp){
				let result = resp.data.result;
				for (let key in result) {
					let nameArray = [];
					let telArray = [];
					for (let contact of result[key]) {
						let name = contact.name;
						let tel = contact.tel;
						let dept = contact.dept;
						dept = dept != '' ? '（ ' + dept + ' ）' : '';
						nameArray.push(name + dept);
						telArray.push(tel);
					}
					that.list.push({
						letter: key,
						data: nameArray,
						tel: telArray
					});
				}
			});
		},
		methods: {
			clickContact: function(e) {
				let that = this;
				let name = e.item.name;
				let key = e.item.key;
				let tel = null;
				for (let one of that.list) {
					if (one.letter == key) {
						let index = one.data.indexOf(name);
						tel = one.tel[index];
						break;
					}
				}
				uni.showModal({
					title: "提示信息",
					content: `确认是否要拨打电话给${name}？`,
					success: function(resp) {
						if (resp.confirm) {
							uni.makePhoneCall({
								phoneNumber: tel
							});
						}
					}
				});
			}
		}
	}
</script>

<style lang="less">
	@import url("contacts.less");
</style>
