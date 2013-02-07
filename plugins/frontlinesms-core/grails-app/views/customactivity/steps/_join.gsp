<%@ page import="frontlinesms2.Group" %>
<li class='join-action-step step' index='${stepId}'>
	<div><a class='remove-command remove-step'></a></div>
	<span class='step-title'>Add sender to group</span>
	<g:hiddenField name='stepId' value="${stepId}"/>
	<g:hiddenField name='stepType' value='join'/>
	<g:select name='group' id="" noSelection="${['null':'Select One...']}" from="${Group.getAll()}" value="${groupId}" optionKey="id" optionValue="name" class="notnull"/>
</li>
