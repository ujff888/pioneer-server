<%@ page language="java" contentType="text/html; charset=UTF-8" isELIgnored="false"
    pageEncoding="UTF-8"%>
 <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
 <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:out value="${error}"></c:out>
<c:forEach items="${roundList}" var="round">
	<h2>第${round.seqNo}回合</h2>
	<p>
		<strong><span style="color:red">进攻方：</span></strong>  <br/>
		<c:forEach items="${round.roundInfoOff}" var="entry">  
	       
	     	  兵力情况:<br/>
	        <c:forEach items="${entry.value.roundTroopInfoMap}" var="info">
	        	<ul>
	        		<li>兵种ID:${info.value.troopId}</li>
	        		<li>数量：${info.value.count}</li>
	        		<li>损失：${info.value.lost}</li>
	        	</ul>
	        </c:forEach>
		</c:forEach>  
		<br/>
		士气：<fmt:formatNumber value="${round.roundDetailOff.morale}" type="percent"/><br/><br/>
		阵地情况：<br/>
		<c:forEach items="${round.roundDetailOff.roundTroopDetailMap}" var="entry">  
	        <ul>
	        	<li>兵种ID:${entry.value.troopId}</li>
	        	<li>位置：
	        		<c:choose>
	        			<c:when test="${entry.value.fieldType == 'FIELD_REMOTE'}">远程</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_CLOSE'}">近战</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_FIRE'}">火炮</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_SIDE'}">侧翼</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_FLY'}">空战</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_FLY_FIRE'}">轰炸</c:when>
	        		</c:choose>
	        	</li>
	        	<li>数量：${entry.value.count}</li>
	        	<li>损失：${entry.value.lost}</li>
	        	<li>弹药量：
	        		<c:if test="${entry.value.amountRemain == -1.0}">
	        			无弹药
	        		</c:if>
	        		<c:if test="${entry.value.amountRemain != -1.0}">
	        			<fmt:formatNumber value="${entry.value.amountRemain}" type="percent" />
	        		</c:if>
	        	</li>
	        </ul>
		</c:forEach>  
		<br/><br/>
		
		<strong><span style="color:green">防守方：</span></strong>  <br/>
		<c:forEach items="${round.roundInfoDef}" var="entry">  
	        兵力情况:<br/>
	        <c:forEach items="${entry.value.roundTroopInfoMap}" var="info">
	        	<ul>
	        		<li>兵种ID:${info.value.troopId}</li>
	        		<li>数量：${info.value.count}</li>
	        		<li>损失：${info.value.lost}</li>
	        	</ul>
	        </c:forEach>
		</c:forEach>  
		<br/>
		士气：<fmt:formatNumber value="${round.roundDetailDef.morale}" type="percent"/><br/><br/>
		阵地情况：<br/>
		<c:forEach items="${round.roundDetailDef.roundTroopDetailMap}" var="entry">  
	        <ul>
	        	<li>兵种ID:${entry.value.troopId}</li>
	        	<li>位置：
	        		<c:choose>
	        			<c:when test="${entry.value.fieldType == 'FIELD_REMOTE'}">远程</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_CLOSE'}">近战</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_FIRE'}">火炮</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_SIDE'}">侧翼</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_FLY'}">空战</c:when>
	        			<c:when test="${entry.value.fieldType == 'FIELD_FLY_FIRE'}">轰炸</c:when>
	        		</c:choose>
	        	</li>
	        	<li>数量：${entry.value.count}</li>
	        	<li>损失：${entry.value.lost}</li>
	        	<li>弹药量：
	        		<c:if test="${entry.value.amountRemain == -1.0}">
	        			无弹药
	        		</c:if>
	        		<c:if test="${entry.value.amountRemain != -1.0}">
	        			<fmt:formatNumber value="${entry.value.amountRemain}" type="percent" />
	        		</c:if>
	        	</li>
	        </ul>
		</c:forEach>  
		<br/><br/>
	</p>	
</c:forEach>
战斗结果： 
<c:choose>
	<c:when test="${result == 1}">
	进攻方胜利 
	</c:when>
	<c:when test="${result == 2}">
	防守方胜利 
	</c:when>
	<c:when test="${result == 3}">
	双方不分胜负 
	</c:when>
</c:choose>
