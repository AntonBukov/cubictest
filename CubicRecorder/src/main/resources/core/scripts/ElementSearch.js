(function () {
var Cubic = Cubic || {};

Cubic.dom = {};

Cubic.dom.serializeDomNode = function(domNode) {
	var data = {};
	if(!domNode.cubicId) {
		var date = new Date();
		domNode.cubicId = date.valueOf() + '';
	}
	data.parentCubicId = Cubic.dom.getParentCubicId(domNode.parentNode);
	
	data.properties = {};
	for(var i=0; i < Cubic.dom.serializeDomNode.properties.length; i++) {
		property = Cubic.dom.serializeDomNode.properties[i];
		if(typeof domNode[property] != "undefined") {
			data.properties[property] = domNode[property];			
		}
	}
	if((domNode.tagName == "INPUT" || domNode.tagName == "SELECT" || domNode.tagName == "TEXTAREA") && domNode.id) {
		var ancestor = domNode.ownerDocument;
		//var labels = Element.getElementsBySelector(ancestor, "label");
		var labels = ancestor.getElementsByTagName("label");

		for(var i=0; i < labels.length; i++) {
			if(labels[i].htmlFor == domNode.id) {
				data.label = labels[i].innerHTML;
			}
		}
	}
	
	if(domNode.tagName == "SELECT") {
		data.selected = domNode.value;
		
	}
	
	return toJSON(data);
}

/* escape a character */

escapeJSONChar =
function escapeJSONChar(c)
{
    if(c == "\"" || c == "\\") return "\\" + c;
    else if (c == "\b") return "\\b";
    else if (c == "\f") return "\\f";
    else if (c == "\n") return "\\n";
    else if (c == "\r") return "\\r";
    else if (c == "\t") return "\\t";
    var hex = c.charCodeAt(0).toString(16);
    if(hex.length == 1) return "\\u000" + hex;
    else if(hex.length == 2) return "\\u00" + hex;
    else if(hex.length == 3) return "\\u0" + hex;
    else return "\\u" + hex;
};


/* encode a string into JSON format */

escapeJSONString =
function escapeJSONString(s)
{
    /* The following should suffice but Safari's regex is b0rken
       (doesn't support callback substitutions)
       return "\"" + s.replace(/([^\u0020-\u007f]|[\\\"])/g,
       escapeJSONChar) + "\"";
    */

    /* Rather inefficient way to do it */
    var parts = s.split("");
    for(var i=0; i < parts.length; i++) {
	var c =parts[i];
	if(c == '"' ||
	   c == '\\' ||
	   c.charCodeAt(0) < 32 ||
	   c.charCodeAt(0) >= 128)
	    parts[i] = escapeJSONChar(parts[i]);
    }
    return "\"" + parts.join("") + "\"";
};
toJSON = function toJSON(o)
{
    if(o == null) {
	return "null";
    } else if(o.constructor == String) {
	return escapeJSONString(o);
    } else if(o.constructor == Number) {
	return o.toString();
    } else if(o.constructor == Boolean) {
	return o.toString();
    } else if(o.constructor == Date) {
	return '{javaClass: "java.util.Date", time: ' + o.valueOf() +'}';
    } else if(o.constructor == Array) {
	var v = [];
	for(var i = 0; i < o.length; i++) v.push(toJSON(o[i]));
	return "[" + v.join(", ") + "]";
    } else {
	var v = [];
	for(attr in o) {
	    if(o[attr] == null) v.push("\"" + attr + "\": null");
	    else if(typeof o[attr] == "function"); /* skip */
	    else v.push(escapeJSONString(attr) + ": " + toJSON(o[attr]));
	}
	return "{" + v.join(", ") + "}";
    }
};
Cubic.dom.serializeDomNode.properties = [
	'cubicId',
	'href',
	'id',
	'innerHTML',
	'label',
	'name',
	'src',
	'tagName',
	'type',
	'value',
	'alt'
];

Cubic.dom.getSelectedText = function(w) {
	w = w || window;
	var txt = '';
	var foundIn = '';
	if(w.getSelection)
	{
		txt = w.getSelection();
		foundIn = 'window.getSelection()';
	}
	else if(w.document.getSelection)
	{
		txt = w.document.getSelection();
		foundIn = 'document.getSelection()';
	}
	else if(w.document.selection)
	{
		txt = w.document.selection.createRange().text;
		foundIn = 'document.selection.createRange()';
	}

	return txt;	
}


/**
 * 
 * @param {Element} element
 */
Cubic.dom.getParentCubicId = function (element){
	if(typeof element.cubicId != "undefined"){
		return element.cubicId + "";
	}
	if(element.parentElement == null || typeof element.parentElement == "undefined"){
		return null;
	}else{
		return Cubic.dom.getParentCubicId(element.parentElement);
	}
}

// =================== XPATH 

    function getInputElementsByTypeAndValue(inputType, inputValue) {
        var inputs = document.getElementsByTagName('input');

        var result = new Array();

        for (var i = 0; i < inputs.length; i++) {
            if (inputs[i].type === inputType && inputs[i].value === inputValue) {
                result.push(inputs[i]);
            }
        }
        return result;
    }

    function getPathTo(element) {
        var elementTagName = element.tagName.toLowerCase();

        // Check if node has ID and this ID is unique over the document
        if (element.id && document.getElementById(element.id) === element) {
            return 'id("' + element.id + '")';
        }

        // Check element name
        else if (element.name && document.getElementsByName(element.name).length === 1) {
            return "//" + elementTagName + "[@name='" + element.name + "']";
        }

        // Submit value
        else if (elementTagName === "input" && getInputElementsByTypeAndValue("submit", element.value).length === 1) {
            return "input[@type='submit' and @value='" + element.value + "']";
        }

        if (element === document.body) {
            return '/html/' + elementTagName;
        }

        var ix = 0;
        var siblings = element.parentNode.childNodes;
        for (var i = 0; i < siblings.length; i++) {
            var sibling = siblings[i];
            if (sibling === element)
                return getPathTo(element.parentNode) + '/' + elementTagName + '[' + (ix + 1) + ']';
            if (sibling.nodeType === 1 && sibling.tagName.toLowerCase() === element.tagName.toLowerCase())
                ix++;
        }
    }

    function getPageXY(element) {
        var x = 0, y = 0;
        while (element) {
            x += element.offsetLeft;
            y += element.offsetTop;
            element = element.offsetParent;
        }
        return [x, y];
    }
    // ==========================

    // ====== SHOW DIV Coords==============
    function showPos(event, xpath) {
        
        var el, x, y;

        el = document.getElementById('lenPR_PopUp');
        
        if (window.event) {
            x = window.event.clientX + document.documentElement.scrollLeft + document.body.scrollLeft;
            y = window.event.clientY + document.documentElement.scrollTop + document.body.scrollTop;
        }
        else {
            x = event.clientX + window.scrollX;
            y = event.clientY + window.scrollY;
        }
        x -= 2; y -= 2;
        y = y+15;

		clearElementForm();
		
		var selection = '';
		if (window.getSelection) {
			selection = window.getSelection();
		} else if (document.getSelection) {
			selection = document.getSelection();
		} else if (document.selection) {
			selection = document.selection.createRange().text;
		}

		if (selection != '') {
			selection = selection + '';
			var s = selection;
			if (s.length > 10) {
				s = s.substring(0, 7) + "...";
			}
			addItem("Assert Text " + s + " Present", function() {
				closeClickHandler (event);
				addTextPresent(selection);
			});
		}

        divMenu.style.left = x + "px";
        divMenu.style.top = y + "px";
        divMenu.style.display = "block";

		if(lenPR_obj.tagName == "INPUT" || lenPR_obj.tagName == "TEXTAREA"){
			var menuItem = document.createElement("div");
			menuItem.innerHTML = "Enter text <input type='text' id='len_PopUp_text' onKeyDown='if(event.keyCode==13){this.click();}'/>";
			menuItem.addEventListener("click", function len_sent(){
				addTypeText(document.getElementById("len_PopUp_text").value);
			}, false);
			menuItem.addEventListener("mouseover", function(event) {
				var t = event.target || event.srcElement
				t.setAttribute("class", "over");
			}, false);
			menuItem.addEventListener("mouseout", function(event) {
				var t = event.target || event.srcElement
				t.setAttribute("class", "out");
			}, false);
			divMenu.appendChild(menuItem);
			document.getElementById("len_PopUp_text").focus();
		}
		var actions = ["Present", "Not present", "Click", "Double click", "Mouse over", "Mouse out", "Set focus", "Remove focus"];
		for(var i = 0; i < actions.length; i++){
			addAction(actions[i]);
		}
		
		addItem("Page Title Present", function() {
			closeClickHandler(event);
			addTitle();
		});
					
        document.getElementById("lenPR_PopUp_XPathLocator").innerHTML = xpath;

        console.log(x + ";" + y);
    }

    function addAction(name) {
		addItem(name,function(event) {
	    	if(lenPR_obj.tagName == "OPTION"){
				var JsonData = {
	            	command: "Present",
	            	elementXPath : document.getElementById("lenPR_PopUp_XPathLocator").firstChild.nodeValue,
		  			object : Cubic.dom.serializeDomNode(lenPR_obj.parentNode)
	    	    };
		        createCommand(JsonData);
	    	}
	    
			var JsonData = {
            	command: name,
            	elementXPath : document.getElementById("lenPR_PopUp_XPathLocator").firstChild.nodeValue,
	  			object : Cubic.dom.serializeDomNode(lenPR_obj)
    	    };

	        createCommand(JsonData);
		});        
    };

    function addTextPresent(text) {
        var JsonData = {
            command: "Text",
			text : text,
 	  		object : Cubic.dom.serializeDomNode(lenPR_obj)
        };

        createCommand(JsonData);
    };

    function addTypeText(text) {
        var JsonData = {
            command: "Enter text",
            text : text,
           	elementXPath : document.getElementById("lenPR_PopUp_XPathLocator").firstChild.nodeValue,
	  		object : Cubic.dom.serializeDomNode(lenPR_obj)
        };

        createCommand(JsonData);
    };

    function addTitle() {
        var JsonData = {
            command: "Title",
            title: document.title,
	  		object : Cubic.dom.serializeDomNode(document.getElementsByTagName("TITLE")[0])
        };

        createCommand(JsonData);
    };

    //===========================

    function createCommand(jsonData) {
    	if(typeof window.lenpr_command !== 'undefined'){
    		setTimeout(function(){
    		   createCommand(jsonData);
    		},500);
    	} else {
	        var myJSONText = JSON.stringify(jsonData, null, 2);
	        window.lenpr_command = myJSONText;
	    }
    }

   	function closeClickHandler(){
   		document.getElementById('lenPR_PopUp').style.display = 'none';
   	}

    var divMenu;
    
    function addItem(itemLabel, handler) {
		var menuItem = document.createElement("div");
		menuItem.innerHTML = itemLabel;
		menuItem.addEventListener("click", handler, false);
		menuItem.addEventListener("mouseover", function(event) {
			var t = event.target || event.srcElement
			t.setAttribute("class", "over");
		}, false);
		menuItem.addEventListener("mouseout", function(event) {
			var t = event.target || event.srcElement
			t.setAttribute("class", "out");
		}, false);
		divMenu.appendChild(menuItem);
	}

    function createElementForm() {
        //Create an input type dynamically.   
        divMenu = document.createElement("div");
        //Assign different attributes to the element. 
        divMenu.id = 'lenPR_PopUp';
        document.getElementsByTagName('body')[0].appendChild(divMenu);

		clearElementForm();

        divMenu.style.background = "#eeeeff";
        divMenu.style.position = "absolute";
        divMenu.style.border = "1px solid #333";
        divMenu.style.padding = "5px 5px 5px 5px";
        divMenu.style.zIndex = 2147483647;
    }

    function clearElementForm() {
        divMenu.innerHTML = 
        ' <table id="lenTable">' +
        '   <tr>' +
        '     <td><span id="lenPR_PopUp_XPathLocator">Element</span></td>' +
        '   </tr>' +
        '   </table>' + 
        '';
    }

    function addStyle(str) {
        var el = document.createElement('style');
        if (el.styleSheet) el.styleSheet.cssText = str;
        else {
            el.appendChild(document.createTextNode(str));
        }
        return document.getElementsByTagName('head')[0].appendChild(el);
    }

    function preventEvent(event)
    {
        if (event.preventDefault) event.preventDefault();
        event.returnValue = false;

        //IE9 & Other Browsers
        if (event.stopPropagation) {
            event.stopPropagation();
        }
            //IE8 and Lower
        else {
            event.cancelBubble = true;
        }

        return false;
    }


    // ========== MAIN !!!!!! ============================
    addStyle(".highlight { background-color:silver !important}");
    addStyle(".over {	background-color:#8888ff;	cursor:pointer;	}");
    addStyle("table#lenTable { border-collapse:collapse;} table#lenTable,table#lenTable th, table#lenTable td { font-family: Verdana, Arial; font-size: 10pt; padding-left:10pt; padding-right:10pt; border-bottom: 1px solid black; }");
    addStyle("div#lenPR_PopUp { display:none; } div#lenPR_PopUp_Element_Name { display:table; width: 100%; } ");


    createElementForm();
    //===================================================================

    var prev;
    window.len_prevActiveElement = undefined;

    if (document.body.addEventListener) {
        document.body.addEventListener('mouseover', handler, false);
		document.body.addEventListener('click', function (e) {
			document.getElementById('lenPR_PopUp').style.display = 'none';
		}, false);
        document.addEventListener('contextmenu', rightClickHandler, false);
    }
    else if (document.body.attachEvent) {
        document.body.attachEvent('mouseover', function (e) {
            return handler(e || window.event);
        });
        document.body.attachEvent('oncontextmenu', function (e) {
            return rightClickHandler(e || window.event);
        });
        document.body.attachEvent('click', function (e) {
            return closeClickHandler(e || window.event);
        });
    }
    else {
        document.body.onmouseover = handler;
        document.body.onmouseover = rightClickHandler;
    }


    function handler(event) {
        
        if (event.target === document.body
        || (prev && prev === event.target)) {
            return;
        }
        if (prev) {
            prev.className = prev.className.replace(/\bhighlight\b/, '');
            prev = undefined;
        }
        if (event.target && event.ctrlKey) {
            prev = event.target;
            prev.className += " highlight";
        }
    }

	var lenPR_obj;
    // =========================
    function rightClickHandler(event) { // Ctrl + Right button
 //        if (event.ctrlKey) {
             // =====================

             if (event === undefined) event = window.event;                     // IE hack
             var target = 'target' in event ? event.target : event.srcElement; // another IE hack

             var root = document.compatMode === 'CSS1Compat' ? document.documentElement : document.body;
             var mxy = [event.clientX + root.scrollLeft, event.clientY + root.scrollTop];

             var path = getPathTo(target);
             var txy = getPageXY(target);
             lenPR_obj = target;
             // alert('Clicked element '+path+' offset '+(mxy[0]-txy[0])+', '+(mxy[1]-txy[1]));

             // xpath = 'Clicked element '+path+' offset '+(mxy[0]-txy[0])+', '+(mxy[1]-txy[1]);

             var body = document.getElementsByTagName('body')[0];
             var xpath = path;

             /*var JsonData = {
                 "Command": "GetXPathFromElement",
                 "Caller": "EventListener : mousedown",
                 "XPathValue" : xpath,

             };
             createCommand(JsonData);
				*/
             showPos(event, xpath);

             return preventEvent(event);
 //        }
     }


    // ====================
    window.len_visual_search_injected = "len_visual_search_injected";
    // ====================


})();


