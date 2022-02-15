<template>
	<view class="page">
		<uni-list>
			<uni-list-chat v-for="one in list" :key="one.id" :title="one.senderName" 
				:note="one.msg" :avatar="one.senderPhoto" badgePositon="left" 
				:badgeText="one.readFlag ? '' : 'dot'" link="navigateTo" 
				:to="'../message/message?id=' + one.id + '&readFlag=' + one.readFlag + '&refId=' + one.refId" 
			>
				<view class="date-right">
					<text class="date">{{ one.sendTime }}</text>
				</view>
			</uni-list-chat>
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
				page: 1,
				length: 20,
				list: [],
				isLastPage: false
			}
		},
		onShow() {
			let that = this;
			that.page = 1;
			that.isLastPage = false;
			uni.pageScrollTo({
				scrollTop: "0"
			});
			that.loadMessageList(that);
		},
		onReachBottom() {
			let that = this;
			if (that.isLastPage) {
				return;
			}
			that.page = that.page + 1;
			that.loadMessageList(that);
		},
		methods: {
			loadMessageList: function(ref) {
				let that = ref;
				let data = {
					page: that.page,
					length: that.length
				};
				that.ajax(that.url.searchMessageByPage, "POST", data, function(resp){
					let result = resp.data.result;
					if (result == null || result.length == 0) {
						that.isLastPage = true;
						that.page = that.page - 1;
						uni.showToast({
							icon: "none",
							title: "已经到底了~"
						})
					} else {
						if (that.page == 1) {
							that.list = [];
						}
						that.list = that.list.concat(result);
					}
				})
			}
		}
	}
</script>

<style lang="less">
	@import url("message_list.less");
</style>
