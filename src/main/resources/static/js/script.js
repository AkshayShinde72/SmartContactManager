console.log("Sidebar script loaded");

$(document).ready(function () {
    window.toggleSidebar = function () {
        if ($(".sidebar").is(":visible")) {
            $(".sidebar").css("display", "none");
            $(".content").css("margin-left", "0%");
        } else {
            $(".sidebar").css("display", "block");
            $(".content").css("margin-left", "20%");
        }
    };
});

console.log("Sidebar script loaded");

function toggleSidebar() {
    const sidebar = document.querySelector(".sidebar");
    const content = document.querySelector(".content");

    if (getComputedStyle(sidebar).display === "none") {
        sidebar.style.display = "block";
        content.style.marginLeft = "20%";
    } else {
        sidebar.style.display = "none";
        content.style.marginLeft = "0%";
    }
}

