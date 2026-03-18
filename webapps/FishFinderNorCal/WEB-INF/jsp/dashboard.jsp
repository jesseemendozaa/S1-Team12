<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<jsp:include page="header.jsp" />

<h1>Dashboard</h1>
<p>Welcome back, <strong><%= session.getAttribute("username") %></strong>!</p>

<div class="card-grid">
    <div class="card">
        <h3>My Reports</h3>
        <p class="stat-number"><%= request.getAttribute("reportCount") %></p>
        <a href="${pageContext.request.contextPath}/reports?userId=mine" class="btn btn-primary">View Reports</a>
    </div>
    <div class="card">
        <h3>My Favorites</h3>
        <p class="stat-number"><%= request.getAttribute("favoriteCount") %></p>
        <a href="${pageContext.request.contextPath}/favorites" class="btn btn-primary">View Favorites</a>
    </div>
    <div class="card">
        <h3>Total Locations</h3>
        <p class="stat-number"><%= request.getAttribute("locationCount") %></p>
        <a href="${pageContext.request.contextPath}/locations" class="btn btn-primary">Browse Locations</a>
    </div>
    <div class="card">
        <h3>Species Database</h3>
        <p class="stat-number"><%= request.getAttribute("speciesCount") %></p>
    </div>
</div>

<h2>Recent Catch Reports</h2>
<%
    List<Map<String, Object>> recentReports = (List<Map<String, Object>>) request.getAttribute("recentReports");
    if (recentReports != null && !recentReports.isEmpty()) {
%>
<table class="data-table">
    <thead>
        <tr>
            <th>Date</th>
            <th>Species</th>
            <th>Location</th>
            <th>Angler</th>
            <th>Weight (lbs)</th>
            <th>Length (in)</th>
        </tr>
    </thead>
    <tbody>
        <% for (Map<String, Object> report : recentReports) { %>
        <tr>
            <td><a href="${pageContext.request.contextPath}/reports?id=<%= report.get("reportId") %>"><%= report.get("catchDate") %></a></td>
            <td><%= report.get("speciesName") %></td>
            <td><%= report.get("locationName") %></td>
            <td><%= report.get("username") %></td>
            <td><%= report.get("weightLbs") != null ? report.get("weightLbs") : "-" %></td>
            <td><%= report.get("lengthInches") != null ? report.get("lengthInches") : "-" %></td>
        </tr>
        <% } %>
    </tbody>
</table>
<% } else { %>
<p>No catch reports yet. Be the first to log a catch!</p>
<% } %>

<% if ("moderator".equals(session.getAttribute("role"))) { %>
<h2>Moderator Tools</h2>
<div class="card">
    <h3>Moderation</h3>
    <p>Manage users, review reports, and moderate content.</p>
    <a href="${pageContext.request.contextPath}/admin/users" class="btn btn-primary">Manage Users</a>
</div>
<% } %>

<h2>Quick Actions</h2>
<div class="hero-actions">
    <a href="${pageContext.request.contextPath}/report/new" class="btn btn-primary">Log a Catch</a>
    <a href="${pageContext.request.contextPath}/location/new" class="btn btn-accent">Add Location</a>
</div>

<jsp:include page="footer.jsp" />
