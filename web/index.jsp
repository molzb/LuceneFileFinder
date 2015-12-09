<%@page import="de.bernhard.FileModel"%>
<%@page import="de.bernhard.FileManagerLucene"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page trimDirectiveWhitespaces="true"%>

<!DOCTYPE html>
<%
	FileManagerLucene fm = new FileManagerLucene();
	fm.createIndex(fm.indexedDir);
	List<FileModel> res = fm.searchTerm(fm.FIELD_NAME + ":host*");
	String json = fm.toJson(res);
%>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<meta name="keywords" content="Lucene, Big Data, Solr">
		<meta name="robots"   content="index,follow">
		<meta name="DC.title" content="Lucene, pretty basic example">
        <title>Lucene Sample Web App</title>
		<link rel="stylesheet" href="css/fm.css" type="text/css"/>
		<script type="text/javascript" src="js/angular.min.js"></script>
		<script type="text/javascript">var json = <%=json%>, indexedDir = "<%=fm.indexedDir%>", term = "<%=fm.term%>"</script>
		<script type="text/javascript" src="js/fm.js"></script>
    </head>
    <body data-ng-app="fmApp" data-ng-controller="ctrl">
        <div id="container">
			<div id="autoHeight">
				<h1>Lucene File Finder</h1>
				<p>Sample web app, where you can use the Lucene API to find files on a hard disk.<br/>
					The files on the hard disk has been indexed before by Lucene to enable fast searches.<br/><br/>
					Don't expect it to behave exactly like Windows Search, when it comes to jokers and wildcards.<br/>
					This webapp is just to <b>demonstrate Lucene searches</b>.<br/><br/>
					Download the sources <a href="">here</a> (as a Netbeans project).
				</p>
				<table class="fileTableHead">
					<thead>
						<tr>
							<th class="col1"></th>
							<th class="col2">
								<img src="images/drive.png"/>
								<!--input type="text" disabled value="{{indexPath}}"/-->
								<span style="vertical-align: top">C:</span><br/>
								<b><%=fm.numDocs%> Documents (=directories and files)</b><br/><br/>
								Lucene Query:<br/>
								<input name="txtTerm" type="text" ng-model="term" disabled=""/>
							</th>
							<th class="col3"></th>
							<th class="col4">
								<span>Name:</span> 
								<input name="txtSearchFile" data-ng-keyup="$event.keyCode === 13 && search()" type="text" 
									   placeholder="e.g. host*" data-ng-model="fileName"/>
								<span>but not: </span>
								<input name="txtSearchFileNot" data-ng-keyup="$event.keyCode === 13 && search()" type="text" 
									   placeholder="e.g. hostname.exe" data-ng-model="fileNameNot"/><br/>
								<span>Size >=</span>
								<input name="txtFromSize" data-ng-keyup="$event.keyCode === 13 && search()" type="text" 
									   placeholder="e.g. 500" data-ng-model="fileFromSize"/> 
								<span>and &lt;=</span>
								<input name="txtToSize" data-ng-keyup="$event.keyCode === 13 && search()" type="text" 
									   placeholder="e.g. 1000" data-ng-model="fileToSize"/>bytes<br/>
								<span>Date >=</span>
								<input name="txtFromDate" data-ng-keyup="$event.keyCode === 13 && search()" type="text" 
									   placeholder="2.2.2011 22:22" data-ng-model="fileFromDate"/> 
								<span>and &lt;=</span>
								<input name="txtToDate" data-ng-keyup="$event.keyCode === 13 && search()" type="text" 
									   placeholder="2.2.2011 23:23" data-ng-model="fileToDate"/><br/>
								<input type="button" id="btnSearch" data-ng-click="search()" value="Find!"/>
								<img id="imgWait" src="images/wait20trans.gif" alt="Searching..." 
									 data-ng-class="{'hidden': isSearching}" class="hidden"/>
							</th>
							<th class="col5"></th>
						</tr>
						<tr>
							<th class="col1"></th>
							<th class="col2">
								Name<a data-ng-click="sortCol('name');">{{sortNameIcon()}}</a>
								<span class="filter">
									<img src="images/filter.png" alt="Filter name"/>
									<!--<span class="icon icon_filter" title="Filter, no regexes here"/>-->
									<input name="txtFilterName" type="text" data-ng-model="filterName"/>
								</span>
							</th>
							<th class="col3">Modified<a data-ng-click="sortCol('size');">{{sortSizeIcon()}} </a></th>
							<th class="col4">Path<a data-ng-click="sortCol('path');">{{sortPathIcon()}} </a></th>
							<th class="col5">Size<a data-ng-click="sortCol('size');">{{sortSizeIcon()}} </a></th>
						</tr>
					</thead>
				</table>
			</div>
			<div id="remainingHeight">
				<div id="tblWrapper">
					<table class="fileTable">
						<tbody>
							<tr data-ng-repeat="j in filtered = (json| filter: filterName | orderBy: predicate: reverse)">
								<td class="col1">
									<span class="icon" ng-class="j.dir ? 'icon_folder' : 'icon_file'"/>
								</td>
								<td class="col2" data-ng-class="j.hidden ? 'hiddenFile' : ''">{{j.name}}</td>
								<td class="col3">{{j.lastModifiedStr}}</td>
								<td class="col4">{{j.path}}</td>
								<td class="col5">{{j.size}}</td>
							</tr>
						</tbody>
					</table>
				</div>
				<span><br/><b>{{filtered.length}} Element(s)</b></span>
				<span id="perf">Lucene search: <span id="perfMs">{{duration}} ms</span></span>
			</div><!-- remaining -->
		</div><!-- container -->
	</body>
</html>
