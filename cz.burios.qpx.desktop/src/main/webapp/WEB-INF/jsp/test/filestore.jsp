<%@ page language="java" contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="cs">
	<head>
		<meta charset="UTF-8">
		<meta name="viewport" content="width=device-width, initial-scale=1">

		<title>${appTitle}</title>

		<link rel="icon" href="/${appPath}/favicon.png">
		<link rel="stylesheet" href="/${appPath}/libs/qpx/themes/jquery.qpx.default.css?build=${timeNo}" rel="stylesheet" type="text/css">

		<style>
		.fs-layout{display:flex;gap:16px;min-height:420px}.fs-panel{padding:16px;border:1px solid #ddd;border-radius:4px}
		.fs-tree-panel{flex:0 0 320px}.fs-files-panel{flex:1 1 auto;min-width:0}.fs-toolbar{display:flex;gap:8px;align-items:center;flex-wrap:wrap;margin-bottom:12px}
		.fs-status{font-size:13px;min-height:20px}.fs-dir-title{font-weight:600;margin-bottom:8px}.fs-path{opacity:.7;font-size:13px;margin-bottom:12px}
		.fs-empty{padding:24px;text-align:center;opacity:.6}
		</style>

		<script type="text/javascript" src="/${appPath}/libs/jquery/jquery-3.7.1.js"></script>
		<script type="text/javascript" src="/${appPath}/libs/qpx/jquery.qpx.all.js?build=${timeNo}"></script>
	</head>
	<body class="qpx-view">
		<div class="qpx-test-topbar1"><div id="pageTopbar" style="width:100%"></div></div>
			<div class="qpx-test-content">
			<header class="page-head"><h1>FileStore – test</h1><p class="subtitle">Test fyzického úložiště, DB metadat a REST API.</p></header>
			<main>
				<div class="fs-toolbar">
					<button type="button" id="btnNewDir">Nová složka</button>
					<input type="file" id="fileInput"><button type="button" id="btnUpload">Nahrát</button>
					<span id="status" class="fs-status"></span>
				</div>
				<div class="fs-layout">
					<section class="fs-panel fs-tree-panel"><div class="fs-dir-title">Adresáře</div><div id="tree"></div></section>
					<section class="fs-panel fs-files-panel"><div class="fs-path" id="currentPath">Kořen</div><div id="grid"></div><div id="empty" class="fs-empty" style="display:none">Složka neobsahuje žádné soubory.</div></section>
				</div>
			</main>
		</div>
		<script>
		(function() {
			var contextPath = "${appPath}",
				api = contextPath + "/api/filestore",
				selectedDirectoryId = null,
				tree,
				grid;
			
			// console.log("contextPath: ", contextPath);
			// console.log("api: ", api);
			
			function setStatus(t, e) {
				$("#status").text(t || "").css("color", e ? "#b00020" : "");
			}
			function request(url, o){
				return $.ajax($.extend({
					url: url,
					dataType: "json"
				}, o || {}));
			}
			function loadDirectories(){
				return request(api + "/directories").then(buildTree);
			}
			function buildTree(roots) {
				var items=[{
					id: "__root__",
					parentId: null,
					text: "Kořen",
					expanded: true
				}];
				roots.forEach(function(d) {
					items.push({
						id: d.ID,
						parentId: "__root__",
						text: d.NAME,
						expanded: false
					});
				});
				if(tree) 
					tree.option("items",items); 
				else 
					tree=qpx.ui({
						view: "qpTreeView",
						items: items,
						selectionMode: "single",
						onSelectionChanged:function(e) {
							var key = e.selectedItemKeys && e.selectedItemKeys.length ? e.selectedItemKeys[0] : "__root__";
							var item = items.find(function(x) {
								return String(x.id) === String(key);
							});
							selectDirectory(key === "__root__" ? null : key, key === "__root__" ? "Kořen" : (item?item.text:key));
						}
					}, "#tree");
					selectDirectory(null, "Kořen");
			}
			function selectDirectory(id, name) {
				selectedDirectoryId = id;
				$("#currentPath").text("Složka: " + name);
				loadFiles();
			}
			function loadFiles(){
				if (!selectedDirectoryId){
					renderFiles([]);
					return;
				}
				request(api + "/files",{
					data:{
						fileStoreId: selectedDirectoryId
					}
				}).done(renderFiles).fail(function(x) {
					setStatus("Načtení souborů selhalo: " + x.status,true);
				});
			}
			function renderFiles(files) {
				$("#empty").toggle(!files.length);
				var data=files.map(function(f){return{id:f.ID,name:f.NAME,extension:f.EXTENSION||"",size:f.SIZE,contentType:f.CONTENT_TYPE||""};});
				if (grid){
					grid.option("dataSource", data);
					return;
				}
				grid = qpx.ui({
					view: "qpDataGrid",
					width: "100%",
					height: 320,
					keyExpr: "id",
					dataSource: data,
					columns:[
						{ dataField: "name", caption: "Název", minWidth: 180 },
						{ dataField: "extension", caption: "Typ", width: 80 },
						{ dataField: "contentType", caption: "Content-Type", width: 180 },
						{ dataField: "size", caption: "Velikost", width: 100, dataType: "number", alignment: "right" },
						{ caption: "Akce", width: 100, cellTemplate: function(c, o) {
							$("<a>",{
								href:api + "/files/" + encodeURIComponent(o.data.id) + "/content",
								target: "_blank",
								text: "Stáhnout"
							}).appendTo(c);
						}}
					],
					sorting: { mode: "single" },
					selection:{mode:"single"},
					showBorders: true, responsive: true
				}, "#grid");
			}
			$("#btnNewDir").on("click", function() {
				var name = window.prompt("Název nové složky:");
				if(!name)
					return;
				request(api + "/directories", {
					method: "POST",
					data: {
						parentId: selectedDirectoryId || "",
						name: name
					}
				}).done(function() {
					setStatus("Složka vytvořena.");
					loadDirectories();
				}).fail(function(x) {
					setStatus("Vytvoření složky selhalo: " + (x.responseText || x.status), true);
				});
			});
			$("#btnUpload").on("click",function(){
				var input=$("#fileInput")[0];
				if(!selectedDirectoryId){setStatus("Nejprve vyberte konkrétní složku.",true);return;}
				if(!input.files.length){setStatus("Vyberte soubor.",true);return;}
				var form=new FormData();form.append("fileStoreId",selectedDirectoryId);form.append("file",input.files[0]);
				setStatus("Nahrávám…");
				$.ajax({
					url: api + "/files",
					method: "POST",
					data: form,
					processData: false,
					contentType: false,
					dataType: "json"
				}).done(function(){
					input.value="";
					setStatus("Soubor byl uložen.");
					loadFiles();
				}).fail(function(x) {
					setStatus("Nahrání selhalo: " + (x.responseText || x.status), true);
				});
			});
			$(function() {
				loadDirectories().fail(function(x) {
					setStatus("Načtení adresářů selhalo: " + x.status,true);
				});
			});
		})();
		</script>
	</body>
</html>