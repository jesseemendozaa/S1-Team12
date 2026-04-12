<%@ page import="java.util.*" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="header.jsp" />

<h1>Manage Users</h1>

<%
List<Map<String, Object>> users = (List<Map<String, Object>>) request.getAttribute("users");
if (users == null || users.isEmpty()) {
%>
    <p>No users found.</p>
<%
} else {
%>
    <table>
        <thead>
            <tr>
                <th>User</th>
                <th>Email</th>
                <th>Status</th>
                <th>Role</th>
                <th>Active Ban</th>
                <th>Actions</th>
            </tr>
        </thead>
        <tbody>
<%
    for (Map<String, Object> user : users) {
        boolean hasActiveBan = user.get("activeBanId") != null;
%>
            <tr>
                <td><strong><%= user.get("username") %></strong><br><small>ID <%= user.get("userId") %></small></td>
                <td><%= user.get("email") %></td>
                <td><%= user.get("status") %></td>
                <td><%= Boolean.TRUE.equals(user.get("isModerator")) ? "Moderator" : "User" %></td>
                <td>
<% if (hasActiveBan) { %>
                    <strong>Banned</strong><br>
                    <small><%= user.get("banReason") %></small>
<% if (user.get("banEnd") != null) { %>
                    <br><small>Until <%= user.get("banEnd") %></small>
<% } %>
<% } else { %>
                    None
<% } %>
                </td>
                <td>
<% if (hasActiveBan) { %>
                    <form method="POST" action="<%= request.getContextPath() %>/admin/users" class="inline-form">
                        <input type="hidden" name="action" value="unban">
                        <input type="hidden" name="targetUserId" value="<%= user.get("userId") %>">
                        <button type="submit" class="btn btn-secondary">Remove Ban</button>
                    </form>
<% } else if (!"deleted".equals(user.get("status"))) { %>
                    <form method="POST" action="<%= request.getContextPath() %>/admin/users" class="form-card" style="padding: 0.75rem;">
                        <input type="hidden" name="action" value="ban">
                        <input type="hidden" name="targetUserId" value="<%= user.get("userId") %>">
                        <label for="reason-<%= user.get("userId") %>">Reason</label>
                        <input type="text" id="reason-<%= user.get("userId") %>" name="reason" required>
                        <label for="banEnd-<%= user.get("userId") %>">Ends At</label>
                        <input type="datetime-local" id="banEnd-<%= user.get("userId") %>" name="banEnd">
                        <button type="submit" class="btn btn-danger">Issue Ban</button>
                    </form>
<% } %>
                </td>
            </tr>
<%
    }
%>
        </tbody>
    </table>
<%
}
%>

<jsp:include page="footer.jsp" />
