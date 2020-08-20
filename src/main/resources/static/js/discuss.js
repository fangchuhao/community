$(function () {
    $("#topBtn").click(setTop)
    $("#essenceBtn").click(setEssence)
    $("#delBtn").click(delPost)
})
function like(button,entityType,entityId,beLikedUserId,postId) {
    $.post(
        CONTEXT_PATH+"/like",
        {"entityType":entityType,"entityId":entityId,"beLikedUserId":beLikedUserId,"postId":postId},
        function(data) {
            data=$.parseJSON(data)
            if(data.code==0) {
                $(button).children("i").text(data.likeCount)
                $(button).children("b").text(data.status==1?'已赞':'赞')
            }else {
                alert(data.msg)
            }
        }
    )
}
function setTop() {
    $.post(
        CONTEXT_PATH+"/discuss/top",
        {"id":$("#post-id").val()},
        function (data) {
            data=$.parseJSON(data)
            if(data.code==0) {
                $("#topBtn").attr("disabled","disabled")
            }else {
                alert(data.msg)
            }
        }

    )
}
function setEssence() {
    $.post(
        CONTEXT_PATH+"/discuss/essence",
        {"id":$("#post-id").val()},
        function (data) {
            data=$.parseJSON(data)
            if(data.code==0) {
                $("#essenceBtn").attr("disabled","disabled")
            }else {
                alert(data.msg)
            }
        }
    )
}
function delPost() {
    $.post(
        CONTEXT_PATH+"/discuss/block",
        {"id":$("#post-id").val()},
        function (data) {
            data=$.parseJSON(data)
            if(data.code==0) {
               location.href=CONTEXT_PATH+"/index"
            }else {
                alert(data.msg)
            }
        }

    )
}