<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= request.getAttribute("pageTitle") != null ? request.getAttribute("pageTitle") : "FishFinder NorCal" %></title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/app.js" defer></script>
</head>
<body>
    <nav class="navbar">
        <% if (session.getAttribute("userId") != null) { %>
            <a href="${pageContext.request.contextPath}/dashboard" class="navbar-brand">FishFinder NorCal</a>
        <% } else { %>
            <a href="${pageContext.request.contextPath}/home.html" class="navbar-brand">FishFinder NorCal</a>
        <% } %>
        <div class="navbar-links">
            <a href="${pageContext.request.contextPath}/locations">Locations</a>
            <% if (session.getAttribute("userId") == null) { %>
                <a href="${pageContext.request.contextPath}/login">Login</a>
                <a href="${pageContext.request.contextPath}/register">Register</a>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/reports">Reports</a>
                <a href="${pageContext.request.contextPath}/favorites">Favorites</a>
                <a href="${pageContext.request.contextPath}/account">Account</a>
                <% if ("moderator".equals(session.getAttribute("role"))) { %>
                    <a href="${pageContext.request.contextPath}/admin/users">Manage</a>
                <% } %>
                <a href="${pageContext.request.contextPath}/logout">Logout</a>
            <% } %>
        </div>
    </nav>
    <div class="main-content">
        <div class="container">
            <% if (session.getAttribute("successMessage") != null) { %>
                <div class="alert alert-success"><%= session.getAttribute("successMessage") %></div>
                <% session.removeAttribute("successMessage"); %>
            <% } %>
            <% if (session.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-error"><%= session.getAttribute("errorMessage") %></div>
                <% session.removeAttribute("errorMessage"); %>
            <% } %>
