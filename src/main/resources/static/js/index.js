$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	var content=$('#message-text').val()
	var title=$("input[name='title']").val()

	//发送请求之前,将csrf令牌设置到请求的消息头中
	// var token=$("meta[name='_csrf']").attr('content')
    // var header=$("meta[name='_csrf_header']").attr('content')
	// $(document).ajaxSend(function(e,xhr,option) {
	// 	xhr.setRequestHeader(header,token)
	// })

	$.post(
        CONTEXT_PATH+'/discuss/add',
		{title:title,content:content},
		function (data) {
			data=$.parseJSON(data)
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show");
            setTimeout(function(){
                $("#hintModal").modal("hide");
                if(data.code==0) {
                	window.location.reload()
				}
            }, 2000);
		}
	)
}