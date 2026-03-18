<%@ page import="java.util.*, java.sql.Timestamp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<%
Map<String, Object> report = (Map<String, Object>) request.getAttribute("report");
boolean isOwner = Boolean.TRUE.equals(request.getAttribute("isOwner"));
boolean isMod = Boolean.TRUE.equals(request.getAttribute("isMod"));
int reportId = (int) report.get("reportId");
%>

<h1>Catch Report Details</h1>

<div class="info-section">
    <p><strong>Species:</strong> <%= report.get("speciesName") %></p>
    <p><strong>Location:</strong> <a href="<%= request.getContextPath() %>/location?id=<%= report.get("locationId") %>"><%= report.get("locationName") %></a></p>
    <p><strong>Angler:</strong> <%= report.get("username") != null ? report.get("username") : "Anonymous" %></p>
    <p><strong>Date:</strong> <%= report.get("catchDate") != null ? report.get("catchDate") : "N/A" %></p>
    <p><strong>Weight:</strong> <%= report.get("weightLbs") != null ? report.get("weightLbs") + " lbs" : "N/A" %></p>
    <p><strong>Length:</strong> <%= report.get("lengthInches") != null ? report.get("lengthInches") + " in" : "N/A" %></p>
    <p><strong>Method:</strong> <%= report.get("method") != null ? report.get("method") : "N/A" %></p>
    <p><strong>Notes:</strong> <%= report.get("notes") != null ? report.get("notes") : "N/A" %></p>
    <p><strong>Submitted on:</strong> <%= report.get("createdAt") != null ? report.get("createdAt") : "N/A" %></p>
</div>

<% if (isOwner || isMod) { %>
    <p>
        <a href="<%= request.getContextPath() %>/report/edit?id=<%= reportId %>&edit=true" class="btn">Edit Report</a>
    </p>
    <form method="POST" action="<%= request.getContextPath() %>/report/edit" class="inline-form"
          onsubmit="return confirm('Are you sure you want to delete this report?');">
        <input type="hidden" name="reportId" value="<%= reportId %>">
        <input type="hidden" name="action" value="delete">
        <button type="submit" class="btn btn-danger">Delete Report</button>
    </form>
<% } %>

<h2>Comments</h2>

<%
List<Map<String, Object>> comments = (List<Map<String, Object>>) request.getAttribute("comments");
if (comments == null || comments.isEmpty()) {
%>
    <p>No comments yet.</p>
<%
} else {
    for (Map<String, Object> c : comments) {
%>
    <div class="comment-box">
        <p><strong><%= c.get("username") != null ? c.get("username") : "Anonymous" %></strong>
           <span class="comment-date"><%= c.get("createdAt") %></span></p>
        <p><%= c.get("commentText") %></p>
    </div>
<%
    }
}
%>

<%
HttpSession sess = request.getSession(false);
if (sess != null && sess.getAttribute("userId") != null) {
%>
    <form method="POST" action="<%= request.getContextPath() %>/comment" class="form-card">
        <input type="hidden" name="reportId" value="<%= reportId %>">
        <label for="commentText">Add a Comment</label>
        <textarea id="commentText" name="commentText" rows="3" required></textarea>
        <button type="submit" class="btn">Add Comment</button>
    </form>
<%
}
%>

<jsp:include page="footer.jsp" />
