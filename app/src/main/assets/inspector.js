$(function () {
    var tree = null;
    $.ajax({
        url: '/api/tree',
        type: 'GET',
        dataType: 'json',
        async: false,
        success: function (data) {
            tree = data;
        }
    });

    var scale = $("#screen>img").height() / tree.node.bounds.bottom;
    $("#screen>img").css({
        height: $("#screen>img").height() + "px",
    });
    $("#screen").css({
        height: $("#screen").height() + "px",
    });
    $("#container").css({
        height: $("#container").outerHeight() + "px"
    });
    $("#panel1").css({
        'width': $("#screen").width() + "px",
    });
    $("#panel2").css({
        'margin-left': $("#screen").width() + "px",
    });
    $("#prepare-mask").hide();


    function formatTree(tree, index, xpath) {
        index = index || 0;
        tree.node.xpath = (xpath || '/') + '/' + tree.node.cls + '[' + index + ']' +
            (tree.node.id ? "[@resource-id='" + tree.node.id + "']" : '') +
            (tree.node.text ? "[@text='" + tree.node.text + "']" : '') +
            (tree.node.desc ? "[@content-desc='" + tree.node.desc + "']" : '');
        tree.node.index = index;
        tree.node.bounds.centerX = (tree.node.bounds.left + tree.node.bounds.right) / 2
        tree.node.bounds.centerY = (tree.node.bounds.top + tree.node.bounds.bottom) / 2
        tree.node.bounds.width = tree.node.bounds.right - tree.node.bounds.left;
        tree.node.bounds.height = tree.node.bounds.bottom - tree.node.bounds.top;
        if (tree.children) {
            let i = 0;
            tree.children.forEach(element => {
                formatTree(element, i, tree.node.xpath);
                i++;
            });
        }
    }
    formatTree(tree);

    function treeToList(tree) {
        var treeNodes = {};
        treeNodes[tree.node.hash] = tree.node;
        if (tree.children) {
            tree.children
                .forEach(element => {
                    var children = treeToList(element);
                    for (const key in children) {
                        treeNodes[key] = children[key];
                    }
                });
        }
        return treeNodes;
    }
    var treeNodes = treeToList(tree);

    function genRange(tree, base) {
        var $dom = $(`<div class="node" id="rnode-${tree.node.hash}" data-hash="${tree.node.hash}"></div>`);
        var baseTop = (base && base.top + 0) || 0;
        var baseLeft = (base && base.left + 0) || 0;
        var baseLv = (base && base.lv + 0) || 0;
        $dom.css({
            top: (tree.node.bounds.top - baseTop) * scale + "px",
            left: (tree.node.bounds.left - baseLeft) * scale + "px",
            width: (tree.node.bounds.right - tree.node.bounds.left) * scale + "px",
            height: (tree.node.bounds.bottom - tree.node.bounds.top) * scale + "px",
        });
        $dom.addClass("node" + baseLv);
        if (tree.children) {
            tree.children
                .forEach(element => {
                    $dom.append(
                        genRange(element, {
                            top: tree.node.bounds.top,
                            left: tree.node.bounds.left,
                            lv: baseLv + 1
                        })
                    );
                });
        }
        return $dom;
    }

    function genTree(tree, base) {
        var $dom = $(
            `<div class="node" id="tnode-${tree.node.hash}" data-hash="${tree.node.hash}"><span class="name">cls(${tree.node.cls})${tree.node.id ? '.id('+tree.node.id+')' : ''}${tree.node.text ? '.text('+tree.node.text+')' : ''}${tree.node.desc ? '.desc('+tree.node.desc+')' : ''}</span></div>`
        );
        var baseLv = (base && base.lv + 0) || 0;
        $dom.addClass("node" + baseLv);
        if (baseLv == 0) {
            $dom.css('display', 'block');
        }
        if (!tree.node.visibleToUser) {
            $dom.addClass('invisible');
        }
        if (tree.children) {
            $dom.addClass('descendants');
            tree.children
                .forEach(element => {
                    $dom.append(
                        genTree(element, {
                            lv: baseLv + 1
                        })
                    );
                });
        }
        return $dom;
    }

    function displayAttrs(node) {
        node = node || treeNodes[$("#tree .node.current:first").data('hash')];
        $("#attrs table td.value").html('');
        for (const key in node) {
            $(`tr#attr-${key} td.value`).text(typeof node[key] == 'object' ? JSON.stringify(node[key]) : node[key]);
        }
    }
    $("#screen").append(genRange(tree));
    $("#tree").append(genTree(tree));
    $("#screen").mousemove(function (e) {
        $('#screen .node').removeClass("hover");
        $('#tree .node').removeClass("hover expand");
        for (const key in treeNodes) {
            var bounds = treeNodes[key].bounds;
            if (bounds.left * scale < e.offsetX && e.offsetX < bounds.right * scale &&
                bounds.top * scale < e.offsetY && e.offsetY < bounds.bottom * scale) {
                if (!$("#rnode-" + key).is('.node0')) {
                    $("#rnode-" + key).addClass("hover");
                    $("#tnode-" + key).addClass("hover");
                }
                $("#tnode-" + key).parents('.node').removeClass("hover").add($("#tnode-" + key)).addClass(
                    "expand");
            }
        }
        displayAttrs(treeNodes[$("#tree .node.hover:first").data('hash')]);
    }).mouseleave(function (e) {
        $('#tree .node').removeClass("hover expand");
        $('#tree .current').parents('.node').add($('#tree .current')).addClass("expand");
    }).click(function (e) {
        $('#screen .node').removeClass("current");
        $('#tree .node').removeClass("current expand");
        for (const key in treeNodes) {
            var bounds = treeNodes[key].bounds;
            if (bounds.left * scale < e.offsetX && e.offsetX < bounds.right * scale &&
                bounds.top * scale < e.offsetY && e.offsetY < bounds.bottom * scale) {
                if (!$("#rnode-" + key).is('.node0')) {
                    $("#rnode-" + key).addClass("current");
                    $("#tnode-" + key).addClass("current");
                }
                $("#rnode-" + key).parents('.node').removeClass("current");
                $("#tnode-" + key).parents('.node').removeClass("current").add($("#tnode-" + key)).addClass(
                    "expand");
            }
        }
        displayAttrs();
    });

    $("#tree")
        .mouseleave(function (e) {
            $('#tree .node').removeClass("hover");
            displayAttrs();
        })
        .on("mouseover", ".node", function (e) {
            e.stopPropagation();

            $('#screen .node').removeClass("hover");
            $('#tree .node').removeClass("hover");
            $(this).addClass("hover");
            $("#rnode-" + $(this).data("hash")).addClass("hover");
            displayAttrs(treeNodes[$(this).data("hash")]);
        })
        .on("click", ".node", function (e) {
            e.stopPropagation();

            $('#screen .node').removeClass("current");
            $('#tree .node').removeClass("current expand");
            $(this).addClass("current");
            $("#rnode-" + $(this).data("hash")).addClass("current");
            $(this).parents('.node').add(this).addClass("expand");
            displayAttrs(treeNodes[$(this).data("hash")]);
        });
});