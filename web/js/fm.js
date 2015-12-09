var app = angular.module('fmApp', []);
var myScope;
app.controller('ctrl', function ($scope, $http) {
	myScope = $scope;
	$scope.json = json;
	$scope.term = term;
	$scope.indexPath = indexedDir;
	$scope.isSearching = false;
	$scope.filterName = "";

	$scope.fileName = "host*";
	$scope.fileNameNot = "";
	$scope.fileFromSize = "";
	$scope.fileToSize = "";
	$scope.fileFromDate = "";
	$scope.fileToDate = "";
	//sorting
	$scope.sortNameAsc = true, $scope.sortSizeAsc = false, $scope.sortPathAsc = false;
	$scope.predicate = "size";
	$scope.reverse = false;
	$scope.duration = 0;

	$scope.search = function () {
		var t1 = window.performance.now();
		var qParams = ($scope.fileName === "") ? "" : "+name:" + $scope.fileName;
		qParams += ($scope.fileNameNot === "") ? "" : " -name:" + $scope.fileNameNot;
		if ($scope.fileFromSize !== "" || $scope.fileToSize !== "") {
			qParams += (qParams.trim() !== "" ? " AND " : "");
			if ($scope.fileFromSize !== "" && $scope.fileToSize !== "") {
				qParams += "size:[" + this.pad($scope.fileFromSize, 10) + " TO " + this.pad($scope.fileToSize, 10) + "] ";
			} else {
				qParams += "size:" + ($scope.fileFromSize !== "" ? this.pad($scope.fileFromSize, 10) :
						this.pad($scope.fileToSize, 10));
			}
		}
		if ($scope.fileFromDate !== "" || $scope.fileToDate !== "") {
			qParams += (qParams !== "" ? " AND " : "");
			if ($scope.fileFromDate !== "" && $scope.fileToDate !== "") {
				var fromDateTime = this.parseDate($scope.fileFromDate), 
					toDateTime = this.parseDate($scope.fileToDate);
				qParams += "modified:[" + fromDateTime + " TO " + toDateTime + "]";
			} else {
				qParams += "modified:" + ($scope.fileFromDate !== "" ? this.parseDate($scope.fileFromDate) : 
						this.parseDate($scope.fileToDate));
			}
		}
		$scope.term = qParams;
		$scope.isSearching = false;
		$http.get("FileManagerServlet?term=" + encodeURIComponent(qParams)).success(function (data) {
			$scope.json = data;
			$scope.duration = parseInt(window.performance.now() - t1);
		}).error(function () {
		}).finally(function () {
			$scope.isSearching = true;
		});
	};

	$scope.pad = function (n, width) {
		n = n + '';
		return n.length >= width ? n : new Array(width - n.length + 1).join('0') + n;
	};

	$scope.parseDate = function (dateString) {
//	var dateString = "16.11.2015 00:27";
		var regExLong  = /(\d{1,2}).(\d{1,2}).(\d{4}) (\d{1,2}):(\d{1,2})/;
		var regExShort = /(\d{1,2}).(\d{1,2}).(\d{4})/;
		if (dateString.match(regExLong) !== null) {
			var dateArr = regExLong.exec(dateString);
			return new Date(dateArr[3], dateArr[2] - 1, dateArr[1], dateArr[4], dateArr[5], 0).getTime();
		}
		if (dateString.match(regExShort) !== null) {
			var dateArr = regExShort.exec(dateString);
			return new Date(dateArr[3], dateArr[2] - 1, dateArr[1]).getTime();
		}
		return null;
	};

	$scope.sortCol = function (pred) {
		$scope.predicate = pred;
		switch (pred) {
			case "name":
				$scope.sortNameAsc = !$scope.sortNameAsc;
				break;
			case "size":
				$scope.sortSizeAsc = !$scope.sortSizeAsc;
				break;
			case "path":
				$scope.sortPathAsc = !$scope.sortPathAsc;
				break;
		}
		$scope.reverse = !$scope.reverse;
		return false;
	};

	$scope.sortNameIcon = function () {
		return $scope.sortNameAsc ? "↑" : "↓";
	};

	$scope.sortSizeIcon = function () {
		return $scope.sortSizeAsc ? "↑" : "↓";
	};

	$scope.sortPathIcon = function () {
		return $scope.sortPathAsc ? "↑" : "↓";
	};

});