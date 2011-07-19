$(document).ready(function() {
	$('tr :checkbox[checked="true"]').parent().parent().addClass('checked');
});

function setStarStatus(object,data){
	if($("#"+object).hasClass("starred")) {
		$("#"+object).removeClass("starred");
	}
	
	$("#"+object).addClass(data);
	if(data != '') {
		$("#"+object).empty().append("Remove Star");
	} else {
		$("#"+object).empty().append("Add Star");
	}	
}

var messageDetails;

function checkAllMessages(){
	if($(':checkbox')[0].checked){
		$(':checkbox').each(function(index){
			this.checked = true;
			if(index > 0){
				$(this).parent().parent().addClass('checked');
				addMessageIdToList(this.value);
			}
		});
		
		changeMessageCount($(':checkbox').size()-1);
	} else {
		$(':checkbox').each(function(index, element){
			this.checked = false;
			if(index > 0)			
				$(this).parent().parent().removeClass('checked');
		});
		emptyMessageList();
		showMessageDetails();
	}
	
}

function updateMessageDetails(id){
	highlightRow(id); 
	
	var count = countCheckedMessages();
	if(count == 1) {
		loadCheckedMessage();
	}
	if(count > 1){
		changeMessageCount(count);
	}
}

function loadCheckedMessage() {
	var location = window.location.pathname
	var messageSection = $('input:hidden[name=messageSection]').val();
	var ownerId = $('input:hidden[name=ownerId]').val();
	var messageId = $('tr :checkbox[checked="true"]').val();
	
	if(messageSection == 'sent' || messageSection == 'pending' || messageSection == 'trash') {
		window.location = "/frontlinesms2/message/"+messageSection+"?messageId="+messageId+"&checkedId="+messageId;
	}else if(messageSection == 'poll' || messageSection == 'folder'){
		window.location = "/frontlinesms2/message/"+messageSection+"/"+ownerId+"/show/"+messageId+"?checkedId="+messageId;
	} else{
		window.location = "/frontlinesms2/message/"+messageSection+"/show/"+messageId+"?checkedId="+messageId;
	}
}

function countCheckedMessages(){
	var count = 0;
	$(':checkbox').each(function(){
			if(this.checked){
				count++;
				addMessageIdToList(this.value);
			} else{
				removeMessageIdFromList(this.value);
			}
		});
	
	return validateCheckedMessageCount(count);
}

function validateCheckedMessageCount(count) {
	//Check whether all messages are checked
	if(count == $(':checkbox').size()-1 && !$(':checkbox')[0].checked){
			$(':checkbox')[0].checked = true;
		} else if($(':checkbox')[0].checked){
			$(':checkbox')[0].checked = false;
			count--;
		}
		return count;
}

function changeMessageCount(count){
	if(messageDetails == null){
		messageDetails = $('#message-details').html();
	}
	$('#message-details').empty();
	$('#message-details').append("<p> "+count+" messages selected</p>");
	setMessageActions();
}

function setMessageActions() {
	var replyAll = '';
	var messageSection = $('input:hidden[name=messageSection]').val()
	if(messageSection != 'pending'){
		replyAll = "<a id='btn_reply_all' >Reply All</a>";
	}
	deleteAll = "<a id='btn_delete_all' >Delete All</a>";
	$('#message-details').append("<div class='buttons'></div>");
	$('#message-details div.buttons').append(replyAll+"&nbsp;"+deleteAll);
	$('#btn_reply_all').click(quickReplyClickAction);
	$('#btn_delete_all').click(deleteAllClickAction);
}

function quickReplyClickAction() {
	var me = $(this);
	var messageType = me.text();
	var checkedMessageIdList = $('input:hidden[name=checkedMessageIdList]').val();

	$.ajax({
		type:'POST',
		data: {checkedMessageIdList: checkedMessageIdList},
		url: '/frontlinesms2/quickMessage/create',
		success: function(data, textStatus){ launchWizard(messageType, data); }
	});
}

function deleteAllClickAction() {
	var me = $(this);
	var messageType = me.text();
	var checkedMessageIdList = $('input:hidden[name=checkedMessageIdList]').val();
	var messageSection = $('input:hidden[name=messageSection]').val();
	var ownerId = $('input:hidden[name=ownerId]').val();
	$.ajax({
		type:'POST',
		context:document.body,
		data: {messageSection: messageSection, checkedMessageIdList: checkedMessageIdList, ownerId: ownerId},
		url: '/frontlinesms2/message/deleteMessage',
		success: function(data) { $(this).empty().append(data); }
	});
}

function showMessageDetails(){
	if(messageDetails == null){
		return;
	}
	$('#message-details').empty();
	$('#message-details').html(messageDetails);
}

function highlightRow(id){
	if( $('tr :checkbox[value='+id+']').attr('checked') == 'checked'){
		$("#message-"+id).addClass('checked')
	} else {
		$("#message-"+id).removeClass('checked')
	}
}

function removeMessageIdFromList(id) {
	var f = $('input:hidden[name=checkedMessageIdList]');
	var oldList = f.val().replace(0 +',', ",");
	if(oldList.indexOf(id + ',') != -1) {
		var newList = oldList.replace(id +',', ",");
			f.val(newList);
	}
}

function addMessageIdToList(id) {
	var f = $('input:hidden[name=checkedMessageIdList]');
	var oldList = f.val();
	
	if(oldList.indexOf(id + ',') == -1) {
		var newList;
		newList = oldList + id + ',';
		f.val(newList);
	}
}

function emptyMessageList() {
	var f = $('input:hidden[name=checkedMessageList]');
	f.val("");
}
