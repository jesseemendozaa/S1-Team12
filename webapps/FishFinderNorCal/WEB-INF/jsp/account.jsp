<%@ page contentType="text/html;charset=UTF-8" %>
<jsp:include page="header.jsp" />

<h1>My Account</h1>

<div class="account-info" style="margin-bottom: 1.5rem;">
    <p><strong>Member since:</strong> <%= request.getAttribute("userCreatedAt") %></p>
    <p><strong>Account status:</strong> <span class="badge badge-active"><%= request.getAttribute("userStatus") %></span></p>
</div>

<form method="post" action="${pageContext.request.contextPath}/account" class="form-card">
    <div class="form-group">
        <label for="username">Username</label>
        <input type="text" id="username" name="username" value="<%= request.getAttribute("userUsername") != null ? request.getAttribute("userUsername") : "" %>" required />
    </div>

    <div class="form-group">
        <label for="email">Email</label>
        <input type="email" id="email" name="email" value="<%= request.getAttribute("userEmail") != null ? request.getAttribute("userEmail") : "" %>" required />
    </div>

    <hr />

    <h3>Change Password</h3>
    <p><em>Leave blank to keep your current password.</em></p>

    <div class="form-group">
        <label for="currentPassword">Current Password</label>
        <input type="password" id="currentPassword" name="currentPassword" />
    </div>

    <div class="form-group">
        <label for="newPassword">New Password</label>
        <input type="password" id="newPassword" name="newPassword" minlength="6" />
    </div>

    <button type="submit" class="btn btn-primary">Update Account</button>
</form>

<div class="danger-zone" style="border: 2px solid var(--danger); padding: 1rem; margin-top: 2rem; border-radius: 8px;">
    <h3>Delete Account</h3>
    <p>This will permanently deactivate your account.</p>
    <form method="post" action="${pageContext.request.contextPath}/account">
        <input type="hidden" name="action" value="delete" />
        <button type="submit" class="btn btn-danger" onclick="return confirm('Are you sure you want to delete your account? This action cannot be undone.');">Delete My Account</button>
    </form>
</div>

<jsp:include page="footer.jsp" />
